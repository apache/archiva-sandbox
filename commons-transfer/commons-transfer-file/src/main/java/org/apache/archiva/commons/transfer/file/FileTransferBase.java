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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.archiva.commons.transfer.AbstractTransferBase;
import org.apache.commons.io.IOUtils;

/**
 * FileTransferBase
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class FileTransferBase
    extends AbstractTransferBase
{
    protected URI baseuri;

    protected File baseDir;

    public FileTransferBase( URI baseuri )
    {
        this.baseuri = baseuri.normalize();

        if ( !baseuri.isAbsolute() )
        {
            throw new IllegalArgumentException( "Base URI must be absolute (got \"" + baseuri.toASCIIString() + "\")." );
        }

        if ( !( "file".equals( baseuri.getScheme() ) ) )
        {
            throw new IllegalArgumentException( "Base URI for " + getClass().getName()
                + " must use file protocol (got \"" + baseuri.toASCIIString() + "\")" );
        }

        if ( !baseuri.toASCIIString().endsWith( "/" ) )
        {
            throw new IllegalArgumentException( "Base URI must end in '/' character \"" + baseuri + "\"" );
        }

        baseDir = new File( baseuri.getPath() );
    }

    private void assertIsFileURI( URI uri )
    {
        if ( uri.isAbsolute() && !( "file".equals( uri.getScheme() ) ) )
        {
            throw new IllegalArgumentException( "Invalid file:// URI \"" + uri.toASCIIString() + "\"" );
        }
    }

    protected void assertURI( URI uri )
    {
        if ( uri.isAbsolute() )
        {
            assertIsFileURI( uri );

            String testPath = new File( uri.getPath() ).getAbsolutePath();

            if ( !testPath.startsWith( baseDir.getAbsolutePath() ) )
            {
                throw new IllegalArgumentException( "Absolute URI \"" + uri.toASCIIString()
                    + "\" must be within base dir " + baseDir.getAbsolutePath() );
            }
        }
    }

    protected void copyFile( URI uri, File srcFile, File destFile )
        throws IOException
    {
        // Sanity Check.
        if ( srcFile == null )
        {
            throw new NullPointerException( "Source File must not be null" );
        }
        if ( destFile == null )
        {
            throw new NullPointerException( "Destination File must not be null" );
        }

        // Source should exist, as a file.
        if ( srcFile.exists() == false )
        {
            throw new FileNotFoundException( "Source '" + srcFile + "' does not exist" );
        }
        if ( srcFile.isFile() == false )
        {
            throw new IOException( "Source '" + srcFile + "' exists but is not a file (directory?)" );
        }

        // Are they the same file?
        if ( srcFile.getCanonicalPath().equals( destFile.getCanonicalPath() ) )
        {
            throw new IOException( "Source '" + srcFile + "' and destination '" + destFile + "' are the same" );
        }

        // Create the directories on the destination side (if needed)
        if ( ( destFile.getParentFile() != null ) && ( destFile.getParentFile().exists() == false ) )
        {
            if ( destFile.getParentFile().mkdirs() == false )
            {
                throw new IOException( "Destination '" + destFile + "' directory cannot be created" );
            }
        }

        // Can we write to the destination ?
        if ( destFile.exists() && ( destFile.canWrite() == false ) )
        {
            throw new IOException( "Destination '" + destFile + "' exists but is read-only" );
        }

        // Perform the copy
        FileInputStream input = null;
        FileOutputStream output = null;
        try
        {
            input = new FileInputStream( srcFile );
            output = new FileOutputStream( destFile );

            transfer( uri, input, output, srcFile.length() );
        }
        finally
        {
            IOUtils.closeQuietly( output );
            IOUtils.closeQuietly( input );
        }

        if ( srcFile.length() != destFile.length() )
        {
            throw new IOException( "Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'" );
        }

        // Preserve the file date/time
        destFile.setLastModified( srcFile.lastModified() );
    }
}
