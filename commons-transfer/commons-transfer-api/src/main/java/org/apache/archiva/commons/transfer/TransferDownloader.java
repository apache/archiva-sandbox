package org.apache.archiva.commons.transfer;

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
import java.util.Set;

/**
 * TransferDownloader
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public interface TransferDownloader
{
    /**
     * Add to the list of transfer monitors for the {@link TransferDownloader}.
     * 
     * @param monitor the monitor to add
     */
    public void addTransferMonitor( TransferMonitor monitor );

    /**
     * Download a remote resource URI into the local outputFile.
     * 
     * @param uri the remote resource URI to transfer from.
     * @param outputFile the local outputFile to transfer to.
     * @throws TransferException if there was a problem initiating the download.
     * @throws IOException if there was a problem transfering the content.
     */
    public void download( URI uri, File outputFile )
        throws TransferException, IOException;

    public Set<String> getListing( URI uri, final TransferFileFilter filter )
        throws TransferException, IOException;

    /**
     * Remove from the list of transfer monitors on the {@link TransferDownloader}.
     * 
     * @param monitor the monitor to remove
     */
    public void removeTransferMonitor( TransferMonitor monitor );
}
