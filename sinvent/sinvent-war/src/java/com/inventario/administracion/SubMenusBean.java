/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.administracion;

import com.inventario.entidades.Codigos;
import com.inventario.entidades.Menusistema;
import com.inventario.entidades.Perfil;
import com.inventario.excepciones.BorrarException;
import com.inventario.excepciones.ConsultarException;
import com.inventario.excepciones.GrabarException;
import com.inventario.excepciones.InsertarException;
import com.inventario.seguridad.SeguridadBean;
import com.inventario.servicios.MenusistemaFacade;
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
@ManagedBean(name = "subMenusTabla")
@ViewScoped
public class SubMenusBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of MenusBean
     */
    public SubMenusBean() {
    }
    @ManagedProperty(value = "#{seguridadBean}")
    private SeguridadBean seguridadBean;
    private Formulario formulario = new Formulario();
    private List<Menusistema> menus;
    private Menusistema menu;
    private Menusistema menuPadre;
    private Codigos modulo;
    private Perfil perfil;
    @EJB
    private MenusistemaFacade ejbMenus;

    @PostConstruct
    private void activar() {
        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String redirect = (String) params.get("faces-redirect");
        if (redirect == null) {
            return;
        }

        String p = (String) params.get("p");
        String nombreForma = "SubMenusVista";

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

    // colocamos los metodos en verbo
    public String crear() {
        // se deberia chequear perfil?
        if (!perfil.getNuevo()) {
            MensajesErrores.advertencia("No tiene autorización para crear un registro");
        }

        menu = new Menusistema();
        menu.setIdpadre(menuPadre);
        formulario.insertar();
        return null;
    }

    public String modificar() {
        if (!perfil.getModificacion()) {
            MensajesErrores.advertencia("No tiene autorización para modificar un registro");
        }
        menu = menus.get(formulario.getFila().getRowIndex());
        formulario.editar();
        return null;
    }

    public String eliminar() {
        if (!perfil.getBorrado()) {
            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
        }
        menu = menus.get(formulario.getFila().getRowIndex());
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
        try {
            Map parametros = new HashMap();
            parametros.put(";where", "o.idpadre=:padre");
            parametros.put(";orden", "o.texto");
            parametros.put("padre", menuPadre);
            menus = ejbMenus.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
            Logger.getLogger(SubMenusBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    // acciones de base de datos

    private boolean validar() {
        if ((menu.getTexto() == null) || (menu.getTexto().isEmpty())) {
            MensajesErrores.advertencia("Es necesario Texto a desplegar");
            return true;
        }
        if ((menu.getFormulario() == null) || (menu.getFormulario().isEmpty())) {
            MensajesErrores.advertencia("Es necesario nombre de formulario");
            return true;
        }
        if ((menu.getIdpadre() == null)) {
            MensajesErrores.advertencia("Es necesario seleccionar un  Menú");
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
            ejbMenus.create(menu, seguridadBean.getEntidad().getUserid());
        } catch (InsertarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(SubMenusBean.class.getName()).log(Level.SEVERE, null, ex);
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
            ejbMenus.edit(menu, seguridadBean.getEntidad().getUserid());
        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(SubMenusBean.class.getName()).log(Level.SEVERE, null, ex);
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
            ejbMenus.remove(menu, seguridadBean.getEntidad().getUserid());
        } catch (BorrarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(SubMenusBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        formulario.cancelar();
        buscar();
        return null;
    }
    // falta el combo

    /**
     * @return the menus
     */
    public List<Menusistema> getMenus() {
        return menus;
    }

    /**
     * @param menus the menus to set
     */
    public void setMenus(List<Menusistema> menus) {
        this.menus = menus;
    }

    /**
     * @return the menu
     */
    public Menusistema getMenu() {
        return menu;
    }

    /**
     * @param menu the menu to set
     */
    public void setMenu(Menusistema menu) {
        this.menu = menu;
    }

    /**
     * @return the menuPadre
     */
    public Menusistema getMenuPadre() {
        return menuPadre;
    }

    /**
     * @param menuPadre the menuPadre to set
     */
    public void setMenuPadre(Menusistema menuPadre) {
        this.menuPadre = menuPadre;
    }

    /**
     * @return the modulo
     */
    public Codigos getModulo() {
        return modulo;
    }

    /**
     * @param modulo the modulo to set
     */
    public void setModulo(Codigos modulo) {
        this.modulo = modulo;
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

}
