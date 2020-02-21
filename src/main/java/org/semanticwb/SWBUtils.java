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
package org.semanticwb;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.PropertyConfigurator;
import org.semanticwb.base.db.DBConnectionManager;
import org.semanticwb.base.db.DBConnectionPool;
import org.semanticwb.base.db.PoolConnectionTimeLock;
import org.semanticwb.base.util.*;
import org.semanticwb.base.util.imp.Logger4jImpl;
import org.semanticwb.base.util.parser.html.HTMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.internet.InternetAddress;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.math.BigInteger;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.security.*;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Contains utilities for managing error logs, text, database, IO, Zip files,
 * emails, Xml, Xslt , Dom, Collections and encryption
 * <p>
 * Contiene utilerias para manejo de log de errores, texto, base de datos, IO,
 * Zip, Email, Xml, colecciones y encripciones
 * </p>
 * .
 *
 * @author Javier Solis Gonzalez
 * @author Jorge Jiménez
 * @author Hasdai Pacheco
 * @version 1.0
 */
public class SWBUtils {

	/**
	 * Holds a reference to a log utility.
	 * <p>
	 * Mantiene una referencia a la utiler&iacute;a de generaci&oacute;n de
	 * bit&aacute;coras.
	 * </p>
	 */
	private static final Logger LOG = getLogger(SWBUtils.class);
	/**
	 * Holds a reference to an object of this class.
	 * <p>
	 * Mantiene una referencia a un objeto de esta clase.
	 * </p>
	 */
	 private static SWBUtils instance;
	/**
	 * The string representing this application's physical path.
	 * <p>
	 * La cadena que representa la ruta f&iacute;sica de esta aplicaci&oacute;n
	 * </p>
	 */
	private static String applicationPath = SWBUtils.class.getResource("/").toString();

	static {
		if (applicationPath.endsWith("/")) {
			applicationPath = applicationPath.substring(0, applicationPath.length() - 1);
		}
	}
	/**
	 * Defines the size used for creating arrays that will be used in I/O
	 * operations.
	 * <p>
	 * Define el tama&ntilde;o utilizado en la creaci&oacute;n de arrays que
	 * ser&aacute;n utilizados en operaciones de entrada/salida.
	 * </p>
	 */
	private static int bufferSize = 8192;
	/**
	 * Indicates wheater or not the logger services have been iniciated.
	 * <p>
	 * Indica si los servicios del generador de bit&aacute;coras se han iniciado o
	 * no.
	 * </p>
	 */
	private static boolean initLogger = false;
	/**
	 * Specifies a default language to use.
	 * <p>
	 * Especifica un lenguaje a usar por defecto.
	 * </p>
	 */
	private static Locale locale = Locale.ENGLISH;
	/**
	 * Indicates the services used to provide localized messages.
	 * <p>
	 * Indica los servcios utilizados para proveer mensajes localizados.
	 * </p>
	 */
	public static final String LOCALE_SERVICES = "locale_services";
	/**
	 * Stablishes the logger's output as the one used for <code>System.err</code>.
	 * <p>
	 * Establece la salida del generador de bit&aacute;coras, como el mismo que usa
	 * {@code System.err}
	 * </p>
	 */
	private static PrintWriter fileLogWriter = new PrintWriter(System.err);
	/**
	 * Contains the error elements to show in the administration site's error
	 * viewer.
	 * <p>
	 * Contiene los elementos de error a mostrar en el visor de errores del sitio de
	 * administraci&oacute;n.
	 * </p>
	 */
	private static List<ErrorElement> errorElement = new ArrayList<>();
	/**
	 * Specifies the number of error elements to show in the administration site's
	 * error viewer.
	 * <p>
	 * Indica el n&uacute;mero de elementos de error a mostrar en el visor de
	 * errores del sitio de administraci&oacute;n.
	 * </p>
	 */
	private static int errorElementSize = 200;

	/**
	 * Creates a new object of this class.
	 */
	private SWBUtils() {
		LOG.event("Initializing SemanticWebBuilder Base...");
		LOG.event("-->AppicationPath: " + SWBUtils.applicationPath);
		init();
		LOG.event("-->Default Encoding: " + TEXT.getDafaultEncoding());
	}

	/**
	 * Retrieves a reference to the only one existing object of this class.
	 * <p>
	 * Obtiene una referencia al &uacute;nico objeto existente de esta clase.
	 * </p>
	 *
	 * @param applicationPath
	 *            a string representing the path for this application
	 * @return a reference to the only one existing object of this class
	 */
	 public static synchronized SWBUtils createInstance(String applicationPath) {
		SWBUtils.applicationPath = IO.normalizePath(applicationPath);
		if (instance == null) {
			initFileLogger();
			instance = new SWBUtils();
		}
		return instance;
	}

	/**
	 * Initializes the class variables needed to provide this object's services
	 * <p>Inicializa las variables de clase necesarias para proveer los servicios de
	 * este objeto.</p>
	 */
	private void init() {

	}

	/**
	 * Gets the physical path for the Web application to serve. For example: <br/>
	 * /tomcat/webapps/swb
	 * <p>
	 * Obtiene la ruta f&iacute;sica de la aplicaci&oacute;n Web a servir.
	 * </p>
	 *
	 * @return a string representing the physical path for the Web application to
	 *         serve
	 */
	public static String getApplicationPath() {
		return applicationPath;
	}

	/**
	 * Initializes the configuration fot the default logger for SWB. The following
	 * is the configuration set: <br/>
	 * {@code log4j.rootLogger=info, stdout} <br/>
	 * {@code log4j.appender.stdout=org.apache.log4j.ConsoleAppender} <br/>
	 * {@code log4j.appender.stdout.layout=org.apache.log4j.PatternLayout} <br/>
	 * {@code log4j.appender.stdout.layout.ConversionPattern=%d %p - %m%n} <br/>
	 * {@code log4j.logger.org.semanticwb=trace}
	 * <p>
	 * Inicializa el generador de bit&aacute;coras por defecto de SWB, con la
	 * configuraci&oacute;n descrita anteriormente.
	 * </p>
	 */
	private static void initLogger() {
		String logConf = "log4j.rootLogger=error, stdout" + "\n"
				+ "log4j.appender.stdout=org.apache.log4j.ConsoleAppender" + "\n"
				+ "log4j.appender.stdout.layout=org.apache.log4j.PatternLayout" + "\n"
				+ "log4j.appender.stdout.layout.ConversionPattern=%d %p - %m%n" + "\n"
				+ "log4j.logger.org.semanticwb=error";

		try {
			Properties proper = new Properties();
			proper.load(IO.getStreamFromString(logConf));
			PropertyConfigurator.configure(proper);
		} catch (Exception e) {
			LOG.error("Error loading logging properties");
		}
		initLogger = true;
	}

	/**
	 * Initializes the configuration fot the SWB's logger according to the content
	 * of a file. The file which stores the configuration to set is located in the
	 * directory {@literal /WEB-INF/classes/logging.properties} within the current
	 * application path.
	 * <p>
	 * Inicializa la configuraci&oacute;n del generador de bit&aacute;coras de SWB
	 * de acuerdo al contenido de un archivo. El archivo que almacena la
	 * configuraci&oacute;n a usar se ubica en el directorio
	 * {@literal /WEB-INF/classes/logging.properties} dentro de la ruta
	 * f&iacute;sica de la aplicaci&oacute;n.
	 * </p>
	 */
	private static void initFileLogger() {
		try {
			String logConf = "log4j.rootLogger=info, stdout" + "\n"
					+ "log4j.appender.stdout=org.apache.log4j.ConsoleAppender" + "\n"
					+ "log4j.appender.stdout.layout=org.apache.log4j.PatternLayout" + "\n"
					+ "log4j.appender.stdout.layout.ConversionPattern=%d %p - %m%n" + "\n"
					+ "log4j.logger.org.semanticwb=trace";

			File file = new File(applicationPath + "/WEB-INF/classes/logging.properties");
			if (!file.exists()) {
				file = new File(applicationPath + "/logging.properties");
			}

			FileInputStream in = new FileInputStream(file);
			logConf = IO.getStringFromInputStream(in);
			logConf = TEXT.replaceAll(logConf, "{apppath}", applicationPath);
			Properties proper = new Properties();
			proper.load(IO.getStreamFromString(logConf));
			PropertyConfigurator.configure(proper);
		} catch (Exception e) {
			LOG.error("Error: logging.properties not found...", e);
		}
		initLogger = true;
	}

	/**
	 * Creates a logger's instance and relates it to the class received.
	 * <p>
	 * Crea una instancia de un generador de archivos de bit&aacute;cora y la
	 * relaciona con la clase recibida.
	 * </p>
	 *
	 * @param cls
	 *            a class to relate to the creating instance
	 * @return a logger related to the class specified
	 */
	public static Logger getLogger(Class cls) {
		if (!initLogger) {
			initLogger();
		}
		return new Logger4jImpl(cls);
	}

	/**
	 * Carga los atributos del archivo de manifiesto del JAR asociado a la clase proporcionada.
	 *
	 * @return Objeto {@link Attributes} con la información del manifiesto o null si ocurre algún problema
	 * al cargar el archivo.
	 */
	public static Attributes loadJarManifestAttributes(Class clazz) {
		Attributes attributes = new Attributes();
		if (null != clazz) {
			URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
			if (null != resource) {
				String classPath = resource.toString();
				if (classPath.startsWith("jar")) {
					String manifestPath = classPath.substring(0, classPath.lastIndexOf('!') + 1) + "/META-INF/MANIFEST.MF";
					try (InputStream manifestStream = new URL(manifestPath).openStream()) {
						Manifest manifest = new Manifest(manifestStream);
						attributes = manifest.getMainAttributes();
					} catch (IOException ignored) {
						//Ignored
					}
				}
			}
		}
		return attributes;
	}

	/**
	 * Manages the element errors that are shown in the error viewer.
	 * <p>
	 * Administra los elementos que se muestran en el visor de errores.
	 * </p>
	 */
	public static class ERROR {

		private ERROR () { }

		/**
		 * Adds an element to the class variable {@link #errorElement} If the
		 * quantity of elements exceeds the {@link #errorElementSize}'s value,
		 * the last element of {@link #errorElement} is removed. Every element
		 * is added at the beginning of {@link #errorElement}.
		 * <p>
		 * Agrega un elemento a la variable de clase {@link #errorElement}. Si
		 * la cantidad de elementos excede el valor de
		 * {@link #errorElementSize}, el &uacute;ltimo elemento de
		 * {@link #errorElement} es eliminado. Cada elemento se agrega al inicio
		 * de {@link #errorElement}.
		 * </p>
		 *
		 * @param msg
		 *            a string containing the error's description
		 * @param e
		 *            the throwable object generated by the error
		 * @param cls
		 *            the class asociated to the logger
		 * @param level
		 *            a string representing the error element's level
		 */
		public static void addError(String msg, Throwable e, Class cls, String level) {
			errorElement.add(0, new ErrorElement(e, msg, cls, level));
			if (errorElement.size() > errorElementSize) {
				errorElement.remove(errorElementSize);
			}
		}

		/**
		 * Retrieves the {@link #errorElement}'s iterator, whose size is defined
		 * by the class member {@link #errorElementSize}.
		 * <p>
		 * Regresa el iterador de {@link #errorElement}, cuyo tamaño est&aacute;
		 * definido por la variable de clase {@link #errorElementSize}.
		 *
		 * @return the error elements
		 */
		public static Iterator<ErrorElement> getErrorElements() {
			return new ArrayList<>(errorElement).iterator();
		}

		/**
		 * Gets the value of the class member {@code SWBUtils.errorElementSize}.
		 * <p>
		 * Obtiene el valor de la variable de clase {@code SWBUtils.errorElementSize}.
		 * </p>
		 *
		 * @return the value of the class member {@code SWBUtils.errorElementSize}.
		 */
		public static int getErrorElementSize() {
			return errorElementSize;
		}

		/**
		 * Sets the value of the class member {@code SWBUtils.errorElementSize}.
		 * <p>
		 * Asigna el valor de la variable de clase {@code SWBUtils.errorElementSize}.
		 * </p>
		 *
		 * @param size
		 *            the new value of the class member
		 *            {@code SWBUtils.errorElementSize}.
		 */
		public static void setErrorElementSize(int size) {
			errorElementSize = size;
		}
	}

	/**
	 * Supplies several functions for handling strings commonly used, like:
	 * encoding, parsing, formatting, replacement, i18n and localization.
	 * <p>
	 * Provee varias funciones para la manipulaci&oacute;n de cadenas de texto
	 * utilizadas com&uacute;nmente, como: codificaci&oacute;n, an&aacute;lisis
	 * sint&aacute;ctico, formato y reemplazo, internacionalizaci&oacute;n y
	 * localizaci&oacute;n.
	 * </p>
	 */
	public static class TEXT {

		private TEXT() {}

		/**
		 * Specifies the value for the charset ISO8859-1.
		 * <p>
		 * Especifica el valor para el c&oacute;digo de caracteres ISO8859-1.
		 * </p>
		 */
		//TODO: Remove this harcoded charset name
		public static final String CHARSET_ISO8859_1 = "ISO8859_1";
		/**
		 * Specifies the value for the charset UTF-8
		 * <p>
		 * Especifica el valor para el c&oacute;digo de caracteres UTF-8.
		 * </p>
		 */
		//TODO: Remove this harcoded charset name
		public static final String CHARSET_UTF8 = "UTF8";
		/**
		 * Stores the name of the character encoding used by default.
		 * <p>
		 * Almacena el nombre del c&oacute;digo de caracteres utilizado por defecto.
		 * </p>
		 */
		private static String defencoding = null;
		/**
		 * a string that contains all characters in the english alphabet and the 10
		 * digits.
		 * <p>
		 * una string que contiene todos los caracteres del alfabeto ingl&eacute;s y los
		 * 10 d&iacute;gitos del sistema decimal.
		 * </p>
		 * Used to generate strings with characters selected randomly.
		 */
		private final static String ALPHABETH = "ABCDEFGHiJKLMNPQRSTUVWXYZ123456789+=?";

		//TODO: Refactor SimpleDateFormatTS attributes to use Java DateTimeFormatter class
		public static final SimpleDateFormatTS formatter = new SimpleDateFormatTS("MMMM");
		public static final SimpleDateFormatTS iso8601Full = new SimpleDateFormatTS("yyyy-MM-dd'T'HH:mm:ss'.'SSS");
		public static final SimpleDateFormatTS iso8601Long = new SimpleDateFormatTS("yyyy-MM-dd'T'HH:mm:ss");
		public static final SimpleDateFormatTS iso8601Short = new SimpleDateFormatTS("yyyy-MM-dd");

		/**
		 * Given a string specifying a charset, returns the value of
		 * {@code SWBUtils.TEXT.CHARSET_ISO8859_1} or the value of
		 * {@code SWBUtils.TEXT.CHARSET_UTF8}. If {@code charset} contains
		 * <q>UTF</q> anywhere the value returned will be {@value #CHARSET_UTF8}
		 * otherwise the value returned will be {@value #CHARSET_ISO8859_1}. For
		 * example: <br>
		 * if {@code charset} contains
		 * <q>UTF-8</q> or
		 * <q>UTF-2</q>, the value returned will be {@value #CHARSET_UTF8}.
		 * <p>
		 * Dada una cadena especificando un c&oacute;digo de caracteres, regresa el
		 * valor de
		 *
		 * @param charset
		 *            a string representing the name of a charset (like
		 *            <q>ISO-8859-1</q> or
		 *            <q>ISO8859-1</q> or
		 *            <q>8859-1</q> or
		 *            <q>UTF-8</q> or
		 *            <q>UTF_8</q>
		 * @return the string contained in {@code SWBUtils.TEXT.CHARSET_ISO8859_1} or in
		 *         {@code SWBUtils.TEXT.CHARSET_UTF8}, depending on the value received
		 *         in {@code charset}. {@code SWBUtils.TEXT.CHARSET_ISO8859_1} o el
		 *         valor de {@code SWBUtils.TEXT.CHARSET_UTF8}. Ejemplo: <br>
		 * 		si {@code charset} contiene
		 *         <q>ISO-8859-1</q> o
		 *         <q>ISO8859-1</q> o
		 *         <q>8859-1</q> el valor regresado, ser&aacute;
		 *         {@value #CHARSET_ISO8859_1}
		 *         </p>
		 */
		//TODO: Check why this method is necessary in Distributor.java
		public static String getHomCharSet(String charset) {
			String ret = TEXT.CHARSET_ISO8859_1;
			if (charset.toUpperCase().indexOf("UTF") > -1) {
				ret = TEXT.CHARSET_UTF8;
			}
			return ret;
		}

		/**
		 * @deprecated This method was only used in Banner.java to encode a query string.
		 * Use {@link URLEncoder} instead.
		 *
		 * Encode URLChars 4 Cross Site Scripting
		 * @param urlString the URL to sanitize.
		 * @return Sanitized URL string.
		 */
		@Deprecated
		public static String encodeURLChars4CSS(String urlString) {
			if (null != urlString) {
				return urlString.replace("\"", "%22")
						.replace("'", "%27")
						.replace(">", "%3E")
						.replace("<", "%3C");
			}
			return null;
		}

		/**
		 * Pluralizes an english word using naive rules.
		 * @param name Word to pluralize.
		 * @return Pluralized form o word.
		 */
		public static String getPlural(String name) {
			String nname = name;

			if (null != name) {
				if (nname.endsWith("y") && !(nname.endsWith("ay") || nname.endsWith("ey") || nname.endsWith("iy")
						|| nname.endsWith("oy") || nname.endsWith("uy"))) {
					nname = nname.substring(0, nname.length() - 1);
					nname += "ies";
				} else if (nname.endsWith("s") || nname.endsWith("z") || nname.endsWith("x") || nname.endsWith("ch")
						|| nname.endsWith("sh")) {
					nname += "es";
				} else if (nname.endsWith("is")) {
					nname = nname.substring(0, nname.length() - 2);
					nname += "es";
				} else if (nname.endsWith("fe")) {
					nname = nname.substring(0, nname.length() - 2);
					nname += "ves";
				} else {
					nname += "s";
				}
				return nname;
			}
			return null;
		}

		/**
		 * Generates a string of {@code size} characters selected in a random basis.
		 * <p>
		 * Genera una string con {@code size} caracteres seleccionados en orden
		 * aleatorio.
		 * </p>
		 *
		 * @param size the number of characters the resulting string will contain
		 * @return a string with {@code size} characters selected in a random basis. If
		 * {@code size} equals zero the returning string will be empty, and if
		 * {@code size} is less than zero an exception will be thrown.
		 * @throws NegativeArraySizeException if the {@code size} argument is less than zero.
		 */
		public static String getRandomString(int size) {
			StringBuilder sb = new StringBuilder(size);
			for (int i = 0; i < size; i++) {
				sb.append(ALPHABETH.charAt((int) (Math.random() * ALPHABETH.length())));
			}
			return sb.toString();
		}

		/**
		 * @deprecated Use {@link #capitalize(String)}.
		 * To upper case.
		 *
		 * @param data
		 *            the data
		 * @return the string
		 */
		@Deprecated
		public static String toUpperCase(String data) {
			return capitalize(data);
		}

		/**
		 * Capitalizes a String.
		 * @param data String to capitalize.
		 * @return Capitalized string.
		 */
		public static String capitalize(String data) {
			if (null != data) {
				return data.substring(0, 1).toUpperCase() + data.substring(1);
			}
			return null;
		}

		/**
		 * Returns the number corresponding to the month's full name specified according
		 * to the language received.
		 * <p>
		 * Regresa el n&uacute;mero correspondiente al nombre (completo) del mes
		 * especificado, el nombre del mes debe ser especificado en el lenguaje indicado
		 * en {@code language}.
		 * </p>
		 *
		 * @param month
		 *            a string representing the month's full name in the specified
		 *            {@code language}
		 * @param language
		 *            a string representing the language in which the month's name is
		 *            specified
		 * @return the number of month corresponding to the specified month's name. If
		 *         there is no match found between the name received and a month's name
		 *         in the specified language, a value of -1 is returned. If the language
		 *         specified is not accepted by {@link java.util.Locale}, the English
		 *         language will be used. el n&uacute;mero de mes correspondiente al
		 *         nombre de mes especificado. Si no hay correspondencia entre el nombre
		 *         recibido y un nombre de mes en el lenguaje especificado, un valor de
		 *         -1, ser&aacute; regresado. Si el lenguage especificado no es aceptado
		 *         por {@code Locale}, el lenguaje Ingl&eacute;s sera utilizado.
		 */
		public static int monthToInt(String month, String language) {
			Locale loc;
			try {
				loc = new Locale(language);
			} catch (Exception e) {
				loc = locale;
			}

			GregorianCalendar gc = new GregorianCalendar(loc);
			gc.set(Calendar.MONTH, Calendar.JANUARY);
			gc.set(Calendar.DATE, 1);
			int i;
			for (i = 1; i <= 12; i++) {
				if (formatter.format(gc.getTime()).equalsIgnoreCase(month)) {
					return i;
				}
				gc.add(Calendar.MONTH, 1);
			}
			return -1;
		}

		/**
		 * Returns the name of the character encoding used by default.
		 * <p>
		 * Regresa el nombre del c&oacute;digo de caracteres utilizado por defecto.
		 * </p>
		 *
		 * @return a string representing the character encoding used by default.
		 *
		 */
		public static String getDafaultEncoding() {
			if (defencoding == null) {
				OutputStreamWriter out = new OutputStreamWriter(new ByteArrayOutputStream());
				defencoding = out.getEncoding();
			}
			return defencoding;
		}

		/**
		 * Replaces from {@code str} all the occurrences of {@code match} with the
		 * content in {@code replace}.
		 * <p>
		 * Reemplaza en {@code str} las ocurrencias encontradas de {@code match} con el
		 * contenido de {@code replace}.
		 * </p>
		 *
		 * @param str
		 *            a string with the original content to modify
		 * @param match
		 *            a string with the content to find in {@code str}
		 * @param replace
		 *            a string with the replacing text
		 * @return a string with all the occurrences of {@code match} found in
		 *         {@code str} substituted by {@code replace}. una cadena con todas las
		 *         ocurrencias de {@code match} encontradas en {@code str} substituidas
		 *         por {@code replace}.
		 */
		public static String replaceAll(String str, String match, String replace) {
			String replaceString = (null != replace ? replace : "");

			if (match == null || match.length() == 0) {
				return str;
			}

			if (match.equals(replaceString)) {
				return str;
			}

			StringBuilder ret = new StringBuilder();
			int i = str.indexOf(match);
			int y = 0;
			while (i >= 0) {
				ret.append(str.substring(y, i));
				ret.append(replaceString);
				y = i + match.length();
				i = str.indexOf(match, y);
			}
			ret.append(str.substring(y));
			return ret.toString();
		}

		/**
		 * Replaces from {@code str} all the occurrences of {@code match} with the
		 * content in {@code replace} ignoring case.
		 * <p>
		 * Reemplaza en {@code str} las ocurrencias encontradas de {@code match} con el
		 * contenido de {@code replace} sin sensibilidad a las may&uacute;sculas.
		 * </p>
		 *
		 * @param str
		 *            a string with the original content to modify
		 * @param match
		 *            a string with the content to find in {@code str}
		 * @param replace
		 *            a string with the replacing text
		 * @return a string with all the occurrences of {@code match} found in
		 *         {@code str} substituted by {@code replace}. una cadena con todas las
		 *         ocurrencias de {@code match} encontradas en {@code str} substituidas
		 *         por {@code replace}.
		 * @author Jorge Jim&eacute;nez
		 */
		public static String replaceAllIgnoreCase(String str, String match, String replace) {
			String replaceString = (null != replace ? replace : "");

			if (match == null || match.length() == 0) {
				return str;
			}

			int i = str.toLowerCase().indexOf(match.toLowerCase());
			int y;
			while (i >= 0) {
				str = str.substring(0, i) + replaceString + str.substring(i + match.length());
				y = i + replaceString.length();
				i = str.toLowerCase().indexOf(match.toLowerCase(), y);
			}
			return str;
		}

		/**
		 * Replaces the first occurrence of {@code match} in {@code str} with the
		 * content of {@code replace} ignoring case.
		 * <p>
		 * Reemplaza la primera ocurrencia de {@code match} en {@code str} con el
		 * contenido de {@code replace} ignorando las may&uacute;sculas.
		 * </p>
		 *
		 * @param str
		 *            a string with the original content to modify
		 * @param match
		 *            a string with the content to find in {@code str}
		 * @param replace
		 *            a string with the replacing text
		 * @return a string with the first occurrence of {@code match} found in
		 *         {@code str} substituted by {@code replace}. una cadena con la primer
		 *         ocurrencia de {@code match} encontrada en {@code str} substituida por
		 *         {@code replace}.
		 * @author Jorge Jim&eacute;nez
		 */
		public static String replaceFirstIgnoreCase(String str, String match, String replace) {
			String replaceString = (null != replace ? replace : "");

			if (match == null || match.length() == 0) {
				return str;
			}

			int i = str.toLowerCase().indexOf(match.toLowerCase());
			if (i >= 0) {
				str = str.substring(0, i) + replaceString + str.substring(i + match.length());
			}
			return str;
		}

