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

/**
 * Transfer Transfer Monitor for transfer events
 * 
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public interface TransferMonitor
{
    /**
     * A transfer failed.
     * 
     * @param url the url that was attempted.
     * @param status the http status code for the failure.
     */
    public void transferFailed( URI uri, int status );

    /**
     * A transfer has completed successfully.
     * 
     * @param url the url that was transfered
     * @param size the size, in bytes, that was transfered.
     */
    public void transferFinished( URI uri, long size );

    /**
     * A transfer progress event.
     * 
     * @param url theh url that is being transfered.
     * @param current the current amount of bytes transfered so far.
     * @param total the total number of expected bytes for this transfer.
     */
    public void transferProgress( URI uri, long current, long total );

    /**
     * A transfer has started.
     * 
     * @param url the url that has been started.
     * @param expectedSize the size expected to transfer.
     * @see #transferFailed(String, int)
     * @see #transferProgress(String, long, long)
     */
    public void transferStarted( URI uri, long expectedSize );
}
