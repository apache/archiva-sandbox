package org.apache.archiva.metadata.repository.obr;

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

import org.apache.archiva.metadata.model.MetadataFacet;
import org.apache.archiva.metadata.model.MetadataFacetFactory;
import org.springframework.stereotype.Service;

/**
 * plexus.component role="org.apache.archiva.metadata.model.MetadataFacetFactory" role-hint="org.apache.archiva.metadata.repository.obr"
 */
@Service( "metadataFacetFactory#org.apache.archiva.metadata.repository.obr" )
public class OBRFactory
    implements MetadataFacetFactory
{
    public MetadataFacet createMetadataFacet() {
        // TODO  return new ArchivaOBR();
        return null;
    }

    public MetadataFacet createMetadataFacet( String repositoryId, String name ) {
        // TODO return new ArchivaOBR( repositoryId, name );
        return null;
    }
}