		/**
		 * @deprecated Use format method of {@link #iso8601Full} static attribute
		 * Converts a date into a string with the format
		 * {@literal yyyy-MM-dd'T'HH:mm:ss'.'SSS}.
		 * <p>
		 * Convierte un objeto date a uno string con el formato
		 * {@literal yyyy-MM-dd'T'HH:mm:ss'.'SSS}.
		 * </p>
		 *
		 * @param date
		 *            a date to convert
		 * @return a string representing the date received with the format
		 *         {@literal yyyy-MM-dd'T'HH:mm:ss'.'SSS}. un objeto string que
		 *         representa al date recibido, con el formato
		 *         {@literal yyyy-MM-dd'T'HH:mm:ss'.'SSS}.
		 */
		@Deprecated
		public static String iso8601DateFormat(Date date) {
			return iso8601Full.format(date);
		}

		/**
		 * @deprecated @deprecated Use parse method of {@link #iso8601Full} static attribute
		 * Converts a string representing a date with the format
		 * {@literal yyyy-MM-dd'T'HH:mm:ss'.'SSS} into a date.
		 * <p>
		 * Convierte un objeto string que representa una fecha con formato
		 *
		 * @param date
		 *            a string representing a date with the format
		 *            {@literal yyyy-MM-dd'T'HH:mm:ss'.'SSS}
		 * @return a date equivalent to the value of to the string received.
		 *
		 * @throws java.text.ParseException
		 *             if the value received does not represent a valid date.
		 *             <p>
		 *             Si el valor recibido no representa una fecha valida.
		 *             </p>
		 * @throws ParseException
		 *             the parse exception {@literal yyyy-MM-dd'T'HH:mm:ss'.'SSS}, en un
		 *             objeto date.
		 *             </p>
		 */
		@Deprecated
		public static Date iso8601DateParse(String date) throws ParseException {
			//TODO: Remove this method, logic is naive and error prone
			SimpleDateFormatTS iso8601dateFormat;
			if (date.length() > 19) {
				iso8601dateFormat = iso8601Full;// "yyyy-MM-dd'T'HH:mm:ss'.'SSS"
			} else if (date.length() > 10) {
				iso8601dateFormat = iso8601Long;// "yyyy-MM-dd'T'HH:mm:ss"
			} else {
				iso8601dateFormat = iso8601Short;// "yyyy-MM-dd"
			}
			return iso8601dateFormat.parse(date);
		}

		/**
		 * @deprecated Method is used only in Distributor.java and code can be written using parseInt directly.
		 * Converts a string in an integer value; if this is not possible, it returns
		 * the integer received.
		 * <p>
		 * Convierte un objeto string en un valor entero; si no es posible, devuelve el
		 * valor entero recibido.
		 * </p>
		 *
		 * @param val
		 *            a string representing a valid integer
		 * @param defa
		 *            a value to return in case the convertion is not possible
		 * @return an integer equivalent to the value represented by {@code val}, or
		 *         {@code defa} if the convertion is not possible or if {@code val} is
		 *         {@code null}. un entero equivalente al valor representado por
		 *         {@code val}, o {@code defa}, si la conversi&oacute;n no es posible o
		 *         si {@code val} es {@code null}.
		 */
		@Deprecated
		public static int getInt(String val, int defa) {
			try {
				return Integer.parseInt(val);
			} catch (Exception e) {
				return defa;
			}
		}

		/**
		 * Applies the charset specified in {@code enc} to the {@code data} received.
		 * <p>
		 * Aplica el conjunto de caracteres especificado en {@code enc} a la
		 * informaci&oacute;n recibida en {@code data}.
		 * </p>
		 *
		 * @param data
		 *            a string with the information to apply the charset
		 * @param enc
		 *            the charset to apply
		 * @return the string containing the {@code data} received with the charset
		 *         applied. el objeto string que contiene la informaci&oacute;n recibida
		 *         con el conjunto de caracteres aplicado.
		 * @throws java.io.UnsupportedEncodingException
		 *             If the specified charset's name is not supported.
		 *             <p>
		 *             Si el nombre del conjunto de caracteres especificado no es
		 *             soportado.
		 *             </p>
		 * @throws java.io.IOException
		 *             If there is a problem when applying the charset.
		 *             <p>
		 *             Si ocurre un problema al aplicar el conjunto de caracteres.
		 *             </p>
		 * @throws UnsupportedEncodingException
		 *             the unsupported encoding exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		public static String encode(String data, String enc) throws java.io.IOException {
			ByteArrayOutputStream sw = new java.io.ByteArrayOutputStream();
			OutputStreamWriter out = new OutputStreamWriter(sw, enc);
			out.write(data);
			out.flush();
			return new String(sw.toByteArray());
		}

		/**
		 * Decodes a string applying the specified charset in {@code enc}.
		 * <p>
		 * Decodifica el contenido de {@code data} aplicando el conjunto de caracteres
		 * especificado en {@code enc}.
		 * </p>
		 *
		 * @param data
		 *            the string to decode
		 * @param enc
		 *            the charset to apply
		 * @return a string resulting from applying the charset specified on
		 *         {@code data}. el objeto string que contiene la informaci&oacute;n
		 *         recibida con el conjunto de caracteres aplicado.
		 * @throws java.io.UnsupportedEncodingException
		 *             If the specified charset's name is not supported.
		 *             <p>
		 *             Si el nombre del conjunto de caracteres especificado no es
		 *             soportado.
		 *             </p>
		 * @throws java.io.IOException
		 *             If there is a problem when applying the charset.
		 *             <p>
		 *             Si ocurre un problema al aplicar el conjunto de caracteres.
		 *             </p>
		 * @throws UnsupportedEncodingException
		 *             the unsupported encoding exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		public static String decode(String data, String enc) throws java.io.IOException {
			ByteArrayInputStream sw = new ByteArrayInputStream(data.getBytes());
			InputStreamReader in = new InputStreamReader(sw, enc);
			StringBuilder ret = new StringBuilder(data.length());

			char[] bfile = new char[bufferSize];
			int x;
			while ((x = in.read(bfile, 0, bufferSize)) > -1) {
				ret.append(new String(bfile, 0, x));
			}
			in.close();
			return ret.toString();
		}

		/**
		 * Encodes an regular ASCII string into the corresponding values in Base 64.
		 * <p>
		 * Codifica un objeto string en los valores correspondientes en Base 64.
		 * </p>
		 *
		 * @param txt
		 *            a string to convert to Base 64 encoding
		 * @return a string equivalent to {@code txt} with the content represented in
		 *         Base 64. un objeto string equivalente a {@code txt} con el contenido
		 *         representado en Base 64.
		 */
		public static String encodeBase64(String txt) {
			return SFBase64.encodeString(txt);
		}

		/**
		 * Decodes an ASCII Base 64 represented string into the corresponding values in
		 * regular ASCII.
		 * <p>
		 * Decodifica un objeto string representado en ASCII en Base 64 en los valores
		 * correspondientes en c&oacute;digo ASCII regular.
		 * </p>
		 *
		 * @param txt
		 *            a string to convert from Base 64 encoding
		 * @return a string equivalent to {@code txt} with the content represented in
		 *         ASCII encoding. un objeto string equivalente a {@code txt} con el
		 *         contenido representado en c&oacute;digo ASCII.
		 */
		public static String decodeBase64(String txt) {
			return SFBase64.decodeToString(txt);
		}

		/**
		 * Converts to upper case the first letter of each word in {@code str}. Every
		 * blank space, period, hyphen and underscore is considered a word separator.
		 * <p>
		 * Convierte a may&uacute;scula la primera letra de cada palabra en {@code str}.
		 * Cada espacio en blanco, punto, gui&oacute;n y gui&oacute;n bajo se considera
		 * como separador de palabras.
		 * </p>
		 *
		 * @param str
		 *            a string whose content is going to be modified
		 * @return a string with the same content as {@code str} but with every word's
		 *         first letter turned to upper case. un objeto string con el mismo
		 *         contenido de {@code str} pero con cada inicial de palabra convertida
		 *         a may&uacute;scula.
		 */
		public static String toUpperCaseFL(String str) {
			boolean b = true;
			StringBuilder ret = new StringBuilder();
			for (int x = 0; x < str.length(); x++) {
				char c = str.charAt(x);
				if (b) {
					ret.append(Character.toUpperCase(c));
					b = false;
				} else {
					ret.append(c);
				}
				if (c == ' ' || c == '.' || c == '-' || c == '_') {
					b = true;
				}
			}
			return ret.toString();
		}

		/**
		 * Replaces accented charcacters from a String.
		 * @param in Input string.
		 * @param replaceSpaces Whether to replace blank spaces.
		 * @return
		 */
		private static String replaceAccentedCharacters(String in, boolean replaceSpaces) {
			String aux = in;

			aux = aux.replace('Á', 'A');
			aux = aux.replace('Ä', 'A');
			aux = aux.replace('Å', 'A');
			aux = aux.replace('Â', 'A');
			aux = aux.replace('À', 'A');
			aux = aux.replace('Ã', 'A');

			aux = aux.replace('É', 'E');
			aux = aux.replace('Ê', 'E');
			aux = aux.replace('È', 'E');
			aux = aux.replace('Ë', 'E');

			aux = aux.replace('Í', 'I');
			aux = aux.replace('Î', 'I');
			aux = aux.replace('Ï', 'I');
			aux = aux.replace('Ì', 'I');

			aux = aux.replace('Ó', 'O');
			aux = aux.replace('Ö', 'O');
			aux = aux.replace('Ô', 'O');
			aux = aux.replace('Ò', 'O');
			aux = aux.replace('Õ', 'O');

			aux = aux.replace('Ú', 'U');
			aux = aux.replace('Ü', 'U');
			aux = aux.replace('Û', 'U');
			aux = aux.replace('Ù', 'U');

			aux = aux.replace('Ñ', 'N');

			aux = aux.replace('Ç', 'C');
			aux = aux.replace('Ý', 'Y');

			aux = aux.replace('á', 'a');
			aux = aux.replace('à', 'a');
			aux = aux.replace('ã', 'a');
			aux = aux.replace('â', 'a');
			aux = aux.replace('ä', 'a');
			aux = aux.replace('å', 'a');

			aux = aux.replace('é', 'e');
			aux = aux.replace('è', 'e');
			aux = aux.replace('ê', 'e');
			aux = aux.replace('ë', 'e');

			aux = aux.replace('í', 'i');
			aux = aux.replace('ì', 'i');
			aux = aux.replace('î', 'i');
			aux = aux.replace('ï', 'i');

			aux = aux.replace('ó', 'o');
			aux = aux.replace('ò', 'o');
			aux = aux.replace('ô', 'o');
			aux = aux.replace('ö', 'o');
			aux = aux.replace('õ', 'o');

			aux = aux.replace('ú', 'u');
			aux = aux.replace('ù', 'u');
			aux = aux.replace('ü', 'u');
			aux = aux.replace('û', 'u');

			aux = aux.replace('ñ', 'n');

			aux = aux.replace('ç', 'c');
			aux = aux.replace('ÿ', 'y');
			aux = aux.replace('ý', 'y');

			if (replaceSpaces) {
				aux = aux.replace(' ', '_');
			}

			return aux;
		}

		/**
		 * Replaces accented characters and blank spaces in the string given. Makes the
		 * changes in a case sensitive manner, the following are some examples of the
		 * changes this method makes: <br>
		 *
		 * @param txt
		 *            a string in which the characters are going to be replaced
		 * @param replaceSpaces
		 *            a {@code boolean} indicating if blank spaces are going to be
		 *            replaced or not
		 * @return a string similar to {@code txt} but with neither accented or special
		 *         characters nor symbols in it. un objeto string similar a {@code txt}
		 *         pero sin caracteres acentuados o especiales y sin s&iacute;mbolos
		 *         {@literal Á} is replaced by {@literal A} <br>
		 *         {@literal Ê} is replaced by {@literal E} <br>
		 *         {@literal Ï} is replaced by {@literal I} <br>
		 *         {@literal â} is replaced by {@literal a} <br>
		 *         {@literal ç} is replaced by {@literal c} <br>
		 *         {@literal ñ} is replaced by {@literal n} <br>
		 *         and blank spaces are replaced by underscore characters, any symbol in
		 *         {@code txt} other than underscore is eliminated including the
		 *         periods.
		 *         <p>
		 *         Reemplaza caracteres acentuados y espacios en blanco en {@code txt}.
		 *         Realiza los cambios respetando caracteres en may&uacute;sculas o
		 *         min&uacute;sculas los caracteres en blanco son reemplazados por
		 *         guiones bajos, cualquier s&iacute;mbolo diferente a gui&oacute;n bajo
		 *         es eliminado.
		 *         </p>
		 */
		public static String replaceSpecialCharacters(String txt, boolean replaceSpaces) {
			StringBuilder ret = new StringBuilder();
			String aux = replaceAccentedCharacters(txt, replaceSpaces);
			int l = aux.length();

			for (int x = 0; x < l; x++) {
				char ch = aux.charAt(x);
				if ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_'
						|| ch == '-') {
					ret.append(ch);
				}
			}
			aux = ret.toString();
			return aux;
		}

		/**
		 * Replaces special characters for a file name, replacing spaces using underscore character.
		 * @param fileName File name.
		 * @param ch
		 * @param replaceSpaces whether to replace blank spaces using underscore.
		 * @return File name without special characters.
		 */
		public static String replaceSpecialCharactersForFile(String fileName, char ch, boolean replaceSpaces) {
			return replaceSpecialCharactersForFile(fileName, ch, replaceSpaces, '_');
		}

		/**
		 * Replaces special characters for a file name, replacing spaces using specified word separator.
		 * @param fileName File name.
		 * @param ch
		 * @param replaceSpaces whether to replace blank spaces using underscore.
		 * @return File name without special characters.
		 */
		public static String replaceSpecialCharactersForFile(String fileName, char ch, boolean replaceSpaces, char wordSeparator) {
			String aux = replaceAccentedCharacters(fileName, replaceSpaces);
			if (replaceSpaces) {
				aux = aux.replace(' ', wordSeparator);
			}
			return aux;
		}

		/**
		 * Replaces accented characters and blank spaces in the string given. Makes the
		 * changes in a case sensitive manner, the following are some examples of the
		 * changes this method makes: <br>
		 *
		 * @param txt
		 *            a string in which the characters are going to be replaced
		 * @param replaceSpaces
		 *            a {@code boolean} indicating if blank spaces are going to be
		 *            replaced or not
		 * @param ch
		 *            a {@code char} especifica algún caracter de puntuación permitido
		 * @return a string similar to {@code txt} but with neither accented or special
		 *         characters nor symbols in it. un objeto string similar a {@code txt}
		 *         pero sin caracteres acentuados o especiales y sin s&iacute;mbolos
		 *         {@literal Á} is replaced by {@literal A} <br>
		 *         {@literal Ê} is replaced by {@literal E} <br>
		 *         {@literal Ï} is replaced by {@literal I} <br>
		 *         {@literal â} is replaced by {@literal a} <br>
		 *         {@literal ç} is replaced by {@literal c} <br>
		 *         {@literal ñ} is replaced by {@literal n} <br>
		 *         and blank spaces are replaced by underscore characters, any symbol in
		 *         {@code txt} other than underscore is eliminated.
		 *         <p>
		 *         Reemplaza caracteres acentuados y espacios en blanco en {@code txt}.
		 *         Realiza los cambios respetando caracteres en may&uacute;sculas o
		 *         min&uacute;sculas los caracteres en blanco son reemplazados por
		 *         guiones bajos, cualquier s&iacute;mbolo diferente a gui&oacute;n bajo
		 *         es eliminado.
		 *         </p>
		 */
		public static String replaceSpecialCharacters(String txt, char ch, boolean replaceSpaces) {
			StringBuilder ret = new StringBuilder();
			String aux = replaceSpecialCharactersForFile(txt, ch, replaceSpaces);
			int l = aux.length();
			for (int x = 0; x < l; x++) {
				char chr = aux.charAt(x);
				if (chr == ch || (chr >= '0' && chr <= '9') || (chr >= 'a' && chr <= 'z') || (chr >= 'A' && chr <= 'Z')
						|| chr == '_' || chr == '-') {
					ret.append(chr);
				}
			}
			aux = ret.toString();
			return aux;
		}

		/**
		 * Scape4 script.
		 *
		 * @param txt
		 *            the txt
		 * @return the string
		 */
		public static String scape4Script(String txt) {
			return txt.replace("'", "\\'")
					.replace("\"", "&quot;");
		}

		/**
		 * Crop text.
		 *
		 * @param txt
		 *            the txt
		 * @param size
		 *            the size
		 * @return the string
		 */
		public static String cropText(String txt, int size) {
			String ret = txt;
			if (txt != null && txt.length() > size) {
				ret = txt.substring(0, size) + "...";
			}
			return ret;
		}

		/**
		 * Gets the value for a {@code key} in the specified {@code Bundle} with the
		 * default {@code locale}.
		 * <p>
		 * Obtiene el valor correspondiente al {@code key} especificado con el objeto
		 * {@code locale} utilizado por defecto.
		 * </p>
		 *
		 * @param bundle
		 *            a string specifying the bundle that contains the data to retrieve
		 * @param key
		 *            a string indicating the key name whose value is required
		 * @return a string representing the specified {@code key}'s value stored in
		 *         {@code Bundle}. un objeto string que representa el valor del elemento
		 *         {@code key} especificado almacenado en {@code Bundle}.
		 */
		public static String getLocaleString(String bundle, String key) {
			return getLocaleString(bundle, key, locale);
		}

		/**
		 * Gets the value for a {@code key} in the specified {@code Bundle} with the
		 * indicated {@code locale}.
		 * <p>
		 * Obtiene el valor correspondiente al {@code key} especificado con el objeto
		 * {@code locale} indicado.
		 * </p>
		 *
		 * @param bundle
		 *            a string specifying the bundle that contains the data to retrieve
		 * @param key
		 *            a string indicating the key name whose value is required
		 * @param locale
		 *            the locale that will be used to retrieve the {@code key} specified
		 * @return a string representing the specified {@code key}'s value stored in
		 *         {@code Bundle} in the language indicated by {@code locale}. un objeto
		 *         string que representa el valor del elemento {@code key} especificado
		 *         almacenado en {@code Bundle}.
		 */
		public static String getLocaleString(String bundle, String key, Locale locale) {
			return getLocaleString(bundle, key, locale, null);
		}

		/**
		 * Gets the value for a {@code key} in the specified {@code Bundle} with the
		 * indicated {@code locale} and class loader.
		 * <p>
		 * Obtiene el valor correspondiente al {@code key} especificado con los objetos
		 * {@code locale} y {@code loader} indicados.
		 * </p>
		 *
		 * @param bundle
		 *            a string specifying the bundle that contains the data to retrieve
		 * @param key
		 *            a string indicating the key name whose value is required
		 * @param locale
		 *            the locale that will be used to retrieve the {@code key} specified
		 * @param loader
		 *            the class loader from which the resource bundle is loaded
		 * @return a string representing the specified {@code key}'s value stored in
		 *         {@code Bundle} in the language indicated by {@code locale}. un objeto
		 *         string que representa el valor del elemento {@code key} especificado
		 *         almacenado en {@code Bundle}.
		 */
		public static String getLocaleString(String bundle, String key, Locale locale, ClassLoader loader) {
			String cad = "";
			try {
				if (loader == null) {
					cad = java.util.ResourceBundle.getBundle(bundle, locale).getString(key);
				} else {
					cad = java.util.ResourceBundle.getBundle(bundle, locale, loader).getString(key);
				}
			} catch (Exception e) {
				LOG.error("Error while looking for properties key:" + key + " in " + bundle);
			}
			return cad;
		}

		/**
		 * Returns the language which this object is working with.
		 * <p>
		 * Regresa el lenguaje con el que est&aacute; trabajando este objeto.
		 * </p>
		 * return the {@code locale} which this object is working with.
		 * <p>
		 * el objeto {@code locale} con el que est&aacute; trabajando este objeto.
		 * </p>
		 *
		 * @return the locale
		 */
		public static Locale getLocale() {
			return locale;
		}

		/**
		 * Splits a string according to a regular expression which is treated as a
		 * delimiter. All the splits and the delimiters found in {@code txt} are stored
		 * in an array list which is then returned.
		 * <p>
		 * Divide un objeto string de acuerdo a una expresi&oacute;n regular que es
		 * tratada como delimitador. Todas las divisiones (subcadenas) y los
		 * delimitadores encontrados se almacenan en un array list para al final ser
		 * devuelto.
		 * </p>
		 *
		 * @param txt
		 *            a string to be split
		 * @param regexp
		 *            a regular expression used as a delimeter to split {@code txt}
		 * @return an array list containing the substrings delimited by {@code regexp}
		 *         and all the substrings that complied with {@code regexp} un objeto
		 *         array list que contiene las subcadenas delimitadas por {@code regexp}
		 *         y todas las subcadenas que cumplen con {@code regexp}.
		 */
		// version 1.4
		public static ArrayList<String> regExpSplit(String txt, String regexp) {
			int index = 0;
			ArrayList<String> matchList = new ArrayList<>();
			java.util.regex.Matcher m = java.util.regex.Pattern.compile(regexp).matcher(txt);

			while (m.find()) {
				String match = txt.substring(index, m.start());
				if (match.length() > 0) {
					matchList.add(match);
				}
				match = txt.substring(m.start(), m.end());
				if (match.length() > 0) {
					matchList.add(match);
				}
				index = m.end();
			}

			String match = txt.substring(index, txt.length());
			if (match.length() > 0) {
				matchList.add(match);
			}
			return matchList;
		}

		/**
		 * Finds the substrings delimited by two given strings, inside another string.
		 * <p>
		 * Encuentra las subcadenas delimitadas por dos objetos string dados, dentro de
		 * otro objeto string.
		 * </p>
		 *
		 * @param str
		 *            a string into which the substrings are going to be looked for
		 * @param pre
		 *            a string that precedes the substring to extract from {@code str}
		 * @param pos
		 *            pos a string that goes immediatly after the substring to extract
		 *            from {@code str}
		 * @return an iterator with all the substrings found.
		 *
		 */
		public static Iterator<String> findInterStr(String str, String pre, String pos) {
			List<String> ret = new ArrayList<>();
			int y = 0;
			do {
				y = findInterStr(str, pre, pos, y, ret);
			} while (y > -1);
			return ret.iterator();
		}

		/**
		 * @deprecated Use {@link #findInterStr(String, String, String, int, List)}
		 * Finds a substring in {@code str} which position must be after {@code index}
		 * and is delimited by {@code pre} and {@code pos} strings. The substring found
		 * is then stored in {@code arr}.
		 * <p>
		 * Encuentra una subcadena en {@code str} cuya posici&oacute;n debe ser
		 * posterior a {@code index} y es delimitada por las cadenas {@code pre} y
		 * {@code pos}. La subcadena encontrada se almacena en {@code arr}.
		 * </p>
		 *
		 * @param str
		 *            a string from which a substring is going to be extracted
		 * @param pre
		 *            a string that precedes the substring to extract from {@code str}
		 * @param pos
		 *            a string that goes immediatly after the substring to extract from
		 *            {@code str}
		 * @param index
		 *            the position in {@code str} from which {@code pre} is looked for
		 * @param arr
		 *            the object in which the substring extracted is going to be stored
		 * @return the index in {@code str} immediatly after {@code pos}, or -1 if
		 *         {@code pre} is not found in {@code str}. El &iacute;ndice en
		 *         {@code str} inmediatamente despu&eacute;s de {@code pos}, o -1 si
		 *         {@code pre} no es encontrado en {@code str}.
		 *         </p>
		 */
		@Deprecated
		private static int findInterStr(String str, String pre, String pos, int index, ArrayList<String> arr) {
			return findInterStr(str, pre, pos, index, arr);
		}

