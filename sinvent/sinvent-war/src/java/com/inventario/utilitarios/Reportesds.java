
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.utilitarios;

/**
 *
 * @author luisfernando
 */
import com.icesoft.faces.context.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRException;
import java.util.List;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 *
 * @author paulc
 */
public class Reportesds implements Resource, Serializable {

    private String resourceName;
    private final Date lastModified = null;
    private Map param;
    private String archivo;
    private List ds;

    public Reportesds(Map param, String archivo, List ds, String resourceName) {
        this.param = param;
        this.archivo = archivo;
        this.ds = ds;
        this.resourceName = resourceName;
    }

    @Override
    public InputStream open() throws IOException {
        try {


            return new ByteArrayInputStream(this.iceFacesReporte());

        } catch (Exception e) {
            MensajesErrores.fatal(e.getMessage() + ":" + e.getCause());
            Logger.getLogger("Reportes").log(Level.SEVERE, null, e);
        }

        return null;
    }

    public byte[] iceFacesReporte() throws JRException {

        JasperPrint jasperPrint=new JasperPrint();
        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(ds);
        jasperPrint = JasperFillManager.fillReport(archivo, param, beanCollectionDataSource);

//        jasperPrint.set
        byte[] fichero = JasperExportManager.exportReportToPdf(jasperPrint);
        return fichero;
    }


    @Override
    public String calculateDigest() {
        return resourceName;
    }


    @Override
    public Date lastModified() {
        return lastModified;
    }


    @Override
    public void withOptions(Options arg0) throws IOException {

    }

    /**
     * @return the param
     */
    public Map getParam() {
        return param;
    }

    /**
     * @param param the param to set
     */
    public void setParam(Map param) {
        this.param = param;
    }

    /**
     * @return the archivo
     */
    public String getArchivo() {
        return archivo;
    }

    /**
     * @param archivo the archivo to set
     */
    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }
}
