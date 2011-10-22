package org.apache.archiva.commons.transfer.http;

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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.apache.archiva.commons.transfer.TransferException;
import org.apache.archiva.commons.transfer.TransferFileFilter;
import org.apache.commons.io.FileUtils;

/**
 * HttpTransferDownloaderTest
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class HttpTransferDownloaderTest
    extends AbstractHttpTransferTestCase
{

    private void assertDownloadAbsolute( String testDir, String sourcePath, String destPath )
        throws Exception
    {
        File serverRoot = new File( "src/test/resources" );
        this.httpServer = startHttpServer( serverRoot );

        File destFile = new File( "target/http-downloader/" + testDir, destPath );
        File sourceFile = new File( httpServer.root, sourcePath );
        URI sourceURI = httpServer.baseURI.resolve( sourcePath );

        destFile.getParentFile().mkdirs();

        HttpTransferDownloader downloader = new HttpTransferDownloader( httpServer.baseURI );
        downloader.download( sourceURI, destFile );

        assertTrue( "File " + destFile.getAbsolutePath() + " should exist.", destFile.exists() );

        String expected = FileUtils.readFileToString( sourceFile, null );
        String actual = FileUtils.readFileToString( destFile, null );
        assertEquals( "File contents should match.", expected, actual );
    }

    private void assertDownloadRelative( String testDir, String sourcePath, String destPath )
        throws Exception
    {
        File serverRoot = new File( "src/test/resources" );
        this.httpServer = startHttpServer( serverRoot );

        File destFile = new File( "target/http-downloader/" + testDir, destPath );
        File sourceFile = new File( httpServer.root, sourcePath );
        URI sourceURI = new URI( sourcePath );

        destFile.getParentFile().mkdirs();

        HttpTransferDownloader downloader = new HttpTransferDownloader( httpServer.baseURI );
        downloader.download( sourceURI, destFile );

        assertTrue( "File " + destFile.getAbsolutePath() + " should exist.", destFile.exists() );

        String expected = FileUtils.readFileToString( sourceFile, null );
        String actual = FileUtils.readFileToString( destFile, null );
        assertEquals( "File contents should match.", expected, actual );
    }

    private void assertExceptionOnBadURI( HttpTransferDownloader downloader, URI uri, String type )
        throws IOException, URISyntaxException, TransferException
    {
        try
        {
            downloader.download( uri, getOutputFile( "twain.txt" ) );
            fail( "Should have thrown a IllegalArgumentException on using an " + type
                + " URI with the HttpTransferDownloader." );
        }
        catch ( IllegalArgumentException e )
        {
            // Expected Path.
        }
    }

    private void assertExceptionOnInvalidDownloader( String uri, String protocol )
        throws TransferException, URISyntaxException
    {
        try
        {
            new HttpTransferDownloader( new URI( uri ) );
            fail( "Should have thrown a IllegalArgumentException on using an " + protocol
                + ":// URI to create a new HttpTransferDownloader." );
        }
        catch ( IllegalArgumentException e )
        {
            // Expected Path.
        }
    }

    private File getOutputFile( String path )
    {
        return new File( "target/test-output/transfer-file", path );
    }

    public void testDetecteNoWebDav()
        throws Exception
    {
        File serverRoot = new File( "src/test/resources" );
        this.httpServer = startHttpServer( serverRoot );

        HttpTransferDownloader downloader = new HttpTransferDownloader( httpServer.baseURI );
        assertFalse( "Should NOT detect WebDav", downloader.isDav() );
    }

    public void testDownloadDeepAbsoluteURI()
        throws Exception
    {
        assertDownloadAbsolute( "downloader-deep-absolute", "more/quotes/from/BenFranklin.txt", "franklin.txt" );
    }

    public void testDownloadDeepRelativeURI()
        throws Exception
    {
        assertDownloadRelative( "downloader-deep-relative", "more/quotes/from/BenFranklin.txt", "ben_franklin.txt" );
    }

    public void testDownloadInvalidURI()
        throws IOException, URISyntaxException, TransferException
    {
        HttpTransferDownloader downloader = new HttpTransferDownloader( new URI( "http://localhost/" ) );

        assertExceptionOnBadURI( downloader, new URI( "file://C:/temp/" ), "file" );
        assertExceptionOnBadURI( downloader, new URI( "dav://machine.com/" ), "dav" );
        assertExceptionOnBadURI( downloader, new URI( "davs://machine.com/" ), "davs" );
        assertExceptionOnBadURI( downloader, new URI( "ftp://machine.com/" ), "ftp" );
        assertExceptionOnBadURI( downloader, new URI( "cifs://machine.com/" ), "cifs" );
    }

    public void testDownloadSimpleAbsoluteURI()
        throws Exception
    {
        assertDownloadAbsolute( "downloader-simple-absolute", "MarkTwain.txt", "twain.txt" );
    }

    public void testDownloadSimpleRelativeURI()
        throws Exception
    {
        assertDownloadRelative( "downloader-simple-relative", "MarkTwain.txt", "mark_twain.txt" );
    }

    public void testFileListing()
        throws Exception
    {
        File serverRoot = new File( "src/test/resources/links" );
        this.httpServer = startHttpServer( serverRoot );

        HttpTransferDownloader downloader = new HttpTransferDownloader( httpServer.baseURI );

        Set<String> links = downloader.getListing( httpServer.baseURI, new TransferFileFilter()
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

        for ( String expect : expected )
        {
            assertTrue( "Get Listing", links.contains( expect ) );
        }
    }

    public void testInvalidDownloaderURIs()
        throws IOException, URISyntaxException, TransferException
    {
        assertExceptionOnInvalidDownloader( "file://C:/temp/", "file" );
        assertExceptionOnInvalidDownloader( "udp://machine.com/", "dav" );
        assertExceptionOnInvalidDownloader( "httpu://machine.com/", "davs" );
        assertExceptionOnInvalidDownloader( "ftp://machine.com/", "ftp" );
        assertExceptionOnInvalidDownloader( "cifs://machine.com/", "cifs" );
    }
}
