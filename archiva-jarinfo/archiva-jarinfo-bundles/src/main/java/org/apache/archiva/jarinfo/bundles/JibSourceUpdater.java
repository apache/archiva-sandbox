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

public interface JibSourceUpdater
{
    /**
     * Requests that the JibSource contents be updated.
     * 
     * @param jibSource the {@link JibSource} information to work off of.
     * @param jibSourceDir the directory to store the {@link JibFile}s. 
     * @return the number of {@link JibFile}s updated within {@link JibSource}.
     * @throws IOException if there was a problem updating the the {@link JibSource}.
     */
    public int update( JibSource jibSource, final File jibSourceDir )
        throws IOException;
}
