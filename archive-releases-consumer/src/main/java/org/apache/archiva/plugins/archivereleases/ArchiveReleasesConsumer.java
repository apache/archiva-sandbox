package org.apache.archiva.plugins.archivereleases;

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

import org.apache.archiva.admin.model.beans.ManagedRepository;
import org.apache.archiva.configuration.ArchivaConfiguration;
import org.apache.archiva.configuration.FileTypes;
import org.apache.archiva.consumers.AbstractMonitoredConsumer;
import org.apache.archiva.consumers.ConsumerException;
import org.apache.archiva.consumers.KnownRepositoryContentConsumer;
import org.apache.archiva.redback.components.registry.Registry;
import org.apache.archiva.redback.components.registry.RegistryListener;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.SelectorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <code>ArchiveReleasesConsumer</code>
 */
@Service("knownRepositoryContentConsumer#archive-releases")
@Scope("prototype")
public class ArchiveReleasesConsumer
    extends AbstractMonitoredConsumer
    implements KnownRepositoryContentConsumer, RegistryListener
{

    private Logger log = LoggerFactory.getLogger( ArchiveReleasesConsumer.class );

    private String id = "archive-releases";

    private String description = "Find releases older than a certain age and move to an archived location.";

    @Inject
    private FileTypes filetypes;

    @Inject
    private ArchivaConfiguration configuration;

    private List<String> propertyNameTriggers = new ArrayList<>();

    private List<String> includes = new ArrayList<>();

    private ManagedRepository repository;

    /**
     * Date before which artifacts are archived. Default is 2 years ago.
     */
    @Value( "${archivereleases.age:2 years}" )
    private String age;

    @Value( "#{'${archivereleases.whitelist:**/**}'.split(',')}" )
    private List<String> whitelist;

    @Value( "#{'${archivereleases.blacklist:}'.split(',')}" )
    private List<String> blacklist = Collections.emptyList();

    @Value( "${archivereleases.repositoryArchiveBase:}" )
    private File repositoryArchiveBase;

    @Value( "${archivereleases.dryRun:true}" )
    private boolean dryRun;

    private File repositoryArchive;

    private long totalSize = 0;

    private Date olderThan;

    public void beginScan( ManagedRepository repository, Date whenGathered )
        throws ConsumerException
    {
        beginScan( repository, whenGathered, true );
    }

    public void beginScan( ManagedRepository repository, Date whenGathered, boolean executeOnEntireRepo )
        throws ConsumerException
    {
        olderThan = convertAge( age );

        this.repository = repository;
        log.info( "Beginning scan of repository [" + this.repository.getId() + "]" );

        if ( repositoryArchiveBase == null && !dryRun )
        {
            throw new IllegalArgumentException( "Must configure archivereleases.repositoryArchiveBase" );
        }

        repositoryArchive = new File( repositoryArchiveBase, repository.getId() );

        log.info( "Starting archive run:" +
                      "\n  - Releases before:   " + olderThan +
                      "\n  - Archive directory: " + repositoryArchive +
                      "\n  - Whitelist:         " + whitelist +
                      "\n  - Blacklist:         " + blacklist +
                      "\n  - Dry run:           " + dryRun );

        totalSize = 0;
    }

    private static Date convertAge( String age )
    {
        Calendar cal = Calendar.getInstance();

        Matcher m = Pattern.compile( "([0-9]+) (year|month|day)s?( ago)?" ).matcher( age );
        if ( !m.matches() )
        {
            throw new IllegalArgumentException( "Invalid age: " + age );
        }

        int amount = Integer.parseInt( m.group( 1 ) );
        String period = m.group( 2 );
        switch ( period )
        {
            case "year":
                cal.add( Calendar.YEAR, -amount );
                break;
            case "month":
                cal.add( Calendar.MONTH, -amount );
                break;
            case "day":
                cal.add( Calendar.DAY_OF_MONTH, -amount );
                break;
            default:
                throw new IllegalArgumentException( "Unexpected period: " + period );
        }

        return cal.getTime();
    }

    public void processFile( String path )
        throws ConsumerException
    {
        processFile( path, true );
    }

    public void processFile( String path, boolean executeOnEntireRepo )
        throws ConsumerException
    {
        File artifactFile = new File( repository.getLocation(), path );

        // NOTE: as below, assumes a Maven 2 repository layout
        if ( artifactFile.getParentFile().getName().endsWith( "-SNAPSHOT" ) )
        {
            log.debug( "Skipping entry [" + path + "] as it appears to be a snapshot" );
            return;
        }

        if ( filetypes.matchesDefaultExclusions( path ) )
        {
            log.debug( "Skipping entry [" + path + "] as it is in the default exclusions" );
            return;
        }

        if ( !matchesList( whitelist, path ) )
        {
            log.debug( "Skipping entry [" + path + "] as it does not match whitelist" );
            return;
        }

        if ( matchesList( blacklist, path ) )
        {
            log.debug( "Skipping entry [" + path + " as it matches the blacklist" );
            return;
        }

        if ( fileNewerThanTarget( artifactFile ) )
        {
            log.debug( "Skipping entry [" + path + "] from repository [" + repository.getId()
                           + "] as it is newer than the target of " + olderThan );
            return;
        }

        // look at other files in the directory, if any are new then don't archive
        // NOTE: this assumes a Maven 2 repository layout - would be good to get the repository API properly extracted
        //  without depending on the metadata storage
        File dir = artifactFile.getParentFile();
        for ( File f : dir.listFiles() )
        {
            if ( !filetypes.matchesDefaultExclusions( f.getName() ) )
            {
                if ( matchesList( includes, f.getName() ) )
                {
                    if ( fileNewerThanTarget( f ) )
                    {
                        log.debug( "Skipping entry [" + path + "] from repository [" + repository.getId()
                                       + "] as another artifact file [" + f.getName() + "] is newer than the target of "
                                       + olderThan );
                        return;
                    }
                }
                else
                {
                    // File that is not an artifact and not excluded is not going to get found
                    log.warn( "Skipping entry [" + path + "] from repository [" + repository.getId()
                                  + "] due to immovable file [" + f.getName() + "]" );
                    return;
                }
            }
        }

        totalSize += artifactFile.length();

        File targetFile = new File( repositoryArchive, path );
        if ( dryRun )
        {
            log.info( "DRY RUN: would archive file [" + artifactFile.getAbsolutePath() + "] to [" + targetFile + "]" );
        }
        else
        {
            log.info( "archiving file [" + artifactFile.getAbsolutePath() + "] to [" + targetFile + "]" );

            try
            {
                FileUtils.moveFileToDirectory( artifactFile, targetFile.getParentFile(), true );
            }
            catch ( IOException e )
            {
                log.error(
                    "Error moving file [" + artifactFile.getAbsolutePath() + "] to [" + targetFile + "]: " + e.getLocalizedMessage(), e );
            }
        }
    }

    private boolean fileNewerThanTarget( File artifactFile )
    {
        return new Date( artifactFile.lastModified() ).after( olderThan );
    }

    private boolean matchesList( List<String> list, String path )
    {
        for ( String w : list )
        {
            if ( SelectorUtils.matchPath( w, path ) )
            {
                return true;
            }
        }
        return false;
    }

    public void completeScan()
    {
        completeScan( true );
    }

    public void completeScan( boolean executeOnEntireRepo )
    {
        log.info( "Finished scan of repository [" + this.repository.getId() + "]" );

        log.info( "Total size of artifacts archived: " + totalSize + " (" + this.repository.getLocation() + ")" );
    }

    /**
     * Used by archiva to determine if the consumer wishes to process all of a repository's entries or just those that
     * have been modified olderThan the last scan.
     *
     * @return boolean true if the consumer wishes to process all entries on each scan, false for only those modified
     * olderThan the last scan
     */
    public boolean isProcessUnmodified()
    {
        return super.isProcessUnmodified();
    }

    public void afterConfigurationChange( org.apache.archiva.redback.components.registry.Registry registry,
                                          String propertyName, Object propertyValue )
    {
        if ( propertyNameTriggers.contains( propertyName ) )
        {
            initIncludes();
        }
    }

    public void beforeConfigurationChange( Registry registry, String propertyName, Object propertyValue )
    {
        /* do nothing */
    }

    private void initIncludes()
    {
        // Pass through a set to find uniques
        Set<String> types = new HashSet<>();
        types.addAll( filetypes.getFileTypePatterns( FileTypes.ARTIFACTS ) );
        types.addAll( filetypes.getFileTypePatterns( FileTypes.INDEXABLE_CONTENT ) );

        includes.clear();
        includes.addAll( types );
    }

    @PostConstruct
    public void initialize()
    {
        propertyNameTriggers = new ArrayList<>();
        propertyNameTriggers.add( "repositoryScanning" );
        propertyNameTriggers.add( "fileTypes" );
        propertyNameTriggers.add( "fileType" );
        propertyNameTriggers.add( "patterns" );
        propertyNameTriggers.add( "pattern" );

        configuration.addChangeListener( this );

        initIncludes();
    }

    public String getId()
    {
        return this.id;
    }

    public String getDescription()
    {
        return this.description;
    }

    public List<String> getExcludes()
    {
        return null;
    }

    public List<String> getIncludes()
    {
        return this.includes;
    }

    public boolean isPermanent()
    {
        return false;
    }

    public void setRepositoryArchiveBase( File repositoryArchiveBase )
    {
        this.repositoryArchiveBase = repositoryArchiveBase;
    }
}
