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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author luis
 */
@Entity
@Table(name = "grupousuario")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Grupousuario.findAll", query = "SELECT g FROM Grupousuario g"),
    @NamedQuery(name = "Grupousuario.findById", query = "SELECT g FROM Grupousuario g WHERE g.id = :id"),
    @NamedQuery(name = "Grupousuario.findByUsuario", query = "SELECT g FROM Grupousuario g WHERE g.usuario = :usuario")})
public class Grupousuario implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Size(max = 2147483647)
    @Column(name = "usuario")
    private String usuario;
    @JoinColumn(name = "modulo", referencedColumnName = "id")
    @ManyToOne
    private Codigos modulo;
    @JoinColumn(name = "grupo", referencedColumnName = "id")
    @ManyToOne
    private Codigos grupo;
    @JoinColumn(name = "centro", referencedColumnName = "id")
    @ManyToOne
    private Centros centro;

    public Grupousuario() {
    }

    public Grupousuario(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Codigos getModulo() {
        return modulo;
    }

    public void setModulo(Codigos modulo) {
        this.modulo = modulo;
    }

    public Codigos getGrupo() {
        return grupo;
    }

    public void setGrupo(Codigos grupo) {
        this.grupo = grupo;
    }

    public Centros getCentro() {
        return centro;
    }

    public void setCentro(Centros centro) {
        this.centro = centro;
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
        if (!(object instanceof Grupousuario)) {
            return false;
        }
        Grupousuario other = (Grupousuario) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.inventario.entidades.Grupousuario[ id=" + id + " ]";
    }
    
}
