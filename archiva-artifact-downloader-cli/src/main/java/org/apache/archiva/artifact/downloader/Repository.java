package org.apache.archiva.artifact.downloader;

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
import java.net.URISyntaxException;

/**
 * Repository
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class Repository
{
    private String layout;

    private URI rootUri;

    public String getLayout()
    {
        return layout;
    }

    public URI getRootUri()
    {
        return rootUri;
    }

    public void setLayout( String layout )
    {
        this.layout = layout;
    }

    public void setRootUri( String uri )
        throws URISyntaxException
    {
        this.rootUri = new URI( uri );
    }

    public void setRootUri( URI rootUri )
    {
        this.rootUri = rootUri;
    }
}
