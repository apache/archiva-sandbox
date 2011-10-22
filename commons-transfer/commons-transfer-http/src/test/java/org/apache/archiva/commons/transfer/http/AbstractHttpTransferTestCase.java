package org.apache.archiva.commons.transfer.http;

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
import java.net.URI;

import junit.framework.TestCase;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHandler;

/**
 * AbstractHttpTransferTestCase
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public abstract class AbstractHttpTransferTestCase
    extends TestCase
{
    class TestableHttpServer
    {
        public Server server;

        public URI baseURI;

        public File root;
    }

    protected TestableHttpServer httpServer;

    protected void shutdownServer( TestableHttpServer httpServer )
    {
        if ( httpServer != null )
        {
            if ( httpServer.server != null )
            {
                if ( httpServer.server.isRunning() )
                {
                    try
                    {
                        httpServer.server.stop();
                    }
                    catch ( Exception e )
                    {
                        e.printStackTrace( System.err );
                    }
                }
            }
        }
    }

    protected TestableHttpServer startHttpServer( File serverRoot )
        throws Exception
    {
        TestableHttpServer repo = new TestableHttpServer();
        repo.root = serverRoot;

        repo.server = new Server();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        repo.server.setHandler( contexts );

        SocketConnector connector = new SocketConnector();
        connector.setPort( 0 ); // 0 means, choose and empty port. (we'll find out which, later)

        repo.server.setConnectors( new Connector[] { connector } );

        String contextPath = "/content";

        ContextHandler context = new ContextHandler();
        context.setContextPath( contextPath );
        context.setResourceBase( repo.root.getAbsolutePath() );
        context.setAttribute( "dirAllowed", true );
        context.setAttribute( "maxCacheSize", 0 );
        ServletHandler servlet = new ServletHandler();
        servlet.addServletWithMapping( DefaultServlet.class.getName(), "/" );
        context.setHandler( servlet );
        contexts.addHandler( context );

        repo.server.start();

        int port = connector.getLocalPort();
        repo.baseURI = new URI( "http://localhost:" + port + contextPath + "/" );
        System.out.println( "Remote HTTP Server started on " + repo.baseURI.toASCIIString() );

        return repo;
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        shutdownServer( httpServer );
        super.tearDown();
    }

}
