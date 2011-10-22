package org.apache.archiva.artifact.downloader.config;

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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.archiva.artifact.downloader.Repository;
import org.xml.sax.SAXException;

/**
 * ConfigParserTest
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class ConfigParserTest
    extends TestCase
{
    private File basedir;

    public File getBasedir()
    {
        if ( basedir == null )
        {
            String dirprop = System.getProperty( "basedir" );
            if ( dirprop == null )
            {
                dirprop = System.getProperty( "user.dir" );
            }
            basedir = new File( dirprop );
        }

        return basedir;
    }

    public File getTestFile( String filename )
    {
        return new File( getTestResourcesDir(), filename );
    }

    public File getTestResourcesDir()
    {
        return new File( getBasedir(), "src/test/resources" );
    }

    public void testParseConfig()
        throws IOException, SAXException
    {
        File configXml = getTestFile( "config/artifact-downloader.xml" );
        FileReader reader = new FileReader( configXml );

        Config config = ConfigParser.parseConfig( reader );
        assertNotNull( "Config should never be null", config );

        assertEquals( "Config layouts count", 2, config.getLayoutPatterns().size() );
        assertEquals( "Config repositories count", 3, config.getRepositories().size() );

        List<String> expectedUris = new ArrayList<String>();
        expectedUris.add( "http://download.java.net/maven/2/" );
        expectedUris.add( "http://download.java.net/maven/1/" );
        expectedUris.add( "http://repo1.maven.org/maven2/" );

        List<String> actualUris = new ArrayList<String>();
        for ( Repository repository : config.getRepositories() )
        {
            String rooturi = repository.getRootUri().toASCIIString();
            actualUris.add( rooturi );
            assertNotNull( "URI should not be null", rooturi );
        }

        Collections.sort( expectedUris );
        Collections.sort( actualUris );

        // Compare actual vs expected
        for ( int i = 0; i < expectedUris.size(); i++ )
        {
            String expected = expectedUris.get( i );
            String actual = actualUris.get( i );
            assertEquals( "URI[" + i + "]", expected, actual );
        }
    }
}
