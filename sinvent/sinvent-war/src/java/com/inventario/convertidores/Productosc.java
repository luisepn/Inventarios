/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.convertidores;

import com.inventario.entidades.Productos;
import com.inventario.excepciones.ConsultarException;
import com.inventario.sistema.ProductosBean;
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
@FacesConverter(forClass = Productos.class)
public class Productosc implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Productos codigo = null;
        try {
            ProductosBean bean = (ProductosBean) context.getELContext().getELResolver().getValue(context.getELContext(), null, "productos");
            Integer id = Integer.parseInt(value);
            codigo = bean.traer(id);
        } catch (ConsultarException ex) {
            Logger.getLogger(Productosc.class.getName()).log(Level.SEVERE, null, ex);
        }
        return codigo;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        Productos codigo = (Productos) value;
        if (codigo.getId() == null) {
            return "0";
        }
        return codigo.getId().toString();
    }

}
