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

import org.apache.archiva.metadata.repository.cassandra.model.Namespace;
import org.apache.archiva.metadata.repository.cassandra.model.Repository;
import org.apache.archiva.test.utils.ArchivaSpringJUnit4ClassRunner;
import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Olivier Lamy
 */
@RunWith( ArchivaSpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath*:/META-INF/spring-context.xml", "classpath*:/spring-context.xml" } )
public class RepositoriesNamespaceTest
{

    private Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    @Named( value = "archivaEntityManagerFactory#cassandra" )
    ArchivaCassandraEntityManagerFactory archivaCassandraEntityManagerFactory;


    CassandraMetadataRepository cmr;

    @Before
    public void setup()
        throws Exception
    {

        cmr = new CassandraMetadataRepository( null, null, archivaCassandraEntityManagerFactory.getKeyspace() );

    }

    @After
    public void shutdown()
        throws Exception
    {
        //em.close();
        //emf.close();
    }


    @Test
    public void testMetadataRepo()
        throws Exception
    {

        Repository r = null;
        Namespace n = null;

        try
        {

            cmr.updateNamespace( "release", "org" );

            r = cmr.getRepositoryEntityManager().get( "release" );

            Assertions.assertThat( r ).isNotNull();

            Assertions.assertThat( cmr.getRepositories() ).isNotEmpty().hasSize( 1 );
            Assertions.assertThat( cmr.getNamespaces( "release" ) ).isNotEmpty().hasSize( 1 );

            n = cmr.getNamespaceEntityManager().get( "org" + "-" + "release" );

            Assertions.assertThat( n ).isNotNull();
            Assertions.assertThat( n.getRepository() ).isNotNull();

            cmr.updateNamespace( "release", "org.apache" );

            r = cmr.getRepositoryEntityManager().get( "release" );

            Assertions.assertThat( r ).isNotNull();
            Assertions.assertThat( cmr.getNamespaces( "release" ) ).isNotEmpty().hasSize( 2 );
        }
        catch ( Exception e )
        {
            logger.error( e.getMessage(), e );
            throw e;
        }
        finally
        {
            clearReposAndNamespace();
        }
    }

    protected void clearReposAndNamespace()
        throws Exception
    {
        if ( true )
        {
            return;
        }
    }
}
