/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.convertidores;

import com.inventario.administracion.MaestrosBean;
import com.inventario.entidades.Maestros;
import com.inventario.excepciones.ConsultarException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author edwin
 */
@FacesConverter(forClass = Maestros.class)
public class Maestrosc implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Maestros codigo = null;
        try {
            if (value == null) {
                return null;
            }
            MaestrosBean bean = (MaestrosBean) context.getELContext().getELResolver().
                    getValue(context.getELContext(), null, "codigosMaestros");
            Integer id = Integer.parseInt(value);
            codigo = bean.traer(id);
        } catch (ConsultarException ex) {
            Logger.getLogger(Maestrosc.class.getName()).log(Level.SEVERE, null, ex);
        }
        return codigo;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        Maestros codigo = (Maestros) value;
        if (codigo.getId() == null) {
            return "0";
        }
        return ((Maestros) value).getId().toString();
    }
}
