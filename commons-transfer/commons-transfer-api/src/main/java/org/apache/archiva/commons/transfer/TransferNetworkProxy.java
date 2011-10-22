package org.apache.archiva.commons.transfer;

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

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Network Proxy model information.
 * 
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class TransferNetworkProxy
{

    protected boolean enabled = false;

    protected String host;

    protected int port;

    protected boolean authEnabled = false;

    protected String username;

    protected String password;

    protected Set<String> noProxyHosts = new TreeSet<String>();

    protected Set<Pattern> noProxyPatterns = new HashSet<Pattern>();

    public void addNoProxyHost( String host )
    {
        noProxyHosts.add( host );
        recalcPatterns();
    }

    public boolean authExists()
    {
        return ( StringUtils.isNotBlank( username ) && StringUtils.isNotBlank( password ) );
    }

    public String getHost()
    {
        return host;
    }

    public Set<String> getNoProxyHosts()
    {
        return noProxyHosts;
    }

    public String getPassword()
    {
        return password;
    }

    public int getPort()
    {
        return port;
    }

    public String getUsername()
    {
        return username;
    }

    public boolean isAuthEnabled()
    {
        return authEnabled;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Tests the hostname to see if it needs to be proxied (or not)
     * Applies the the {@link #getNoProxyHosts()} to the test.
     * 
     * @param hostname the hostname to test against.
     * @return true if the hostname needs to be proxied. false if it appears within the {@link #getNoProxyHosts()} collection.
     */
    public boolean needsProxy( String hostname )
    {
        if ( !enabled )
        {
            return false;
        }

        Matcher mat;
        for ( Pattern pat : noProxyPatterns )
        {
            mat = pat.matcher( hostname );
            if ( mat.matches() )
            {
                return false;
            }
        }
        return true;
    }

    private void recalcPatterns()
    {
        noProxyPatterns.clear();
        for ( String host : noProxyHosts )
        {
            String hostregex = host.replaceAll( "\\.", "\\." );
            hostregex = host.replaceAll( "\\*", ".*" );
            Pattern pat = Pattern.compile( hostregex, Pattern.CASE_INSENSITIVE );
            noProxyPatterns.add( pat );
        }
    }

    public void removeNoProxyHost( String host )
    {
        if ( noProxyHosts.remove( host ) )
        {
            recalcPatterns();
        }
    }

    public void setAuthEnabled( boolean authEnabled )
    {
        this.authEnabled = authEnabled;
    }

    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
    }

    public void setHost( String host )
    {
        this.host = host;
    }

    public void setNoProxyHosts( Set<String> noProxyHosts )
    {
        this.noProxyHosts = noProxyHosts;
        recalcPatterns();
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String toDump()
    {
        StringBuffer dbg = new StringBuffer();

        dbg.append( "  Proxy / enabled: " );
        dbg.append( isEnabled() );
        dbg.append( "\n        / host: " );
        dbg.append( StringUtils.defaultString( getHost(), "<null>" ) );
        dbg.append( "\n        / port: " ).append( getPort() );
        dbg.append( "\n        / auth-enabled: " ).append( isAuthEnabled() );
        dbg.append( "\n        / username: " );
        dbg.append( StringUtils.defaultString( getUsername(), "<null>" ) );
        dbg.append( "\n        / password: " );
        String rawpass = getPassword();
        if ( rawpass == null )
        {
            dbg.append( "<null>" );
        }
        else
        {
            int len = rawpass.length();
            for ( int i = 0; i < len; i++ )
            {
                dbg.append( '*' );
            }
        }
        dbg.append( "\n        / no-proxy: (" );
        dbg.append( getNoProxyHosts().size() + " entries)" );
        for ( String host : getNoProxyHosts() )
        {
            dbg.append( "\n                  / " ).append( host );
        }
        dbg.append( "\n" );

        return dbg.toString();
    }

    public boolean valid()
    {
        return ( StringUtils.isNotBlank( host ) && ( port > 0 ) && ( port < 65535 ) );
    }
}