package org.apache.archiva.artifact.downloader.config;

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
import java.io.Reader;

import org.apache.archiva.artifact.downloader.Repository;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

/**
 * ConfigParser
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class ConfigParser
{
    public static Config parseConfig( Reader reader )
        throws IOException, SAXException
    {
        Digester digester = new Digester();

        digester.setValidating( false );
        digester.setNamespaceAware( true );

        digester.addObjectCreate( "configuration", Config.class );
        digester.addSetProperties( "configuration" );

        digester.addCallMethod( "configuration/layouts/layout", "addLayoutPattern", 2 );
        digester.addCallParam( "configuration/layouts/layout", 0, "id" );
        digester.addCallParam( "configuration/layouts/layout", 1 );

        digester.addObjectCreate( "configuration/repositories/repository", Repository.class );
        digester.addSetProperties( "configuration/repositories/repository" );
        digester.addCallMethod( "configuration/repositories/repository", "setRootUri", 1 );
        digester.addCallParam( "configuration/repositories/repository", 0 );
        digester.addSetNext( "configuration/repositories/repository", "addRepository", Repository.class.getName() );

        return (Config) digester.parse( reader );
    }
}
