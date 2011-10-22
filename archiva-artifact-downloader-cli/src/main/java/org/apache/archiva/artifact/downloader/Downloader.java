package org.apache.archiva.artifact.downloader;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.archiva.artifact.downloader.config.Config;
import org.apache.archiva.commons.transfer.Transfer;
import org.apache.archiva.commons.transfer.TransferDownloader;
import org.apache.archiva.commons.transfer.TransferException;
import org.apache.archiva.commons.transfer.TransferFactory;
import org.apache.commons.io.FileUtils;

/**
 * Downloader
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class Downloader
{
    class DownloadTask
    {
        public URI remoteRepoRelURI;

        public File localFile;

        public DownloadTask checkfile( String ext )
            throws URISyntaxException
        {
            DownloadTask checktask = new DownloadTask();
            checktask.remoteRepoRelURI = new URI( this.remoteRepoRelURI.toASCIIString() + ext );
            checktask.localFile = new File( this.localFile.getAbsolutePath() + ext );
            return checktask;
        }
    }

    private File repositoryDir;

    private List<ArtifactKey> artifactKeys = new ArrayList<ArtifactKey>();

    private Config config;

    private List<DownloadTask> createDownloadTasks( LayoutUtil layout, Repository repository, ArtifactKey key )
        throws URISyntaxException
    {
        List<DownloadTask> tasks = new ArrayList<DownloadTask>();

        // Jar
        DownloadTask jarTask = createTask( layout, repository, key, null, "jar" );
        tasks.add( jarTask );
        tasks.add( jarTask.checkfile( ".sha1" ) );
        tasks.add( jarTask.checkfile( ".md5" ) );
        // Pom
        DownloadTask pomTask = createTask( layout, repository, key, null, "pom" );
        tasks.add( pomTask );
        tasks.add( pomTask.checkfile( ".sha1" ) );
        tasks.add( pomTask.checkfile( ".md5" ) );
        // Sources
        DownloadTask sourcesTask = createTask( layout, repository, key, "sources", "jar" );
        tasks.add( sourcesTask );
        tasks.add( sourcesTask.checkfile( ".sha1" ) );
        tasks.add( sourcesTask.checkfile( ".md5" ) );
        // Javadoc
        DownloadTask javadocTask = createTask( layout, repository, key, "javadoc", "jar" );
        tasks.add( javadocTask );
        tasks.add( javadocTask.checkfile( ".sha1" ) );
        tasks.add( javadocTask.checkfile( ".md5" ) );

        return tasks;
    }

    private DownloadTask createTask( LayoutUtil layout, Repository repository, ArtifactKey key, String classifier,
                                     String type )
        throws URISyntaxException
    {
        DownloadTask task = new DownloadTask();
        String format = layout.format( repository.getLayout(), key.getGroupId(), key.getArtifactId(), key.getVersion(),
                                       classifier, type );
        task.remoteRepoRelURI = new URI( format );
        task.localFile = new File( this.repositoryDir, layout.format( "default", key.getGroupId(), key.getArtifactId(),
                                                                      key.getVersion(), classifier, type ) );
        return task;
    }

    public void download()
        throws TransferException, IOException, URISyntaxException
    {
        for ( ArtifactKey key : artifactKeys )
        {
            downloadFromAllRepos( key );
        }
    }

    private void downloadFromAllRepos( ArtifactKey key )
        throws TransferException, IOException, URISyntaxException
    {
        LayoutUtil layout = new LayoutUtil( this.config );
        List<DownloadTask> downloadTasks;
        Transfer transfer;
        TransferDownloader transferDownloader;
        for ( Repository repository : config.getRepositories() )
        {
            transfer = TransferFactory.getTransfer( repository.getRootUri() );
            System.out.println( "URI: " + repository.getRootUri().toASCIIString() );
            transferDownloader = transfer.getDownloader( repository.getRootUri() );

            downloadTasks = createDownloadTasks( layout, repository, key );

            for ( DownloadTask task : downloadTasks )
            {
                if ( task.localFile.exists() )
                {
                    System.out.println( "Task Skipped (local file exists): " + task.localFile.getAbsolutePath() );
                    continue;
                }

                File tmpFile = File.createTempFile( "artifact-download.", ".dat" );
                try
                {
                    transferDownloader.download( task.remoteRepoRelURI, tmpFile );
                    if ( tmpFile.exists() && tmpFile.length() > 0 )
                    {
                        System.out.println( "Successful Transfer: " + task.localFile );
                        FileUtils.copyFile( tmpFile, task.localFile );
                    }
                }
                catch ( TransferException e )
                {
                    System.out.println( "Transfer Warning: " + e.getMessage() );
                }
                finally
                {
                    tmpFile.delete();
                }
            }
        }
    }

    public List<ArtifactKey> getArtifactKeys()
    {
        return artifactKeys;
    }

    public Config getConfig()
    {
        return config;
    }

    public File getRepositoryDir()
    {
        return repositoryDir;
    }

    public void setArtifactKeys( List<ArtifactKey> artifactKeys )
    {
        this.artifactKeys = artifactKeys;
    }

    public void setConfig( Config config )
    {
        this.config = config;
    }

    public void setRepositoryDir( File repositoryDir )
    {
        this.repositoryDir = repositoryDir;
    }
}
