package org.apache.archiva.metadata.repository.jcr;

import org.apache.archiva.metadata.model.ArtifactMetadata;
import org.apache.archiva.metadata.model.MetadataFacetFactory;
import org.apache.archiva.metadata.model.ProjectVersionMetadata;
import org.apache.archiva.metadata.repository.MetadataRepository;
import org.apache.archiva.metadata.repository.MetadataRepositoryException;
import org.apache.archiva.metadata.repository.file.FileMetadataRepository;
import org.apache.archiva.metadata.repository.storage.RepositoryStorageMetadataInvalidException;
import org.apache.archiva.metadata.repository.storage.RepositoryStorageMetadataNotFoundException;
import org.apache.archiva.metadata.repository.storage.maven2.Maven2RepositoryPathTranslator;
import org.apache.archiva.metadata.repository.storage.maven2.Maven2RepositoryStorage;
import org.apache.archiva.metadata.repository.storage.maven2.MavenArtifactFacet;
import org.apache.archiva.metadata.repository.storage.maven2.MavenArtifactFacetFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.archiva.configuration.ArchivaConfiguration;
import org.codehaus.plexus.spring.PlexusClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;

/**
 * Hello world!
 */
public class PopulateRepoApp
{
    private static final String REPO_ID = "internal";

    private static final String[] CONFIG_LOCATIONS =
        new String[]{"classpath*:META-INF/spring-context.xml", "classpath*:META-INF/plexus/components.xml",
            "classpath:META-INF/spring-context.xml"};

    private static Logger log = LoggerFactory.getLogger( PopulateRepoApp.class );

    public static void main( String[] args )
        throws RepositoryException, MetadataRepositoryException, RepositoryStorageMetadataNotFoundException,
        RepositoryStorageMetadataInvalidException
    {
        MavenArtifactFacetFactory factory = new MavenArtifactFacetFactory();

        Map<String, MetadataFacetFactory> metadataFacetFactories =
            Collections.<String, MetadataFacetFactory>singletonMap( MavenArtifactFacet.FACET_ID, factory );

        ConfigurableApplicationContext applicationContext = new PlexusClassPathXmlApplicationContext(
            CONFIG_LOCATIONS );

        MetadataRepository metadataRepository = null;
        try
        {
            File basedir;
            if ( args.length > 0 )
            {
                basedir = new File( args[0] );
            }
            else
            {
                basedir = new File( System.getProperty( "user.home" ),
                                    "Library/Application Support/Archiva/data/repositories/internal" );
            }

            PopulateRepoAppConfig archivaConfig = (PopulateRepoAppConfig) applicationContext.getBean(
                "archivaConfiguration" );
            archivaConfig.addManagedRepo( REPO_ID, basedir );

            metadataRepository = createJcrMetadataRepository( metadataFacetFactories );
//            metadataRepository = createFileMetadataRepository( metadataFacetFactories, archivaConfig );

            Maven2RepositoryPathTranslator translator = (Maven2RepositoryPathTranslator) applicationContext.getBean(
                "repositoryPathTranslator#maven2" );

            Maven2RepositoryStorage storage = (Maven2RepositoryStorage) applicationContext.getBean(
                "repositoryStorage#maven2" );

//            File dir = basedir;
            File dir = new File( basedir, "org/apache/archiva" );

            IOFileFilter dirFilter = new NotFileFilter( new WildcardFileFilter( new String[]{".index*", ".archiva"} ) );
            IOFileFilter fileFilter = new NotFileFilter( new WildcardFileFilter(
                new String[]{"*.asc", "*.md5", "*.sha1", "maven-metadata*.xml"} ) );
            Collection<File> files = FileUtils.listFiles( dir, fileFilter, dirFilter );

            int index = basedir.getAbsolutePath().length() + 1;

            int count = 0, total = files.size();
            System.out.println( "Processing " + total + " files" );
            for ( File f : files )
            {
                String path = f.getAbsolutePath().substring( index );

                ArtifactMetadata artifact = translator.getArtifactForPath( REPO_ID, path );
                artifact.setWhenGathered( new Date() );

                artifact.setFileLastModified( f.lastModified() );
                artifact.setSize( f.length() );

                // skipping checksums for speed

                try
                {
                    ProjectVersionMetadata metadata;
                    metadata = storage.readProjectVersionMetadata( REPO_ID, artifact.getNamespace(),
                                                                   artifact.getProject(),
                                                                   artifact.getProjectVersion() );

                    metadataRepository.updateProjectVersion( REPO_ID, artifact.getNamespace(), artifact.getProject(),
                                                             metadata );
                }
                catch ( Exception e )
                {
                    log.warn( "Unable to read POM, skipping: " + e.getMessage() );
                }

                metadataRepository.updateArtifact( REPO_ID, artifact.getNamespace(), artifact.getProject(),
                                                   artifact.getProjectVersion(), artifact );

                if ( ++count % 500 == 0 )
                {
                    System.out.println( "saving " + ( count * 100 / total ) + "%" );
                    metadataRepository.save();
                }
            }
            metadataRepository.save();
        }
        finally
        {
            if ( metadataRepository != null )
            {
                metadataRepository.close();
            }
            applicationContext.close();
        }
    }

    private static MetadataRepository createJcrMetadataRepository(
        Map<String, MetadataFacetFactory> metadataFacetFactories )
        throws RepositoryException
    {
        RepositoryConfig config = RepositoryConfig.create( "src/main/repository.xml", "jcr" );
        Repository repository = new TransientRepository( config );

        JcrMetadataRepository jcrMetadataRepository = new JcrMetadataRepository( metadataFacetFactories, repository );

        JcrMetadataRepository.initialize( jcrMetadataRepository.getJcrSession() );
        return jcrMetadataRepository;
    }

    private static MetadataRepository createFileMetadataRepository(
        Map<String, MetadataFacetFactory> metadataFacetFactories, ArchivaConfiguration archivaConfig )
        throws RepositoryException
    {
        return new FileMetadataRepository( metadataFacetFactories, archivaConfig );
    }
}
