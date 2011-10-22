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

import org.apache.commons.httpclient.auth.AuthScope;

/**
 * HttpAuthKey
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class HttpAuthKey
{
    /** Required: host for the auth */
    private String host;

    /** Required: port for the auth */
    private int port;

    /** Optional: the realm for the auth */
    private String realm;

    /** Optional: the scheme (basic, digest, ntlm, etc...) */
    private String scheme;

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final HttpAuthKey other = (HttpAuthKey) obj;
        if ( host == null )
        {
            if ( other.host != null )
            {
                return false;
            }
        }
        else if ( !host.equals( other.host ) )
        {
            return false;
        }
        if ( port != other.port )
        {
            return false;
        }
        if ( realm == null )
        {
            if ( other.realm != null )
            {
                return false;
            }
        }
        else if ( !realm.equals( other.realm ) )
        {
            return false;
        }
        if ( scheme == null )
        {
            if ( other.scheme != null )
            {
                return false;
            }
        }
        else if ( !scheme.equals( other.scheme ) )
        {
            return false;
        }
        return true;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public String getRealm()
    {
        return realm;
    }

    public String getScheme()
    {
        return scheme;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( host == null ) ? 0 : host.hashCode() );
        result = prime * result + port;
        result = prime * result + ( ( realm == null ) ? 0 : realm.hashCode() );
        result = prime * result + ( ( scheme == null ) ? 0 : scheme.hashCode() );
        return result;
    }

    public void setHost( String host )
    {
        this.host = host;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    public void setRealm( String realm )
    {
        this.realm = realm;
    }

    public void setScheme( String scheme )
    {
        this.scheme = scheme;
    }

    public AuthScope toAuthScope()
    {
        return new AuthScope( host, port, realm, scheme );
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();

        buf.append( this.host );
        buf.append( ':' ).append( this.port );
        buf.append( '/' ).append( this.scheme );
        buf.append( '/' ).append( this.realm );

        return buf.toString();
    }
}
