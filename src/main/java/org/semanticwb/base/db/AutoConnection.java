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

import java.sql.*;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Wrapper class for a {@link Connection} object with methods to perform SWB specific actions
 * on a {@link DBConnectionPool}.
 *
 * @author javier.solis
 */
public class AutoConnection implements Connection {
	private static final Logger log = SWBUtils.getLogger(AutoConnection.class);
	private java.sql.Connection con;
	private DBConnectionPool pool;
	private String description = "";
	private long id = 0;
	private boolean isClosed = false;

	/**
	 * Creates a new {@link AutoConnection} instance.
	 * @param con {@link Connection} object to wrap.
	 * @param pool {@link DBConnectionPool} object responsible for managing the connections.
	 */
	public AutoConnection(Connection con, DBConnectionPool pool) {
		this.con = con;
		this.pool = pool;
		log.trace("AutoConnection(" + getId() + "," + pool.getName() + "):" + pool.checkedOut);
	}

	/**
	 * Checks the connection.
	 * @return true, if connection is open.
	 */
	public boolean checkConnection() {
		boolean ret = false;
		if (!isClosed) {
			if (con != null) {
				try {
					if (con.isClosed()) {
						changeConnection();
						ret = true;
					}
				} catch (SQLException e) {
					log.error("AutoConnection: Error accessing database.", e);
					changeConnection();
					ret = true;
				}
			} else {
				changeConnection();
			}
		}
		return ret;
	}

	/**
	 * Creates a new {@link Connection} object managed by the {@link AutoConnection}.
	 */
	public void changeConnection() {
		con = pool.newNoPoolConnection();
	}

	/**
	 * Gets the id property.
	 * @return ID for this object.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id property.
	 * @param id Identifier for this object.
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Gets the wrapped {@link Connection} object.
	 * @return {@link Connection} object wrapped by this class.
	 */
	public java.sql.Connection getNativeConnection() {
		return con;
	}

	/**
	 * Gets the {@link DBConnectionPool} associated to this object.
	 * @return {@link DBConnectionPool} object.
	 */
	public DBConnectionPool getPool() {
		return pool;
	}

