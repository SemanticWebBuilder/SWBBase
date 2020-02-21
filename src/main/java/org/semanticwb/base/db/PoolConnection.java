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

import java.io.PrintStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Custom implementation of a Database connection.
 * @author Javier Solis Gonzalez (jsolis@infotec.com.mx)
 *
 * @see java.sql.Connection
 */
public class PoolConnection implements java.sql.Connection {
	private static Logger log = SWBUtils.getLogger(PoolConnection.class);
	private java.sql.Connection con;
	private ArrayList<Object> statements = new ArrayList<>();
	private DBConnectionPool pool;
	private boolean isClosed = false;
	private String description = "";
	private long id = 0;
	private long idleTime;
	private boolean destroy = false;
	private volatile boolean isDestroyed = false;
	private StackTraceElement[] stack = null;
	private String threadName = null;

	/**
	 * Constructor. Creates a new instance of {@link PoolConnection}.
	 * @param con connection object to wrap in.
	 */
	public PoolConnection(Connection con) {
		this(con, null);
	}

	/**
	 * Constructor. Creates a new instance of {@link PoolConnection} using a specified {@link DBConnectionPool}.
	 * @param con connection object to wrap in.
	 * @param pool connection pool to use.
	 */
	public PoolConnection(Connection con, DBConnectionPool pool) {
		idleTime = System.currentTimeMillis();
		this.con = con;
		this.pool = pool;
		if (pool != null) {
			pool.checkedOut++;
			log.trace("PoolConnection(" + getId() + "," + pool.getName() + "):" + pool.checkedOut);
		}
		init();
	}

	/**
	 * Inits the instance.
	 */
	public void init() {
		isClosed = false;
		description = "";
		id = 0;
	}

	/**
	 * Gets connection id.
	 * @return connection id.
	 *
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets connection id.
	 * @param id connection id.
	 */
	public void setId(long id) {
		threadName = Thread.currentThread().getName();
		stack = Thread.currentThread().getStackTrace();
		this.id = id;
	}

	/**
	 * Gets the stack trace elements.
	 * @return the stack trace elements
	 */
	public StackTraceElement[] getStackTraceElements() {
		return stack;
	}

	/**
	 * Prints the track trace to a {@link PrintStream}.
	 * @param out print stream.
	 */
	public void printTrackTrace(PrintStream out) {
		for (int x = 0; x < stack.length; x++) {
			out.println(stack[x]);
		}
	}

	/**
	 * Gets wrapped {@link Connection} object.
	 * @return wrapped connection.
	 */
	public Connection getNativeConnection() {
		return con;
	}

	/**
	 * Gets the {@link DBConnectionPool} object
	 * @return connection pool.
	 */
	public DBConnectionPool getPool() {
		return pool;
	}

	/**
	 * Gets the description of this Pool. If no description is set explicitly, a concatenation
	 * of stack trace and thread name is returned.
	 * @return Pool description.
	 */
	public String getDescription() {
		if (description == null || description.length() == 0) {
			StringBuilder ret = new StringBuilder();
			ret.append(threadName);
			for (int x = 0; x < stack.length; x++) {
				if (x >= 0) {
					ret.append(stack[x].toString());
					ret.append("\n");
				}
			}
			return ret.toString();
		}
		return description;
	}

	/**
	 * Sets the description for this Pool.
	 * @param description pool description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Close statements.
	 * @return true if statements were closed with no errors.
	 */
	public boolean closeStatements() {
		boolean success = true;
		while (!statements.isEmpty()) {
			PoolStatement st = (PoolStatement) statements.get(0);
			if (st != null && !st.isClosed()) {
				try {
					ResultSet rs = st.getResultSet();
					if (rs != null) {
						rs.close();
					}
					st.close();
					log.warn("Statement was not closed..., " + description);
					success = false;
				} catch (SQLException noe) {/* Es correcto el error, ya que el susuario cerro la conexion */

				}
			}
			statements.remove(0);
		}
		return success;
	}

