/*
 * SemanticWebBuilder es una plataforma para el desarrollo de portales y aplicaciones de integración,
 * colaboración y conocimiento, que gracias al uso de tecnología semántica puede generar contextos de
 * información alrededor de algún tema de interés o bien integrar información y aplicaciones de diferentes
 * fuentes, donde a la información se le asigna un significado, de forma que pueda ser interpretada y
 * procesada por personas y/o sistemas, es una creación original del Fondo de Información y Documentación
 * para la Industria INFOTEC, cuyo registro se encuentra actualmente en trámite.
 *
 * INFOTEC pone a su disposición la herramienta SemanticWebBuilder a través de su licenciamiento abierto al público (‘open source’),
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
 *  http://www.semanticwebbuilder.org
 */
package org.semanticwb.base.util;

import org.semanticwb.SWBUtils;
import org.semanticwb.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * Objeto: para el manejo de archivos de propiedades.
 * @author Javier Solis Gonzalez
 */
public class SWBProperties extends Properties {

    /** The log. */
    Logger log = SWBUtils.getLogger(SWBProperties.class);
    
    /** The read only. */
    private boolean readOnly = false;
    
    /** The Constant PREFIX. */
    private final static String PREFIX = "_comm_";
    
    /** The Constant keyValueSeparators. */
    private static final String keyValueSeparators = "=: \t\r\n\f";
    
    /** The Constant strictKeyValueSeparators. */
    private static final String strictKeyValueSeparators = "=:";
    
    /** The Constant specialSaveChars. */
    private static final String specialSaveChars = "=: \t\r\n\f#!";
    
    /** The Constant whiteSpaceChars. */
    private static final String whiteSpaceChars = " \t\r\n\f";
    
    /** The arr. */
    private Vector<String> arr = new Vector<>();
    
    /** The change. */
    private boolean change = false;

    /**
     * Creates an empty property list with no default values.
     */
    public SWBProperties()
    {
        this(null);
    }

    /**
     * Creates an empty property list with the specified defaults.
     * 
     * @param defaults the defaults
     */
    public SWBProperties(Properties defaults)
    {
        super(defaults);
    }