	/**
	 * Gets the description of this object.
	 * @return description of this object.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of this object.
	 * @param description object description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Closes connection without waiting. A connection can be automatically closed when
	 * garbage collected or by certain errors.
	 * @throws SQLException when a database error occurs closing the connection.
	 */
	public void close() throws SQLException {
		isClosed = true;
		con.close();
		log.trace("close:(" + getId() + "," + pool.getName() + "):" + pool.checkedOut);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#getWarnings()
	 */
	public SQLWarning getWarnings() throws SQLException {
		checkConnection();
		return con.getWarnings();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#getCatalog()
	 */
	public String getCatalog() throws SQLException {
		checkConnection();
		return con.getCatalog();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#setCatalog(java.lang.String)
	 */
	public void setCatalog(String catalog) throws SQLException {
		checkConnection();
		con.setCatalog(catalog);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#getTypeMap()
	 */
	public java.util.Map getTypeMap() throws SQLException {
		checkConnection();
		return con.getTypeMap();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	public void setTypeMap(java.util.Map map) throws SQLException {
		checkConnection();
		con.setTypeMap(map);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#getTransactionIsolation()
	 */
	public int getTransactionIsolation() throws SQLException {
		checkConnection();
		return con.getTransactionIsolation();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#setTransactionIsolation(int)
	 */
	public void setTransactionIsolation(int level) throws SQLException {
		checkConnection();
		con.setTransactionIsolation(level);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#isReadOnly()
	 */
	public boolean isReadOnly() throws SQLException {
		checkConnection();
		return con.isReadOnly();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readOnly) throws SQLException {
		checkConnection();
		con.setReadOnly(readOnly);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#getMetaData()
	 */
	public DatabaseMetaData getMetaData() throws SQLException {
		checkConnection();
		return con.getMetaData();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#clearWarnings()
	 */
	public void clearWarnings() throws SQLException {
		checkConnection();
		con.clearWarnings();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	public String nativeSQL(String sql) throws SQLException {
		checkConnection();
		return con.nativeSQL(sql);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		checkConnection();
		return con.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#isClosed()
	 */
	public boolean isClosed() throws SQLException {
		checkConnection();
		return con.isClosed();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#createStatement()
	 */
	public Statement createStatement() throws SQLException {
		checkConnection();
		return new AutoStatement(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#createStatement(int, int)
	 */
	public Statement createStatement(int param, int param1) throws SQLException {
		checkConnection();
		return new AutoStatement(this, param, param1);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareStatement(java.lang.String)
	 */
	public PreparedStatement prepareStatement(String str) throws SQLException {
		checkConnection();
		return new AutoPreparedStatement(this, str);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#getAutoCommit()
	 */
	public boolean getAutoCommit() throws SQLException {
		checkConnection();
		return con.getAutoCommit();
	}

	/**
	 * Sets connection auto-commit mode. On auto-commit enabled connections, each SQL
	 * sentence will be processed and committed as a separate transaction. When connection
	 * is not in auto-commit mode, all SQL sentences are grouped in a single transaction
	 * that will be committed using <code>commit</code> or <code>rollback</code> methods.
	 * New instances of {@link PoolConnection} class are auto-commit enabled by default.
	 *
	 * @param enable boolean value to set auto-commit. TRUE for enable, FALSE for disable.
	 * @throws SQLException if value setting fails.
	 */
	public void setAutoCommit(boolean enable) throws SQLException {
		checkConnection();
		con.setAutoCommit(enable);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareCall(java.lang.String)
	 */
	public CallableStatement prepareCall(String sql) throws SQLException {
		checkConnection();
		return con.prepareCall(sql);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#commit()
	 */
	public void commit() throws SQLException {
		checkConnection();
		con.commit();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
	 */
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		checkConnection();
		return con.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#rollback()
	 */
	public void rollback() throws SQLException {
		checkConnection();
		con.rollback();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		log.warn("finalize()..., connection was not closed, " + description);
	}

	// ************************************ jdk 1.4
	// *****************************************************************
	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#setSavepoint()
	 */
	public Savepoint setSavepoint() throws SQLException {
		checkConnection();
		return con.setSavepoint();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		checkConnection();
		return con.prepareStatement(sql, autoGeneratedKeys);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		checkConnection();
		return con.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		checkConnection();
		return con.prepareStatement(sql, columnIndexes);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#getHoldability()
	 */
	public int getHoldability() throws SQLException {
		checkConnection();
		return con.getHoldability();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#setHoldability(int)
	 */
	public void setHoldability(int holdability) throws SQLException {
		checkConnection();
		con.setHoldability(holdability);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	public Savepoint setSavepoint(java.lang.String str) throws SQLException {
		checkConnection();
		return con.setSavepoint(str);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#createStatement(int, int, int)
	 */
	public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		checkConnection();
		return con.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		checkConnection();
		return con.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		checkConnection();
		con.releaseSavepoint(savepoint);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareStatement(java.lang.String,
	 * java.lang.String[])
	 */
	public java.sql.PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		checkConnection();
		return con.prepareStatement(sql, columnNames);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	public void rollback(java.sql.Savepoint savepoint) throws SQLException {
		checkConnection();
		con.rollback(savepoint);
	}

	// ********************************* JAVA 1.6
	public Clob createClob() throws SQLException {
		checkConnection();
		return con.createClob();
	}

	public Blob createBlob() throws SQLException {
		checkConnection();
		return con.createBlob();
	}

	public NClob createNClob() throws SQLException {
		checkConnection();
		return con.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		checkConnection();
		return con.createSQLXML();
	}

	public boolean isValid(int timeout) throws SQLException {
		checkConnection();
		return con.isValid(timeout);
	}

	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		checkConnection();
		con.setClientInfo(name, value);
	}

	public String getClientInfo(String name) throws SQLException {
		checkConnection();
		return con.getClientInfo(name);
	}

	public Properties getClientInfo() throws SQLException {
		checkConnection();
		return con.getClientInfo();
	}

	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		checkConnection();
		con.setClientInfo(properties);
	}

	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		checkConnection();
		return con.createArrayOf(typeName, elements);
	}

	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		checkConnection();
		return con.createStruct(typeName, attributes);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		checkConnection();
		return con.unwrap(iface);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		checkConnection();
		return con.isWrapperFor(iface);
	}

	@Override
	public String getSchema() throws SQLException {
		return con.getSchema();
	}

	/* MAPS74 - JSE 7 Wrapper */
	@Override
	public void setSchema(String schema) throws SQLException {
		con.setSchema(schema);
	}

	@Override
	public void abort(Executor executor) throws SQLException {
		con.abort(executor);
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		con.setNetworkTimeout(executor, milliseconds);
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return con.getNetworkTimeout();
	}
}
