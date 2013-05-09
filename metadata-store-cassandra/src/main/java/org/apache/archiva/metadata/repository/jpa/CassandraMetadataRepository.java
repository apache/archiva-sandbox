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
import org.apache.archiva.metadata.repository.jpa.model.Namespace;
import org.apache.archiva.metadata.repository.jpa.model.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Olivier Lamy
 */
public class CassandraMetadataRepository
    implements MetadataRepository
{

    private EntityManager entityManager;

    private ArchivaConfiguration configuration;

    private final Map<String, MetadataFacetFactory> metadataFacetFactories;

    public CassandraMetadataRepository( Map<String, MetadataFacetFactory> metadataFacetFactories,
                                        ArchivaConfiguration configuration, EntityManager entityManager )
    {
        this.metadataFacetFactories = metadataFacetFactories;
        this.configuration = configuration;
        this.entityManager = entityManager;
    }

    @Override
    public void updateNamespace( String repositoryId, String namespace )
        throws MetadataRepositoryException
    {
        Repository repository = this.entityManager.find( Repository.class, repositoryId );

        if ( repository == null )
        {
            repository = new Repository( repositoryId );
            this.entityManager.persist( repository );
            Namespace n = new Namespace( namespace );
            n.setRepository( repository );
            this.entityManager.persist( n );
            repository.getNamespaces().add( n );
            this.entityManager.persist( repository );
        }
        else
        {

            Namespace n = new Namespace( namespace );
            // contains the namespace ?
            if ( !repository.getNamespaces().contains( n ) )
            {
                entityManager.persist( n );
                repository.getNamespaces().add( n );
                entityManager.merge( repository );

            }
        }


    }

    @Override
    public void removeNamespace( String repositoryId, String namespace )
        throws MetadataRepositoryException
    {
        TypedQuery<Namespace> typedQuery =
            this.entityManager.createQuery( "select n from namespace n where n.id=:id and n.repository.id=:repoid",
                                            Namespace.class );

        typedQuery = typedQuery.setParameter( "id", namespace ).setParameter( "repoid", repositoryId );

        Namespace n = typedQuery.getSingleResult();

        if ( n != null )
        {

        }
        this.entityManager.remove( n );
    }

    @Override
    public Collection<String> getRootNamespaces( String repoId )
        throws MetadataResolutionException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<String> getNamespaces( String repoId, String namespace )
        throws MetadataResolutionException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
    public Collection<String> getRepositories()
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
    public void removeRepository( String repositoryId )
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
