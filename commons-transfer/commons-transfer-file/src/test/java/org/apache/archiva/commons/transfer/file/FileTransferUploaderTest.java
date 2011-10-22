package org.apache.archiva.commons.transfer.file;

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

import junit.framework.TestCase;

import org.apache.archiva.commons.transfer.TransferException;
import org.apache.commons.io.FileUtils;

/**
 * FileTransferUploaderTest
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class FileTransferUploaderTest
    extends TestCase
{
    private void assertExceptionOnBadURI( FileTransferUploader uploader, URI uri, String type )
        throws IOException, URISyntaxException, TransferException
    {
        try
        {
            uploader.upload( getTestFile( "twain.txt" ), uri );
            fail( "Should have thrown a TransferException on using an " + type + " URI with the FileTransferUploader." );
        }
        catch ( IllegalArgumentException e )
        {
            // Expected Path.
        }
    }

    private void assertUploadAbsolute( String testDir, String destPath )
        throws TransferException, IOException, URISyntaxException
    {
        File sourceFile = getTestFile( "SourceFile.txt" );
        File targetBaseDir = new File( "target/test-output/" + testDir );

        targetBaseDir.mkdirs();

        FileTransferUploader uploader = new FileTransferUploader( targetBaseDir.toURI() );
        uploader.upload( sourceFile, new File( targetBaseDir, destPath ).toURI() );

        File destFile = new File( targetBaseDir, destPath );

        assertTrue( "File " + destFile.getAbsolutePath() + " should exist.", destFile.exists() );

        String expected = FileUtils.readFileToString( sourceFile, null );
        String actual = FileUtils.readFileToString( destFile, null );
        assertEquals( "File contents should match.", expected, actual );
    }

    private void assertUploadRelative( String testDir, String destPath )
        throws TransferException, IOException, URISyntaxException
    {
        File sourceFile = getTestFile( "SourceFile.txt" );
        File targetBaseDir = new File( "target/test-output/" + testDir );

        targetBaseDir.mkdirs();

        FileTransferUploader uploader = new FileTransferUploader( targetBaseDir.toURI() );
        uploader.upload( sourceFile, new URI( destPath ) );

        File destFile = new File( targetBaseDir, destPath );

        assertTrue( "File " + destFile.getAbsolutePath() + " should exist.", destFile.exists() );

        String expected = FileUtils.readFileToString( sourceFile, null );
        String actual = FileUtils.readFileToString( destFile, null );
        assertEquals( "File contents should match.", expected, actual );
    }

    private URI getBaseURI( File dir )
        throws URISyntaxException
    {
        URI dirURI = dir.toURI();
        String uri = dirURI.toASCIIString();
        if ( uri.endsWith( "/" ) )
        {
            return new URI( uri );
        }

        return new URI( uri + "/" );
    }

    private File getTestFile( String path )
    {
        return new File( "src/test/resources/transfer/file", path );
    }

    public void testUploadDeepAbsoluteURI()
        throws TransferException, IOException, URISyntaxException
    {
        assertUploadAbsolute( "uploader-deep-absolute", "a/deep/dir/mark_twain.txt" );
    }

    public void testUploadDeepRelativeURI()
        throws TransferException, IOException, URISyntaxException
    {
        assertUploadRelative( "uploader-deep-relative", "a/deep/dir/marktwain.txt" );
    }

    public void testUploadInvalidURI()
        throws IOException, URISyntaxException, TransferException
    {
        URI baseURI = getBaseURI( new File( "target/test-output/transfer-file-uploader/" ) );
        FileTransferUploader uploader = new FileTransferUploader( baseURI );

        assertExceptionOnBadURI( uploader, new URI( "http://machine.com/" ), "http" );
        assertExceptionOnBadURI( uploader, new URI( "https://machine.com/" ), "https" );
        assertExceptionOnBadURI( uploader, new URI( "dav://machine.com/" ), "dav" );
        assertExceptionOnBadURI( uploader, new URI( "davs://machine.com/" ), "davs" );
        assertExceptionOnBadURI( uploader, new URI( "ftp://machine.com/" ), "ftp" );
        assertExceptionOnBadURI( uploader, new URI( "cifs://machine.com/" ), "cifs" );
    }

    public void testUploadSimpleAbsoluteURI()
        throws TransferException, IOException, URISyntaxException
    {
        assertUploadAbsolute( "uploader-simple-absolute", "mark_twain.txt" );
    }

    public void testUploadSimpleRelativeURI()
        throws TransferException, IOException, URISyntaxException
    {
        assertUploadRelative( "uploader-simple-relative", "mark_twain.txt" );
    }
}
