package org.apache.archiva.metadata.repository.jpa;

import org.apache.archiva.configuration.ArchivaConfiguration;
import org.apache.archiva.metadata.model.MetadataFacetFactory;
import org.apache.archiva.metadata.repository.MetadataRepository;
import org.apache.archiva.metadata.repository.MetadataResolver;
import org.apache.archiva.metadata.repository.RepositorySession;
import org.apache.archiva.metadata.repository.RepositorySessionFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Olivier Lamy
 */
@Service("repositorySessionFactory#cassandra")
public class CassandraRepositorySessionFactory
    implements RepositorySessionFactory
{

    private Map<String, MetadataFacetFactory> metadataFacetFactories;

    @Inject
    @Named(value = "archivaConfiguration#default")
    private ArchivaConfiguration configuration;

    @Inject
    private MetadataResolver metadataResolver;

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private ArchivaEntityManagerFactory archivaEntityManagerFactory;

    private EntityManager entityManager;

    @PostConstruct
    public void initialize()
    {
        entityManager = archivaEntityManagerFactory.getEntityManager();

        Map<String, MetadataFacetFactory> tmpMetadataFacetFactories =
            applicationContext.getBeansOfType( MetadataFacetFactory.class );
        // olamy with spring the "id" is now "metadataFacetFactory#hint"
        // whereas was only hint with plexus so let remove  metadataFacetFactory#
        metadataFacetFactories = new HashMap<String, MetadataFacetFactory>( tmpMetadataFacetFactories.size() );

        for ( Map.Entry<String, MetadataFacetFactory> entry : tmpMetadataFacetFactories.entrySet() )
        {
            metadataFacetFactories.put( StringUtils.substringAfterLast( entry.getKey(), "#" ), entry.getValue() );
        }
    }


    @Override
    public RepositorySession createSession()
    {
        MetadataRepository metadataRepository =
            new CassandraMetadataRepository( metadataFacetFactories, configuration, entityManager );

        return new RepositorySession( metadataRepository, metadataResolver );
    }
}
