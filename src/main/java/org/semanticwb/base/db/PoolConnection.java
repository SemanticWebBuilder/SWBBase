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

import java.io.PrintStream;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.semanticwb.Logger;
import org.semanticwb.SWBUtils;

/**
 * Una conexión (sesión) con una base de datos. Sentencias SQL son ejecutadas y los resultados se
 * obtienen dentro del contexto de la conexión.
 *
 * Un objeto PoolConnection con una base de datos puede proporcionar la información que describe sus
 * tablas, soporta la gramática SQL, los procedimientos almacenados, las capacidades de esta conexión,
 * etc. Esta información se obtiene con el método getMetaData().}
 *
 * @author  Javier Solis Gonzalez (jsolis@infotec.com.mx) 
 *
 * @see java.sql.Connection
 */
public class PoolConnection implements java.sql.Connection {

    /** The log. */
    private static Logger log = SWBUtils.getLogger(PoolConnection.class);
    
    /** The con. */
    private java.sql.Connection con = null;
    
    /** The vec. */
    private ArrayList<Object> vec = new ArrayList<>();
    
    /** The pool. */
    private DBConnectionPool pool;
    
    /** The isclosed. */
    private boolean isclosed = false;
    
    /** The description. */
    private String description = "";
    
    /** The id. */
    private long id = 0;
    
    /** The idleTime. */
    private long idleTime = 0;
    
    /** The destroy. */
    private boolean destroy = false;
    
    /** The isdestroyed. */
    private volatile boolean isdestroyed = false;
    
    /** The stack. */
    private StackTraceElement []stack = null;
    private String threadName = null;
    
    public PoolConnection(Connection con)
    {
        this(con,null);
    }
    

    /**
     * Instantiates a new pool connection.
     * 
     * @param con the con
     * @param pool the pool
     */
    public PoolConnection(Connection con, DBConnectionPool pool)
    {
    		idleTime = System.currentTimeMillis();
        this.con = con;
        this.pool = pool;
        if(pool!=null)
        {
            pool.checkedOut++;
            log.trace("PoolConnection(" + getId() + "," + pool.getName() + "):" + pool.checkedOut);
        }
        init();
    }

    /**
     * Inits the.
     */
    public void init()
    {
        isclosed = false;
        description = "";
        id = 0;
    }

    /** Getter for property id.
     * @return Value of property id.
     *
     */
    public long getId()
    {
        return id;
    }

    /** Setter for property id.
     * @param id New value of property id.
     *
     */
    public void setId(long id)
    {
        threadName = Thread.currentThread().getName();
        stack = Thread.currentThread().getStackTrace();
        this.id = id;
    }

    /**
     * Gets the stack trace elements.
     * 
     * @return the stack trace elements
     */
    public StackTraceElement[] getStackTraceElements()
    {
        return stack;
    }

    /**
     * Prints the track trace.
     * 
     * @param out the out
     */
    public void printTrackTrace(PrintStream out)
    {
        for (int x = 0; x < stack.length; x++)
        {
            out.println(stack[x]);
        }
    }

    /**
     * Gets the native connection.
     * 
     * @return the native connection
     * @return
     */
    public java.sql.Connection getNativeConnection()
    {
        return con;
    }

    /** Getter for property pool.
     * @return Value of property pool.
     *
     */
    public DBConnectionPool getPool()
    {
        return pool;
    }

    /** Getter for property description.
     * @return Value of property description.
     *
     */
    public java.lang.String getDescription()
    {
        if(description==null || description.length()==0)
        {
            StringBuilder ret=new StringBuilder();
            ret.append(threadName);
            for(int x=0;x<stack.length;x++)
            {
                if(x>=0)
                {
                    ret.append(stack[x].toString());
                    ret.append("\n");
                }
            }
            return ret.toString();
        }
        return description;
    }

    /** Setter for property description.
     * @param description New value of property description.
     *
     */
    public void setDescription(java.lang.String description)
    {
        this.description = description;
    }

    /**
     * Close statements.
     * 
     * @return true, if successful
     * @return
     */
    public boolean closeStatements()
    {
        boolean noerrors = true;
        while (!vec.isEmpty())
        {
            PoolStatement st = (PoolStatement) vec.get(0);
            if (st != null)
            {
                try
                {
                    if (!st.isClosed())
                    {
                        ResultSet rs = st.getResultSet();
                        if (rs != null)
                        {
                            rs.close();
                        }
                        st.close();
                        log.warn("Statement was not closed..., " + description);
                        noerrors = false;
                    }
                } catch (SQLException noe)
                {/*Es correcto el error, ya que el susuario cerro la conexion*/

                }
            }
            vec.remove(0);
        }
        return noerrors;
    }

    /**
     * _close.
     * 
     */
    public void _close()
    {
        destroyConnection();
    }

