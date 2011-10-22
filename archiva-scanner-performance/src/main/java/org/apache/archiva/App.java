package org.apache.archiva;

import org.apache.archiva.repository.scanner.DefaultRepositoryScanner;
import org.apache.archiva.repository.scanner.RepositoryScanStatistics;
import org.apache.archiva.repository.scanner.RepositoryScanner;
import org.apache.archiva.repository.scanner.RepositoryScannerException;
import org.apache.archiva.configuration.ManagedRepositoryConfiguration;
import org.apache.archiva.consumers.InvalidRepositoryContentConsumer;
import org.apache.archiva.consumers.KnownRepositoryContentConsumer;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Hello world!
 *
 * Note: run with -XX:NewSize=1024m to get accurate values for "used memory" You need to avoid any GC during the method,
 * which can be monitored with -verbose:gc. Even without the NewSize, watching the quantity of GC gives a reasonable
 * expectation of memory use.
 */
public class App
{
    private static final Runtime RUNTIME = Runtime.getRuntime();

    public static void main( String[] args )
        throws RepositoryScannerException, IOException, InterruptedException
    {
        File basedir = args.length > 0 ? new File( args[0] ) : new File( System.getProperty( "user.home" ),
                                                                         ".m2/repository" );

        String[] excludes = RepositoryScanner.IGNORABLE_CONTENT;

        gc();
        Thread.sleep( 3000 ); // help get accurate retained size

        // note first call has a disadvantage due to disk caching
        System.out.println( "ant scanner" );
        scanUsingAntScanner( basedir, excludes );

        System.out.println( "plexus-utils walker" );
        scanUsingUtilsWalker( basedir, Arrays.asList( excludes ) );

        System.out.println( "new walker" );
        scanUsingNewWalker( basedir, Arrays.asList( excludes ) );

        System.out.println( "commons-io walker" );
        scanUsingCommonsWalker( basedir, Arrays.asList( excludes ) );

        System.out.println( "Current repository scanner" );
        scanUsingRepositoryScanner( basedir, Arrays.asList( excludes ) );
        
        System.out.println( "plexus-utils scanner" );
        scanUsingUtilsScanner( basedir, excludes );
    }

    private static void scanUsingCommonsWalker( File basedir, List<String> excludes )
        throws IOException, InterruptedException
    {
        long startMemory = getUsedMemory();
        long start = System.currentTimeMillis();
        MyDirectoryWalker walker = new MyDirectoryWalker( basedir, excludes );
        walker.scan();
        printTime( start );
        showFreeMemory( startMemory );
        System.out.println( "Files: " + walker.getCount() );
    }

    private static void scanUsingAntScanner( File basedir, String[] excludes )
        throws InterruptedException
    {
        long startMemory = getUsedMemory();
        long start = System.currentTimeMillis();

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( basedir );
        scanner.setIncludes( new String[]{"**/*"} );
        scanner.setExcludes( excludes );
        scanner.scan();
        printTime( start );
        showFreeMemory( startMemory );
        System.out.println( "Files: " + scanner.getIncludedFiles().length );
    }

    private static void scanUsingUtilsScanner( File basedir, String[] excludes )
        throws InterruptedException
    {
        long startMemory = getUsedMemory();
        long start = System.currentTimeMillis();

        org.codehaus.plexus.util.DirectoryScanner scanner = new org.codehaus.plexus.util.DirectoryScanner();
        scanner.setBasedir( basedir );
        scanner.setIncludes( new String[]{"**/*"} );
        scanner.setExcludes( excludes );
        scanner.scan();
        printTime( start );
        showFreeMemory( startMemory );
        System.out.println( "Files: " + scanner.getIncludedFiles().length );
    }

    private static void scanUsingUtilsWalker( File basedir, List excludes )
        throws InterruptedException
    {
        long startMemory = getUsedMemory();
        long start = System.currentTimeMillis();
        org.codehaus.plexus.util.DirectoryWalker walker = new org.codehaus.plexus.util.DirectoryWalker();
        walker.setBaseDir( basedir );
        walker.setExcludes( excludes );
        PlexusUtilsDirectoryWalkListener walkListener = new PlexusUtilsDirectoryWalkListener();
        walker.addDirectoryWalkListener( walkListener );
        walker.scan();
        printTime( start );
        showFreeMemory( startMemory );
        System.out.println( "Files: " + walkListener.getCount() );
    }

