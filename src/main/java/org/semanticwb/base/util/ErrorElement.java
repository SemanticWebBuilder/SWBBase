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

import java.util.Date;

/**
 * Class to hold information about an error. Used in SWBAErrorElementViewer of SemanticWebBuilder Portal
 * and {@link org.semanticwb.base.util.imp.Logger4jImpl} classes. Instances of this class are managed by
 * {@link org.semanticwb.SWBUtils.ERROR} static class and stored in memory as a
 * {@link org.semanticwb.SWBUtils} class attribute.
 * @author Javier Solis Gonzalez
 */
public class ErrorElement {
    static long counter;
    private long id = 0;
    private String msg;
    private java.util.Date date;
    private Throwable throwable;
    private Class cls;
    private String level;
    private static final String NEWLINE = "\n";

    /**
     * Gets the error class.
     * @return the error class
     */
    public Class getErrorClass() {
        return cls;
    }

    /**
     * Gets the error level
     * @return the error level.
     */
    public String getErrorLevel() {
        return level;
    }

    /**
     * Constructor. Creates a new instance of {@link ErrorElement}.
     * @param throwable the {@link Throwable} object related to the logged error.
     * @param message the error message.
     * @param cls class where error is generated.
     * @param level the error level.
     */
    public ErrorElement(Throwable throwable, String message, Class cls, String level) {
        id = getCounter();
        date = new java.util.Date();
        this.throwable = throwable;
        this.msg = message;
    }

    /**
     * Gets the error counter.
     * @return the counter.
     */
    public static synchronized long getCounter() {
        return counter++;
    }

    /**
     * Gets the error id.
     * @return error id.
     */
    public long getId() {
        return id;
    }

    /**
     * Gets error date.
     * @return error date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Gets the error {@link Throwable} object.
     * @return throwable.
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Gets the error message.
     * @return error message.
     */
    public String getMessage() {
        if (msg != null) {
            return msg; //La alternativa a este multiple return implica crear objetos que no se utilizaran
        }
        return throwable.toString();
    }

    /**
     * Gets the error stack trace.
     * @return the stack trace.
     */
    public String getStackTrace() {
        return printThrowable(throwable);
    }

    /**
     * Prints a {@link Throwable} object stack trace information.
     * @param throwableobj the {@link Throwable} object.
     * @return String with a stack trace information of a {@link Throwable}.
     */
    private String printThrowable(Throwable throwableobj) {
        StringBuilder bug = new StringBuilder();
        if (throwableobj != null) {
            bug.append(date).append(": ").append(throwableobj.getMessage()).append(NEWLINE);

            StackTraceElement []elements = throwableobj.getStackTrace();
            bug.append("// ").append(throwableobj).append(NEWLINE);

            for (int x = 0; x < elements.length; x++) {
                bug.append("// ").append(elements[x]).append(NEWLINE);
            }

            Throwable rth = throwableobj.getCause();
            if (rth != null && rth != throwableobj) {
                bug.append(NEWLINE).append("Root Cause:").append(NEWLINE).append(NEWLINE);
                bug.append(printThrowable(rth));
            }
        }
        return bug.toString();
    }
}
