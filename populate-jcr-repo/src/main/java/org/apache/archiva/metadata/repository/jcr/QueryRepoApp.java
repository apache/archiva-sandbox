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
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.config.RepositoryConfig;

import java.util.Collections;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;

/**
 * Hello world!
 */
public class QueryRepoApp
{
    public static void main( String[] args )
        throws RepositoryException
    {
        RepositoryConfig config = RepositoryConfig.create( "src/main/repository.xml", "jcr" );
        Repository repository = new TransientRepository( config );

        JcrMetadataRepository jcrMetadataRepository = new JcrMetadataRepository(
            Collections.<String, MetadataFacetFactory>emptyMap(), repository );

        try
        {
            Session session = jcrMetadataRepository.getJcrSession();

            QueryManager queryManager = session.getWorkspace().getQueryManager();

            queryProjectsWithDependency( queryManager, "org.springframework", "spring-web", "2.5.6" );

            queryProjectsWithDependency( queryManager, "org.springframework", "spring-web" );

            queryProjectsWithDependency( queryManager, "org.codehaus.plexus", "plexus-utils" );

//            queryAllArtifactsInRepo( queryManager );
//
//            queryAllProjectsInRepo( queryManager );
//
//            queryAllGroupsInRepo( queryManager );
        }
        finally
        {
            jcrMetadataRepository.close();
        }
    }

    private static void queryProjectsWithDependency( QueryManager queryManager, String namespace, String project )
        throws RepositoryException
    {
        queryProjectsWithDependency( queryManager, namespace, project, null );
    }

    private static void queryAllGroupsInRepo( QueryManager queryManager )
        throws RepositoryException
    {
        System.out.println( "querying # groups in repo" );

        long time = System.currentTimeMillis();
//            query = queryManager.createQuery(
//                "SELECT * FROM [archiva:namespace] WHERE ISDESCENDANTNODE([/repositories/internal/content]) AND namespace IS NOT NULL",
//                Query.JCR_SQL2 );
        Query query = queryManager.createQuery(
            "SELECT * FROM archiva:namespace WHERE jcr:path LIKE '/repositories/internal/content/%' AND namespace IS NOT NULL ORDER BY jcr:score",
            Query.SQL );
        System.out.println(
            "groups = " + query.execute().getRows().getSize() + " in " + ( System.currentTimeMillis() - time ) +
                " ms" );
    }

    private static void queryProjectsWithDependency( QueryManager queryManager, String namespace, String project,
                                                     String version )
        throws RepositoryException
    {
        System.out.println( "querying dependencies on project " + namespace + ":" + project + ":" + version );

        long time = System.currentTimeMillis();

        // TODO: bind variables instead
        String q =
            "SELECT * FROM [archiva:dependency] WHERE ISDESCENDANTNODE([/repositories/internal/content]) AND [groupId]='" +
                namespace + "' AND [artifactId]='" + project + "'";
        if ( version != null )
        {
            q += " AND [version]='" + version + "'";
        }
        Query query = queryManager.createQuery( q, Query.JCR_SQL2 );

        QueryResult execute = query.execute();
        System.out.println( "query in " + ( System.currentTimeMillis() - time ) + " ms" );
        time = System.currentTimeMillis();
        for ( Row r : JcrUtils.getRows( execute ) )
        {
            Node n = r.getNode();
//            n = n.getParent(); // dependency version
            String dependencyVersion = n.getName();

//            n = n.getParent(); // dependency project
//            n = n.getParent(); // dependency namespace
            n = n.getParent(); // dependencies element
            n = n.getParent(); // project version

            String usedByProjectVersion = n.getName();

            n = n.getParent(); // project
            String usedByProject = n.getName();

            n = n.getParent(); // namespace
            String usedByNamespace = n.getProperty( "namespace" ).getString();

            System.out.println(
                usedByNamespace + ":" + usedByProject + ":" + usedByProjectVersion + " -> " + dependencyVersion );
        }

        System.out.println( "results in " + ( System.currentTimeMillis() - time ) + " ms" );
    }

    private static void queryAllProjectsInRepo( QueryManager queryManager )
        throws RepositoryException
    {
        System.out.println( "querying # projects in repo" );

        long time = System.currentTimeMillis();
        Query query = queryManager.createQuery(
            "SELECT * FROM archiva:project WHERE jcr:path LIKE '/repositories/internal/content/%' ORDER BY jcr:score",
            Query.SQL );
//            query = queryManager.createQuery(
//                "SELECT * FROM [archiva:project] WHERE ISDESCENDANTNODE([/repositories/internal/content])",
//                Query.JCR_SQL2 );
        System.out.println(
            "projects = " + query.execute().getRows().getSize() + " in " + ( System.currentTimeMillis() - time ) +
                " ms" );
    }

    private static void queryAllArtifactsInRepo( QueryManager queryManager )
        throws RepositoryException
    {
        System.out.println( "querying # artifacts in repo" );

        long time = System.currentTimeMillis();

        // fails on Jackrabbit 2.2.0
//            String q = "SELECT size FROM [archiva:artifact] AS artifact WHERE ISDESCENDANTNODE([/repositories/internal/content])";
//            Query query = session.getWorkspace().getQueryManager().createQuery( q, Query.JCR_SQL2 );

//            String q = "SELECT size FROM archiva:artifact WHERE jcr:path LIKE '/repositories/internal/content/%'";
//            Query query = session.getWorkspace().getQueryManager().createQuery( q, Query.SQL );
//
        String q = "SELECT size FROM [archiva:artifact]";
        Query query = queryManager.createQuery( q, Query.JCR_SQL2 );

        QueryResult queryResult = query.execute();
        System.out.println( "query in " + ( System.currentTimeMillis() - time ) + "ms" );

        time = System.currentTimeMillis();

        long totalSize = 0, totalJars = 0, totalArtifacts = 0;
        for ( Row row : JcrUtils.getRows( queryResult ) )
        {
            totalSize += row.getValue( "size" ).getLong();
            if ( row.getNode().getName().endsWith( ".jar" ) )
            {
                totalJars++;
            }
            totalArtifacts++;
        }
        System.out.println( totalArtifacts + " artifacts, " + totalSize + " bytes, " + totalJars + " jars (count in " +
                                ( System.currentTimeMillis() - time ) + "ms)" );
    }
}