    /**
     * Copia el contenido de properties
     * ejemplo:
     * System.getProperties
     * 
     * @param source the source
     */
    public void copy(Properties source)
    {
        Iterator it = source.keySet().iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            setProperty(key, source.getProperty(key));
            if (!arr.contains(key))
            {
                arr.add(key);
            }
        }
    }

    /**
     * Calls the <tt>Hashtable</tt> method <code>put</code>. Provided for
     * parallelism with the <tt>getProperty</tt> method. Enforces use of
     * strings for property keys and values. The value returned is the
     * result of the <tt>Hashtable</tt> call to <code>put</code>.
     * 
     * @param key the key to be placed into this property list.
     * @param value the value corresponding to <tt>key</tt>.
     * @return     the previous value of the specified key in this property
     * list, or  if it did not have one.
     * @see #getProperty
     * @since    1.2
     */
    @Override
    public synchronized Object setProperty(String key, String value)
    {
        setChange(true);
        return super.setProperty(key, value);
    }

    /**
     * Calls the <tt>Hashtable</tt> method <code>put</code>. Provided for
     * parallelism with the <tt>getProperty</tt> method. Enforces use of
     * strings for property keys and values. The value returned is the
     * result of the <tt>Hashtable</tt> call to <code>put</code>.
     * 
     * @param key the key to be placed into this property list.
     * @param value the value corresponding to <tt>key</tt>.
     * @param comment the comment
     * @return the previous value of the specified key in this property
     * list, or  if it did not have one.
     * @see #getProperty
     * @since 1.2
     */
    public synchronized Object setProperty(String key, String value, String comment)
    {
        setChange(true);
        StringBuilder com = new StringBuilder();
        if (!arr.contains(key))
        {
            arr.add(key);
        }
        if (comment != null)
        {
            java.io.StringBufferInputStream inb = new java.io.StringBufferInputStream(comment);
            try
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(inb, "8859_1"));
                String line = null;
                while ((line = in.readLine()) != null)
                {
                    if (line.length() > 0)
                    {
                        if (line.charAt(0) != '#')
                        {
                            com.append("#" + line + "\r\n");
                        } else
                        {
                            com.append(line + "\r\n");
                        }
                    } else
                    {
                        com.append("\r\n");
                    }
                }
            } catch (Exception e)
            {
                log.error(e);
            }
            put(PREFIX + key, com.toString());
        }
        return put(key, value);
    }

    /**
     * Checks for change it.
     * 
     * @return true, if successful
     * @return
     */
    public boolean hasChangeIt()
    {
        return isChange();
    }

    /**
     * Reads a property list (key and element pairs) from the input
     * stream.  The stream is assumed to be using the ISO 8859-1
     * character encoding; that is each byte is one Latin1 character.
     * Characters not in Latin1, and certain special characters, can
     * be represented in keys and elements using escape sequences
     * similar to those used for character and string literals (see <a
     * href="http://java.sun.com/docs/books/jls/second_edition/html/lexical.doc.html#100850">&sect;3.3</a>
     * and <a
     * href="http://java.sun.com/docs/books/jls/second_edition/html/lexical.doc.html#101089">&sect;3.10.6</a>
     * of the <i>Java Language Specification</i>).
     * 
     * The differences from the character escape sequences used for
     * characters and strings are:
     * 
     * <ul>
     * <li> Octal escapes are not recognized.
     * 
     * <li> The character sequence <code>\b</code> does <i>not</i>
     * represent a backspace character.
     * 
     * <li> The method does not treat a backslash character,
     * <code>\</code>, before a non-valid escape character as an
     * error; the backslash is silently dropped.  For example, in a
     * Java string the sequence <code>"\z"</code> would cause a
     * compile time error.  In contrast, this method silently drops
     * the backslash.  Therefore, this method treats the two character
     * sequence <code>"\b"</code> as equivalent to the single
     * character <code>'b'</code>.
     * 
     * <li> Escapes are not necessary for single and double quotes;
     * however, by the rule above, single and double quote characters
     * preceded by a backslash still yield single and double quote
     * characters, respectively.
     * 
     * </ul>
     * 
     * An <code>IllegalArgumentException</code> is thrown if a
     * malformed Unicode escape appears in the input.
     * 
     * <p>
     * This method processes input in terms of lines.  A natural line
     * of input is terminated either by a set of line terminator
     * characters (<code>\n</code> or <code>\r</code> or
     * <code>\r\n</code>) or by the end of the file.  A natural line
     * may be either a blank line, a comment line, or hold some part
     * of a key-element pair.  The logical line holding all the data
     * for a key-element pair may be spread out across several adjacent
     * natural lines by escaping the line terminator sequence with a
     * backslash character, <code>\</code>.  Note that a comment line
     * cannot be extended in this manner; every natural line that is a
     * comment must have its own comment indicator, as described
     * below.  If a logical line is continued over several natural
     * lines, the continuation lines receive further processing, also
     * described below.  Lines are read from the input stream until
     * end of file is reached.
     * 
     * <p>
     * A natural line that contains only white space characters is
     * considered blank and is ignored.  A comment line has an ASCII
     * <code>'#'</code> or <code>'!'</code> as its first non-white
     * space character; comment lines are also ignored and do not
     * encode key-element information.  In addition to line
     * terminators, this method considers the characters space
     * (<code>' '</code>, <code>'&#92;u0020'</code>), tab
     * (<code>'\t'</code>, <code>'&#92;u0009'</code>), and form feed
     * (<code>'\f'</code>, <code>'&#92;u000C'</code>) to be white
     * space.
     * 
     * <p>
     * If a logical line is spread across several natural lines, the
     * backslash escaping the line terminator sequence, the line
     * terminator sequence, and any white space at the start the
     * following line have no affect on the key or element values.
     * The remainder of the discussion of key and element parsing will
     * assume all the characters constituting the key and element
     * appear on a single natural line after line continuation
     * characters have been removed.  Note that it is <i>not</i>
     * sufficient to only examine the character preceding a line
     * terminator sequence to to see if the line terminator is
     * escaped; there must be an odd number of contiguous backslashes
     * for the line terminator to be escaped.  Since the input is
     * processed from left to right, a non-zero even number of
     * 2<i>n</i> contiguous backslashes before a line terminator (or
     * elsewhere) encodes <i>n</i> backslashes after escape
     * processing.
     * 
     * <p>
     * The key contains all of the characters in the line starting
     * with the first non-white space character and up to, but not
     * including, the first unescaped <code>'='</code>,
     * <code>':'</code>, or white space character other than a line
     * terminator. All of these key termination characters may be
     * included in the key by escaping them with a preceding backslash
     * character; for example,<p>
     * 
     * <code>\:\=</code><p>
     * 
     * would be the two-character key <code>":="</code>.  Line
     * terminator characters can be included using <code>\r</code> and
     * <code>\n</code> escape sequences.  Any white space after the
     * key is skipped; if the first non-white space character after
     * the key is <code>'='</code> or <code>':'</code>, then it is
     * ignored and any white space characters after it are also
     * skipped.  All remaining characters on the line become part of
     * the associated element string; if there are no remaining
     * characters, the element is the empty string
     * <code>&quot;&quot;</code>.  Once the raw character sequences
     * constituting the key and element are identified, escape
     * processing is performed as described above.
     * 
     * <p>
     * As an example, each of the following three lines specifies the key
     * <code>"Truth"</code> and the associated element value
     * <code>"Beauty"</code>:
     * <p>
     * <pre>
     * Truth = Beauty
     * Truth:Beauty
     * Truth			:Beauty
     * </pre>
     * As another example, the following three lines specify a single
     * property:
     * <p>
     * <pre>
     * fruits                           apple, banana, pear, \
     * cantaloupe, watermelon, \
     * kiwi, mango
     * </pre>
     * The key is <code>"fruits"</code> and the associated element is:
     * <p>
     * <pre>"apple, banana, pear, cantaloupe, watermelon, kiwi, mango"</pre>
     * Note that a space appears before each <code>\</code> so that a space
     * will appear after each comma in the final result; the <code>\</code>,
     * line terminator, and leading white space on the continuation line are
     * merely discarded and are <i>not</i> replaced by one or more other
     * characters.
     * <p>
     * As a third example, the line:
     * <p>
     * <pre>cheeses
     * </pre>
     * specifies that the key is <code>"cheeses"</code> and the associated
     * element is the empty string <code>""</code>.<p>
     * 
     * @param inStream the in stream
     * @throws IOException Signals that an I/O exception has occurred.
     * @exception  IOException  if an error occurred when reading from the
     * input stream.
     * @throws	   IllegalArgumentException if the input stream contains a
     * malformed Unicode escape sequence.
     */
    @Override
    public synchronized void load(InputStream inStream) throws IOException
    {
        setChange(false);
        arr.clear();
        clear();
        String buf = "";
        BufferedReader in = new BufferedReader(new InputStreamReader(inStream, "8859_1"));
        while (true)
        {
            // Get next line
            String line = in.readLine();
            if (line == null)
            {
                return;
            }

            if (line.length() > 0)
            {

                // Find start of key
                int len = line.length();
                int keyStart;
                for (keyStart = 0; keyStart < len; keyStart++)
                {
                    if (whiteSpaceChars.indexOf(line.charAt(keyStart)) == -1)
                    {
                        break;
                    }
                }

                // Blank lines are ignored
                if (keyStart == len)
                {
                    continue;
                }

                // Continue lines that end in slashes if they are not comments
                char firstChar = line.charAt(keyStart);
                if ((firstChar != '#') && (firstChar != '!'))
                {
                    //System.out.println("Si:"+line);
                    while (continueLine(line))
                    {
                        String nextLine = in.readLine();
                        if (nextLine == null)
                        {
                            nextLine = "";
                        }
                        String loppedLine = line.substring(0, len - 1);
                        // Advance beyond whitespace on new line
                        int startIndex;
                        for (startIndex = 0; startIndex < nextLine.length(); startIndex++)
                        {
                            if (whiteSpaceChars.indexOf(nextLine.charAt(startIndex)) == -1)
                            {
                                break;
                            }
                        }
                        nextLine = nextLine.substring(startIndex, nextLine.length());
                        line = loppedLine + nextLine;
                        len = line.length();
                    }

                    // Find separation between key and value
                    int separatorIndex;
                    for (separatorIndex = keyStart; separatorIndex < len; separatorIndex++)
                    {
                        char currentChar = line.charAt(separatorIndex);
                        if (currentChar == '\\')
                        {
                            separatorIndex++;
                        } else if (keyValueSeparators.indexOf(currentChar) != -1)
                        {
                            break;
                        }
                    }

                    // Skip over whitespace after key if any
                    int valueIndex;
                    for (valueIndex = separatorIndex; valueIndex < len; valueIndex++)
                    {
                        if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                        {
                            break;
                        }
                    }

                    // Skip over one non whitespace key value separators if any
                    if (valueIndex < len && strictKeyValueSeparators.indexOf(line.charAt(valueIndex)) != -1)
                    {
                            valueIndex++;
                    }

                    // Skip over white space after other separators if any
                    while (valueIndex < len)
                    {
                        if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                        {
                            break;
                        }
                        valueIndex++;
                    }
                    String key = line.substring(keyStart, separatorIndex);
                    String value = (separatorIndex < len) ? line.substring(valueIndex, len) : "";

                    // Convert then store key and value
                    key = loadConvert(key);
                    value = loadConvert(value);
                    if (!arr.contains(key))
                    {
                        arr.add(key);
                    }
                    put(key, value);
                    put(PREFIX + key, buf);
                    buf = "";
                } else
                {
                    buf += line + "\r\n";
                }
            } else
            {
                buf += "\r\n";
            }
        }
    }

    /**
     * Load convert.
     * 
     * @param theString the the string
     * @return the string
     */
    private String loadConvert(String theString)
    {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);

        for (int x = 0; x < len;)
        {
            aChar = theString.charAt(x++);
            if (aChar == '\\')
            {
                aChar = theString.charAt(x++);
                if (aChar == 'u')
                {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++)
                    {
                        aChar = theString.charAt(x++);
                        switch (aChar)
                        {
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
                                throw new IllegalArgumentException(
                                        "Malformed \\uxxxx encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else
                {
                    if (aChar == 't')
                    {
                        aChar = '\t';
                    } else if (aChar == 'r')
                    {
                        aChar = '\r';
                    } else if (aChar == 'n')
                    {
                        aChar = '\n';
                    } else if (aChar == 'f')
                    {
                        aChar = '\f';
                    }
                    outBuffer.append(aChar);
                }
            } else
            {
                outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }

    /**
     * Returns an enumeration of all the keys in this property list,
     * including distinct keys in the default property list if a key
     * of the same name has not already been found from the main
     * properties list.
     *
     * @return  an enumeration of all the keys in this property list, including
     *          the keys in the default property list.
     * @see     java.util.Enumeration
     * @see     java.util.Properties#defaults
     */
    @Override
    public Enumeration propertyNames()
    {
        Hashtable h = new Hashtable();
        enumerate(h);
        return h.keys();
    }

    /**
     * Property ordered names.
     * 
     * @return the enumeration
     * @return
     */
    public Enumeration propertyOrderedNames()
    {
        if (arr.isEmpty())
        {
            return propertyNames();
        }
        return arr.elements();
    }

    /**
     * Enumerates all key/value pairs in the specified hastable.
     * @param h the hashtable
     */
    private synchronized void enumerate(Hashtable h)
    {
        if (defaults != null)
        {
            for (Enumeration e = defaults.keys(); e.hasMoreElements();)
            {
                String key = (String) e.nextElement();
                if (!key.startsWith(PREFIX))
                {
                    h.put(key, defaults.get(key));
                }
            }
        }
        for (Enumeration e = keys(); e.hasMoreElements();)
        {
            String key = (String) e.nextElement();
            if (!key.startsWith(PREFIX))
            {
                h.put(key, get(key));
            }
        }
    }

    /**
     * Searches for the property with the specified key in this property list.
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns
     * <code>null</code> if the property is not found.
     * 
     * @param key the key
     * @return  the value in this property list with the specified key value.
     * @see     #setProperty
     * @see     #defaults
     */
    public String getComment(String key)
    {
        key = PREFIX + key;
        Object oval = super.get(key);
        String sval = (oval instanceof String) ? (String) oval : null;
        return ((sval == null) && (defaults != null)) ? defaults.getProperty(key) : sval;
    }

    /*
     * Converts unicodes to encoded &#92;uxxxx
     * and writes out any of the characters in specialSaveChars
     * with a preceding slash
     */
    /**
     * Save convert.
     * 
     * @param theString the the string
     * @param escapeSpace the escape space
     * @return the string
     */
    private String saveConvert(String theString, boolean escapeSpace)
    {
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len * 2);

        for (int x = 0; x < len; x++)
        {
            char aChar = theString.charAt(x);
            switch (aChar)
            {
                case ' ':
                    if (x == 0 || escapeSpace)
                    {
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
                    if ((aChar < 0x0020) || (aChar > 0x007e))
                    {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >> 8) & 0xF));
                        outBuffer.append(toHex((aChar >> 4) & 0xF));
                        outBuffer.append(toHex(aChar & 0xF));
                    } else
                    {
                        if (specialSaveChars.indexOf(aChar) != -1)
                        {
                            outBuffer.append('\\');
                        }
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    /*
     * Returns true if the given line is a line that must
     * be appended to the next line
     */
    /**
     * Continue line.
     * 
     * @param line the line
     * @return true, if successful
     */
    private boolean continueLine(String line)
    {
        int slashCount = 0;
        int index = line.length() - 1;
        while ((index >= 0) && (line.charAt(index--) == '\\'))
        {
            slashCount++;
        }
        return (slashCount % 2 != 0);
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#hashCode()
     */
    @Override
    public synchronized int hashCode()
    {
        return super.hashCode();
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#equals(java.lang.Object)
     */
    @Override
    public synchronized boolean equals(Object o)
    {
        return super.equals(o);
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#remove(java.lang.Object)
     */
    @Override
    public Object remove(Object key)
    {
        setChange(true);
        arr.remove(key);
        super.remove(PREFIX + key);
        return super.remove(key);
    }

    /**
     * Writes this property list (key and element pairs) in this
     * <code>Properties</code> table to the output stream in a format suitable
     * for loading into a <code>Properties</code> table using the.
     * 
     * @param out the out
     * @param header the header
     * @throws IOException Signals that an I/O exception has occurred.
     * {@link #load(InputStream) load} method.
     * The stream is written using the ISO 8859-1 character encoding.
     * <p>
     * Properties from the defaults table of this <code>Properties</code>
     * table (if any) are <i>not</i> written out by this method.
     * <p>
     * If the header argument is not null, then an ASCII <code>#</code>
     * character, the header string, and a line separator are first written
     * to the output stream. Thus, the <code>header</code> can serve as an
     * identifying comment.
     * <p>
     * Next, a comment line is always written, consisting of an ASCII
     * <code>#</code> character, the current date and time (as if produced
     * by the <code>toString</code> method of <code>Date</code> for the
     * current time), and a line separator as generated by the Writer.
     * <p>
     * Then every entry in this <code>Properties</code> table is
     * written out, one per line. For each entry the key string is
     * written, then an ASCII <code>=</code>, then the associated
     * element string. Each character of the key and element strings
     * is examined to see whether it should be rendered as an escape
     * sequence. The ASCII characters <code>\</code>, tab, form feed,
     * newline, and carriage return are written as <code>\\</code>,
     * <code>\t</code>, <code>\f</code> <code>\n</code>, and
     * <code>\r</code>, respectively. Characters less than
     * <code>&#92;u0020</code> and characters greater than
     * <code>&#92;u007E</code> are written as
     * <code>&#92;u</code><i>xxxx</i> for the appropriate hexadecimal
     * value <i>xxxx</i>.  For the key, all space characters are
     * written with a preceding <code>\</code> character.  For the
     * element, leading space characters, but not embedded or trailing
     * space characters, are written with a preceding <code>\</code>
     * character. The key and element characters <code>#</code>,
     * <code>!</code>, <code>=</code>, and <code>:</code> are written
     * with a preceding backslash to ensure that they are properly loaded.
     * <p>
     * After the entries have been written, the output stream is flushed.  The
     * output stream remains open after this method returns.
     * @exception  IOException if writing this property list to the specified
     * output stream throws an <tt>IOException</tt>.
     * @exception  ClassCastException  if this <code>Properties</code> object
     * contains any keys or values that are not <code>Strings</code>.
     * @exception  NullPointerException  if <code>out</code> is null.
     * @since 1.2
     */
    @Override
    public synchronized void store(OutputStream out, String header)
            throws IOException
    {
        setChange(false);
        BufferedWriter awriter;
        awriter = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
        if (header != null)
        {
            writeln(awriter, "#" + header);
            writeln(awriter, "#" + new Date().toString());
        }
        for (Enumeration e = arr.elements(); e.hasMoreElements();)
        {
            String key = (String) e.nextElement();

            String comm = (String) get(PREFIX + key);
            if (comm != null)
            {
                awriter.write(comm);
            }

            String val = (String) get(key);
            key = saveConvert(key, true);

            /* No need to escape embedded and trailing spaces for value, hence
             * pass false to flag.
             */
            val = saveConvert(val, false);
            writeln(awriter, key + "=" + val);
        }
        awriter.flush();
    }

    /**
     * Writeln.
     * 
     * @param bw the bw
     * @param s the s
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void writeln(BufferedWriter bw, String s) throws IOException
    {
        bw.write(s);
        bw.newLine();
    }

    /**
     * Convert a nibble to a hex character.
     * 
     * @param nibble the nibble
     * @return the char
     * @param	nibble	the nibble to convert.
     */
    private static char toHex(int nibble)
    {
        return hexDigit[(nibble & 0xF)];
    }

    /**
     * Getter for property readOnly.
     * @return Value of property readOnly.
     */
    public boolean isReadOnly()
    {
        return readOnly;
    }

    /**
     * Setter for property readOnly.
     * @param readOnly New value of property readOnly.
     */
    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }
    
    /** A table of hex digits. */
    private static final char[] hexDigit =
    {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    /**
     * Checks if is change.
     * 
     * @return true, if is change
     */
    public boolean isChange()
    {
        return change;
    }

    /**
     * Sets the change.
     * 
     * @param change the new change
     */
    public synchronized void setChange(boolean change)
    {
        this.change = change;
    }
}
