package org.apache.archiva.commons.transfer;

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
 * MimeTypesTest
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class MimeTypesTest
    extends TestCase
{
    public void testMimeTypeArtifactPom()
    {
        assertEquals( "application/xml", new MimeTypes().getMimeType( "app-1.0.pom" ) );
    }

    public void testMimeTypeAsc()
    {
        assertEquals( "text/plain", new MimeTypes().getMimeType( "app-1.0.jar.asc" ) );
    }

    public void testMimeTypeJar()
    {
        assertEquals( "application/java-archive", new MimeTypes().getMimeType( "app-1.0.jar" ) );
    }

    public void testMimeTypeMd5()
    {
        assertEquals( "text/plain", new MimeTypes().getMimeType( "app-1.0.jar.md5" ) );
    }

    public void testMimeTypePomXml()
    {
        assertEquals( "application/xml", new MimeTypes().getMimeType( "pom.xml" ) );
    }

    public void testMimeTypeSha1()
    {
        assertEquals( "text/plain", new MimeTypes().getMimeType( "app-1.0.jar.sha1" ) );
    }

    public void testMimeTypeTar()
    {
        assertEquals( "application/x-tar", new MimeTypes().getMimeType( "app-1.0.tar" ) );
    }

    public void testMimeTypeTarBz2()
    {
        assertEquals( "application/octet-stream", new MimeTypes().getMimeType( "app-1.0.tar.bz2" ) );
    }

    public void testMimeTypeTarGz()
    {
        assertEquals( "application/octet-stream", new MimeTypes().getMimeType( "app-1.0.tar.gz" ) );
    }

    public void testMimeTypeText()
    {
        assertEquals( "text/plain", new MimeTypes().getMimeType( "notes.txt" ) );
    }

    public void testMimeTypeZip()
    {
        assertEquals( "application/zip", new MimeTypes().getMimeType( "app-1.0.zip" ) );
    }
}
