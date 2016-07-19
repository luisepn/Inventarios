/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.entidades;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author limon
 */
@Entity
@Table(name = "centros")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Centros.findAll", query = "SELECT c FROM Centros c"),
    @NamedQuery(name = "Centros.findById", query = "SELECT c FROM Centros c WHERE c.id = :id"),
    @NamedQuery(name = "Centros.findByNombre", query = "SELECT c FROM Centros c WHERE c.nombre = :nombre"),
    @NamedQuery(name = "Centros.findByWeb", query = "SELECT c FROM Centros c WHERE c.web = :web"),
    @NamedQuery(name = "Centros.findByEmail", query = "SELECT c FROM Centros c WHERE c.email = :email"),
    @NamedQuery(name = "Centros.findByLogotipo", query = "SELECT c FROM Centros c WHERE c.logotipo = :logotipo"),
    @NamedQuery(name = "Centros.findByRuc", query = "SELECT c FROM Centros c WHERE c.ruc = :ruc")})
public class Centros implements Serializable {
    @OneToMany(mappedBy = "centro")
    private List<Grupousuario> grupousuarioList;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Size(max = 2147483647)
    @Column(name = "nombre")
    private String nombre;
    @Size(max = 2147483647)
    @Column(name = "web")
    private String web;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Correo electrónico no válido")//if the field contains email address consider using this annotation to enforce field validation
    @Size(max = 2147483647)
    @Column(name = "email")
    private String email;
    @Column(name = "logotipo")
    private Integer logotipo;
    @Size(max = 2147483647)
    @Column(name = "ruc")
    private String ruc;
    @JoinColumn(name = "direccion", referencedColumnName = "id")
    @OneToOne
    private Direcciones direccion;

    public Centros() {
    }

    public Centros(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getWeb() {
        return web;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getLogotipo() {
        return logotipo;
    }

    public void setLogotipo(Integer logotipo) {
        this.logotipo = logotipo;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public Direcciones getDireccion() {
        return direccion;
    }

    public void setDireccion(Direcciones direccion) {
        this.direccion = direccion;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Centros)) {
            return false;
        }
        Centros other = (Centros) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return nombre;
    }

    @XmlTransient
    public List<Grupousuario> getGrupousuarioList() {
        return grupousuarioList;
    }

    public void setGrupousuarioList(List<Grupousuario> grupousuarioList) {
        this.grupousuarioList = grupousuarioList;
    }
    
}
