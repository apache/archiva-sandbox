package org.apache.archiva.metadata.repository.jpa.model;

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
@Table( name = "repositories", schema = "metadata@archiva" )
public class Repository
    implements Serializable
{
    @Id
    private String id;

    @OneToMany( fetch = FetchType.EAGER )
    private List<Namespace> namespaces = new ArrayList<Namespace>();

    public Repository()
    {
        // no op
    }

    public Repository( String id )
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

    public List<Namespace> getNamespaces()
    {
        return namespaces;
    }

    public void setNamespaces( List<Namespace> namespaces )
    {
        this.namespaces = namespaces;
    }
}
