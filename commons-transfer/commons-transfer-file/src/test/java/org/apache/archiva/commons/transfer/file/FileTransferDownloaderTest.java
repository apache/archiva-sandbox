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
 * FileTransferDownloaderTest
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class FileTransferDownloaderTest
    extends TestCase
{
    private void assertDownloadAbsolute( String testDir, String sourcePath, String destPath )
        throws TransferException, IOException, URISyntaxException
    {
        File targetFile = new File( "target/test-output/" + testDir, destPath );
        File sourceBaseDir = new File( "src/test/resources/transfer/file" );
        File sourceFile = new File( sourceBaseDir, sourcePath );

        sourceBaseDir.mkdirs();

        FileTransferDownloader downloader = new FileTransferDownloader( sourceBaseDir.toURI() );
        downloader.download( sourceFile.toURI(), targetFile );

        assertTrue( "File " + targetFile.getAbsolutePath() + " should exist.", targetFile.exists() );

        String expected = FileUtils.readFileToString( sourceFile, null );
        String actual = FileUtils.readFileToString( targetFile, null );
        assertEquals( "File contents should match.", expected, actual );
    }

    private void assertDownloadRelative( String testDir, String sourcePath, String destPath )
        throws TransferException, IOException, URISyntaxException
    {
        File targetFile = new File( "target/test-output/" + testDir, destPath );
        File sourceBaseDir = new File( "src/test/resources/transfer/file" );
        File sourceFile = new File( sourceBaseDir, sourcePath );

        sourceBaseDir.mkdirs();

        FileTransferDownloader downloader = new FileTransferDownloader( sourceBaseDir.toURI() );
        downloader.download( new URI( sourcePath ), targetFile );

        assertTrue( "File " + targetFile.getAbsolutePath() + " should exist.", targetFile.exists() );

        String expected = FileUtils.readFileToString( sourceFile, null );
        String actual = FileUtils.readFileToString( targetFile, null );
        assertEquals( "File contents should match.", expected, actual );
    }

    private void assertExceptionOnBadURI( FileTransferDownloader downloader, URI uri, String type )
        throws TransferException, IOException, URISyntaxException
    {
        try
        {
            downloader.download( uri, getOutputFile( "twain.txt" ) );
            fail( "Should have thrown a TransferException on using an " + type
                + " URI with the FileTransferDownloader." );
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

    private URI getSourceBaseURI()
    {
        return new File( "src/test/resources/transfer/file" ).toURI();
    }

    public void testDownloadDeepAbsoluteURI()
        throws TransferException, IOException, URISyntaxException
    {
        assertDownloadAbsolute( "downloader-deep-absolute", "SourceFile.txt", "mark_twain.txt" );
    }

    public void testDownloadDeepRelativeURI()
        throws TransferException, IOException, URISyntaxException
    {
        assertDownloadRelative( "downloader-deep-relative", "SourceFile.txt", "a/deep/dir/marktwain.txt" );
    }

    public void testDownloadInvalidURI()
        throws IOException, URISyntaxException, TransferException
    {
        FileTransferDownloader downloader = new FileTransferDownloader( getSourceBaseURI() );

        assertExceptionOnBadURI( downloader, new URI( "http://machine.com/" ), "http" );
        assertExceptionOnBadURI( downloader, new URI( "https://machine.com/" ), "https" );
        assertExceptionOnBadURI( downloader, new URI( "dav://machine.com/" ), "dav" );
        assertExceptionOnBadURI( downloader, new URI( "davs://machine.com/" ), "davs" );
        assertExceptionOnBadURI( downloader, new URI( "ftp://machine.com/" ), "ftp" );
        assertExceptionOnBadURI( downloader, new URI( "cifs://machine.com/" ), "cifs" );
    }

    public void testDownloadSimpleAbsoluteURI()
        throws TransferException, IOException, URISyntaxException
    {
        assertDownloadAbsolute( "downloader-simple-absolute", "SourceFile.txt", "mark_twain.txt" );
    }

    public void testDownloadSimpleRelativeURI()
        throws TransferException, IOException, URISyntaxException
    {
        assertDownloadRelative( "downloader-simple-relative", "SourceFile.txt", "mark_twain.txt" );
    }
}
