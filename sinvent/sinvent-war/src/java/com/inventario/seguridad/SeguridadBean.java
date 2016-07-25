package com.inventario.seguridad;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.icesoft.faces.component.selectinputtext.SelectInputText;
import com.inventario.entidades.Centros;
import com.inventario.entidades.Entidades;
import com.inventario.entidades.Grupousuario;
import com.inventario.entidades.Menusistema;
import com.inventario.entidades.Perfil;
import com.inventario.excepciones.ConsultarException;
import com.inventario.excepciones.GrabarException;
import com.inventario.servicios.CentrosFacade;
import com.inventario.servicios.EntidadesFacade;
import com.inventario.servicios.GrupousuarioFacade;
import com.inventario.servicios.MenusistemaFacade;
import com.inventario.servicios.PerfilFacade;
import com.inventario.utilitarios.Codificador;
import com.inventario.utilitarios.Formulario;
import com.inventario.utilitarios.MensajesErrores;
import java.io.IOException;
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
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import org.icefaces.ace.component.submenu.Submenu;
import org.icefaces.ace.model.DefaultMenuModel;
import org.icefaces.ace.model.MenuModel;

/**
 *
 * @author luisfernando
 */
@ManagedBean(name = "seguridadBean")
@SessionScoped
public class SeguridadBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of SeguridadBean
     */
    public SeguridadBean() {
    }

    @PostConstruct
    private void iniciar() {
        try {
            centro = ejbCentros.find(1);
        } catch (ConsultarException ex) {
            Logger.getLogger(SeguridadBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String usr;
    private String pwd;
    private String usrLoggeado;
    private Entidades entidad;
    private MenuModel modelo;
    private Grupousuario grupo;
    private Centros centro;

    private Formulario formulario;
    private String claveNueva;
    private String claveRetpeada;
    private String claveAnterior;

    private Entidades cliente;
    private List clientes;
    private String claveBusqueda;

    @EJB
    private EntidadesFacade ejbEntidades;

    @EJB
    private MenusistemaFacade ejbMenus;
    @EJB
    private PerfilFacade ejbPerfiles;
    @EJB
    private GrupousuarioFacade ejbGrupos;
    @EJB
    private CentrosFacade ejbCentros;

    public String login() {
        try {
            if ((usr == null) && (usr.isEmpty())) {
                MensajesErrores.advertencia("Ingrese un usuario válido");
                return null;
            }

            if ((pwd == null) && (pwd.isEmpty())) {
                MensajesErrores.advertencia("Ingrese una clave válida");
                return null;
            }
            Codificador c = new Codificador();
            entidad = ejbEntidades.login(usr, c.getEncoded(pwd, "MD5"));

            if (entidad == null) {
                MensajesErrores.advertencia("Usuario no registrado, o clave invalida");
                return null;
            }
            if (entidad.getActivo() == false) {
                MensajesErrores.advertencia("Usuario no activo");
                entidad = null;
                return null;
            }

            if (entidad.getPwd().equals(c.getEncoded(entidad.getPin(), "MD5"))) {
                formulario = new Formulario();
                formulario.editar();
                return null;
            }
            Map parametros = new HashMap();
            parametros.put(";where", " o.usuario=:usuario");
            parametros.put("usuario", entidad.getPin());
            List<Grupousuario> aux = ejbGrupos.encontarParametros(parametros);
            if (!aux.isEmpty()) {
                grupo = aux.get(0);
            } else {
                MensajesErrores.advertencia("Usuario sin perfil asignado");
                entidad = null;
                return null;
            }

            modelo = getMenuPrincipal();

            return grupo.getModulo().getParametros().trim() + "/PrincipalVista.jsf?faces-redirect=true";

        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
            Logger.getLogger("LOGIN").log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String logout() {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        String ctxPath = ((ServletContext) ctx.getContext()).getContextPath();

        try {
            ((HttpSession) ctx.getSession(false)).invalidate();
            ctx.redirect(ctxPath + "/IngresoVista.jsf");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public MenuModel getMenuPrincipal() {
        if (entidad == null) {
            return null;
        }
        MenuModel retorno = new DefaultMenuModel();

        List<Menusistema> ml;
        try {
            Map parametros = new HashMap();
            parametros.put(";where", " o.modulo=:modulo");
            parametros.put("modulo", grupo.getModulo());
            parametros.put(";orden", " o.texto");
            ml = ejbMenus.encontarParametros(parametros);

            for (Menusistema m : ml) {
                Submenu nuevoSubmenu = new Submenu();
                nuevoSubmenu.setId("sm_" + (m.getId()));
                nuevoSubmenu.setLabel(m.getTexto());
                // traer los perfiles
                parametros = new HashMap();
                parametros.put(";where", " o.menu.idpadre=:menu and o.grupo=:grupo");
                parametros.put(";orden", " o.menu.texto");
                parametros.put("menu", m);
                parametros.put("grupo", grupo.getGrupo());
                List<Perfil> pl = ejbPerfiles.encontarParametros(parametros);
                //nuevoSubmenu.getChildren().clear();
                for (Perfil p : pl) {
                    org.icefaces.ace.component.menuitem.MenuItem nuevo = new org.icefaces.ace.component.menuitem.MenuItem();
                    nuevo.setId(nuevoSubmenu.getId() + "_mmi_" + p.getId());
                    nuevo.setValue(p.getMenu().getTexto());
                    nuevo.setUrl("../" + grupo.getModulo().getParametros().trim() + "/" + p.getMenu().getFormulario().trim() + ".jsf?faces-redirect=true&p=" + p.getId());
                    nuevoSubmenu.getChildren().add(nuevo);
                }

                retorno.addSubmenu(nuevoSubmenu);
            }

        } catch (ConsultarException ex) {
            Logger.getLogger(SeguridadBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return retorno;
    }

    public String cerraSession() {
        FacesContext context = FacesContext.getCurrentInstance();

        ExternalContext externalContext = context.getExternalContext();

        Object session = externalContext.getSession(false);

        HttpSession httpSession = (HttpSession) session;

        httpSession.invalidate();
        return "../IngresoVista.jsf";
    }

    public Perfil traerPerfil(String id) {
        Integer c = Integer.valueOf(id);
        try {
            return ejbPerfiles.find(c);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(SeguridadBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String cambio() {
        // dos password nuva y una anterior
        try {
            if ((claveNueva == null) && (claveNueva.isEmpty())) {
                MensajesErrores.advertencia("Ingrese una clave válida");
                return null;
            }
            if ((claveAnterior == null) && (claveAnterior.isEmpty())) {
                MensajesErrores.advertencia("Ingrese una clave válida");
                return null;
            }
            if ((claveRetpeada == null) && (claveRetpeada.isEmpty())) {
                MensajesErrores.advertencia("Ingrese una clave válida");
                return null;
            }
            Codificador c = new Codificador();
            String cnCodificada = c.getEncoded(claveNueva, "MD5");
            String caCodificada = c.getEncoded(claveAnterior, "MD5");
            String cnrCodificada = c.getEncoded(claveRetpeada, "MD5");
            if (!(caCodificada.equals(entidad.getPwd()))) {
                MensajesErrores.advertencia("Ingrese una clave anterior válida");
                return null;
            }
            if (!(cnCodificada.equals(cnrCodificada))) {
                MensajesErrores.advertencia("Ingrese una clave retipeada igual a la nueva clave");
                return null;
            }

            Map parametros = new HashMap();
            parametros.put(";where", " o.activo = true and o.userid=:userid and o.pwd=:pwd");
            parametros.put("userid", usr);
            parametros.put("pwd", cnCodificada);

            int aux = ejbEntidades.contar(parametros);
            if (aux > 0) {
                MensajesErrores.advertencia("Clave no válida, inténtelo de nuevo.");
                return null;
            }

            entidad.setPwd(cnCodificada);
            ejbEntidades.edit(entidad, null);
        } catch (GrabarException | ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
            Logger.getLogger("LOGIN").log(Level.SEVERE, null, ex);
        }
        MensajesErrores.informacion("Su clave ha sido cambiada con éxito!");
        formulario = new Formulario();
        formulario.cancelar();
        return null;
    }

    public String cambiar() {
        return "../administracion/CambioClaveVista.jsf?faces-redirect=true";
    }

    public void cambiaEntidades(ValueChangeEvent event) {
        cliente = null;
        if (event.getComponent() instanceof SelectInputText) {
            // get the number of displayable records from the component
            SelectInputText autoComplete
                    = (SelectInputText) event.getComponent();
            // get the new value typed by component user.
            String newWord = (String) event.getNewValue();
            // traer la consulta
            Map parametros = new HashMap();
            String where = " o.activo=true and o.rol like :rol  and  (upper(o.apellidos) like :pin or upper(o.pin) like :pin)";
            parametros.put("rol", "%#C%");
            parametros.put("pin", "%" + newWord.toUpperCase() + "%");
            parametros.put(";where", where);
            parametros.put(";orden", " o.apellidos");
            int total = ((SelectInputText) event.getComponent()).getRows();
            // Contadores

            parametros.put(";inicial", 0);
            parametros.put(";final", total);

            clientes = new ArrayList();

            try {

                List<Entidades> aux = ejbEntidades.encontarParametros(parametros);
                for (Entidades p : aux) {
                    SelectItem s = new SelectItem(p, p.toString());
                    clientes.add(s);
                }

            } catch (ConsultarException ex) {
                MensajesErrores.error(ex.getMessage() + ":" + ex.getCause());
                Logger.getLogger("ERROR").log(Level.SEVERE, null, ex);
            }

            if (autoComplete.getSelectedItem() != null) {
                cliente = (Entidades) autoComplete.getSelectedItem().getValue();
            } else {

                Entidades tmp = null;
                for (int i = 0, max = getClientes().size(); i < max; i++) {
                    SelectItem e = (SelectItem) getClientes().get(i);
                    if (e.getLabel().compareToIgnoreCase(newWord) == 0) {
                        tmp = (Entidades) e.getValue();
                    }
                }
                if (tmp != null) {
                    cliente = tmp;
                }
            }

        }
    }

    public String getAdministrador() {
        Map parametros = new HashMap();
        parametros.put(";where", " o.centro=:centro and upper(o.grupo.codigo)='ADMIC'");
        parametros.put("centro", centro);
        try {
            List<Grupousuario> aux = ejbGrupos.encontarParametros(parametros);
            for (Grupousuario gu : aux) {
                parametros = new HashMap();
                parametros.put(";where", " o.activo=true and o.pin=:pin");
                parametros.put("pin", gu.getUsuario());
                List<Entidades> lista = ejbEntidades.encontarParametros(parametros);
                if (!lista.isEmpty()) {
                    return lista.get(0).toString();
                }
            }
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(SeguridadBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    /**
     * @return the usrLoggeado
     */
    public String getUsrLoggeado() {
        return usrLoggeado;
    }

    /**
     * @param usrLoggeado the usrLoggeado to set
     */
    public void setUsrLoggeado(String usrLoggeado) {
        this.usrLoggeado = usrLoggeado;
    }

    /**
     * @return the usr
     */
    public String getUsr() {
        return usr;
    }

    /**
     * @param usr the usr to set
     */
    public void setUsr(String usr) {
        this.usr = usr;
    }

    /**
     * @return the pwd
     */
    public String getPwd() {
        return pwd;
    }

    /**
     * @param pwd the pwd to set
     */
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    /**
     * @return the modelo
     */
    public MenuModel getModelo() {
        return modelo;
    }

    /**
     * @param modelo the modelo to set
     */
    public void setModelo(MenuModel modelo) {
        this.modelo = modelo;
    }

    /**
     * @return the grupo
     */
    public Grupousuario getGrupo() {
        return grupo;
    }

    /**
     * @param grupo the grupo to set
     */
    public void setGrupo(Grupousuario grupo) {
        this.grupo = grupo;
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
     * @return the entidad
     */
    public Entidades getEntidad() {
        return entidad;
    }

    /**
     * @param entidad the entidad to set
     */
    public void setEntidad(Entidades entidad) {
        this.entidad = entidad;
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
     * @return the claveNueva
     */
    public String getClaveNueva() {
        return claveNueva;
    }

    /**
     * @param claveNueva the claveNueva to set
     */
    public void setClaveNueva(String claveNueva) {
        this.claveNueva = claveNueva;
    }

    /**
     * @return the claveRetpeada
     */
    public String getClaveRetpeada() {
        return claveRetpeada;
    }

    /**
     * @param claveRetpeada the claveRetpeada to set
     */
    public void setClaveRetpeada(String claveRetpeada) {
        this.claveRetpeada = claveRetpeada;
    }

    /**
     * @return the claveAnterior
     */
    public String getClaveAnterior() {
        return claveAnterior;
    }

    /**
     * @param claveAnterior the claveAnterior to set
     */
    public void setClaveAnterior(String claveAnterior) {
        this.claveAnterior = claveAnterior;
    }

    /**
     * @return the cliente
     */
    public Entidades getCliente() {
        return cliente;
    }

    /**
     * @param cliente the cliente to set
     */
    public void setCliente(Entidades cliente) {
        this.cliente = cliente;
    }

    /**
     * @return the clientes
     */
    public List getClientes() {
        return clientes;
    }

    /**
     * @param clientes the clientes to set
     */
    public void setClientes(List clientes) {
        this.clientes = clientes;
    }

    /**
     * @return the claveBusqueda
     */
    public String getClaveBusqueda() {
        return claveBusqueda;
    }

    /**
     * @param claveBusqueda the claveBusqueda to set
     */
    public void setClaveBusqueda(String claveBusqueda) {
        this.claveBusqueda = claveBusqueda;
    }

}
