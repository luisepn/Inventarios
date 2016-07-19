/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.convertidores;




import com.inventario.administracion.CodigosBean;
import com.inventario.entidades.Codigos;
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
@FacesConverter(forClass = Codigos.class)
public class Codigosc implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Codigos codigo = null;
        try {
            if (value == null) {
                return null;
            }
            CodigosBean bean = (CodigosBean)
                    context.getELContext().getELResolver().
                    getValue(context.getELContext(), null, "tablaDeCodigos");
            Integer id = Integer.parseInt(value);
            codigo= bean.traer(id);
        } catch (ConsultarException ex) {
            Logger.getLogger(Codigosc.class.getName()).log(Level.SEVERE, null, ex);
        }
        return codigo;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        Codigos codigo=(Codigos) value;
        if (codigo.getId()==null)
                return "0";
        return ((Codigos) value).getId().toString();
    }
}