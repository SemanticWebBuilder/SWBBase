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
import org.semanticwb.base.util.SFBase64;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Connection Pool implementation. Creates connections on demand up to a defined limit if specified.
 * This implementation checks if the connection is open before returning it to clients.
 *
 * @author Javier Solis Gonzalez (jsolis@infotec.com.mx)
 */
public class DBConnectionPool {

	private static final String KEY = "akdhfyehe38";
	private static Logger log = SWBUtils.getLogger(DBConnectionPool.class);
	protected int checkedOut;
	protected ConcurrentLinkedQueue freeConnections = new ConcurrentLinkedQueue();
	private int maxConn;
	private String name;
	private String password = "";
	private String url;
	private String user;
	private long idleTime = 0;
	private DBConnectionManager manager;
	private long hits = 0;
	private long hitsTime = 0;

	/**
	 * Creates a new instance of a {@link DBConnectionPool}.
	 * @param manager {@link DBConnectionManager} object responsible for managing pools.
	 * @param name Pool name.
	 * @param jdbcURL URL connection String for the pool.
	 * @param user User for the connection. Can be null.
	 * @param password Password for the connection. Can be null.
	 * @param maxConn Maximum number of connections. A value of 0 means no limit.
	 * @param idleTime Connection idle time in seconds.
	 */
	public DBConnectionPool(DBConnectionManager manager, String name, String jdbcURL, String user, String password, int maxConn, long idleTime) {
		this.manager = manager;
		this.name = name;
		setURL(jdbcURL);
		this.user = user;
		setPassword(password);
		this.maxConn = maxConn;
		this.idleTime = idleTime * 1000;
	}

	/**
	 * Disposes a connection from the pool. Notifies threads waiting for connections.
	 * @param con {@link Connection} to dispose.
	 */
	public void freeConnection(Connection con) {
		boolean add = true;
		// Put the connection at the end of the Vector
		try {
			if (idleTime > 0 && (System.currentTimeMillis() - ((PoolConnection) con).getIdleTime()) > idleTime) {
				((PoolConnection) con).destroyConnection();
				add = false;
			}
			if (((PoolConnection) con).getNativeConnection().isClosed()) {
				((PoolConnection) con).destroyConnection();
				add = false;
			}
			if (add) {
				freeConnections.add(con);
			}
		} catch (Exception e) {
			log.warn("Exception when returning connection to pool", e);
		}
	}

	/**
	 * Gets a {@link Connection} from the {@link DBConnectionPool}. If no connections are
	 * available, a new one is created.
	 *
	 * @return new {@link Connection} object.
	 */
	public Connection getConnection() {
		// Escoje la primera conexión en el vector o utiliza round-robin.
		PoolConnection con = (PoolConnection) freeConnections.poll();

		if (con != null) {
			try {
				if (idleTime > 0 && (System.currentTimeMillis() - con.getIdleTime()) > idleTime) {
					log.warn("Removed bad connection " + con.getId() + " (idle_time) from " + name + ", "
							+ con.getDescription());
					con.destroyConnection();
					con = null;
					return getConnection();
				}
				if (con.getNativeConnection().isClosed()) {
					log.warn("Removed bad connection " + con.getId() + " (isClosed) from " + name + ", "
							+ con.getDescription());
					con.destroyConnection();
					con = null;
					return getConnection();
				}

				try {
					Statement st = con.getNativeConnection().createStatement();
					st.close();
				} catch (Exception e) {
					log.warn("Removed bad connection " + con.getId() + " (desc) from " + name + ", "
							+ con.getDescription());
					con.destroyConnection();
					con = null;
					return getConnection();
				}
			} catch (SQLException e) {
				log.error("Removed bad connection " + con.getId() + " from " + name + ", " + con.getDescription(), e);
				con.destroyConnection();
				con = null;
				return getConnection();
			}
			con.init();
		} else if (maxConn == 0 || checkedOut < maxConn) {
			con = (PoolConnection) newConnection();
		}

		if (con != null) {
			manager.getTimeLock().addConnection(con);
			log.trace("getConnection():" + con.getId() + " " + con.getPool().getName() + " " + freeConnections.size());
		}
		return con;
	}

	/**
	 * Gets a {@link Connection} from the {@link DBConnectionPool}. This method returns null
	 * after <code>timeout</code> milliseconds if no connection can be established.
	 *
	 * @param timeout timeout in milliseconds.
	 * @return new connection or null after timeout.
	 */
	public Connection getConnection(long timeout) {
		long startTime = System.currentTimeMillis();
		Connection con;
		while ((con = getConnection()) == null) {
			if ((System.currentTimeMillis() - startTime) >= timeout) {
				// Timeout ha transcurrido.
				return null;
			}
		}
		return con;
	}

