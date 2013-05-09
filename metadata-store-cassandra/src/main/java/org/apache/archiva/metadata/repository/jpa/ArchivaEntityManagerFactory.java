package org.apache.archiva.metadata.repository.jpa;

import javax.persistence.EntityManager;

/**
 * @author Olivier Lamy
 */
public interface ArchivaEntityManagerFactory
{
    EntityManager getEntityManager();
}
