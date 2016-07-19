/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.entidades;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author luis
 */
@Entity
@Table(name = "perfil")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Perfil.findAll", query = "SELECT p FROM Perfil p"),
    @NamedQuery(name = "Perfil.findById", query = "SELECT p FROM Perfil p WHERE p.id = :id"),
    @NamedQuery(name = "Perfil.findByConsulta", query = "SELECT p FROM Perfil p WHERE p.consulta = :consulta"),
    @NamedQuery(name = "Perfil.findByModificacion", query = "SELECT p FROM Perfil p WHERE p.modificacion = :modificacion"),
    @NamedQuery(name = "Perfil.findByBorrado", query = "SELECT p FROM Perfil p WHERE p.borrado = :borrado"),
    @NamedQuery(name = "Perfil.findByNuevo", query = "SELECT p FROM Perfil p WHERE p.nuevo = :nuevo")})
public class Perfil implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "consulta")
    private Boolean consulta;
    @Column(name = "modificacion")
    private Boolean modificacion;
    @Column(name = "borrado")
    private Boolean borrado;
    @Column(name = "nuevo")
    private Boolean nuevo;
    @JoinColumn(name = "menu", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Menusistema menu;
    @JoinColumn(name = "grupo", referencedColumnName = "id")
    @ManyToOne
    private Codigos grupo;

    public Perfil() {
    }

    public Perfil(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getConsulta() {
        return consulta;
    }

    public void setConsulta(Boolean consulta) {
        this.consulta = consulta;
    }

    public Boolean getModificacion() {
        return modificacion;
    }

    public void setModificacion(Boolean modificacion) {
        this.modificacion = modificacion;
    }

    public Boolean getBorrado() {
        return borrado;
    }

    public void setBorrado(Boolean borrado) {
        this.borrado = borrado;
    }

    public Boolean getNuevo() {
        return nuevo;
    }

    public void setNuevo(Boolean nuevo) {
        this.nuevo = nuevo;
    }

    public Menusistema getMenu() {
        return menu;
    }

    public void setMenu(Menusistema menu) {
        this.menu = menu;
    }

    public Codigos getGrupo() {
        return grupo;
    }

    public void setGrupo(Codigos grupo) {
        this.grupo = grupo;
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
        if (!(object instanceof Perfil)) {
            return false;
        }
        Perfil other = (Perfil) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.inventario.entidades.Perfil[ id=" + id + " ]";
    }
    
}
