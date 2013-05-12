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

    @Before
    public void setup()
        throws Exception
    {
        em = archivaEntityManagerFactory.getEntityManager();
    }

    @After
    public void shutdown()
        throws Exception
    {
        //em.close();
        //emf.close();
    }

    @Test
    public void quicktest()
        throws Exception
    {
        try
        {
            em.clear();

            Repository repoReleases = new Repository( "releases" );

            em.persist( repoReleases );

            Repository repoSnapshots = new Repository( "snapshots" );

            em.persist( repoSnapshots );

            Repository repositoryFromData = em.find( Repository.class, "releases" );

            Assertions.assertThat( repositoryFromData ).isNotNull();
            Assertions.assertThat( repositoryFromData.getName() ).isEqualTo( "releases" );

            repositoryFromData = em.find( Repository.class, "snapshots" );

            Assertions.assertThat( repositoryFromData ).isNotNull();
            Assertions.assertThat( repositoryFromData.getId() ).isEqualTo( "snapshots" );

            //em.clear();

            TypedQuery<Repository> query = em.createQuery( "SELECT r FROM Repository r", Repository.class );

            List<Repository> repositories = query.getResultList();

            Assertions.assertThat( repositories ).isNotNull().isNotEmpty().hasSize( 2 );

            Namespace namespace = new Namespace( "org" );
            namespace.setRepository( repoReleases );

            repoReleases.getNamespaces().add( namespace );

            em.persist( namespace );

            em.persist( repoReleases );

            repositoryFromData = em.find( Repository.class, "releases" );

            Assertions.assertThat( repositoryFromData ).isNotNull();
            Assertions.assertThat( repositoryFromData.getName() ).isEqualTo( "releases" );
            //Assertions.assertThat( repositoryFromData.getNamespaces() ).isNotNull().isNotEmpty().hasSize( 1 );

            namespace = em.find( Namespace.class, "org" );
            Assertions.assertThat( namespace ).isNotNull();
            Assertions.assertThat( namespace.getRepository() ).isNotNull();

        }
        finally
        {
            clearReposAndNamespace();
        }

    }

    @Test
    public void testMetadataRepo()
        throws Exception
    {
        Repository r = null;
        Namespace n = null;
        try
        {
            CassandraMetadataRepository cmr = new CassandraMetadataRepository( null, null, em );

            cmr.updateNamespace( "release", "org" );

            r = em.find( Repository.class, "release" );

            Assertions.assertThat( r ).isNotNull();
            Assertions.assertThat( r.getNamespaces() ).isNotEmpty().hasSize( 1 );

            n = em.find( Namespace.class, "org" );

            Assertions.assertThat( n ).isNotNull();
            Assertions.assertThat( n.getRepository() ).isNotNull();
        }
        finally
        {
            //em.remove( r );
            //em.remove( n );
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
