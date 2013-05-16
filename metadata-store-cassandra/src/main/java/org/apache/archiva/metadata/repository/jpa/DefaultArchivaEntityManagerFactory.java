package org.apache.archiva.metadata.repository.jpa;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.google.common.collect.ImmutableMap;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolType;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.util.Properties;

/**
 * @author Olivier Lamy
 */
@Service( "archivaEntityManagerFactory#jpa-archiva" )
public class DefaultArchivaEntityManagerFactory
    implements ArchivaEntityManagerFactory
{

    @Inject
    private ApplicationContext applicationContext;

    private EntityManager entityManager;

    private static final String CLUSTER_NAME = "archiva";

    private static final String KEYSPACE_NAME = "ArchivaKeySpace";

    private Keyspace keyspace;

    private AstyanaxContext<Keyspace> keyspaceContext;


    @PostConstruct
    public void initialize()
        throws ConnectionException
    {

        //entityManager = Persistence.createEntityManagerFactory( "archiva", new Properties() ).createEntityManager();

        keyspaceContext = new AstyanaxContext.Builder().forCluster( CLUSTER_NAME ).forKeyspace(
            KEYSPACE_NAME ).withAstyanaxConfiguration(
            new AstyanaxConfigurationImpl().setDiscoveryType( NodeDiscoveryType.RING_DESCRIBE ).setConnectionPoolType(
                ConnectionPoolType.TOKEN_AWARE ) ).withConnectionPoolConfiguration(
            new ConnectionPoolConfigurationImpl( CLUSTER_NAME + "_" + KEYSPACE_NAME ).setSocketTimeout(
                30000 ).setMaxTimeoutWhenExhausted( 2000 ).setMaxConnsPerHost( 20 ).setInitConnsPerHost( 10 ).setSeeds(
                "localhost:9160" ) ).withConnectionPoolMonitor( new CountingConnectionPoolMonitor() ).buildKeyspace(
            ThriftFamilyFactory.getInstance() );

        keyspaceContext.start();

        keyspace = keyspaceContext.getClient();

        ImmutableMap<String, Object> options = ImmutableMap.<String, Object>builder().put( "strategy_options",
                                                                                           ImmutableMap.<String, Object>builder().put(
                                                                                               "replication_factor",
                                                                                               "1" ).build() ).put(
            "strategy_class", "SimpleStrategy" ).build();

        keyspace.createKeyspace( options );

    }

    @Override
    public EntityManager getEntityManager()
    {
        return entityManager;
    }

    @Override
    public Keyspace getKeyspace()
    {
        return keyspace;
    }
}
