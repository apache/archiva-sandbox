package org.apache.archiva.metadata.repository.cassandra;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.archiva.metadata.repository.AbstractMetadataRepositoryTest;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;

/**
 * @author Olivier Lamy
 */
public class CassandraMetadataRepositoryTest
    extends AbstractMetadataRepositoryTest
{
    private Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    @Named( value = "archivaEntityManagerFactory#cassandra" )
    CassandraEntityManagerFactory cassandraEntityManagerFactory;

    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();

        File directory = new File( "target/test-repositories" );
        if ( directory.exists() )
        {
            FileUtils.deleteDirectory( directory );
        }

        this.repository = new CassandraMetadataRepository( null, null, cassandraEntityManagerFactory.getKeyspace() );
    }
}
