/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventario.servicios;

import com.inventario.entidades.Codigos;
import com.inventario.entidades.Maestros;
import com.inventario.excepciones.BorrarException;
import com.inventario.excepciones.ConsultarException;
import com.inventario.excepciones.GrabarException;
import com.inventario.excepciones.InsertarException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author luisfernando
 */
public abstract class AbstractFacade<T> {

    private Class<T> entityClass;

    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract EntityManager getEntityManager();

//    protected abstract String modificarObjetos(T nuevo);
//
//    protected abstract String insertarObjetos(T nuevo);
//
//    protected abstract String borrarObjetos(T nuevo);
//    
    public void create(T entity, String usuario) throws InsertarException {
        try {

            getEntityManager().persist(entity);

        } catch (Exception e) {
            throw new InsertarException(entity.toString(), e);
        } finally {
            Logger.getLogger(entityClass.getName()).log(Level.INFO, "Entidad Creada:{0}", entity.toString());
        }
    }

    public void edit(T entity, String usuario) throws GrabarException {
        try {
            getEntityManager().merge(entity);
        } catch (Exception e) {
            throw new GrabarException(entity.toString(), e);
        } finally {
            Logger.getLogger(entityClass.getName()).log(Level.INFO, "Entidad modificada:{0}", entity.toString());
        }
    }

    public void remove(T entity, String usuario) throws BorrarException {
        try {
            getEntityManager().remove(getEntityManager().merge(entity));
            //sendJMSMessageToColadeNotificacion(entity);
        } catch (Exception e) {
            throw new BorrarException(entity.toString(), e);
        } finally {
            Logger.getLogger(entityClass.getName()).log(Level.INFO, "Entidad borrada:{0}", entity.toString());
        }
    }

    public T find(Object id) throws ConsultarException {
        try {
            return getEntityManager().find(entityClass, id);
        } catch (Exception e) {
            throw new ConsultarException(id.toString(), e);
        }
    }

    public List<T> findAll() throws ConsultarException {
        try {
            javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
            cq.select(cq.from(entityClass));
            return getEntityManager().createQuery(cq).getResultList();
        } catch (Exception e) {
            throw new ConsultarException(entityClass.toString(), e);
        }
    }

    public List<T> findRange(int[] range) throws ConsultarException {
        try {
            javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
            cq.select(cq.from(entityClass));
            javax.persistence.Query q = getEntityManager().createQuery(cq);
            q.setMaxResults(range[1] - range[0]);
            q.setFirstResult(range[0]);
            return q.getResultList();
        } catch (Exception e) {
            throw new ConsultarException(entityClass.toString(), e);
        }
    }

    public int count() throws ConsultarException {
        try {
            javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
            javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
            cq.select(getEntityManager().getCriteriaBuilder().count(rt));
            javax.persistence.Query q = getEntityManager().createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } catch (Exception e) {
            throw new ConsultarException(entityClass.toString(), e);
        }
    }

    /**
     * <p>
     * Función genérica para toda consulta: Necesita un paràmetro cuyo KEY sea
     * igual a where con la cadena de where</p>
     * <p>
     * Necesita un parámetro ;orden on la cadena order by</p>
     *
     * <p>
     * Necesita un paràmetro llamado ;inicial para rango inicial Necesita un
     * parámetro llamado ;final indicar el rango final</p>
     *
     * @param parametros
     * @return - lista de objetos
     * @throws com.medical.excepciones.ConsultarException
     * @Param Map de parametos
     */
    public List<T> encontarParametros(Map parametros) throws ConsultarException {
        try {
            Iterator it = parametros.entrySet().iterator();
            String where = "";
            String orden = "";
            boolean all = false;
            int maxResults = -1;
            int firstResult = -1;
            String tabla = entityClass.getSimpleName();
            Map par = new HashMap();
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry) it.next();
                String clave = (String) e.getKey();
                if (clave.contains(";where")) {
                    where = (String) e.getValue();
                } else if (clave.contains(";inicial")) {
                    firstResult = (Integer) e.getValue();
                    all = true;
                } else if (clave.contains(";orden")) {
                    orden = " order by " + (String) e.getValue();

                } else if (clave.contains(";final")) {
                    all = true;
                    maxResults = (Integer) e.getValue();
                } else {
                    par.put(clave, e.getValue());
                }

            }
            if (!where.isEmpty()) {
                where = " where " + where;
            }

