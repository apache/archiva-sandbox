package org.apache.archiva.metadata.repository.jpa;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.util.Properties;

/**
 * @author Olivier Lamy
 */
@Service( "archivaEntityManagerFactory#jpa-archiva" )
public class DefaultArchivaEntityManagerFactory
    implements ArchivaEntityManagerFactory
{

    @Inject
    private ApplicationContext applicationContext;

    private EntityManager entityManager;

    @PostConstruct
    public void initialize()
    {
        entityManager = Persistence.createEntityManagerFactory( "archiva", new Properties() ).createEntityManager();
    }

    @Override
    public EntityManager getEntityManager()
    {
        return entityManager;
    }
}
