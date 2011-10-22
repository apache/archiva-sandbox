package org.apache.maven.archiva.consumer.plugin;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.archiva.configuration.ArchivaConfiguration;
import org.apache.archiva.configuration.FileTypes;
import org.apache.archiva.configuration.ManagedRepositoryConfiguration;
import org.apache.archiva.consumers.AbstractMonitoredConsumer;
import org.apache.archiva.consumers.ConsumerException;
import org.apache.archiva.consumers.KnownRepositoryContentConsumer;
import org.apache.maven.archiva.indexer.search.CrossRepositorySearch;
import org.apache.maven.archiva.indexer.search.SearchResultHit;
import org.apache.maven.archiva.indexer.search.SearchResultLimits;
import org.apache.maven.archiva.indexer.search.SearchResults;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.registry.Registry;
import org.codehaus.plexus.registry.RegistryListener;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @plexus.component role="org.apache.archiva.consumers.KnownRepositoryContentConsumer"
 *                   role-hint="discover-new-artifact" instantiation-strategy="per-lookup"
 */
public class DiscoverNewArtifactConsumer
    extends AbstractMonitoredConsumer
    implements KnownRepositoryContentConsumer, RegistryListener, Initializable
{
    /**
     * @plexus.configuration default-value="discover-new-artifact"
     */
    private String id;

    /**
     * @plexus.configuration default-value="Discover new artifacts in the repository."
     */
    private String description;

    /**
     * @plexus.requirement
     */
    private FileTypes filetypes;

    /**
     * @plexus.requirement role-hint="default"
     */
    private CrossRepositorySearch repoSearch;

    /**
     * @plexus.requirement
     */
    private ArchivaConfiguration configuration;

    private List propertyNameTriggers = new ArrayList();

    private List includes = new ArrayList();

    private ManagedRepositoryConfiguration repository;

    private File dumpFile;

    static final String DUMP_FILE_NAME = "new-artifacts.zzz";

    public String getId()
    {
        return this.id;
    }

    public String getDescription()
    {
        return this.description;
    }

    public boolean isPermanent()
    {
        return false;
    }

    public List getExcludes()
    {
        return null;
    }

    public List getIncludes()
    {
        return this.includes;
    }

    public void beginScan( ManagedRepositoryConfiguration repository )
        throws ConsumerException
    {
        this.repository = repository;

        dumpFile = new File( repository.getLocation() + "/" + DUMP_FILE_NAME );

        try
        {
            if ( dumpFile.exists() )
            {
                dumpFile.delete();
                dumpFile = null;
                dumpFile = new File( repository.getLocation() + "/" + DUMP_FILE_NAME );
            }

            dumpFile.createNewFile();
        }
        catch ( IOException ie )
        {
            throw new ConsumerException( ie.getMessage() );
        }
    }

    public void processFile( String path )
        throws ConsumerException
    {
        String id = repository.getId() + "/" + path;

        boolean found = isFoundInRepository( path, id );

        if ( !found )
        {
            dumpToFile( id );
        }
    }

    private boolean isFoundInRepository( String path, String id )
    {
        List repoSearchList = new ArrayList();
        repoSearchList.add( repository.getId() );

        SearchResults results = repoSearch.searchForTerm( "guest", repoSearchList, path, new SearchResultLimits( 0 ) );

        List hits = results.getHits();
        boolean found = false;

        for ( Iterator iter = hits.iterator(); iter.hasNext(); )
        {
            SearchResultHit hit = (SearchResultHit) iter.next();
            if ( id.equalsIgnoreCase( hit.getUrl() ) )
            {
                found = true;
                break;
            }
        }
        return found;
    }

    public void completeScan()
    {
       // dumpToFile( "Scan Complete" );
    }

    public void afterConfigurationChange( Registry registry, String propertyName, Object propertyValue )
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
        includes.clear();
        includes.addAll( filetypes.getFileTypePatterns( FileTypes.INDEXABLE_CONTENT ) );
    }

    public void initialize()
        throws InitializationException
    {
        propertyNameTriggers = new ArrayList();
        propertyNameTriggers.add( "repositoryScanning" );
        propertyNameTriggers.add( "fileTypes" );
        propertyNameTriggers.add( "fileType" );
        propertyNameTriggers.add( "patterns" );
        propertyNameTriggers.add( "pattern" );

        configuration.addChangeListener( this );

        initIncludes();
    }

    private void dumpToFile( String id )
        throws ConsumerException
    {
        try
        {
            
             IOUtils.write(  IOUtils.toString( new FileInputStream( dumpFile ) ) + id +"\n" ,
                            new FileOutputStream( dumpFile ) );
        }
        catch ( IOException e )
        {
            throw new ConsumerException( "Error writing '" + id + "' to new artifacts file '"
                                          + dumpFile.getPath()  + "'", e );
        }
    }

    public CrossRepositorySearch getSearch()
    {
        return repoSearch;
    }
}
