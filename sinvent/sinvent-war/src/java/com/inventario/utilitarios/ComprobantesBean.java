/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.utilitarios;

import com.icesoft.faces.context.Resource;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

/**
 *
 * @author luisfernando
 */
@ManagedBean(name = "comprobantes")
@ViewScoped
public class ComprobantesBean {

    private Resource recursoPdf;
    private String nombre;

    public ComprobantesBean() {
    }

    public void generar() {
        List<Comprobantes> lista = new LinkedList<>();

        Comprobantes comp = new Comprobantes();
        comp.setEfiscal("2016");
        comp.setBase("1");
        comp.setCodigo("IMP");
        comp.setTipo("IMPT");
        comp.setPorcentaje("100");
        comp.setValor(new Float(300.00));

        lista.add(comp);

        comp = new Comprobantes();
        comp.setEfiscal("2016");
        comp.setBase("1");
        comp.setCodigo("IMP");
        comp.setTipo("IMPT");
        comp.setPorcentaje("100");
        comp.setValor(new Float(10.00));

        lista.add(comp);

        recursoPdf = null;
        Map parametros = new HashMap();

        parametros.put("sres", "Ev Consultoresnoside 1986");
        parametros.put("ruc", "1725351736001");
        parametros.put("direccion", "Centro Histórico");
        parametros.put("fecha", "15-05-2016");
        parametros.put("tipo", "Tipo");
        parametros.put("numero", "0000123");
        parametros.put("total", (float) 310.00);

        Calendar c = Calendar.getInstance();
        recursoPdf = new Reportesds(parametros,
                FacesContext.getCurrentInstance().getExternalContext().getRealPath("Reportes/Comprobante.jasper"),
                lista, "Comprobante" + String.valueOf(c.getTimeInMillis()) + ".pdf");

    }

    /**
     * @return the recursoPdf
     */
    public Resource getRecursoPdf() {
        return recursoPdf;
    }

    /**
     * @param recursoPdf the recursoPdf to set
     */
    public void setRecursoPdf(Resource recursoPdf) {
        this.recursoPdf = recursoPdf;
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}