            String sql = "Select object(o) from " + tabla + " as o " + where + orden;
            javax.persistence.Query q = getEntityManager().createQuery(sql);

            it = par.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry) it.next();
                String clave = (String) e.getKey();

                q.setParameter(clave, e.getValue());
            }
            if (all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } catch (Exception e) {
            throw new ConsultarException(entityClass.toString(), e);
        }
    }

    public int contar(Map parametros) throws ConsultarException {
        try {
            Iterator it = parametros.entrySet().iterator();
            String where = "";
            String orden = "";
            boolean all = false;
            int maxResults = -1;
            int firstResult = -1;
            String tabla = entityClass.getSimpleName();
            Map par = new HashMap();
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry) it.next();
                String clave = (String) e.getKey();
                if (clave.contains("where")) {
                    where = (String) e.getValue();
                } else if (clave.contains(";inicial")) {
                    firstResult = (Integer) e.getValue();
                    all = true;
                } else if (clave.contains(";orden")) {
                    orden = " order by " + (String) e.getValue();

                } else if (clave.contains(";final")) {
                    all = true;
                    maxResults = (Integer) e.getValue();
                } else {
                    par.put(clave, e.getValue());
                }

            }
            if (!where.isEmpty()) {
                where = " where " + where;
            }

            String sql = "Select count(o) from " + tabla + " as o " + where;
            javax.persistence.Query q = getEntityManager().createQuery(sql);

            it = par.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry) it.next();
                String clave = (String) e.getKey();

                q.setParameter(clave, e.getValue());
            }

            return ((Long) q.getSingleResult()).intValue();
        } catch (Exception e) {
            throw new ConsultarException(entityClass.toString(), e);
        }
    }

    public List<Object[]> sumar(Map parametros) throws ConsultarException {

        List<Object[]> retorno = null;
        Iterator it = parametros.entrySet().iterator();
        String where = "";
        String grupo = "";
        String campo = "";
        String orden = "";
        String suma = "";
        boolean all = false;
        int maxResults = -1;
        int firstResult = -1;
        String tabla = entityClass.getSimpleName();
        Map par = new HashMap();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            String clave = (String) e.getKey();
            if (clave.contains(";where")) {
                where = (String) e.getValue();
                
            } else if (clave.contains(";inicial")) {
                firstResult = (Integer) e.getValue();
                all = true;
            } else if (clave.contains(";campo")) {
                grupo = " group by " + (String) e.getValue();
                campo = (String) e.getValue();
            } else if (clave.contains(";suma")) {
                suma = (String) e.getValue();

            } 
             else if (clave.contains(";orden")) {
                    orden = " order by " + (String) e.getValue();

                }else if (clave.contains(";final")) {
                all = true;
                maxResults = (Integer) e.getValue();
            } else {
                par.put(clave, e.getValue());
            }

        }
        if (!where.isEmpty()) {
            where = " where " + where;
        }

        String sql = "Select " + campo + "," + suma + " from " + tabla + " as o " + where + grupo+ orden;
        javax.persistence.Query q = getEntityManager().createQuery(sql);
