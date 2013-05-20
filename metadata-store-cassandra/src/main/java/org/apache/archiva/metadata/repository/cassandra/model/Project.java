package org.apache.archiva.metadata.repository.cassandra.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author Olivier Lamy
 */
@Entity
public class Project
    implements Serializable
{
    @Id
    @Column( name = "projectId" )
    private String projectId;

    @Column( name = "id" )
    private String id;

    @Column( name = "repository" )
    private Namespace namespace;

    public Project()
    {
        // no op
    }

    public Project( String id, Namespace namespace )
    {
        this.id = id;
        this.namespace = namespace;
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public Namespace getNamespace()
    {
        return namespace;
    }

    public void setNamespace( Namespace namespace )
    {
        this.namespace = namespace;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        Project project = (Project) o;

        if ( !id.equals( project.id ) )
        {
            return false;
        }
        if ( !namespace.equals( project.namespace ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id.hashCode();
        result = 31 * result + namespace.hashCode();
        return result;
    }

    public static class KeyBuilder
    {

        private Namespace namespace;

        private String projectId;

        public KeyBuilder()
        {
            // no op
        }

        public KeyBuilder withNamespace( Namespace namespace )
        {
            this.namespace = namespace;
            return this;
        }

        public KeyBuilder withProjectId( String projectId )
        {
            this.projectId = projectId;
            return this;
        }


        public String build()
        {
            // FIXME add some controls
            return new Namespace.KeyBuilder().withNamespace( this.namespace ) + "-" + this.projectId;
        }
    }
}
