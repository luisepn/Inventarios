/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.convertidores;




import com.inventario.entidades.Entidades;
import com.inventario.excepciones.ConsultarException;
import com.inventario.seguridad.UsuariosSistemaBean;
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
@FacesConverter(forClass = Entidades.class)
public class Entidadesc implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Entidades codigo = null;
        
        
        
        try {
            if (value == null) {
                return null;
            }
            UsuariosSistemaBean bean = (UsuariosSistemaBean)
                    context.getELContext().getELResolver().
                    getValue(context.getELContext(), null, "usuariosSistema");
            Integer id = Integer.parseInt(value);
            codigo= bean.traer(id);
        } catch (ConsultarException ex) {
            Logger.getLogger(Entidadesc.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return codigo;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        Entidades codigo=(Entidades) value;
        if (codigo.getId()==null)
                return "0";
        return ((Entidades) value).getId().toString();
    }
}