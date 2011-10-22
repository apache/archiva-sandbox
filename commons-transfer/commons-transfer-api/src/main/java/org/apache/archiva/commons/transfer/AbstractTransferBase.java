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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * AbstractTransferBase
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public abstract class AbstractTransferBase
{
    private static final int BUFSIZE = 8096;

    protected List<TransferMonitor> monitors = new ArrayList<TransferMonitor>();

    public void addTransferMonitor( TransferMonitor monitor )
    {
        monitors.add( monitor );
    }

    public void removeTransferMonitor( TransferMonitor monitor )
    {
        monitors.remove( monitor );
    }

    protected long transfer( URI uri, InputStream input, OutputStream output, long expectedSize )
        throws IOException
    {
        triggerStart( uri, expectedSize );

        try
        {
            byte[] buffer = new byte[BUFSIZE];
            long count = 0;
            int n = 0;
            while ( -1 != ( n = input.read( buffer ) ) )
            {
                output.write( buffer, 0, n );
                count += n;
                if ( count > 0 )
                {
                    triggerProgress( uri, count, expectedSize );
                }
            }
            triggerFinished( uri, count );

            return count;
        }
        catch ( IOException e )
        {
            triggerFailure( uri, -1 );
            throw e;
        }
    }

    protected void triggerFailure( URI uri, int status )
    {
        for ( TransferMonitor monitor : monitors )
        {
            monitor.transferFailed( uri, status );
        }
    }

    protected void triggerFinished( URI uri, long downloadedSize )
    {
        for ( TransferMonitor monitor : monitors )
        {
            monitor.transferFinished( uri, downloadedSize );
        }
    }

    protected void triggerProgress( URI uri, long count, long expectedSize )
    {
        for ( TransferMonitor monitor : monitors )
        {
            monitor.transferProgress( uri, count, expectedSize );
        }
    }

    protected void triggerStart( URI uri, long expectedSize )
    {
        for ( TransferMonitor monitor : monitors )
        {
            monitor.transferStarted( uri, expectedSize );
        }
    }
}
