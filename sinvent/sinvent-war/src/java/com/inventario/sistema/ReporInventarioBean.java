/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.sistema;

import com.inventario.entidades.Inventario;
import com.inventario.entidades.Perfil;
import com.inventario.entidades.Productos;
import com.inventario.entidades.Productoxproveedor;
import com.inventario.entidades.Proveedores;
import com.inventario.excepciones.ConsultarException;
import com.inventario.seguridad.SeguridadBean;
import com.inventario.servicios.InventarioFacade;
import com.inventario.servicios.ProductoxproveedorFacade;
import com.inventario.utilitarios.Formulario;
import com.inventario.utilitarios.MensajesErrores;
import java.io.Serializable;
import java.util.Calendar;
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
@ManagedBean(name = "RepInven")
@ViewScoped
public class ReporInventarioBean implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * Creates a new instance of InventarioBean
     */
    @ManagedProperty(value = "#{seguridadBean}")
    private SeguridadBean seguridadBean;
    private Date desde = new Date();
    private Date hasta = new Date();
    private Formulario formulario = new Formulario();
    private LazyDataModel<Inventario> listaInventarios;
    private Inventario inventario;
    private Proveedores proveedor;
    private Productos producto;
    private Productoxproveedor prodxprov;
    private Perfil perfil;
    private final Calendar fecha = Calendar.getInstance();

    @EJB
    private InventarioFacade ejbInventario;
    @EJB
    private ProductoxproveedorFacade ejbProdxprov;

    @PostConstruct
    private void activar() {
        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String redirect = (String) params.get("faces-redirect");
        if (redirect == null) {
            return;
        }

        String p = (String) params.get("p");
        String nombreForma = "RepInventarioVista";

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

    public ReporInventarioBean() {
        listaInventarios = new LazyDataModel<Inventario>() {
            @Override
            public List<Inventario> load(int i, int i1, SortCriteria[] scs, Map<String, String> map) {
                return null;
            }
        };
    }

    public List<Inventario> carga(int i, int pageSize, SortCriteria[] scs, Map<String, String> map) {
        Map parametros = new HashMap();
        String orden = "";
        if (scs.length == 0) {
            orden = "o.fecha desc";
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

        if (producto != null) {
            where += " and o.producto=:producto";
            parametros.put("producto", producto);
        }

        if (proveedor != null) {
            where += " and o.proveedor=:proveedor";
            parametros.put("proveedor", proveedor);
        }

        if (desde != null && hasta != null) {
            where += " and o.fecha between :desde and :hasta ";
            parametros.put("desde", desde);
            parametros.put("hasta", hasta);
        }

        parametros.put(";where", where);

        int total = 0;

        try {
            total = ejbInventario.contar(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(ReporInventarioBean.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ReporInventarioBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public String buscar() {

        if (desde == null) {
            desde = new Date();
        }
        if (hasta == null) {
            hasta = new Date();
        }

        if (desde.after(hasta)) {
            MensajesErrores.advertencia("Fecha Desde no Puede Ser Mayor que Fecha Hasta");
            listaInventarios.setRowCount(0);
            return null;
        }

        fecha.setTime(desde);
        fecha.set(Calendar.HOUR_OF_DAY, 00);
        fecha.set(Calendar.MINUTE, 00);
        desde = fecha.getTime();

        fecha.setTime(hasta);
        fecha.set(Calendar.HOUR_OF_DAY, 23);
        fecha.set(Calendar.MINUTE, 59);
        hasta = fecha.getTime();

        listaInventarios = new LazyDataModel<Inventario>() {

            @Override
            public List<Inventario> load(int i, int i1, SortCriteria[] scs, Map<String, String> map) {
                return carga(i, i1, scs, map);
            }
        };

        return null;
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

    /**
     * @return the desde
     */
    public Date getDesde() {
        return desde;
    }

    /**
     * @param desde the desde to set
     */
    public void setDesde(Date desde) {
        this.desde = desde;
    }

    /**
     * @return the hasta
     */
    public Date getHasta() {
        return hasta;
    }

    /**
     * @param hasta the hasta to set
     */
    public void setHasta(Date hasta) {
        this.hasta = hasta;
    }

}
