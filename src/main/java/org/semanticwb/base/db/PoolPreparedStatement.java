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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

/**
 * Implementation of {@link PreparedStatement} for usage with a {@link PoolConnection}.
 * @author  Javier Solis Gonzalez (jsolis@infotec.com.mx) 
 */
public class PoolPreparedStatement implements java.sql.PreparedStatement {
    private PreparedStatement st;
    private boolean closed = false;
    private String query = null;
    private Connection con;

    /**
     * Creates a new instance of a {@link PoolStatement}.
     * @param st the statement.
     * @param con the connection.
     */
    public PoolPreparedStatement(PreparedStatement st, Connection con) {
        this.st = st;
        this.con = con;
    }

    /**
     * Creates a new instance of {@link PoolStatement}.
     * @param st the statement.
     * @param query the SQL query String.
     * @param con the connection.
     */
    public PoolPreparedStatement(PreparedStatement st, String query, Connection con) {
        this.st = st;
        this.query = query;
        this.con = con;
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#addBatch(java.lang.String)
     */
    public void addBatch(String sql) throws SQLException {
        st.addBatch(sql);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#cancel()
     */
    public void cancel() throws SQLException {
        st.cancel();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#clearBatch()
     */
    public void clearBatch() throws SQLException {
        st.clearBatch();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#clearWarnings()
     */
    public void clearWarnings() throws SQLException {
        st.clearWarnings();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#close()
     */
    public void close() throws SQLException {
        closed = true;
        st.close();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#execute(java.lang.String)
     */
    public boolean execute(String sql) throws SQLException {
        return st.execute(sql);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#executeBatch()
     */
    public int[] executeBatch() throws SQLException {
        return st.executeBatch();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#executeQuery(java.lang.String)
     */
    public ResultSet executeQuery(String sql) throws SQLException {
        return st.executeQuery(sql);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#executeUpdate(java.lang.String)
     */
    public int executeUpdate(String sql) throws SQLException {
        return st.executeUpdate(sql);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getConnection()
     */
    public Connection getConnection() throws SQLException {
        return con;
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getFetchDirection()
     */
    public int getFetchDirection() throws SQLException {
        return st.getFetchDirection();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getFetchSize()
     */
    public int getFetchSize() throws SQLException {
        return st.getFetchSize();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getMaxFieldSize()
     */
    public int getMaxFieldSize() throws SQLException {
        return st.getMaxFieldSize();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getMaxRows()
     */
    public int getMaxRows() throws SQLException {
        return st.getMaxRows();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getMoreResults()
     */
    public boolean getMoreResults() throws SQLException {
        return st.getMoreResults();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getQueryTimeout()
     */
    public int getQueryTimeout() throws SQLException {
        return st.getQueryTimeout();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getResultSet()
     */
    public java.sql.ResultSet getResultSet() throws SQLException {
        return st.getResultSet();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getResultSetConcurrency()
     */
    public int getResultSetConcurrency() throws SQLException {
        return st.getResultSetConcurrency();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getResultSetType()
     */
    public int getResultSetType() throws SQLException {
        return st.getResultSetType();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getUpdateCount()
     */
    public int getUpdateCount() throws SQLException {
        return st.getUpdateCount();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getWarnings()
     */
    public java.sql.SQLWarning getWarnings() throws SQLException {
        return st.getWarnings();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#setCursorName(java.lang.String)
     */
    public void setCursorName(String name) throws SQLException {
        st.setCursorName(name);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#setEscapeProcessing(boolean)
     */
    public void setEscapeProcessing(boolean enable) throws SQLException {
        st.setEscapeProcessing(enable);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#setFetchDirection(int)
     */
    public void setFetchDirection(int direction) throws SQLException {
        st.setFetchDirection(direction);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#setFetchSize(int)
     */
    public void setFetchSize(int fetchSize) throws SQLException {
        st.setFetchSize(fetchSize);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#setMaxFieldSize(int)
     */
    public void setMaxFieldSize(int max) throws SQLException {
        st.setMaxFieldSize(max);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#setMaxRows(int)
     */
    public void setMaxRows(int max) throws SQLException {
        st.setMaxRows(max);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#setQueryTimeout(int)
     */
    public void setQueryTimeout(int seconds) throws SQLException {
        st.setQueryTimeout(seconds);
    }

    /**
     * Checks if statement is closed.
     * @return true if it is closed.
     */
    public boolean isClosed() {
        return closed;
    }

//********************************** version 1.4
    /* (non-Javadoc)
 * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
 */
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return st.execute(sql, columnNames);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#execute(java.lang.String, int[])
     */
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return st.execute(sql, columnIndexes);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#execute(java.lang.String, int)
     */
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return st.execute(sql, autoGeneratedKeys);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getResultSetHoldability()
     */
    public int getResultSetHoldability() throws SQLException {
        return st.getResultSetHoldability();
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getMoreResults(int)
     */
    public boolean getMoreResults(int current) throws SQLException {
        return st.getMoreResults(current);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])
     */
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return st.executeUpdate(sql, columnNames);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#executeUpdate(java.lang.String, int)
     */
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return st.executeUpdate(sql, autoGeneratedKeys);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
     */
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return st.executeUpdate(sql, columnIndexes);
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getGeneratedKeys()
     */
    public ResultSet getGeneratedKeys() throws SQLException {
        return st.getGeneratedKeys();
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#executeQuery()
     */
    public ResultSet executeQuery() throws SQLException {
        return st.executeQuery();
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#executeUpdate()
     */
    public int executeUpdate() throws SQLException {
        return st.executeUpdate();
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setNull(int, int)
     */
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        st.setNull(parameterIndex, sqlType);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setBoolean(int, boolean)
     */
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        st.setBoolean(parameterIndex, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setByte(int, byte)
     */
    public void setByte(int parameterIndex, byte x) throws SQLException {
        st.setByte(parameterIndex, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setShort(int, short)
     */
    public void setShort(int parameterIndex, short x) throws SQLException {
        st.setShort(parameterIndex, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setInt(int, int)
     */
    public void setInt(int parameterIndex, int x) throws SQLException {
        st.setInt(parameterIndex, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setLong(int, long)
     */
    public void setLong(int parameterIndex, long x) throws SQLException {
        st.setLong(parameterIndex, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setFloat(int, float)
     */
    public void setFloat(int parameterIndex, float x) throws SQLException {
        st.setFloat(parameterIndex, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setDouble(int, double)
     */
    public void setDouble(int parameterIndex, double x) throws SQLException {
        st.setDouble(parameterIndex, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
     */
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        st.setBigDecimal(parameterIndex, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setString(int, java.lang.String)
     */
    public void setString(int parameterIndex, String x) throws SQLException {
        st.setString(parameterIndex, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setBytes(int, byte[])
     */
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        st.setBytes(parameterIndex, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
     */
    public void setDate(int parameterIndex, Date x) throws SQLException {
        st.setDate(parameterIndex, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
     */
    public void setTime(int parameterIndex, Time x) throws SQLException {
        st.setTime(parameterIndex, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
     */
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        st.setTimestamp(parameterIndex, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, int)
     */
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        st.setAsciiStream(parameterIndex, x, length);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setUnicodeStream(int, java.io.InputStream, int)
     */
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        st.setCharacterStream(parameterIndex, new InputStreamReader(x), length);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, int)
     */
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        st.setBinaryStream(parameterIndex, x, length);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#clearParameters()
     */
    public void clearParameters() throws SQLException {
        st.clearParameters();
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int, int)
     */
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        st.setObject(parameterIndex, x, targetSqlType, scale);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
     */
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        st.setObject(parameterIndex, x, targetSqlType);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
     */
    public void setObject(int parameterIndex, Object x) throws SQLException {
        st.setObject(parameterIndex, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#execute()
     */
    public boolean execute() throws SQLException {
        boolean ret;
        ret = st.execute();
        return ret;
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#addBatch()
     */
    public void addBatch() throws SQLException {
        st.addBatch();
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, int)
     */
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        st.setCharacterStream(parameterIndex, reader, length);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
     */
    public void setRef(int i, Ref x) throws SQLException {
        st.setRef(i, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
     */
    public void setBlob(int i, Blob x) throws SQLException {
        st.setBlob(i, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
     */
    public void setClob(int i, Clob x) throws SQLException {
        st.setClob(i, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
     */
    public void setArray(int i, Array x) throws SQLException {
        st.setArray(i, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#getMetaData()
     */
    public ResultSetMetaData getMetaData() throws SQLException {
        return st.getMetaData();
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setDate(int, java.sql.Date, java.util.Calendar)
     */
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        st.setDate(parameterIndex, x, cal);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setTime(int, java.sql.Time, java.util.Calendar)
     */
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        st.setTime(parameterIndex, x, cal);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp, java.util.Calendar)
     */
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
     */
    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        st.setNull(paramIndex, sqlType, typeName);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
     */
    public void setURL(int parameterIndex, URL x) throws SQLException {
        st.setURL(parameterIndex, x);
    }

    /* (non-Javadoc)
     * @see java.sql.PreparedStatement#getParameterMetaData()
     */
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return st.getParameterMetaData();
    }
//********************************** version 1.6

    public void setPoolable(boolean poolable) throws SQLException {
        st.setPoolable(poolable);
    }

    public boolean isPoolable() throws SQLException {
        return st.isPoolable();
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return st.unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return st.isWrapperFor(iface);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        st.setAsciiStream(parameterIndex, x, length);
    }

    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        st.setAsciiStream(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        st.setBinaryStream(parameterIndex, x, length);
    }

    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        st.setBinaryStream(parameterIndex, x);
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        st.setBlob(parameterIndex, inputStream, length);
    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        st.setBlob(parameterIndex, inputStream);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        st.setCharacterStream(parameterIndex, reader, length);
    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        st.setCharacterStream(parameterIndex, reader);
    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        st.setClob(parameterIndex, reader, length);
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        st.setClob(parameterIndex, reader);
    }

    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        st.setNCharacterStream(parameterIndex, value, length);
    }

    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        st.setNCharacterStream(parameterIndex, value);
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        st.setNClob(parameterIndex, value);
    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        st.setNClob(parameterIndex, reader, length);
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        st.setNClob(parameterIndex, reader);
    }

    public void setNString(int parameterIndex, String value) throws SQLException {
        st.setNString(parameterIndex, value);
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        st.setRowId(parameterIndex, x);
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        st.setSQLXML(parameterIndex, xmlObject);
    }

    /* MAPS74 - JDK 7 SE Wrapper */  
    @Override
    public void closeOnCompletion() throws SQLException {
        st.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return isCloseOnCompletion();
    }
}
