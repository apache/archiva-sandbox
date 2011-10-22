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

import java.io.IOException;
import java.util.Set;

/**
 * The TransferStore interface.
 * 
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public interface TransferStore
{

    public abstract boolean getBoolean( String key, boolean defaultvalue );

    public abstract int getInt( String key );

    public abstract int getInt( String key, int defaultvalue );

    public abstract TransferNetworkProxy getNetworkProxy();

    public abstract String getPassword( String key );

    public abstract Set<String> getPrefixedKeys( String prefix );

    public abstract String getString( String key, String defaultvalue );

    public abstract boolean isInteractive();

    public abstract void load()
        throws IOException;

    public abstract void save()
        throws IOException;

    public abstract void setBoolean( String key, boolean val );

    public abstract void setInt( String key, int val );

    public abstract void setInteractive( boolean interactive );

    public abstract void setNetworkProxy( TransferNetworkProxy proxy );

    public abstract void setPassword( String key, String decrypted );

    public abstract void setString( String key, String val );
}