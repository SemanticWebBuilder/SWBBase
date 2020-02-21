/*
 * SemanticWebBuilder es una plataforma para el desarrollo de portales y aplicaciones de integración,
 * colaboración y conocimiento, que gracias al uso de tecnología semántica puede generar contextos de
 * información alrededor de algún tema de interés o bien integrar información y aplicaciones de diferentes
 * fuentes, donde a la información se le asigna un significado, de forma que pueda ser interpretada y
 * procesada por personas y/o sistemas, es una creación original del Fondo de Información y Documentación
 * para la Industria INFOTEC, cuyo registro se encuentra actualmente en trámite.
 *
 * INFOTEC pone a su disposición la herramienta SemanticWebBuilder a través de su licenciamiento abierto al público ('open source'),
 * en virtud del cual, usted podrá usarlo en las mismas condiciones con que INFOTEC lo ha diseñado y puesto a su disposición;
 * aprender de él; distribuirlo a terceros; acceder a su código fuente y modificarlo, y combinarlo o enlazarlo con otro software,
 * todo ello de conformidad con los términos y condiciones de la LICENCIA ABIERTA AL PÚBLICO que otorga INFOTEC para la utilización
 * del SemanticWebBuilder 4.0.
 *
 * INFOTEC no otorga garantía sobre SemanticWebBuilder, de ninguna especie y naturaleza, ni implícita ni explícita,
 * siendo usted completamente responsable de la utilización que le dé y asumiendo la totalidad de los riesgos que puedan derivar
 * de la misma.
 *
 * Si usted tiene cualquier duda o comentario sobre SemanticWebBuilder, INFOTEC pone a su disposición la siguiente
 * dirección electrónica:
 *  http://www.semanticwebbuilder.org.mx
 */
package org.semanticwb.base.db;

import org.semanticwb.Logger;
import org.semanticwb.SWBUtils;
import org.semanticwb.base.util.SWBProperties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * Class to manage several connection pools defined in db.properties configuration file.
 *
 * @author  Javier Solis Gonzalez (jsolis@infotec.com.mx)
 */
public class DBConnectionManager {
    private static Logger log = SWBUtils.getLogger(DBConnectionManager.class);
    private ArrayList<Driver> drivers = new ArrayList<>();
    private Hashtable<String, Object> pools = new Hashtable<>();
    private boolean isJNDI;
    private String jndiPattern;
    private Context initCtx;
    private PoolConnectionTimeLock timeLock = new PoolConnectionTimeLock();

    /**
     * Creates a new {@link DBConnectionManager}.
     */
    public DBConnectionManager() {
        log.event("Initializing DBConnectionManager...");
        init();
    }

    /**
     * Gets the total number of free connections available in all managed pools.
     * @return free connection count on all pools.
     */
    public int getNumConnections() {
        int cl = 0;
        if (!isJNDI) {
            Enumeration allPools = pools.elements();
            while (allPools.hasMoreElements()) {
                DBConnectionPool pool = (DBConnectionPool) allPools.nextElement();
                cl += pool.freeConnections.size();
            }
        }
        return cl;
    }

    /**
     * Gets total count of connections of a named Pool.
     * @param name Pool name.
     * @return number of connections.
     */
    public int getConnections(String name) {
        int cl = 0;
        if (!isJNDI) {
            DBConnectionPool pool = (DBConnectionPool) pools.get(name);
            if (pool != null) {
                cl = pool.checkedOut;
            }
        }
        return cl;
    }

    /**
     * Gets count of free connections on a named Pool.
     * @param name pool name.
     * @return free connections count.
     */
    public int getFreeConnections(String name) {
        int cl = 0;
        if (!isJNDI) {
            DBConnectionPool pool = (DBConnectionPool) pools.get(name);
            if (pool != null) {
                cl = pool.freeConnections.size();
            }
        }
        return cl;
    }

