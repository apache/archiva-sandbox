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
import java.util.Iterator;

/**
 * JibSource
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class JibSource
    implements Iterable<JibFile>
{
    private URI uri;

    private String id;

    private Calendar lastUpdate;

    public String getId()
    {
        return id;
    }

    public Calendar getLastUpdate()
    {
        return lastUpdate;
    }

    public URI getUri()
    {
        return uri;
    }

    public Iterator<JibFile> iterator()
    {
        return null;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public void setLastUpdate( Calendar lastUpdate )
    {
        this.lastUpdate = lastUpdate;
    }

    public void setUri( URI uri )
    {
        this.uri = uri;
    }
}
