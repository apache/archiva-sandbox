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

import com.google.common.base.Function;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.entitystore.DefaultEntityManager;
import com.netflix.astyanax.entitystore.EntityManager;
import org.apache.archiva.configuration.ArchivaConfiguration;
import org.apache.archiva.metadata.model.ArtifactMetadata;
import org.apache.archiva.metadata.model.MetadataFacet;
import org.apache.archiva.metadata.model.MetadataFacetFactory;
import org.apache.archiva.metadata.model.ProjectMetadata;
import org.apache.archiva.metadata.model.ProjectVersionMetadata;
import org.apache.archiva.metadata.model.ProjectVersionReference;
import org.apache.archiva.metadata.repository.MetadataRepository;
import org.apache.archiva.metadata.repository.MetadataRepositoryException;
import org.apache.archiva.metadata.repository.MetadataResolutionException;
import org.apache.archiva.metadata.repository.cassandra.model.Namespace;
import org.apache.archiva.metadata.repository.cassandra.model.Repository;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Olivier Lamy
 */
public class CassandraMetadataRepository
    implements MetadataRepository
{

    private Logger logger = LoggerFactory.getLogger( getClass() );

    private ArchivaConfiguration configuration;

    private final Map<String, MetadataFacetFactory> metadataFacetFactories;

    private Keyspace keyspace;

    private EntityManager<Repository, String> repositoryEntityManager;

    private EntityManager<Namespace, String> namespaceEntityManager;

    public CassandraMetadataRepository( Map<String, MetadataFacetFactory> metadataFacetFactories,
                                        ArchivaConfiguration configuration, Keyspace keyspace )
    {
        this.metadataFacetFactories = metadataFacetFactories;
        this.configuration = configuration;

        this.keyspace = keyspace;

        try
        {
            Properties properties = keyspace.getKeyspaceProperties();
            logger.info( "keyspace properties: {}", properties );
        }
        catch ( ConnectionException e )
        {
            // FIXME better logging !
            logger.warn( e.getMessage(), e );
        }

        try
        {
            repositoryEntityManager =
                new DefaultEntityManager.Builder<Repository, String>().withEntityType( Repository.class ).withKeyspace(
                    keyspace ).build();

            repositoryEntityManager.createStorage( null );

            namespaceEntityManager =
                new DefaultEntityManager.Builder<Namespace, String>().withEntityType( Namespace.class ).withKeyspace(
                    keyspace ).build();

            namespaceEntityManager.createStorage( null );
        }
        catch ( PersistenceException e )
        {
            logger.error( e.getMessage(), e );
        }
    }

    public EntityManager<Repository, String> getRepositoryEntityManager()
    {
        return repositoryEntityManager;
    }

    public EntityManager<Namespace, String> getNamespaceEntityManager()
    {
        return namespaceEntityManager;
    }

    public void setRepositoryEntityManager( EntityManager<Repository, String> repositoryEntityManager )
    {
        this.repositoryEntityManager = repositoryEntityManager;
    }

    public void setNamespaceEntityManager( EntityManager<Namespace, String> namespaceEntityManager )
    {
        this.namespaceEntityManager = namespaceEntityManager;
    }

    @Override
    public void updateNamespace( String repositoryId, String namespaceId )
        throws MetadataRepositoryException
    {
        Repository repository = this.repositoryEntityManager.get( repositoryId );

        if ( repository == null )
        {
            repository = new Repository( repositoryId );

            Namespace namespace = new Namespace( namespaceId, repository );
            //namespace.setRepository( repository );
            //repository.getNamespaces().add( namespace );
            this.repositoryEntityManager.put( repository );

            this.namespaceEntityManager.put( namespace );
        }
        // FIXME add a Namespace id builder
        Namespace namespace = namespaceEntityManager.get(
            new Namespace.KeyBuilder().withNamespace( namespaceId ).withRepositoryId( repositoryId ).build() );
        if ( namespace == null )
        {
            namespace = new Namespace( namespaceId, repository );
            namespaceEntityManager.put( namespace );
        }

    }


    @Override
    public void removeNamespace( String repositoryId, String namespaceId )
        throws MetadataRepositoryException
    {
        Namespace namespace = namespaceEntityManager.get(
            new Namespace.KeyBuilder().withNamespace( namespaceId ).withRepositoryId( repositoryId ).build() );
        if ( namespace != null )
        {
            namespaceEntityManager.remove( namespace );
        }
    }


    @Override
    public void removeRepository( String repositoryId )
        throws MetadataRepositoryException
    {
        Repository repository = repositoryEntityManager.get( repositoryId );
        if ( repository != null )
        {
            repositoryEntityManager.remove( repository );
        }
    }

    @Override
    public Collection<String> getRepositories()
        throws MetadataRepositoryException
    {
        logger.debug( "getRepositories" );

        List<Repository> repositories = repositoryEntityManager.getAll();
        if ( repositories == null )
        {
            return Collections.emptyList();
        }
        List<String> repoIds = new ArrayList<String>( repositories.size() );
        for ( Repository repository : repositories )
        {
            repoIds.add( repository.getName() );
        }
        logger.debug( "getRepositories found: {}", repoIds );
        return repoIds;

    }


    @Override
    public Collection<String> getRootNamespaces( final String repoId )
        throws MetadataResolutionException
    {
        final Set<Namespace> namespaces = new HashSet<Namespace>();

        namespaceEntityManager.visitAll( new Function<Namespace, Boolean>()
        {
            // @Nullable add dependency ?
            @Override
            public Boolean apply( Namespace namespace )
            {
                if ( namespace != null && namespace.getRepository() != null && StringUtils.equalsIgnoreCase( repoId,
                                                                                                             namespace.getRepository().getId() ) )
                {
                    if ( !StringUtils.contains( namespace.getName(), "." ) )
                    {
                        namespaces.add( namespace );
                    }
                }
                return Boolean.TRUE;
            }
        } );

        List<String> namespaceNames = new ArrayList<String>( namespaces.size() );

        for ( Namespace namespace : namespaces )
        {
            namespaceNames.add( namespace.getName() );
        }

        return namespaceNames;
    }

    @Override
    public Collection<String> getNamespaces( final String repoId, final String namespaceId )
        throws MetadataResolutionException
    {
        final Set<Namespace> namespaces = new HashSet<Namespace>();

        namespaceEntityManager.visitAll( new Function<Namespace, Boolean>()
        {
            // @Nullable add dependency ?
            @Override
            public Boolean apply( Namespace namespace )
            {
                if ( namespace != null && namespace.getRepository() != null && StringUtils.equalsIgnoreCase( repoId,
                                                                                                             namespace.getRepository().getId() ) )
                {
                    if ( StringUtils.startsWith( namespace.getName(), namespaceId ) )
                    {
                        namespaces.add( namespace );
                    }
                }
                return Boolean.TRUE;
            }
        } );

        List<String> namespaceNames = new ArrayList<String>( namespaces.size() );

        for ( Namespace namespace : namespaces )
        {
            namespaceNames.add( namespace.getName() );
        }

        return namespaceNames;

    }

    public List<String> getNamespaces( final String repoId )
        throws MetadataResolutionException
    {
        try
        {
            logger.debug( "getNamespaces for repository '{}'", repoId );
            //TypedQuery<Repository> typedQuery =
            //    entityManager.createQuery( "select n from Namespace n where n.repository_id=:id", Namespace.class );

            //List<Repository> namespaces = typedQuery.setParameter( "id", repoId ).getResultList();

            Repository repository = repositoryEntityManager.get( repoId );

            if ( repository == null )
            {
                return Collections.emptyList();
            }

            // FIXME find correct cql query
            //String query = "select * from namespace where repository.id = '" + repoId + "';";

            //List<Namespace> namespaces = namespaceEntityManager.find( query );

            final Set<Namespace> namespaces = new HashSet<Namespace>();

            namespaceEntityManager.visitAll( new Function<Namespace, Boolean>()
            {
                // @Nullable add dependency ?
                @Override
                public Boolean apply( Namespace namespace )
                {
                    if ( namespace != null && namespace.getRepository() != null && StringUtils.equalsIgnoreCase( repoId,
                                                                                                                 namespace.getRepository().getId() ) )
                    {
                        namespaces.add( namespace );
                    }
                    return Boolean.TRUE;
                }
            } );

            repository.setNamespaces( new ArrayList<Namespace>( namespaces ) );

            if ( repository == null || repository.getNamespaces().isEmpty() )
            {
                return Collections.emptyList();
            }
            List<String> namespaceIds = new ArrayList<String>( repository.getNamespaces().size() );

            for ( Namespace n : repository.getNamespaces() )
            {
                namespaceIds.add( n.getName() );
            }

            logger.debug( "getNamespaces for repository '{}' found {}", repoId, namespaceIds.size() );
            return namespaceIds;
        }
        catch ( PersistenceException e )
        {
            throw new MetadataResolutionException( e.getMessage(), e );
        }
    }


    @Override
    public void updateProject( String repositoryId, ProjectMetadata project )
        throws MetadataRepositoryException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateArtifact( String repositoryId, String namespace, String projectId, String projectVersion,
                                ArtifactMetadata artifactMeta )
        throws MetadataRepositoryException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateProjectVersion( String repositoryId, String namespace, String projectId,
                                      ProjectVersionMetadata versionMetadata )
        throws MetadataRepositoryException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getMetadataFacets( String repositoryId, String facetId )
        throws MetadataRepositoryException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasMetadataFacet( String repositoryId, String facetId )
        throws MetadataRepositoryException
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MetadataFacet getMetadataFacet( String repositoryId, String facetId, String name )
        throws MetadataRepositoryException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addMetadataFacet( String repositoryId, MetadataFacet metadataFacet )
        throws MetadataRepositoryException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeMetadataFacets( String repositoryId, String facetId )
        throws MetadataRepositoryException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeMetadataFacet( String repositoryId, String facetId, String name )
        throws MetadataRepositoryException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ArtifactMetadata> getArtifactsByDateRange( String repositoryId, Date startTime, Date endTime )
        throws MetadataRepositoryException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ArtifactMetadata> getArtifactsByChecksum( String repositoryId, String checksum )
        throws MetadataRepositoryException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeArtifact( String repositoryId, String namespace, String project, String version, String id )
        throws MetadataRepositoryException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeArtifact( ArtifactMetadata artifactMetadata, String baseVersion )
        throws MetadataRepositoryException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeArtifact( String repositoryId, String namespace, String project, String version,
                                MetadataFacet metadataFacet )
        throws MetadataRepositoryException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public List<ArtifactMetadata> getArtifacts( String repositoryId )
        throws MetadataRepositoryException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ProjectMetadata getProject( String repoId, String namespace, String projectId )
        throws MetadataResolutionException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ProjectVersionMetadata getProjectVersion( String repoId, String namespace, String projectId,
                                                     String projectVersion )
        throws MetadataResolutionException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<String> getArtifactVersions( String repoId, String namespace, String projectId,
                                                   String projectVersion )
        throws MetadataResolutionException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<ProjectVersionReference> getProjectReferences( String repoId, String namespace, String projectId,
                                                                     String projectVersion )
        throws MetadataResolutionException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<String> getProjects( String repoId, String namespace )
        throws MetadataResolutionException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<String> getProjectVersions( String repoId, String namespace, String projectId )
        throws MetadataResolutionException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeProjectVersion( String repoId, String namespace, String projectId, String projectVersion )
        throws MetadataRepositoryException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<ArtifactMetadata> getArtifacts( String repoId, String namespace, String projectId,
                                                      String projectVersion )
        throws MetadataResolutionException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeProject( String repositoryId, String namespace, String projectId )
        throws MetadataRepositoryException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void save()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close()
        throws MetadataRepositoryException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void revert()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canObtainAccess( Class<?> aClass )
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object obtainAccess( Class<?> aClass )
        throws MetadataRepositoryException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
