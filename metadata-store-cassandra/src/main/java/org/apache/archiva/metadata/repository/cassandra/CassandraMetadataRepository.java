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
import com.netflix.astyanax.connectionpool.exceptions.NotFoundException;
import com.netflix.astyanax.entitystore.DefaultEntityManager;
import com.netflix.astyanax.entitystore.EntityManager;
import net.sf.beanlib.provider.replicator.BeanReplicator;
import org.apache.archiva.configuration.ArchivaConfiguration;
import org.apache.archiva.metadata.model.ArtifactMetadata;
import org.apache.archiva.metadata.model.FacetedMetadata;
import org.apache.archiva.metadata.model.MetadataFacet;
import org.apache.archiva.metadata.model.MetadataFacetFactory;
import org.apache.archiva.metadata.model.ProjectMetadata;
import org.apache.archiva.metadata.model.ProjectVersionMetadata;
import org.apache.archiva.metadata.model.ProjectVersionReference;
import org.apache.archiva.metadata.repository.MetadataRepository;
import org.apache.archiva.metadata.repository.MetadataRepositoryException;
import org.apache.archiva.metadata.repository.MetadataResolutionException;
import org.apache.archiva.metadata.repository.cassandra.model.ArtifactMetadataModel;
import org.apache.archiva.metadata.repository.cassandra.model.MetadataFacetModel;
import org.apache.archiva.metadata.repository.cassandra.model.Namespace;
import org.apache.archiva.metadata.repository.cassandra.model.Project;
import org.apache.archiva.metadata.repository.cassandra.model.ProjectVersionMetadataModel;
import org.apache.archiva.metadata.repository.cassandra.model.Repository;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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

    private EntityManager<Project, String> projectEntityManager;

    private EntityManager<ArtifactMetadataModel, String> artifactMetadataModelEntityManager;

    private EntityManager<MetadataFacetModel, String> metadataFacetModelEntityManager;

    private EntityManager<ProjectVersionMetadataModel, String> projectVersionMetadataModelEntityManager;

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
            boolean exists = columnFamilyExists( "repository" );
            // TODO very basic test we must test model change too
            if ( !exists )
            {
                repositoryEntityManager.createStorage( null );
            }

            namespaceEntityManager =
                new DefaultEntityManager.Builder<Namespace, String>().withEntityType( Namespace.class ).withKeyspace(
                    keyspace ).build();

            exists = columnFamilyExists( "namespace" );
            if ( !exists )
            {
                namespaceEntityManager.createStorage( null );
            }

            projectEntityManager =
                new DefaultEntityManager.Builder<Project, String>().withEntityType( Project.class ).withKeyspace(
                    keyspace ).build();

            exists = columnFamilyExists( "project" );
            if ( !exists )
            {
                projectEntityManager.createStorage( null );
            }

            artifactMetadataModelEntityManager =
                new DefaultEntityManager.Builder<ArtifactMetadataModel, String>().withEntityType(
                    ArtifactMetadataModel.class ).withKeyspace( keyspace ).build();

            exists = columnFamilyExists( "artifactmetadatamodel" );
            if ( !exists )
            {
                artifactMetadataModelEntityManager.createStorage( null );
            }

            metadataFacetModelEntityManager =
                new DefaultEntityManager.Builder<MetadataFacetModel, String>().withEntityType(
                    MetadataFacetModel.class ).withKeyspace( keyspace ).build();

            exists = columnFamilyExists( "metadatafacetmodel" );
            if ( !exists )
            {
                metadataFacetModelEntityManager.createStorage( null );
            }

            projectVersionMetadataModelEntityManager =
                new DefaultEntityManager.Builder<ProjectVersionMetadataModel, String>().withEntityType(
                    ProjectVersionMetadataModel.class ).withKeyspace( keyspace ).build();

            exists = columnFamilyExists( "projectversionmetadatamodel" );
            if ( !exists )
            {
                projectVersionMetadataModelEntityManager.createStorage( null );
            }

        }
        catch ( PersistenceException e )
        {
            // FIXME report exception
            logger.error( e.getMessage(), e );
        }
        catch ( ConnectionException e )
        {
            // FIXME report exception
            logger.error( e.getMessage(), e );
        }
    }

    private boolean columnFamilyExists( String columnFamilyName )
        throws ConnectionException
    {
        try
        {
            Properties properties = keyspace.getColumnFamilyProperties( columnFamilyName );
            logger.debug( "getColumnFamilyProperties for {}: {}", columnFamilyName, properties );
            return true;
        }
        catch ( NotFoundException e )
        {
            return false;
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

    public EntityManager<Project, String> getProjectEntityManager()
    {
        return projectEntityManager;
    }

    public void setProjectEntityManager( EntityManager<Project, String> projectEntityManager )
    {
        this.projectEntityManager = projectEntityManager;
    }

    public EntityManager<ArtifactMetadataModel, String> getArtifactMetadataModelEntityManager()
    {
        return artifactMetadataModelEntityManager;
    }

    public void setArtifactMetadataModelEntityManager(
        EntityManager<ArtifactMetadataModel, String> artifactMetadataModelEntityManager )
    {
        this.artifactMetadataModelEntityManager = artifactMetadataModelEntityManager;
    }

    public EntityManager<MetadataFacetModel, String> getMetadataFacetModelEntityManager()
    {
        return metadataFacetModelEntityManager;
    }

    public void setMetadataFacetModelEntityManager(
        EntityManager<MetadataFacetModel, String> metadataFacetModelEntityManager )
    {
        this.metadataFacetModelEntityManager = metadataFacetModelEntityManager;
    }

    public EntityManager<ProjectVersionMetadataModel, String> getProjectVersionMetadataModelEntityManager()
    {
        return projectVersionMetadataModelEntityManager;
    }

    public void setProjectVersionMetadataModelEntityManager(
        EntityManager<ProjectVersionMetadataModel, String> projectVersionMetadataModelEntityManager )
    {
        this.projectVersionMetadataModelEntityManager = projectVersionMetadataModelEntityManager;
    }

    @Override
    public void updateNamespace( String repositoryId, String namespaceId )
        throws MetadataRepositoryException
    {
        try
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
        catch ( PersistenceException e )
        {
            throw new MetadataRepositoryException( e.getMessage(), e );
        }

    }


    @Override
    public void removeNamespace( String repositoryId, String namespaceId )
        throws MetadataRepositoryException
    {
        try
        {
            Namespace namespace = namespaceEntityManager.get(
                new Namespace.KeyBuilder().withNamespace( namespaceId ).withRepositoryId( repositoryId ).build() );
            if ( namespace != null )
            {
                namespaceEntityManager.remove( namespace );
            }
        }
        catch ( PersistenceException e )
        {
            throw new MetadataRepositoryException( e.getMessage(), e );
        }
    }


    @Override
    public void removeRepository( String repositoryId )
        throws MetadataRepositoryException
    {
        try
        {
            Repository repository = repositoryEntityManager.get( repositoryId );
            if ( repository != null )
            {
                repositoryEntityManager.remove( repository );
            }
        }
        catch ( PersistenceException e )
        {
            throw new MetadataRepositoryException( e.getMessage(), e );
        }
    }

    @Override
    public Collection<String> getRepositories()
        throws MetadataRepositoryException
    {
        try
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
        catch ( PersistenceException e )
        {
            throw new MetadataRepositoryException( e.getMessage(), e );
        }

    }


    @Override
    public Collection<String> getRootNamespaces( final String repoId )
        throws MetadataResolutionException
    {
        try
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
        catch ( PersistenceException e )
        {
            throw new MetadataResolutionException( e.getMessage(), e );
        }
    }

    @Override
    public Collection<String> getNamespaces( final String repoId, final String namespaceId )
        throws MetadataResolutionException
    {
        try
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
        catch ( PersistenceException e )
        {
            throw new MetadataResolutionException( e.getMessage(), e );
        }

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
    public void updateProject( String repositoryId, ProjectMetadata projectMetadata )
        throws MetadataRepositoryException
    {

        // project exists ? if yes return
        String projectKey = new Project.KeyBuilder().withProjectId( projectMetadata.getId() ).withNamespace(
            new Namespace( projectMetadata.getNamespace(), new Repository( repositoryId ) ) ).build();

        Project project = projectEntityManager.get( projectKey );
        if ( project != null )
        {
            return;
        }

        // FIXME really needed ?
        // test if the namespace exist
        String namespaceKey = new Namespace.KeyBuilder().withRepositoryId( repositoryId ).withNamespace(
            projectMetadata.getNamespace() ).build();
        Namespace namespace = namespaceEntityManager.get( namespaceKey );
        if ( namespace == null )
        {
            updateNamespace( repositoryId, projectMetadata.getNamespace() );
        }

        project = new Project( projectKey, namespace );

        projectEntityManager.put( project );

    }

    @Override
    public void updateArtifact( String repositoryId, String namespaceId, String projectId, String projectVersion,
                                ArtifactMetadata artifactMeta )
        throws MetadataRepositoryException
    {
        // TODO verif repository namespace exists ?

        String key =
            new ArtifactMetadataModel.KeyBuilder().withRepositoryId( repositoryId ).withNamespace( namespaceId ).withId(
                projectId ).withProjectVersion( projectVersion ).build();

        ArtifactMetadataModel artifactMetadataModel = artifactMetadataModelEntityManager.get( key );
        if ( artifactMetadataModel == null )
        {
            artifactMetadataModel = new ArtifactMetadataModel( key, artifactMeta.getId(), repositoryId, namespaceId,
                                                               artifactMeta.getProject(), projectVersion,
                                                               artifactMeta.getVersion(),
                                                               artifactMeta.getFileLastModified(),
                                                               artifactMeta.getSize(), artifactMeta.getMd5(),
                                                               artifactMeta.getSha1(), artifactMeta.getWhenGathered() );
            artifactMetadataModelEntityManager.put( artifactMetadataModel );

        }

        // now facets
        updateFacets( artifactMeta, artifactMetadataModel );

    }

    /**
     * iterate over available facets to remove/add from the artifactMetadata
     *
     * @param facetedMetadata
     * @param artifactMetadataModel only use for the key
     */
    private void updateFacets( final FacetedMetadata facetedMetadata,
                               final ArtifactMetadataModel artifactMetadataModel )
    {

        for ( final String facetId : metadataFacetFactories.keySet() )
        {
            MetadataFacet metadataFacet = facetedMetadata.getFacet( facetId );
            if ( metadataFacet == null )
            {
                continue;
            }
            // clean first

            final List<MetadataFacetModel> metadataFacetModels = new ArrayList<MetadataFacetModel>();

            metadataFacetModelEntityManager.visitAll( new Function<MetadataFacetModel, Boolean>()
            {
                @Override
                public Boolean apply( MetadataFacetModel metadataFacetModel )
                {
                    ArtifactMetadataModel tmp = metadataFacetModel.getArtifactMetadataModel();
                    if ( StringUtils.equals( metadataFacetModel.getFacetId(), facetId ) && StringUtils.equals(
                        tmp.getRepositoryId(), artifactMetadataModel.getRepositoryId() ) && StringUtils.equals(
                        tmp.getNamespace(), artifactMetadataModel.getNamespace() ) && StringUtils.equals(
                        tmp.getProject(), artifactMetadataModel.getProject() ) )
                    {
                        metadataFacetModels.add( metadataFacetModel );
                    }
                    return Boolean.TRUE;
                }
            } );

            metadataFacetModelEntityManager.remove( metadataFacetModels );

            Map<String, String> properties = metadataFacet.toProperties();

            final List<MetadataFacetModel> metadataFacetModelsToAdd =
                new ArrayList<MetadataFacetModel>( properties.size() );

            for ( Map.Entry<String, String> entry : properties.entrySet() )
            {
                String key = new MetadataFacetModel.KeyBuilder().withKey( entry.getKey() ).withArtifactMetadataModel(
                    artifactMetadataModel ).withFacetId( facetId ).withName( metadataFacet.getName() ).build();
                MetadataFacetModel metadataFacetModel =
                    new MetadataFacetModel( key, artifactMetadataModel, facetId, entry.getKey(), entry.getValue(),
                                            metadataFacet.getName() );
                metadataFacetModelsToAdd.add( metadataFacetModel );
            }

            metadataFacetModelEntityManager.put( metadataFacetModelsToAdd );

        }
    }

    @Override
    public void updateProjectVersion( String repositoryId, String namespace, String projectId,
                                      ProjectVersionMetadata versionMetadata )
        throws MetadataRepositoryException
    {
        // we don't test of repository and namespace really exist !
        String key = new ProjectVersionMetadataModel.KeyBuilder().withRepository( repositoryId ).withNamespace(
            namespace ).withProjectId( projectId ).withId( versionMetadata.getId() ).build();

        ProjectVersionMetadataModel projectVersionMetadataModel = projectVersionMetadataModelEntityManager.get( key );

        projectVersionMetadataModel =
            new BeanReplicator().replicateBean( versionMetadata, ProjectVersionMetadataModel.class );
        projectVersionMetadataModel.setRowId( key );

        projectVersionMetadataModel.setCiManagement( versionMetadata.getCiManagement() );
        projectVersionMetadataModel.setIssueManagement( versionMetadata.getIssueManagement() );
        projectVersionMetadataModel.setOrganization( versionMetadata.getOrganization() );
        projectVersionMetadataModel.setScm( versionMetadata.getScm() );
        // FIXME collections !!

        projectVersionMetadataModelEntityManager.put( projectVersionMetadataModel );

        ArtifactMetadataModel artifactMetadataModel = new ArtifactMetadataModel();
        artifactMetadataModel.setArtifactMetadataModelId(
            new ArtifactMetadataModel.KeyBuilder().withId( versionMetadata.getId() ).withRepositoryId(
                repositoryId ).withNamespace( namespace ).withProjectVersion( versionMetadata.getVersion() ).build() );
        artifactMetadataModel.setRepositoryId( repositoryId );
        artifactMetadataModel.setNamespace( namespace );
        artifactMetadataModel.setProject( projectId );
        // facets etc...
        updateFacets( versionMetadata, artifactMetadataModel );
    }


    private static class BooleanHolder
    {
        private boolean value = false;
    }

    @Override
    public List<String> getMetadataFacets( final String repositoryId, final String facetId )
        throws MetadataRepositoryException
    {
        // FIXME use cql query !!
        final List<String> facets = new ArrayList<String>();
        this.metadataFacetModelEntityManager.visitAll( new Function<MetadataFacetModel, Boolean>()
        {
            @Override
            public Boolean apply( MetadataFacetModel metadataFacetModel )
            {
                if ( metadataFacetModel != null )
                {
                    if ( StringUtils.equals( metadataFacetModel.getArtifactMetadataModel().getRepositoryId(),
                                             repositoryId ) && StringUtils.equals( metadataFacetModel.getFacetId(),
                                                                                   facetId ) )
                    {
                        facets.add( metadataFacetModel.getName() );
                    }
                }
                return Boolean.TRUE;
            }
        } );

        return facets;

    }

    @Override
    public boolean hasMetadataFacet( String repositoryId, String facetId )
        throws MetadataRepositoryException
    {
        return !getMetadataFacets( repositoryId, facetId ).isEmpty();
    }

    @Override
    public MetadataFacet getMetadataFacet( final String repositoryId, final String facetId, final String name )
        throws MetadataRepositoryException
    {
        // FIXME use cql query !!
        final List<MetadataFacetModel> facets = new ArrayList<MetadataFacetModel>();
        this.metadataFacetModelEntityManager.visitAll( new Function<MetadataFacetModel, Boolean>()
        {
            @Override
            public Boolean apply( MetadataFacetModel metadataFacetModel )
            {
                if ( metadataFacetModel != null )
                {
                    if ( StringUtils.equals( metadataFacetModel.getArtifactMetadataModel().getRepositoryId(),
                                             repositoryId ) && StringUtils.equals( metadataFacetModel.getFacetId(),
                                                                                   facetId ) && StringUtils.equals(
                        metadataFacetModel.getName(), name ) )
                    {
                        facets.add( metadataFacetModel );
                    }
                }
                return Boolean.TRUE;
            }
        } );

        if ( facets.isEmpty() )
        {
            return null;
        }

        MetadataFacetFactory metadataFacetFactory = metadataFacetFactories.get( facetId );
        MetadataFacet metadataFacet = metadataFacetFactory.createMetadataFacet( repositoryId, name );
        Map<String, String> map = new HashMap<String, String>( facets.size() );
        for ( MetadataFacetModel metadataFacetModel : facets )
        {
            map.put( metadataFacetModel.getKey(), metadataFacetModel.getValue() );
        }
        metadataFacet.fromProperties( map );
        return metadataFacet;
    }

    @Override
    public void addMetadataFacet( String repositoryId, MetadataFacet metadataFacet )
        throws MetadataRepositoryException
    {

        if ( metadataFacet == null || metadataFacet.toProperties() == null || metadataFacet.toProperties().isEmpty() )
        {
            return;
        }
        for ( Map.Entry<String, String> entry : metadataFacet.toProperties().entrySet() )
        {

            String key = new MetadataFacetModel.KeyBuilder().withRepositoryId( repositoryId ).withFacetId(
                metadataFacet.getFacetId() ).withName( metadataFacet.getName() ).withKey( entry.getKey() ).build();

            MetadataFacetModel metadataFacetModel = metadataFacetModelEntityManager.get( key );
            if ( metadataFacetModel == null )
            {
                metadataFacetModel = new MetadataFacetModel();
                // we need to store the repositoryId
                ArtifactMetadataModel artifactMetadataModel = new ArtifactMetadataModel();
                artifactMetadataModel.setRepositoryId( repositoryId );
                metadataFacetModel.setArtifactMetadataModel( artifactMetadataModel );
                metadataFacetModel.setId( key );
                metadataFacetModel.setKey( entry.getKey() );
                metadataFacetModel.setFacetId( metadataFacet.getFacetId() );
                metadataFacetModel.setName( metadataFacetModel.getName() );
            }
            metadataFacetModel.setValue( entry.getValue() );
            metadataFacetModelEntityManager.put( metadataFacetModel );

        }
    }

    @Override
    public void removeMetadataFacets( final String repositoryId, final String facetId )
        throws MetadataRepositoryException
    {
        logger.debug( "removeMetadataFacets repositoryId: '{}', facetId: '{}'", repositoryId, facetId );
        final List<MetadataFacetModel> toRemove = new ArrayList<MetadataFacetModel>();

        // FIXME cql query
        metadataFacetModelEntityManager.visitAll( new Function<MetadataFacetModel, Boolean>()
        {
            @Override
            public Boolean apply( MetadataFacetModel metadataFacetModel )
            {
                if ( metadataFacetModel != null )
                {
                    if ( StringUtils.equals( metadataFacetModel.getArtifactMetadataModel().getRepositoryId(),
                                             repositoryId ) && StringUtils.equals( metadataFacetModel.getFacetId(),
                                                                                   facetId ) )
                    {
                        toRemove.add( metadataFacetModel );
                    }
                }
                return Boolean.TRUE;
            }
        } );
        logger.debug( "removeMetadataFacets repositoryId: '{}', facetId: '{}', toRemove: {}", repositoryId, facetId,
                      toRemove );
        metadataFacetModelEntityManager.remove( toRemove );
    }

    @Override
    public void removeMetadataFacet( final String repositoryId, final String facetId, final String name )
        throws MetadataRepositoryException
    {
        logger.debug( "removeMetadataFacets repositoryId: '{}', facetId: '{}'", repositoryId, facetId );
        final List<MetadataFacetModel> toRemove = new ArrayList<MetadataFacetModel>();

        // FIXME cql query
        metadataFacetModelEntityManager.visitAll( new Function<MetadataFacetModel, Boolean>()
        {
            @Override
            public Boolean apply( MetadataFacetModel metadataFacetModel )
            {
                if ( metadataFacetModel != null )
                {
                    if ( StringUtils.equals( metadataFacetModel.getArtifactMetadataModel().getRepositoryId(),
                                             repositoryId ) && StringUtils.equals( metadataFacetModel.getFacetId(),
                                                                                   facetId ) && StringUtils.equals(
                        metadataFacetModel.getName(), name ) )
                    {
                        toRemove.add( metadataFacetModel );
                    }
                }
                return Boolean.TRUE;
            }
        } );
        logger.debug( "removeMetadataFacets repositoryId: '{}', facetId: '{}', toRemove: {}", repositoryId, facetId,
                      toRemove );
        metadataFacetModelEntityManager.remove( toRemove );
    }

    @Override
    public List<ArtifactMetadata> getArtifactsByDateRange( final String repositoryId, final Date startTime,
                                                           final Date endTime )
        throws MetadataRepositoryException
    {

        final List<ArtifactMetadataModel> artifactMetadataModels = new ArrayList<ArtifactMetadataModel>();

        // FIXME cql query
        artifactMetadataModelEntityManager.visitAll( new Function<ArtifactMetadataModel, Boolean>()
        {
            @Override
            public Boolean apply( ArtifactMetadataModel artifactMetadataModel )
            {
                if ( artifactMetadataModel != null )
                {
                    if ( StringUtils.equals( artifactMetadataModel.getRepositoryId(), repositoryId )
                        && artifactMetadataModel.getNamespace() != null &&
                        artifactMetadataModel.getProject() != null && artifactMetadataModel.getId() != null )
                    {

                        Date when = artifactMetadataModel.getWhenGathered();
                        if ( ( startTime != null ? when.getTime() >= startTime.getTime() : true ) && ( endTime != null ?
                            when.getTime() <= endTime.getTime() : true ) )
                        {
                            artifactMetadataModels.add( artifactMetadataModel );
                        }
                    }
                }
                return Boolean.TRUE;
            }
        } );
        List<ArtifactMetadata> artifactMetadatas = new ArrayList<ArtifactMetadata>( artifactMetadataModels.size() );

        for ( ArtifactMetadataModel model : artifactMetadataModels )
        {
            ArtifactMetadata artifactMetadata = new BeanReplicator().replicateBean( model, ArtifactMetadata.class );
            populateFacets( artifactMetadata );
            artifactMetadatas.add( artifactMetadata );
        }

        // FIXME facets ?

        logger.debug( "getArtifactsByDateRange repositoryId: {}, startTime: {}, endTime: {}, artifactMetadatas: {}",
                      repositoryId, startTime, endTime, artifactMetadatas );

        return artifactMetadatas;
    }

    protected void populateFacets( final ArtifactMetadata artifactMetadata )
    {
        final List<MetadataFacetModel> metadataFacetModels = new ArrayList<MetadataFacetModel>();

        metadataFacetModelEntityManager.visitAll( new Function<MetadataFacetModel, Boolean>()
        {
            @Override
            public Boolean apply( MetadataFacetModel metadataFacetModel )
            {
                if ( metadataFacetModel != null )
                {
                    ArtifactMetadataModel artifactMetadataModel = metadataFacetModel.getArtifactMetadataModel();
                    if ( artifactMetadataModel != null )
                    {
                        if ( StringUtils.equals( artifactMetadata.getRepositoryId(),
                                                 artifactMetadataModel.getRepositoryId() ) && StringUtils.equals(
                            artifactMetadata.getNamespace(), artifactMetadataModel.getNamespace() )
                            && StringUtils.equals( artifactMetadata.getRepositoryId(),
                                                   artifactMetadataModel.getRepositoryId() ) && StringUtils.equals(
                            artifactMetadata.getProject(), artifactMetadataModel.getProject() ) && StringUtils.equals(
                            artifactMetadata.getId(), artifactMetadataModel.getId() ) )
                        {
                            metadataFacetModels.add( metadataFacetModel );
                        }
                    }
                }
                return Boolean.TRUE;
            }
        } );
        Map<String, Map<String, String>> facetValuesPerFacet = new HashMap<String, Map<String, String>>();

        for ( MetadataFacetModel model : metadataFacetModels )
        {
            Map<String, String> values = facetValuesPerFacet.get( model.getName() );
            if ( values == null )
            {
                values = new HashMap<String, String>();
            }
            values.put( model.getKey(), model.getValue() );
            facetValuesPerFacet.put( model.getName(), values );
        }

        for ( Map.Entry<String, Map<String, String>> entry : facetValuesPerFacet.entrySet() )
        {
            MetadataFacetFactory factory = metadataFacetFactories.get( entry.getKey() );
            MetadataFacet metadataFacet =
                factory.createMetadataFacet( artifactMetadata.getRepositoryId(), entry.getKey() );
            metadataFacet.fromProperties( entry.getValue() );
            artifactMetadata.addFacet( metadataFacet );
        }
    }

    @Override
    public List<ArtifactMetadata> getArtifactsByChecksum( final String repositoryId, final String checksum )
        throws MetadataRepositoryException
    {
        final List<ArtifactMetadataModel> artifactMetadataModels = new ArrayList<ArtifactMetadataModel>();

        // FIXME cql query
        artifactMetadataModelEntityManager.visitAll( new Function<ArtifactMetadataModel, Boolean>()
        {
            @Override
            public Boolean apply( ArtifactMetadataModel artifactMetadataModel )
            {
                if ( artifactMetadataModel != null )
                {
                    if ( StringUtils.equals( artifactMetadataModel.getRepositoryId(), repositoryId )
                        && artifactMetadataModel.getNamespace() != null &&
                        artifactMetadataModel.getProject() != null && artifactMetadataModel.getId() != null )
                    {

                        if ( StringUtils.equals( checksum, artifactMetadataModel.getMd5() ) || StringUtils.equals(
                            checksum, artifactMetadataModel.getSha1() ) )
                        {
                            artifactMetadataModels.add( artifactMetadataModel );
                        }
                    }
                }
                return Boolean.TRUE;
            }
        } );
        List<ArtifactMetadata> artifactMetadatas = new ArrayList<ArtifactMetadata>( artifactMetadataModels.size() );

        for ( ArtifactMetadataModel model : artifactMetadataModels )
        {
            ArtifactMetadata artifactMetadata = new BeanReplicator().replicateBean( model, ArtifactMetadata.class );
            populateFacets( artifactMetadata );
            artifactMetadatas.add( artifactMetadata );
        }

        logger.debug( "getArtifactsByChecksum repositoryId: {}, checksum: {}, artifactMetadatas: {}", repositoryId,
                      checksum, artifactMetadatas );

        return artifactMetadatas;
    }

    @Override
    public void removeArtifact( String repositoryId, String namespace, String project, String version, String id )
        throws MetadataRepositoryException
    {
        logger.debug( "removeArtifact repositoryId: '{}', namespace: '{}', project: '{}', version: '{}', id: '{}'",
                      repositoryId, namespace, project, version, id );
        String key =
            new ArtifactMetadataModel.KeyBuilder().withRepositoryId( repositoryId ).withNamespace( namespace ).withId(
                id ).withProjectVersion( version ).build();

        ArtifactMetadataModel artifactMetadataModel = new ArtifactMetadataModel();
        artifactMetadataModel.setArtifactMetadataModelId( key );

        artifactMetadataModelEntityManager.remove( artifactMetadataModel );
    }

    @Override
    public void removeArtifact( ArtifactMetadata artifactMetadata, String baseVersion )
        throws MetadataRepositoryException
    {
        logger.debug( "removeArtifact repositoryId: '{}', namespace: '{}', project: '{}', version: '{}', id: '{}'",
                      artifactMetadata.getRepositoryId(), artifactMetadata.getNamespace(),
                      artifactMetadata.getProject(), baseVersion, artifactMetadata.getId() );
        String key =
            new ArtifactMetadataModel.KeyBuilder().withRepositoryId( artifactMetadata.getRepositoryId() ).withNamespace(
                artifactMetadata.getNamespace() ).withId( artifactMetadata.getId() ).withProjectVersion(
                baseVersion ).build();

        ArtifactMetadataModel artifactMetadataModel = new ArtifactMetadataModel();
        artifactMetadataModel.setArtifactMetadataModelId( key );

        artifactMetadataModelEntityManager.remove( artifactMetadataModel );
    }

    @Override
    public void removeArtifact( final String repositoryId, final String namespace, final String project,
                                final String version, final MetadataFacet metadataFacet )
        throws MetadataRepositoryException
    {
        final List<MetadataFacetModel> metadataFacetModels = new ArrayList<MetadataFacetModel>();
        metadataFacetModelEntityManager.visitAll( new Function<MetadataFacetModel, Boolean>()
        {
            @Override
            public Boolean apply( MetadataFacetModel metadataFacetModel )
            {
                if ( metadataFacetModel != null )
                {
                    ArtifactMetadataModel artifactMetadataModel = metadataFacetModel.getArtifactMetadataModel();
                    if ( artifactMetadataModel != null )
                    {
                        if ( StringUtils.equals( repositoryId, artifactMetadataModel.getRepositoryId() )
                            && StringUtils.equals( namespace, artifactMetadataModel.getNamespace() )
                            && StringUtils.equals( project, artifactMetadataModel.getProject() ) && StringUtils.equals(
                            version, artifactMetadataModel.getVersion() ) )
                        {
                            if ( StringUtils.equals( metadataFacetModel.getFacetId(), metadataFacet.getFacetId() )
                                && StringUtils.equals( metadataFacetModel.getName(), metadataFacet.getName() ) )
                            {
                                metadataFacetModels.add( metadataFacetModel );
                            }
                        }
                    }
                }
                return Boolean.TRUE;
            }
        } );
        metadataFacetModelEntityManager.remove( metadataFacetModels );
    }


    @Override
    public List<ArtifactMetadata> getArtifacts( final String repositoryId )
        throws MetadataRepositoryException
    {
        final List<ArtifactMetadataModel> artifactMetadataModels = new ArrayList<ArtifactMetadataModel>();
        // FIXME use cql query !
        artifactMetadataModelEntityManager.visitAll( new Function<ArtifactMetadataModel, Boolean>()
        {
            @Override
            public Boolean apply( ArtifactMetadataModel artifactMetadataModel )
            {
                if ( artifactMetadataModel != null )
                {
                    if ( StringUtils.equals( repositoryId, artifactMetadataModel.getRepositoryId() ) )
                    {
                        artifactMetadataModels.add( artifactMetadataModel );
                    }
                }

                return Boolean.TRUE;
            }
        } );

        List<ArtifactMetadata> artifactMetadatas = new ArrayList<ArtifactMetadata>( artifactMetadataModels.size() );

        for ( ArtifactMetadataModel model : artifactMetadataModels )
        {
            ArtifactMetadata artifactMetadata = new BeanReplicator().replicateBean( model, ArtifactMetadata.class );
            populateFacets( artifactMetadata );
            artifactMetadatas.add( artifactMetadata );
        }

        return artifactMetadatas;
    }

    @Override
    public ProjectMetadata getProject( final String repoId, final String namespace, final String projectId )
        throws MetadataResolutionException
    {
        //basically just checking it exists
        // FIXME use cql query

        final BooleanHolder booleanHolder = new BooleanHolder();

        projectEntityManager.visitAll( new Function<Project, Boolean>()
        {
            @Override
            public Boolean apply( Project project )
            {
                if ( project != null )
                {
                    if ( StringUtils.equals( repoId, project.getNamespace().getRepository().getName() )
                        && StringUtils.equals( namespace, project.getNamespace().getName() ) && StringUtils.equals(
                        projectId, project.getId() ) )
                    {
                        booleanHolder.value = true;
                    }
                }
                return Boolean.TRUE;
            }
        } );

        ProjectMetadata projectMetadata = new ProjectMetadata();
        projectMetadata.setId( projectId );
        projectMetadata.setNamespace( namespace );

        logger.debug( "getProject repoId: {}, namespace: {}, projectId: {} -> {}", repoId, namespace, projectId,
                      projectMetadata );

        return projectMetadata;
    }

    @Override
    public ProjectVersionMetadata getProjectVersion( String repoId, String namespace, String projectId,
                                                     String projectVersion )
        throws MetadataResolutionException
    {
        String key = new ProjectVersionMetadataModel.KeyBuilder().withRepository( repoId ).withNamespace(
            namespace ).withProjectId( projectId ).withId( projectVersion ).build();

        ProjectVersionMetadataModel projectVersionMetadataModel = projectVersionMetadataModelEntityManager.get( key );

        ProjectVersionMetadata projectVersionMetadata =
            new BeanReplicator().replicateBean( projectVersionMetadataModel, ProjectVersionMetadata.class );

        logger.debug( "getProjectVersion repoId: '{}', namespace: '{}', projectId: '{}', projectVersion: {} -> {}",
                      repoId, namespace, projectId, projectVersion, projectVersionMetadata );

        projectVersionMetadata.setCiManagement( projectVersionMetadataModel.getCiManagement() );
        projectVersionMetadata.setIssueManagement( projectVersionMetadataModel.getIssueManagement() );
        projectVersionMetadata.setOrganization( projectVersionMetadataModel.getOrganization() );
        projectVersionMetadata.setScm( projectVersionMetadataModel.getScm() );

        // FIXME complete collections !!

        return projectVersionMetadata;
    }

    @Override
    public Collection<String> getArtifactVersions( final String repoId, final String namespace, final String projectId,
                                                   final String projectVersion )
        throws MetadataResolutionException
    {
        final List<String> versions = new ArrayList<String>();
        // FIXME use cql query
        artifactMetadataModelEntityManager.visitAll( new Function<ArtifactMetadataModel, Boolean>()
        {
            @Override
            public Boolean apply( ArtifactMetadataModel artifactMetadataModel )
            {
                if ( artifactMetadataModel != null )
                {
                    if ( StringUtils.equals( repoId, artifactMetadataModel.getRepositoryId() ) && StringUtils.equals(
                        namespace, artifactMetadataModel.getNamespace() ) && StringUtils.equals( projectId,
                                                                                                 artifactMetadataModel.getId() )
                        && StringUtils.equals( projectVersion, artifactMetadataModel.getProjectVersion() ) )
                    {
                        versions.add( artifactMetadataModel.getVersion() );
                    }
                }
                return Boolean.TRUE;
            }
        } );

        return versions;
    }

    @Override
    public Collection<ProjectVersionReference> getProjectReferences( String repoId, String namespace, String projectId,
                                                                     String projectVersion )
        throws MetadataResolutionException
    {
        // FIXME implement this
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getProjects( final String repoId, final String namespace )
        throws MetadataResolutionException
    {
        final Set<String> projects = new HashSet<String>();

        // FIXME use cql query
        artifactMetadataModelEntityManager.visitAll( new Function<ArtifactMetadataModel, Boolean>()
        {
            @Override
            public Boolean apply( ArtifactMetadataModel artifactMetadataModel )
            {
                if ( artifactMetadataModel != null )
                {
                    if ( StringUtils.equals( repoId, artifactMetadataModel.getRepositoryId() ) && StringUtils.equals(
                        namespace, artifactMetadataModel.getNamespace() ) )
                    {
                        projects.add( artifactMetadataModel.getProject() );
                    }
                }
                return Boolean.TRUE;
            }
        } );

        return projects;
    }

    @Override
    public Collection<String> getProjectVersions( final String repoId, final String namespace, final String projectId )
        throws MetadataResolutionException
    {
        final Set<String> versions = new HashSet<String>();

        // FIXME use cql query
        artifactMetadataModelEntityManager.visitAll( new Function<ArtifactMetadataModel, Boolean>()
        {
            @Override
            public Boolean apply( ArtifactMetadataModel artifactMetadataModel )
            {
                if ( artifactMetadataModel != null )
                {
                    if ( StringUtils.equals( repoId, artifactMetadataModel.getRepositoryId() ) && StringUtils.equals(
                        namespace, artifactMetadataModel.getNamespace() ) && StringUtils.equals( projectId,
                                                                                                 artifactMetadataModel.getId() ) )
                    {
                        versions.add( artifactMetadataModel.getVersion() );
                    }
                }
                return Boolean.TRUE;
            }
        } );

        return versions;
    }

    @Override
    public void removeProjectVersion( final String repoId, final String namespace, final String projectId,
                                      final String projectVersion )
        throws MetadataRepositoryException
    {

        String key =
            new ArtifactMetadataModel.KeyBuilder().withRepositoryId( repoId ).withNamespace( namespace ).withId(
                projectId ).withProjectVersion( projectVersion ).build();

        ArtifactMetadataModel artifactMetadataModel = artifactMetadataModelEntityManager.get( key );

        if ( artifactMetadataModel == null )
        {
            logger.debug( "removeProjectVersion not found" );
            return;
        }

        logger.debug( "removeProjectVersion" );

        artifactMetadataModelEntityManager.remove( artifactMetadataModel );

        /*

        final List<ArtifactMetadataModel> versions = new ArrayList<ArtifactMetadataModel>();

        // FIXME use cql query
        artifactMetadataModelEntityManager.visitAll( new Function<ArtifactMetadataModel, Boolean>()
        {
            @Override
            public Boolean apply( ArtifactMetadataModel artifactMetadataModel )
            {
                if ( artifactMetadataModel != null )
                {
                    if ( StringUtils.equals( repoId, artifactMetadataModel.getRepositoryId() ) && StringUtils.equals(
                        namespace, artifactMetadataModel.getNamespace() ) && StringUtils.equals( projectId,
                                                                                                 artifactMetadataModel.getId() )
                        && StringUtils.equals( projectId,
                                               artifactMetadataModel.getProjectVersion() )
                        )
                    {
                        versions.add( artifactMetadataModel );
                    }
                }
                return Boolean.TRUE;
            }
        } );

        artifactMetadataModelEntityManager.remove( versions );
        */
    }

    @Override
    public Collection<ArtifactMetadata> getArtifacts( final String repoId, final String namespace,
                                                      final String projectId, final String projectVersion )
        throws MetadataResolutionException
    {
        final List<ArtifactMetadataModel> artifactMetadataModels = new ArrayList<ArtifactMetadataModel>();
        // FIXME use cql query !
        artifactMetadataModelEntityManager.visitAll( new Function<ArtifactMetadataModel, Boolean>()
        {
            @Override
            public Boolean apply( ArtifactMetadataModel artifactMetadataModel )
            {
                if ( artifactMetadataModel != null )
                {
                    if ( StringUtils.equals( repoId, artifactMetadataModel.getRepositoryId() ) && StringUtils.equals(
                        namespace, artifactMetadataModel.getNamespace() ) && StringUtils.equals( projectId,
                                                                                                 artifactMetadataModel.getProject() )
                        && StringUtils.equals( projectVersion, artifactMetadataModel.getProjectVersion() ) )
                    {
                        artifactMetadataModels.add( artifactMetadataModel );
                    }
                }

                return Boolean.TRUE;
            }
        } );

        List<ArtifactMetadata> artifactMetadatas = new ArrayList<ArtifactMetadata>( artifactMetadataModels.size() );

        for ( ArtifactMetadataModel model : artifactMetadataModels )
        {
            ArtifactMetadata artifactMetadata = new BeanReplicator().replicateBean( model, ArtifactMetadata.class );
            populateFacets( artifactMetadata );
            artifactMetadatas.add( artifactMetadata );
        }

        return artifactMetadatas;
    }

    @Override
    public void removeProject( String repositoryId, String namespace, String projectId )
        throws MetadataRepositoryException
    {
        String key = new Project.KeyBuilder().withNamespace(
            new Namespace( namespace, new Repository( repositoryId ) ) ).withProjectId( projectId ).build();
        Project project = projectEntityManager.get( key );
        if ( project == null )
        {
            logger.debug( "removeProject notfound" );
            return;
        }
        logger.debug( "removeProject {}", project );
        projectEntityManager.remove( project );
    }

    @Override
    public void save()
    {
        logger.trace( "save" );
    }

    @Override
    public void close()
        throws MetadataRepositoryException
    {
        logger.trace( "close" );
    }

    @Override
    public void revert()
    {
        logger.trace( "revert" );
    }

    @Override
    public boolean canObtainAccess( Class<?> aClass )
    {
        return false;
    }

    @Override
    public <T> T obtainAccess( Class<T> aClass )
        throws MetadataRepositoryException
    {
        throw new IllegalArgumentException(
            "Access using " + aClass + " is not supported on the cassandra metadata storage" );
    }
}
