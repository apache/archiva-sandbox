package org.apache.archiva.artifact.downloader;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import org.apache.archiva.artifact.downloader.config.Config;

/**
 * LayoutUtil
 * 
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class LayoutUtil
{
    private Config config;

    public LayoutUtil( Config config )
    {
        this.config = config;
    }

    public String format( String layoutId, String groupId, String artifactId, String version )
    {
        return format( layoutId, groupId, artifactId, version, null, "jar" );
    }

    public String format( String layoutId, String groupId, String artifactId, String version, String classifier,
                          String type )
    {
        String pattern = config.getLayoutPattern( layoutId );

        pattern = pattern.replace( "${groupId}", groupId );
        pattern = pattern.replace( "${groupIdPath}", groupId.replace( ".", "/" ) );
        pattern = pattern.replace( "${artifactId}", artifactId );
        pattern = pattern.replace( "${version}", version );
        if ( classifier != null )
        {
            pattern = pattern.replace( "${classifier}", "-" + classifier );
        }
        else
        {
            pattern = pattern.replace( "${classifier}", "" );
        }
        pattern = pattern.replace( "${type}", type );

        return pattern;
    }
}
