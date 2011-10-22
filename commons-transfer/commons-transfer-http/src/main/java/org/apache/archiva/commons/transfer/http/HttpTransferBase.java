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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.archiva.commons.transfer.AbstractTransferBase;
import org.apache.archiva.commons.transfer.TransferNetworkProxy;
import org.apache.archiva.commons.transfer.TransferStore;
import org.apache.archiva.commons.transfer.defaults.DefaultTransferStore;
import org.apache.archiva.commons.transfer.http.auth.HttpAuthStore;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * HttpTransferBase
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class HttpTransferBase
    extends AbstractTransferBase
{
    private static final Log LOG = LogFactory.getLog( HttpTransferBase.class );

    private TransferStore transferStore;

    private HttpAuthStore httpAuthStore;

    protected URI baseuri;

    protected HttpClient client;

    protected boolean isDav;

    private Map<String, String> protocolMapping = new TreeMap<String, String>();

    public HttpTransferBase( URI baseuri )
    {
        this.baseuri = baseuri;

        if ( !baseuri.isAbsolute() )
        {
            throw new IllegalArgumentException( "Base URI must be absolute (got \"" + baseuri.toASCIIString() + "\")." );
        }

        protocolMapping.put( "http", "http" );
        protocolMapping.put( "https", "https" );
        protocolMapping.put( "ssl", "https" );
        protocolMapping.put( "dav", "http" );
        protocolMapping.put( "davs", "https" );
        protocolMapping.put( "dav-https", "https" );
        protocolMapping.put( "dav-http", "http" );

        String protocol = baseuri.getScheme();

        if ( !protocolMapping.containsKey( protocol ) )
        {
            StringBuffer msg = new StringBuffer();
            msg.append( "Base URI for " ).append( getClass().getName() );
            msg.append( " must use one of the following protocols " );
            for ( String acceptableProtocol : protocolMapping.keySet() )
            {
                msg.append( "[" ).append( acceptableProtocol ).append( "] " );
            }
            msg.append( ". (got \"" ).append( baseuri.toASCIIString() ).append( "\")" );

            throw new IllegalArgumentException( msg.toString() );
        }
        else
        {
            // We might need to map the protocol.
            String actualProtocol = protocolMapping.get( protocol );
            if ( !actualProtocol.equals( protocol ) )
            {
                String tmpuri = baseuri.toASCIIString();
                try
                {
                    this.baseuri = new URI( actualProtocol + tmpuri.substring( protocol.length() ) );
                }
                catch ( URISyntaxException e )
                {
                    throw new IllegalArgumentException( "Unable to map protocol [" + protocol + "] to ["
                        + actualProtocol + "] with uri " + baseuri.toASCIIString(), e );
                }
            }
        }

        if ( !baseuri.toASCIIString().endsWith( "/" ) )
        {
            throw new IllegalArgumentException( "Base URI must end in '/' character \"" + baseuri + "\"" );
        }

        this.client = new HttpClient();
        this.client.getParams().setAuthenticationPreemptive( true );
        initAuth();
        initProxy( baseuri );

        this.isDav = isWebDavCapableServer();
    }

    protected void assertURI( URI uri )
    {
        if ( uri.isAbsolute() )
        {
            String testurl = uri.toASCIIString();
            String baseurl = baseuri.toASCIIString();
            if ( !testurl.startsWith( baseurl ) )
            {
                throw new IllegalArgumentException( "Invalid request with URI " + testurl + ", " + getClass().getName()
                    + " expects fully absolute URIs to be part of the base URI " + baseurl );
            }
        }
    }

    public HttpAuthStore getHttpAuthStore()
    {
        if ( httpAuthStore == null )
        {
            httpAuthStore = new HttpAuthStore();
        }
        return httpAuthStore;
    }

    public TransferStore getTransferStore()
    {
        if ( transferStore == null )
        {
            transferStore = DefaultTransferStore.getDefault();
        }
        return transferStore;
    }

    public void initAuth()
    {
        client.getParams().setParameter( CredentialsProvider.PROVIDER, getHttpAuthStore() );
    }

    public void initProxy( URI uri )
    {
        TransferNetworkProxy proxy = getTransferStore().getNetworkProxy();

        // Skip if no proxy is defined.
        if ( proxy == null )
        {
            LOG.debug( "Not proxying [" + uri + "], NetworkProxy is null" );
            unsetProxy();
            return;
        }

        // Skip if proxy is not enabled.
        if ( !proxy.isEnabled() )
        {
            LOG.debug( "Not proxying [" + uri + "], NetworkProxy.enabled is false" );
            return;
        }

        // Skip if proxy is invalid.
        if ( !proxy.valid() )
        {
            LOG.debug( "Not proxying [" + uri + "], NetworkProxy is invalid" );
            return;
        }

        String hostname = uri.getHost();

        // Do settings exist?
        if ( proxy.needsProxy( hostname ) )
        {
            LOG.debug( "Setting up http proxy: " + proxy.getHost() + ":" + proxy.getPort() );
            ProxyHost netproxy = new ProxyHost( proxy.getHost(), proxy.getPort() );
            client.getHostConfiguration().setProxyHost( netproxy );

            // Do auth settings exist?
            if ( proxy.authExists() )
            {
                Credentials credentials = new UsernamePasswordCredentials( proxy.getUsername(), proxy.getPassword() );
                AuthScope authScope = new AuthScope( proxy.getHost(), proxy.getPort() );
                client.getState().setProxyCredentials( authScope, credentials );
                LOG.debug( "Setting up http proxy credentials for : " + proxy.getUsername() );
            }
        }
        else
        {
            LOG.debug( "Not proxying [" + uri + "], proxy.needsProxy(\"" + hostname + "\") is false" );
        }
    }

    public boolean isDav()
    {
        return isDav;
    }

    public boolean isWebDavCapableServer()
    {
        OptionsMethod method = new OptionsMethod( baseuri.toASCIIString() );
        try
        {
            int status = client.executeMethod( method );
            if ( status == HttpStatus.SC_OK )
            {
                // Test DAV Header Options
                boolean supportsDav1 = false;

                Header davOptionHeader = method.getResponseHeader( "dav" );
                if ( davOptionHeader != null )
                {
                    String davSupport[] = StringUtils.split( davOptionHeader.getValue(), "," );
                    for ( String support : davSupport )
                    {
                        support = support.trim();
                        if ( "1".equals( support ) )
                        {
                            supportsDav1 = true;
                        }
                    }
                }

                if ( !supportsDav1 )
                {
                    LOG.info( "No DAV Support: " + baseuri.toASCIIString() );
                    return false;
                }

                // Not validate.
                String requiredMethods[] = new String[] { "HEAD", "GET", "MKCOL", "PROPFIND", "PUT", "OPTIONS" };

                boolean supportsRequired = true;
                for ( String required : requiredMethods )
                {
                    if ( !method.isAllowed( required ) )
                    {
                        LOG.info( "No " + required + " Support: " + baseuri.toASCIIString() );
                        supportsRequired = false;
                    }
                }

                return supportsRequired;
            }
        }
        catch ( HttpException e )
        {
            LOG.info( "Unable to get OPTIONS from " + baseuri.toASCIIString(), e );
        }
        catch ( IOException e )
        {
            LOG.info( "Unable to get OPTIONS from " + baseuri.toASCIIString(), e );
        }
        finally
        {
            method.releaseConnection();
        }

        return false;
    }

    public void setHttpAuthStore( HttpAuthStore httpAuthStore )
    {
        this.httpAuthStore = httpAuthStore;
    }

    public void setTransferStore( TransferStore transferStore )
    {
        this.transferStore = transferStore;
    }

    public void unsetProxy()
    {
        client.getHostConfiguration().setProxyHost( null );
    }
}
