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

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;
import java.util.Properties;

/**
 * @author Olivier Lamy
 */
@RunWith( ArchivaSpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath*:/META-INF/spring-context.xml", "classpath*:/spring-context.xml" } )
public class RepositoriesNamespaceTest
{

    private static Logger LOGGER = LoggerFactory.getLogger( RepositoriesNamespaceTest.class );

    //@Inject
    //private ThriftEntityManager em;
    //EntityManagerImpl em;
    EntityManager em;

    @Before
    public void setup()
        throws Exception
    {
        LOGGER.info( "setup" );
        //CassandraHostConfigurator hostConfigurator = new CassandraHostConfigurator( "localhost:9160" );
        //Cluster cluster = HFactory.getOrCreateCluster( "ArchivaCluster", hostConfigurator );
        //Keyspace keyspace = HFactory.createKeyspace( "ArchivaKeySpace", cluster );
        //em = new EntityManagerImpl( keyspace, "org.apache.archiva.metadata.repository.jpa.model" );
        em = Persistence.createEntityManagerFactory("archiva", new Properties( )).createEntityManager();

        LOGGER.info( "end setup" );

    }

    @After
    public void shutdown()
        throws Exception
    {
        //em.close();
        //emf.close();
    }

    @Test
    public void addRepositories()
        throws Exception
    {
        Repository repo1 = new Repository( "releases" );

        em.persist( repo1 );

        Repository repo2 = new Repository( "snapshots" );

        em.persist( repo2 );

        //em.flush();

        Repository repositoryFromData = em.find( Repository.class, "releases" );

        Assertions.assertThat( repositoryFromData ).isNotNull();
        Assertions.assertThat( repositoryFromData.getName() ).isEqualTo( "releases" );

        repositoryFromData = em.find( Repository.class, "snapshots" );

        Assertions.assertThat( repositoryFromData ).isNotNull();
        Assertions.assertThat( repositoryFromData.getId() ).isEqualTo( "snapshots" );

        //em.clear();

        Query query = em.createQuery( "SELECT r FROM Repository r" );

        List<Repository> repositories = query.getResultList();

        Assertions.assertThat( repositories ).isNotNull().isNotEmpty().hasSize( 2 );

    }
}
