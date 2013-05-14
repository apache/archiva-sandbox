package org.apache.archiva.metadata.repository.jpa;

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

import org.apache.archiva.metadata.repository.jpa.model.Namespace;
import org.apache.archiva.metadata.repository.jpa.model.Repository;
import org.apache.archiva.test.utils.ArchivaSpringJUnit4ClassRunner;
import org.apache.cassandra.dht.BootStrapper;
import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author Olivier Lamy
 */
@RunWith( ArchivaSpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath*:/META-INF/spring-context.xml", "classpath*:/spring-context.xml" } )
public class RepositoriesNamespaceTest
{

    private Logger logger = LoggerFactory.getLogger( getClass() );

    //@Inject
    //private ThriftEntityManager em;
    //EntityManagerImpl em;

    @Inject
    @Named( value = "archivaEntityManagerFactory#jpa-archiva" )
    ArchivaEntityManagerFactory archivaEntityManagerFactory;

    EntityManager em;

    CassandraMetadataRepository cmr;

    @Before
    public void setup()
        throws Exception
    {
        em = archivaEntityManagerFactory.getEntityManager();
        cmr = new CassandraMetadataRepository( null, null, em );
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
        //com.alvazan.orm.api.base.Bootstrap.create( null ).createEntityManager().
        Repository r = null;
        Namespace n = null;

        try
        {

            cmr.updateNamespace( "release", "org" );

            r = em.find( Repository.class, "release" );

            Assertions.assertThat( r ).isNotNull();

            Assertions.assertThat( cmr.getRepositories()).isNotEmpty().hasSize( 1 );
            Assertions.assertThat( cmr.getNamespaces( "release" ) ).isNotEmpty().hasSize( 1 );

            n = em.find( Namespace.class, "org" );

            Assertions.assertThat( n ).isNotNull();
            Assertions.assertThat( n.getRepository() ).isNotNull();

            cmr.updateNamespace( "release", "org.apache" );

            r = em.find( Repository.class, "release" );

            Assertions.assertThat( r ).isNotNull();
            Assertions.assertThat( cmr.getNamespaces( "release" ) ).isNotEmpty().hasSize( 2 );

        }
        finally
        {
            clearReposAndNamespace();
        }
    }

    protected void clearReposAndNamespace()
        throws Exception
    {
        TypedQuery<Repository> queryR = em.createQuery( "SELECT r FROM Repository r", Repository.class );
        for ( Repository r : queryR.getResultList() )
        {
            em.remove( r );
        }

        TypedQuery<Namespace> query = em.createQuery( "SELECT n FROM Namespace n", Namespace.class );
        for ( Namespace n : query.getResultList() )
        {
            em.remove( n );
        }


        em.clear();
    }
}
