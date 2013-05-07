package org.apache.archiva.metadata.repository.jpa;

import info.archinnov.achilles.entity.manager.ThriftEntityManager;
import org.apache.archiva.metadata.repository.jpa.model.Repository;
import org.apache.archiva.test.utils.ArchivaSpringJUnit4ClassRunner;
import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;

/**
 * @author Olivier Lamy
 */
@RunWith( ArchivaSpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath*:/META-INF/spring-context.xml", "classpath*:/spring-context.xml" } )
public class RepositoriesNamespaceTest
{
    @Inject
    private ThriftEntityManager em;

    @Before
    public void setup()
        throws Exception
    {
        //emf = Persistence.createEntityManagerFactory( "archiva" );
        //em = emf.createEntityManager();
    }

    @After
    public void shutdown()
        throws Exception
    {
        em.close();
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

        em.flush();

        Repository repositoryFromData = em.find( Repository.class, "releases" );

        Assertions.assertThat( repositoryFromData ).isNotNull();
        Assertions.assertThat( repositoryFromData.getId() ).isEqualTo( "releases" );

        repositoryFromData = em.find( Repository.class, "snapshots" );

        Assertions.assertThat( repositoryFromData ).isNotNull();
        Assertions.assertThat( repositoryFromData.getId() ).isEqualTo( "snapshots" );

        //em.clear();

        Query query = em.createQuery( "SELECT r FROM Repository r" );

        List<Repository> repositories = query.getResultList();

        Assertions.assertThat( repositories ).isNotNull().isNotEmpty().hasSize( 2 );

    }
}
