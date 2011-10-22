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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Stack;

import org.apache.archiva.commons.transfer.MimeTypes;
import org.apache.archiva.commons.transfer.TransferException;
import org.apache.archiva.commons.transfer.TransferUploader;
import org.apache.archiva.commons.transfer.http.dav.DavResource;
import org.apache.archiva.commons.transfer.http.dav.MkColMethod;
import org.apache.archiva.commons.transfer.http.dav.MultiStatus;
import org.apache.archiva.commons.transfer.http.dav.PropFindMethod;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * HttpTransferUploader
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class HttpTransferUploader
    extends HttpTransferBase
    implements TransferUploader
{
    private static final Log LOG = LogFactory.getLog( HttpTransferUploader.class );

    private MimeTypes mimeTypes;

    public HttpTransferUploader( URI baseuri )
        throws TransferException
    {
        super( baseuri );
        mimeTypes = new MimeTypes();
    }

    public boolean davCollectionExists( URI uri )
        throws TransferException
    {
        PropFindMethod method = new PropFindMethod( uri );
        try
        {
            method.setDepth( 1 );
            int status = client.executeMethod( method );
            if ( ( status == HttpStatus.SC_MULTI_STATUS ) || ( status == HttpStatus.SC_OK ) )
            {
                MultiStatus multistatus = method.getMultiStatus();
                if ( multistatus == null )
                {
                    return false;
                }

                DavResource resource = multistatus.getResource( uri.getPath() );
                if ( resource == null )
                {
                    return false;
                }

                return resource.isCollection();
            }

            if ( status == HttpStatus.SC_BAD_REQUEST )
            {
                throw new TransferException( "Bad HTTP Request (400) during PROPFIND on \"" + uri.toASCIIString()
                    + "\"" );
            }
        }
        catch ( HttpException e )
        {
            LOG.info( "Can't determine if collection exists: " + uri, e );
        }
        catch ( IOException e )
        {
            LOG.info( "Can't determine if collection exists: " + uri, e );
        }
        finally
        {
            method.releaseConnection();
        }
        return false;
    }

    public void davCreateCollection( URI uri )
        throws TransferException
    {
        MkColMethod method = new MkColMethod( uri );
        try
        {
            int status = client.executeMethod( method );
            if ( status == HttpStatus.SC_CREATED )
            {
                return;
            }

            throw new TransferException( "Unable to create collection (" + status + "/"
                + HttpStatus.getStatusText( status ) + "): " + uri.toASCIIString() );
        }
        catch ( HttpException e )
        {
            throw new TransferException( "Unable to create collection: " + uri.toASCIIString(), e );
        }
        catch ( IOException e )
        {
            throw new TransferException( "Unable to create collection: " + uri.toASCIIString(), e );
        }
        finally
        {
            method.releaseConnection();
        }
    }

    /**
     * Collect the stack of missing paths.
     * 
     * @param absoluteURI the abosoluteURI to start from.
     * @return
     * @throws TransferException 
     */
    public Stack<URI> davGetMissingPaths( URI targetURI )
        throws TransferException
    {
        Stack<URI> missingPaths = new Stack<URI>();
        URI currentURI = targetURI;
        if ( !currentURI.getPath().endsWith( "/" ) )
        {
            try
            {
                currentURI = new URI( targetURI.toASCIIString() + "/" );
            }
            catch ( URISyntaxException e )
            {
                LOG.info( "Should never happen.", e );
            }
        }

        boolean done = false;
        while ( !done )
        {
            if ( targetURI.equals( baseuri ) )
            {
                done = true;
                break;
            }

            if ( davCollectionExists( currentURI ) )
            {
                done = true;
                break;
            }

            missingPaths.push( currentURI );

            currentURI = currentURI.resolve( "../" ).normalize();
        }

        return missingPaths;
    }

    /**
     * HTTP RFC 2616 section 9.6 "PUT": "If an existing resource is modified,
     * either the 200 (OK) or 204 (No Content) response codes SHOULD be sent
     * to indicate successful completion of the request." 
     */
    private boolean isSuccessfulPUT( int status )
    {
        return ( ( status == HttpStatus.SC_CREATED ) || ( status == HttpStatus.SC_OK ) || ( status == HttpStatus.SC_NO_CONTENT ) );
    }

    private int putFile( File sourceFile, URI absoluteURI )
        throws HttpException, IOException
    {
        PutMethod method = new PutMethod( absoluteURI.toASCIIString() );
        try
        {
            String contentType = mimeTypes.getMimeType( sourceFile.getName() );
            FileRequestEntity requestEntity = new FileRequestEntity( sourceFile, contentType );
            method.setRequestEntity( requestEntity );
            int status = client.executeMethod( method );

            LOG.info( "PUT Status: " + status + "/" + HttpStatus.getStatusText( status ) );
            return status;
        }
        finally
        {
            method.releaseConnection();
        }
    }

    public void upload( File sourceFile, URI uri )
        throws TransferException, IOException
    {
        assertURI( uri );

        URI absoluteURI = baseuri.resolve( uri );

        if ( !isDav )
        {
            throw new TransferException( "Can't upload (not WebDav capable): " + absoluteURI.toASCIIString() );
        }

        // Try to put file first.
        int status = putFile( sourceFile, absoluteURI );
        if ( isSuccessfulPUT( status ) )
        {
            // Expected path.
            return;
        }

        // Problem. Check that the collections exist.
        if ( status == HttpStatus.SC_CONFLICT )
        {
            URI parentURI = absoluteURI.resolve( "./" ).normalize();
            Stack<URI> missingPaths = davGetMissingPaths( parentURI );

            if ( missingPaths.empty() )
            {
                throw new TransferException( "Unable to upload (Conflict, collections exist) file "
                    + sourceFile.getAbsolutePath() + " to " + absoluteURI.toASCIIString() );
            }

            while ( !missingPaths.empty() )
            {
                URI missingPath = missingPaths.pop();
                davCreateCollection( missingPath );
            }

            status = putFile( sourceFile, absoluteURI );
            if ( isSuccessfulPUT( status ) )
            {
                // Expected Good result.
                return;
            }
        }

        throw new TransferException( "Unable to upload (" + status + "/" + HttpStatus.getStatusText( status )
            + ") file " + sourceFile.getAbsolutePath() + " to " + absoluteURI.toASCIIString() );
    }

}
