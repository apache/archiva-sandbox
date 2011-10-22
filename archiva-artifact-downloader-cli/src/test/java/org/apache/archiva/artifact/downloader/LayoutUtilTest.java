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

import junit.framework.TestCase;

import org.apache.archiva.artifact.downloader.config.Config;

/**
 * LayoutUtilTest
 * 
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class LayoutUtilTest
    extends TestCase
{
    private void assertLayout( LayoutUtil layout, String layoutId, String expectedFormat, String artifactKey )
    {
        ArtifactKey key = new ArtifactKey( artifactKey );
        String actual = layout.format( layoutId, key.getGroupId(), key.getArtifactId(), key.getVersion() );
        assertEquals( "Layout.format()", expectedFormat, actual );
    }

    private void assertLayout( LayoutUtil layout, String layoutId, String expectedFormat, String artifactKey,
                               String classifier, String type )
    {
        ArtifactKey key = new ArtifactKey( artifactKey );
        String actual = layout.format( layoutId, key.getGroupId(), key.getArtifactId(), key.getVersion(), classifier,
                                       type );
        assertEquals( "Layout.format()", expectedFormat, actual );
    }

    public void testFormatDefault()
    {
        Config config = new Config();
        config
            .addLayoutPattern( "default",
                               "${groupIdPath}/${artifactId}/${version}/${artifactId}-${version}${classifier}.${type}" );

        LayoutUtil layout = new LayoutUtil( config );

        assertLayout( layout, "default", "commons-io/commons-io/1.0/commons-io-1.0.jar", "commons-io:commons-io:1.0" );
        assertLayout( layout, "default", "commons-io/commons-io/1.0/commons-io-1.0-site.pom",
                      "commons-io:commons-io:1.0", "site", "pom" );
        assertLayout( layout, "default", "org/apache/archiva/archiva-parent/4-SNAPSHOT/archiva-parent-4-SNAPSHOT.jar",
                      "org.apache.archiva:archiva-parent:4-SNAPSHOT" );
    }

    public void testFormatLegacy()
    {
        Config config = new Config();
        config.addLayoutPattern( "legacy", "${groupId}/${type}s/${artifactId}-${version}${classifier}.${type}" );

        LayoutUtil layout = new LayoutUtil( config );

        assertLayout( layout, "legacy", "commons-io/jars/commons-io-1.0.jar", "commons-io:commons-io:1.0" );
        assertLayout( layout, "legacy", "commons-io/poms/commons-io-1.0-site.pom", "commons-io:commons-io:1.0", "site",
                      "pom" );
        assertLayout( layout, "legacy", "org.apache.archiva/jars/archiva-parent-4-SNAPSHOT.jar",
                      "org.apache.archiva:archiva-parent:4-SNAPSHOT" );
    }
}
