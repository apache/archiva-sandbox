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
import java.util.HashMap;
import java.util.Map;

import org.apache.archiva.commons.transfer.defaults.DefaultTransferDiscovery;

/**
 * The main factory for obtaining Transfer implementations.
 * 
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class TransferFactory
{
    private static TransferDiscovery transferDiscovery = new DefaultTransferDiscovery();

    private static Map<String, Transfer> transferByProtocol = new HashMap<String, Transfer>();

    public static void addTransfer( Transfer transfer )
    {
        for ( String protocol : transfer.getProtocols() )
        {
            transferByProtocol.put( protocol, transfer );
        }
    }

    public static Transfer getTransfer( URI uri )
        throws TransferException
    {
        synchronized ( transferByProtocol )
        {
            if ( transferByProtocol.isEmpty() )
            {
                resetTransfers();
            }
        }

        String protocol = uri.getScheme();
        synchronized ( transferByProtocol )
        {
            Transfer transport = transferByProtocol.get( protocol );
            if ( transport == null )
            {
                throw new TransferException( "No transfer transport available for protocol [" + protocol + "]." );
            }

            return transport;
        }
    }

    public static TransferDiscovery getTransferDiscovery()
    {
        return transferDiscovery;
    }

    private static void resetTransfers()
    {
        synchronized ( transferByProtocol )
        {
            for ( Transfer transfer : transferDiscovery.findAvailableTransfers() )
            {
                addTransfer( transfer );
            }
        }
    }

    public static void setTransferDiscovery( TransferDiscovery discovery )
    {
        TransferFactory.transferDiscovery = discovery;
        resetTransfers();
    }
}