//        if (all) {
//            q.setMaxResults(maxResults);
//            q.setFirstResult(firstResult);
//        }
        it = par.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            String clave = (String) e.getKey();

            q.setParameter(clave, e.getValue());
        }
        retorno = q.getResultList();

        return retorno;
    }
    
      public BigDecimal sumarCampo(Map parametros) throws ConsultarException {
        try {
            Iterator it = parametros.entrySet().iterator();
            String where = "";
            String orden = "";
            String campo="o.valor";
            boolean all = false;
            int maxResults = -1;
            int firstResult = -1;
            String tabla = entityClass.getSimpleName();
            Map par = new HashMap();
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry) it.next();
                String clave = (String) e.getKey();
                if (clave.contains("where")) {
                    where = (String) e.getValue();
                } else if (clave.contains(";inicial")) {
                    firstResult = (Integer) e.getValue();
                    all = true;
                } else if (clave.contains(";orden")) {
                    orden = " order by " + (String) e.getValue();

                } else if (clave.contains(";final")) {
                    all = true;
                    maxResults = (Integer) e.getValue();
                }   else if (clave.contains(";campo")) {
                    campo = (String) e.getValue();
                } else {
                    par.put(clave, e.getValue());
                }

            }
            if (!where.isEmpty()) {
                where = " where " + where;
            }

            String sql = "Select sum("+campo +") from " + tabla + " as o " + where;
            Query q = getEntityManager().createQuery(sql);

            it = par.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry) it.next();
                String clave = (String) e.getKey();

                q.setParameter(clave, e.getValue());
            }
            BigDecimal r=(BigDecimal) q.getSingleResult();
            return r==null?new BigDecimal(BigInteger.ZERO):r;
        } catch (Exception e) {
            throw new ConsultarException(entityClass.toString(), e);
        }
    }
    

    /**
     * <p>
     * Función genérica para toda consulta: Necesita un parámetro cuyo KEY sea
     * igual a al nombre de una sentencia preparada</p>
     * <p>
     * Necesita un parámetro ;orden on la cadena order by</p>
     *
     * <p>
     * Necesita un parámetro llamado ;inicial para rango inicial Necesita un
     * parámetro llamado ;final indicar el rango final</p>
     *
     * @param parametros
     * @return - lista de objetos
     * @throws com.medical.excepciones.ConsultarException
     * @Param Map de parametos
     */
    public List<T> encontarSqlArmado(Map parametros) throws ConsultarException {
        try {
            Iterator it = parametros.entrySet().iterator();
            String consulta = "";
            String orden = "";
            boolean all = false;
            int maxResults = -1;
            int firstResult = -1;
            String tabla = entityClass.getSimpleName();
            Map par = new HashMap();
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry) it.next();
                String clave = (String) e.getKey();
                if (clave.contains(";consulta")) {
                    consulta = (String) e.getValue();
                } else if (clave.contains(";inicial")) {
                    firstResult = (Integer) e.getValue();
                    all = true;
                } else if (clave.contains(";orden")) {
                    orden = " order by " + (String) e.getValue();

                } else if (clave.contains(";final")) {
                    all = true;
                    maxResults = (Integer) e.getValue();
                } else {
                    par.put(clave, e.getValue());
                }

            }

            javax.persistence.Query q = getEntityManager().createNamedQuery(consulta);

            it = par.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry) it.next();
                String clave = (String) e.getKey();

                q.setParameter(clave, e.getValue());
            }
            if (all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } catch (Exception e) {
            throw new ConsultarException(entityClass.toString(), e);
        }
    }

    public Maestros traerMaestroCodigo(String codigo,
            String modulo) throws ConsultarException {
        Query q = getEntityManager().createQuery("Select object(o) from Maestros as o"
                + " where o.codigo=:maestroParametro and o.modulo=:modulo");
        q.setParameter("maestroParametro", codigo);
        q.setParameter("modulo", modulo);
        List<Maestros> lista = q.getResultList();
        if ((lista == null) || (lista.isEmpty())) {
            return null;
        }
        return lista.get(0);
    }

    public Codigos traerCodigo(String maestro, String codigo, String modulo) throws ConsultarException {
        String sql = "Select object(o) from Codigos as o"
                + " where o.maestro.codigo=:maestroParametro and "
                + " o.codigo=:codigo " + (modulo == null ? "" : " and o.maestro.modulo=:modulo ");
        Query q = getEntityManager().createQuery(sql);
        q.setParameter("maestroParametro", maestro);
        if (modulo != null) {
            q.setParameter("modulo", modulo);
        }
        q.setParameter("codigo", codigo);
        List<Codigos> lista = q.getResultList();
        if ((lista == null) || (lista.isEmpty())) {
            return null;
        }
        return lista.get(0);
    }

    public List<Codigos> traerCodigos(String maestro, String modulo) throws ConsultarException {
        String sql = "Select object(o) from Codigos as o"
                + " where o.maestro.codigo=:maestroParametro "
                + " " + (modulo == null ? "" : " and o.maestro.modulo=:modulo ");
        Query q = getEntityManager().createQuery(sql);
        q.setParameter("maestroParametro", maestro);
        if (modulo != null) {
            q.setParameter("modulo", modulo);
        }
        return q.getResultList();
    }
}
