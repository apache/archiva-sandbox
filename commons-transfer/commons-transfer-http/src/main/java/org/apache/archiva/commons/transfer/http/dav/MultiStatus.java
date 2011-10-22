package org.apache.archiva.commons.transfer.http.dav;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

/**
 * MultiStatus
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class MultiStatus
{
    public static MultiStatus parse( InputStream stream )
        throws IOException, SAXException
    {
        Digester digester = new Digester();

        digester.setValidating( false );
        digester.setNamespaceAware( true );

        digester.addObjectCreate( "multistatus", MultiStatus.class );
        digester.addSetProperties( "multistatus" );

        digester.addObjectCreate( "multistatus/response", DavResource.class );
        digester.addCallMethod( "multistatus/response/href", "setHref", 1 );
        digester.addCallParam( "multistatus/response/href", 0 );

        String propKey = "multistatus/response/propstat/prop";

        digester.addCallMethod( propKey + "/getcontenttype", "setContentType", 1 );
        digester.addCallParam( propKey + "/getcontenttype", 0 );
        digester.addCallMethod( propKey + "/getetag", "setEtag", 1 );
        digester.addCallParam( propKey + "/getetag", 0 );
        digester.addCallMethod( propKey + "/creationdate", "setCreationDate", 1 );
        digester.addCallParam( propKey + "/creationdate", 0 );
        digester.addCallMethod( propKey + "/getlastmodified", "setLastModified", 1 );
        digester.addCallParam( propKey + "/getlastmodified", 0 );

        digester.addCallMethod( propKey + "/resourcetype/collection", "setAsCollection" );

        digester.addCallMethod( "multistatus/response/propstat/status", "parseStatus", 1 );
        digester.addCallParam( "multistatus/response/propstat/status", 0 );

        digester.addSetNext( "multistatus/response", "addResource", DavResource.class.getName() );

        return (MultiStatus) digester.parse( stream );
    }

    private Map<String, DavResource> resources = new HashMap<String, DavResource>();

    public MultiStatus()
    {
        /* ignore */
    }

    public void addResource( DavResource resource )
    {
        this.resources.put( resource.getHref(), resource );
    }

    public List<DavResource> getCollectionResources()
    {
        List<DavResource> dirs = new ArrayList<DavResource>();

        for ( DavResource resource : resources.values() )
        {
            if ( resource.isCollection() )
            {
                dirs.add( resource );
            }
        }

        return dirs;
    }

    public List<DavResource> getFileResources()
    {
        List<DavResource> files = new ArrayList<DavResource>();

        for ( DavResource resource : resources.values() )
        {
            if ( !resource.isCollection() )
            {
                files.add( resource );
            }
        }

        return files;
    }

    public DavResource getResource( String href )
    {
        return resources.get( href );
    }

    public Collection<DavResource> getResources()
    {
        return resources.values();
    }
}