    private static void scanUsingNewWalker( File basedir, List<String> excludes )
        throws InterruptedException
    {
        long startMemory = getUsedMemory();
        long start = System.currentTimeMillis();
        DirectoryWalker walker = new DirectoryWalker();
        walker.setBaseDir( basedir );
        walker.setExcludes( excludes );
        MyDirectoryWalkListener walkListener = new MyDirectoryWalkListener();
        walker.addDirectoryWalkListener( walkListener );
        walker.scan();
        printTime( start );
        showFreeMemory( startMemory );
        System.out.println( "Files: " + walkListener.getCount() );
    }

    private static void scanUsingRepositoryScanner( File basedir, List<String> excludes )
        throws RepositoryScannerException, InterruptedException
    {
        long startMemory = getUsedMemory();
        long start = System.currentTimeMillis();
        ManagedRepositoryConfiguration config = new ManagedRepositoryConfiguration();
        config.setLocation( basedir.getAbsolutePath() );

        RepositoryScanner scanner = new DefaultRepositoryScanner();
        RepositoryScanStatistics stats = scanner.scan( config, Collections.<KnownRepositoryContentConsumer>emptyList(),
                                                       Collections.<InvalidRepositoryContentConsumer>emptyList(),
                                                       excludes, RepositoryScanner.FRESH_SCAN );

        printTime( start );
        showFreeMemory( startMemory );
        System.out.println( stats.toDump( config ) );
    }

    private static void printTime( long start )
    {
        System.out.print( ( System.currentTimeMillis() - start ) + " ms; " );
    }

    private static long getUsedMemory()
        throws InterruptedException
    {
        gc();
        return RUNTIME.totalMemory() - RUNTIME.freeMemory();
    }

    private static void showFreeMemory( long startMemory )
        throws InterruptedException
    {
        long used = RUNTIME.totalMemory() - RUNTIME.freeMemory() - startMemory;
        long endMemory = getUsedMemory();
        System.out.print( "retained memory ~" + ( endMemory - startMemory ) + "; (used ~" + used + "); " );
    }

    private static void gc()
        throws InterruptedException
    {
        System.gc();
    }

    private static final class MyDirectoryWalker
        extends org.apache.commons.io.DirectoryWalker
    {
        private final File basedir;

        private int count = 0;

        public MyDirectoryWalker( File basedir, List<String> excludes )
        {
            super( new SelectorFileFilter( convertToPatterns( excludes ) ), -1 );

            this.basedir = basedir;
        }

        private static List<TokenizedPattern> convertToPatterns( List<String> excludes )
        {
            List<TokenizedPattern> patterns = new ArrayList<TokenizedPattern>( excludes.size() );

            for ( String exclude : excludes )
            {
                patterns.add( new TokenizedPattern( exclude ) );
            }

            return patterns;
        }

        public void scan()
            throws IOException
        {
            super.walk( basedir, null );
        }

        @Override
        protected void handleFile( File file, int depth, Collection results )
            throws IOException
        {
            count++;
        }

        public int getCount()
        {
            return count;
        }

        private static final class SelectorFileFilter
            implements FileFilter
        {
            private List<TokenizedPattern> excludes;

            public SelectorFileFilter( List<TokenizedPattern> excludes )
            {
                this.excludes = excludes;
            }

            public boolean accept( File pathname )
            {
                for ( TokenizedPattern exclude : excludes )
                {
                    if ( exclude.matchPath( pathname.getPath(), true ) )
                    {
                        return false;
                    }
                }
                return true;
            }
        }
    }

    @SuppressWarnings( {"UnusedDeclaration"} )
    private static abstract class AbstractDirectoryWalkListener
    {
        private int count = 0;

        public void directoryWalkStarting( File basedir )
        {
        }

        public void directoryWalkStep( int percentage, File file )
        {
            count++;
        }

        public void directoryWalkFinished()
        {
        }

        public void debug( String message )
        {
        }

        public int getCount()
        {
            return count;
        }
    }

    private static class PlexusUtilsDirectoryWalkListener
        extends AbstractDirectoryWalkListener
        implements org.codehaus.plexus.util.DirectoryWalkListener
    {

    }

    private static class MyDirectoryWalkListener
        extends AbstractDirectoryWalkListener
        implements DirectoryWalkListener
    {

    }

}
