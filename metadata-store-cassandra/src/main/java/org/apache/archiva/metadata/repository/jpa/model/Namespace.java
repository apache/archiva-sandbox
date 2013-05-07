package org.apache.archiva.metadata.repository.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


/**
 * @author Olivier Lamy
 */
@Entity
@Table( name = "namespaces", schema = "metadata@archiva" )
public class Namespace
    implements Serializable
{
    @Id
    private String id;

    public Namespace()
    {
        // no op
    }

    public Namespace( String id )
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }
}
