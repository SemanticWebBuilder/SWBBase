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
package org.semanticwb.base.util;

import org.semanticwb.Logger;
import org.semanticwb.SWBUtils;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import java.io.*;
import java.util.*;

/**
 * Class to manage SWBPortal configuration properties.
 * @author Javier Solis Gonzalez
 */
public class SWBProperties extends Properties {
	static final Logger LOG = SWBUtils.getLogger(SWBProperties.class);
	private boolean readOnly = false;
	private static final String PREFIX = "_comm_";
	private static final String KEYVALUESEPARATORS = "=: \t\r\n\f";
	private static final String STRICTKEYVALUESEPARATORS = "=:";
	private static final String SPECIALSAVECHARS = "=: \t\r\n\f#!";
	private static final String WHITESPACECHARS = " \t\r\n\f";
	private ArrayList<String> arr = new ArrayList<>();
	private boolean changed = false;
	/** A table of hex digits. */
	private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };

	/**
	 * Constructor. Creates an empty property list with no default values.
	 */
	public SWBProperties() {
		this(null);
	}

	/**
	 * Constructor. Creates an empty property list with the specified defaults.
	 * @param defaults the default properties.
	 */
	public SWBProperties(Properties defaults) {
		super(defaults);
	}

	/**
	 * Clones properties from a {@link Properties} object.
	 * @param source source {@link Properties}
	 */
	public void copy(Properties source) {
		if (null !=  source) {
			Iterator it = source.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				setProperty(key, source.getProperty(key));
				if (!arr.contains(key)) {
					arr.add(key);
				}
			}
		}
	}

	@Override
	public synchronized Object setProperty(String key, String value) {
		setChanged(true);
		return super.setProperty(key, value);
	}

	/**
	 * Calls the <tt>Hashtable</tt> method <code>put</code>. Provided for
	 * parallelism with the <tt>getProperty</tt> method. Enforces use of strings for
	 * property keys and values. The value returned is the result of the
	 * <tt>Hashtable</tt> call to <code>put</code>.
	 * 
	 * @param key
	 *            the key to be placed into this property list.
	 * @param value
	 *            the value corresponding to <tt>key</tt>.
	 * @param comment
	 *            the comment
	 * @return the previous value of the specified key in this property list, or if
	 *         it did not have one.
	 * @see #getProperty
	 * @since 1.2
	 */
	public synchronized Object setProperty(String key, String value, String comment) {
		setChanged(true);
		StringBuilder com = new StringBuilder();
		if (!arr.contains(key)) {
			arr.add(key);
		}
		if (comment != null) {
			InputStream inb = new ByteArrayInputStream(comment.getBytes());
			try (BufferedReader in = new BufferedReader(new InputStreamReader(inb, "8859_1"))) {
				String line;
				while ((line = in.readLine()) != null) {
					if (line.length() > 0) {
						if (line.charAt(0) != '#') {
							com.append("#" + line + "\r\n");
						} else {
							com.append(line + "\r\n");
						}
					} else {
						com.append("\r\n");
					}
				}
			} catch (Exception e) {
				LOG.error(e);
			}
			put(PREFIX + key, com.toString());
		}
		return put(key, value);
	}

	/**
	 * @deprecated Use {@link #isChanged()}.
	 * Checks for change it.
	 * @return true, if successful
	 */
	public boolean hasChangeIt() {
		return isChange();
	}

	@Override
	public synchronized void load(InputStream inStream) throws IOException {
		setChanged(false);
		arr.clear();
		clear();
		StringBuilder buf = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(inStream, "8859_1"));
		while (true) {
			// Get next line
			String line = in.readLine();
			if (line == null) {
				return;
			}

			if (line.length() > 0) {

				// Find start of key
				int len = line.length();
				int keyStart;
				for (keyStart = 0; keyStart < len; keyStart++) {
					if (WHITESPACECHARS.indexOf(line.charAt(keyStart)) == -1) {
						break;
					}
				}

				// Blank lines are ignored
				if (keyStart == len) {
					continue;
				}

				// Continue lines that end in slashes if they are not comments
				char firstChar = line.charAt(keyStart);
				if ((firstChar != '#') && (firstChar != '!')) {
					while (continueLine(line)) {
						String nextLine = in.readLine();
						if (nextLine == null) {
							nextLine = "";
						}
						String loppedLine = line.substring(0, len - 1);
						// Advance beyond whitespace on new line
						int startIndex;
						for (startIndex = 0; startIndex < nextLine.length(); startIndex++) {
							if (WHITESPACECHARS.indexOf(nextLine.charAt(startIndex)) == -1) {
								break;
							}
						}
						nextLine = nextLine.substring(startIndex, nextLine.length());
						line = loppedLine + nextLine;
						len = line.length();
					}

					// Find separation between key and value
					int separatorIndex;
					for (separatorIndex = keyStart; separatorIndex < len; separatorIndex++) {
						char currentChar = line.charAt(separatorIndex);
						if (currentChar == '\\') {
							separatorIndex++;
						} else if (KEYVALUESEPARATORS.indexOf(currentChar) != -1) {
							break;
						}
					}

					// Skip over whitespace after key if any
					int valueIndex;
					for (valueIndex = separatorIndex; valueIndex < len; valueIndex++) {
						if (WHITESPACECHARS.indexOf(line.charAt(valueIndex)) == -1) {
							break;
						}
					}

					// Skip over one non whitespace key value separators if any
					if (valueIndex < len && STRICTKEYVALUESEPARATORS.indexOf(line.charAt(valueIndex)) != -1) {
						valueIndex++;
					}

					// Skip over white space after other separators if any
					while (valueIndex < len) {
						if (WHITESPACECHARS.indexOf(line.charAt(valueIndex)) == -1) {
							break;
						}
						valueIndex++;
					}
					String key = line.substring(keyStart, separatorIndex);
					String value = (separatorIndex < len) ? line.substring(valueIndex, len) : "";

					// Convert then store key and value
					key = loadConvert(key);
					value = loadConvert(value);
					if (!arr.contains(key)) {
						arr.add(key);
					}
					put(key, value);
					put(PREFIX + key, buf);
					buf = new StringBuilder();
				} else {
					buf.append(line).append("\r\n");
				}
			} else {
				buf.append("\r\n");
			}
		}
	}

	/**
	 * Load convert.
	 * @param theString the the string
	 * @return the string
	 */
	private String loadConvert(String theString) {
		char aChar;
		int len = theString.length();
		StringBuilder outBuffer = new StringBuilder(len);

		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
						}
					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't') {
						aChar = '\t';
					} else if (aChar == 'r') {
						aChar = '\r';
					} else if (aChar == 'n') {
						aChar = '\n';
					} else if (aChar == 'f') {
						aChar = '\f';
					}
					outBuffer.append(aChar);
				}
			} else {
				outBuffer.append(aChar);
			}
		}
		return outBuffer.toString();
	}

	@Override
	public Enumeration propertyNames() {
		Hashtable h = new Hashtable();
		toEnumeration(h);
		return h.keys();
	}

	/**
	 * Property ordered names.
	 * @return the enumeration.
	 */
	public Enumeration propertyOrderedNames() {
		if (arr.isEmpty()) {
			return propertyNames();
		}
		return Collections.enumeration(arr);
	}

	/**
	 * Enumerates all key/value pairs in the specified hashtable.
	 * @param h the hashtable
	 */
	private synchronized void toEnumeration(Hashtable h) {
		if (defaults != null) {
			for (Enumeration e = defaults.keys(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				if (!key.startsWith(PREFIX)) {
					h.put(key, defaults.get(key));
				}
			}
		}
		for (Enumeration e = keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			if (!key.startsWith(PREFIX)) {
				h.put(key, get(key));
			}
		}
	}

	/**
	 * Searches for the property with the specified key in this property list. If
	 * the key is not found in this property list, the default property list, and
	 * its defaults, recursively, are then checked. The method returns
	 * <code>null</code> if the property is not found.
	 * 
	 * @param key the key
	 * @return the value in this property list with the specified key value.
	 */
	public String getComment(String key) {
		key = PREFIX + key;
		Object oval = super.get(key);
		String sval = (oval instanceof String) ? (String) oval : null;
		return ((sval == null) && (defaults != null)) ? defaults.getProperty(key) : sval;
	}


	/**
	 * Converts unicodes to encoded &#92;uxxxx and writes out any of the characters
	 * in specialSaveChars with a preceding slash.
	 * 
	 * @param theString the the string
	 * @param escapeSpace the escape space
	 * @return the string
	 */
	private String saveConvert(String theString, boolean escapeSpace) {
		int len = theString.length();
		StringBuilder outBuffer = new StringBuilder(len * 2);

		for (int x = 0; x < len; x++) {
			char aChar = theString.charAt(x);
			switch (aChar) {
			case ' ':
				if (x == 0 || escapeSpace) {
					outBuffer.append('\\');
				}

				outBuffer.append(' ');
				break;
			case '\\':
				outBuffer.append('\\');
				outBuffer.append('\\');
				break;
			case '\t':
				outBuffer.append('\\');
				outBuffer.append('t');
				break;
			case '\n':
				outBuffer.append('\\');
				outBuffer.append('n');
				break;
			case '\r':
				outBuffer.append('\\');
				outBuffer.append('r');
				break;
			case '\f':
				outBuffer.append('\\');
				outBuffer.append('f');
				break;
			default:
				if ((aChar < 0x0020) || (aChar > 0x007e)) {
					outBuffer.append('\\');
					outBuffer.append('u');
					outBuffer.append(toHex((aChar >> 12) & 0xF));
					outBuffer.append(toHex((aChar >> 8) & 0xF));
					outBuffer.append(toHex((aChar >> 4) & 0xF));
					outBuffer.append(toHex(aChar & 0xF));
				} else {
					if (SPECIALSAVECHARS.indexOf(aChar) != -1) {
						outBuffer.append('\\');
					}
					outBuffer.append(aChar);
				}
			}
		}
		return outBuffer.toString();
	}


	/**
	 * Returns true if the given line is a line that must be appended to the next line.
	 * @param line the line
	 * @return true, if successful
	 */
	private boolean continueLine(String line) {
		int slashCount = 0;
		int index = line.length() - 1;
		while ((index >= 0) && (line.charAt(index--) == '\\')) {
			slashCount++;
		}
		return (slashCount % 2 != 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Hashtable#hashCode()
	 */
	@Override
	public synchronized int hashCode() {
		return super.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Hashtable#equals(java.lang.Object)
	 */
	@Override
	public synchronized boolean equals(Object o) {
		return super.equals(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Hashtable#remove(java.lang.Object)
	 */
	@Override
	public Object remove(Object key) {
		setChanged(true);
		arr.remove(key);
		super.remove(PREFIX + key);
		return super.remove(key);
	}

	@Override
	public synchronized void store(OutputStream out, String header) throws IOException {
		setChanged(false);
		BufferedWriter awriter;
		awriter = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
		if (header != null) {
			writeln(awriter, "#" + header);
			writeln(awriter, "#" + new Date().toString());
		}
		for (Enumeration e = Collections.enumeration(arr); e.hasMoreElements();) {
			String key = (String) e.nextElement();

			String comm = (String) get(PREFIX + key);
			if (comm != null) {
				awriter.write(comm);
			}

			String val = (String) get(key);
			key = saveConvert(key, true);

			/*
			 * No need to escape embedded and trailing spaces for value, hence pass false to
			 * flag.
			 */
			val = saveConvert(val, false);
			writeln(awriter, key + "=" + val);
		}
		awriter.flush();
	}

	/**
	 * Writes a new line to the given {@link BufferedWriter} after the provided String.
	 * @param bw the {@link BufferedWriter}
	 * @param s the String to write.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void writeln(BufferedWriter bw, String s) throws IOException {
		bw.write(s);
		bw.newLine();
	}

	/**
	 * Converts a nibble to a hex character.
	 * @param nibble the nibble
	 * @return the hex character
	 */
	private static char toHex(int nibble) {
		return hexDigit[(nibble & 0xF)];
	}

	/**
	 * Getter for property readOnly.
	 * @return Value of property readOnly.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Setter for property readOnly.
	 * @param readOnly New value of property readOnly.
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * @deprecated Use {@link #isChanged()}.
	 * Checks if is change.
	 * @return true, if is change
	 */
	@Deprecated
	public boolean isChange() {
		return changed;
	}

	/***
	 * Gets the changed property.
	 * @return Changed property;
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * @deprecated Use {@link #setChanged(boolean)}.
	 * Sets the change.
	 * @param changed New value.
	 */
	@Deprecated
	public synchronized void setChange(boolean changed) {
		this.changed = changed;
	}

	/***
	 * Sets the changed property.
	 * @param changed New value.
	 */
	public synchronized void setChanged(boolean changed) {
		this.changed = changed;
	}
}
