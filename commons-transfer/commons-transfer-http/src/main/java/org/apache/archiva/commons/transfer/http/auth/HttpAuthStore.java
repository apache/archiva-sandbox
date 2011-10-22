package org.apache.archiva.commons.transfer.http.auth;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.archiva.commons.transfer.TransferStore;
import org.apache.archiva.commons.transfer.defaults.DefaultTransferStore;
import org.apache.archiva.commons.transfer.http.auth.ui.NetworkAuthDialog;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.auth.NTLMScheme;
import org.apache.commons.httpclient.auth.RFC2617Scheme;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * HttpAuthStore
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class HttpAuthStore
    implements CredentialsProvider
{
    private static final String NET_AUTH = "net.auth.";

    private static final Log LOG = LogFactory.getLog( HttpAuthStore.class );

    private TransferStore transferStore;

    private Map<HttpAuthKey, Credentials> auths;

    private List<AuthListener> listeners = new ArrayList<AuthListener>();

    public void addAuth( HttpNTLMAuth auth )
    {
        String key = toKey( auth.getKey() );
        getAuths().put( auth.getKey(), auth.getCredentials() );
        if ( auth.isPersisted() )
        {
            storeKey( key, auth.getKey() );
            transferStore.setString( key + ".domain", auth.getCredentials().getDomain() );
            transferStore.setString( key + ".username", auth.getCredentials().getUsername() );
            transferStore.setPassword( key + ".password", auth.getCredentials().getPassword() );
        }
    }

    public void addAuth( HttpSimpleAuth auth )
    {
        String key = toKey( auth.getKey() );
        getAuths().put( auth.getKey(), auth.getCredentials() );
        if ( auth.isPersisted() )
        {
            storeKey( key, auth.getKey() );
            transferStore.setString( key + ".username", auth.getCredentials().getUsername() );
            transferStore.setPassword( key + ".password", auth.getCredentials().getPassword() );
        }
    }

    public void addAuthListener( AuthListener listener )
    {
        this.listeners.add( listener );
    }

    private String clean( String name )
    {
        int len = name.length();
        StringBuffer ret = new StringBuffer();
        for ( int i = 0; i < len; i++ )
        {
            char c = name.charAt( i );
            if ( Character.isLetterOrDigit( c ) )
            {
                ret.append( c );
            }
        }
        return ret.toString();
    }

    private void fireAuthCredentialsProvided( HttpAuthKey authKey, Credentials credentials, boolean persisted )
    {
        for ( AuthListener listener : listeners )
        {
            listener.authCredentialsProvided( authKey, credentials, persisted );
        }
    }

    private void fireAuthRequested( HttpAuthKey authKey )
    {
        for ( AuthListener listener : listeners )
        {
            listener.authRequested( authKey );
        }
    }

    public synchronized Map<HttpAuthKey, Credentials> getAuths()
    {
        if ( auths == null )
        {
            auths = new HashMap<HttpAuthKey, Credentials>();
            loadAuths();
        }

        return auths;
    }

    public Map<AuthScope, UsernamePasswordCredentials> getAuthScopes()
    {
        Map<AuthScope, UsernamePasswordCredentials> authscopes = new HashMap<AuthScope, UsernamePasswordCredentials>();

        for ( HttpAuthKey authkey : getAuths().keySet() )
        {
            AuthScope scope = new AuthScope( authkey.getHost(), authkey.getPort(), authkey.getRealm() );
            Credentials rawcreds = getAuths().get( authkey );
            if ( rawcreds instanceof UndefinedCredentials )
            {
                String storeKey = ( (UndefinedCredentials) rawcreds ).getStoreKey();

                // Make known.
                SimpleCredentials simpleCreds = new SimpleCredentials();
                simpleCreds.setUsername( getTransferStore().getString( NET_AUTH + storeKey + ".username", null ) );
                simpleCreds.setPassword( getTransferStore().getPassword( NET_AUTH + storeKey + ".password" ) );

                authscopes.put( scope, simpleCreds.toUsernamePasswordCredentials() );
            }
            else if ( rawcreds instanceof SimpleCredentials )
            {
                authscopes.put( scope, ( (SimpleCredentials) rawcreds ).toUsernamePasswordCredentials() );
            }
        }

        return authscopes;
    }

    public Credentials getCredentials( AuthScheme authscheme, String host, int port, boolean proxy )
        throws CredentialsNotAvailableException
    {
        if ( authscheme == null )
        {
            return null;
        }

        HttpAuthKey authKey = new HttpAuthKey();
        authKey.setHost( host );
        authKey.setPort( port );
        authKey.setRealm( authscheme.getRealm() );
        authKey.setScheme( authscheme.getSchemeName() );

        fireAuthRequested( authKey );

        if ( authscheme instanceof NTLMScheme )
        {
            return getNTLMCredentials( authKey, proxy );
        }
        else if ( authscheme instanceof RFC2617Scheme )
        {
            return getRFC2617Credentials( authKey, proxy );
        }
        else
        {
            throw new CredentialsNotAvailableException( "Unsupported authentication scheme: "
                + authscheme.getClass().getName() );
        }
    }

    private Credentials getNTLMCredentials( HttpAuthKey authKey, boolean proxy )
        throws CredentialsNotAvailableException
    {
        // TODO: Create NTLM Auth Dialog to gather Domain / User / Password information.
        // String domain = getDomain();
        // String user = getUser();
        // String password = getPassword();
        //        
        // return new NTCredentials(user, password, host, domain);

        throw new CredentialsNotAvailableException( "NTLM (NT Lan Manager) Authentication not yet supported." );
    }

    private Credentials getRFC2617Credentials( HttpAuthKey authKey, boolean proxy )
        throws CredentialsNotAvailableException
    {
        try
        {
            if ( getAuths().containsKey( authKey ) )
            {
                Credentials creds = getAuths().get( authKey );

                // Loaded from disk, but type unknown.
                if ( creds instanceof UndefinedCredentials )
                {
                    String storeKey = ( (UndefinedCredentials) creds ).getStoreKey();

                    // Make known.
                    SimpleCredentials simpleCreds = new SimpleCredentials();
                    simpleCreds.setUsername( getTransferStore().getString( NET_AUTH + storeKey + ".username", null ) );
                    simpleCreds.setPassword( getTransferStore().getPassword( NET_AUTH + storeKey + ".password" ) );

                    getAuths().put( authKey, simpleCreds );

                    fireAuthCredentialsProvided( authKey, simpleCreds, true );

                    return simpleCreds.toUsernamePasswordCredentials();
                }
                else if ( creds instanceof SimpleCredentials )
                {
                    fireAuthCredentialsProvided( authKey, creds, true );

                    return ( (SimpleCredentials) creds ).toUsernamePasswordCredentials();
                }
            }
        }
        catch ( ClassCastException e )
        {
            LOG.info( "Unable to use credentials from other auth scheme." );
        }

        if ( getTransferStore().isInteractive() )
        {
            HttpSimpleAuth auth = new HttpSimpleAuth();
            auth.setPersisted( false );
            auth.setKey( authKey );

            // Popup a dialog to gather auth.
            NetworkAuthDialog dialog = new NetworkAuthDialog();

            HttpSimpleAuth useauth = dialog.getAuth( auth );
            if ( useauth == null )
            {
                throw new CredentialsNotAvailableException( "Authentication credentials not provided." );
            }

            fireAuthCredentialsProvided( useauth.getKey(), useauth.getCredentials(), useauth.isPersisted() );

            if ( useauth.isPersisted() )
            {
                addAuth( useauth );
                save();
            }

            SimpleCredentials credentials = useauth.getCredentials();
            return credentials.toUsernamePasswordCredentials();
        }

        throw new CredentialsNotAvailableException( "No Authentication Provided." );
    }

    public TransferStore getTransferStore()
    {
        if ( transferStore == null )
        {
            transferStore = DefaultTransferStore.getDefault();
        }
        return transferStore;
    }

    private void loadAuths()
    {
        HttpAuthKey authKey;

        Set<String> seenKeys = new HashSet<String>();
        Set<String> netAuths = getTransferStore().getPrefixedKeys( NET_AUTH );
        for ( String rawkey : netAuths )
        {
            int idx = rawkey.lastIndexOf( '.' );
            String key = rawkey.substring( NET_AUTH.length(), idx );
            //            LOG.info("Auth [" + rawkey + "] --> " + key);
            if ( seenKeys.contains( key ) )
            {
                // Processed already, skip
                continue;
            }
            authKey = loadKey( key );
            auths.put( authKey, new UndefinedCredentials( key ) );
            seenKeys.add( key );
        }
    }

    private HttpAuthKey loadKey( String key )
    {
        TransferStore store = getTransferStore();
        HttpAuthKey authkey = new HttpAuthKey();
        authkey.setHost( store.getString( NET_AUTH + key + ".host", "" ) );
        authkey.setPort( store.getInt( NET_AUTH + key + ".port", 80 ) );
        authkey.setScheme( store.getString( NET_AUTH + key + ".scheme", "" ) );
        authkey.setRealm( store.getString( NET_AUTH + key + ".realm", "" ) );
        return authkey;
    }

    public void removeAuthListener( AuthListener listener )
    {
        this.listeners.remove( listener );
    }

    private void save()
    {
        try
        {
            getTransferStore().save();
        }
        catch ( IOException e )
        {
            LOG.warn( e.getMessage(), e );
        }
    }

    public void setTransferStore( TransferStore transferStore )
    {
        this.transferStore = transferStore;
    }

    private void storeKey( String key, HttpAuthKey authKey )
    {
        transferStore.setString( key + ".host", authKey.getHost() );
        transferStore.setInt( key + ".port", authKey.getPort() );
        transferStore.setString( key + ".scheme", authKey.getScheme() );
        transferStore.setString( key + ".realm", authKey.getRealm() );
    }

    private String toKey( HttpAuthKey authkey )
    {
        return toKey( authkey.getHost(), authkey.getPort(), authkey.getRealm() );
    }

    private String toKey( String host, int port, String realm )
    {
        StringBuffer key = new StringBuffer();
        key.append( NET_AUTH );
        key.append( host );
        key.append( '.' ).append( port );
        key.append( '.' ).append( clean( realm ) );
        return key.toString();
    }
}
