/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.administracion;

import com.inventario.entidades.Archivos;
import com.inventario.excepciones.ConsultarException;
import com.inventario.servicios.ArchivosFacade;
import com.inventario.utilitarios.Formulario;
import com.inventario.utilitarios.MensajesErrores;
import com.inventario.utilitarios.Recurso;
import java.io.Serializable;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

/**
 *
 * @author luisfernando
 */
@ManagedBean
@ViewScoped
public class ArchivosBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Archivos archivo;
    private Recurso recurso;
    private String nombre;
    private String tipo;
    private Formulario formularioDescargas = new Formulario();

    @EJB
    private ArchivosFacade ejbArchivos;

    public ArchivosBean() {
    }

    public String seleccionaImagen(Integer id) {
        try {
            archivo = ejbArchivos.find(id);
            FacesContext fc = FacesContext.getCurrentInstance();
            ExternalContext ec = fc.getExternalContext();
            recurso = new Recurso(ec, archivo.getNombre(), archivo.getArchivo());
            tipo = archivo.getTipo();
            Calendar c = Calendar.getInstance();
            nombre = c.getTimeInMillis() + archivo.getNombre();
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(Archivos.class.getName()).log(Level.SEVERE, null, ex);
        }
        formularioDescargas.insertar();
        return null;
    }

    public Archivos traerArchivo(Integer id) throws ConsultarException {
        return ejbArchivos.find(id);
    }

    /**
     * @return the archivo
     */
    public Archivos getArchivo() {
        return archivo;
    }

    /**
     * @param archivo the archivo to set
     */
    public void setArchivo(Archivos archivo) {
        this.archivo = archivo;
    }

    /**
     * @return the recurso
     */
    public Recurso getRecurso() {
        return recurso;
    }

    /**
     * @param recurso the recurso to set
     */
    public void setRecurso(Recurso recurso) {
        this.recurso = recurso;
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return the tipo
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * @param tipo the tipo to set
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * @return the formularioDescargas
     */
    public Formulario getFormularioDescargas() {
        return formularioDescargas;
    }

    /**
     * @param formularioDescargas the formularioDescargas to set
     */
    public void setFormularioDescargas(Formulario formularioDescargas) {
        this.formularioDescargas = formularioDescargas;
    }

}