    /**
     * Closes a connection on a named Pool.
     * @param name pool name.
     * @param con {@link Connection} object to close.
     */
    public void freeConnection(String name, Connection con) {
        if (!isJNDI) {
            DBConnectionPool pool = (DBConnectionPool) pools.get(name);
            if (pool != null) {
                pool.freeConnection(con);
            }
        } else {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                log.error("Error closing JNDI Pool Connection...", ex);
            }
        }
    }

    /**
     * Gets a {@link Connection} object related with no Pool from a named Pool.
     * @param name pool name.
     * @return new connection or null.
     */
    public Connection getNoPoolConnection(String name) {
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);
        if (pool != null) {
            return pool.newNoPoolConnection();
        }
        return null;
    }

    /**
     * Gets an {@link AutoConnection} object related with no Pool from a named Pool.
     * @param name pool name.
     * @return new connection or null.
     */
    public Connection getAutoConnection(String name) {
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);
        if (pool != null) {
            return pool.newAutoConnection();
        }
        return null;
    }

    /**
     * Gets an open connection or creates a new one without description.
     * @param name Pool name.
     * @return new connection or null.
     */
    public Connection getConnection(String name) {
        return getConnection(name, null);
    }

    /**
     * Gets an open connection or creates a new one with given description.
     * 
     * @param name pool name.
     * @param description connection description.
     * @return new connection or null..
     */
    public Connection getConnection(String name, String description) {
        Connection ret = null;
        if (!isJNDI) {
            DBConnectionPool pool = (DBConnectionPool) pools.get(name);
            if (pool != null) {
                PoolConnection con = (PoolConnection) pool.getConnection();
                ret = con;
            }
        } else {
            DataSource ds = (DataSource) pools.get(name);
            if (ds == null) {
                try {
                    ds = (DataSource) initCtx.lookup(jndiPattern + name);
                    pools.put(name, ds);
                    log.info("Initialized JNDI Connection Pool " + name);
                } catch (Exception ex) {
                    log.error("Error to get DataSource of Context...", ex);
                }
            } else {
                try {
                    ret = ds.getConnection();
                } catch (SQLException ex) {
                    log.error("Error to get JNDI Pool Connection...", ex);
                }
            }
        }
        return ret;
    }

    /**
     * Gets an open connection or creates a new one. It will wait a maximum of <code>time</code> milliseconds
     * while attempting to connect to a database.
     * 
     * @param name pool name.
     * @param time timeout in milliseconds.
     * @return connection or null.
     */
    public Connection getConnection(String name, long time) {
        Connection ret = null;
        if (!isJNDI) {
            DBConnectionPool pool = (DBConnectionPool) pools.get(name);
            if (pool != null) {
                ret = pool.getConnection(time);
            }
        } else {
            DataSource ds = (DataSource) pools.get(name);
            if (ds == null) {
                try {
                    ds = (DataSource) initCtx.lookup(jndiPattern + name);
                    pools.put(name, ds);
                    log.info("Initialized JNDI Pool [" + name + "]");
                } catch (Exception ex) {
                    log.error("Error to get DataSource of Context...", ex);
                }
            } else {
                try {
                    ds.setLoginTimeout((int) (time / 1000));
                    ret = ds.getConnection();
                } catch (SQLException ex) {
                    log.error("Error to get JNDI Pool Connection...", ex);
                }
            }
        }
        return ret;
    }

    /**
     * Closes all connections.
     */
    public void closeAllConnection() {
        Enumeration allPools = pools.elements();
        if (!isJNDI) {
            while (allPools.hasMoreElements()) {
                DBConnectionPool pool = (DBConnectionPool) allPools.nextElement();
                pool.release();
            }
        }
    }

    /**
     * Creates {@link DBConnectionPool} instances with parameters from a {@link Properties} object.
     * Properties must match values defined in db.properties file.
     * <PRE>
     * &lt;poolname&gt;.url         JDBC URL connection String.
     * &lt;poolname&gt;.user        Database user (optional)
     * &lt;poolname&gt;.password    Database password (required if user is defined)
     * &lt;poolname&gt;.maxconn     Maximum number of connections managed by the pool (optional)
     * </PRE>
     * 
     * @param props properties object.
     */
    private void createPools(Properties props) {
        Enumeration propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();
            if (name.endsWith(".url")) {
                String poolName = name.substring(0, name.lastIndexOf('.'));
                String url = props.getProperty(poolName + ".url");
                if (url == null) {
                    log.error("No URL specified for " + poolName);
                    continue;
                }
                String user = props.getProperty(poolName + ".user");
                String password = props.getProperty(poolName + ".password");
                String maxconn = props.getProperty(poolName + ".maxconn", "0");
                String sidleTime = props.getProperty(poolName + ".idle_time", "0");
                if (user != null) {
                    user = user.trim();
                }
                if (password != null) {
                    password = password.trim();
                }

                int max = 0;
                try {
                    max = Integer.parseInt(maxconn.trim());
                } catch (NumberFormatException e) {
                    log.warn("Invalid maxconn value " + maxconn + " for " + poolName);
                }

                long idleTime = 0;
                try {
                    idleTime = Long.parseLong(sidleTime.trim());
                } catch (NumberFormatException e) {
                    log.warn("Invalid idle_time value " + sidleTime + " for " + poolName);
                }

                DBConnectionPool pool =
                        new DBConnectionPool(this, poolName, url, user, password, max, idleTime);
                pools.put(poolName, pool);
                log.info("Initialized Connection Pool [" + poolName + "]");
            }
        }
    }

    /**
     * Initializes {@link DBConnectionManager} with parameters from db.properties file.
     */
    private void init() {
        //TODO: Desacoplar dependencia con archivo db.properties
        InputStream is = getClass().getResourceAsStream("/db.properties");
        Properties dbProps = new SWBProperties();
        try {
            if (is != null) {
                dbProps.load(is);
            } else {
                throw new FileNotFoundException();
            }
            is.close();
        } catch (Exception e) {
            log.error("Can't read the properties file. Make sure db.properties is in the CLASSPATH", e);
            return;
        }

        String jndi = dbProps.getProperty("jndi_pool", "false");
        if (jndi.equals("true")) {
            log.info("JDNI Pool found...");
            isJNDI = true;
            jndiPattern = dbProps.getProperty("jndi_patern", "java:comp/env/jdbc/");
            try {
                initCtx = new InitialContext();
            } catch (javax.naming.NamingException ex) {
                log.error("Error to initialize JNDI Context", ex);
            }
        }
        if (!isJNDI) {
            loadDrivers(dbProps);
            createPools(dbProps);
        }
    }

    /**
     * Loads and registers shared JDBC drivers with parameters from a {@link Properties} object.
     * @param props properties object.
     */
    private void loadDrivers(Properties props) {
        String driverClasses = props.getProperty("drivers");
        StringTokenizer st = new StringTokenizer(driverClasses);
        while (st.hasMoreElements()) {
            String driverClassName = st.nextToken().trim();
            try {
                Driver driver = (Driver) Class.forName(driverClassName).newInstance();
                DriverManager.registerDriver(driver);
                drivers.add(driver);
                log.info("Registered JDBC driver " + driverClassName);
            } catch (Exception e) {
                log.error("Can't register JDBC driver: " + driverClassName + ", Exception: " + e);
            }
        }
    }

    /** Gets the time lock value.
     * @return Value of property timeLock.
     */
    public PoolConnectionTimeLock getTimeLock() {
        return timeLock;
    }

    /** Gets the Pool objects.
     * @return Hashtable of Pool objects.
     */
    public Hashtable getPools() {
        Hashtable map = new Hashtable();
        Enumeration en = pools.keys();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            Object obj = pools.get(key);
            if (obj instanceof DBConnectionPool) {
                map.put(key, obj);
            }
        }
        return map;
    }

    /**
     * Gets the total count of requests made to all Pools.
     * @return count of requests on all pools.
     */
    public long getHits() {
        long hits = 0;
        Enumeration en = pools.elements();
        while (en.hasMoreElements()) {
            Object obj = en.nextElement();
            if (obj instanceof DBConnectionPool) {
                hits += ((DBConnectionPool) obj).getHits();
            }
        }
        return hits;
    }
}
