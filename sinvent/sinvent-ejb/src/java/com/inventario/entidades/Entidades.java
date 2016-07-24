/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.entidades;

import java.io.Serializable;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author limon
 */
@Entity
@Table(name = "entidades")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Entidades.findAll", query = "SELECT e FROM Entidades e"),
    @NamedQuery(name = "Entidades.findById", query = "SELECT e FROM Entidades e WHERE e.id = :id"),
    @NamedQuery(name = "Entidades.findByNombres", query = "SELECT e FROM Entidades e WHERE e.nombres = :nombres"),
    @NamedQuery(name = "Entidades.findByApellidos", query = "SELECT e FROM Entidades e WHERE e.apellidos = :apellidos"),
    @NamedQuery(name = "Entidades.findByEmail", query = "SELECT e FROM Entidades e WHERE e.email = :email"),
    @NamedQuery(name = "Entidades.findByUserid", query = "SELECT e FROM Entidades e WHERE e.userid = :userid"),
    @NamedQuery(name = "Entidades.findByPwd", query = "SELECT e FROM Entidades e WHERE e.pwd = :pwd"),
    @NamedQuery(name = "Entidades.findByPin", query = "SELECT e FROM Entidades e WHERE e.pin = :pin"),
    @NamedQuery(name = "Entidades.findByFecha", query = "SELECT e FROM Entidades e WHERE e.fecha = :fecha"),
    @NamedQuery(name = "Entidades.findByActivo", query = "SELECT e FROM Entidades e WHERE e.activo = :activo"),
    @NamedQuery(name = "Entidades.findByRol", query = "SELECT e FROM Entidades e WHERE e.rol = :rol")})
public class Entidades implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Size(max = 2147483647)
    @Column(name = "nombres")
    private String nombres;
    @Size(max = 2147483647)
    @Column(name = "apellidos")
    private String apellidos;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Correo electrónico no válido")//if the field contains email address consider using this annotation to enforce field validation
    @Size(max = 2147483647)
    @Column(name = "email")
    private String email;
    @Size(max = 2147483647)
    @Column(name = "userid")
    private String userid;
    @Size(max = 2147483647)
    @Column(name = "pwd")
    private String pwd;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2147483647)
    @Column(name = "pin")
    private String pin;
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "rol")
    private String rol;
    @OneToMany(mappedBy = "cliente")
    private List<RegistroVenta> registroVentaList;
    @JoinColumn(name = "direccion", referencedColumnName = "id")
    @OneToOne
    private Direcciones direccion;

    public Entidades() {
    }

    public Entidades(Integer id) {
        this.id = id;
    }

    public Entidades(Integer id, String pin) {
        this.id = id;
        this.pin = pin;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    @XmlTransient
    public List<RegistroVenta> getRegistroVentaList() {
        return registroVentaList;
    }

    public void setRegistroVentaList(List<RegistroVenta> registroVentaList) {
        this.registroVentaList = registroVentaList;
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
        if (!(object instanceof Entidades)) {
            return false;
        }
        Entidades other = (Entidades) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return apellidos + " " + nombres;
    }
    
}
