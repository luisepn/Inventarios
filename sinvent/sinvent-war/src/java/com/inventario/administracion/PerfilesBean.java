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
import com.inventario.servicios.PerfilFacade;
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

/**
 *
 * @author edwin
 */
@ManagedBean(name = "perfilesSistema")
@ViewScoped
public class PerfilesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of MenusBean
     */
    public PerfilesBean() {
    }
    @ManagedProperty(value = "#{seguridadBean}")
    private SeguridadBean seguridadBean;
    private Formulario formulario = new Formulario();
    private List<Perfil> perfiles;
    private Perfil perfil;
    private Menusistema menuSeleccionado;
    private Perfil perfilS;
//    private Menusistema submenuSeleccionado;
    @EJB
    private PerfilFacade ejbPerfiles;
    @EJB
    private MenusistemaFacade ejbMenus;
    private Codigos grupo;

    @PostConstruct
    private void activar() {
        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String redirect = (String) params.get("faces-redirect");
        if (redirect == null) {
            return;
        }

        String p = (String) params.get("p");
        String nombreForma = "PerfilVista";

        if (p == null) {
            MensajesErrores.fatal("Sin perfil válido");
            seguridadBean.cerraSession();
        }
        perfilS = seguridadBean.traerPerfil((String) params.get("p"));

        if (this.perfilS == null) {
            MensajesErrores.fatal("Sin perfil válido");
            seguridadBean.cerraSession();
        }

        if (nombreForma.contains(perfilS.getMenu().getFormulario())) {
            if (!this.perfilS.getGrupo().equals(seguridadBean.getGrupo().getGrupo())) {
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
        if (!perfilS.getNuevo()) {
            MensajesErrores.advertencia("No tiene autorización para crear un registro");
            return null;
        }
        if ((getGrupo() == null)) {
            MensajesErrores.advertencia("Por favor seleccione un grupo");
            return null;
        }
        if (menuSeleccionado == null) {
            MensajesErrores.advertencia("Seleccione un menú primero");
            return null;
        }
        perfil = new Perfil();
        perfil.setGrupo(getGrupo());
        //perfil.setIdpadre(menuPadre);
        formulario.insertar();
        return null;
    }

    public String modificar() {
        if (!perfilS.getModificacion()) {
            MensajesErrores.advertencia("No tiene autorización para modificar un registro");
            return null;
        }
        perfil = perfiles.get(formulario.getFila().getRowIndex());

        formulario.editar();
        return null;
    }

    public String eliminar() {
        if (!perfilS.getBorrado()) {
            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
            return null;
        }
        perfil = perfiles.get(formulario.getFila().getRowIndex());
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
        if (!perfilS.getConsulta()) {
            MensajesErrores.advertencia("No tiene autorización para consultar");
            return null;
        }
        if ((getGrupo() == null)) {
            MensajesErrores.advertencia("Por favor seleccione un grupo");
            return null;
        }
        if (menuSeleccionado == null) {
            MensajesErrores.advertencia("Seleccione un menú primero");
            return null;
        }
        try {
            Map parametros = new HashMap();
            parametros.put(";where", "o.grupo=:grupo and o.menu.idpadre=:menu");
            parametros.put(";orden", "o.menu.texto");
            parametros.put("menu", menuSeleccionado);
            parametros.put("grupo", getGrupo());
            perfiles = ejbPerfiles.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
            Logger.getLogger(PerfilesBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    // acciones de base de datos

    private boolean validar() {
        if ((perfil.getGrupo() == null)) {
            MensajesErrores.advertencia("Es necesario grupo");
            return true;
        }

        if ((perfil.getMenu() == null)) {
            MensajesErrores.advertencia("Es necesario seleccionar un  Menú");
            return true;
        }
        return false;
    }

    public String insertar() {
        if (validar()) {
            return null;
        }
        try {
            ejbPerfiles.create(perfil, seguridadBean.getEntidad().getUserid());
        } catch (InsertarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(PerfilesBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }

    public String grabar() {
        if (validar()) {
            return null;
        }
        try {
            ejbPerfiles.edit(perfil, seguridadBean.getEntidad().getUserid());
        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(PerfilesBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }

    public String borrar() {
        if (!perfilS.getBorrado()) {
            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
        }
        try {
            ejbPerfiles.remove(perfil, seguridadBean.getEntidad().getUserid());
        } catch (BorrarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(PerfilesBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        formulario.cancelar();
        buscar();
        return null;
    }

    /**
     * @return the perfiles
     */
    public List<Perfil> getPerfiles() {
        return perfiles;
    }

    /**
     * @param perfiles the perfiles to set
     */
    public void setPerfiles(List<Perfil> perfiles) {
        this.perfiles = perfiles;
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
     * @return the menuSeleccionado
     */
    public Menusistema getMenuSeleccionado() {
        return menuSeleccionado;
    }

    /**
     * @param menuSeleccionado the menuSeleccionado to set
     */
    public void setMenuSeleccionado(Menusistema menuSeleccionado) {
        this.menuSeleccionado = menuSeleccionado;
    }

    public SelectItem[] getComboDisponibles() {
        Map parametros = new HashMap();
        parametros.put(";where", "o.idpadre=:padre");
        parametros.put(";orden", "o.texto");
        parametros.put("padre", menuSeleccionado);
        try {
            List<Menusistema> menus = ejbMenus.encontarParametros(parametros);
            List<Menusistema> disponibles = new LinkedList<>();
            for (Menusistema m : menus) {
                parametros = new HashMap();
                parametros.put(";where", "o.grupo=:grupo and o.menu=:menu");
//                parametros.put(";orden", "o.menu.texto");
                parametros.put("menu", m);
                parametros.put("grupo", grupo);
                List<Perfil> lp = ejbPerfiles.encontarParametros(parametros);
                if ((lp == null) || (lp.isEmpty())) {
                    disponibles.add(m);
                }
            }
            return Combos.SelectItems(disponibles, true);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(PerfilesBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public SelectItem[] getComboTodos() {
        Map parametros = new HashMap();
        parametros.put(";where", "o.idpadre=:padre");
        parametros.put(";orden", "o.texto");
        parametros.put("padre", menuSeleccionado);
        try {
            List<Menusistema> menus = ejbMenus.encontarParametros(parametros);
            List<Menusistema> disponibles = new LinkedList<>();
            for (Menusistema m : menus) {
                parametros = new HashMap();
                parametros.put(";where", "o.grupo=:grupo and o.menu=:menu");
//                parametros.put(";orden", "o.menu.texto");
                parametros.put("menu", m);
                parametros.put("grupo", getGrupo());
                List<Perfil> lp = ejbPerfiles.encontarParametros(parametros);
                //if ((lp == null) || (lp.isEmpty())) {
                disponibles.add(m);
                //}
            }
            return Combos.SelectItems(disponibles, true);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(PerfilesBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

//    public SelectItem[] getComboMenus() {
//        Map parametros = new HashMap();
//        parametros.put(";where", "o.idpadre=:padre");
//        parametros.put(";orden", "o.texto");
//        parametros.put("padre", menuSeleccionado);
//        try {
//            List<Menusistema> menus = ejbMenus.encontarParametros(parametros);
//            List<Menusistema> disponibles = new LinkedList<>();
//            List<Menusistema> otros = new LinkedList<>();
//            List<Perfil> lp;
//            for (Menusistema m : menus) {
//                parametros = new HashMap();
//                parametros.put(";where", "o.grupo=:grupo and o.menu=:menu");
//                parametros.put("menu", m);
//                parametros.put("grupo", getGrupo());
//                lp = ejbPerfiles.encontarParametros(parametros);
//                for (Perfil p : lp) {
//                    if (m.equals(p.getMenu())) {
//                        disponibles.add(m);
//                    }
//                }
//            }
//            otros = menus;
////            for (Menusistema s : menus) {
////                for (Menusistema m : disponibles) {
////                    if (m.equals(s)) {
////                        otros.remove(m);
////                    }
////                }
////            }
//            return Combos.SelectItems(otros, true);
//        } catch (ConsultarException ex) {
//            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
//            Logger.getLogger(PerfilesBean.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//
//    }
    /**
     * @return the grupo
     */
    public Codigos getGrupo() {
        return grupo;
    }

    /**
     * @param grupo the grupo to set
     */
    public void setGrupo(Codigos grupo) {
        this.grupo = grupo;
    }

    /**
     * @return the perfilS
     */
    public Perfil getPerfilS() {
        return perfilS;
    }

    /**
     * @param perfilS the perfilS to set
     */
    public void setPerfilS(Perfil perfilS) {
        this.perfilS = perfilS;
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
