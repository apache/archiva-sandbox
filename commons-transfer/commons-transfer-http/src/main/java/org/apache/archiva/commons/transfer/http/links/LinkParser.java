package org.apache.archiva.commons.transfer.http.links;

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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.archiva.commons.transfer.TransferFileFilter;
import org.apache.commons.lang.StringUtils;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * LinkParser
 * 
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class LinkParser
{
    private Set<Pattern> skips = new HashSet<Pattern>();

    public LinkParser()
    {
        // Apache Fancy Index Sort Headers
        skips.add( Pattern.compile( "\\?C.*=.*" ) );

        // URLs with excessive paths.
        skips.add( Pattern.compile( "/[^/]*/" ) );

        // URLs that to a parent directory.
        skips.add( Pattern.compile( "\\.\\./" ) );
    }

    public String cleanLink( URI baseURI, String link )
    {
        if ( StringUtils.isBlank( link ) )
        {
            return "";
        }

        String ret = link;

        try
        {
            URI linkuri = new URI( ret );
            URI relativeURI = baseURI.relativize( linkuri ).normalize();
            ret = relativeURI.toASCIIString();
            if ( ret.startsWith( baseURI.getPath() ) )
            {
                ret = ret.substring( baseURI.getPath().length() );
            }
        }
        catch ( URISyntaxException e )
        {
        }

        return ret;
    }

    public Set<String> collectLinks( URI baseURI, InputStream stream, TransferFileFilter filter )
        throws SAXException, IOException
    {
        DOMParser parser = new DOMParser();
        parser.setFeature( "http://cyberneko.org/html/features/augmentations", true );
        parser.setProperty( "http://cyberneko.org/html/properties/names/elems", "upper" );
        parser.setProperty( "http://cyberneko.org/html/properties/names/attrs", "upper" );
        parser.parse( new InputSource( stream ) );

        Set<String> links = new HashSet<String>();

        recursiveLinkCollector( parser.getDocument(), baseURI, links, filter );

        return links;
    }

    //    private String dumpAttributes(Element elem) {
    //        StringBuffer buf = new StringBuffer();
    //        NamedNodeMap nodemap = elem.getAttributes();
    //        int len = nodemap.getLength();
    //        for (int i = 0; i < len; i++) {
    //            Node att = nodemap.item(i);
    //            buf.append(" ");
    //            buf.append(att.getNodeName()).append("=\"");
    //            buf.append(att.getNodeValue()).append("\"");
    //        }
    //        return buf.toString();
    //    }

    private boolean isAcceptableLink( String link, TransferFileFilter filter )
    {
        if ( StringUtils.isBlank( link ) )
        {
            return false;
        }

        for ( Pattern skipPat : skips )
        {
            if ( skipPat.matcher( link ).find() )
            {
                return false;
            }
        }

        if ( filter != null )
        {
            return filter.accept( link );
        }
        return true;
    }

    private void recursiveLinkCollector( Node node, URI baseURI, Set<String> links, TransferFileFilter filter )
    {
        if ( node.getNodeType() == Node.ELEMENT_NODE )
        {
            //            System.out.println("Element <" + node.getNodeName() + dumpAttributes((Element) node) + ">");
            if ( "A".equals( node.getNodeName() ) )
            {
                Element anchor = (Element) node;
                NamedNodeMap nodemap = anchor.getAttributes();
                Node href = nodemap.getNamedItem( "HREF" );
                if ( href != null )
                {
                    String link = cleanLink( baseURI, href.getNodeValue() );
                    //                    System.out.println("HREF (" + href.getNodeValue() + " => " + link + ")");
                    if ( isAcceptableLink( link, filter ) )
                    {
                        links.add( link );
                    }
                }
            }
        }

        Node child = node.getFirstChild();
        while ( child != null )
        {
            recursiveLinkCollector( child, baseURI, links, filter );
            child = child.getNextSibling();
        }
    }
}