    /**
     * Cierra la conexión con la base de datos en vez de esperar. Una conexión puede ser cerrada
     * automáticamente cuando es garbage collected. También ciertos errores fatales puden cerrar la
     * conexión.
     * 
     * @throws SQLException the sQL exception
     * @exception java.sql.SQLException
     * Si un error de acceso a kla base de datos ocurre.
     */
    public void close() throws SQLException
    {
        if (!isclosed)
        {
            if (destroy)
            {
                log.trace("Connection.close(destroy):" + getId());
                try
                {
                    destroyConnection();
                    return;
                } catch (Exception e)
                {
                    log.error("Connection " + description + ", close.setAutocomit(false):", e);
                }
            }
            isclosed = true;
            if(pool!=null)pool.getConnectionManager().getTimeLock().removeConnection(this);
            idleTime = System.currentTimeMillis();
            try
            {
                closeStatements();
            } catch (Exception e)
            {
                log.error("Connection " + description + ", closeStatement:", e);
            }
            try
            {
                if(pool!=null)pool.freeConnection(this);
            } catch (Exception e)
            {
                log.error("Connection " + description + ", freeConnection:", e);
            }
            if(pool!=null)log.trace("close:(" + getId() + "," + pool.getName() + "):" + pool.checkedOut);
        }
    }

