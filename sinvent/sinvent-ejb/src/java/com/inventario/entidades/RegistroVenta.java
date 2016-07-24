/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.entidades;

import java.io.Serializable;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author limon
 */
@Entity
@Table(name = "registro_venta")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RegistroVenta.findAll", query = "SELECT r FROM RegistroVenta r"),
    @NamedQuery(name = "RegistroVenta.findById", query = "SELECT r FROM RegistroVenta r WHERE r.id = :id"),
    @NamedQuery(name = "RegistroVenta.findByFecha", query = "SELECT r FROM RegistroVenta r WHERE r.fecha = :fecha")})
public class RegistroVenta implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "fecha")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;
    @JoinColumn(name = "cliente", referencedColumnName = "id")
    @ManyToOne
    private Entidades cliente;

    public RegistroVenta() {
    }

    public RegistroVenta(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Entidades getCliente() {
        return cliente;
    }

    public void setCliente(Entidades cliente) {
        this.cliente = cliente;
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
        if (!(object instanceof RegistroVenta)) {
            return false;
        }
        RegistroVenta other = (RegistroVenta) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.inventario.entidades.RegistroVenta[ id=" + id + " ]";
    }
    
}
