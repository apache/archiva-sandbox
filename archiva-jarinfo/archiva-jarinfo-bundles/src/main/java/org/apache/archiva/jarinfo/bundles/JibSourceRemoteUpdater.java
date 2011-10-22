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

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * JibSourceRemoteUpdater
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public abstract class JibSourceRemoteUpdater
    implements JibSourceUpdater
{
    public abstract JibSourceCatalog getCatalog()
        throws IOException;

    public abstract boolean getJib( URI uri, File destFile )
        throws IOException;

    public int update( JibSource jibSource, File jibSourceDir )
        throws IOException
    {
        // TODO: check catalog update frequency against last update in cache.
        // TODO: get catalog.
        // TODO: fetch each file in catalog.
        return 0;
    }
}
