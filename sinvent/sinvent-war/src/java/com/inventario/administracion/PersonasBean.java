/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.administracion;

import com.icesoft.faces.component.selectinputtext.SelectInputText;
import com.inventario.entidades.Direcciones;
import com.inventario.entidades.Entidades;
import com.inventario.entidades.Perfil;
import com.inventario.excepciones.ConsultarException;
import com.inventario.excepciones.GrabarException;
import com.inventario.excepciones.InsertarException;
import com.inventario.seguridad.SeguridadBean;
import com.inventario.servicios.DireccionesFacade;
import com.inventario.servicios.EntidadesFacade;
import com.inventario.utilitarios.Codificador;
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
 * @author edwin
 *
 *
 * Roles [0 - Usuarios] [1 - Docentes]
 *
 */
@ManagedBean(name = "personas")
@ViewScoped
public abstract class PersonasBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of MaestrosBean
     */
    @ManagedProperty(value = "#{seguridadBean}")
    protected SeguridadBean seguridadBean;

    protected Formulario formulario = new Formulario();
    protected Formulario formularioExiste = new Formulario();
    protected LazyDataModel<Entidades> entidades;
    protected Entidades entidad;
    protected Entidades existente;
    protected Direcciones direccion;
    protected Entidades entidadSeleccionado;
    @EJB
    protected EntidadesFacade ejbEntidad;
    @EJB
    protected DireccionesFacade ejbDireccion;

    //Autocompletar
    protected List listaUsuarios;
    protected String apellidos;
    private String rol;
    protected Perfil perfil;

    public PersonasBean() {
        entidades = new LazyDataModel<Entidades>() {
            @Override
            public List<Entidades> load(int i, int i1, SortCriteria[] scs, Map<String, String> map) {
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
        String nombreForma = "MenusVista";

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
            return null;
        }
        entidad = new Entidades();
        direccion = new Direcciones();
        formulario.insertar();
        return null;
    }

    public String modificar() {
        if (!perfil.getModificacion()) {
            MensajesErrores.advertencia("No tiene autorización para modificar un registro");
        }
        entidad = (Entidades) entidades.getRowData();
        direccion = entidad.getDireccion();
        if (direccion == null) {
            direccion = new Direcciones();
        }

        formulario.editar();
        return null;
    }

    public String eliminar() {
        if (!perfil.getBorrado()) {
            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
        }

        entidad = (Entidades) entidades.getRowData();
        direccion = entidad.getDireccion();
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
        entidades = new LazyDataModel<Entidades>() {
            @Override
            public List<Entidades> load(int i, int pageSize, SortCriteria[] scs, Map<String, String> map) {
                Map parametros = new HashMap();
                if (scs.length == 0) {
                    parametros.put(";orden", "o.apellidos");
                } else {
                    parametros.put(";orden", "o." + scs[0].getPropertyName()
                            + (scs[0].isAscending() ? " ASC" : " DESC"));
                }
                String where = " o.activo=true and o.rol  like '%" + rol + "%'"; // ( and o.rol  like '%" + rol + "%') 
                for (Map.Entry e : map.entrySet()) {
                    String clave = (String) e.getKey();
                    String valor = (String) e.getValue();

                    where += " and upper(o." + clave + ") like :" + clave;
                    parametros.put(clave, valor.toUpperCase() + "%");
                }

                int total = 0;
                try {
                    parametros.put(";where", where);
                    total = ejbEntidad.contar(parametros);
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
                getEntidades().setRowCount(total);
                try {
                    return ejbEntidad.encontarParametros(parametros);
                } catch (ConsultarException ex) {
                    MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
                    Logger.getLogger("").log(Level.SEVERE, null, ex);
                }
                return null;
            }
        };

        return null;

    }

    // acciones de base de datos
    protected boolean validar() {
        if ((entidad.getPin() == null) || (entidad.getPin().isEmpty())) {
            MensajesErrores.advertencia("CI o RUC es obligatorio");
            return true;
        }
        if ((entidad.getNombres() == null) || (entidad.getNombres().isEmpty())) {
            MensajesErrores.advertencia("Nombres es obligatorio");
            return true;
        }
        if ((entidad.getApellidos() == null) || (entidad.getApellidos().isEmpty())) {
            MensajesErrores.advertencia("Apellidos es obligatorio");
            return true;
        }

        return false;
    }

    public String insertar() {
        if (!perfil.getNuevo()) {
            MensajesErrores.advertencia("No tiene autorización para crear un registro");
            return null;
        }
        if (formularioExiste.isNuevo()) {
            MensajesErrores.advertencia("Registro ya existe, no se puede crear con CI repetido");
            return null;
        }
        if (formularioExiste.isBorrar()) {
            MensajesErrores.advertencia("Registro ya existe, no se puede crear con CI en este nuevo rol");
            return null;
        }
        try {
            Map parametros = new HashMap();
            parametros.put(";where", " o.pin=:pin and o.activo = true");
            parametros.put("pin", entidad.getPin());

            int aux = ejbEntidad.contar(parametros);

            if (aux > 0) {
                MensajesErrores.advertencia("Registro ya existe");
                return null;
            }

            if (validar()) {
                return null;
            }

            if (entidad.getRol() == null) {
                entidad.setRol(rol);
            } else if (!entidad.getRol().contains(rol)) {
                entidad.setRol(entidad.getRol() + rol);
            }

            entidad.setActivo(Boolean.TRUE);

            if (direccion != null) {
                ejbDireccion.create(direccion, seguridadBean.getEntidad().getUserid());
                entidad.setDireccion(direccion);

            }
            Codificador c = new Codificador();
            entidad.setPwd(c.getEncoded(entidad.getPin(), "MD5"));
            ejbEntidad.create(entidad, seguridadBean.getEntidad().getUserid());
        } catch (InsertarException | ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger("").log(Level.SEVERE, null, ex);
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
            entidad.setActivo(Boolean.TRUE);
            if (entidad.getRol() == null) {
                entidad.setRol(rol);
            } else if (!entidad.getRol().contains(rol)) {
                entidad.setRol(entidad.getRol() + rol);
            }
            if (direccion != null) {
                if (direccion.getId() == null) {
                    ejbDireccion.create(direccion, seguridadBean.getEntidad().getUserid());
                } else {
                    ejbDireccion.edit(direccion, seguridadBean.getEntidad().getUserid());
                }
                entidad.setDireccion(direccion);
                ejbEntidad.edit(entidad, seguridadBean.getEntidad().getUserid());

            } else {
                ejbEntidad.edit(entidad, seguridadBean.getEntidad().getUserid());
            }

        } catch (InsertarException | GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger("").log(Level.SEVERE, null, ex);
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
            entidad.setActivo(Boolean.FALSE);
            ejbEntidad.edit(entidad, seguridadBean.getEntidad().getUserid());
//            ejbDireccion.remove(direccion);
        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger("").log(Level.SEVERE, null, ex);
        }

        formulario.cancelar();
        buscar();
        return null;
    }
    // falta el combo

    public Entidades traer(Integer id) throws ConsultarException {
        return ejbEntidad.find(id);
    }

    public Entidades traerCedula(String cedula) {
        if (cedula == null) {
            return null;
        }
        Map parametros = new HashMap();
        parametros.put(";where", "o.pin=:cedula");
        parametros.put("cedula", cedula);
        try {
            List<Entidades> el = ejbEntidad.encontarParametros(parametros);
            if (!((el == null) || (el.isEmpty()))) {
                return el.get(0);
            }
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger("PERSONAS").log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * @return the entidades
     */
    public LazyDataModel<Entidades> getEntidades() {
        return entidades;
    }

    /**
     * @param entidades the entidades to set
     */
    public void setEntidades(LazyDataModel<Entidades> entidades) {
        this.entidades = entidades;
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
     * @return the ejbEntidad
     */
    public EntidadesFacade getEjbEntidad() {
        return ejbEntidad;
    }

    /**
     * @param ejbEntidad the ejbEntidad to set
     */
    public void setEjbEntidad(EntidadesFacade ejbEntidad) {
        this.ejbEntidad = ejbEntidad;
    }

    /**
     * @return the listaUsuarios
     */
    public List getListaUsuarios() {
        return listaUsuarios;
    }

    /**
     * @param listaUsuarios the listaUsuarios to set
     */
    public void setListaUsuarios(List listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    /**
     * @return the apellidos
     */
    public String getApellidos() {
        return apellidos;
    }

    /**
     * @param apellidos the apellidos to set
     */
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    /**
     * @return the entidadSeleccionado
     */
    public Entidades getEntidadSeleccionado() {
        return entidadSeleccionado;
    }

    /**
     * @param entidadSeleccionado the entidadSeleccionado to set
     */
    public void setEntidadSeleccionado(Entidades entidadSeleccionado) {
        this.entidadSeleccionado = entidadSeleccionado;
    }

    public void cambiaCedula(ValueChangeEvent event) {
        // cambia el texto de la cedula
        String nuevaCedula = (String) event.getNewValue();
        // consultar por cedula 
        List<Entidades> aux;
        // traer la consulta
        Map parametros = new HashMap();
        String where = " (o.pin  =:pin) and o.activo=true";
        parametros.put("pin", nuevaCedula);
        parametros.put(";where", where);
        try {
            aux = ejbEntidad.encontarParametros(parametros);
            if ((aux == null) || (aux.isEmpty())) {
                formularioExiste.setBorrar(false);
                formularioExiste.setNuevo(false);
            } else {
                // existe ver si rol permite cambiar
                existente = aux.get(0);
                formularioExiste.insertar();

            }
        } catch (ConsultarException ex) {
            MensajesErrores.error(ex.getMessage() + ":" + ex.getCause());
            Logger.getLogger("ERROR").log(Level.SEVERE, null, ex);
        }

    }

    public void cambiaApellido(ValueChangeEvent event) {
        setEntidadSeleccionado(null);
        if (event.getComponent() instanceof SelectInputText) {
            // get the number of displayable records from the component
            SelectInputText autoComplete
                    = (SelectInputText) event.getComponent();
            // get the new value typed by component user.
            String newWord = (String) event.getNewValue();
            //        getEmpresasBeans().setComercial("");

            List<Entidades> aux = new LinkedList<>();
            // traer la consulta
            Map parametros = new HashMap();
//             String where = " (o.rol  like '%1%')";
            String where = " (o.rol  like '%" + rol + "%') and o.activo=true";
//            parametros.put("tipo", "%1%");
//            parametros.put("tipo", getTipo());
            where += " and  upper(o.apellidos) like :pin";
            parametros.put("pin", newWord.toUpperCase() + "%");
            parametros.put(";where", where);
            parametros.put(";orden", " o.apellidos");
            int total = ((SelectInputText) event.getComponent()).getRows();
            // Contadores

            parametros.put(";inicial", 0);
            parametros.put(";final", total);
            try {
                aux = ejbEntidad.encontarParametros(parametros);
            } catch (ConsultarException ex) {
                MensajesErrores.error(ex.getMessage() + ":" + ex.getCause());
                Logger.getLogger("ERROR").log(Level.SEVERE, null, ex);
            }
            setListaUsuarios(new ArrayList());
            for (Entidades e : aux) {
                String campo = "";

                SelectItem s = new SelectItem(e, e.getApellidos());
                getListaUsuarios().add(s);
            }
            if (autoComplete.getSelectedItem() != null) {
                setEntidadSeleccionado((Entidades) autoComplete.getSelectedItem().getValue());
            } else {

                Entidades tmp = null;
                for (int i = 0, max = getListaUsuarios().size(); i < max; i++) {
                    SelectItem e = (SelectItem) getListaUsuarios().get(i);
                    if (e.getLabel().compareToIgnoreCase(newWord) == 0) {
                        tmp = (Entidades) e.getValue();
                    }

                }
                if (tmp != null) {
                    setEntidadSeleccionado(tmp);
                }
            }

        }
    }

    public void cambiaCedulaEntidad(ValueChangeEvent event) {

        entidad = new Entidades();
        direccion = new Direcciones();
        if (event.getComponent() instanceof SelectInputText) {
            // get the number of displayable records from the component
            SelectInputText autoComplete
                    = (SelectInputText) event.getComponent();
            // get the new value typed by component user.
            String newWord = (String) event.getNewValue();
            List<Entidades> aux = new LinkedList<>();
            //Traer Consulta
            Map parametros = new HashMap();
            String where = "o.pin  like :pinprofesor and o.activo=true ";
            parametros.put("pinprofesor", newWord + "%");
            parametros.put(";where", where);
            parametros.put(";orden", " o.pin");

            // Contadores
            int total = ((SelectInputText) event.getComponent()).getRows();
            parametros.put(";inicial", 0);
            parametros.put(";final", total);
            try {
                aux = ejbEntidad.encontarParametros(parametros);
            } catch (ConsultarException ex) {
                MensajesErrores.error(ex.getMessage() + ":" + ex.getCause());
                Logger.getLogger("ERROR").log(Level.SEVERE, null, ex);
            }

            listaUsuarios = new ArrayList();
            for (Entidades e : aux) {
                SelectItem s = new SelectItem(e, e.getPin());
                listaUsuarios.add(s);
            }

            //setListadoRepresentantes(new ArrayList());
            if (autoComplete.getSelectedItem() != null) {
                entidad = (Entidades) autoComplete.getSelectedItem().getValue();
                if (entidad.getDireccion() != null) {
                    direccion = entidad.getDireccion();
                } else {
                    direccion = new Direcciones();
                }

            } else {

                Entidades tmp = null;
                for (int i = 0, max = listaUsuarios.size(); i < max; i++) {
                    SelectItem e = (SelectItem) listaUsuarios.get(i);
                    if (e.getLabel().compareToIgnoreCase(newWord) == 0) {
                        tmp = (Entidades) e.getValue();
                    }
                    if (tmp != null) {
                        entidad = tmp;
                        if (entidad.getDireccion() != null) {
                            direccion = entidad.getDireccion();
                        } else {
                            direccion = new Direcciones();
                        }
                    } else {
                        entidad = new Entidades();
                        direccion = new Direcciones();

                    }
                }
                if (entidad == null) {
                    entidad = new Entidades();
                    direccion = new Direcciones();
                }
            }

        }
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

    public String aceptarNuevo() {
        entidad = existente;
        formularioExiste.cancelar();
        direccion = entidad.getDireccion();
        if (direccion == null) {
            direccion = new Direcciones();
        }
        entidad.setActivo(Boolean.TRUE);
        formulario.editar();
        return null;
    }

    public String noAceptaNuevo() {
        // se deberia chequear perfil?
//        entidad = existente;
        formularioExiste.setNuevo(false);
        return null;
    }

    /**
     * @return the rol
     */
    public String getRol() {
        return rol;
    }

    /**
     * @param rol the rol to set
     */
    public void setRol(String rol) {
        this.rol = rol;
    }

    /**
     * @return the formularioExiste
     */
    public Formulario getFormularioExiste() {
        return formularioExiste;
    }

    /**
     * @param formularioExiste the formularioExiste to set
     */
    public void setFormularioExiste(Formulario formularioExiste) {
        this.formularioExiste = formularioExiste;
    }

    /**
     * @return the existente
     */
    public Entidades getExistente() {
        return existente;
    }

    /**
     * @param existente the existente to set
     */
    public void setExistente(Entidades existente) {
        this.existente = existente;
    }

    public void cambiaTodos(ValueChangeEvent event) {
        setEntidadSeleccionado(null);
        if (event.getComponent() instanceof SelectInputText) {
            // get the number of displayable records from the component
            SelectInputText autoComplete
                    = (SelectInputText) event.getComponent();
            // get the new value typed by component user.
            String newWord = (String) event.getNewValue();
            //        getEmpresasBeans().setComercial("");

            List<Entidades> aux = new LinkedList<>();
            // traer la consulta
            Map parametros = new HashMap();
//             String where = " (o.rol  like '%1%')";
            String where = " ";
//            parametros.put("tipo", "%1%");
//            parametros.put("tipo", getTipo());
            where += "  upper(o.apellidos) like :pin and o.activo=true";
            parametros.put("pin", newWord.toUpperCase() + "%");
            parametros.put(";where", where);
            parametros.put(";orden", " o.apellidos");
            int total = ((SelectInputText) event.getComponent()).getRows();
            // Contadores

            parametros.put(";inicial", 0);
            parametros.put(";final", total);
            try {
                aux = ejbEntidad.encontarParametros(parametros);
            } catch (ConsultarException ex) {
                MensajesErrores.error(ex.getMessage() + ":" + ex.getCause());
                Logger.getLogger("ERROR").log(Level.SEVERE, null, ex);
            }
            setListaUsuarios(new ArrayList());
            for (Entidades e : aux) {
                String campo = "";

                SelectItem s = new SelectItem(e, e.getApellidos());
                getListaUsuarios().add(s);
            }
            if (autoComplete.getSelectedItem() != null) {
                setEntidadSeleccionado((Entidades) autoComplete.getSelectedItem().getValue());
            } else {

                Entidades tmp = null;
                for (int i = 0, max = getListaUsuarios().size(); i < max; i++) {
                    SelectItem e = (SelectItem) getListaUsuarios().get(i);
                    if (e.getLabel().compareToIgnoreCase(newWord) == 0) {
                        tmp = (Entidades) e.getValue();
                    }

                }
                if (tmp != null) {
                    setEntidadSeleccionado(tmp);
                }
            }

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
