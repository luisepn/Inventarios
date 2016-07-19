/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.servicios;

import com.inventario.entidades.RegistroVenta;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author luis
 */
@Stateless
public class RegistroVentaFacade extends AbstractFacade<RegistroVenta> {
    @PersistenceContext(unitName = "Inventarios-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RegistroVentaFacade() {
        super(RegistroVenta.class);
    }
    
}
