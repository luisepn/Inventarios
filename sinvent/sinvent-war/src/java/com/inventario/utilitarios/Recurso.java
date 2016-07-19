/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.utilitarios;

import com.icesoft.faces.context.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import javax.faces.context.ExternalContext;

/**
 *
 * @author limon
 */
public final class Recurso implements Resource, Serializable {

    private static final long serialVersionUID = 1L;
    private String resourceName;
    private InputStream inputStream;
    private Date lastModified;
    private ExternalContext extContext;
    private byte[] byteArray;

    public Recurso(ExternalContext ec, String resourceName, byte[] byteArray) {
        this.extContext = ec;
        this.resourceName = resourceName;
        this.lastModified = new Date();
        this.byteArray = byteArray;
    }

    @Override
    public InputStream open() throws IOException {
        if (inputStream == null) {
            inputStream = new ByteArrayInputStream(byteArray);
        }
        return inputStream;
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
    public void withOptions(Resource.Options arg0) throws IOException {
    }

    /**
     * @return the resourceName
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * @param resourceName the resourceName to set
     */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * @return the inputStream
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * @param inputStream the inputStream to set
     */
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * @return the lastModified
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * @param lastModified the lastModified to set
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * @return the extContext
     */
    public ExternalContext getExtContext() {
        return extContext;
    }

    /**
     * @param extContext the extContext to set
     */
    public void setExtContext(ExternalContext extContext) {
        this.extContext = extContext;
    }

    /**
     * @return the byteArray
     */
    public byte[] getByteArray() {
        return byteArray;
    }

    /**
     * @param byteArray the byteArray to set
     */
    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

}
