package org.apache.archiva.metadata.repository.jcr;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.archiva.metadata.model.MetadataFacetFactory;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.config.RepositoryConfig;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Hello world!
 */
public class ExportRepoApp
{
    public static void main( String[] args )
        throws RepositoryException, IOException
    {
        RepositoryConfig config = RepositoryConfig.create( "src/main/repository.xml", "jcr" );
        Repository repository = new TransientRepository( config );

        JcrMetadataRepository jcrMetadataRepository = new JcrMetadataRepository(
            Collections.<String, MetadataFacetFactory>emptyMap(), repository );

        try
        {
            Session session = jcrMetadataRepository.getJcrSession();

            session.exportSystemView( "/repositories/internal/content/org/apache/archiva", new FileOutputStream(
                "artifacts.xml" ), false, false );
        }
        finally
        {
            jcrMetadataRepository.close();
        }
    }
}
