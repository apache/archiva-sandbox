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

import java.net.URI;
import java.util.Set;

/**
 * Transfer
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public interface Transfer
{
    /**
     * Add to the list of transfer monitors that are automatically
     * attached to all new {@link TransferDownloader} and {@link TransferUploader}
     * implementations.
     * 
     * @param monitor the monitor to add
     */
    public void addTransferMonitor( TransferMonitor monitor );

    /**
     * The Transfer specific downloader for the baseuri specified.
     * 
     * @param baseuri the Base (root) URI for the downloader.
     * @return the transfer downloader.
     * @throws TransferException 
     */
    public TransferDownloader getDownloader( URI baseuri )
        throws TransferException;

    /**
     * Get the set of protocols (in url notation) that the transport handles.
     * 
     * Example: <code>Set&lt;"http", "https"&gt;</code> or
     * <code>Set&lt;"dav", "davs"&gt;</code> or <code>Set&lt;"file"&gt;</code>
     * 
     * @return set of protocols handled by transfer transport.
     */
    public Set<String> getProtocols();

    /**
     * The Transfer specific uploader for the baseuri specified.
     * 
     * @param baseuri the Base (root) URI for the uploader.
     * @return the transfer uploader.
     * @throws TransferException
     */
    public TransferUploader getUploader( URI baseuri )
        throws TransferException;

    /**
     * Remove from the list of transfer monitors that are automatically
     * attached to all new {@link TransferDownloader} and {@link TransferUploader}
     * implementations.
     * 
     * @param monitor the monitor to remove
     */
    public void removeTransferMonitor( TransferMonitor monitor );
}
