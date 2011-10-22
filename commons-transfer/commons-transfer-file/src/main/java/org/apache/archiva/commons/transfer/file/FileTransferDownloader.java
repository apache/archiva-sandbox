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
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.archiva.commons.transfer.TransferDownloader;
import org.apache.archiva.commons.transfer.TransferException;
import org.apache.archiva.commons.transfer.TransferFileFilter;

/**
 * FileTransferDownloader
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class FileTransferDownloader
    extends FileTransferBase
    implements TransferDownloader
{
    public FileTransferDownloader( URI baseuri )
        throws TransferException
    {
        super( baseuri );
    }

    public void download( URI uri, File outputFile )
        throws TransferException, IOException
    {
        assertURI( uri );
        URI sourceURI = baseuri.resolve( uri );
        File inputFile = new File( sourceURI.getPath() );
        copyFile( sourceURI, inputFile, outputFile );
    }

    public Set<String> getListing( URI uri, final TransferFileFilter filter )
        throws TransferException, IOException
    {
        assertURI( uri );
        Set<String> listing = new HashSet<String>();

        URI absoluteURI = baseuri.resolve( uri );

        File dir = new File( absoluteURI.getPath() );
        if ( dir.exists() && dir.isDirectory() )
        {
            File[] files = dir.listFiles( new FileFilter()
            {
                public boolean accept( File pathname )
                {
                    if ( pathname.isDirectory() )
                    {
                        return false;
                    }
                    return filter.accept( pathname.getName() );
                }
            } );

            for ( File file : files )
            {
                listing.add( file.getName() );
            }
        }
        return listing;
    }
}
