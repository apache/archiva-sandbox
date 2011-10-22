package org.apache.archiva.jarinfo.bundles;

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
import java.util.Calendar;
import java.util.List;

/**
 * JibSourceCatalog
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class JibSourceCatalog
{
    private List<URI> jibUris;

    private String name;

    private URI uri;

    /**
     * The frequency of the updates to the catalog. Measured in hours. eg, 24
     * hours means once every day. This is present to allow the remote server to
     * specify how often the clients should check for new Jib content.
     */
    private int updateFrequencyHours;

    private Calendar lastUpdate;

    public List<URI> getJibUris()
    {
        return jibUris;
    }

    public Calendar getLastUpdate()
    {
        return lastUpdate;
    }

    public String getName()
    {
        return name;
    }

    public int getUpdateFrequencyHours()
    {
        return updateFrequencyHours;
    }

    public URI getUri()
    {
        return uri;
    }

    public void setJibUris( List<URI> jibUris )
    {
        this.jibUris = jibUris;
    }

    public void setLastUpdate( Calendar lastUpdate )
    {
        this.lastUpdate = lastUpdate;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void setUpdateFrequencyHours( int updateFrequencyHours )
    {
        this.updateFrequencyHours = updateFrequencyHours;
    }

    public void setUri( URI uri )
    {
        this.uri = uri;
    }
}
