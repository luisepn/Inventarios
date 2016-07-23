/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.sistema;

import com.inventario.entidades.Proveedores;
import com.inventario.entidades.Direcciones;
import com.inventario.entidades.Perfil;
import com.inventario.excepciones.BorrarException;
import com.inventario.excepciones.ConsultarException;
import com.inventario.excepciones.GrabarException;
import com.inventario.excepciones.InsertarException;
import com.inventario.seguridad.SeguridadBean;
import com.inventario.servicios.ProveedoresFacade;
import com.inventario.servicios.DireccionesFacade;
import com.inventario.utilitarios.Combos;
import com.inventario.utilitarios.Formulario;
import com.inventario.utilitarios.MensajesErrores;
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
import org.icefaces.ace.model.table.LazyDataModel;
import org.icefaces.ace.model.table.SortCriteria;

/**
 *
 * @author luisfernando
 */
@ManagedBean(name = "proveedores")
@ViewScoped
public class ProveedoresBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of ProveedoresBean
     */
    @ManagedProperty(value = "#{seguridadBean}")
    private SeguridadBean seguridadBean;

    private Formulario formulario = new Formulario();

    private LazyDataModel<Proveedores> listaProveedores;
    private Proveedores proveedor;
    private Direcciones direccion;
    private String nombre;
    private Perfil perfil;

    @EJB
    private ProveedoresFacade ejbProveedores;
    @EJB
    private DireccionesFacade ejbDirecciones;

    public ProveedoresBean() {
        listaProveedores = new LazyDataModel<Proveedores>() {
            @Override
            public List<Proveedores> load(int i, int i1, SortCriteria[] scs, Map<String, String> map) {
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
        String nombreForma = "ProveedoresVista";

        if (p == null) {
            MensajesErrores.fatal("Sin perfil válido");
            getSeguridadBean().cerraSession();
        }
        setPerfil(getSeguridadBean().traerPerfil((String) params.get("p")));

        if (this.getPerfil() == null) {
            MensajesErrores.fatal("Sin perfil válido");
            getSeguridadBean().cerraSession();
        }

        if (nombreForma.contains(getPerfil().getMenu().getFormulario())) {
            if (!this.perfil.getGrupo().equals(seguridadBean.getGrupo().getGrupo())) {
                MensajesErrores.fatal("Sin perfil válido, grupo invalido");
                getSeguridadBean().cerraSession();
            }
        }
    }

    public List<Proveedores> carga(int i, int pageSize, SortCriteria[] scs, Map<String, String> map) {
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
            parametros.put("nombre", getNombre().toUpperCase() + "%");
        }

        parametros.put(";where", where);

        int total = 0;

        try {
            total = ejbProveedores.contar(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(ProveedoresBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        int endIndex = i + pageSize;

        if (endIndex > total) {
            endIndex = total;
        }

        parametros.put(";inicial", i);
        parametros.put(";final", endIndex);
        getListaProveedores().setRowCount(total);

        try {
            return ejbProveedores.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(ProveedoresBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public String buscar() {
        setListaProveedores(new LazyDataModel<Proveedores>() {
            @Override
            public List<Proveedores> load(int i, int i1, SortCriteria[] scs, Map<String, String> map) {
                return carga(i, i1, scs, map);
            }
        });
        return null;
    }

    public String nuevo() {
        setProveedor(new Proveedores());
        setDireccion(new Direcciones());
        getFormulario().insertar();
        return null;
    }

    public String modificar() {
        this.setProveedor((Proveedores) getListaProveedores().getRowData());
        this.setDireccion(this.getProveedor().getDireccion());
        getFormulario().editar();
        return null;
    }

    public String eliminar() {
        this.setProveedor((Proveedores) getListaProveedores().getRowData());
        this.setDireccion(this.getProveedor().getDireccion());
        getFormulario().eliminar();
        return null;
    }

    private boolean validar() {
        if (getProveedor().getNombre() == null || getProveedor().getNombre().isEmpty()) {
            MensajesErrores.advertencia("Nombre de la institución es necesario");
            return true;
        }
        if (getDireccion().getPrimaria() == null || getDireccion().getPrimaria().isEmpty()) {
            MensajesErrores.advertencia("Calle primaria es necesaria");
            return true;
        }
        if (getDireccion().getNumero() == null || getDireccion().getNumero().isEmpty()) {
            MensajesErrores.advertencia("Número es necesario");
            return true;
        }
        if (getDireccion().getSecundaria() == null || getDireccion().getSecundaria().isEmpty()) {
            MensajesErrores.advertencia("Calle secundaria es necesaria");
            return true;
        }
        if (getDireccion().getTelefono() == null || getDireccion().getTelefono().isEmpty()) {
            MensajesErrores.advertencia("Número telefónico es necesario");
            return true;
        }
        if (getDireccion().getCiudad() == null) {
            MensajesErrores.advertencia("Ciudad es necesaria");
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
            ejbDirecciones.create(getDireccion(), getSeguridadBean().getEntidad().getUserid());
            getProveedor().setDireccion(getDireccion());
            ejbProveedores.create(getProveedor(), getSeguridadBean().getEntidad().getUserid());
        } catch (InsertarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(ProveedoresBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        getFormulario().cancelar();
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
            ejbDirecciones.edit(getDireccion(), getSeguridadBean().getEntidad().getUserid());
            ejbProveedores.edit(getProveedor(), getSeguridadBean().getEntidad().getUserid());
        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(ProveedoresBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        getFormulario().cancelar();
        buscar();
        return null;
    }

    public String borrar() {
        if (!perfil.getBorrado()) {
            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
            return null;
        }
        try {
            ejbProveedores.remove(getProveedor(), getSeguridadBean().getEntidad().getUserid());
        } catch (BorrarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(ProveedoresBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        getFormulario().cancelar();
        buscar();
        return null;
    }

    public String cancelar() {
        getFormulario().cancelar();
        buscar();
        return null;
    }

    public Proveedores traer(Integer id) throws ConsultarException {
        return ejbProveedores.find(id);
    }

    public SelectItem[] getComboProveedores() {
        List<Proveedores> li = new LinkedList<>();
        Map parametros = new HashMap();
        parametros.put(";orden", "o.nombre");
        try {
            li = ejbProveedores.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(ProveedoresBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Combos.SelectItems(li, true);
    }

    public SelectItem[] getComboProveedoresF() {
        List<Proveedores> li = new LinkedList<>();
        Map parametros = new HashMap();
        parametros.put(";orden", "o.nombre");
        try {
            li = ejbProveedores.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(ProveedoresBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Combos.SelectItems(li, false);
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
     * @return the listaProveedores
     */
    public LazyDataModel<Proveedores> getListaProveedores() {
        return listaProveedores;
    }

    /**
     * @param listaProveedores the listaProveedores to set
     */
    public void setListaProveedores(LazyDataModel<Proveedores> listaProveedores) {
        this.listaProveedores = listaProveedores;
    }

    /**
     * @return the proveedor
     */
    public Proveedores getProveedor() {
        return proveedor;
    }

    /**
     * @param proveedor the proveedor to set
     */
    public void setProveedor(Proveedores proveedor) {
        this.proveedor = proveedor;
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

}
