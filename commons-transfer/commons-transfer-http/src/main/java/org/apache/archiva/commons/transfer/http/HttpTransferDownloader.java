package org.apache.archiva.commons.transfer.http;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.archiva.commons.transfer.TransferAuthenticationRequiredException;
import org.apache.archiva.commons.transfer.TransferDownloader;
import org.apache.archiva.commons.transfer.TransferException;
import org.apache.archiva.commons.transfer.TransferFileFilter;
import org.apache.archiva.commons.transfer.http.dav.DavResource;
import org.apache.archiva.commons.transfer.http.dav.MultiStatus;
import org.apache.archiva.commons.transfer.http.dav.PropFindMethod;
import org.apache.archiva.commons.transfer.http.links.LinkParser;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 * HttpTransferDownloader
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class HttpTransferDownloader
    extends HttpTransferBase
    implements TransferDownloader
{
    private static final Log log = LogFactory.getLog( HttpTransferDownloader.class );

    private LinkParser linkParser = new LinkParser();

    public HttpTransferDownloader( URI baseuri )
        throws TransferException
    {
        super( baseuri );
    }

    public void download( URI uri, File outputFile )
        throws TransferException, IOException
    {
        assertURI( uri );
        URI absoluteURI = baseuri.resolve( uri );
        initProxy( absoluteURI );
        initAuth();

        String url = absoluteURI.toASCIIString();
        GetMethod getmethod = new GetMethod( url );
        try
        {
            getmethod.setFollowRedirects( true );

            int status = client.executeMethod( getmethod );

            if ( status != 200 )
            {
                triggerFailure( absoluteURI, status );
                switch ( status )
                {
                    case HttpStatus.SC_UNAUTHORIZED:
                    case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
                        throw new TransferAuthenticationRequiredException( HttpStatus.getStatusText( status ) + " - "
                            + uri );
                    default:
                        throw new TransferException( "Error[" + status + ":" + HttpStatus.getStatusText( status )
                            + "] " + uri );
                }
            }

            long expectedSize = getmethod.getResponseContentLength();

            InputStream input = null;
            OutputStream output = null;

            try
            {
                input = getmethod.getResponseBodyAsStream();
                output = new FileOutputStream( outputFile );

                transfer( absoluteURI, input, output, expectedSize );
            }
            finally
            {
                IOUtils.closeQuietly( output );
                IOUtils.closeQuietly( input );
            }
        }
        finally
        {
            getmethod.releaseConnection();
        }
    }

    private Set<String> getDavListing( URI absoluteURI, TransferFileFilter filter )
        throws HttpException, IOException
    {
        PropFindMethod method = new PropFindMethod( absoluteURI );
        try
        {
            int status = client.executeMethod( method );
            if ( ( status == HttpStatus.SC_MULTI_STATUS ) || ( status == HttpStatus.SC_OK ) )
            {
                MultiStatus multistatus = method.getMultiStatus();
                Set<String> listing = new HashSet<String>();

                for ( DavResource resource : multistatus.getResources() )
                {
                    if ( resource.isCollection() )
                    {
                        continue;
                    }
                    String href = resource.getHref();

                    int idx = href.lastIndexOf( '/' );
                    if ( idx < 0 )
                    {
                        listing.add( href );
                    }
                    else
                    {
                        listing.add( href.substring( idx + 1 ) );
                    }
                }

                return listing;
            }

            return Collections.emptySet();
        }
        finally
        {
            method.releaseConnection();
        }
    }

    private Set<String> getHttpListing( URI absoluteURI, TransferFileFilter filter )
        throws HttpException, IOException, TransferException
    {
        GetMethod getmethod = new GetMethod( absoluteURI.toASCIIString() );
        try
        {
            getmethod.setFollowRedirects( true );

            int status = client.executeMethod( getmethod );

            if ( status != 200 )
            {
                triggerFailure( absoluteURI, status );
                switch ( status )
                {
                    case HttpStatus.SC_NOT_FOUND:
                        log.info( "Listing Not Found (404) for " + absoluteURI.toASCIIString() );
                        return Collections.emptySet();
                    case HttpStatus.SC_UNAUTHORIZED:
                    case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
                        throw new TransferAuthenticationRequiredException( HttpStatus.getStatusText( status ) + " - "
                            + absoluteURI.toASCIIString() );
                    default:
                        throw new TransferException( "Error[" + status + ":" + HttpStatus.getStatusText( status )
                            + "] " + absoluteURI.toASCIIString() );
                }
            }

            InputStream input = null;

            try
            {
                input = getmethod.getResponseBodyAsStream();
                return linkParser.collectLinks( absoluteURI, input, filter );
            }
            catch ( SAXException e )
            {
                log.warn( "Unable to parse " + absoluteURI.toASCIIString() );
                return Collections.emptySet();
            }
            finally
            {
                IOUtils.closeQuietly( input );
            }
        }
        finally
        {
            getmethod.releaseConnection();
        }
    }

    public Set<String> getListing( URI uri, TransferFileFilter filter )
        throws TransferException, IOException
    {
        assertURI( uri );
        URI absoluteURI = baseuri.resolve( uri );
        initProxy( absoluteURI );
        initAuth();

        if ( isDav )
        {
            return getDavListing( absoluteURI, filter );
        }
        else
        {
            return getHttpListing( absoluteURI, filter );
        }
    }

}