    /**
     * Configura el modo auto-commit de la conexión en el estado determinado. Si una conexión está en
     * auto-commit, entonces cada sentencia SQL será procesada y el commit se ejecutará por cada una
     * como una transacción individual. De lo contrario, sus sentencias SQL se agrupan en una transacción
     * que finalizará por una llamada al método <code>commit</code> o al método <code>rollback</code>.
     * Por default un nuevo objeto PoolConnection está en modo auto-commit.
     * 
     * @param param the new auto commit
     * @throws SQLException the sQL exception
     * @exception java.sql.SQLException
     * Si un error de acceso a kla base de datos ocurre.
     * @see       getAutoCommit()
     */
    public void setAutoCommit(boolean param) throws SQLException
    {
        if (!param)
        {
            destroy = true;
        }
        con.setAutoCommit(param);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#getWarnings()
     */
    public SQLWarning getWarnings() throws SQLException
    {
        return con.getWarnings();
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#getCatalog()
     */
    public String getCatalog() throws SQLException
    {
        return con.getCatalog();
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#setTypeMap(java.util.Map)
     */
    public void setTypeMap(java.util.Map map) throws SQLException
    {
        destroy = true;
        con.setTypeMap(map);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#getTypeMap()
     */
    public java.util.Map getTypeMap() throws SQLException
    {
        return con.getTypeMap();
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#getTransactionIsolation()
     */
    public int getTransactionIsolation() throws SQLException
    {
        return con.getTransactionIsolation();
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#isReadOnly()
     */
    public boolean isReadOnly() throws SQLException
    {
        return con.isReadOnly();
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#getMetaData()
     */
    public DatabaseMetaData getMetaData() throws SQLException
    {
        return con.getMetaData();
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#clearWarnings()
     */
    public void clearWarnings() throws SQLException
    {
        con.clearWarnings();
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#nativeSQL(java.lang.String)
     */
    public String nativeSQL(String str) throws SQLException
    {
        return con.nativeSQL(str);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
     */
    public PreparedStatement prepareStatement(String str, int param, int param2) throws SQLException
    {
        return con.prepareStatement(str, param, param2);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#setTransactionIsolation(int)
     */
    public void setTransactionIsolation(int param) throws SQLException
    {
        con.setTransactionIsolation(param);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#setReadOnly(boolean)
     */
    public void setReadOnly(boolean param) throws SQLException
    {
        destroy = true;
        con.setReadOnly(param);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#setCatalog(java.lang.String)
     */
    public void setCatalog(String str) throws SQLException
    {
        destroy = true;
        con.setCatalog(str);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#isClosed()
     */
    public boolean isClosed() throws SQLException
    {
        return isclosed || con.isClosed();
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#createStatement()
     */
    public Statement createStatement() throws SQLException
    {
        Statement st = new PoolStatement(con.createStatement(), this);
        vec.add(st);
        return st;
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#createStatement(int, int)
     */
    public Statement createStatement(int param, int param1) throws SQLException
    {
        Statement st = new PoolStatement(con.createStatement(param, param1), this);
        vec.add(st);
        return st;
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#prepareStatement(java.lang.String)
     */
    public PreparedStatement prepareStatement(String str) throws SQLException
    {
        return new PoolPreparedStatement(con.prepareStatement(str), str, this);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#getAutoCommit()
     */
    public boolean getAutoCommit() throws SQLException
    {
        return con.getAutoCommit();
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#prepareCall(java.lang.String)
     */
    public CallableStatement prepareCall(String str) throws SQLException
    {
        return con.prepareCall(str);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#commit()
     */
    public void commit() throws SQLException
    {
        con.commit();
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
     */
    public CallableStatement prepareCall(String str, int param, int param2) throws SQLException
    {
        return con.prepareCall(str, param, param2);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#rollback()
     */
    public void rollback() throws SQLException
    {
        con.rollback();
    }

    /**
     * Destroy connection.
     */
    protected void destroyConnection()
    {
        if (isdestroyed == false)
        {
            isdestroyed = true;
            isclosed = true;
            if(pool!=null)
            {
                pool.checkedOut--;
                pool.getConnectionManager().getTimeLock().removeConnection(this);
            }
            try
            {
                if(!con.isClosed())con.close();
            } catch (Exception e)
            {
                log.error("Connection " + description + " finalize",e);                
            }
            if(pool!=null)log.debug("destroyConnection:(" + getId() + "," + pool.getName() + "):" + pool.checkedOut);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable
    {
        // We are no longer referenced by anyone (including the
        // connection pool). Time to close down.
        try
        {
            if (isdestroyed == false)
            {
                log.warn("finalize(" + getId() + ")..., connection was not closed, " + description);
                destroyConnection();
            }
        } finally
        {
            super.finalize();
        }
    }
//************************************ jdk 1.4 *****************************************************************

    /* (non-Javadoc)
 * @see java.sql.Connection#setSavepoint()
 */
public java.sql.Savepoint setSavepoint() throws java.sql.SQLException
    {
        destroy = true;
        return con.setSavepoint();
        //return null;
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#setHoldability(int)
     */
    public void setHoldability(int param) throws java.sql.SQLException
    {
        destroy = true;
        con.setHoldability(param);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#prepareStatement(java.lang.String, int)
     */
    public java.sql.PreparedStatement prepareStatement(java.lang.String str, int param) throws java.sql.SQLException
    {
        return con.prepareStatement(str, param);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
     */
    public java.sql.PreparedStatement prepareStatement(java.lang.String str, int param, int param2, int param3) throws java.sql.SQLException
    {
        return con.prepareStatement(str, param, param2, param3);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
     */
    public java.sql.PreparedStatement prepareStatement(java.lang.String str, int[] values) throws java.sql.SQLException
    {
        return con.prepareStatement(str, values);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#getHoldability()
     */
    public int getHoldability() throws java.sql.SQLException
    {
        return con.getHoldability();
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#setSavepoint(java.lang.String)
     */
    public java.sql.Savepoint setSavepoint(java.lang.String str) throws java.sql.SQLException
    {
        destroy = true;
        return con.setSavepoint(str);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#createStatement(int, int, int)
     */
    public java.sql.Statement createStatement(int param, int param1, int param2) throws java.sql.SQLException
    {
        return con.createStatement(param, param1, param2);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
     */
    public java.sql.CallableStatement prepareCall(java.lang.String str, int param, int param2, int param3) throws java.sql.SQLException
    {
        return con.prepareCall(str, param, param2, param3);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
     */
    public void releaseSavepoint(java.sql.Savepoint savepoint) throws java.sql.SQLException
    {
        con.releaseSavepoint(savepoint);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#prepareStatement(java.lang.String, java.lang.String[])
     */
    public java.sql.PreparedStatement prepareStatement(java.lang.String str, java.lang.String[] str1) throws java.sql.SQLException
    {
        return con.prepareStatement(str, str1);
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#rollback(java.sql.Savepoint)
     */
    public void rollback(java.sql.Savepoint savepoint) throws java.sql.SQLException
    {
        destroy = true;
        con.rollback(savepoint);
    }

    /**
     * Getter for property idle_time.
     * @return Value of property idle_time.
     */
    public long getIdleTime()
    {
        return idleTime;
    }
//********************************* JAVA 1.6   
    
    public Clob createClob() throws SQLException {
    return con.createClob();
    }

    public Blob createBlob() throws SQLException {
    return con.createBlob();
    }

    public NClob createNClob() throws SQLException {
    return con.createNClob();
    }

    public SQLXML createSQLXML() throws SQLException {
    return con.createSQLXML();
    }

    public boolean isValid(int timeout) throws SQLException {
    return con.isValid(timeout);
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
    con.setClientInfo(name, value);
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
    con.setClientInfo(properties);
    }

    public String getClientInfo(String name) throws SQLException {
    return con.getClientInfo(name);
    }

    public Properties getClientInfo() throws SQLException {
    return con.getClientInfo();
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    return con.createArrayOf(typeName, elements);
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    return con.createStruct(typeName, attributes);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
    return con.unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return con.isWrapperFor(iface);
    }
    
/* MAPS74 - Wrapping for Java SE 7 */  
    @Override
    public void setSchema(String schema) throws SQLException
    {
        con.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException
    {
        return con.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException
    {
        con.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
    {
        con.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException
    {
        return con.getNetworkTimeout();
    }
}
