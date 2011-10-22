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
import java.net.URI;
import java.util.Stack;

import org.apache.archiva.commons.transfer.http.HttpTransferUploader;
import org.apache.commons.io.FileUtils;

/**
 * DavTransferUploaderTest
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class DavTransferUploaderTest
    extends AbstractDavTestCase
{

    public void testGatherMissingFolderStack()
        throws Exception
    {
        // Setup
        File serverRoot = new File( "target/dav-tests/upload-gathermissing" );
        FileUtils.deleteDirectory( serverRoot );
        serverRoot.mkdirs();

        new File( serverRoot, "quotes/byauthor/f" ).mkdirs();

        davServer = startDavServer( serverRoot );

        // Execute Test
        HttpTransferUploader uploader = new HttpTransferUploader( davServer.baseURI );

        URI testURI = davServer.baseURI.resolve( "quotes/byauthor/f/franklin/benjamin" );
        Stack<URI> missing = uploader.davGetMissingPaths( testURI );

        // Verify Results
        String expectedPaths[] = new String[] {
            "/dav/quotes/byauthor/f/franklin/",
            "/dav/quotes/byauthor/f/franklin/benjamin/" };

        assertEquals( "Missing URI.size", expectedPaths.length, missing.size() );
        for ( String element : expectedPaths )
        {
            URI uri = missing.pop();
            assertEquals( "Expected Path", element, uri.getPath() );
        }
    }

    public void testUploadDeepFolder()
        throws Exception
    {
        // Setup
        File serverRoot = new File( "target/dav-tests/upload-deepfolder" );
        FileUtils.deleteDirectory( serverRoot );
        serverRoot.mkdirs();
        davServer = startDavServer( serverRoot );

        // Execute Test
        String RESOURCE = "quotes/byauthor/t/twain/mark_twain.txt";
        URI testURI = davServer.baseURI.resolve( RESOURCE );
        HttpTransferUploader uploader = new HttpTransferUploader( davServer.baseURI );

        File sourceFile = new File( "src/test/resources/MarkTwain.txt" );
        uploader.upload( sourceFile, testURI );

        // Verify Results
        File destFile = new File( serverRoot, RESOURCE );

        assertTrue( "File " + destFile.getAbsolutePath() + " should exist.", destFile.exists() );

        String expected = FileUtils.readFileToString( sourceFile, null );
        String actual = FileUtils.readFileToString( destFile, null );
        assertEquals( "File contents should match.", expected, actual );
    }

    public void testUploadSimple()
        throws Exception
    {
        // Setup
        File serverRoot = new File( "target/dav-tests/upload-simple" );
        FileUtils.deleteDirectory( serverRoot );
        serverRoot.mkdirs();
        davServer = startDavServer( serverRoot );

        // Execute Test
        URI testURI = davServer.baseURI.resolve( "twain.txt" );
        HttpTransferUploader uploader = new HttpTransferUploader( davServer.baseURI );

        File sourceFile = new File( "src/test/resources/MarkTwain.txt" );
        uploader.upload( sourceFile, testURI );

        // Verify Results
        File destFile = new File( serverRoot, "twain.txt" );

        assertTrue( "File " + destFile.getAbsolutePath() + " should exist.", destFile.exists() );

        String expected = FileUtils.readFileToString( sourceFile, null );
        String actual = FileUtils.readFileToString( destFile, null );
        assertEquals( "File contents should match.", expected, actual );
    }

    public void testUploadSingleFolder()
        throws Exception
    {
        // Setup
        File serverRoot = new File( "target/dav-tests/upload-singlefolder" );
        FileUtils.deleteDirectory( serverRoot );
        serverRoot.mkdirs();
        davServer = startDavServer( serverRoot );

        // Execute Test
        URI testURI = davServer.baseURI.resolve( "quotes/twain.txt" );
        HttpTransferUploader uploader = new HttpTransferUploader( davServer.baseURI );

        File sourceFile = new File( "src/test/resources/MarkTwain.txt" );
        uploader.upload( sourceFile, testURI );

        // Verify Results
        File destFile = new File( serverRoot, "quotes/twain.txt" );

        assertTrue( "File " + destFile.getAbsolutePath() + " should exist.", destFile.exists() );

        String expected = FileUtils.readFileToString( sourceFile, null );
        String actual = FileUtils.readFileToString( destFile, null );
        assertEquals( "File contents should match.", expected, actual );
    }

}