	/**
	 * Closes all available connections.
	 */
	public void release() {
		PoolConnection con;
		while ((con = (PoolConnection) freeConnections.poll()) != null) {
			try {
				con.destroyConnection();
				log.debug("Closed connection for pool " + name + ", " + con.getDescription());
			} catch (Exception e) {
				log.error("Can't close connection for pool " + name + ", " + con.getDescription(), e);
			}
		}
	}

	/**
	 * Creates a new {@link Connection} with instance parameters.
	 * @return new Database connection or null.
	 */
	private Connection createConnection() {
		Connection con;
		try {
			if (user == null) {
				con = DriverManager.getConnection(url);
			} else {
				con = DriverManager.getConnection(url, user, getDecryptedPassword());
			}
			log.debug("Created a new connection in pool " + name);
		} catch (SQLException e) {
			log.error("Can't create a new connection for " + url, e);
			con = null;
		}
		return con;
	}

	/**
	 * Creates a new {@link Connection}.
	 * @return Database connection or null.
	 */
	public Connection newNoPoolConnection() {
		return createConnection();
	}

	/**
	 * Creates a new {@link AutoConnection}.
	 * @return Database autoconnection or null.
	 */
	public Connection newAutoConnection() {
		Connection con = createConnection();

		if (con != null) {
			return new AutoConnection(con, this);
		} else {
			return null;
		}
	}

	/**
	 * Creates a new {@link PoolConnection}.
	 * @return the pool connection or null.
	 */
	private Connection newConnection() {
		Connection con = createConnection();
		if (con != null) {
			return new PoolConnection(con, this);
		} else {
			return null;
		}
	}

	/**
	 * Gets the Pool name
	 * @return pool name.
	 *
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the Pool password.
	 * @return pool password.
	 *
	 */
	public String getPassword() {
		if (password.startsWith("{") && password.endsWith("}")) {
			return password;
		} else {
			try {
				return "{" + SFBase64.encodeBytes(SWBUtils.CryptoWrapper.PBEAES128Cipher(KEY, password.getBytes()))
						+ "}";
			} catch (Exception e) {
				log.error(e);
			}
			return null;
		}
	}

	/**
	 * Sets the Pool password.
	 * @param password password string.
	 */
	public void setPassword(String password) {
		if (password != null) {
			this.password = password;
		}
	}

	/**
	 * Gets decrypted password string.
	 * @return plain string for password.
	 */
	private String getDecryptedPassword() {
		if (password.startsWith("{") && password.endsWith("}")) {
			String pwd = password.substring(1, password.length() - 1);
			try {
				return new String(SWBUtils.CryptoWrapper.PBEAES128Decipher(KEY, SFBase64.decode(pwd)));
			} catch (Exception e) {
				log.error(e);
			}
			return null;
		} else {
			return password;
		}
	}

	/**
	 * Gets maximum number of connections allowed.
	 * @return maximum connection number.
	 *
	 */
	public int getMaxConn() {
		return maxConn;
	}

	/**
	 * Sets maximum number of connections allowed.
	 * @param maxConn connections allowed.
	 */
	public void setMaxConn(int maxConn) {
		this.maxConn = maxConn;
	}

	/**
	 * Gets the URL connection String for this pool.
	 * @return URL connection String.
	 *
	 */
	public String getURL() {
		return url;
	}

	/**
	 * Sets the URL connection String for this pool. Special tag <code>apppath</code> is replaced
	 * by application path.
	 * @param url URL connection String.
	 */
	public void setURL(String url) {
		this.url = SWBUtils.TEXT.replaceAll(url, "{apppath}", SWBUtils.getApplicationPath());
	}

	/**
	 * Gets the connection user.
	 * @return connection user.
	 *
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the connection user.
	 * @param user user.
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Gets total count of connection requests.
	 * @return connection request count.
	 */
	public long getHits() {
		return hits;
	}

	/**
	 * Gets the hits time.
	 * @return hits time.
	 */
	public long getHitsTime() {
		return hitsTime / 100;
	}

	/**
	 * Adds a hit.
	 * @param time hit time.
	 */
	public void addHit(long time) {
		long ttime = time * 100;
		hits++;
		hitsTime = (hitsTime * 9 + ttime) / 10;
	}

	/**
	 * Gets the {@link DBConnectionManager} object managed by the Pool.
	 * @return the connection manager.
	 */
	public DBConnectionManager getConnectionManager() {
		return manager;
	}

	/**
	 * Gets the Pool idle time.
	 * @return idle time.
	 */
	public long getIdleTime() {
		return idleTime;
	}
}
