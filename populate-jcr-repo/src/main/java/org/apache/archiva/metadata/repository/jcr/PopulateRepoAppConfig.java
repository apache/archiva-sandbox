package org.apache.archiva.metadata.repository.jcr;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.archiva.configuration.ArchivaConfiguration;
import org.apache.archiva.configuration.Configuration;
import org.apache.archiva.configuration.ConfigurationListener;
import org.apache.archiva.configuration.IndeterminateConfigurationException;
import org.apache.archiva.configuration.ManagedRepositoryConfiguration;
import org.codehaus.plexus.registry.RegistryException;
import org.codehaus.plexus.registry.RegistryListener;

import java.io.File;

public class PopulateRepoAppConfig
    implements ArchivaConfiguration
{
    private Configuration configuration = new Configuration();

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void save( Configuration configuration )
        throws RegistryException, IndeterminateConfigurationException
    {
        throw new UnsupportedOperationException();
    }

    public boolean isDefaulted()
    {
        throw new UnsupportedOperationException();
    }

    public void addListener( ConfigurationListener listener )
    {
        throw new UnsupportedOperationException();
    }

    public void removeListener( ConfigurationListener listener )
    {
        throw new UnsupportedOperationException();
    }

    public void addChangeListener( RegistryListener listener )
    {
        throw new UnsupportedOperationException();
    }

    public void addManagedRepo( String id, File location )
    {
        ManagedRepositoryConfiguration config = new ManagedRepositoryConfiguration();
        config.setId( id );
        config.setLocation( location.getAbsolutePath() );
        configuration.addManagedRepository( config );
    }
}
