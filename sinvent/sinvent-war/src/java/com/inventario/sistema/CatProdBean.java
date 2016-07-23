/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.sistema;

import com.inventario.entidades.Codigos;
import com.inventario.entidades.Maestros;
import com.inventario.entidades.Perfil;
import com.inventario.excepciones.BorrarException;
import com.inventario.excepciones.ConsultarException;
import com.inventario.excepciones.GrabarException;
import com.inventario.excepciones.InsertarException;
import com.inventario.seguridad.SeguridadBean;
import com.inventario.servicios.CodigosFacade;
import com.inventario.servicios.MaestrosFacade;
import com.inventario.utilitarios.Formulario;
import com.inventario.utilitarios.MensajesErrores;
import java.io.Serializable;
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

/**
 *
 * @author edwin
 */
@ManagedBean(name = "catProd")
@ViewScoped
public class CatProdBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of CodigosBean
     */
    public CatProdBean() {
        
    }
    
    @ManagedProperty(value = "#{seguridadBean}")
    private SeguridadBean seguridadBean;
    private Formulario formulario = new Formulario();
    private List<Codigos> codigos;
    private Codigos codigo;
    private Integer maestro;
    private Maestros maestroEntidad;
    private String codigoMaestro;
    private Perfil perfil;
    @EJB
    private CodigosFacade ejbCodigos;
    @EJB
    private MaestrosFacade ejbMaestro;


    @PostConstruct
    private void activar() {
        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String redirect = (String) params.get("faces-redirect");
        if (redirect == null) {
            return;
        }
        String p = (String) params.get("p");
        String nombreForma = "CategoriaProdVista";

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
     * @return the codigos
     */
    public List<Codigos> getCodigos() {
        return codigos;
    }

    /**
     * @param codigos the codigos to set
     */
    public void setCodigos(List<Codigos> codigos) {
        this.codigos = codigos;
    }

    /**
     * @return the codigo
     */
    public Codigos getCodigo() {
        return codigo;
    }

    /**
     * @param codigo the codigo to set
     */
    public void setCodigo(Codigos codigo) {
        this.codigo = codigo;
    }

    // colocamos los metodos en verbo
    public String crear() {
        // se deberia chequear perfil?
        if (!perfil.getNuevo()) {
            MensajesErrores.advertencia("No tiene autorización para crear un registro");
            return null;
        }
//        if (maestroEntidad == null) {
//            MensajesErrores.advertencia("Seleccione una tabla maestra primero");
//            return null;
//        }
          try {
            maestroEntidad=ejbMaestro.find(4);
        } catch (ConsultarException ex) {
            Logger.getLogger(CatProdBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        codigo = new Codigos();
        setCodigoMaestro(getMaestroEntidad().getCodigo());
    
        codigo.setMaestro(maestroEntidad);
//        

        formulario.insertar();
        return null;
    }

    public String modificar() {
        if (!perfil.getModificacion()) {
            MensajesErrores.advertencia("No tiene autorización para modificar un registro");
            return null;
        }
        codigo = codigos.get(formulario.getFila().getRowIndex());
        formulario.editar();
        return null;
    }

    public String eliminar() {
        if (!perfil.getBorrado()) {
            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
            return null;
        }
        codigo = codigos.get(formulario.getFila().getRowIndex());
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
//        if ((codigoMaestro == null) || (codigoMaestro.isEmpty())) {
//            MensajesErrores.advertencia("Seleccione una tabla maestra primero");
//            return null;
//        }
//        try {
//
//            maestroEntidad = ejbCodigos.traerMaestroCodigo(codigoMaestro,Combos.getModuloStr());
//        } catch (ConsultarException ex) {
//            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
//            Logger.getLogger(CodigosBean.class.getName()).log(Level.SEVERE, null, ex);
//        }
        if (!perfil.getConsulta()) {
            MensajesErrores.advertencia("No tiene autorización para consultar");
            return null;
        }
//        if (getMaestroEntidad() == null) {
//            MensajesErrores.advertencia("Seleccione una tabla maestra primero");
//            return null;
//        }
        
          try {
            maestroEntidad=ejbMaestro.find(4);
        } catch (ConsultarException ex) {
            Logger.getLogger(CatProdBean.class.getName()).log(Level.SEVERE, null, ex);
        }    
        
        try {
            Map parametros = new HashMap();
            parametros.put(";where", "o.maestro=:maestroParametro");
            parametros.put("maestroParametro", getMaestroEntidad());
//            parametros.put("modulo", maestroEntidad.getCodigo());
            codigos = ejbCodigos.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(CatProdBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    // acciones de base de datos

    private boolean validar() {
        if ((codigo.getCodigo() == null) || (codigo.getCodigo().isEmpty())) {
            MensajesErrores.advertencia("Es necesario código");
            return true;
        }
        if ((codigo.getNombre() == null) || (codigo.getNombre().isEmpty())) {
            MensajesErrores.advertencia("Es necesario nombre");
            return true;
        }
        if ((codigo.getDescripcion() == null) || (codigo.getDescripcion().isEmpty())) {
            MensajesErrores.advertencia("Es necesario descripción");
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
            codigo.setMaestro(maestroEntidad);
            ejbCodigos.create(codigo, seguridadBean.getEntidad().getUserid());
        } catch (InsertarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(CatProdBean.class.getName()).log(Level.SEVERE, null, ex);
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
            ejbCodigos.edit(codigo, seguridadBean.getEntidad().getUserid());

        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(CatProdBean.class.getName()).log(Level.SEVERE, null, ex);
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
            ejbCodigos.remove(codigo, seguridadBean.getEntidad().getUserid());
        } catch (BorrarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(CatProdBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        formulario.cancelar();
        buscar();
        return null;
    }
    // falta el combo

    public Codigos traer(Integer id) throws ConsultarException {
        return ejbCodigos.find(id);
    }

    /**
     * @return the maestro
     */
    public Integer getMaestro() {
        return maestro;
    }

    /**
     * @param maestro the maestro to set
     */
    public void setMaestro(Integer maestro) {
        this.maestro = maestro;
    }

    /**
     * @return the codigoMaestro
     */
    public String getCodigoMaestro() {
        return codigoMaestro;
    }

    /**
     * @param codigoMaestro the codigoMaestro to set
     */
    public void setCodigoMaestro(String codigoMaestro) {
        this.codigoMaestro = codigoMaestro;
    }

    /**
     * @return the maestroEntidad
     */
    public Maestros getMaestroEntidad() {
        return maestroEntidad;
    }

    /**
     * @param maestroEntidad the maestroEntidad to set
     */
    public void setMaestroEntidad(Maestros maestroEntidad) {
        this.maestroEntidad = maestroEntidad;
    }

//    public static SelectItem[] getSelectItems(List<Codigos> entities, boolean selectOne) {
//        if (entities == null) {
//            return null;
//        }
//        int size = selectOne ? entities.size() + 1 : entities.size();
//        SelectItem[] items = new SelectItem[size];
//        int i = 0;
//        if (selectOne) {
//            items[0] = new SelectItem(null, "---");
//            i++;
//        }
//        for (Codigos x : entities) {
//            items[i++] = new SelectItem(x.getCodigo(), x.toString());
//        }
//        return items;
//    }


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


    
}
