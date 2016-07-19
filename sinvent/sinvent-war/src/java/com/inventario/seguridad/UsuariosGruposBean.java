/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.seguridad;

import com.icesoft.faces.component.selectinputtext.SelectInputText;
import com.inventario.entidades.Centros;
import com.inventario.entidades.Codigos;
import com.inventario.entidades.Entidades;
import com.inventario.entidades.Grupousuario;
import com.inventario.entidades.Perfil;
import com.inventario.excepciones.BorrarException;
import com.inventario.excepciones.ConsultarException;
import com.inventario.excepciones.GrabarException;
import com.inventario.excepciones.InsertarException;
import com.inventario.servicios.CodigosFacade;
import com.inventario.servicios.EntidadesFacade;
import com.inventario.servicios.GrupousuarioFacade;
import com.inventario.utilitarios.Formulario;
import com.inventario.utilitarios.MensajesErrores;
import java.io.Serializable;
import java.util.ArrayList;
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
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

/**
 *
 * @author edwin
 */
@ManagedBean(name = "usuarioGrupo")
@ViewScoped
public class UsuariosGruposBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of UsuariosGruposBean
     */
    public UsuariosGruposBean() {
    }

    @PostConstruct

    private void activar() {
        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String redirect = (String) params.get("faces-redirect");
        if (redirect == null) {
            return;
        }

        String p = (String) params.get("p");
        String nombreForma = "UsrGrpVista";

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
    private Grupousuario grupoUsuario;
    private Codigos modulo;
    private Centros centro;
    private Perfil perfil;
    private Formulario formulario = new Formulario();

    private List<Grupousuario> listaGrupoUsuarios;

    @EJB
    private GrupousuarioFacade ejbGrpUsr;
    @EJB
    private CodigosFacade ejbCodigos;
    @EJB
    private EntidadesFacade ejbEntidades;

    private Entidades usuario;
    private List usuarios;
    private String apellido;

    public void cambiaApellidoUsuarios(ValueChangeEvent event) {
        usuario = null;
        if (event.getComponent() instanceof SelectInputText) {
            // get the number of displayable records from the component
            SelectInputText autoComplete
                    = (SelectInputText) event.getComponent();
            // get the new value typed by component user.
            String newWord = (String) event.getNewValue();
            // traer la consulta
            Map parametros = new HashMap();
            String where = " o.activo=true and o.rol like '%#U%'  and  upper(o.apellidos) like :pin";

            parametros.put("pin", newWord.toUpperCase() + "%");
            parametros.put(";where", where);
            parametros.put(";orden", " o.apellidos");
            int total = ((SelectInputText) event.getComponent()).getRows();
            // Contadores

            parametros.put(";inicial", 0);
            parametros.put(";final", total);

            usuarios = new ArrayList();

            try {

                List<Entidades> auxent = ejbEntidades.encontarParametros(parametros);
                for (Entidades ent : auxent) {
                    centro = null;
                    SelectItem s = new SelectItem(ent, ent.getApellidos());
                    usuarios.add(s);
                }

            } catch (ConsultarException ex) {
                MensajesErrores.error(ex.getMessage() + ":" + ex.getCause());
                Logger.getLogger("ERROR").log(Level.SEVERE, null, ex);
            }

            if (autoComplete.getSelectedItem() != null) {
                usuario = (Entidades) autoComplete.getSelectedItem().getValue();
            } else {

                Entidades tmp = null;
                for (int i = 0, max = usuarios.size(); i < max; i++) {
                    SelectItem e = (SelectItem) usuarios.get(i);
                    if (e.getLabel().compareToIgnoreCase(newWord) == 0) {
                        tmp = (Entidades) e.getValue();
                    }

                }
                if (tmp != null) {
                    usuario = tmp;
                }
            }

        }
    }

    /**
     * @return the grupoUsuario
     */
    public Grupousuario getGrupoUsuario() {
        return grupoUsuario;
    }

    /**
     * @param grupoUsuario the grupoUsuario to set
     */
    public void setGrupoUsuario(Grupousuario grupoUsuario) {
        this.grupoUsuario = grupoUsuario;
    }

    public String buscar() {
        if (usuario == null) {
            MensajesErrores.informacion("Es necesario un usuario válido");
            return null;
        }
        Map parametros = new HashMap();
        String where = "o.usuario=:usuario";

        if (centro != null) {
            where += " and o.institucion=:institucion";
            parametros.put("institucion", centro);
        }
        if (modulo != null) {
            where += " and o.modulo=:modulo";
            parametros.put("modulo", modulo);
        }

        parametros.put(";where", where);
        parametros.put("usuario", usuario.getPin());

        try {
            listaGrupoUsuarios = ejbGrpUsr.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
            Logger.getLogger(UsuariosGruposBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String nuevo() {
        if (!perfil.getNuevo()) {
            MensajesErrores.advertencia("No tiene autorización para crear un registro");
        }
        if (usuario == null) {
            MensajesErrores.informacion("Es necesario un usuario válido");
            return null;
        }
        grupoUsuario = new Grupousuario();
        grupoUsuario.setUsuario(usuario.getPin());
        if (centro != null) {
            grupoUsuario.setCentro(centro);
        }
        formulario.insertar();
        return null;
    }

    public String modificar() {
        if (!perfil.getModificacion()) {
            MensajesErrores.advertencia("No tiene autorización para modificar un registro");
        }
        grupoUsuario = listaGrupoUsuarios.get(formulario.getFila().getRowIndex());
        formulario.editar();
        return null;
    }

    public String eliminar() {
        if (!perfil.getBorrado()) {
            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
        }
        grupoUsuario = listaGrupoUsuarios.get(formulario.getFila().getRowIndex());
        formulario.eliminar();
        return null;
    }

    public String cancelar() {
        formulario.cancelar();
        buscar();
        return null;
    }

    private boolean validar() {
        if (grupoUsuario.getGrupo() == null) {
            MensajesErrores.advertencia("Seleccione un grupo");
            return true;
        }
//        if (grupoUsuario.getModulo() == null) {
//            MensajesErrores.advertencia("Seleccione un grupo");
//            return true;
//        }
        return false;
    }

    public String insertar() {
        if (validar()) {
            return null;
        }
        try {
            ejbGrpUsr.create(grupoUsuario, seguridadBean.getEntidad().getUserid());
        } catch (InsertarException ex) {
            MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
            Logger.getLogger(UsuariosGruposBean.class.getName()).log(Level.SEVERE, null, ex);
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
            ejbGrpUsr.edit(grupoUsuario, seguridadBean.getEntidad().getUserid());
        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
            Logger.getLogger(UsuariosGruposBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }

    public String borrar() {
        if (validar()) {
            return null;
        }
        try {
            ejbGrpUsr.remove(grupoUsuario, seguridadBean.getEntidad().getUserid());
        } catch (BorrarException ex) {
            MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
            Logger.getLogger(UsuariosGruposBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }

    public String getNombres(String pin) {
        Map parametros = new HashMap();
        parametros.put(";where", "o.pin=:pin");
        parametros.put("pin", pin);
        List<Entidades> lista;
        try {
            lista = ejbEntidades.encontarParametros(parametros);
            if (!lista.isEmpty()) {
                return lista.get(0).toString();
            }
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
            Logger.getLogger(UsuariosGruposBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
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
     * @return the usuario
     */
    public Entidades getUsuario() {
        return usuario;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(Entidades usuario) {
        this.usuario = usuario;
    }

    /**
     * @return the usuarios
     */
    public List getUsuarios() {
        return usuarios;
    }

    /**
     * @param usuarios the usuarios to set
     */
    public void setUsuarios(List usuarios) {
        this.usuarios = usuarios;
    }

    /**
     * @return the apellido
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * @param apellido the apellido to set
     */
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    /**
     * @return the listaGrupoUsuarios
     */
    public List<Grupousuario> getListaGrupoUsuarios() {
        return listaGrupoUsuarios;
    }

    /**
     * @param listaGrupoUsuarios the listaGrupoUsuarios to set
     */
    public void setListaGrupoUsuarios(List<Grupousuario> listaGrupoUsuarios) {
        this.listaGrupoUsuarios = listaGrupoUsuarios;
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
}
