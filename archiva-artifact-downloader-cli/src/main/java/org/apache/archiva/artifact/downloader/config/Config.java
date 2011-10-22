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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.archiva.artifact.downloader.Repository;

/**
 * Config
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class Config
{
    private Map<String, String> layoutPatterns = new HashMap<String, String>();

    private List<Repository> repositories = new ArrayList<Repository>();

    public void addRepository( Repository repository )
    {
        this.repositories.add( repository );
    }

    public List<Repository> getRepositories()
    {
        return repositories;
    }

    public void setRepositories( List<Repository> repositories )
    {
        this.repositories = repositories;
    }

    public void addLayoutPattern( String key, String pattern )
    {
        this.layoutPatterns.put( key, pattern );
    }

    public String getLayoutPattern( String key )
    {
        return this.layoutPatterns.get( key );
    }

    public Map<String, String> getLayoutPatterns()
    {
        return layoutPatterns;
    }

    public void setLayoutPatterns( Map<String, String> layoutPatterns )
    {
        this.layoutPatterns = layoutPatterns;
    }
}
