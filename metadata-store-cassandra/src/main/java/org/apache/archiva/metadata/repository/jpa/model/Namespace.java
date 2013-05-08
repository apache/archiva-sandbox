package org.apache.archiva.metadata.repository.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


/**
 * @author Olivier Lamy
 */
@Entity
@Table( name = "namespaces", schema = "ArchivaKeySpace@archiva")
public class Namespace
    implements Serializable
{

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Column(name = "name")
    private String name;

    public Namespace()
    {
        // no op
    }

    public Namespace( String id )
    {
        this.id = id;
        this.name = id;
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }
}
