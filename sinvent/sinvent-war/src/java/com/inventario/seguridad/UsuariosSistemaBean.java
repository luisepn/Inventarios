/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.seguridad;

import com.inventario.administracion.PersonasBean;
import com.inventario.entidades.Entidades;
import com.inventario.excepciones.ConsultarException;
import com.inventario.excepciones.GrabarException;
import com.inventario.utilitarios.Codificador;
import com.inventario.utilitarios.Combos;
import com.inventario.utilitarios.Formulario;
import com.inventario.utilitarios.MensajesErrores;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

/**
 *
 * @author edwin
 */
@ManagedBean(name = "usuariosSistema")
@ViewScoped
public class UsuariosSistemaBean extends PersonasBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Formulario formularioClave = new Formulario();

    public UsuariosSistemaBean() {
        super.setRol("#U"); //Usuario del sistema
    }

    @Override
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
        if ((entidad.getEmail() == null) || (entidad.getEmail().isEmpty())) {
            MensajesErrores.advertencia("email es obligatorio");
            return true;
        }
        if ((entidad.getUserid() == null) || (entidad.getUserid().isEmpty())) {
            MensajesErrores.advertencia("User ID es obligatorio");
            return true;
        }

        Map parametros = new HashMap();
        parametros.put(";where", " o.activo = true and o.userid=:userid");
        parametros.put("userid", entidad.getUserid());

        try {
            List<Entidades> lista = ejbEntidad.encontarParametros(parametros);
            if (!lista.isEmpty()) {
                if (formulario.isNuevo()) {
                    MensajesErrores.advertencia("El User ID digitado ya se encuentra en uso.");
                    return true;
                }

            }
        } catch (ConsultarException ex) {
            Logger.getLogger(PersonasBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public String modificarClave() {
        entidad = (Entidades) entidades.getRowData();
        formularioClave.insertar();
        return null;
    }

    public String grabarClave() {
        try {
            Codificador c = new Codificador();
            String clave = c.getEncoded(entidad.getPwd(), "MD5");
            entidad.setPwd(clave);
            ejbEntidad.edit(entidad, seguridadBean.getEntidad().getUserid());
        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
            Logger.getLogger("CAMBIO CLAVE").log(Level.SEVERE, null, ex);
        }
        MensajesErrores.informacion("Clave ha sido cambiada con éxito!");
        formularioClave.cancelar();
        return null;
    }

    public SelectItem[] getComboUsuarios() {
        Map parametros = new HashMap();
        parametros.put(";where", " o.activo=true and o.rol like '%#U%'");
        parametros.put(";orden", " o.apellidos");

        try {
            return Combos.SelectItems(ejbEntidad.encontarParametros(parametros), true);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(PersonasBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * @return the formularioClave
     */
    public Formulario getFormularioClave() {
        return formularioClave;
    }

    /**
     * @param formularioClave the formularioClave to set
     */
    public void setFormularioClave(Formulario formularioClave) {
        this.formularioClave = formularioClave;
    }

}