		/**
		 * Finds a substring in {@code str} which position must be after {@code index}
		 * and is delimited by {@code pre} and {@code pos} strings. The substring found
		 * is then stored in {@code arr}.
		 * <p>
		 * Encuentra una subcadena en {@code str} cuya posici&oacute;n debe ser
		 * posterior a {@code index} y es delimitada por las cadenas {@code pre} y
		 * {@code pos}. La subcadena encontrada se almacena en {@code arr}.
		 * </p>
		 *
		 * @param str
		 *            a string from which a substring is going to be extracted
		 * @param pre
		 *            a string that precedes the substring to extract from {@code str}
		 * @param pos
		 *            a string that goes immediatly after the substring to extract from
		 *            {@code str}
		 * @param index
		 *            the position in {@code str} from which {@code pre} is looked for
		 * @param arr
		 *            the object in which the substring extracted is going to be stored
		 * @return the index in {@code str} immediatly after {@code pos}, or -1 if
		 *         {@code pre} is not found in {@code str}. El &iacute;ndice en
		 *         {@code str} inmediatamente despu&eacute;s de {@code pos}, o -1 si
		 *         {@code pre} no es encontrado en {@code str}.
		 *         </p>
		 */
		private static int findInterStr(String str, String pre, String pos, int index, List<String> arr) {
			int i = str.indexOf(pre, index);
			if (i > -1) {
				i = i + pre.length();
				int j = str.indexOf(pos, i);
				if (j > -1) {
					arr.add(str.substring(i, j));
					return j + pos.length();
				}
			}
			return -1;
		}

		/**
		 * @deprecated
		 * This method is locale dependent implementation and will be removed in future API versions.
		 * Use DateFormat methods instead.
		 *
		 * Obtains the day's name corresponding to the number received specifying the
		 * day of the week. The first day of the week is Sunday and its day number is
		 * zero.
		 * <p>
		 * Obtiene el nombre del d&iacute;a correspondiente al n&uacute;mero recibido
		 * especificando el d&iacute;a de la semana. El primer d&iacute;a de la semana
		 * es Domingo y le corresponde el n&uacute;mero cero.
		 * </p>
		 *
		 * @param day
		 *            the number of the day of the week
		 * @param lang
		 *            a string representing a language for obtaining the corresponding
		 *            name
		 * @return a string representing the name of the day specified. un objeto string
		 *         que representa el nombre del d&iacute;a de la semana especificado.
		 */
		@Deprecated
		public static String getStrDay(int day, String lang) {
			if (lang != null) {
				return getLocaleString("locale_date", "day_" + day, new Locale(lang));
			} else {
				return getLocaleString("locale_date", "day_" + day);
			}
		}

		/**
		 * @deprecated
		 * This method is locale dependent implementation and will be removed in future API versions.
		 * Use DateFormat methods instead.
		 *
		 * Obtains the month's name corresponding to the number received specifying the
		 * month of the year. The first month of the year is January and its
		 * corresponding number is zero.
		 * <p>
		 * Obtiene el nombre del mes correspondiente al n&uacute;mero recibido
		 * especificando el mes del a&ntilde;o. El primer mes del a&ntilde;o es Enero y
		 * le corresponde el n&uacute;mero cero.
		 * </p>
		 *
		 * @param month
		 *            the number of the month of the year
		 * @param lang
		 *            a string representing a language for obtaining the corresponding
		 *            name
		 * @return a string representing the name of the month specified.
		 *
		 */
		@Deprecated
		public static String getStrMonth(int month, String lang) {
			if (lang != null) {
				return getLocaleString("locale_date", "month_" + month, new Locale(lang));
			} else {
				return getLocaleString("locale_date", "month_" + month);
			}
		}

		/**
		 * Converts a given date into a string in the language specified.
		 * <p>
		 * Convierte un objeto date dado en un objeto string en el lenguaje
		 * especificado.
		 * </p>
		 *
		 * @param date
		 *            a date to be converted
		 * @param lang
		 *            a string representing the language to use in the convertion
		 * @return a string representing the date specified writen in the language
		 *         specified. un objeto string representando el objeto date
		 *         especificado, escrito en el lenguaje especificado.
		 */
		public static String getStrDate(Date date, String lang) {
			return getStrDate(date, lang, null);
		}

		/**
		 * Converts a date int o a string with the format and in the language specified.
		 * <p>
		 * Convierte una fecha en una cadena con el formato y en el lenguaje
		 * especificados.
		 * </p>
		 *
		 * @param date
		 *            a date to convert to a string
		 * @param lang
		 *            a string representing the language of the string to return
		 * @param format
		 *            a string representing the date format to show in the string to
		 *            return
		 * @return a string representing the date received, in the language and with the
		 *         format specified. un objeto string que representa la fecha recibida,
		 *         en el lenguaje y con el formato especificados.
		 */
		public static String getStrDate(Date date, String lang, String format) {
			String ret;
			if (format != null) {
				ret = getStrFormat(date, format, lang);
			} else if (lang != null) {
				Locale loc = new Locale(lang);
				DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, loc);
				ret = df.format(date);
			} else {
				DateFormat df = new SimpleDateFormat();
				ret = df.format(date);
			}

