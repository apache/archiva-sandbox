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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.archiva.commons.transfer.Transfer;
import org.apache.archiva.commons.transfer.TransferDownloader;
import org.apache.archiva.commons.transfer.TransferException;
import org.apache.archiva.commons.transfer.TransferMonitor;
import org.apache.archiva.commons.transfer.TransferUploader;

/**
 * FileTransfer
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class FileTransfer
    implements Transfer
{
    private Set<String> protocols;

    private Map<URI, FileTransferDownloader> downloaders;

    private Map<URI, FileTransferUploader> uploaders;

    private List<TransferMonitor> monitors = new ArrayList<TransferMonitor>();

    public FileTransfer()
    {
        protocols = new HashSet<String>();
        protocols.add( "file" );

        downloaders = new WeakHashMap<URI, FileTransferDownloader>();
        uploaders = new WeakHashMap<URI, FileTransferUploader>();
    }

    public void addTransferMonitor( TransferMonitor monitor )
    {
        monitors.add( monitor );
    }

    public TransferDownloader getDownloader( URI baseuri )
        throws TransferException
    {
        FileTransferDownloader downloader = downloaders.get( baseuri );
        if ( downloader != null )
        {
            return downloader;
        }

        downloader = new FileTransferDownloader( baseuri );
        for ( TransferMonitor monitor : monitors )
        {
            downloader.addTransferMonitor( monitor );
        }
        downloaders.put( baseuri, downloader );

        return downloader;
    }

    public Set<String> getProtocols()
    {
        return protocols;
    }

    public TransferUploader getUploader( URI baseuri )
        throws TransferException
    {
        FileTransferUploader uploader = uploaders.get( baseuri );
        if ( uploader != null )
        {
            return uploader;
        }

        uploader = new FileTransferUploader( baseuri );
        for ( TransferMonitor monitor : monitors )
        {
            uploader.addTransferMonitor( monitor );
        }
        uploaders.put( baseuri, uploader );

        return uploader;
    }

    public void removeTransferMonitor( TransferMonitor monitor )
    {
        monitors.remove( monitor );
    }
}
