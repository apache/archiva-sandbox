package org.apache.archiva.commons.transfer.defaults;

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
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.archiva.commons.transfer.Transfer;
import org.apache.archiva.commons.transfer.TransferDiscovery;

/**
 * The default {@link TransferDiscovery} implementation that uses "/META-INF/commons-transfer.properties" 
 * files to discover available Transfer providers.
 * 
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class DefaultTransferDiscovery implements TransferDiscovery {

    private Set<Transfer> availableTransfers;

    public DefaultTransferDiscovery() {
        /* ignore */
    }

    public synchronized Set<Transfer> findAvailableTransfers() {
        if (availableTransfers == null) {
            availableTransfers = new HashSet<Transfer>();
            try {
                Enumeration<URL> en = this.getClass().getClassLoader().getResources("META-INF/commons-transfer.properties");
                while (en.hasMoreElements()) {
                    URL url = en.nextElement();
                    Properties props = new Properties();

                    InputStream stream = url.openStream();
                    props.load(stream);

                    String transferProvider = props.getProperty("commons-transfer.provider");

                    if ((transferProvider != null) && (transferProvider.length() > 0)) {
                        try {
                            Class<?> transferclazz = Class.forName(transferProvider);
                            Transfer transfer = (Transfer) transferclazz.newInstance();
                            availableTransfers.add(transfer);
                        } catch (ClassNotFoundException e) {
                            // Class Not Found.
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            // Cannot create a new Transfer instance.
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            // Cannot create new Transfer instance due to Security settings.
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                // Issue finding resources.
                e.printStackTrace();
            }
        }
        return availableTransfers;
    }

}
