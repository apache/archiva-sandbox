package org.apache.archiva.metadata.repository.jpa;

import org.apache.archiva.metadata.model.ArtifactMetadata;
import org.apache.archiva.metadata.model.MetadataFacet;
import org.apache.archiva.metadata.model.ProjectMetadata;
import org.apache.archiva.metadata.model.ProjectVersionMetadata;
import org.apache.archiva.metadata.model.ProjectVersionReference;
import org.apache.archiva.metadata.repository.MetadataRepository;
import org.apache.archiva.metadata.repository.MetadataRepositoryException;
import org.apache.archiva.metadata.repository.MetadataResolutionException;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Olivier Lamy
 */
public class JpaMetadataRepository
    implements MetadataRepository
{

    @Override
    public void updateNamespace( String repositoryId, String namespace )
        throws MetadataRepositoryException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeNamespace( String repositoryId, String namespace )
        throws MetadataRepositoryException
    {
        //To change body of implemented methods use File | Settings | File Templates.
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
