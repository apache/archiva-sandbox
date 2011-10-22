package org.apache.archiva.commons.transfer.http.dav;

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
import java.util.Set;

import org.apache.archiva.commons.transfer.TransferFileFilter;
import org.apache.archiva.commons.transfer.http.HttpTransferDownloader;

/**
 * DavTransferDownloaderTest
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class DavTransferDownloaderTest
    extends AbstractDavTestCase
{
    public void testDetectWebDav()
        throws Exception
    {
        // Setup
        File serverRoot = new File( "src/test/resources" );
        davServer = startDavServer( serverRoot );

        // Execute Test
        HttpTransferDownloader downloader = new HttpTransferDownloader( davServer.baseURI );

        assertTrue( "Should detect WebDav", downloader.isDav() );
    }

    public void testFileListing()
        throws Exception
    {
        // Setup
        File serverRoot = new File( "src/test/resources" );
        davServer = startDavServer( serverRoot );

        HttpTransferDownloader downloader = new HttpTransferDownloader( davServer.baseURI );

        Set<String> links = downloader.getListing( davServer.baseURI.resolve( "links" ), new TransferFileFilter()
        {
            public boolean accept( String potentialFile )
            {
                return ( potentialFile.endsWith( ".html" ) );
            }
        } );

        String[] expected = new String[] {
            "archiva-1.0.1.html",
            "commons-lang.html",
            "mevenide.html",
            "nekohtml.html",
            "net_sf.html",
            "org.codehaus.html" };

        assertEquals( "Get Listing", expected.length, links.size() );

        for ( String name : links )
        {
            System.err.println( "   \"" + name + "\"," );
        }

        for ( String expect : expected )
        {
            assertTrue( "Get Listing contains[" + expect + "]", links.contains( expect ) );
        }
    }
}
