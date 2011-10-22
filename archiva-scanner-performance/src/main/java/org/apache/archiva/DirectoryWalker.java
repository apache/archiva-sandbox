package org.apache.archiva;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * DirectoryWalker.
 *
 * @version $Id: DirectoryWalker.java 5958 2007-02-28 10:29:55Z olamy $
 */
public class DirectoryWalker
{
    private File baseDir;

    private int baseDirOffset;

    private List<TokenizedPattern> excludes;

    private boolean isCaseSensitive = true;

    private List<DirectoryWalkListener> listeners;

    public static final String[] DEFAULTEXCLUDES = {
        // Miscellaneous typical temporary files
        "**/*~", "**/#*#", "**/.#*", "**/%*%", "**/._*",

        // CVS
        "**/CVS", "**/CVS/**", "**/.cvsignore",

        // SCCS
        "**/SCCS", "**/SCCS/**",

        // Visual SourceSafe
        "**/vssver.scc",

        // Subversion
        "**/.svn", "**/.svn/**",

        // Arch
        "**/.arch-ids", "**/.arch-ids/**",

        //Bazaar
        "**/.bzr", "**/.bzr/**",

        //SurroundSCM
        "**/.MySCMServerInfo",

        // Mac
        "**/.DS_Store"};

    public DirectoryWalker()
    {
        this.excludes = new ArrayList<TokenizedPattern>();
        this.listeners = new ArrayList<DirectoryWalkListener>();
    }

    public void addDirectoryWalkListener( DirectoryWalkListener listener )
    {
        this.listeners.add( listener );
    }

    public void addExclude( String exclude )
    {
        this.excludes.add( fixPattern( exclude ) );
    }

    /**
     * Add's to the Exclude List the default list of SCM excludes.
     */
    public void addSCMExcludes()
    {
        String scmexcludes[] = DEFAULTEXCLUDES;
        for ( String scmexclude : scmexcludes )
        {
            addExclude( scmexclude );
        }
    }

    private void fireStep( File file )
    {
        for ( DirectoryWalkListener listener : this.listeners )
        {
            listener.directoryWalkStep( 0, file );
        }
    }

    private void fireWalkFinished()
    {
        for ( DirectoryWalkListener listener : this.listeners )
        {
            listener.directoryWalkFinished();
        }
    }

    private void fireWalkStarting()
    {
        for ( DirectoryWalkListener listener : this.listeners )
        {
            listener.directoryWalkStarting( this.baseDir );
        }
    }

    private TokenizedPattern fixPattern( String pattern )
    {
        String cleanPattern = pattern;

        if ( File.separatorChar != '/' )
        {
            cleanPattern = cleanPattern.replace( '/', File.separatorChar );
        }

        if ( File.separatorChar != '\\' )
        {
            cleanPattern = cleanPattern.replace( '\\', File.separatorChar );
        }

        return new TokenizedPattern( cleanPattern );
    }

    /**
     * @return Returns the baseDir.
     */
    public File getBaseDir()
    {
        return baseDir;
    }

    /**
     * @return Returns the excludes.
     */
    public List getExcludes()
    {
        return excludes;
    }

    private boolean isExcluded( String name )
    {
        return isMatch( this.excludes, name );
    }

    private boolean isMatch( List<TokenizedPattern> patterns, String name )
    {
        for ( TokenizedPattern pattern : patterns )
        {
            if ( pattern.matchPath( name, isCaseSensitive ) )
            {
                return true;
            }
        }

        return false;
    }

    private String relativeToBaseDir( File file )
    {
        return file.getAbsolutePath().substring( baseDirOffset + 1 );
    }

    /**
     * Removes a DirectoryWalkListener.
     *
     * @param listener the listener to remove.
     */
    public void removeDirectoryWalkListener( DirectoryWalkListener listener )
    {
        this.listeners.remove( listener );
    }

    /**
     * Performs a Scan against the provided {@link #setBaseDir(java.io.File)}
     */
    public void scan()
    {
        if ( baseDir == null )
        {
            throw new IllegalStateException( "Scan Failure.  BaseDir not specified." );
        }

        if ( !baseDir.exists() )
        {
            throw new IllegalStateException( "Scan Failure.  BaseDir does not exist." );
        }

        if ( !baseDir.isDirectory() )
        {
            throw new IllegalStateException( "Scan Failure.  BaseDir is not a directory." );
        }

        fireWalkStarting();
        scanDir( this.baseDir );
        fireWalkFinished();
    }

    private void scanDir( File dir )
    {
        File files[] = dir.listFiles();

        if ( files == null )
        {
            return;
        }

        for ( File file : files )
        {
            String name = relativeToBaseDir( file );

            if ( isExcluded( name ) )
            {
                continue;
            }

            if ( file.isDirectory() )
            {
                scanDir( file );
            }
            else
            {
                fireStep( file );
            }
        }
    }

    /**
     * @param baseDir The baseDir to set.
     */
    public void setBaseDir( File baseDir )
    {
        this.baseDir = baseDir;
        this.baseDirOffset = baseDir.getAbsolutePath().length();
    }

    /**
     * @param entries The excludes to set.
     */
    public void setExcludes( List<String> entries )
    {
        this.excludes.clear();
        if ( entries != null )
        {
            for ( String entry : entries )
            {
                this.excludes.add( fixPattern( entry ) );
            }
        }
    }
}