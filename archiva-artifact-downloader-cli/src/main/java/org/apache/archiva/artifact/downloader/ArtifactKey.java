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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ArtifactKey
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class ArtifactKey
{
    private String groupId;

    private String artifactId;

    private String version;

    public ArtifactKey()
    {
        /* default constructor */
    }

    public ArtifactKey( String mergedKey )
    {
        Pattern pat = Pattern.compile( "^([^:]*):([^:]*):(.*)$" );
        Matcher mat = pat.matcher( mergedKey );
        if ( mat.matches() )
        {
            this.groupId = mat.group( 1 );
            this.artifactId = mat.group( 2 );
            this.version = mat.group( 3 );
        }
        else
        {
            throw new IllegalArgumentException( "Key \"" + mergedKey
                + "\" is not a merged key in the format \"groupId:artifactId:version\"." );
        }
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

}
