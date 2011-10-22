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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.archiva.artifact.downloader.config.Config;
import org.apache.archiva.artifact.downloader.config.ConfigParser;
import org.apache.archiva.commons.transfer.interactive.NetworkProxyDialog;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXException;

/**
 * CLI
 * 
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class CLI
{
    private static final String OPT_PROXY_CONFIG = "proxy-config";

    private static final String OPT_HELP = "?";

    private static final String OPT_REPO_DIR = "o";

    private static final String OPT_CONFIG = "c";

    public static void main( String[] args )
    {
        ( new CLI() ).execute( args );
    }

    private Options options;

    private void execute( String[] args )
    {
        CommandLineParser parser = new GnuParser();
        try
        {
            CommandLine cmdline = parser.parse( getOptions(), args );

            if ( cmdline.hasOption( OPT_HELP ) )
            {
                showHelp( getOptions() );
                return;
            }

            if ( cmdline.hasOption( OPT_PROXY_CONFIG ) )
            {
                SwingUtilities.invokeLater( new Runnable()
                {
                    public void run()
                    {
                        NetworkProxyDialog.collectAndSave();
                        System.exit( -1 );
                    }
                } );
                return;
            }

            List<ArtifactKey> artifactKeys = new ArrayList<ArtifactKey>();
            for ( String arg : cmdline.getArgs() )
            {
                artifactKeys.add( new ArtifactKey( arg ) );
            }

            if ( artifactKeys.isEmpty() )
            {
                System.err
                    .println( "ERROR: You must specify at least 1 Artifact Key in format groupId:artifactId:version" );
                showHelp( getOptions() );
                return;
            }

            Config config = loadDefaultConfig();

            if ( cmdline.hasOption( OPT_CONFIG ) )
            {
                String configPath = cmdline.getOptionValue( OPT_CONFIG );
                File configFile = new File( configPath );
                if ( configFile.exists() == false )
                {
                    System.err.println( "Unable to find config file: " + configFile.getAbsolutePath() );
                    return;
                }
                FileReader reader = new FileReader( configFile );
                config = ConfigParser.parseConfig( reader );
            }

            String outputDir = cmdline.getOptionValue( OPT_REPO_DIR );
            if ( outputDir == null )
            {
                outputDir = System.getProperty( "user.dir" );
            }

            File repoDir = new File( outputDir );
            System.out.println( ".\\ Archiva Artifact Downloader \\._______________" );

            Downloader downloader = new Downloader();
            downloader.setArtifactKeys( artifactKeys );
            downloader.setRepositoryDir( repoDir );
            downloader.setConfig( config );
            downloader.download();
        }
        catch ( MissingOptionException e )
        {
            System.err.println( "ERROR: " + e.getMessage() );
            showHelp( options );
        }
        catch ( ParseException e )
        {
            showHelp( options );
            e.printStackTrace( System.err );
        }
        catch ( Throwable t )
        {
            t.printStackTrace( System.err );
        }
    }

    private Options getOptions()
    {
        if ( options != null )
        {
            return options;
        }

        Option repoDir = new Option( OPT_REPO_DIR, "repoDir", true, "Repository Root Directory." );
        repoDir.setArgName( "dir" );
        repoDir.setRequired( false );

        Option configFile = new Option( OPT_CONFIG, "configFile", true, "Config File." );
        configFile.setArgName( "file" );
        configFile.setRequired( false );

        Option proxyConfig = OptionBuilder.withLongOpt( OPT_PROXY_CONFIG )
        .withDescription( "Network Proxy Configuration UI" )
        .isRequired( false ).create();

        Option help = new Option( OPT_HELP, "help", false, "Help" );
        help.setRequired( false );

        options = new Options();
        options.addOption( repoDir );
        options.addOption( configFile );
        options.addOption( proxyConfig );
        options.addOption( help );

        return options;
    }

    private Config loadDefaultConfig()
        throws IOException, SAXException
    {
        // Find the config in the jar.
        URL url = this.getClass().getClassLoader().getResource( "artifact-downloader.xml" );
        InputStreamReader reader = new InputStreamReader( url.openStream() );
        return ConfigParser.parseConfig( reader );
    }

    private void showHelp( Options options )
    {
        HelpFormatter help = new HelpFormatter();
        help.printHelp( "ArtifactDownloader.jar [options] [ [groupId:artifactId:version] ... ]", options );
    }
}