			if (ret == null || ret.length() == 0) {
				DateFormat df = new SimpleDateFormat();
				ret = df.format(date);
			}
			return ret;
		}

		/**
		 * Formats a given {@code date} according to the specified {@code format} and
		 * language. The valid patterns for each field in the date, are as follows:<br/>
		 * Day: DAY, Day, day, dd<br/>
		 * Month: MONTH, Month, month, mm<br/>
		 * Year: yyyy, yy<br/>
		 * Hours: hh<br/>
		 * Minutes: %m<br/>
		 * Seconds: ss
		 * <p>
		 * Da formato a una fecha proporcionada de acuerdo al patr&oacute;n de formato y
		 * lenguaje especificado. Los patrones v&aacute;lidos para cada uno de los
		 * campos de la fecha se describen arriba.
		 * </p>
		 *
		 * @param date
		 *            a date to format
		 * @param format
		 *            a string representing the pattern to use in the convertion
		 * @param lang
		 *            a string representing the language of the string to return
		 * @return a string representing the date received expressed in the format and
		 *         language specified. un objeto string que representa la fecha
		 *         recibida, expresada en el formato y lenguaje especificados.
		 */
		private static String getStrFormat(Date date, String format, String lang) {
			String ln = "es";
			if (null != lang && !lang.isEmpty()) {
				ln = lang;
			}

			Locale loc = new Locale(ln);
			String dayName = new SimpleDateFormat("EEEE", loc).format(date);
			String dayNumber = new SimpleDateFormat("dd", loc).format(date);
			String monthName = new SimpleDateFormat("MMMM", loc).format(date);
			String monthNumber = new SimpleDateFormat("mm", loc).format(date);
			String lYear = new SimpleDateFormat("yyyy", loc).format(date);
			String sYear = new SimpleDateFormat("yy", loc).format(date);
			String hour = new SimpleDateFormat("hh", loc).format(date);
			String minute = new SimpleDateFormat("mm", loc).format(date);
			String second = new SimpleDateFormat("ss", loc).format(date);

			String ret = format;
			ret = replaceAll(ret, "Day", dayName.substring(0,1).toUpperCase() + dayName.substring(1));
			ret = replaceAll(ret, "DAY", dayName.toUpperCase());
			ret = replaceAll(ret, "day", dayName.toLowerCase());
			ret = replaceAll(ret, "dd", dayNumber);
			ret = replaceAll(ret, "Month", monthName.substring(0,1).toUpperCase() + monthName.substring(1));
			ret = replaceAll(ret, "MONTH", monthName.toUpperCase());
			ret = replaceAll(ret, "month", monthName.toLowerCase());
			ret = replaceAll(ret, "mm", monthNumber);
			ret = replaceAll(ret, "yyyy", lYear);
			ret = replaceAll(ret, "yy", sYear);
			ret = replaceAll(ret, "hh", hour);
			ret = replaceAll(ret, "%m", minute);
			ret = replaceAll(ret, "ss", second);
			return ret;
		}

		/**
		 * Gets the difference in time between one date given and the system date. This
		 * difference is expressed in the biggest unit of time possible. These units of
		 * time being: seconds, minutes, hours, days, months and years.
		 * <p>
		 * Obtiene la diferencia en tiempo entre una fecha dada y la fecha del sistema.
		 * Esta diferencia se expresa en la unidad de tiempo m&aacute;s grande posible.
		 * Las unidades de tiempo manejadas son: segundos, minutos, horas, d&iacute;s,
		 * meses y a&ntilde;os.
		 * </p>
		 *
		 * @param creationDate
		 *            the date to compare
		 * @param lang
		 *            a string indicating the language in which the date is going to be
		 *            presented
		 * @return a string representing the difference between the date given and the
		 *         system's date, expressed in the biggest unit of time possible. un
		 *         objeto string que representa la diferencia entre la fecha dada y la
		 *         fecha del sistema, expresada en la unidad de tiempo m&aacute;s grande
		 *         posible.
		 */
		public static String getTimeAgo(Date creationDate, String lang) {
			return getTimeAgo(new Date(), creationDate, lang);
		}

		/**
		 * Gets the difference in time between two dates given. This difference is
		 * expressed in the biggest unit of time possible. These units of time being:
		 * seconds, minutes, hours, days, months and years.
		 * <p>
		 * Obtiene la diferencia en tiempo entre dos fechas dadas. Esta diferencia se
		 * expresa en la unidad de tiempo m&aacute;s grande posible. Las unidades de
		 * tiempo manejadas son: segundos, minutos, horas, d&iacute;s, meses y
		 * a&ntilde;os.
		 * </p>
		 *
		 * @param currentDate
		 *            the most recent date to compare
		 * @param creationDate
		 *            the oldest date to compare
		 * @param lang
		 *            a string indicating the language in which the date is going to be
		 *            presented
		 * @return a string representing the difference between the two dates given,
		 *         expressed in the biggest unit of time possible. un objeto string que
		 *         representa la diferencia entre dos fechas dadas, expresada en la
		 *         unidad de tiempo m&aacute;s grande posible.
		 */
		//TODO: Refactor method to return single time unit and remove unit conversion and labeling
		public static String getTimeAgo(Date currentDate, Date creationDate, String lang) {
			String ret = "";
			int second;
			int secondCurrent;
			int secondCreation;
			int minute;
			int minuteCurrent;
			int minuteCreation;
			int hour;
			int hourCurrent;
			int hourCreation;
			int day;
			int dayCurrent;
			int dayCreation;
			int month;
			int monthCurrent;
			int monthCreation;
			int year;
			int yearCurrent;
			int yearCreation;
			int dayMonth;

			secondCurrent = currentDate.getSeconds();
			secondCreation = creationDate.getSeconds();
			minuteCurrent = currentDate.getMinutes();
			minuteCreation = creationDate.getMinutes();
			hourCurrent = currentDate.getHours();
			hourCreation = creationDate.getHours();
			dayCurrent = currentDate.getDate();
			dayCreation = creationDate.getDate();
			monthCurrent = currentDate.getMonth();
			monthCreation = creationDate.getMonth();
			yearCurrent = currentDate.getYear();
			yearCreation = creationDate.getYear();

			boolean leapYear = false;
			if (monthCurrent > 1 || (dayCreation == 29 && monthCreation == 1)) {
				leapYear = (yearCreation % 4 == 0) && (yearCreation % 100 != 0 || yearCreation % 400 == 0);
			}
			dayMonth = 0;
			day = 0;
			switch (monthCreation) {
			case 0:
				dayMonth = 31;
				break;
			case 1:
				if (leapYear) {
					dayMonth = 29;
				} else {
					dayMonth = 28;
				}
				break;
			case 2:
				dayMonth = 31;
				break;
			case 3:
				dayMonth = 30;
				break;
			case 4:
				dayMonth = 31;
				break;
			case 5:
				dayMonth = 30;
				break;
			case 6:
				dayMonth = 31;
				break;
			case 7:
				dayMonth = 31;
				break;
			case 8:
				dayMonth = 30;
				break;
			case 9:
				dayMonth = 31;
				break;
			case 10:
				dayMonth = 30;
				break;
			case 11:
				dayMonth = 31;
				break;
			}
			if (secondCurrent >= secondCreation) {
				second = secondCurrent - secondCreation;
			} else {
				second = (60 - secondCreation) + secondCurrent;
				minuteCurrent = minuteCurrent - 1;
			}
			if (minuteCurrent >= minuteCreation) {
				minute = minuteCurrent - minuteCreation;
			} else {
				minute = (60 - minuteCreation) + minuteCurrent;
				hourCurrent = hourCurrent - 1;
			}
			if (hourCurrent >= hourCreation) {
				hour = hourCurrent - hourCreation;
			} else {
				hour = (24 - hourCreation) + hourCurrent;
				dayCurrent = dayCurrent - 1;
			}
			if (dayCurrent >= dayCreation) {
				day = day + (dayCurrent - dayCreation);
			} else {
				day = day + ((dayMonth - dayCreation) + dayCurrent);
				monthCurrent = monthCurrent - 1;
			}
			if (monthCurrent >= monthCreation) {
				month = monthCurrent - monthCreation;
			} else {
				month = (12 - monthCreation) + monthCurrent;
				yearCurrent = yearCurrent - 1;
			}

			//TODO: Move specific labeling outside method
			year = yearCurrent - yearCreation;
			if ("en".equals(lang)) {
				if (year > 0) {
					ret = (year + " years ago");
				} else if (month > 0) {
					ret = (month + " month ago");
				} else if (day > 0) {
					ret = (day + " days ago");
				} else if (hour > 0) {
					ret = (hour + " hours ago");
				} else if (minute > 0) {
					ret = (minute + " minutes ago");
				} else {
					ret = (second + " second ago");
				}
			} else {
				if (year > 0) {
					ret = (year + " año(s) atrás");
				} else if (month > 0) {
					ret = (month + " mes(es) atrás");
				} else if (day > 0) {
					ret = (day + " día(s) atrás");
				} else if (hour > 0) {
					ret = (hour + " hora(s) atrás");
				} else if (minute > 0) {
					ret = (minute + " minuto(s) atrás");
				} else {
					ret = (second + " segundo(s) atrás");
				}
			}
			return ret;
		}

		/**
		 * Finds in {@code path} the query string contained and extracts all the
		 * parameters and their corrresponding values. Every parameter has associated an
		 * array of strings containing all its values.
		 * <p>
		 * Encuentra en {@code path} la cadena de consulta contenida y extrae todos los
		 * par&aacute;metros y sus valores correspondientes. Cada par&aacute;metro tiene
		 * asociado un array de objetos string conteniendo todos sus valores.
		 * </p>
		 *
		 * @param path
		 *            a string representing a path that contains a query string
		 * @return a map that matches every parameter's name with its values. un objeto
		 *         map que relaciona el nombre de cada par&aacute;metro con el conjunto
		 *         de sus valores almacenados en un array de objetos string.
		 */
		public static Map<String, String[]> parseQueryParams(String path) {
			Map<String, String[]> map = new HashMap<>();
			if (path == null) {
				return map;
			}
			int idx = path.indexOf('?');
			String parms = path.substring(idx + 1);
			StringTokenizer st = new StringTokenizer(parms, "&");
			while (st.hasMoreTokens()) {
				String pair = st.nextToken();
				if (pair.indexOf('=') > 0) {
					String key = pair.substring(0, pair.indexOf('='));
					String val = pair.substring(pair.indexOf('=') + 1);
					map.put(key, new String[] { val });
				}
			}
			return map;
		}

		/**
		 * @deprecated Use {@link #getPropertiesFile(String)}
		 * Creates a properties object from the file whose name equals the value of the
		 * parameter {@code name}.
		 * <p>
		 * Crea un objeto properties a partir de un archivo cuyo nombre sea el mismo que
		 * el valor del par&aacute;metro {@code name}.
		 * </p>
		 *
		 * @param name
		 *            a string representing the name of the file from which the object
		 *            properties is going to be created.
		 * @return a properties object whose name equals the value of the parameter
		 *         {@code name}.
		 *         <p>
		 *         un objeto properties cuyo nombre es el mismo que el valor del
		 *         par&aacute;metro {@code name}.
		 *         </p>
		 */
		@Deprecated
		public static Properties getPropertyFile(String name) {
			return getPropertiesFile(name);
		}

		/**
		 * Creates a properties object from the file whose name equals the value of the
		 * parameter {@code name}.
		 * <p>
		 * Crea un objeto properties a partir de un archivo cuyo nombre sea el mismo que
		 * el valor del par&aacute;metro {@code name}.
		 * </p>
		 *
		 * @param name
		 *            a string representing the name of the file from which the object
		 *            properties is going to be created.
		 * @return a properties object whose name equals the value of the parameter
		 *         {@code name}.
		 *         <p>
		 *         un objeto properties cuyo nombre es el mismo que el valor del
		 *         par&aacute;metro {@code name}.
		 *         </p>
		 */
		public static Properties getPropertiesFile(String name) {
			Properties p = new SWBProperties();
			try (InputStream in = SWBUtils.class.getResourceAsStream(name)) {
				LOG.info("-->Loading Property File:" + name);
				p.load(in);
			} catch (Exception ex) {
				LOG.error("Error loading property file:" + name, ex);
			}

			return p;
		}

		/**
		 * Encodes from {@code str} those characters whose ASCII code is greater than
		 * 127. If a character has an ASCII code greater than 127, that character is
		 * replaced by
		 *
		 * @param str
		 *            a string to be parsed and modified
		 * @return a string with those characters whose ASCII code is greater than 127
		 *         replaced. If {@code str} is {@code null}, the empty string is
		 *         returned. un objeto string con aquellos caracteres cuyo c&oacute;digo
		 *         ASCII es superior a 127 reemplazados. Si {@code str} es nulo, regresa
		 *         cadena vacia. {@literal &#[ASCIIcode];} in {@code str}.
		 *         <p>
		 *         Codifica aquellos caracteres cuyo c&oacute;digo ASCII es superior a
		 *         127 en {@code str}. Si un caracter tiene un c&oacute;digo ASCII
		 *         superior a 127, ese caracter es reemplazado por
		 *         {@literal &#[codigoASCII];}
		 *         </p>
		 */
		public static String encodeExtendedCharacters(String str) {
			StringBuilder ret = new StringBuilder();
			if (str != null) {
				for (int x = 0; x < str.length(); x++) {
					char ch = str.charAt(x);
					if (ch > 127) {
						ret.append("&#" + (int) ch + ";");
					} else {
						ret.append(ch);
					}
				}
			}
			return ret.toString();
		}

		/**
		 * Decodes a string like the ones returned by
		 * {@link #encodeExtendedCharacters(java.lang.String)}.
		 * <p>
		 * Decodifica una cadena como las que regresa el m&eacute;todo
		 *
		 * @param str
		 *            a string with those characters whose ASCII code is greater than
		 *            127 encoded as {@literal &#[ASCIIcode];}
		 * @return a string with no characters encoded.
		 *
		 *         {@code encodeExtendedCharacters(java.lang.String)}.
		 *         </p>
		 */
		public static String decodeExtendedCharacters(String str) {
			StringBuilder ret = new StringBuilder();
			int l = str.length();
			for (int x = 0; x < l; x++) {
				char ch = str.charAt(x);
				boolean addch = false;
				if (ch == '&' && (x + 1) < l && str.charAt(x + 1) == '#') {
					int i = str.indexOf(';', x);
					if (i > 2) {
						try {
							int v = Integer.parseInt(str.substring(x + 2, i));
							if (v > 255) {
								ret.append((char) v);
								x = i;
								addch = true;
							}
						} catch (NumberFormatException e) { // Si no se puede parsear no se agrega
						}
					}
				}
				if (!addch) {
					ret.append(ch);
				}
			}
			return ret.toString();
		}

		/**
		 * Extracts all the text in a HTML document.
		 * <p>
		 * Extrae el texto de un documento HTML.
		 * </p>
		 *
		 * @param txt
		 *            a string representing the content of a HTML document
		 * @return a string representing all the text in the HTML document. un objeto
		 *         string que representa el texto contenido en el documento HTML.
		 * @throws java.io.IOException
		 *             if an I/O error occurs.
		 *             <p>
		 *             si ocurre cualquier error de E/S.
		 *             </p>
		 * @throws java.lang.InterruptedException
		 *             if this execution thread is interrupted by another thread.
		 *             <p>
		 *             si este hilo de ejecuci&oacute;n es interrumpido por otro hilo.
		 *             </p>
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @throws InterruptedException
		 *             the interrupted exception
		 */
		public static String parseHTML(String txt) throws IOException {
			String ret = null;
			if (txt != null) {
				HTMLParser parser = new HTMLParser(new StringReader(txt));
				ret = parser.getText();
			}
			return ret;
		}

		/**
		 * @deprecated Not intended to be used anymore.
		 * Valida si txt es nulo regresa def de lo contrario regresa txt
		 *
		 * @param txt
		 * @param def
		 * @return
		 */
		@Deprecated
		public static String nullValidate(String txt, String def) {
			if (txt == null) {
				return def;
			}
			return txt;
		}

		/**
		 * @deprecated Not intended to be used anymore.
		 * Valida si obj es nulo regresa def de lo contrario regresa txt
		 *
		 * @param txt
		 * @param def
		 * @return
		 */
		@Deprecated
		public static String nullValidate(Object obj, String txt, String def) {
			if (obj == null) {
				return def;
			}
			return txt;
		}

		/**
		 * @deprecated Use join method of {@link String} class.
		 * Une los elementos del arreglo <b>arr</b> de Strings con el valor
		 * <b>concat</b>
		 *
		 * @param concat,
		 *            valor a concatenar
		 * @param arr,
		 *            valores a concatenar con el valor del parametro concat
		 * @return
		 */
		@Deprecated
		public static String join(String concat, String[] arr) {
			StringBuilder ret = new StringBuilder();
			for (int x = 0; x < arr.length; x++) {
				ret.append(arr[x]);
				if ((x + 1) < arr.length) {
					ret.append(concat);
				}
			}
			return ret.toString();
		}
	}

	/**
	 * Supplies several I/O functions commonly used, involving streams, files,
	 * strings and entire file system structures.
	 * <p>
	 * Provee varias funciones de E/S utilizadas com&uacute;nmente, involucrando
	 * flujos, arhcivos, cadenas y estructuras completas del sistema de archivos.
	 * </p>
	 */
	public static class IO {

		private IO () {}

		/**
		 * Gets the value for the class variable {@code SWBUtils.bufferSize}.
		 * <p>
		 * Obtiene el valor de la variable de clase {@code SWBUtils.bufferSize}.
		 * </p>
		 *
		 * @return the integer value for the class variable {@code SWBUtils.bufferSize}.
		 *
		 */
		public static int getBufferSize() {
			return bufferSize;
		}

		/**
		 * Creates an inputStream from the string received.
		 * <p>
		 * Crea un objeto inputStream con el contenido del objeto string recibido.
		 * </p>
		 *
		 * @param str
		 *            a string from which an input stream is created
		 * @return the input stream created from the string received.
		 *
		 */
		public static InputStream getStreamFromString(String str) {
			InputStream ret = null;
			if (str != null) {
				ret = new ByteArrayInputStream(str.getBytes());
			}
			return ret;
		}

		/**
		 * Copies an input stream into an output stream using the buffer size defined by
		 * {@code SWBUtils.bufferSize} in the reading/writing operations.
		 * <p>
		 * Copia un flujo de entrada en uno de salida utilizando el tama&ntilde;o de
		 * buffer definido por {@code SWBUtils.bufferSize} en las operaciones de
		 * lectura/escritura.
		 * </p>
		 *
		 * @param in
		 *            the input stream to read from
		 * @param out
		 *            the output stream to write to
		 * @throws IOException
		 *             if either the input or the output stream is {@code null}.
		 *             <p>
		 *             Si el flujo de entrada o el de salida es {@code null}.
		 *             </p>
		 */
		public static void copyStream(InputStream in, OutputStream out) throws IOException {
			copyStream(in, out, bufferSize);
		}

		/**
		 * Copies an input stream into an output stream using the specified buffer size
		 * in the reading/writing operations.
		 * <p>
		 * Copia un flujo de entrada en uno de salida utilizando el tama&ntilde;o de
		 * buffer especificado en las operaciones de lectura/escritura.
		 * </p>
		 *
		 * @param in
		 *            the input stream to read from
		 * @param out
		 *            the output stream to write to
		 * @param bufferSize
		 *            the number of bytes read/writen at the same time in each I/O
		 *            operation
		 * @throws IOException
		 *             if either the input or the output stream is {@code null}.
		 *             <p>
		 *             Si el flujo de entrada o el de salida es {@code null}.
		 *             </p>
		 */
		public static void copyStream(InputStream in, OutputStream out, int bufferSize) throws IOException {
			if (in == null) {
				throw new IOException("Null Input Stream");
			}
			if (out == null) {
				throw new IOException("Null Ouput Stream");
			}

			byte[] bfile = new byte[bufferSize];
			int x;
			while ((x = in.read(bfile, 0, bufferSize)) > -1) {
				out.write(bfile, 0, x);
			}
			in.close();
			out.flush();
			out.close();
		}

		/**
		 * @deprecated Use {@link #getStringFromInputStream(InputStream)}.
		 *
		 * Reads an input stream and creates a string with that content.
		 * <p>
		 * Lee un objeto inputStream y crea un objeto string con el contenido
		 * le&iacute;do.
		 * </p>
		 *
		 * @param in
		 *            an input stream to read its content
		 * @return a string whose content is the same as for the input stream read. un
		 *         objeto string cuyo contenido es el mismo que el del objeto
		 *         inputStream le&iacute;do.
		 * @throws IOException
		 *             if the input stream received is {@code null}.
		 *             <p>
		 *             Si el objeto inputStream recibido tiene un valor {@code null}.
		 *             </p>
		 */
		@Deprecated
		public static String readInputStream(InputStream in) throws IOException {
			return getStringFromInputStream(in);
		}

		/**
		 * Reads an input stream and creates a string with that content.
		 * <p>
		 * Lee un objeto inputStream y crea un objeto string con el contenido
		 * le&iacute;do.
		 * </p>
		 *
		 * @param in
		 *            an input stream to read its content
		 * @return a string whose content is the same as for the input stream read. un
		 *         objeto string cuyo contenido es el mismo que el del objeto
		 *         inputStream le&iacute;do.
		 * @throws IOException
		 *             if the input stream received is {@code null}.
		 *             <p>
		 *             Si el objeto inputStream recibido tiene un valor {@code null}.
		 *             </p>
		 */
		public static String getStringFromInputStream(InputStream in) throws IOException {
			if (in == null) {
				throw new IOException("Input Stream null");
			}
			StringBuilder buf = new StringBuilder();
			byte[] bfile = new byte[SWBUtils.bufferSize];
			int x;
			while ((x = in.read(bfile, 0, SWBUtils.bufferSize)) > -1) {
				String aux = new String(bfile, 0, x);
				buf.append(aux);
			}
			in.close();
			return buf.toString();
		}

		/**
		 * @deprecated Use {@link #getStringFromReader(Reader)}.
		 * Reads a reader and creates a string with that content.
		 * <p>
		 * Lee un objeto Reader y crea un objeto string con el contenido le&iacute;do.
		 * </p>
		 *
		 * @param in
		 *            an input stream to read its content
		 * @return a string whose content is the same as for the input stream read. un
		 *         objeto string cuyo contenido es el mismo que el del objeto
		 *         inputStream le&iacute;do.
		 * @throws IOException
		 *             if the input stream received is {@code null}.
		 *             <p>
		 *             Si el objeto inputStream recibido tiene un valor {@code null}.
		 *             </p>
		 */
		@Deprecated
		public static String readReader(Reader in) throws IOException {
			return getStringFromReader(in);
		}

		/**
		 * Reads a reader and creates a string with that content.
		 * <p>
		 * Lee un objeto Reader y crea un objeto string con el contenido le&iacute;do.
		 * </p>
		 *
		 * @param in
		 *            an input stream to read its content
		 * @return a string whose content is the same as for the input stream read. un
		 *         objeto string cuyo contenido es el mismo que el del objeto
		 *         inputStream le&iacute;do.
		 * @throws IOException
		 *             if the input stream received is {@code null}.
		 *             <p>
		 *             Si el objeto inputStream recibido tiene un valor {@code null}.
		 *             </p>
		 */
		public static String getStringFromReader(Reader in) throws IOException {
			if (in == null) {
				throw new IOException("Input Stream null");
			}
			StringBuilder buf = new StringBuilder();
			char[] bfile = new char[SWBUtils.bufferSize];
			int x;
			while ((x = in.read(bfile, 0, SWBUtils.bufferSize)) > -1) {
				String aux = new String(bfile, 0, x);
				buf.append(aux);
			}
			in.close();
			return buf.toString();
		}

		/**
		 * @deprecated Use {@link #getStringFromInputStream(InputStream, String)}
		 * Reads an input stream and creates a string with the content read using the
		 * charset especified by name through {@code enc}.
		 * <p>
		 * Lee un objeto inputStream y crea una cadena con el contenido le&iacute;do
		 * utilizando el conjunto de caracteres especificado por nombre a trav&eacute;s
		 * de
		 *
		 * @param in
		 *            the input stream to read
		 * @param enc
		 *            the charset's name to use for representing the input stream's
		 *            content
		 * @return a string representing the input stream's content with the charset
		 *         specified. un objeto string que representa el contenido del objeto
		 *         inputStream con el conjunto de caracteres especificado.
		 * @throws java.io.UnsupportedEncodingException
		 *             if {@code enc} is {@code null}.
		 *             <p>
		 *             si el valor de {@code enc} es {@code null}.
		 *             </p>
		 * @throws java.io.IOException
		 *             if {@code inp} is {@code null}.
		 *             <p>
		 *             si el valor de {@code inp} es {@code null}.
		 *             </p>
		 * @throws UnsupportedEncodingException
		 *             the unsupported encoding exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred. {@code enc}.
		 */
		@Deprecated
		public static String readInputStream(InputStream in, String enc) throws java.io.IOException {
			return getStringFromInputStream(in, enc);
		}

		/**
		 * Reads an input stream and creates a string with the content read using the
		 * charset especified by name through {@code enc}.
		 * <p>
		 * Lee un objeto inputStream y crea una cadena con el contenido le&iacute;do
		 * utilizando el conjunto de caracteres especificado por nombre a trav&eacute;s
		 * de
		 *
		 * @param in
		 *            the input stream to read
		 * @param enc
		 *            the charset's name to use for representing the input stream's
		 *            content
		 * @return a string representing the input stream's content with the charset
		 *         specified. un objeto string que representa el contenido del objeto
		 *         inputStream con el conjunto de caracteres especificado.
		 * @throws java.io.UnsupportedEncodingException
		 *             if {@code enc} is {@code null}.
		 *             <p>
		 *             si el valor de {@code enc} es {@code null}.
		 *             </p>
		 * @throws java.io.IOException
		 *             if {@code inp} is {@code null}.
		 *             <p>
		 *             si el valor de {@code inp} es {@code null}.
		 *             </p>
		 * @throws UnsupportedEncodingException
		 *             the unsupported encoding exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred. {@code enc}.
		 */
		public static String getStringFromInputStream(InputStream in, String enc) throws java.io.IOException {
			if (in == null) {
				throw new IOException("Null Input Stream");
			}
			if (enc == null) {
				throw new UnsupportedEncodingException("Null Encoding");
			}
			InputStreamReader reader = new InputStreamReader(in, enc);
			return getStringFromReader(reader);
		}

		/**
		 * Normalizes the {@code path} received. Substitutes the following
		 * characters:<br/>
		 * <q>\</q> for
		 * <q>/</q><br/>
		 * <q>\\</q> for
		 * <q>/</q><br/>
		 * <q>//</q> for
		 * <q>/</q><br/>
		 * <q>/./</q> for
		 * <q>/</q><br/>
		 * and removes any relative path.
		 * <p>
		 * Normaliza la ruta descrita por {@code path}. Sustituyendo algunos caracteres
		 * como se describe arriba y eliminando las rutas relativas encontradas.
		 *
		 * @param path
		 *            a string representing a path
		 * @return a string representing the path received normalized according to the
		 *         rules described above. un objeto string que representa la ruta
		 *         recibida, normalizada de acuerdo a las reglas descritas
		 *         anteriormente.
		 */
		public static String normalizePath(String path) {
			if (path == null) {
				return null;
			}
			String normalized = path;
			if (normalized.equals("/.")) {
				return "/";
			}

			if (normalized.indexOf('\\') >= 0) {
				normalized = normalized.replace('\\', '/');
			}

			if (!normalized.startsWith("/") && normalized.indexOf(':') < 0) {
				normalized = "/" + normalized;
			}

			do {
				int index = normalized.indexOf("//");
				if (index < 0) {
					break;
				}
				normalized = normalized.substring(0, index) + normalized.substring(index + 1);
			} while (true);
			do {
				int index = normalized.indexOf("/./");
				if (index < 0) {
					break;
				}
				normalized = normalized.substring(0, index) + normalized.substring(index + 2);
			} while (true);
			do {
				int index = normalized.indexOf("/../");
				if (index >= 0) {
					if (index == 0) {
						return null;
					}
					int index2 = normalized.lastIndexOf('/', index - 1);
					normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
				} else {
					return normalized;
				}
			} while (true);
		}

		/**
		 * Deletes from file system the directory (with all its files and
		 * subdirectories) whose {@code path} is specified.
		 * <p>
		 * Elimina del sistema de archivos, el directorio (con todos sus archivos y
		 * subdirectorios) cuya ruta es especificada por {@code path}.
		 * </p>
		 *
		 * @param path
		 *            a string representing the directory's path
		 * @return a {@code boolean} indicating wheather the directory was eliminated or
		 *         not.
		 *
		 */
		public static boolean removeDirectory(String path) {
			try {
				File dir = new File(path);
				if (dir.exists() && dir.isDirectory()) {
					File[] listado = dir.listFiles();
					if (null != listado) {
						for (File fi : listado) {
							if (fi.isFile()) {
								Files.delete(fi.toPath());
							} else if (fi.isDirectory()) {
								String lpath = fi.getPath();
								if (removeDirectory(lpath)) {
									Files.delete(fi.toPath());
								}
							}
						}
					}
					Files.delete(dir.toPath());
					return true;
				}
			} catch (Exception e) {
				LOG.error("Can't recursively delete " + path, e);
			}
			return false;
		}

		/**
		 * @deprecated Use {@link #readFile(String)}
		 *
		 * Reads the file corresponding to the {@code path} specified.
		 * <p>
		 * Lee el archivo correspondiente a la ruta especificada por {@code path}.
		 * </p>
		 *
		 * @param path
		 *            a string representing the path of the file to read.
		 * @return a string with the file's content read, null if the file don't exist
		 *
		 */
		@Deprecated
		public static String getFileFromPath(String path) {
			return readFile(path);
		}

		/**
		 * Reads the file corresponding to the {@code path} specified.
		 * <p>
		 * Lee el archivo correspondiente a la ruta especificada por {@code path}.
		 * </p>
		 *
		 * @param path
		 *            a string representing the path of the file to read.
		 * @return a string with the file's content read, null if the file don't exist
		 *
		 */
		public static String readFile(String path) {
			String ret = null;
			File f = new File(path);
			if (f.exists()) {
				try (FileInputStream in = new FileInputStream(f)) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					copyStream(in, out);
					ret = out.toString();
				} catch (Exception e) {
					LOG.error("Can't read file:" + path, e);
				}
			}
			return ret;
		}

		/**
		 * Calculates the number of bytes occupied by the abstract pathname specified.
		 * If the pathname corresponds to a directory, the size of each file the
		 * directory contains its considered and added to the result.
		 * <p>
		 * Calcula el n&uacute;mero de bytes ocupados por la ruta abstracta
		 * especificada. Si la ruta corresponde a un directorio, se considera el
		 * tama&ntilde;o de cada archivo que contiene el directorio y se agrega al
		 * resultado.
		 * </p>
		 *
		 * @param path
		 *            a string representing the abstract pathname
		 * @return a {@code long} value representing (in bytes) the length of the file,
		 *         or the size of the directory represented by the pathname received. un
		 *         valor {@code long} que representa (en bytes) la longitud del archivo,
		 *         o el tama&ntilde;o del directorio representado por la ruta recibida.
		 * @throws NullPointerException
		 *             if the pathname received is {@code null}.
		 *             <p>
		 *             si la ruta recibida es {@code null}.
		 *             </p>
		 */
		public static long getFileSize(String path) {
			return getFileSize(new File(path));
		}

		/**
		 * Calculates the number of bytes occupied by the file or directory specified.
		 * If it's a directory, the size of each file the directory contains its
		 * considered and added to the result.
		 * <p>
		 * Calcula el n&uacute;mero de bytes ocupados por el archivo o directorio
		 * especificado. Si la ruta corresponde a un directorio, se considera el
		 * tama&ntilde;o de cada archivo que contiene el directorio y se agrega al
		 * resultado.
		 * </p>
		 *
		 * @param file
		 *            a file representing an abstract pathname
		 * @return a {@code long} value representing (in bytes) the length of the file,
		 *         or the size of the directory represented by the pathname received. un
		 *         valor {@code long} que representa (en bytes) la longitud del archivo,
		 *         o el tama&ntilde;o del directorio representado por la ruta recibida.
		 * @throws NullPointerException
		 *             if the {@code file} received is {@code null}.
		 *             <p>
		 *             si el objeto {@code file} recibido es {@code null}.
		 *             </p>
		 */
		public static long getFileSize(File file) {
			long ret = 0;
			if (file.isFile()) {
				ret = file.length();
			} else if (file.isDirectory()) {
				File [] files = file.listFiles();
				if (null != files) {
					for (File fi : files) {
						ret += getFileSize(fi);
					}
				}
			}
			return ret;
		}

		/**
		 * Creates a directory in the specified pathname.
		 * <p>
		 * Crea un directorio en el nombre de ruta especificado
		 * </p>
		 *
		 * @param path
		 *            a string representing the pathname in which the new directory will
		 *            reside. If this parameter's value is {@code null} the directory
		 *            won't be created.
		 * @return a {@code boolean} indicating wheather the directory was crated or
		 *         not.
		 *
		 */
		public static boolean createDirectory(String path) {
			try {
				File f = new File(path);
				if (!f.exists()) {
					f.mkdirs();
				}
				return true;
			} catch (Exception e) {
				LOG.error(e);
			}
			return false;
		}

		/**
		 * @deprecated Use {@link #copyDirectory(String, String)}
		 * Creates a copy of the given pathname's file system substructure. The origen
		 * pathname ({@code source}) must represent a directory's path in the file
		 * system.
		 * <p>
		 * Crea una copia de la subestructura del sistema de archivos de la ruta dada.
		 * La ruta de origen ({@code source}) debe representar la ruta de un directorio
		 * en el sistema de archivos.
		 * </p>
		 *
		 * @param source
		 *            pathname of the directory to copy. Must not be null.
		 * @param target
		 *            pathname of the new directory where file system's substructure
		 *            will be copied. Must not be null.
		 * @return a {@code boolean} indicating if the source directory was copied
		 *         succefully or not.
		 *
		 */
		@Deprecated
		public static boolean copyStructure(String source, String target) {
			return copyDirectory(source, target);
		}

		/**
		 * Creates a copy of the given pathname's file system substructure. The origen
		 * pathname ({@code source}) must represent a directory's path in the file
		 * system.
		 * <p>
		 * Crea una copia de la subestructura del sistema de archivos de la ruta dada.
		 * La ruta de origen ({@code source}) debe representar la ruta de un directorio
		 * en el sistema de archivos.
		 * </p>
		 *
		 * @param source
		 *            pathname of the directory to copy. Must not be null.
		 * @param target
		 *            pathname of the new directory where file system's substructure
		 *            will be copied. Must not be null.
		 * @return a {@code boolean} indicating if the source directory was copied
		 *         succefully or not.
		 *
		 */
		public static boolean copyDirectory(String source, String target) {
			try {
				copyDirectory(source, target, false, "", "");
				return true;
			} catch (Exception e) {
				LOG.error(e);
			}
			return false;
		}

		/**
		 * @deprecated Use {@link #copyDirectory(String, String, boolean, String, String)}
		 * Creates a copy of the given pathname's file system substructure and replaces
		 * a specified path located within the files of that substructure. The origen
		 * pathname ({@code source}) must represent a directory's path in the file
		 * system.
		 * <p>
		 * Crea una copia de la subestructura del sistema de archivos de la ruta dada y
		 * reemplaza una ruta especificada por {@code sourcePath} en el contenido de los
		 * archivos a copiar. La ruta de origen ({@code source}) debe representar la
		 * ruta de un directorio en el sistema de archivos.
		 * </p>
		 *
		 * @param source
		 *            pathname of the directory to copy. Must not be null.
		 * @param target
		 *            pathname of the new directory where file system's substructure
		 *            will be copied. Must not be null.
		 * @param changePath
		 *            a {@code boolean} that indicates if the files' content will be
		 *            parsed. Must not be null.
		 * @param sourcePath
		 *            a pathname string located in the source files to be replaced by
		 *            {@code targetPath}. Must not be null.
		 * @param targetPath
		 *            the pathname string to write in the target files in replacement of
		 *            {@code sourcePath}
		 * @return a {@code boolean} indicating if the source directory was copied
		 *         succefully or not.
		 *
		 */
		@Deprecated
		public static boolean copyStructure(String source, String target, boolean changePath, String sourcePath, String targetPath) {
			return copyDirectory(source, target, changePath, sourcePath, targetPath);
		}

		/**
		 * Creates a copy of the given pathname's file system substructure and replaces
		 * a specified path located within the files of that substructure. The origen
		 * pathname ({@code source}) must represent a directory's path in the file
		 * system.
		 * <p>
		 * Crea una copia de la subestructura del sistema de archivos de la ruta dada y
		 * reemplaza una ruta especificada por {@code sourcePath} en el contenido de los
		 * archivos a copiar. La ruta de origen ({@code source}) debe representar la
		 * ruta de un directorio en el sistema de archivos.
		 * </p>
		 *
		 * @param source
		 *            pathname of the directory to copy. Must not be null.
		 * @param target
		 *            pathname of the new directory where file system's substructure
		 *            will be copied. Must not be null.
		 * @param changePath
		 *            a {@code boolean} that indicates if the files' content will be
		 *            parsed. Must not be null.
		 * @param sourcePath
		 *            a pathname string located in the source files to be replaced by
		 *            {@code targetPath}. Must not be null.
		 * @param targetPath
		 *            the pathname string to write in the target files in replacement of
		 *            {@code sourcePath}
		 * @return a {@code boolean} indicating if the source directory was copied
		 *         succefully or not.
		 *
		 */
		public static boolean copyDirectory(String source, String target, boolean changePath, String sourcePath, String targetPath) {
			try {
				File ftarget = new File(target);
				if (!ftarget.exists()) {
					ftarget.mkdirs();
				}
				File dir = new File(source);
				if (dir.exists() && dir.isDirectory()) {
					File[] listado = dir.listFiles();
					for (int i = 0; i < listado.length; i++) {
						try {
							if (listado[i].isFile()) {
								File targetFile = new File(target + listado[i].getName());
								if (targetFile.length() == 0) {
									copyFile(source + listado[i].getName(), target + listado[i].getName(), changePath,
											sourcePath, targetPath);
								}
							}
							if (listado[i].isDirectory()) {
								String newpath = listado[i].getPath();
								File f = new File(target + listado[i].getName());
								f.mkdirs();
								boolean flag = copyDirectory(newpath + "/", target + listado[i].getName() + "/",
										changePath, sourcePath, targetPath);
								if (flag) {
									listado[i].delete();
								}
							}
						} catch (Exception e) {
							LOG.error(e);
							return false;
						}
					}
				}
			} catch (Exception e) {
				LOG.error(e);
				return false;
			}
			return true;
		}

		/**
		 * @deprecated Use {@link #copyFile(String, String, boolean, String, String)}
		 * Copies a file to another directory, modifying a path in its content. If
		 * indicated through {@code ChangePath} the string represented by
		 *
		 * @param sourceName
		 *            pathname of the file to copy. Must not be null.
		 * @param destName
		 *            pathname of the new location of the file to copy. Must not be
		 *            null.
		 * @param changePath
		 *            a {@code boolean} indicating if the file will be parsed to modify
		 *            its content
		 * @param sourcePath
		 *            a pathname string in the source file to be replaced
		 * @param targetPath
		 *            a pathname string that will replace {@code sourcePath}
		 * @throws IOException
		 *             Signals that an I/O exception has occurred. {@code sourcePath} is
		 *             looked for in the file's content and replaced by
		 *             {@code targetPath}, only if the file to copy has an extension
		 *             like: {@literal html}, {@literal html}, {@literal htm.orig} or
		 *             {@literal html.orig}.
		 *             <p>
		 *             Copia un archivo a otro directorio, modificando una ruta en su
		 *             contenido. Si se indica a trav&eacute;s de {@code ChangePath} el
		 *             objeto string representado en {@code sourcePath} se busca en el
		 *             contenido del archivo y se reemplaza por el representado en
		 *             {@code targetPath}, solo si el archivo a copiar tiene por
		 *             extensi&oacute;n: {@literal html}, {@literal html},
		 *             {@literal htm.orig} o {@literal html.orig}.
		 *             </p>
		 */
		@Deprecated
		public static void copy(String sourceName, String destName, boolean changePath, String sourcePath, String targetPath) throws IOException {
			copyFile(sourceName, destName, changePath, sourcePath, targetPath);
		}

		/**
		 * Copies a file to another directory, modifying a path in its content. If
		 * indicated through {@code ChangePath} the string represented by
		 *
		 * @param sourceName
		 *            pathname of the file to copy. Must not be null.
		 * @param destName
		 *            pathname of the new location of the file to copy. Must not be
		 *            null.
		 * @param changePath
		 *            a {@code boolean} indicating if the file will be parsed to modify
		 *            its content
		 * @param sourcePath
		 *            a pathname string in the source file to be replaced
		 * @param targetPath
		 *            a pathname string that will replace {@code sourcePath}
		 * @throws IOException
		 *             Signals that an I/O exception has occurred. {@code sourcePath} is
		 *             looked for in the file's content and replaced by
		 *             {@code targetPath}, only if the file to copy has an extension
		 *             like: {@literal html}, {@literal html}, {@literal htm.orig} or
		 *             {@literal html.orig}.
		 *             <p>
		 *             Copia un archivo a otro directorio, modificando una ruta en su
		 *             contenido. Si se indica a trav&eacute;s de {@code ChangePath} el
		 *             objeto string representado en {@code sourcePath} se busca en el
		 *             contenido del archivo y se reemplaza por el representado en
		 *             {@code targetPath}, solo si el archivo a copiar tiene por
		 *             extensi&oacute;n: {@literal html}, {@literal html},
		 *             {@literal htm.orig} o {@literal html.orig}.
		 *             </p>
		 */
		public static void copyFile(String sourceName, String destName, boolean changePath, String sourcePath, String targetPath) throws IOException {
			File sourceFile = new File(sourceName);
			File destinationFile = new File(destName);
			FileOutputStream destination;
			FileInputStream source = new FileInputStream(sourceFile);

			destination = new FileOutputStream(destinationFile);
			if (changePath && (sourceFile.getName().endsWith(".htm") || sourceFile.getName().endsWith(".html")
					|| sourceFile.getName().endsWith(".html.orig")
					|| sourceFile.getName().endsWith(".htm.orig"))) {
				String content = getStringFromInputStream(source);
				content = content.replaceAll(sourcePath, targetPath);
				copyStream(getStreamFromString(content), destination);
			} else {
				copyStream(source, destination);
			}
			destination.close();
		}

		/**
		 * Decodes the hexadecimal formatted string received and deserializes the result
		 * to create an object.
		 * <p>
		 * Decodifica el objeto string con formato hexadecimal recibido y deserealiza el
		 * resultado para crear un objeto.
		 * </p>
		 *
		 * @param txt
		 *            a hexadecimal formatted string representing an object's state
		 * @return a deserialized object
		 *
		 * @throws IOException
		 *             if an I/O error occurs while reading stream header
		 *             <p>
		 *             si un error de E/S ocurre mientras se lee el encabezado del
		 *             objeto stream
		 *             </p>
		 * @throws ClassNotFoundException
		 *             if the Class of a serialized object cannot be found
		 *             <p>
		 *             si la clase de un objeto serializado no puede encontrarse
		 *             </p>
		 */
		public static Object decodeObject(String txt) throws IOException, ClassNotFoundException {
			byte []arr = new byte[txt.length() / 2];
			for (int x = 0; x < txt.length(); x += 2) {
				String val = txt.substring(x, x + 2);
				int v = Integer.decode("0x" + val);
				if (v > 127) {
					v = v - 256;
				}
				arr[x / 2] = (byte) (v);
			}
			java.io.ObjectInputStream s = new java.io.ObjectInputStream(new ByteArrayInputStream(arr));
			return s.readObject();
		}

		/**
		 * Serializes the object received and generates a hexadecimal formatted string
		 * with the object's state.
		 * <p>
		 * Serializa el objeto recibido y genera un objeto string con el estado del
		 * objeto en formato hexadecimal.
		 * </p>
		 *
		 * @param obj
		 *            an object to serialize
		 * @return a hexadecimal formatted string representing the received object's
		 *         state un objeto string con formato hexadecimal que representa el
		 *         estado del objeto recibido.
		 * @throws IOException
		 *             if any I/O problem occurs while serializing the object.
		 *             <p>
		 *             si cualquier problema de E/S ocurre mientras se serializa el
		 *             objeto.
		 *             </p>
		 */
		public static String encodeObject(Object obj) throws IOException {
			ByteArrayOutputStream f = new ByteArrayOutputStream();
			ObjectOutput s = new ObjectOutputStream(f);
			s.writeObject(obj);
			s.flush();
			s.close();
			byte []arr = f.toByteArray();
			StringBuilder hex = new StringBuilder();
			for (int x = 0; x < arr.length; x++) {
				int v = arr[x];
				if (v < 0) {
					v = 256 + v;
				}
				String val = Integer.toHexString(v);
				if (val.length() == 1) {
					val = "0" + val;
				}
				hex.append(val);
			}
			return hex.toString();
		}

		/**
		 * Reads a file and stores the content in an array of bytes. <p>Lee un archivo y
		 * almacena el contenido en un arreglo de bytes</p>
		 *
		 * @param file the {@code file} to read. Must not be {@code null}
		 *
		 * @return an array of bytes that stores the content of the file specified.
		 * <p>un arreglo de bytes que almecena el contenido del archivo
		 * especificado.</p>
		 *
		 * @throws java.io.FileNotFoundException if the specified file does not exists.
		 * <p>si el archivo especificado no existe.</p>
		 *
		 * @throws java.io.IOException if an I/O error occurs while reading the file's
		 * content. <p>si un error de E/S ocurre durante la lectura del contenido del
		 * archivo.</p>
		 */
		public static byte[] readFile(File file) throws IOException {
			if (!file.exists()) {
				throw new FileNotFoundException();
			}

			try (FileInputStream in = new FileInputStream(file)) {
				int len = (int) file.length();

				byte[] bfile = new byte[len];
				int x = 0;
				int y = 0;
				while ((x = in.read(bfile, y, len - y)) > -1) {
					y += x;
					if (y == len) {
						break;
					}
				}
				return bfile;
			}
		}

		/**
		 * Extracts the files included in an HTTP request and stores them in the
		 * pathname indicated.
		 * <p>
		 * Extrae los archivos incluidos en una petici&oacute;n HTTP y los almacena en
		 * la ruta especificada.
		 * </p>
		 *
		 * @param request
		 *            an HTTP request that contains the files to store.
		 * @param destPath
		 *            the string representing the pathname where the files are to be
		 *            stored
		 * @return an iterator containing the file items detected in the HTTP request.
		 *         un objeto iterador que contiene los archivos detectados en la
		 *         petici&oacute;n HTTP.
		 */
		public static Iterator<FileItem> fileUpload(javax.servlet.http.HttpServletRequest request, String destPath) {
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload fu = new ServletFileUpload(factory);
			List<FileItem> items = null;

			try {
				items = fu.parseRequest(request);
			} catch (FileUploadException e) {
				LOG.error(e);
			}

			if (items != null && destPath == null) {
				return items.iterator();
			} else if (items != null) {
				Iterator<FileItem> iter = items.iterator();
				while (iter.hasNext()) {
					FileItem item = iter.next();
					// Si No es un campo de forma comun, es un campo tipo file, grabarlo
					if (!item.isFormField()) {
						File fichero = new File(destPath + item.getName());
						try {
							item.write(fichero);
						} catch (Exception e) {
							LOG.error(e);
						}
					}
				}
				return iter;
			}
			return null;
		}

		/**
		 * Logs the message indicated in {@code msg} into the {@code filePath} received.
		 * <p>
		 * Registra el mensaje indicado en {@code msg} dentro del archivo definido por
		 * la ruta recibida en {@code filePath}.
		 * </p>
		 *
		 * @param filePath
		 *            the file path
		 * @param msg
		 *            a string containing the message to register
		 * @throws java.io.IOException
		 *             if an I/O error occurs, which is possible because the
		 *             construction of the canonical pathname may require filesystem
		 *             queries or while writing the message into the file.
		 *             <p>
		 *             si un error de E/S ocurre, lo que es posible debido a que la
		 *             construcci&oacute;n de la ruta de archivo can&oacute;nica puede
		 *             requerir consultas del sistema de archivos o que ocurra durante
		 *             la escritura del mensaje en el archivo.
		 *             </p>
		 * @throws NullPointerException
		 *             if the pathname argument is {@code null}.
		 *             <p>
		 *             si el argumento de la ruta del archivo es {@code null}.
		 *             </p>
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		public static void log2File(String filePath, String msg) throws IOException {
			String path = null;
			int pos = filePath.lastIndexOf('/');
			if (pos > -1) {
				path = filePath.substring(0, pos - 1);
			}
			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
			}
			String logFile = new File(filePath).getCanonicalPath().replace('\\', '/');
			fileLogWriter = new PrintWriter(new FileWriter(logFile, true), true);
			fileLogWriter.println(msg);
		}

		/**
		 * Compress a directory's content into the zip output stream indicated.
		 * <p>
		 * Comprime el contenido de un directorio en el objeto zip output stream
		 * indicado.
		 * </p>
		 *
		 * @param directory
		 *            the {@code file} denoting the directory to compress. If it is
		 *            {@code null} or non-existent, no operation is done.
		 * @param base
		 *            the {@code file} denoting the directory base whose path is treated
		 *            as the base path for the files contained in the zip output stream.
		 * @param zos
		 *            the zip output stream to contain the directory's content
		 * @throws java.io.IOException
		 *             if there is any I/O error during: ZIP formatting, file inclusion
		 *             in the zip output stream, data writing in the zip output stream
		 *             or closure of the streams used to read or to write data.
		 *             <p>
		 *             si hay alg&uacute;n error de E/S durante: el formato en zip de la
		 *             informaci&oacute;n, la inclusi&oacute;n en el objeto {@code zip
		 * output stream}; o el cierre de los flujos utilizados para para leer o
		 *             escribir informaci&oacute;n.
		 *             </p>
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @author Jorge Jim&eacute;nez
		 */
		public static void zip(File directory, File base, ZipOutputStream zos) throws IOException {
			if (directory == null || !directory.exists()) {
				throw new FileNotFoundException();
			}
			byte[] buffer = new byte[bufferSize];
			int read;
			File[] files = directory.listFiles();

			if (null != files) {
				for (File fi : files) {
					if (fi.isDirectory()) {
						zip(fi, base, zos);
					} else {
						try (FileInputStream in = new FileInputStream(fi)) {
							ZipEntry entry = new ZipEntry(fi.getPath().substring(base.getPath().length() + 1));
							zos.putNextEntry(entry);
							while (-1 != (read = in.read(buffer))) {
								zos.write(buffer, 0, read);
							}
							zos.closeEntry();
						} catch (FileNotFoundException e) {
							LOG.error(e);
						}
					}
				}
			}
		}

		/**
		 * Checks whether a fine is UTF-8 encoded.
		 * @param file the file
		 * @return true, if the file is UTF-8 encoded.
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static boolean isUTF8(File file) throws IOException {
			int c3 = -61;
			byte[] buffer = new byte[8192];
			try (FileInputStream fin = new FileInputStream(file)) {
				int read = fin.read(buffer);
				while (read != -1) {
					for (int i = 0; i < read; i++) {
						if (buffer[i] == c3) {
							return true;
						}
					}
					read = fin.read(buffer);
				}
			}
			return false;
		}

		/**
		 * @deprecated Use {@link #addFilesToZip(File, File[])}
		 * Adds the files received to the specified zip file.
		 * <p>
		 * Agrega los archivos recibidos al archivo comprimido especificado.
		 * </p>
		 *
		 * @param zipFile
		 *            a compressed file to include some files at
		 * @param files
		 *            an array of files that will be added to {@code zipFile}
		 * @throws java.io.IOException
		 *             if some I/O error occurs during data reading or writing.
		 *             <p>
		 *             si alg&uacute;n error de E/S ocurre durante la lectura o
		 *             escritura de informaci&oacute;n.
		 *             </p>
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @author Jorge Jim&eacute;nez
		 */
		@Deprecated
		public static void addFilesToExistingZip(File zipFile, File[] files) throws IOException {
			addFilesToZip(zipFile, files);
		}

		/**
		 * Adds the files received to the specified zip file.
		 * <p>
		 * Agrega los archivos recibidos al archivo comprimido especificado.
		 * </p>
		 *
		 * @param zipFile
		 *            a compressed file to include some files at
		 * @param files
		 *            an array of files that will be added to {@code zipFile}
		 * @throws java.io.IOException
		 *             if some I/O error occurs during data reading or writing.
		 *             <p>
		 *             si alg&uacute;n error de E/S ocurre durante la lectura o
		 *             escritura de informaci&oacute;n.
		 *             </p>
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		public static void addFilesToZip(File zipFile, File[] files) throws IOException {
			// get a temp file
			// MAPS74 En Solaris no se vale renombrar un archivo hacia /var/tmp
			File tempFile = File.createTempFile(zipFile.getName(), null, zipFile.getParentFile());
			// delete it, otherwise you cannot rename your existing zip to it.
			tempFile.delete();

			boolean renameOk = zipFile.renameTo(tempFile);
			if (!renameOk) {
				throw new RuntimeException(
						"could not rename the file " + zipFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
			}
			byte[] buf = new byte[1024];

			ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

			ZipEntry entry = zin.getNextEntry();
			while (entry != null) {
				String name = entry.getName();
				boolean notInFiles = true;
				for (File f : files) {
					if (f.getName().equals(name)) {
						notInFiles = false;
						break;
					}
				}
				if (notInFiles) {
					// Add ZIP entry to output stream.
					out.putNextEntry(new ZipEntry(name));
					// Transfer bytes from the ZIP file to the output file
					int len;
					while ((len = zin.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
				}
				entry = zin.getNextEntry();
			}
			// Close the streams
			zin.close();
			// Compress the files
			for (int i = 0; i < files.length; i++) {
				InputStream in = new FileInputStream(files[i]);
				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(files[i].getName()));
				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				// Complete the entry
				out.closeEntry();
				in.close();
			}
			// Complete the ZIP file
			out.close();
			tempFile.delete();
		}

		/**
		 * Unzips a zipped file to a specific directory.
		 * <p>
		 * Descomprime un archivo comprimido en un directorio espec&iacute;fico.
		 * </p>
		 *
		 * @param zip
		 *            a zipped file
		 * @param destPath
		 *            a file that denotes a directory path
		 * @throws java.io.IOException
		 *             if an I/O error occurs during reading or writing data.
		 *             <p>
		 *             si un error ocurre durante la lectura o escritura de
		 *             informaci&oacute;n.
		 *             </p>
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @author Jorge Jim&eacute;nez
		 */
		public static void unzip(File zip, File destPath) throws IOException {
			unzip(zip, destPath, new ArrayList<>(), null, null);
		}

		/**
		 * Unzips a zipped file to a specific directory with the option of replacing a
		 * string in those files whose names end with specified extensions.
		 * <p>
		 * Descomprime un archivo comprimido a un directorio espec&iacute;fico con la
		 * opci&oacute;n de reemplazar una cadena en aquellos archivos cuyos nombres
		 * tengan las extensiones especificadas.
		 * </p>
		 *
		 * @param zip
		 *            the zipped file to unzip
		 * @param extractTo
		 *            the directory in which the file will be unzipped
		 * @param fext2parse
		 *            list of the file extensions to consider for replacing a string in
		 *            file's content
		 * @param parse
		 *            a string to look for in the file's content
		 * @param parse2
		 *            a string that will replace {@code parse} in file's content
		 * @throws java.io.IOException
		 *             if an I/O error occurs during reading or writing data.
		 *             <p>
		 *             si un error ocurre durante la lectura o escritura de
		 *             informaci&oacute;n.
		 *             </p>
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @author Jorge Jim&eacute;nez
		 */
		public static final void unzip(File zip, File destPath, ArrayList<String> fext2parse, String parse, String parse2) throws IOException {
			ZipFile archive = new ZipFile(zip);
			Enumeration e = archive.entries();
			while (e.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				// TODO:Pienso que con esto se soluciona el problema de creación de rutas en linux
				File file = new File(destPath, TEXT.replaceAll(entry.getName(), "\\", "/"));
				if (entry.isDirectory() && !file.exists()) {
					file.mkdirs();
				} else {
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					}

					InputStream in = archive.getInputStream(entry);
					// Parse file content
					if (!fext2parse.isEmpty()) {
						String ext = null;
						int pos = file.getName().lastIndexOf('.');
						if (pos > -1) {
							ext = file.getName().substring(pos + 1);
						}
						if (fext2parse.contains(ext)) {
							String content = getStringFromInputStream(in);
							content = content.replaceAll(parse, parse2);
							in = getStreamFromString(content);
						}
					}

					// Writes content
					try {
						FileOutputStream out = new FileOutputStream(file);
						copyStream(in, out);
						out.close();
						in.close();
					} catch (Exception ex) {
						LOG.debug(ex);
					}
				}
			}
			archive.close();
		}

		/**
		 * Reads a zipped file's content and returns all files in an
		 * {@link java.util.Iterator}
		 * <p>
		 * Lee el contenido de un archivo comprimido y regresa todos los archivos en un
		 * {@code java.util.Iterator}
		 * </p>
		 *
		 * @param zipName
		 *            a string representing the name of the zipped file to read
		 * @return an {@code java.util.Iterator} containing all the entries (files) in
		 *         the zipped file specified. un objeto {@code java.util.Iterator} que
		 *         contiene todas las entradas (archivos) contenidas en el archivo
		 *         comprimido.
		 */
		public static Iterator<ZipEntry> readZip(String zipName) {
			List<ZipEntry> itFiles = new ArrayList<>();
			try (ZipFile zf = new ZipFile(zipName)) {
				Enumeration enu = zf.entries();
				while (enu.hasMoreElements()) {
					ZipEntry zen = (ZipEntry) enu.nextElement();
					if (!zen.isDirectory()) {
						itFiles.add(zen);
					}
				}
			} catch (Exception ex) {
				LOG.error(ex); // MAPS74
			}
			return itFiles.iterator();
		}

		/**
		 * Reads a specific file contained in a zipped file.
		 * <p>
		 * Lee un archivo espec&iacute;fico contenido en un archivo comprimido.
		 * </p>
		 *
		 * @param zipName
		 *            a string representing the pathname of a zipped file
		 * @param file2Read
		 *            a string representing the pathname of a file inside the zipped
		 *            file
		 * @return a string with the specified file's content.
		 *
		 * @author Jorge Jim&eacute;nez
		 */
		public static String readFileFromZipAsString(String zipName, String file2Read) {
			String content = null;
			try (ZipFile zip = new ZipFile(new File(zipName))) {
				for (Enumeration e = zip.entries(); e.hasMoreElements();) {
					ZipEntry entry = (ZipEntry) e.nextElement();
					if (entry.getName() != null && entry.getName().equals(file2Read)) {
						InputStream is = zip.getInputStream(entry);
						content = getStringFromInputStream(is);
					}
				}
			} catch (Exception e) {
				LOG.debug(e);
			}
			return content;
		}
	}

	/**
	 * Supplies several functions commonly used for sending e-mails.
	 * <p>
	 * Provee varias funciones para env&iacute;o de correos electr&oacute;nicos
	 * utilizadas com&uacute;nmente.
	 * </p>
	 *
	 * @author Jorge Jim&eacute;nez
	 */
	public static class EMAIL {

		private EMAIL () {}

		/**
		 * The smtpssl.
		 */
		private static boolean smtpssl = false;
		/**
		 * Represents the domain name or IP addres for the SMTP server to use.
		 * <p>
		 * Reresenta el nombre de dominio o direcci&oacute;n IP del servidor SMTP a
		 * usar.
		 * </p>
		 */
		private static String smtpserver = null;
		/**
		 * Represents the portof the IP addres for the SMTP server to use.
		 * <p>
		 * Reresenta el puerto de la IP del servidor SMTP a usar.
		 * </p>
		 */
		private static int smtpport = 0;

		/**
		 * The smtptls.
		 */
		private static boolean smtptls = false;
		/**
		 * Represents the user name registered in the SMTP server for sending messages.
		 * <p>
		 * Representa el nombre de usuario registrado en el servidor SMTP para enviar
		 * mensajes.
		 * </p>
		 */
		private static String smtpuser = null;
		/**
		 * Represents the user's password registered in the SMTP server for sending
		 * messages.
		 * <p>
		 * Representa la contrase&ntilde;a del usuario registrada en el servidor SMTP
		 * para enviar mensajes.
		 * </p>
		 */
		private static String smtppassword = null;
		/**
		 * Represents the e-mail account designated to receive e-mail communications.
		 * <p>
		 * Representa la cuenta de correo designada para recibir las comunicaciones por
		 * correo electr&oacute;nico.
		 * </p>
		 */
		private static String adminEmail = null;

		/**
		 * Sets the sMTP ssl.
		 *
		 * @param val
		 *            the new sMTP ssl
		 */
		public static void setSMTPSsl(boolean val) {
			smtpssl = val;
		}

		/**
		 * Sets the domain name or IP address for the SMTP server to use.
		 * <p>
		 * Fija el nombre de dominio o la direcci&oacute;n IP del servidor SMTP a usar.
		 * </p>
		 *
		 * @param val
		 *            a string representing the domain name or the IP address for the
		 *            SMTP server.
		 *            <p>
		 *            un objeto string que representa el nombre de dominio o la
		 *            direcci&oacute;n IP del servidor SMTP.
		 *            </p>
		 */
		public static void setSMTPServer(String val) {
			smtpserver = val;
		}

		/**
		 * Sets the sMTP port.
		 *
		 * @param port
		 *            the new sMTP port
		 */
		public static void setSMTPPort(int port) {
			smtpport = port;
		}

		/**
		 * Sets the sMTP tls.
		 *
		 * @param tls
		 *            the new sMTP tls
		 */
		public static void setSMTPTls(boolean tls) {
			smtptls = tls;
		}

		/**
		 * Gets the domain name or IP address for the SMTP server in use.
		 * <p>
		 * Obtiene el nombre de dominio o la direcci&oacute;n IP del servidor SMTP en
		 * uso.
		 * </p>
		 *
		 * @return a string representing the domain name or IP address for the SMTP
		 *         server in use. un objeto string que representa el nombre de dominio o
		 *         la direcci&oacute;n IP del servidor SMTP en uso.
		 */
		public static String getSMTPServer() {
			return smtpserver;
		}

		/**
		 * Sets an e-mail account to generate e-mail messages.
		 * <p>
		 * Fija una cuenta de correo para generar mensajes de correo electr&oacute;nico.
		 * </p>
		 *
		 * @param val
		 *            a string representing a valid e-mail account.
		 *            <p>
		 *            un objeto string que representa una cuenta de correo
		 *            electr&oacute;nico v&aacute;lida.
		 *            </p>
		 */
		public static void setAdminEmail(String val) {
			adminEmail = val;
		}

		public static String getAdminEmail() {
			return adminEmail;
		}

		/**
		 * @deprecated Use {@link #sendMail(SWBMail)}
         * Sends an e-mail with the information supplied. The e-mail body can be
		 * formatted as HTML or plain text.
		 * <p>
		 * Env&iacute;a un correo electr&oacute;nico con la informaci&oacute;n
		 * proporcionada. El cuerpo del correo puede ser enviado en formato HTML o en
		 * texto plano.
		 * </p>
		 *
		 * @param senderEmail
		 *            a string representing the sender's e-mail account. Must be a valid
		 *            e-mail account, otherwise the mail will not be sent.
		 * @param senderName
		 *            a string representing the sender's name
		 * @param recipients
		 *            a collection of the recipients' e-mail accounts. Every element in
		 *            the collection is expected to be a valid
		 *            {@link InternetAddress}. Must not be null,
		 *            otherwise the mail will not be sent.
		 * @param ccRecipients
		 *            a collection of e-mail accounts to send the email as a copy. Every
		 *            element in the collection is expected to be a valid
		 *            {@literal java.mail.internet.InternetAddress}. If it is
		 *            {@code null}, the mail won't be sent as a carbon copy to anyone.
		 * @param bccRecipients
		 *            a collection of e-mail accounts to send the email as a blind
		 *            carbon copy. Every element in the collection is expected to be a
		 *            valid {@literal java.mail.internet.InternetAddress}. If it is
		 *            {@code null}, the mail won't be sent as a blind carbon copy to
		 *            anyone.
		 * @param subject
		 *            a string indicating the e-mail's subject
		 * @param contentType
		 *            a string indicating the content type of the mail. {@literal HTML}
		 *            indicates the body has an HTML format, otherwise it will be send
		 *            in text plain format. Must not be {@code null}.
		 * @param body
		 *            a string containing the e-mail body's text
		 * @param login
		 *            a string representing a login name for SMTP server authentication.
		 *            If it is {@code null} authentication will not be performed.
		 * @param password
		 *            a string representing a password for SMTP server authentication.
		 *            If it is {@code null} authentication will not be performed.
		 * @param attachments
		 *            a list containing all the attachments for the e-mail. Every
		 *            element in the collection is expected to be of type
		 *            {@literal org.apache.commons.mail.EmailAttachment}.
		 * @return the message id of the underlying MimeMessage. See {@link HtmlEmail#send()}
		 */
		@Deprecated
		public static String sendMail(String senderEmail, String senderName, Collection<InternetAddress> recipients,
				Collection<InternetAddress> ccRecipients, Collection<InternetAddress> bccRecipients, String subject,
				String contentType, String body, String login, String password, List<EmailAttachment> attachments) {

		    SWBMail mail = new SWBMail();
		    mail.setSenderEmail(senderEmail);
		    mail.setSenderName(senderName);
		    mail.setRecipients(recipients);
		    mail.setCcRecipients(ccRecipients);
		    mail.setBccRecipients(bccRecipients);
		    mail.setSubject(subject);
		    mail.setContentType(contentType);
		    mail.setMessage(body);
		    mail.setLogin(login);
		    mail.setPassword(password);
		    mail.setAttachments(attachments);

			return sendMail(mail);
		}

		/**
		 * Sends an e-mail with the information supplied through {@code message}. The
		 * e-mail body can be formatted as HTML or plain text.
		 * <p>
		 * Env&iacute;a un correo electr&oacute;nico con la informaci&oacute;n
		 * proporcionada a trav&eacute;s de {@code message}. El cuerpo del correo puede
		 * ser enviado en formato HTML o en texto plano.
		 * </p>
		 *
		 * @param message
		 *            an object of type {@link org.semanticwb.base.util.SWBMail}
		 * @return a string that at the moment of writing this documentation is equal to
		 *         {@code null}. un objeto string que al momento de escribir esta
		 *         documentaci&oacute;n es igual a {@code null}.
		 * @throws SocketException
		 *             the socket exception
		 */
		public static String sendMail(SWBMail message) {
            try {
                HtmlEmail email = new HtmlEmail();
                email.setHostName(smtpserver);
                email.setSSLOnConnect(smtpssl);
                email.setStartTLSEnabled(smtptls);
                email.setSSLCheckServerIdentity(smtpssl || smtptls);

                if (message.getAttachments() != null) {
                    for (EmailAttachment attachment: message.getAttachments()) {
                        email.attach(attachment);
                    }
                }

                if (smtpport > 0) {
                    if (smtpssl) {
                        email.setSslSmtpPort(Integer.toString(smtpport));
                    }
                    email.setSmtpPort(smtpport);
                }

                email.setFrom(message.getSenderEmail(), message.getSenderName());
                email.setTo(message.getRecipients());

                if (message.getCcRecipients() != null) {
                    email.setCc(message.getCcRecipients());
                }

                if (message.getBccRecipients() != null) {
                    email.setBcc(message.getBccRecipients());
                }
                email.setSubject(message.getSubject());

                if (message.getContentType() != null && message.getContentType().toLowerCase().contains("html")) {
                    email.setHtmlMsg(message.getMessage()); // set the html message
                } else {
                    email.setMsg(message.getMessage());
                }

                // Set authentication default to config, as in sendBGEmail method
                if (null != smtpuser && null != smtppassword) {
                    email.setAuthenticator(new DefaultAuthenticator(smtpuser, smtppassword));
                }

                if (message.getLogin() != null && message.getPassword() != null) {
                    email.setAuthenticator(new DefaultAuthenticator(message.getLogin(), message.getPassword()));
                }

                return email.send();
            } catch (Exception e) {
                LOG.error(e);
            }
            return null;
		}

		/**
		 * Sends an e-mail to the account in {@code EMAIL.adminEmail}, requiring minimal
		 * information to be supplied.
		 * <p>
		 * Env&iacute;a un correo electr&oacute;nico a la cuenta de
		 * {@code EMAIL.adminEmail}, requiriendo la informaci&oacute;n m&iacute;nima.
		 * </p>
		 *
		 * @param recipient
		 *            toEmail a string representing the repicients' e-mail accounts.
		 *            This string can contain more than one e-mail accounts,
		 *            semicolon-separated. If it is {@code null} the e-mail will not be
		 *            sent.
		 * @param subject
		 *            a string representing the message's subject
		 * @param msg
		 *            a string containing the message's body
		 * @return by the time this documentation comments were included, this value is
		 *         always {@code null}. para cuando estos comentarios de
		 *         documentaci&oacute;n fueron incluidos, este valor siempre es
		 *         {@code null}.
		 * @throws java.net.SocketException
		 *             is never thrown
		 * @throws SocketException
		 *             the socket exception
		 */
		public static String sendMail(String recipient, String subject, String msg) {
			if (null != recipient && null != subject && null != msg) {
				List<InternetAddress> recipients = parseAddressList(recipient);

				SWBMail mail = new SWBMail();
				mail.setSenderEmail(adminEmail);
				mail.setSenderName("");
				mail.setRecipients(recipients);
				mail.setSubject(subject);
				mail.setMessage(msg);
				mail.setLogin(smtpuser);
				mail.setPassword(smtppassword);

				return sendMail(mail);
			}

			return null;
		}

		/**
		 * Sends an e-mail in background mode with the sender address as the one set in
		 * {@code EMAIL.adminEmail}.
		 * <p>
		 * Env&iacute;a un correo electr&oacute;nico en segundo plano con la cuenta de
		 * correo del remitente como la asignada a {@code EMAIL.adminEmail}.
		 * </p>
		 *
		 * @param recipient
		 *            a string representing the repicients' e-mail accounts. This string
		 *            can contain more than one e-mail accounts, semicolon-separated. If
		 *            it is {@code null} the e-mail will not be sent.
		 * @param subject
		 *            a string representing the message's subject
		 * @param body
		 *            a string containing the message's body
		 * @throws java.net.SocketException
		 *             if an error occurs while creating the mail sender.
		 *             <p>
		 *             si ocurre alg&uacute;n error en la creaci&oacute;n del remitente
		 *             de correos.
		 *             </p>
		 */
		public static void sendBGEmail(String recipient, String subject, String body) {
			List<InternetAddress> recipients = parseAddressList(recipient);

			SWBMail mail = new SWBMail();
			mail.setSenderEmail(adminEmail);
			mail.setRecipients(recipients);
			mail.setSubject(subject);
			mail.setMessage(body);
			mail.setLogin(smtpuser);
			mail.setPassword(smtppassword);

			sendBGEmail(mail);
		}

		/**
		 * @deprecated Use {@link #sendBGEmail(SWBMail)}
         * Sends an e-mail in background mode with the information supplied.
		 * <p>
		 * Env&iacute;a un correo electr&oacute;nico en segundo plano con la
		 * informaci&oacute;n proporcionada.
		 * </p>
		 *
		 * @param senderEmail
		 *            a string representing the sender's e-mail account. Must be a valid
		 *            e-mail account, if it is equal to {@code null}, the value of
		 *            {@code EMAIL.adminEmail} will be used.
		 * @param senderName
		 *            a string representing the sender's name
		 * @param recipients
		 *            a collection of the recipients' e-mail accounts. Every element in
		 *            the collection is expected to be a valid
		 *            {@link InternetAddress}. Must not be null,
		 *            otherwise the mail will not be sent.
		 * @param ccRecipients
		 *            a collection of e-mail accounts to send the email as a copy. Every
		 *            element in the collection is expected to be a valid
		 *            {@literal java.mail.internet.InternetAddress}. If it is
		 *            {@code null}, the mail won't be sent as a carbon copy to anyone.
		 * @param bccRecipients
		 *            a collection of e-mail accounts to send the email as a blind
		 *            carbon copy. Every element in the collection is expected to be a
		 *            valid {@literal java.mail.internet.InternetAddress}. If it is
		 *            {@code null}, the mail won't be sent as a blind carbon copy to
		 *            anyone.
		 * @param subject
		 *            a string indicating the e-mail's subject
		 * @param contentType
		 *            a string indicating the content type of the mail. {@literal HTML}
		 *            indicates the body has an HTML format, otherwise it will be send
		 *            in text plain format. Must not be {@code null}.
		 * @param body
		 *            a string containing the e-mail body's text
		 * @param login
		 *            a string representing a login name for SMTP server authentication.
		 *            If it is {@code null} authentication will not be performed.
		 * @param password
		 *            a string representing a password for SMTP server authentication.
		 *            If it is {@code null} authentication will not be performed.
		 * @param attachments
		 *            a list containing all the attachments for the e-mail. Every
		 *            element in the collection is expected to be of type
		 *            {@literal org.apache.commons.mail.EmailAttachment}.
		 * @throws java.net.SocketException
		 *             if an error occurs during new thread's creation for working in
		 *             background mode.
		 *             <p>
		 *             si ocurre un error durante la creaci&oacute;n del nuevo thread
		 *             para trabajar en segundo plano.
		 *             </p>
		 */
		@Deprecated
		public static void sendBGEmail(String senderEmail, String senderName, Collection<InternetAddress> recipients,
				Collection<InternetAddress> ccRecipients, Collection<InternetAddress> bccRecipients, String subject,
				String contentType, String body, String login, String password, List<EmailAttachment> attachments) {

			SWBMail message = new SWBMail();
			message.setContentType(null == contentType ? "text/html" : contentType);

			if (senderEmail == null && adminEmail != null) {
				senderEmail = adminEmail;
			}

			if (senderEmail != null) {
				message.setSenderEmail(senderEmail);
			}

			if (senderName != null) {
				message.setSenderName(senderName);
			}

			if (recipients != null) {
				message.setRecipients(recipients);
			}

			if (ccRecipients!= null) {
				message.setCcRecipients(ccRecipients);
			}

			if (bccRecipients != null) {
				message.setBccRecipients(bccRecipients);
			}

			if (subject != null) {
				message.setSubject(subject);
			}

			if (body != null) {
				message.setMessage(body);
			}

			if (smtpuser != null) {
				message.setLogin(smtpuser);
			}

			if (login != null) {
				message.setLogin(login);
			}

			if (smtppassword != null) {
				message.setPassword(smtppassword);
			}

			if (password != null) {
				message.setPassword(password);
			}

			if (attachments != null) {
				message.setAttachments(attachments);
			}

			sendBGEmail(message);
		}

		/**
		 * Parses a semi-colon separated list of e-mail addresses. No address validation is performed.
		 * @param addressList semi-colon separated list of e-mail addresses.
		 * @return List of parsed {@link InternetAddress} objects.
		 */
		private static List<InternetAddress> parseAddressList(String addressList) {
			ArrayList<InternetAddress> recipients = new ArrayList<>();

			if (null != addressList && !addressList.isEmpty()) {
				String[] addresses;
				if (addressList.contains(";")) {
					addresses = addressList.split(";");
					for (int i = 0; i < addresses.length; i++) {
						addresses[i] = addresses[i].trim();
					}
				} else {
					addresses = new String[1];
					addresses[0] = addressList.trim();
				}

				for (String address : addresses) {
					InternetAddress iaddress = new InternetAddress();
					iaddress.setAddress(address);
					recipients.add(iaddress);
				}
			}

			return recipients;
		}

		/**
		 * Sends an e-mail in background mode with the information supplied through
		 * {@code message}.
		 * <p>
		 * Env&iacute;a un correo electr&oacute;nico en segundo plano con la
		 * informaci&oacute;n proporcionada a trav&eacute;s de {@code message}.
		 * </p>
		 *
		 * @param message
		 *            an object of type {@link org.semanticwb.base.util.SWBMail}
		 *
		 */
		public static void sendBGEmail(SWBMail message) {
			SWBMailSender swbMailSender = new SWBMailSender();
			swbMailSender.addEMail(message);
			swbMailSender.start();
		}

		/**
		 * Sets the value for the user's name registered in the SMTP server for sending
		 * messages.
		 * <p>
		 * Asigna el valor del nombre de usuario registrado en el servidor SMTP para
		 * enviar mensajes.
		 * </p>
		 *
		 * @param val
		 *            a string representing a user's name registered in the SMTP server.
		 */
		public static void setSMTPUser(String val) {
			smtpuser = val;
		}

		/**
		 * Sets the value for the user's password registered in the SMTP server for
		 * sending messages.
		 * <p>
		 * Asigna el valor de la contrase&ntilde;a de usuario registrado en el servidor
		 * SMTP para enviar mensajes.
		 * </p>
		 *
		 * @param val
		 *            a string representing a user's password registered in the SMTP
		 *            server.
		 */
		public static void setSMTPPassword(String val) {
			smtppassword = val;
		}

		/**
		 * Checks if is valid email address.
		 *
		 * @param emailAddress
		 *            the email address
		 * @return true, if is valid email address
		 */
		public static boolean isValidEmailAddress(String emailAddress) {
			String expression = "^[\\w]([\\.\\w\\-])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";

			CharSequence inputStr = emailAddress;
			Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(inputStr);
			return matcher.matches();
		}
	}

	/**
	 * Provides several common operations to perform processing of DOM documents and
	 * their contents.
	 * <p>
	 * Provee varias operaciones comunes que involucran documentos DOM y su
	 * contenido.
	 * </p>
	 */
	public static class XML {
		//TODO: Review class code because several NPE are not cached

		/**
		 * The only one instance of this object for the entire application.
		 * <p>
		 * La &uacute;nica instancia de este objeto para toda la aplicaci&oacute;n.
		 * </p>
		 */
		private static XML mXml = null;

		private final ThreadLocal<DocumentBuilderFactory> mDbf = new ThreadLocal<DocumentBuilderFactory>() {
			@Override
			public DocumentBuilderFactory initialValue() {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				dbf.setNamespaceAware(true);
				dbf.setIgnoringElementContentWhitespace(true);
				return dbf;
			}
		};

		/**
		 * Creator of objects of type {@link javax.xml.transform.Transformer} and
		 * {@link javax.xml.transform.Templates}.
		 * <p>
		 * Creador de objetos de tipo {@link javax.xml.transform.Transformer} y
		 * {@link javax.xml.transform.Templates}.
		 * </p>
		 */
		private TransformerFactory mTFactory = null; // 1. Instantiate an XPathFactory.
		/**
		 * Creator of {@link javax.xml.xpath.XPath} objects.
		 * <p>
		 * Creador de objetos {@code javax.xml.xpath.XPath}
		 * </p>
		 */
		private XPathFactory xpathFactory = null;
		/**
		 * Evaluator of XPath expressions.
		 * <p>
		 * Evaluador de expresiones XPath.
		 * </p>
		 */
		private XPath xpathObj = null;

		/**
		 * Gets the factory object of {@code javax.xml.xpath.XPath} objects.
		 * <p>
		 * Obtiene el objeto f&aacute;brica de objetos {@code javax.xml.xpath.XPath}.
		 * </p>
		 *
		 * @return the factory object of {@code javax.xml.xpath.XPath} objects.
		 *
		 */
		public static XPathFactory getXPathFactory() {
			XML xml = getInstance();
			return xml.xpathFactory;
		}

		/**
		 * Gets the {@code XPath} object of this object.
		 * <p>
		 * Obtiene el objeto {@code XPath} de este objeto.
		 * </p>
		 *
		 * @return the {@code XPath} object of this object.
		 *
		 */
		public static XPath getXPathObject() {
			XML xml = getInstance();
			return xml.xpathObj;
		}

		/**
		 * Gets a reference for the only one instance of this object.
		 * <p>
		 * Obtiene una referencia para la &uacute;nica instancia de este objeto.
		 * </p>
		 *
		 * @return a reference for the only one instance of this object.
		 *
		 */
		private static XML getInstance() {
			if (SWBUtils.XML.mXml == null) {
				SWBUtils.XML.mXml = new XML();
			}
			return SWBUtils.XML.mXml;
		}

		/**
		 * Gets this object's document builder factory.
		 * <p>
		 * Obtiene la f&aacute;brica constructora de documentos de este objeto.
		 * </p>
		 *
		 * @return this object's document builder factory. la f&aacute;brica
		 *         constructora de documentos de este objeto.
		 */
		public static DocumentBuilderFactory getDocumentBuilderFactory() {
			XML xml = getInstance();
			return xml.mDbf.get();
		}

		/**
		 * Gets this object's transformer factory.
		 * <p>
		 * Obtiene la f&aacute;brica transformadora de este objeto.
		 * </p>
		 *
		 * @return this object's transformer factory.
		 *
		 */
		public static TransformerFactory getTransformerFactory() {
			XML xml = getInstance();
			return xml.mTFactory;
		}

		/**
		 * Creates an instance of this object. The instance's document builder factory
		 * will provide support for XML namespaces, and its parsers created must
		 * eliminate whitespace in element content.
		 * <p>
		 * Crea una instancia de este objeto. La f&aacute;brica constructora de
		 * documentos de esta instancia proveer&aacute; de soporte para namespaces de
		 * XML, y los analizadores sint&aacute;cticos que cree deber&aacute;n eliminar
		 * los espacios en blanco del contenido de los elementos.
		 * </p>
		 */
		private XML() {
			try {
				xpathFactory = javax.xml.xpath.XPathFactory.newInstance();
				xpathObj = xpathFactory.newXPath();

			} catch (Exception e) {
				LOG.error("Error getting DocumentBuilderFactory...", e);
			}

			try {
				mTFactory = TransformerFactory.newInstance();
			} catch (Exception e) {
				LOG.error("Error getting TransformerFactory...", e);
			}
		}

		/**
		 * Transforms a DOM document into a XML formatted string.
		 * <p>
		 * Transforma un documento DOM en un objeto string con formato XML.
		 * </p>
		 *
		 * @param dom
		 *            a DOM document to transform. Must not be {@code null}.
		 * @param encode
		 *            a string representing the preferred character encoding to use in
		 *            the transformation. Must not be {@code null}.
		 * @param indent
		 *            a {@code boolean} indicating wheather or not to indent (by 2 blank
		 *            spaces) the XML to generate.
		 * @return a string representing the DOM document received.
		 *
		 */
		public static String domToXml(Document dom, String encode, boolean indent) {
			String res = null;
			ByteArrayOutputStream sw = new java.io.ByteArrayOutputStream();
			OutputStreamWriter osw = null;
			try {
				osw = new java.io.OutputStreamWriter(sw, encode);
				StreamResult streamResult = new StreamResult(osw);
				TransformerFactory tFactory = getTransformerFactory();
				Transformer transformer = null;
				synchronized (tFactory) {
					transformer = tFactory.newTransformer();
				}
				transformer.setOutputProperty(OutputKeys.ENCODING, encode);
				if (indent) {
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
					try {
						transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
					} catch (Exception noe) {/* No soportado en algunos xerses */

					}
				}
				transformer.setOutputProperty(OutputKeys.METHOD, "xml");
				transformer.transform(new DOMSource(dom), streamResult);
				res = sw.toString();
			} catch (Exception e) {
				LOG.error(e);
			}
			return res;
		}

		/**
		 * Transforms a DOM document into a XML formatted string using the UTF-8
		 * character encoding with no indentation.
		 * <p>
		 * Transforma un documento DOM en un objeto string con formato XML utilizando
		 * codificaci&oacute;n UTF-8 sin sangr&iacute;as.
		 * </p>
		 *
		 * @param dom
		 *            a DOM document to transform. Must not be {@code null}.
		 * @return a XML formatted, UTF-8 encodedstring representing the DOM document
		 *         received.
		 *
		 */
		public static String domToXml(Document dom) {
			return domToXml(dom, "UTF-8", false);
		}

		/**
		 * Transforms a DOM document into a XML formatted string using the UTF-8
		 * character encoding with or whithout indentation.
		 * <p>
		 * Transforma un documento DOM en un objeto string con formato XML utilizando
		 * codificaci&oacute;n UTF-8 con o sin sangr&iacute;as.
		 * </p>
		 *
		 * @param dom
		 *            a DOM document to transform. Must not be {@code null}.
		 * @param ident
		 *            a {@code boolean} indicating wheather or not to indent (by 2 blank
		 *            spaces) the XML to generate.
		 * @return a XML formatted, UTF-8 encodedstring representing the DOM document
		 *         received.
		 *
		 */
		public static String domToXml(Document dom, boolean ident) {
			return domToXml(dom, "UTF-8", ident);
		}

		/**
		 * Test:Jorge Jiménez Method that transforms Node to String.
		 *
		 * @param node
		 *            the node
		 * @return the string
		 * @return
		 */
		public static String nodeToString(Node node) {
			try {
				Source source = new DOMSource(node);
				StringWriter stringWriter = new StringWriter();
				Result result = new StreamResult(stringWriter);
				TransformerFactory factory = TransformerFactory.newInstance();
				Transformer transformer = factory.newTransformer();
				transformer.transform(source, result);
				return stringWriter.getBuffer().toString();
			} catch (TransformerException e) {
				LOG.error(e);
			}
			return null;
		}

		/**
		 * Creates an exact copy of the document received.
		 * <p>
		 * Crea una copia exacta del objeto document recibido.
		 * </p>
		 *
		 * @param dom
		 *            a DOM document to copy.
		 * @return a document with the same content as the document received. un objeto
		 *         document con el mismo contenido que el objeto document recibido.
		 * @throws org.w3c.dom.DOMException
		 *             if an exceptional circumstance occurs while DOM operations are
		 *             performed.
		 *             <p>
		 *             si una circunstancia excepcional ocurre mientras se ejecutan
		 *             operaciones DOM.
		 *             </p>
		 *
		 */
		public static Document copyDom(Document dom) {
			Document n = getNewDocument();
			if (dom != null && dom.hasChildNodes()) {
				Node node = n.importNode(dom.getFirstChild(), true);
				n.appendChild(node);
			}
			return n;
		}

		/**
		 * Creates a document object from the string received.
		 * <p>
		 * Crea un objeto document a partir del objeto string recibido.
		 * </p>
		 *
		 * @param xml
		 *            a string representing the content for a DOM document.
		 * @return a document created with the received string's content. un objeto
		 *         document creado con el contenido del objeto string recibido.
		 */
		public static Document xmlToDom(String xml) {
			if (xml == null || xml.length() == 0) {
				return null;
			}
			Document dom = null;
			try {
				ByteArrayInputStream sr = new java.io.ByteArrayInputStream(xml.getBytes());
				dom = xmlToDom(sr);
			} catch (Exception e) {
				LOG.error(e);
			}
			return dom;
		}

		/**
		 * Creates a document object from the input stream received.
		 * <p>
		 * Crea un objeto document a partir de un flujo de entrada recibido.
		 * </p>
		 *
		 * @param xml
		 *            an input stream from which the DOM document will be created.
		 * @return a document containing the data provided by the input stream received.
		 *         un objeto document que contiene la informaci&oacute;n proporcionada
		 *         por el flujo de entrada recibido.
		 */
		public static Document xmlToDom(InputStream xml) {
			Document dom = null;
			try {
				dom = xmlToDom(new InputSource(xml));
			} catch (Exception e) {
				LOG.error(e);
			}
			return dom;
		}

		/**
		 * Creates a document from the input source received.
		 * <p>
		 * Crea un objeto document a partir de la fuente de entrada recibida.
		 * </p>
		 *
		 * @param xml
		 *            an input source from which the DOM document will be created.
		 * @return a document containing the data provided by the input source received.
		 *         un objeto document que contiene la informaci&oacute;n proporcionada
		 *         por la fuente de entrada recibida.
		 */
		public static Document xmlToDom(InputSource xml) {
			DocumentBuilderFactory dbf;
			DocumentBuilder db;
			Document dom = null;

			try {
				dbf = getDocumentBuilderFactory();
				synchronized (dbf) {
					db = dbf.newDocumentBuilder();
				}
				if (xml != null) {
					dom = db.parse(xml);
					// MAPS74 si no se puede copiar se deberia avisar...
					dom = copyDom(dom);
				}
			} catch (Exception e) {
				LOG.error(e);
			}
			return dom;
		}

		/**
		 * Creates a new DOM document object.
		 * <p>
		 * Crea un nuevo objeto document.
		 * </p>
		 *
		 * @return a brand new (empty) DOM document.
		 *
		 */
		public static Document getNewDocument() {
			DocumentBuilderFactory dbf = getDocumentBuilderFactory();
			DocumentBuilder db;
			Document dom = null;
			try {
				synchronized (dbf) {
					db = dbf.newDocumentBuilder();
				}
				dom = db.newDocument();
			} catch (Exception e) {
				LOG.error(e);
			}
			return dom;
		}

		/**
		 * Generates a template from the input stream received whose content represents
		 * a XSLT.
		 * <p>
		 * Genera un objeto template con el contenido del flujo de entrada recibido, que
		 * representa el c&oacute;digo de un XSLT.
		 * </p>
		 *
		 * @param stream
		 *            an input stream with an XSLT file's content
		 * @return a template which is a compiled representation of the input source's
		 *         content. un objeto template que es una representaci&oacute;n
		 *         compilada del contenido del flujo de entrada recibido.
		 * @throws javax.xml.transform.TransformerConfigurationException
		 *             if it fails the construction of the template during parsing.
		 *             <p>
		 *             si falla la construcci&oacute;n de la plantilla durante el
		 *             an&aacute;lisis sint&aacute;ctico.
		 *             </p>
		 * @throws TransformerConfigurationException
		 *             the transformer configuration exception
		 */
		public static Templates loadTemplateXSLT(InputStream stream) throws TransformerConfigurationException {
			TransformerFactory transFact = getTransformerFactory();
			return transFact.newTemplates(new StreamSource(stream));
		}

		/**
		 * Transforms a document by applying the specified template.
		 * <p>
		 * Transforma un objeto document aplicando la plantilla de transformaciones
		 * especificada.
		 * </p>
		 *
		 * @param tpl
		 *            a transformations template
		 * @param doc
		 *            a DOM document to apply the transformations on
		 * @return a string representing the document's data transformed by the template
		 *         and ready to be displayed in a web browser. un objeto string que
		 *         representa la informaci&oacute;n del documento DOM con las
		 *         transformaciones contenidas en la plantilla y listo para ser
		 *         desplegado en un navegador Web.
		 * @throws javax.xml.transform.TransformerException
		 *             if an exceptional condition occurs during the transformation
		 *             process.
		 *             <p>
		 *             si una condici&oacute;n excepcional ocurre durante el proceso de
		 *             transformaci&oacute;n.
		 *             </p>
		 * @throws TransformerException
		 *             the transformer exception
		 */
		public static String transformDom(Templates tpl, Document doc) throws TransformerException {
			ByteArrayOutputStream sw = new ByteArrayOutputStream();
			Transformer trans = tpl.newTransformer();
			trans.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		}

		/**
		 * Verifies if the document represented in the input source is valid according
		 * to the schema provided.
		 * <p>
		 * Verifica si el documento DOM representado en la fuente de entrada es
		 * v&aacute;lido de acuerdo al esquema provisto.
		 * </p>
		 *
		 * @param schema
		 *            a represented schema to validate a document
		 * @param xml
		 *            an input source containing a DOM document
		 * @return a {@code boolean} indicating if the document is valid or not,
		 *         according to the schema provided. un {@code boolean} que indica si el
		 *         documento DOM recibido es o no v&aacute;lido, de acuerdo al esquema
		 *         proporcionado.
		 */
		public static boolean xmlVerifier(org.xml.sax.InputSource schema, org.xml.sax.InputSource xml) {
			return xmlVerifier(null, schema, null, xml);
		}

		/**
		 * Verifies if the document represented in the input source is valid according
		 * to the schema provided through another input source.
		 * <p>
		 * Verifica si el documento DOM representado en la fuente de entrada es
		 * v&aacute;lido de acuerdo al esquema provisto a trav&eacute;s de otra fuente
		 * de entrada.
		 * </p>
		 *
		 * @param idschema
		 *            a string representing a system id of the schema's input source
		 * @param schema
		 *            an input source containing a schema to validate a document
		 * @param idxml
		 *            a string representing a system id of the document's input source
		 * @param xml
		 *            an input source containing a document
		 * @return a {@code boolean} indicating if the document is valid or not,
		 *         according to the schema provided. un {@code boolean} que indica si el
		 *         documento DOM recibido es o no v&aacute;lido, de acuerdo al esquema
		 *         proporcionado.
		 */
		public static boolean xmlVerifier(String idschema, org.xml.sax.InputSource schema, String idxml,
				org.xml.sax.InputSource xml) {

			boolean bOk = false;
			if (schema == null || xml == null) {
				if (schema == null) {
					LOG.error("Error SWBUtils.XMLVerifier(): Schema source is null.");
				} else {
					LOG.event("Error SWBUtils.XMLVerifier(): The input document source is null.");
				}
				return bOk;
			}

			if (idschema != null && !idschema.trim().equals("")) {
				schema.setSystemId(idschema);
			}
			if (idxml != null && !idxml.trim().equals("")) {
				xml.setSystemId(idxml);
			}
			bOk = xmlVerifierImpl(schema.getSystemId(), schema, xml);
			return bOk;
		}

		/**
		 * Verifies if the document represented in the input stream is valid according
		 * to the schema provided through another input stream.
		 * <p>
		 * Verifica si el documento DOM representado en el flujo de entrada es
		 * v&aacute;lido de acuerdo al esquema provisto a trav&eacute;s de otro flujo de
		 * entrada.
		 * </p>
		 *
		 * @param schema
		 *            an input stream containing a schema to validate a document
		 * @param xml
		 *            an input stream containing a document
		 * @return a {@code boolean} indicating if the document is valid or not,
		 *         according to the schema provided. un {@code boolean} que indica si el
		 *         documento DOM recibido es o no v&aacute;lido, de acuerdo al esquema
		 *         proporcionado.
		 */
		public static boolean xmlVerifier(java.io.InputStream schema, java.io.InputStream xml) {
			return xmlVerifier(null, schema, xml);
		}

		/**
		 * Verifies if the document represented in the input stream is valid according
		 * to the schema provided through another input stream.
		 * <p>
		 * Verifica si el documento DOM representado en el flujo de entrada es
		 * v&aacute;lido de acuerdo al esquema provisto a trav&eacute;s de otro flujo de
		 * entrada.
		 * </p>
		 *
		 * @param idschema
		 *            a string representing a system id of the schema's input source
		 * @param schema
		 *            an input stream containing a schema to validate a document
		 * @param xml
		 *            an input stream containing a document
		 * @return a {@code boolean} indicating if the document is valid or not,
		 *         according to the schema provided. un {@code boolean} que indica si el
		 *         documento DOM recibido es o no v&aacute;lido, de acuerdo al esquema
		 *         proporcionado.
		 */
		public static boolean xmlVerifier(String idschema, InputStream schema, InputStream xml) {

			boolean bOk = false;
			if (schema == null || xml == null) {
				if (schema == null) {
					LOG.error("Error SWBUtils.XMLVerifier(): Schema stream is null.");
				} else {
					LOG.error("Error SWBUtils.XMLVerifier(): The input document stream is null.");
				}
				return bOk;
			}
			org.xml.sax.InputSource inxml = new org.xml.sax.InputSource(xml);
			bOk = xmlVerifierImpl(idschema, schema, inxml);
			return bOk;
		}

		/**
		 * Verifies if a document is valid according to the schema provided.
		 * <p>
		 * Verifica si un documento DOM es v&aacute;lido de acuerdo al esquema provisto.
		 * </p>
		 *
		 * @param sysid
		 *            a string representing a system id of the input stream, in case
		 *            {@code objschema} being an input stream.
		 * @param objschema
		 *            a schema to validate a document. This object can be an instance of
		 * @param objxml
		 *            a document to validate against the schema provided. This document
		 *            can be contained in an instance of the following classes:
		 * @return a {@code boolean} indicating if the document received is valid or
		 *         not, according to the schema provided. un {@code boolean} indicando
		 *         si el documento DOM recibido es v&aacute;lido o no, de acuerdo al
		 *         esquema provisto. {@code java.io.File},
		 *         {@code org.xml.sax.InputSource}, {@code java.io.InputStream} or even
		 *         {@code java.lang.String}. {@code java.io.File},
		 *         {@code org.xml.sax.InputSource}, {@code org.w3c.dom.Node} or even
		 *         {@code java.lang.String}.
		 */
		private static boolean xmlVerifierImpl(String sysid, Object objschema, Object objxml) {
			//TODO: Remove this method and dependencies because is not used anywhere. Move code to WBAdmResourceUtils class and migrate to pure java code.
			boolean bOk = false;
			if (objschema == null || objxml == null) {
				if (objschema == null) {
					LOG.error("Error SWBUtils.XMLVerifier(): Schema is null.");
				} else {
					LOG.error("Error SWBUtils.XMLVerifier(): The input document is null.");
				}
				return bOk;
			}
			org.iso_relax.verifier.VerifierFactory factory = new com.sun.msv.verifier.jarv.TheFactoryImpl();
			org.iso_relax.verifier.Schema schema = null;
			try {
				if (objschema instanceof File) {
					schema = factory.compileSchema((File) objschema);
				} else if (objschema instanceof org.xml.sax.InputSource) {
					schema = factory.compileSchema((org.xml.sax.InputSource) objschema);
				} else if (objschema instanceof InputStream) {
					if (sysid != null && !sysid.trim().equals("")) {
						schema = factory.compileSchema((InputStream) objschema, sysid);
					} else {
						schema = factory.compileSchema((InputStream) objschema);
					}
				} else if (objschema instanceof String) {
					schema = factory.compileSchema((String) objschema);
				}
				try {
					org.iso_relax.verifier.Verifier verifier = schema.newVerifier();
					verifier.setErrorHandler(SWBUtils.XML.silentErrorHandler);

					if (objxml instanceof File) {
						bOk = verifier.verify((File) objxml);
					} else if (objxml instanceof org.xml.sax.InputSource) {
						bOk = verifier.verify((org.xml.sax.InputSource) objxml);
					} else if (objxml instanceof org.w3c.dom.Node) {
						bOk = verifier.verify((org.w3c.dom.Node) objxml);
					} else if (objxml instanceof String) {
						bOk = verifier.verify((String) objxml);
					}
				} catch (org.iso_relax.verifier.VerifierConfigurationException e) {
					LOG.error("Error SWBUtils.XMLVerifier(): Unable to create a new verifier.", e);
				} catch (org.xml.sax.SAXException e) {
					LOG.event("Error SWBUtils.XMLVerifier(): Input document is not wellformed.", e);
				}
			} catch (Exception e) {
				LOG.event("Error SWBUtils.XMLVerifier(): Unable to parse schema file.", e);
			}
			return bOk;
		}

		/**
		 * Verifies if the document represented by a string is valid according to the
		 * schema provided through another string.
		 * <p>
		 * Verifica si el documento DOM representado por una cadena es v&aacute;lido de
		 * acuerdo al esquema provisto a trav&eacute;s de otra cadena.
		 * </p>
		 *
		 * @param schema
		 *            a string containing a schema to validate a document
		 * @param xml
		 *            a string containing a DOM document
		 * @return a {@code boolean} indicating if the document received is valid or
		 *         not, according to the schema provided. un {@code boolean} indicando
		 *         si el documento DOM recibido es v&aacute;lido o no, de acuerdo al
		 *         esquema provisto.
		 */
		public static boolean xmlVerifier(String schema, String xml) {
			return xmlVerifierByURL(null, schema, xml);
		}

		/**
		 * Verifies if the document represented by a string is valid according to the
		 * schema provided through another string.
		 * <p>
		 * Verifica si el documento DOM representado por una cadena es v&aacute;lido de
		 * acuerdo al esquema provisto a trav&eacute;s de otra cadena.
		 * </p>
		 *
		 * @param sysid
		 *            a string representing a system id for constructing the schema.
		 * @param schema
		 *            a string containing a schema to validate a document
		 * @param xml
		 *            a string containing a DOM document
		 * @return a {@code boolean} indicating if the document received is valid or
		 *         not, according to the schema provided. un {@code boolean} indicando
		 *         si el documento DOM recibido es v&aacute;lido o no, de acuerdo al
		 *         esquema provisto.
		 */
		public static boolean xmlVerifierByURL(String sysid, String schema, String xml) {
			return xmlVerifierImpl(sysid, schema, xml);
		}

		/**
		 * Converts a node into a document.
		 * <p>
		 * Convierte un objeto node en un objeto document
		 * </p>
		 *
		 * @param node
		 *            a node to convert
		 * @return a document containing the node's content.
		 *
		 */
		public static Document node2Document(Node node) {
			// ensures xerces dom
			if (node instanceof org.apache.xerces.dom.DocumentImpl) {
				return (Document) node;
			}
			Document document = getNewDocument();
			if (node instanceof Document) {
				node = ((Document) node).getDocumentElement();
			}
			document.appendChild(document.importNode(node, true));
			return document;
		}

		/**
		 * Serializes a document using the UTF-8 character encoding. Gets a document's
		 * content and writes it down into a file with a 2 blank space indentation.
		 * <p>
		 * Serializa un objeto document utilizando el c&oacute;digo de caracteres UTF-8.
		 * Obtiene el contenido de un objeto document y lo escribe en un archivo con 2
		 * espacios en blanco como sangr&iacute;a.
		 * </p>
		 *
		 * @param dom
		 *            a document to serialize
		 * @param file
		 *            a string representing a file's pathname. Where the file is going
		 *            to be created.
		 */
		public static void domToFile(Document dom, String file) {
			domToFile(dom, file, "UTF-8");
		}

		/**
		 * Serializes a document using the specified character encoding. Gets a
		 * document's content and writes it down into a file with a 2 blank space
		 * indentation.
		 * <p>
		 * Serializa un objeto document utilizando el c&oacute;digo de caracteres
		 * especificado. Obtiene el contenido de un objeto document y lo escribe en un
		 * archivo con 2 espacios en blanco como sangr&iacute;a.
		 * </p>
		 *
		 * @param dom
		 *            a document to serialize
		 * @param file
		 *            a string representing a file's pathname. Where the file is going
		 *            to be created.
		 * @param encode
		 *            a string representing a character encoding. This will be used for
		 *            writing the file's content.
		 */
		public static void domToFile(Document dom, String file, String encode) {

			FileOutputStream osw = null;
			try {
				osw = new FileOutputStream(new File(file));
				StreamResult streamResult = new StreamResult(osw);

				Transformer transformer = null;
				TransformerFactory tFactory = getTransformerFactory();
				synchronized (tFactory) {
					transformer = tFactory.newTransformer();
				}
				transformer.setOutputProperty(OutputKeys.ENCODING, encode);
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.METHOD, "xml");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				transformer.transform(new DOMSource(dom), streamResult);
				osw.flush();
				osw.close();
			} catch (Exception e) {
				LOG.error(e);
			}
		}

		/**
		 * An error handler implementation that doesn't report any error.
		 * <p>
		 * Una implementaci&oacute;n de manejador de errores que no reporta error
		 * alguno.
		 * </p>
		 */
		//TODO: Check whether a mute error handler is needed or if its a good practice to do this.
		private static final org.xml.sax.ErrorHandler silentErrorHandler = new org.xml.sax.ErrorHandler() {
			public void fatalError(org.xml.sax.SAXParseException e) {

			}

			public void error(org.xml.sax.SAXParseException e) {

			}

			public void warning(org.xml.sax.SAXParseException e) {

			}
		};

		/**
		 * Replaces some special characters in a XML-formatted string by their entity
		 * names. The characters to replace are: {@literal \t, &, <, y >}.
		 * <p>
		 * Reemplaza algunos de los caracteres especiales presentes en una cadena con
		 * formato XML por su equivalente en nombre de entidad. Los caracteres a
		 * reemplazar son: {@literal \t, &, <, y >.}
		 * </p>
		 *
		 * @param str
		 *            an XML-formatted string with some replaceable characters
		 * @return a string representing the same content that {@code str}, but with
		 *         some characters replaced according to the following relations:
		 *         {@literal \t} replaced by 4 blank spaces {@literal &} replaced by
		 *         {@literal &amp;} {@literal <} replaced by {@literal &lt;} } replaced
		 *         by {@literal &gt;} un objeto string que representa el mismo contenido
		 *         que {@code str}, pero con algunos caracteres reemplazados de acuerdo
		 *         a las relaciones anteriormente mostradas.
		 */
		 public static String replaceXMLChars(String str) {
			if (str == null) {
				return null;
			}
			StringBuilder ret = new StringBuilder(500);

			// split tokens
			StringTokenizer tokenizer = new StringTokenizer(str, " \t@%^&()-+=|\\{}[].;\"<>", true);
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();

				if (token.startsWith("\t")) {
					ret.append("    ");
				} else if (token.startsWith("<")) {
					ret.append("&lt;");
				} else if (token.startsWith(">")) {
					ret.append("&gt;");
				} else if (token.startsWith("&")) {
					ret.append("&amp;");
				}
				ret.append(token);
			}
			return ret.toString();

		}

		/**
		 * Replaces the entity name of some special characters by their representation
		 * in HTML code. The entity names to replace are:
		 * {@literal &lt;, &gt;, y &amp;}.
		 * <p>
		 * Reemplaza el nombre de entidad de algunos caracteres especiales, por su
		 * equivalente en HTML. Los nombres de entidad a reemplazar son:
		 *
		 * @param txt
		 *            a string containing the text to replace
		 * @return a string with the entity names of some special characters replaced by
		 *         their representation in HTML code. The entity names to look for are:
		 *         {@literal &amp;} replaced by {@literal &} {@literal &lt;} replaced by
		 *         {@literal <} } un objeto string con los nombres de entidad de algunos
		 *         caracteres especiales reemplazados por su representaci&oacute;n en
		 *         c&oacute;digo HTML, arriba se mencionan los reemplazos realizados.
		 *         {@literal &lt;, &gt;, y &amp;}.
		 *         </p>
		 */
		 public static String replaceXMLTags(String txt) {
			if (txt == null) {
				return null;
			}

			StringBuilder str = new StringBuilder(txt);
			for (int x = 0; x < str.length(); x++) {
				char ch = str.charAt(x);
				if (ch == '&') {
					if (str.substring(x, x + 4).equals("&lt;")) {
						str.replace(x, x + 4, "<");
					} else if (str.substring(x, x + 4).equals("&gt;")) {
						str.replace(x, x + 4, ">");
					} else if (str.substring(x, x + 5).equals("&amp;")) {
						str.replace(x, x + 5, "&");
					}
				}
			}
			return str.toString();
		}

		/**
		 * Creates an empty child node for the one specified.
		 * <p>
		 * Crea un nodo hijo vac&iacute;o al nodo especificado.
		 * </p>
		 *
		 * @param ele
		 *            father node. Must not be {@code null}.
		 * @param name
		 *            a string representing the name for the node to create. Must not be
		 *            {@code null}.
		 * @return a new {@code element} (node) that is a child of {@code ele} and has
		 *         the name indicated by {@code name}. un nuevo objeto {@code element}
		 *         (nodo) que es hijo de {@code ele} y tiene por nombre, el indicado por
		 *         {@code name}.
		 * @throws org.w3c.dom.DOMException
		 *             if the name specified for the new node is not an XML name
		 *             according to the XML version in use specified in the
		 * @throws java.lang.NullPointerException
		 *             if {@code ele} is {@code null}.
		 *             <p>
		 *             si {@code ele} es {@code null}.
		 *             </p>
		 *             {@code Document.xmlVersion} attribute.
		 *             <p>
		 *             si el nombre especificado para el nuevo nodo no es un nombre de
		 *             XML de acuerdo a la versi&oacute; en uso de XML especificada en
		 *             el atributo {@code Document.xmlVersion}.
		 *             </p>
		 */
		 public static Element appendChild(Element ele, String name) {
			Document doc = ele.getOwnerDocument();
			Element e = doc.createElement(name);
			ele.appendChild(e);
			return e;
		}

		/**
		 * Creates a child text node for the one specified. The new node will contain
		 * the text received through {@code value}.
		 * <p>
		 * Crea un nodo hijo de texto al nodo especificado. El nuevo nodo
		 * contendr&aacute; el texto recibido a trav&eacute;s de {@code value}.
		 * </p>
		 *
		 * @param ele
		 *            father node. This node will contain the new node.
		 * @param name
		 *            a string representing the name for the new node
		 * @param value
		 *            a string representing the new node's content
		 * @return a new DOM {@code element} whose properties: name, value and father
		 *         are the same as the parameters received. un nuevo objeto
		 *         {@code element} de DOM cuyas propiedades: nombre, valor y padre
		 *         tendr&aacute;n como valores los par&aacute;metros recibidos.
		 */
		 public static Element appendChild(Element ele, String name, String value) {
			Document doc = ele.getOwnerDocument();
			Element e = doc.createElement(name);
			e.appendChild(doc.createTextNode(value));
			ele.appendChild(e);
			return e;
		}

		/**
		 * Evaluates a XPath expression in the context of the specified input source and
		 * returns the result as the type specified in {@code resultType}.
		 *
		 * @param expression
		 *            a string representing a XPath expression
		 * @param input
		 *            the input source that contains the context for the expression to
		 *            evaluate
		 * @param resultType
		 *            the qualified name for the result type to retutrn
		 * @return the specified object QName according to {@link XPathConstants}, for
		 *         example {@code XPathConstants.NODE}. el objeto especificado QName de
		 *         acuerdo a {@code XPathConstants}, por ejemplo
		 *         {@code XPathConstants.NODE}.
		 * @throws javax.xml.xpath.XPathExpressionException
		 *             if the expression cannot be compiled.
		 *             <p>
		 *             si la expresi&oacute;n no se puede compilar.
		 *             </p>
		 * @throws XPathExpressionException
		 *             the x path expression exception
		 */
		public static Object getXpathEval(String expression, InputSource input, QName resultType)
				throws javax.xml.xpath.XPathExpressionException {
			XPath xpathObj = getXPathObject();
			javax.xml.xpath.XPathExpression xpathExpression = xpathObj.compile(expression);
			return xpathExpression.evaluate(input, resultType);
		}

		/**
		 * Sets the value for the first element identified by {@code name}, as the tag
		 * name, in the DOM document received. If the tag name specified does not exist,
		 * it is created; if it exists, its value is modified.
		 * <p>
		 * Fija el valor del primer elemento identificado por {@code name}, como el
		 * nombre de la etiqueta, en el documento DOM recibido. Si no existe el elemento
		 * con el nombre especificado, lo crea; si existe, modifica su valor.
		 * </p>
		 *
		 * @param dom
		 *            the document to modify
		 * @param name
		 *            a string representing the element's name to look for
		 * @param value
		 *            a string representing the element's new value. If it is
		 *            {@literal null} and the element identified by {@code name} exists,
		 *            the element is removed.
		 */
		public static void setAttribute(Document dom, String name, String value) {
			NodeList data = dom.getElementsByTagName(name);
			if (data.getLength() > 0) {
				Node txt = data.item(0).getFirstChild();
				if (txt != null) {
					if (value != null) {
						txt.setNodeValue(value);
					} else {
						data.item(0).removeChild(txt);
					}
				} else {
					if (value != null) {
						data.item(0).appendChild(dom.createTextNode(value));
					}
				}
			} else {
				Element res = (Element) dom.getFirstChild();
				Element ele = dom.createElement(name);
				if (value != null) {
					ele.appendChild(dom.createTextNode(value));
				}
				res.appendChild(ele);
			}
		}

		/**
		 * Gets the value for the first occurence of a specified element {@code name} or
		 * the default value. If the element name is not found, the default value is
		 * returned.
		 * <p>
		 * Obtiene el valor de la primera ocurrencia de un elemento especificado por su
		 * nombre de etiqueta a trav&eacute;s de {@code name}, o el valor por defecto.
		 * Si el elemento no es encontrado, regresa {@code null}.
		 * </p>
		 *
		 * @param dom
		 *            the DOM document which contains the data
		 * @param name
		 *            a string representing the name of the element to look for
		 * @param defvalue
		 *            a string to return if specified element is not found
		 * @return a string containing the value asociated to the first element with tag
		 *         name {@code name} whithin the DOM document {@code dom}; or the value
		 *         of {@code defvalue} if the specified element does not exist. un
		 *         objeto string que contiene el valor asociado al primer elemento con
		 *         nombre de etiqueta {@code name} dentro del documento DOM {@code dom};
		 *         o el valor de {@code defvalue} si el elemento especificado no existe.
		 *         </p>
		 */
		public static String getAttribute(Document dom, String name, String defvalue) {
			String ret = getAttribute(dom, name);
			if (ret == null) {
				ret = defvalue;
			}
			return ret;
		}

		/**
		 * Gets the value for the first occurence of a specified element {@code name}.
		 * If the element name is not found, {@code null} is returned.
		 * <p>
		 * Obtiene el valor de la primera ocurrencia de un elemento especificado por su
		 * nombre de etiqueta a trav&eacute;s de {@code name}. Si el elemento no es
		 * encontrado, regresa {@code null}.
		 * </p>
		 *
		 * @param dom
		 *            the DOM document which contains the data
		 * @param name
		 *            a string representing the name of the element to look for
		 * @return a string containing the value asociated to the first element with tag
		 *         name {@code name} whithin the DOM document {@code dom}. un objeto
		 *         string que contiene el valor asociado al primer elemento con nombre
		 *         de etiqueta {@code name} dentro del documento DOM {@code dom}.
		 *         </p>
		 */
		public static String getAttribute(Document dom, String name) {
			String ret = null;
			NodeList data = dom.getElementsByTagName(name);
			if (data.getLength() > 0) {
				Node txt = data.item(0).getFirstChild();
				if (txt != null) {
					ret = txt.getNodeValue();
				}
			}
			return ret;
		}
	}

	/**
	 * Manager of database connection operations and characteristics. All the
	 * connection pool names that <u>can be used</u> with this operations
	 * <strong>must be registered</strong> in the db.properties file.
	 * <p>
	 * Administrador de operaciones y caracter&iacute;sticas de conexiones a base de
	 * datos. Todos los nombres de pool de conexiones que <u>pueden utilizarse</u>
	 * con estas operaciones <strong>deben estar registrados</strong> en el archivo
	 * db.properties.
	 * </p>
	 */
	public static class DB {

		private DB () {}
		/**
		 * Identifier for the Hypersonic database.
		 * <p>
		 * Identificador para la base de datos Hypersonic.
		 * </p>
		 */
		/**
		 * Identifier for the Hypersonic database.
		 * <p>
		 * Identificador para la base de datos Hypersonic.
		 * </p>
		 */
		public static final String DBTYPE_HSQLDB = "HSQLDB";
		/**
		 * Identifier for the MySQL database.
		 * <p>
		 * Identificador para la base de datos MySQL.
		 * </p>
		 */
		public static final String DBTYPE_MySQL = "MySQL";
		/**
		 * Identifier for the Microsoft SQL Server database.
		 * <p>
		 * Identificador para la base de datos de Microsoft SQL Server.
		 * </p>
		 */
		public static final String DBTYPE_MsSQL = "MsSQL";

		/**
		 * The Constant DBTYPE_MsSQL2008.
		 */
		public static final String DBTYPE_MsSQL2008 = "Microsoft SQL Server";
		/**
		 * Identifier for the Oracle database.
		 * <p>
		 * Identificador para la base de datos de Oracle.
		 * </p>
		 */
		public static final String DBTYPE_Oracle = "Oracle";
		/**
		 * Identifier for the PostgreSQL database.
		 * <p>
		 * Identificador para la base de datos de PostgreSQL.
		 * </p>
		 */
		public static final String DBTYPE_PostgreSQL = "PostgreSQL";
		/**
		 * Identifier for the Derby database.
		 * <p>
		 * Identificador para la base de datos de Derby.
		 * </p>
		 */
		public static final String DBTYPE_Derby = "Derby";
		/**
		 * Database connection manager.
		 * <p>
		 * Administrador de conexiones a base de datos.
		 * </p>
		 */
		private static DBConnectionManager manager = null;
		/**
		 * Identifier for the default database pool.
		 * <p>
		 * Identificador para el pool de conexiones a base de datos por defecto.
		 * </p>
		 */
		private static String defaultPoolName = "swb";

		/**
		 * Gets a reference to the database connection manager of this object.
		 * <p>
		 * Obtiene una referencia al administrador de conexiones a base de datos de este
		 * objeto.
		 * </p>
		 *
		 * @return a reference to the database connection manager of this object. una
		 *         referencia al administrador de conexiones a base de datos de este
		 *         objeto.
		 */
		private static DBConnectionManager getConnectionManager() {
			if (manager == null) {
				manager = new DBConnectionManager();
			}
			return manager;
		}

		/**
		 * Gets all the database connection pools available.
		 * <p>
		 * Obtiene todos los pools de conexiones a base de datos disponibles.
		 * </p>
		 *
		 * @return an enumeration of the database connection pools found. una
		 *         enumeraci&oacute;n de los pools de conexi&oacute;n a base de datos
		 *         encontrados.
		 */
		public static Enumeration<DBConnectionPool> getPools() {
			return getConnectionManager().getPools().elements();
		}

		/**
		 * Retrieves the database connection pool whose name matches {@code name}.
		 * <p>
		 * Recupera el pool de conexiones a base de datos cuyo nombre concuerda con
		 * {@code name}.
		 * </p>
		 *
		 * @param name
		 *            a string representing a registered connection pool name to
		 *            retrieve
		 * @return a database connection pool whose name matches {@code name}. un pool
		 *         de conexiones a base de datos cuyo nombre concuerda con {@code name}.
		 */
		public static DBConnectionPool getPool(String name) {
			return (DBConnectionPool) getConnectionManager().getPools().get(name);
		}

		/**
		 * Retrieves the database connection pool whose name matches
		 * {@code SWBUtils.DB.defaultPoolName}.
		 * <p>
		 * Recupera el pool de conexiones a base de datos cuyo nombre concuerda con
		 * {@code SWBUtils.DB.defaultPoolName} whose name matches
		 * {@code SWBUtils.DB.defaultPoolName}.
		 * </p>
		 *
		 * @return the database connection pool whose name matches
		 *         {@code SWBUtils.DB.defaultPoolName}. el pool de conexiones a base de
		 *         datos cuyo nombre concuerda con {@code SWBUtils.DB.defaultPoolName}.
		 */
		public static DBConnectionPool getDefaultPool() {
			return (DBConnectionPool) getConnectionManager().getPools().get(defaultPoolName);
		}

		/**
		 * Gets the name for the default database connection pool.
		 * <p>
		 * Obtiene el nombre del pool de conexiones a base de datos por defecto.
		 * </p>
		 *
		 * @return a string representing the name for the default database connection
		 *         pool. un objeto string que representa el nombre del pool de
		 *         conexiones a base de datos por defecto.
		 */
		public static String getDefaultPoolName() {
			return defaultPoolName;
		}

		/**
		 * Sets the name for the default database connection pool.
		 * <p>
		 * Fija el nombre del pool de conexiones a base de datos por defecto.
		 * </p>
		 *
		 * @param poolName
		 *            a string representing the new name for the default database
		 *            connection pool. Must not be {@code null}.
		 */
		public static void setDefaultPool(String poolName) {
			defaultPoolName = poolName;
		}

		/**
		 * Gets a database connection which does not belong to the connection pool
		 * specified.
		 * <p>
		 * Obtiene una conexi&oacute;n a base de datos que no pertenece al pool de
		 * conexiones especificado.
		 * </p>
		 *
		 * @param poolName
		 *            a string representing the name of a registered connection pool
		 * @return a database connection that does not belong to the connection pool
		 *         whose name matches {@code poolName}. una conexi&oacute;n a base de
		 *         datos que no pertenece al pool de conexiones cuyo nombre concuerda
		 *         con {@code poolName}.
		 */
		public static Connection getNoPoolConnection(String poolName) {
			return getConnectionManager().getNoPoolConnection(poolName);
		}

		/**
		 * Gets a database connection from the default database connection pool setting
		 * the description indicated to the database connection.
		 * <p>
		 * Obtiene una conexi&oacute;n a base de datos del pool de conexiones por
		 * defecto, fijando la descripci&oacute;n indicada a la conexi&oacute;n de base
		 * de datos.
		 * </p>
		 *
		 * @param description
		 *            the description
		 * @return Connection from DBPool.
		 */
		public static Connection getDefaultConnection(String description) {
			return getConnection(defaultPoolName, description);
		}

		/**
		 * Gets a database connection from the default database connection pool with no
		 * description.
		 * <p>
		 * Obtiene una conexi&oacute;n a base de datos del pool de conexiones por
		 * defecto, sin descripci&oacute;n.
		 * </p>
		 *
		 * @return a database connection from the default database connection pool. una
		 *         conexi&oacute;n a base de datos del pool de conexiones por defecto.
		 */
		public static Connection getDefaultConnection() {
			return getConnection(defaultPoolName);
		}

		/**
		 * Gets a database connection from the connection pool specified, setting a
		 * description for that connection.
		 * <p>
		 * Obtiene una conexi&oacute;n a base de datos del pool de conexiones
		 * especificado, fijando una descripci&oacute;n para esa conexi&oacute;n.
		 * </p>
		 *
		 * @param poolName
		 *            a string representing the name of a registered connection pool
		 *            from which a connection will be obtained
		 * @param description
		 *            a string with the description for the database connection
		 * @return a database connection from the connection pool specified.
		 *
		 */
		public static Connection getConnection(String poolName, String description) {
			return getConnectionManager().getConnection(poolName, description);
		}

		/**
		 * Gets a database connection (descriptionless) from the connection pool
		 * specified.
		 * <p>
		 * Obtiene una conexi&oacute;n a base de datos (sin descripci&oacute;n) del pool
		 * de conexiones especificado.
		 * </p>
		 *
		 * @param name
		 *            a string representing the name of a registered connection pool
		 *            from which a connection will be obtained
		 * @return a database connection from the connection pool specified.
		 *
		 */
		public static Connection getConnection(String name) {
			return getConnectionManager().getConnection(name);
		}

		/**
		 * Gets the database name used by the default database connection pool.
		 * <p>
		 * Obtiene el nombre de base de datos utilizado por el pool de conexiones a base
		 * de datos por defecto.
		 * </p>
		 *
		 * @return a string containing the database name used by the default database
		 *         connection pool. un objeto string que contiene el nombre de base de
		 *         datos utilizado por el pool de conexiones a base de datos por
		 *         defecto.
		 */
		public static String getDatabaseName() {
			return getDatabaseName(defaultPoolName);
		}

		/**
		 * Gets the database name used by the database connection pool specified.
		 * <p>
		 * Obtiene el nombre de base de datos utilizado por el pool de conexiones a base
		 * de datos especificado.
		 * </p>
		 *
		 * @param poolName
		 *            a string indicating a registered connection pool name to use. If
		 *            it is {@code null}, the same is returned.
		 * @return a string containing the database name used by the database connection
		 *         pool specified. un objeto string que contiene el nombre de base de
		 *         datos utilizado por el pool de conexiones especificado.
		 */
		public static String getDatabaseName(String poolName) {
			String ret = null;
			try {
				Connection con = getConnectionManager().getConnection(poolName);
				if (con != null) {
					java.sql.DatabaseMetaData md = con.getMetaData();
					ret = md.getDatabaseProductName();
					con.close();
				}
			} catch (Exception e) {
				LOG.error("Not Database Found...", e);
			}
			return ret;
		}

		/**
		 * Gets the database type used by the default connection pool. This database
		 * type is determined by the connected database's name, according to this name,
		 * one of the database identifiers ({@code SWBUtils.DB.DBTYPE_HSQL},
		 * {@code SWBUtils.DB.DBTYPE_MySQL},
		 *
		 * @return a string representing the identified database type used by the
		 *         default connection pool. un objeto string que representa el tipo de
		 *         base de datos identificado que es utilizado por el pool de conexiones
		 *         por defecto. {@code SWBUtils.DB.DBTYPE_MsSQL},
		 *         {@code SWBUtils.DB.DBTYPE_Oracle},
		 *         {@code SWBUtils.DB.DBTYPE_PostgreSQL},
		 *         {@code SWBUtils.DB.DBTYPE_Derby}) is returned.
		 *         <p>
		 *         Obtiene el tipo de base de datos utilizado por el pool de conexiones
		 *         por defecto. Este tipo de base de datos est&aacute; determinado por
		 *         el nombre de la base de datos en uso, de acuerdo a este nombre, uno
		 *         de los identificadores de base de datos
		 *         ({@code SWBUtils.DB.DBTYPE_HSQL}, {@code SWBUtils.DB.DBTYPE_MySQL},
		 *         {@code SWBUtils.DB.DBTYPE_MsSQL}, {@code SWBUtils.DB.DBTYPE_Oracle},
		 *         {@code SWBUtils.DB.DBTYPE_PostgreSQL},
		 *         {@code SWBUtils.DB.DBTYPE_Derby}) es devuelto.
		 *         </p>
		 */
		public static String getDatabaseType() {
			return getDatabaseType(defaultPoolName);
		}

		/**
		 * Gets the database type used by the connection pool specified. This database
		 * type is determined by the connected database's name, according to this name,
		 * one of the database identifiers ({@code SWBUtils.DB.DBTYPE_HSQL},
		 * {@code SWBUtils.DB.DBTYPE_MySQL},
		 *
		 * @param poolName
		 *            a string representing a registered connection pool name
		 * @return a string representing the identified database type used by the
		 *         specified connection pool. un objeto string que representa el tipo de
		 *         base de datos identificado que es utilizado por el pool de conexiones
		 *         especificado. {@code SWBUtils.DB.DBTYPE_MsSQL},
		 *         {@code SWBUtils.DB.DBTYPE_Oracle},
		 *         {@code SWBUtils.DB.DBTYPE_PostgreSQL},
		 *         {@code SWBUtils.DB.DBTYPE_Derby}) is returned.
		 *         <p>
		 *         Obtiene el tipo de base de datos utilizado por el pool de conexiones
		 *         especificado. Este tipo de base de datos est&aacute; determinado por
		 *         el nombre de la base de datos en uso, de acuerdo a este nombre, uno
		 *         de los identificadores de base de datos
		 *         ({@code SWBUtils.DB.DBTYPE_HSQL}, {@code SWBUtils.DB.DBTYPE_MySQL},
		 *         {@code SWBUtils.DB.DBTYPE_MsSQL}, {@code SWBUtils.DB.DBTYPE_Oracle},
		 *         {@code SWBUtils.DB.DBTYPE_PostgreSQL},
		 *         {@code SWBUtils.DB.DBTYPE_Derby}) es devuelto.
		 *         </p>
		 */
		public static String getDatabaseType(String poolName) {
			String ret = getDatabaseName(poolName);

			if (null != ret) {
				if (ret.toLowerCase().contains("hsql")) {
					ret = SWBUtils.DB.DBTYPE_HSQLDB;
				} else if (ret.toLowerCase().contains("mysql")) {
					ret = SWBUtils.DB.DBTYPE_MySQL;
				} else if (ret.toLowerCase().contains("mssql")) {
					ret = SWBUtils.DB.DBTYPE_MsSQL;
				} else if (ret.toLowerCase().contains("oracle")) {
					ret = SWBUtils.DB.DBTYPE_Oracle;
				} else if (ret.toLowerCase().contains("postgresql")) {
					ret = SWBUtils.DB.DBTYPE_PostgreSQL;
				} else if (ret.toLowerCase().contains("derby")) {
					ret = SWBUtils.DB.DBTYPE_Derby;
				}
			}
			return ret;
		}

		/**
		 * Gets the number of opened database connections belonging to a certain
		 * connection pool.
		 * <p>
		 * Obtiene el n&uacute;mero de conexiones a base de datos abiertas,
		 * pertenecientes a un cierto pool de conexiones.
		 * </p>
		 *
		 * @param poolName
		 *            a string representing a registered connection pool name.
		 * @return the number of opened database connections belonging to a certain
		 *         connection pool. el n&uacute;mero de conexiones a base de datos
		 *         abiertas, pertenecientes a un cierto pool de conexiones.
		 */
		public static int getConnections(String poolName) {
			return getConnectionManager().getConnections(poolName);
		}

		/**
		 * Gets the number of free database connections into the specified connection
		 * pool.
		 * <p>
		 * Obtiene el n&uacute;mero de conexiones a base de datos libres en el pool de
		 * conexiones especificado.
		 * </p>
		 *
		 * @param poolName
		 *            a string representing a registered connection pool name.
		 * @return the number of free database connections belonging to the connection
		 *         pool with name like {@code poolName}. el n&uacute;mero de conexiones
		 *         a base de datos libres, pertenecientes al pool de conexiones con
		 *         nombre igual a {@code poolName}.
		 */
		public static int getFreeConnections(String poolName) {
			return getConnectionManager().getFreeConnections(poolName);
		}

		/**
		 * Obtains a reference to the pool connection time-lock associated to the
		 * connection manager.
		 * <p>
		 * Obtiene una referencia al administrador del mecanismo de time-lock del pool
		 * de conexiones asociado al administrador de conexiones.
		 * </p>
		 *
		 * @return a reference to the pool connection time-lock associated to the
		 *         connection manager. una referencia al administrador del mecanismo de
		 *         time-lock del pool de conexiones asociado al administrador de
		 *         conexiones.
		 */
		public static PoolConnectionTimeLock getTimeLock() {
			return getConnectionManager().getTimeLock();
		}
	}

	/**
	 * Wrapper for several criptographic operations.
	 * <p>
	 * Contenedor de varias operaciones criptogr&aacute;ficas.
	 * </p>
	 */
	public static class CryptoWrapper {
		private static SecureRandom sr = null;

		private CryptoWrapper () {}

		static {
			try {
				sr = SecureRandom.getInstance("SHA1PRNG");
			} catch (NoSuchAlgorithmException nsae) {
				LOG.error("Instantiating the secure Random generator", nsae);
			}
		}

		/**
		 * Performs the diggestion of the message in {@code toEncode} through the
		 * SHA-512 algorithm, with a previous validation.
		 * <p>
		 * Realiza la digesti&oacute;n del mensaje en {@code toEncode} utilizando el
		 * algoritmo SHA-512, con una validaci&oacute;n previa.
		 * </p>
		 *
		 * @param toEncode
		 *            a string containing the message to digest. If it starts with any
		 *            of the following prefixes: {@literal "{SHA-512}"},
		 * @return a string representing the received message digested with an
		 *         implementation of the SHA-512 algorithm. This string starts with the
		 *         prefix "{SHA-512}". un objeto string que representa el mensaje
		 *         recibido digerido con una implementaci&oacute;n del algoritmo
		 *         SHA-512. Este string comienza con el prefijo "{SHA-512}".
		 * @throws java.security.NoSuchAlgorithmException
		 *             if there is no implementation of the SHA-512 algorithm available
		 *             in the environment.
		 *             <p>
		 *             si no hay una implementaci&oacute;n del algoritmo SHA-512
		 *             disponible en el ambiente.
		 *             </p>
		 * @throws java.io.UnsupportedEncodingException
		 *             if the character encoding used to obtain the bytes from the
		 *             message received (ISO8859-1) is not supported.
		 *             <p>
		 *             si el c&oacute;digo de caracteres utilizado para obtener los
		 *             bytes del mensaje recibido (ISO8859-1) no es soportado.
		 *             </p>
		 * @throws NoSuchAlgorithmException
		 *             the no such algorithm exception
		 * @throws UnsupportedEncodingException
		 *             the unsupported encoding exception {@literal "{SHA}"},
		 *             {@literal "{SSHA}"}, {@literal "{CRYPT}"}, {@literal "{SMD5}"} OR
		 *             {@literal "{MD5}"}, the resulting string is this same parameter.
		 */
		public static String passwordDigest(String toEncode)
				throws NoSuchAlgorithmException, UnsupportedEncodingException {

			if (toEncode.startsWith("{SHA-512}") || toEncode.startsWith("{SHA}") || toEncode.startsWith("{SSHA}")
					|| toEncode.startsWith("{CRYPT}") || toEncode.startsWith("{SMD5}")
					|| toEncode.startsWith("{MD5}")) {
				return toEncode;
			}
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
			return "{SHA-512}" + SFBase64.encodeBytes(messageDigest.digest(toEncode.getBytes("ISO8859-1")), false);
		}

		/**
		 * Performs the diggestion of the message in {@code toEncode} through the
		 * SHA-512 algorithm.
		 * <p>
		 * Realiza la digesti&oacute;n del mensaje en {@code toEncode} utilizando el
		 * algoritmo SHA-512.
		 * </p>
		 *
		 * @param toEncode
		 *            a string containing the message to digest
		 * @return a string representing the received message digested with an
		 *         implementation of the SHA-512 algorithm. This string starts with the
		 *         prefix "{SHA-512}". un objeto string que representa el mensaje
		 *         recibido digerido con una implementaci&oacute;n del algoritmo
		 *         SHA-512. Este string comienza con el prefijo "{SHA-512}".
		 * @throws java.security.NoSuchAlgorithmException
		 *             if there is no implementation of the SHA-512 algorithm available
		 *             in the environment.
		 *             <p>
		 *             si no hay una implementaci&oacute;n del algoritmo SHA-512
		 *             disponible en el ambiente.
		 *             </p>
		 * @throws NoSuchAlgorithmException
		 *             If something fails when comparing passwords
		 */
		public static String comparablePassword(String toEncode) throws NoSuchAlgorithmException {
			return comparablePassword(toEncode, "SHA-512");
		}

		/**
		 * Performs the diggestion of the message in {@code toEncode} through the
		 * algorithm specified by {@code digestAlgorithm}.
		 * <p>
		 * Realiza la digesti&oacute;n del mensaje en {@code toEncode} utilizando el
		 * algoritmo especificado por {@code digestAlgorithm}.
		 * </p>
		 *
		 * @param toEncode
		 *            a string containing the message to digest
		 * @param digestAlgorithm
		 *            a string containing the algorithm's name to use
		 * @return a string representing the received message digested with an
		 *         implementation of the specified algorithm. This string starts with
		 *         the prefix "{SHA-512}". un objeto string que representa el mensaje
		 *         recibido digerido con una implementaci&oacute;n del algoritmo
		 *         especificado. Este string comienza con el prefijo "{SHA-512}".
		 * @throws java.security.NoSuchAlgorithmException
		 *             if there is no implementation of the specified algorithm
		 *             available in the environment.
		 *             <p>
		 *             si no hay una implementaci&oacute;n del algoritmo especificado
		 *             disponible en el ambiente.
		 *             </p>
		 */
		public static String comparablePassword(String toEncode, String digestAlgorithm) throws NoSuchAlgorithmException {
			MessageDigest messageDigest = MessageDigest.getInstance(digestAlgorithm);
			byte[] bits = null;
			try {
				bits = toEncode.getBytes("ISO8859-1");
			} catch (UnsupportedEncodingException uee) {
				throw new NoSuchAlgorithmException("Can't get bytes from string in ISO8859-1", uee);
			}
			return "{" + digestAlgorithm + "}" + SFBase64.encodeBytes(messageDigest.digest(bits), false);
		}

		/**
		 * Encrypts data applying the Advanced Encryption Standard (AES) algorithm and a
		 * provided phrase as a password.
		 * <p>
		 * Encripta informaci&oacute;n aplicando el algoritmo Advanced Encryption
		 * Standard (AES) y una frase proporcionada a manera de contrase&ntilde;a.
		 * </p>
		 *
		 * @param passPhrase
		 *            a string used as a password for decrypting the data afterward.
		 * @param data
		 *            an array of bytes containing the information to encrypt
		 * @return an array of bytes containing the encrypted information.
		 *
		 * @throws java.security.GeneralSecurityException
		 *             if the key, derived from
		 * @throws GeneralSecurityException
		 *             the general security exception {@code passPhrase}, used in the
		 *             encryption process is not valid.
		 *             <p>
		 *             si la llave, derivada de {@code passPhrase}, utilizada en el
		 *             proceso de encripci&oacute;n no es v&aacute;lida.
		 *             </p>
		 */
		public static byte[] PBEAES128Cipher(String passPhrase, byte[] data) throws GeneralSecurityException {
			SecretKey secretKey = getSecretKey(passPhrase, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return cipher.doFinal(data);
		}

		/**
		 * Decrypts data applying the Advanced Encryption Standard (AES) algorithm and a
		 * provided phrase as a password, used previously for the encryption process.
		 * <p>
		 * Decripta informaci&oacute;n aplicando el algoritmo Advanced Encryption
		 * Standard (AES) y una frase proporcionada a manera de contrase&ntilde;a, que
		 * debi&oacute; ser usada durante el proceso de encriptaci&oacute;n.
		 * </p>
		 *
		 * @param passPhrase
		 *            a string used as a password for decrypting the data. This string
		 *            must be the same that was used for encrypting {@code data}.
		 * @param data
		 *            an array of bytes containing the information to decrypt
		 * @return an array of bytes containing the decrypted information.
		 *
		 * @throws java.security.GeneralSecurityException
		 *             if the key, derived from
		 * @throws GeneralSecurityException
		 *             the general security exception {@code passPhrase}, used in the
		 *             decryption process is not valid.
		 *             <p>
		 *             si la llave, derivada de {@code passPhrase}, utilizada en el
		 *             proceso de decripci&oacute;n no es v&aacute;lida.
		 *             </p>
		 */
		public static byte[] PBEAES128Decipher(String passPhrase, byte[] data) throws GeneralSecurityException {
			SecretKey secretKey = getSecretKey(passPhrase, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return cipher.doFinal(data);
		}

		/**
		 * Gets a {@link SecretKey} object from a String passphrase.
		 * @param passPhrase Passphrase.
		 * @param algorithm Algorithm.
		 * @return SecretKey
		 */
		private static SecretKey getSecretKey(String passPhrase, String algorithm) {
			byte[] key = new byte[16];
			byte[] tmp = passPhrase.getBytes();
			int pos = 0;
			while (pos < 16) {
				System.arraycopy(tmp, 0, key, pos, Math.min(16 - pos, tmp.length));
				pos += tmp.length;
			}
			return new SecretKeySpec(key, algorithm);
		}

		/**
		 * Generates a 512bit DH key pair for secure exchange of symetric keys.
		 *
		 * @return a 512bit DH key pair
		 */
		public static KeyPair genDH512KeyPair() {
			try {
				// G:5921382131818732078214846392362267449901665998994312391474243991527053233608335346713531453486300649663919884743851175664346252910611325867870956952765311
				// P:7320146440434454524904417198968017809897422432177536304525783639409510677068356316239906231779766559317603787655735698196575597092483528499492916137131397
				// L:511
				String seed = java.lang.management.ManagementFactory.getRuntimeMXBean().getName()
						+ System.currentTimeMillis();
				BigInteger p = new BigInteger(
						"7320146440434454524904417198968017809897422432177536304525783639409510677068356316239906231779766559317603787655735698196575597092483528499492916137131397");
				BigInteger g = new BigInteger(
						"5921382131818732078214846392362267449901665998994312391474243991527053233608335346713531453486300649663919884743851175664346252910611325867870956952765311");
				int l = 511;
				DHParameterSpec dhSpec = new DHParameterSpec(p, g, l);
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DiffieHellman");

				keyGen.initialize(dhSpec, new SecureRandom(seed.getBytes()));
				return keyGen.generateKeyPair();
			} catch (Exception e) {
				LOG.error(e);
				assert (false);
			}
			return null;
		}

		/**
		 * Generates a new RSA KeyPair
		 *
		 * @return a new RSA KeyPair
		 */
		public static KeyPair genRSAKeyPair() {
			try {
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
				keyGen.initialize(1024);
				return keyGen.genKeyPair();
			} catch (Exception e) {
				LOG.error(e);
				assert (false);
			}
			return null;
		}

		/**
		 * Storable kp.
		 *
		 * @return the string[]
		 */
		public static String[] storableKP() {
			String[] llaves = new String[2];
			KeyPair kp = genDH512KeyPair();
			llaves[0] = kp.getPrivate().getFormat() + "|" + SFBase64.encodeBytes(kp.getPrivate().getEncoded(), false);
			llaves[1] = kp.getPublic().getFormat() + "|" + SFBase64.encodeBytes(kp.getPublic().getEncoded(), false);
			return llaves;
		}

		/**
		 * Converts an Hex String into a byte array
		 *
		 * @param s
		 *            String to convert
		 * @return byte array of converted string
		 */
		public static byte[] hexStringToByteArray(String s) {
			int len = s.length();
			byte[] data = new byte[len / 2];
			for (int i = 0; i < len; i += 2) {
				data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
			}
			return data;
		}

		/**
		 * Converts an byte array into a Hex String
		 *
		 * @param arr
		 *            byte array
		 * @return String
		 */
		public static String byteArrayToHexString(byte[] arr) {
			String ret = "";
			Formatter formatter = new Formatter();
			for (byte b : arr) {
				formatter.format("%02x", b);
			}
			ret = formatter.toString();
			formatter.close();
			return ret;
		}

		/**
		 * decrypt an RSA passowrd
		 *
		 * @param password
		 * @return
		 */
		public static String decryptPassword(String password, java.security.KeyPair RSA512key) throws GeneralSecurityException {
			Cipher c = Cipher.getInstance("RSA");
			c.init(Cipher.DECRYPT_MODE, RSA512key.getPrivate());
			return new String(c.doFinal(hexStringToByteArray(password)));
		}

		/**
		 * Generates a Secure random script of 64 bits, encoded in a String
		 *
		 * @return String encoded nuance
		 */
		public static String genCryptoToken() {
			BigInteger number = BigInteger.probablePrime(64, sr);
			return number.toString(36);
		}

	}

	/**
	 * Container for operations on collections of data.
	 * <p>
	 * Contenedor de operaciones sobre colecciones de informaci&oacute;n.
	 * </p>
	 */
	public static class Collections {

		private Collections () {}

		/**
		 * Copies the elements in an {@code iterator} into an {@code arrayList}.
		 * <p>
		 * Copia los elementos de un objeto {@code iterator} en un objeto
		 * {@code arrayList}.
		 * </p>
		 *
		 * @param it
		 *            an {@code iterator} from which the elements will be copied.
		 * @return a {@code list} formed by the same elements {@code it} has. un objeto
		 *         {@code list} formado por los mismos elementos que tiene {@code it}.
		 */
		public static List copyIterator(Iterator it) {
			List ret = new ArrayList();
			while (it.hasNext()) {
				Object ref = it.next();
				ret.add(ref);
			}
			return ret;
		}

		/**
		 * Calculates the number of elements in an {@code iterator} and returns that
		 * number.
		 * <p>
		 * Calcula el n&uacute;mero de elementos en un objeto {@code iterator} y regresa
		 * ese n&uacute;mero.
		 * </p>
		 *
		 * @param it
		 *            an {@code iterator} from which the number of elements will be
		 *            calculated.
		 * @return the number of elements in the {@code iterator} received.
		 *
		 */
		public static long sizeOf(Iterator it) {
			long size = 0;
			while (it.hasNext()) {
				it.next();
				size++;
			}
			return size;
		}
	}
}