	/**
	 * Closes connection without waiting. A connection can be automatically closed when
	 * garbage collected or by certain errors.
	 * @throws SQLException when an database error occurs.
	 */
	public void close() throws SQLException {
		if (!isClosed) {
			if (destroy) {
				log.trace("Connection.close(destroy):" + getId());
				try {
					destroyConnection();
					return;
				} catch (Exception e) {
					log.error("Connection " + description + ", close.setAutocomit(false):", e);
				}
			}

			isClosed = true;
			if (pool != null) {
				pool.getConnectionManager().getTimeLock().removeConnection(this);
			}

			idleTime = System.currentTimeMillis();
			try {
				closeStatements();
			} catch (Exception e) {
				log.error("Connection " + description + ", closeStatement:", e);
			}

			try {
				if (pool != null) {
					pool.freeConnection(this);
					log.trace("close:(" + getId() + "," + pool.getName() + "):" + pool.checkedOut);
				}
			} catch (Exception e) {
				log.error("Connection " + description + ", freeConnection:", e);
			}
		}
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
		if (!enable) {
			destroy = true;
		}
		con.setAutoCommit(enable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getWarnings()
	 */
	public SQLWarning getWarnings() throws SQLException {
		return con.getWarnings();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getCatalog()
	 */
	public String getCatalog() throws SQLException {
		return con.getCatalog();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	public void setTypeMap(java.util.Map map) throws SQLException {
		destroy = true;
		con.setTypeMap(map);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getTypeMap()
	 */
	public java.util.Map getTypeMap() throws SQLException {
		return con.getTypeMap();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getTransactionIsolation()
	 */
	public int getTransactionIsolation() throws SQLException {
		return con.getTransactionIsolation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#isReadOnly()
	 */
	public boolean isReadOnly() throws SQLException {
		return con.isReadOnly();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getMetaData()
	 */
	public DatabaseMetaData getMetaData() throws SQLException {
		return con.getMetaData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#clearWarnings()
	 */
	public void clearWarnings() throws SQLException {
		con.clearWarnings();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	public String nativeSQL(String sql) throws SQLException {
		return con.nativeSQL(sql);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return con.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setTransactionIsolation(int)
	 */
	public void setTransactionIsolation(int level) throws SQLException {
		con.setTransactionIsolation(level);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readOnly) throws SQLException {
		destroy = true;
		con.setReadOnly(readOnly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setCatalog(java.lang.String)
	 */
	public void setCatalog(String catalog) throws SQLException {
		destroy = true;
		con.setCatalog(catalog);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#isClosed()
	 */
	public boolean isClosed() throws SQLException {
		return isClosed || con.isClosed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createStatement()
	 */
	public Statement createStatement() throws SQLException {
		Statement st = new PoolStatement(con.createStatement(), this);
		statements.add(st);
		return st;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createStatement(int, int)
	 */
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		Statement st = new PoolStatement(con.createStatement(resultSetType, resultSetConcurrency), this);
		statements.add(st);
		return st;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String)
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return new PoolPreparedStatement(con.prepareStatement(sql), sql, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getAutoCommit()
	 */
	public boolean getAutoCommit() throws SQLException {
		return con.getAutoCommit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareCall(java.lang.String)
	 */
	public CallableStatement prepareCall(String sql) throws SQLException {
		return con.prepareCall(sql);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#commit()
	 */
	public void commit() throws SQLException {
		con.commit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
	 */
	public CallableStatement prepareCall(String sql, int resultType, int resultConcurrency) throws SQLException {
		return con.prepareCall(sql, resultType, resultConcurrency);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#rollback()
	 */
	public void rollback() throws SQLException {
		con.rollback();
	}

	/**
	 * Destroys connection.
	 */
	protected void destroyConnection() {
		if (!isDestroyed) {
			isDestroyed = true;
			isClosed = true;
			if (pool != null) {
				pool.checkedOut--;
				pool.getConnectionManager().getTimeLock().removeConnection(this);
			}

			try {
				if (!con.isClosed()) {
					con.close();
				}
			} catch (Exception e) {
				log.error("Connection " + description + " finalize", e);
			}

			if (pool != null) {
				log.debug("destroyConnection:(" + getId() + "," + pool.getName() + "):" + pool.checkedOut);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		// We are no longer referenced by anyone (including the
		// connection pool). Time to close down.
		try {
			if (!isDestroyed) {
				log.warn("finalize(" + getId() + ")..., connection was not closed, " + description);
				destroyConnection();
			}
		} finally {
			super.finalize();
		}
	}
	// ************************************ jdk 1.4
	// *****************************************************************

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setSavepoint()
	 */
	public Savepoint setSavepoint() throws SQLException {
		destroy = true;
		return con.setSavepoint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setHoldability(int)
	 */
	public void setHoldability(int holdability) throws SQLException {
		destroy = true;
		con.setHoldability(holdability);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return con.prepareStatement(sql, autoGeneratedKeys);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return con.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return con.prepareStatement(sql, columnIndexes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getHoldability()
	 */
	public int getHoldability() throws SQLException {
		return con.getHoldability();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	public Savepoint setSavepoint(String str) throws SQLException {
		destroy = true;
		return con.setSavepoint(str);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createStatement(int, int, int)
	 */
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return con.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return con.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		con.releaseSavepoint(savepoint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String,
	 * java.lang.String[])
	 */
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return con.prepareStatement(sql, columnNames);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	public void rollback(Savepoint savepoint) throws SQLException {
		destroy = true;
		con.rollback(savepoint);
	}

	/**
	 * Gets the idle time.
	 * @return idle time.
	 */
	public long getIdleTime() {
		return idleTime;
	}
	// ********************************* JAVA 1.6

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
	public void setSchema(String schema) throws SQLException {
		con.setSchema(schema);
	}

	@Override
	public String getSchema() throws SQLException {
		return con.getSchema();
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
