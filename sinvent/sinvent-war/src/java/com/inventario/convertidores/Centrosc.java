/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.convertidores;

import com.inventario.administracion.CentrosBean;
import com.inventario.entidades.Centros;
import com.inventario.excepciones.ConsultarException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author luisfernando
 */
@FacesConverter(forClass = Centros.class)
public class Centrosc implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Centros codigo = null;
        try {
            CentrosBean bean = (CentrosBean) context.getELContext().getELResolver().getValue(context.getELContext(), null, "centros");
            Integer id = Integer.parseInt(value);
            codigo = bean.traer(id);
        } catch (ConsultarException ex) {
            Logger.getLogger(Centrosc.class.getName()).log(Level.SEVERE, null, ex);
        }
        return codigo;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        Centros codigo = (Centros) value;
        if (codigo.getId() == null) {
            return "0";
        }
        return codigo.getId().toString();
    }

}
