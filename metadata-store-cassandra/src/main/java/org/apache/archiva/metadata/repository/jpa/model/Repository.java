package org.apache.archiva.metadata.repository.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Olivier Lamy
 */
@Entity
@Table( name = "repositories", schema = "ArchivaKeySpace@archiva" )
public class Repository
    implements Serializable
{

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @OneToMany( fetch = FetchType.EAGER )
    private List<Namespace> namespaces = new ArrayList<Namespace>();

    public Repository()
    {
        // no op
    }

    public Repository( String id )
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

    public List<Namespace> getNamespaces()
    {
        return namespaces;
    }

    public void setNamespaces( List<Namespace> namespaces )
    {
        this.namespaces = namespaces;
    }


}
