/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.sistema;

import com.icesoft.faces.component.selectinputtext.SelectInputText;
import com.inventario.entidades.Codigos;
import com.inventario.entidades.DetalleVenta;
import com.inventario.entidades.Inventario;
import com.inventario.entidades.Perfil;
import com.inventario.entidades.Productos;
import com.inventario.entidades.RegistroVenta;
import com.inventario.excepciones.ConsultarException;
import com.inventario.excepciones.GrabarException;
import com.inventario.excepciones.InsertarException;
import com.inventario.seguridad.SeguridadBean;
import com.inventario.servicios.DetalleVentaFacade;
import com.inventario.servicios.InventarioFacade;
import com.inventario.servicios.ProductosFacade;
import com.inventario.servicios.RegistroVentaFacade;
import com.inventario.utilitarios.Formulario;
import com.inventario.utilitarios.MensajesErrores;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.icefaces.ace.model.table.LazyDataModel;
import org.icefaces.ace.model.table.SortCriteria;

/**
 *
 * @author edison
 */
@ManagedBean(name = "ventas")
@ViewScoped
public class VentasBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManagedProperty(value = "#{seguridadBean}")
    private SeguridadBean seguridadBean;

    private Formulario formulario = new Formulario();
    private LazyDataModel<RegistroVenta> listaVentas;
    private RegistroVenta registro;
    private Codigos categoria;

    private List<DetalleVenta> listaDetalle;
    private Perfil perfil;

    private String clave;
    private List productos;
    private DetalleVenta detalle;

    @EJB
    private InventarioFacade ejbInventario;
    @EJB
    private ProductosFacade ejbProductos;
    @EJB
    private RegistroVentaFacade ejbRegistroVentas;
    @EJB
    private DetalleVentaFacade ejbDetalleVenta;

    @PostConstruct
    private void activar() {
        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String redirect = (String) params.get("faces-redirect");
        if (redirect == null) {
            return;
        }

        String p = (String) params.get("p");
        String nombreForma = "VentasVista";

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

        seguridadBean.setCliente(null);
        seguridadBean.setClaveBusqueda("");
    }

    public VentasBean() {
        listaVentas = new LazyDataModel<RegistroVenta>() {
            @Override
            public List<RegistroVenta> load(int i, int i1, SortCriteria[] scs, Map<String, String> map) {
                return carga(i, i1, scs, map);
            }
        };
    }

    public List<RegistroVenta> carga(int i, int pageSize, SortCriteria[] scs, Map<String, String> map) {
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
            String clave1 = (String) e.getKey();
            String valor = (String) e.getValue();

            where += " and upper(o." + clave1 + ") like :valor";
            parametros.put("valor", valor.toUpperCase() + "%");
        }

        parametros.put(";where", where);

        int total = 0;

        try {
            total = ejbRegistroVentas.contar(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(VentasBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        int endIndex = i + pageSize;

        if (endIndex > total) {
            endIndex = total;
        }

        parametros.put(";inicial", i);
        parametros.put(";final", endIndex);
        listaVentas.setRowCount(total);

        try {
            return ejbRegistroVentas.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(VentasBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public String buscar() {
        listaVentas = new LazyDataModel<RegistroVenta>() {

            @Override
            public List<RegistroVenta> load(int i, int i1, SortCriteria[] scs, Map<String, String> map) {
                return carga(i, i1, scs, map);
            }
        };
        return null;
    }

    public String crear() {
        if (!perfil.getNuevo()) {
            MensajesErrores.advertencia("No tiene autorización para crear un registro");
        }

        if (seguridadBean.getCliente() == null) {
            MensajesErrores.advertencia("Seleccione un cliente");
            return null;
        }

        registro = new RegistroVenta();
        registro.setCliente(seguridadBean.getCliente());
        registro.setVendedor(seguridadBean.getEntidad());
        listaDetalle = new LinkedList<>();
        formulario.insertar();
        return null;
    }

    private boolean validar() {
        if (listaDetalle.isEmpty()) {
            MensajesErrores.advertencia("Ingrese al menos un registro");
            return true;
        }
        return false;
    }

    public String grabar() {
        if (validar()) {
            return null;
        }
        try {

            registro.setFecha(new Date());
            ejbRegistroVentas.create(registro, seguridadBean.getEntidad().getUserid());
            for (DetalleVenta dv : listaDetalle) {
                dv.setRegistroVenta(registro);
                ejbDetalleVenta.create(dv, seguridadBean.getEntidad().getUserid());
                Productos p = dv.getProducto();
                p.setStock(p.getStock() != null ? p.getStock() : 0 - dv.getCantidad());
                ejbProductos.edit(p, seguridadBean.getEntidad().getUserid());

                Inventario inventario = new Inventario();
                inventario.setProducto(p);
                inventario.setCantidad(dv.getCantidad());
                inventario.setEntrada(Boolean.FALSE);
                inventario.setFecha(new Date());
                inventario.setPrecioUnitario(dv.getPrecioUnitario());
                inventario.setDescripcion("Venta realizada por " + seguridadBean.getEntidad().toString());

                ejbInventario.create(inventario, seguridadBean.getEntidad().getUserid());
            }

        } catch (InsertarException | GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(VentasBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        clave = "";
        return null;
    }

    public String cancelar() {
        formulario.cancelar();
        buscar();
        detalle = null;
        clave = "";
        return null;
    }

    public String insertarDetalle() {
        if (detalle == null) {

        }

        if (detalle.getCantidad() == null || detalle.getCantidad() < 0) {
            MensajesErrores.advertencia("Cantidad es necesaria y debe ser positiva");
            return null;
        }

        if (detalle.getPrecioUnitario() == null) {
            MensajesErrores.advertencia("Cantidad es necesaria");
            return null;
        }

        for (DetalleVenta dv : listaDetalle) {
            if (dv.getProducto().equals(detalle.getProducto())) {
                MensajesErrores.advertencia("El producto ya fue registrado");
                return null;
            }
        }

        listaDetalle.add(detalle);
        detalle = null;
        clave = "";
        return null;
    }

    public String eliminarDetalle(int fila) {
        listaDetalle.remove(fila);
        return null;
    }

    public Double calcularTotal(RegistroVenta venta) {
        Double total = 0.00;
        Map parametro = new HashMap();
        parametro.put(";where", " o.registroVenta=:venta");
        parametro.put("venta", venta);

        try {
            List<DetalleVenta> lista = ejbDetalleVenta.encontarParametros(parametro);
            for (DetalleVenta dv : lista) {
                total += dv.getCantidad().doubleValue() * dv.getPrecioUnitario().doubleValue();
            }
        } catch (ConsultarException ex) {
            Logger.getLogger(VentasBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return total;
    }

    public void cambiaProductos(ValueChangeEvent event) {
        detalle = null;
        if (event.getComponent() instanceof SelectInputText) {
            // get the number of displayable records from the component
            SelectInputText autoComplete
                    = (SelectInputText) event.getComponent();
            // get the new value typed by component user.
            String newWord = (String) event.getNewValue();
            // traer la consulta
            Map parametros = new HashMap();
            String where = " o.activo=true and  (upper(o.nombre) like :clave or upper(o.codigo) like :clave)";
            parametros.put("clave", "%" + newWord.toUpperCase() + "%");

            if (categoria != null) {
                where += " and o.categoria=:categoria";
                parametros.put("categoria", categoria);
            }

            parametros.put(";where", where);
            parametros.put(";orden", " o.nombre");
            int total = ((SelectInputText) event.getComponent()).getRows();
            // Contadores

            parametros.put(";inicial", 0);
            parametros.put(";final", total);

            productos = new ArrayList();

            try {
                List<Productos> aux = ejbProductos.encontarParametros(parametros);

                for (Productos p : aux) {
                    SelectItem s = new SelectItem(p, p.toString());
                    productos.add(s);
                }

            } catch (ConsultarException ex) {
                MensajesErrores.error(ex.getMessage() + ":" + ex.getCause());
                Logger.getLogger("ERROR").log(Level.SEVERE, null, ex);
            }

            detalle = new DetalleVenta();

            if (autoComplete.getSelectedItem() != null) {
                detalle.setProducto((Productos) autoComplete.getSelectedItem().getValue());
                detalle.setPrecioUnitario(detalle.getProducto().getPrecio());
            } else {

                Productos tmp = null;
                for (int i = 0, max = productos.size(); i < max; i++) {
                    SelectItem e = (SelectItem) productos.get(i);
                    if (e.getLabel().compareToIgnoreCase(newWord) == 0) {
                        tmp = (Productos) e.getValue();
                    }
                }
                if (tmp != null) {
                    detalle.setProducto(tmp);
                    detalle.setPrecioUnitario(tmp.getPrecio());
                }
            }

        }
    }

    public Double getSuma() {
        double total = 0;
        for (DetalleVenta dv : listaDetalle) {
            total += dv.getCantidad().doubleValue() * dv.getPrecioUnitario().doubleValue();
        }

        return total;
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
     * @return the listaVentas
     */
    public LazyDataModel<RegistroVenta> getListaVentas() {
        return listaVentas;
    }

    /**
     * @param listaVentas the listaVentas to set
     */
    public void setListaVentas(LazyDataModel<RegistroVenta> listaVentas) {
        this.listaVentas = listaVentas;
    }

    /**
     * @return the registro
     */
    public RegistroVenta getRegistro() {
        return registro;
    }

    /**
     * @param registro the registro to set
     */
    public void setRegistro(RegistroVenta registro) {
        this.registro = registro;
    }

    /**
     * @return the categoria
     */
    public Codigos getCategoria() {
        return categoria;
    }

    /**
     * @param categoria the categoria to set
     */
    public void setCategoria(Codigos categoria) {
        this.categoria = categoria;
    }

    /**
     * @return the listaDetalle
     */
    public List<DetalleVenta> getListaDetalle() {
        return listaDetalle;
    }

    /**
     * @param listaDetalle the listaDetalle to set
     */
    public void setListaDetalle(List<DetalleVenta> listaDetalle) {
        this.listaDetalle = listaDetalle;
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
     * @return the clave
     */
    public String getClave() {
        return clave;
    }

    /**
     * @param clave the clave to set
     */
    public void setClave(String clave) {
        this.clave = clave;
    }

    /**
     * @return the productos
     */
    public List getProductos() {
        return productos;
    }

    /**
     * @param productos the productos to set
     */
    public void setProductos(List productos) {
        this.productos = productos;
    }

    /**
     * @return the detalle
     */
    public DetalleVenta getDetalle() {
        return detalle;
    }

    /**
     * @param detalle the detalle to set
     */
    public void setDetalle(DetalleVenta detalle) {
        this.detalle = detalle;
    }

}
