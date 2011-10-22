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

import junit.framework.TestCase;

/**
 * ArtifactKeyTest
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class ArtifactKeyTest
    extends TestCase
{
    public void assertArtifactKey( String merged, String groupId, String artifactId, String version )
    {
        ArtifactKey key = new ArtifactKey( merged );
        assertNotNull( "ArtifactKey should never be null", key );
        assertEquals( "ArtifactKey.groupId", groupId, key.getGroupId() );
        assertEquals( "ArtifactKey.artifactId", artifactId, key.getArtifactId() );
        assertEquals( "ArtifactKey.version", version, key.getVersion() );
    }

    public void testConstructFromMergedKey()
    {
        assertArtifactKey( "commons-io:commons-io:1.0", "commons-io", "commons-io", "1.0" );

        assertArtifactKey( "org.apache.commons:commons-lang:3.0-SNAPSHOT", "org.apache.commons", "commons-lang",
                           "3.0-SNAPSHOT" );
    }
}
