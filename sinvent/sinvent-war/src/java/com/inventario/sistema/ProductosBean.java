/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.sistema;

import com.inventario.administracion.CentrosBean;
import com.inventario.entidades.Archivos;
import com.inventario.entidades.Codigos;
import com.inventario.entidades.Perfil;
import com.inventario.entidades.Productos;
import com.inventario.excepciones.BorrarException;
import com.inventario.excepciones.ConsultarException;
import com.inventario.excepciones.GrabarException;
import com.inventario.excepciones.InsertarException;
import com.inventario.seguridad.SeguridadBean;
import com.inventario.servicios.ArchivosFacade;
import com.inventario.servicios.ProductosFacade;
import com.inventario.utilitarios.Combos;
import com.inventario.utilitarios.Formulario;
import com.inventario.utilitarios.MensajesErrores;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
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
import javax.faces.model.SelectItem;
import org.icefaces.ace.component.fileentry.FileEntry;
import org.icefaces.ace.component.fileentry.FileEntryEvent;
import org.icefaces.ace.component.fileentry.FileEntryResults;
import org.icefaces.ace.model.table.LazyDataModel;
import org.icefaces.ace.model.table.SortCriteria;

/**
 *
 * @author edwin
 */
@ManagedBean(name = "productos")
@ViewScoped
public class ProductosBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of MaestrosBean
     */
    public ProductosBean() {

        productoslista = new LazyDataModel<Productos>() {
            @Override
            public List<Productos> load(int i, int i1, SortCriteria[] scs, Map<String, String> map) {
                //
                return null;
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
        String nombreForma = "ProductosVista";

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

    @ManagedProperty(value = "#{seguridadBean}")
    private SeguridadBean seguridadBean;
    private Formulario formulario = new Formulario();
    private LazyDataModel<Productos> productoslista;
    private Productos producto;
    private String modulo;
    private String nombre;
    private Archivos logo;
    private Codigos categoria;
    private Perfil perfil;
    @EJB
    private ProductosFacade ejbProducto;
    @EJB
    private ArchivosFacade ejbArchivos;

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

    // colocamos los metodos en verbo
    public String crear() {
        // se deberia chequear perfil?
        if (!perfil.getNuevo()) {
            MensajesErrores.advertencia("No tiene autorización para crear un registro");
        }
        setProducto(new Productos());
        producto.setStock(0);
        logo = new Archivos();

        formulario.insertar();
        return null;
    }

    public String modificar() {
        if (!perfil.getModificacion()) {
            MensajesErrores.advertencia("No tiene autorización para modificar un registro");
            return null;
        }
        setProducto((Productos) getProductoslista().getRowData());

        if (producto.getImagen() != null) {
            this.logo = traerArchivo(producto.getImagen().getId());
        } else {
            logo = new Archivos();
        }
        formulario.editar();
        return null;
    }

    public String eliminar() {
        if (!perfil.getBorrado()) {
            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
            return null;
        }
        setProducto((Productos) getProductoslista().getRowData());
        formulario.eliminar();
        return null;
    }

    public String cancelar() {
        formulario.cancelar();
        buscar();
        return null;
    }
    // buscar

    public String buscar() {
        if (!perfil.getConsulta()) {
            MensajesErrores.advertencia("No tiene autorización para consultar");
            return null;
        }

        setProductoslista(new LazyDataModel<Productos>() {
            @Override
            public List<Productos> load(int i, int pageSize, SortCriteria[] scs, Map<String, String> map) {
                Map parametros = new HashMap();
                if (scs.length == 0) {
                    parametros.put(";orden", "o.codigo asc");
                } else {
                    parametros.put(";orden", "o." + scs[0].getPropertyName()
                            + (scs[0].isAscending() ? " ASC" : " DESC"));
                }
                String where = " o.activo=true ";
                for (Map.Entry e : map.entrySet()) {
                    String clave = (String) e.getKey();
                    String valor = (String) e.getValue();
                    where += " and upper(o." + clave + ") like :" + clave;
                    parametros.put(clave, valor.toUpperCase() + "%");
                }

                if (!((nombre == null) || (nombre.isEmpty()))) {
                    where += " and lower(o.nombre) like :nombre";
                    parametros.put("nombre", "%" + nombre.toLowerCase() + "%");//Problemas con la tilde en mayusculas
                }

                if (categoria != null) {
                    where += " and o.categoria=:ciclo";
                    parametros.put("ciclo", categoria);
                }

                int total = 0;
                try {
                    parametros.put(";where", where);
                    total = ejbProducto.contar(parametros);
                } catch (ConsultarException ex) {
                    MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
                    Logger.getLogger("").log(Level.SEVERE, null, ex);
                }
                int endIndex = i + pageSize;
                if (endIndex > total) {
                    endIndex = total;
                }
                parametros.put(";inicial", i);
                parametros.put(";final", endIndex);
                getProductoslista().setRowCount(total);
                try {
                    return ejbProducto.encontarParametros(parametros);
                } catch (ConsultarException ex) {
                    MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
                    Logger.getLogger("").log(Level.SEVERE, null, ex);
                }
                return null;
            }
        });
        return null;
    }

    // acciones de base de datos
    private boolean validar() {
        if ((getProducto().getCodigo() == null) || (getProducto().getCodigo().isEmpty())) {
            MensajesErrores.advertencia("Es necesario código");
            return true;
        }
        if (formulario.isNuevo()) {
            Map parametros = new HashMap();
            parametros.put(";where", " o.codigo=:codigo and o.activo = true");
            parametros.put("codigo", producto.getCodigo());

            try {
                if (ejbProducto.contar(parametros) > 0) {
                    MensajesErrores.advertencia("Código duplicado");
                    return true;
                }
            } catch (ConsultarException ex) {
                Logger.getLogger(ProductosBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if ((getProducto().getNombre() == null) || (getProducto().getNombre().isEmpty())) {
            MensajesErrores.advertencia("Es necesario nombre");
            return true;
        }
        if ((getProducto().getPrecio() == null)) {
            MensajesErrores.advertencia("Es necesario precio");
            return true;
        }

        if ((getProducto().getUnidadMedida() == null)) {
            MensajesErrores.advertencia("Es necesario unidad de medida");
            return true;
        }

        if ((getProducto().getCategoria() == null)) {
            MensajesErrores.advertencia("Es necesario categoria");
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
            producto.setActivo(true);

            ejbArchivos.create(logo, seguridadBean.getEntidad().getUserid());
            producto.setImagen(logo);
            ejbProducto.create(getProducto(), seguridadBean.getEntidad().getUserid());

        } catch (InsertarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(ProductosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }

    public String grabar() {
        if (!perfil.getModificacion()) {
            MensajesErrores.advertencia("No tiene autorización para modificar un registro");
        }
        if (validar()) {
            return null;
        }
        try {
            if (logo.getId() == null) {
                ejbArchivos.create(logo, seguridadBean.getEntidad().getUserid());
                producto.setImagen(logo);
            } else {
                ejbArchivos.edit(logo, seguridadBean.getEntidad().getUserid());
                producto.setImagen(logo);
            }
            ejbProducto.edit(getProducto(), seguridadBean.getEntidad().getUserid());
        } catch (GrabarException | InsertarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(ProductosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }

    public String borrar() {
        if (!perfil.getBorrado()) {
            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
        }
        try {
            producto.setActivo(Boolean.FALSE);
            ejbProducto.edit(getProducto(), seguridadBean.getEntidad().getUserid());
        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(ProductosBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        formulario.cancelar();
        buscar();
        return null;
    }
    // falta el combo

    public Productos traer(Integer id) throws ConsultarException {
        return ejbProducto.find(id);
    }

    public SelectItem[] getComboProductos() {
        try {
            Map parametros = new HashMap();
            String where = "o.activo = true";
            if (categoria != null) {
                where += " and o.categoria=:categoria";
                parametros.put("categoria", categoria);
            }
            parametros.put(";orden", "o.nombre");
            parametros.put(";where", where);
            return Combos.SelectItems(ejbProducto.encontarParametros(parametros), true);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(ProductosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    public String archivoListener(FileEntryEvent e) {
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

    /**
     * @return the modulo
     */
    public String getModulo() {
        return modulo;
    }

    /**
     * @param modulo the modulo to set
     */
    public void setModulo(String modulo) {
        this.modulo = modulo;
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
     * @return the productoslista
     */
    public LazyDataModel<Productos> getProductoslista() {
        return productoslista;
    }

    /**
     * @param productoslista the productoslista to set
     */
    public void setProductoslista(LazyDataModel<Productos> productoslista) {
        this.productoslista = productoslista;
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

}
