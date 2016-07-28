/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.sistema;

import com.inventario.entidades.Codigos;
import com.inventario.entidades.Inventario;
import com.inventario.entidades.Perfil;
import com.inventario.entidades.Productos;
import com.inventario.entidades.Productoxproveedor;
import com.inventario.entidades.Proveedores;
import com.inventario.excepciones.BorrarException;
import com.inventario.excepciones.ConsultarException;
import com.inventario.excepciones.GrabarException;
import com.inventario.excepciones.InsertarException;
import com.inventario.seguridad.SeguridadBean;
import com.inventario.servicios.InventarioFacade;
import com.inventario.servicios.ProductosFacade;
import com.inventario.servicios.ProductoxproveedorFacade;
import com.inventario.utilitarios.Formulario;
import com.inventario.utilitarios.MensajesErrores;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
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
import org.icefaces.ace.model.table.LazyDataModel;
import org.icefaces.ace.model.table.SortCriteria;

/**
 *
 * @author edison
 */
@ManagedBean(name = "inventario")
@ViewScoped
public class InventarioBean implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * Creates a new instance of InventarioBean
     */
    @ManagedProperty(value = "#{seguridadBean}")
    private SeguridadBean seguridadBean;

    private Formulario formulario = new Formulario();
    private LazyDataModel<Inventario> listaInventarios;
    private Inventario inventario;
    private Codigos categoria;
    private Productos producto;
    private Productoxproveedor prodxprov;
    private Perfil perfil;

    @EJB
    private InventarioFacade ejbInventario;
    @EJB
    private ProductoxproveedorFacade ejbProdxprov;
    @EJB
    private ProductosFacade ejbProductos;

    @PostConstruct
    private void activar() {
        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String redirect = (String) params.get("faces-redirect");
        if (redirect == null) {
            return;
        }

        String p = (String) params.get("p");
        String nombreForma = "InventarioVista";

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

    public InventarioBean() {
        listaInventarios = new LazyDataModel<Inventario>() {
            @Override
            public List<Inventario> load(int i, int i1, SortCriteria[] scs, Map<String, String> map) {
                return carga(i, i1, scs, map);
            }
        };
    }

    public List<Inventario> carga(int i, int pageSize, SortCriteria[] scs, Map<String, String> map) {
        Map parametros = new HashMap();
        String orden = "";
        if (scs.length == 0) {
            orden = "o.fecha asc";
        } else {
            orden += " o." + scs[0].getPropertyName() + (scs[0].isAscending() ? " ASC" : " DESC");
        }
        parametros.put(";orden", orden);
        String where = "o.entrada = true";

        for (Map.Entry e : map.entrySet()) {
            String clave = (String) e.getKey();
            String valor = (String) e.getValue();

            where += " and upper(o." + clave + ") like :valor";
            parametros.put("valor", valor.toUpperCase() + "%");
        }

        if (producto != null) {
            where += " and o.producto=:producto";
            parametros.put("producto", producto);
        }

        parametros.put(";where", where);

        int total = 0;

        try {
            total = ejbInventario.contar(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger
                    .getLogger(InventarioBean.class
                            .getName()).log(Level.SEVERE, null, ex);
        }

        int endIndex = i + pageSize;

        if (endIndex > total) {
            endIndex = total;
        }

        parametros.put(";inicial", i);
        parametros.put(";final", endIndex);
        listaInventarios.setRowCount(total);

        try {
            return ejbInventario.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(InventarioBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public String buscar() {

//        if (producto == null) {
//            MensajesErrores.advertencia("Producto es necesario");
//            return null;
//        }
        setListaInventarios(new LazyDataModel<Inventario>() {

            @Override
            public List<Inventario> load(int i, int i1, SortCriteria[] scs, Map<String, String> map) {
                return carga(i, i1, scs, map);
            }
        });
        return null;
    }

    public String crear() {
        if (!perfil.getNuevo()) {
            MensajesErrores.advertencia("No tiene autorización para crear un registro");
        }
        if (producto == null) {
            MensajesErrores.advertencia("Producto es necesario");
            return null;
        }
        inventario = new Inventario();
        inventario.setPrecioUnitario(BigDecimal.ZERO);
        inventario.setFecha(new Date());
        inventario.setEntrada(Boolean.TRUE);
        inventario.setDescripcion("Ingreso realizado por " + seguridadBean.getEntidad().toString());
        formulario.insertar();
        return null;
    }

    public String modificar() {
        inventario = (Inventario) getListaInventarios().getRowData();
        producto = inventario.getProducto();
        formulario.editar();
        return null;
    }

    public String eliminar() {
        inventario = (Inventario) getListaInventarios().getRowData();
        producto = inventario.getProducto();
        formulario.eliminar();
        return null;
    }

    private boolean validar() {
        if (inventario.getCantidad() == null || inventario.getCantidad() <= 0) {
            MensajesErrores.advertencia("Cantidad mayor a cero");
            return true;
        }
        if (inventario.getPrecioUnitario() == null || inventario.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) {
            MensajesErrores.advertencia("Precio Unitario mayor a cero");
            return true;
        }
        if (inventario.getDescripcion() == null || inventario.getDescripcion().isEmpty()) {
            MensajesErrores.advertencia("Descripcion es necesaria");
            return true;
        }
        if (inventario.getProveedor() == null) {
            MensajesErrores.advertencia("Proveedor es necesario");
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

            if (ControlProdXProv(producto, inventario.getProveedor())) {
                prodxprov = new Productoxproveedor();
                prodxprov.setProveedor(inventario.getProveedor());
                prodxprov.setProducto(producto);
                ejbProdxprov.create(prodxprov, getSeguridadBean().getEntidad().getUserid());
            }

            inventario.setProducto(producto);
            inventario.setProveedor(inventario.getProveedor());
            ejbInventario.create(inventario, getSeguridadBean().getEntidad().getUserid());
            producto.setStock(producto.getStock() + inventario.getCantidad());
            ejbProductos.edit(producto, getSeguridadBean().getEntidad().getUserid());
        } catch (InsertarException | ConsultarException | GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(InventarioBean.class.getName()).log(Level.SEVERE, null, ex);
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
            if (ControlProdXProv(producto, inventario.getProveedor())) {
                prodxprov = new Productoxproveedor();
                prodxprov.setProveedor(inventario.getProveedor());
                prodxprov.setProducto(producto);
                ejbProdxprov.create(prodxprov, getSeguridadBean().getEntidad().getUserid());
            }

            ejbInventario.edit(inventario, getSeguridadBean().getEntidad().getUserid());
//            producto.setStock(producto.getStock() + inventario.getCantidad());
//            ejbProductos.edit(producto, getSeguridadBean().getEntidad().getUserid());
        } catch (GrabarException | ConsultarException | InsertarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(InventarioBean.class.getName()).log(Level.SEVERE, null, ex);
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
            if (ControlProdXProv(producto, inventario.getProveedor())) {
                prodxprov = new Productoxproveedor();
                prodxprov.setProveedor(inventario.getProveedor());
                prodxprov.setProducto(producto);
                ejbProdxprov.remove(prodxprov, getSeguridadBean().getEntidad().getUserid());
            }
            ejbInventario.remove(inventario, getSeguridadBean().getEntidad().getUserid());

        } catch (BorrarException | ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(ProveedoresBean.class.getName()).log(Level.SEVERE, null, ex);
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

    public boolean ControlProdXProv(Productos prod, Proveedores prov) throws ConsultarException {
        prodxprov = new Productoxproveedor();
        Map parametros = new HashMap();
        parametros.put(";where", "o.producto=:producto and o.proveedor=:proveedor");
        parametros.put("producto", prod);
        parametros.put("proveedor", prov);
        List<Productoxproveedor> aux = ejbProdxprov.encontarParametros(parametros);
        if (aux.isEmpty()) {
            return true;
        } else {
            prodxprov = aux.get(0);
            return false;
        }
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
     * @return the listaInventarios
     */
    public LazyDataModel<Inventario> getListaInventarios() {
        return listaInventarios;
    }

    /**
     * @param listaInventarios the listaInventarios to set
     */
    public void setListaInventarios(LazyDataModel<Inventario> listaInventarios) {
        this.listaInventarios = listaInventarios;
    }

    /**
     * @return the producto
     */
    public Productos getProducto() {
        return producto;
    }

    /**
     * @param producto the producto to set
     */
    public void setProducto(Productos producto) {
        this.producto = producto;
    }

    /**
     * @return the prodxprov
     */
    public Productoxproveedor getProdxprov() {
        return prodxprov;
    }

    /**
     * @param prodxprov the prodxprov to set
     */
    public void setProdxprov(Productoxproveedor prodxprov) {
        this.prodxprov = prodxprov;
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
     * @return the inventario
     */
    public Inventario getInventario() {
        return inventario;
    }

    /**
     * @param inventario the inventario to set
     */
    public void setInventario(Inventario inventario) {
        this.inventario = inventario;
    }

}
