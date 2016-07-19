/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.administracion;

import com.inventario.entidades.Archivos;
import com.inventario.entidades.Centros;
import com.inventario.entidades.Direcciones;
import com.inventario.entidades.Perfil;
import com.inventario.excepciones.BorrarException;
import com.inventario.excepciones.ConsultarException;
import com.inventario.excepciones.GrabarException;
import com.inventario.excepciones.InsertarException;
import com.inventario.seguridad.SeguridadBean;
import com.inventario.servicios.ArchivosFacade;
import com.inventario.servicios.CentrosFacade;
import com.inventario.servicios.DireccionesFacade;
import com.inventario.utilitarios.Combos;
import com.inventario.utilitarios.Formulario;
import com.inventario.utilitarios.MensajesErrores;
import java.nio.file.Files;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.icefaces.ace.component.fileentry.FileEntry;
import org.icefaces.ace.component.fileentry.FileEntryEvent;
import org.icefaces.ace.component.fileentry.FileEntryResults;
import org.icefaces.ace.model.table.LazyDataModel;
import org.icefaces.ace.model.table.SortCriteria;

/**
 *
 * @author luisfernando
 */
@ManagedBean(name = "centros")
@ViewScoped
public class CentrosBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of CentrosBean
     */
    @ManagedProperty(value = "#{seguridadBean}")
    private SeguridadBean seguridadBean;

    private Formulario formulario = new Formulario();

    private LazyDataModel<Centros> listaCentros;
    private Centros centro;
    private Direcciones direccion;
    private String nombre;
    private Archivos logo;
    private Perfil perfil;

    @EJB
    private CentrosFacade ejbCentros;
    @EJB
    private DireccionesFacade ejbDirecciones;
    @EJB
    private ArchivosFacade ejbArchivos;

    public CentrosBean() {
        listaCentros = new LazyDataModel<Centros>() {
            @Override
            public List<Centros> load(int i, int i1, SortCriteria[] scs, Map<String, String> map) {
                return carga(i, i1, scs, map);
            }
        };
    }

    @PostConstruct
    private void activar() {
        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String redirect = (String) params.get("faces-redirect");
        if (redirect == null) {
            return;
        }

        String p = (String) params.get("p");
        String nombreForma = "CentrosVista";

        if (p == null) {
            MensajesErrores.fatal("Sin perfil válido");
            seguridadBean.cerraSession();
        }
        perfil = seguridadBean.traerPerfil((String) params.get("p"));

        if (this.perfil == null) {
            MensajesErrores.fatal("Sin perfil válido");
            seguridadBean.cerraSession();
        }

        if (nombreForma.contains(perfil.getMenu().getFormulario())) {
            if (!this.perfil.getGrupo().equals(seguridadBean.getGrupo().getGrupo())) {
                MensajesErrores.fatal("Sin perfil válido, grupo invalido");
                seguridadBean.cerraSession();
            }
        }
    }

    public List<Centros> carga(int i, int pageSize, SortCriteria[] scs, Map<String, String> map) {
        Map parametros = new HashMap();
        String orden = "";
        if (scs.length == 0) {
            orden = "o.nombre asc";
        } else {
            orden += " o." + scs[0].getPropertyName() + (scs[0].isAscending() ? " ASC" : " DESC");
        }
        parametros.put(";orden", orden);
        String where = "o.id is not null";

        for (Map.Entry e : map.entrySet()) {
            String clave = (String) e.getKey();
            String valor = (String) e.getValue();

            where += " and upper(o." + clave + ") like :valor";
            parametros.put("valor", valor.toUpperCase() + "%");
        }

        if (!((nombre == null) || (nombre.isEmpty()))) {
            where += " and upper(o.nombre) like :nombre";
            parametros.put("nombre", nombre.toUpperCase() + "%");
        }

        parametros.put(";where", where);

        int total = 0;

        try {
            total = ejbCentros.contar(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(CentrosBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        int endIndex = i + pageSize;

        if (endIndex > total) {
            endIndex = total;
        }

        parametros.put(";inicial", i);
        parametros.put(";final", endIndex);
        listaCentros.setRowCount(total);

        try {
            return ejbCentros.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(CentrosBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public String buscar() {
        listaCentros = new LazyDataModel<Centros>() {
            @Override
            public List<Centros> load(int i, int i1, SortCriteria[] scs, Map<String, String> map) {
                return carga(i, i1, scs, map);
            }
        };
        return null;
    }

    public String nuevo() {
        centro = new Centros();
        logo = new Archivos();
        direccion = new Direcciones();
        formulario.insertar();
        return null;
    }

    public String modificar() {
        this.centro = (Centros) listaCentros.getRowData();
        this.direccion = this.centro.getDireccion();

        if (centro.getLogotipo() != null) {
            this.logo = traerArchivo(centro.getLogotipo());
        } else {
            logo = new Archivos();
        }

        formulario.editar();
        return null;
    }

    private Archivos traerArchivo(Integer id) {
        try {
            return ejbArchivos.find(id);
        } catch (ConsultarException ex) {
            Logger.getLogger(CentrosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String eliminar() {
        this.centro = (Centros) listaCentros.getRowData();
        this.direccion = this.centro.getDireccion();

        if (centro.getLogotipo() != null) {
            this.logo = traerArchivo(centro.getLogotipo());
        } else {
            logo = new Archivos();
        }

        formulario.eliminar();
        return null;
    }

    private boolean validar() {
        if (centro.getNombre() == null || centro.getNombre().isEmpty()) {
            MensajesErrores.advertencia("Nombre de la institución es necesario");
            return true;
        }
        if (direccion.getPrimaria() == null || direccion.getPrimaria().isEmpty()) {
            MensajesErrores.advertencia("Calle primaria es necesaria");
            return true;
        }
        if (direccion.getNumero() == null || direccion.getNumero().isEmpty()) {
            MensajesErrores.advertencia("Número es necesario");
            return true;
        }
        if (direccion.getSecundaria() == null || direccion.getSecundaria().isEmpty()) {
            MensajesErrores.advertencia("Calle secundaria es necesaria");
            return true;
        }
        if (direccion.getTelefono() == null || direccion.getTelefono().isEmpty()) {
            MensajesErrores.advertencia("Número telefónico es necesario");
            return true;
        }
        if (direccion.getCiudad() == null) {
            MensajesErrores.advertencia("Ciudad es necesaria");
            return true;
        }
        if (logo.getNombre() == null) {
            MensajesErrores.advertencia("Logotipo es necesario");
            return true;
        }
        return false;
    }

    public String insertar() {

        if (!perfil.getNuevo()) {
            MensajesErrores.advertencia("No tiene autorización para crear un registro");
            return null;
        }

        if (validar()) {
            return null;
        }

        try {
            ejbDirecciones.create(direccion, seguridadBean.getEntidad().getUserid());
            ejbArchivos.create(logo, seguridadBean.getEntidad().getUserid());
            centro.setDireccion(direccion);
            centro.setLogotipo(logo.getId());
            ejbCentros.create(centro, seguridadBean.getEntidad().getUserid());
        } catch (InsertarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(CentrosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }

    public String grabar() {

        if (!perfil.getModificacion()) {
            MensajesErrores.advertencia("No tiene autorización para modificar un registro");
            return null;
        }

        if (validar()) {
            return null;
        }

        try {
            ejbDirecciones.edit(direccion, seguridadBean.getEntidad().getUserid());
            if (logo.getId() == null) {
                ejbArchivos.create(logo, seguridadBean.getEntidad().getUserid());
                centro.setLogotipo(logo.getId());
            } else {
                ejbArchivos.edit(logo, seguridadBean.getEntidad().getUserid());
                centro.setLogotipo(logo.getId());
            }
            ejbCentros.edit(centro, seguridadBean.getEntidad().getUserid());
        } catch (GrabarException | InsertarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(CentrosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }

    public String borrar() {
        if (!perfil.getBorrado()) {
            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
            return null;
        }
        try {
            ejbCentros.remove(centro, seguridadBean.getEntidad().getUserid());
        } catch (BorrarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(CentrosBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        formulario.cancelar();
        buscar();
        return null;
    }

    public String cancelar() {
        formulario.cancelar();
        buscar();
        return null;
    }

    public String logoListener(FileEntryEvent e) {
        FileEntry fe = (FileEntry) e.getComponent();
        FileEntryResults results = fe.getResults();

        for (FileEntryResults.FileInfo i : results.getFiles()) {
            try {
                File file = i.getFile();
                logo.setArchivo(Files.readAllBytes(file.toPath()));
                logo.setNombre(i.getFileName());
                logo.setTipo(i.getContentType());
            } catch (IOException ex) {
                MensajesErrores.fatal(ex.getMessage() + ":" + ex.getCause());
                Logger.getLogger(CentrosBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public Centros traer(Integer id) throws ConsultarException {
        return ejbCentros.find(id);
    }

    public SelectItem[] getComboCentros() {
        List<Centros> li = new LinkedList<>();
        Map parametros = new HashMap();
        parametros.put(";orden", "o.nombre");
        try {
            li = ejbCentros.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(CentrosBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Combos.SelectItems(li, true);
    }

    public SelectItem[] getComboCentrosF() {
        List<Centros> li = new LinkedList<>();
        Map parametros = new HashMap();
        parametros.put(";orden", "o.nombre");
        try {
            li = ejbCentros.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(CentrosBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Combos.SelectItems(li, false);
    }

    /**
     * @return the formulario
     */
    public Formulario getFormulario() {
        return formulario;
    }

    /**
     * @param formulario the formulario to set
     */
    public void setFormulario(Formulario formulario) {
        this.formulario = formulario;
    }

    /**
     * @param institucion the centro to set
     */
    public void setInstitucion(Centros institucion) {
        this.centro = institucion;
    }

    /**
     * @return the direccion
     */
    public Direcciones getDireccion() {
        return direccion;
    }

    /**
     * @param direccion the direccion to set
     */
    public void setDireccion(Direcciones direccion) {
        this.direccion = direccion;
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
     * @return the perfil
     */
    public Perfil getPerfil() {
        return perfil;
    }

    /**
     * @param perfil the perfil to set
     */
    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    /**
     * @return the listaCentros
     */
    public LazyDataModel<Centros> getListaCentros() {
        return listaCentros;
    }

    /**
     * @param listaCentros the listaCentros to set
     */
    public void setListaCentros(LazyDataModel<Centros> listaCentros) {
        this.listaCentros = listaCentros;
    }

    /**
     * @return the centro
     */
    public Centros getCentro() {
        return centro;
    }

    /**
     * @param centro the centro to set
     */
    public void setCentro(Centros centro) {
        this.centro = centro;
    }

    /**
     * @return the logo
     */
    public Archivos getLogo() {
        return logo;
    }

    /**
     * @param logo the logo to set
     */
    public void setLogo(Archivos logo) {
        this.logo = logo;
    }

    /**
     * @return the seguridadBean
     */
    public SeguridadBean getSeguridadBean() {
        return seguridadBean;
    }

    /**
     * @param seguridadBean the seguridadBean to set
     */
    public void setSeguridadBean(SeguridadBean seguridadBean) {
        this.seguridadBean = seguridadBean;
    }

}
