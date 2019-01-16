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
package org.semanticwb.css.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * CSS Parser utility class.
 * @author victor.lorenzana
 */
//TODO: Migrate to use a maintained library instead of custom parsing because CSS grammar evolves
public class CSSParser {
    private ArrayList<Selector> selectors = new ArrayList<>();

    /**
     * Constructor. Creates a new instance of a {@link CSSParser}.
     * Provided css string is parsed immediately.
     * @param css CSS string.
     */
    public CSSParser(String css) {
        css = cleanComments(css);
        if (css.startsWith("@")) {
            css = css.substring(1);
            int pos = css.indexOf(';');
            if (pos != -1) {
                css = css.substring(pos + 1);
            }
        }

        css = css.replace("\r", " ").replace("\n", " ");
        int pos = css.indexOf('{');
        while (pos != -1) {
            String selectorrow = css.substring(0, pos).trim();
            HashSet<Attribute> atts = new HashSet<>();
            css = css.substring(pos + 1);
            int pos2 = css.indexOf('}');
            if (pos2 != -1) {
                String values = css.substring(0, pos2).trim();
                css = css.substring(pos2 + 1);
                StringTokenizer st = new StringTokenizer(values, ";");
                while (st.hasMoreTokens()) {
                    String attribute = st.nextToken();
                    pos = attribute.indexOf(':');
                    if(pos != -1) {
                        String name = attribute.substring(0,pos);
                        String value = attribute.substring(pos + 1);
                        Attribute att = new Attribute(name, value);
                        atts.add(att);
                    } else {
                        String name = attribute;
                        String value = "";
                        Attribute att = new Attribute(name, value);
                        atts.add(att);
                    }
                }
            } else {
                throw new IllegalArgumentException("The css is invalid");
            }
            
            for(String selector : getSelectors(selectorrow)) {
                selectors.add(new Selector(selector, atts));
            }
            pos = css.indexOf('{');
        }
    }
    
    /**
     * Gets the selectors of the CSS parsed string.
     * @param selector the selector.
     * @return the selectors.
     */
    private String[] getSelectors(String selector) {
        HashSet<String> getSelectors = new HashSet<>();
        selector = selector.replace(' ',',');
        StringTokenizer st = new StringTokenizer(selector,",");
        while(st.hasMoreTokens()) {
            String nselector = st.nextToken().trim();
            if(!nselector.equals("")) {
                getSelectors.add(nselector);                
            }            
        }
        return getSelectors.toArray(new String[getSelectors.size()]);
        
    }

    /**
     * Gets the selectors.
     * @return the selectors.
     */
    public Selector[] getSelectors() {
        return selectors.toArray(new Selector[selectors.size()]);
    }

    /**
     * Strips block comments from a CSS string.
     * @param css the css string.
     * @return css string without block comments.
     */
    private String cleanComments(String css) {
        StringBuilder cssclean = new StringBuilder();
        int pos = css.indexOf("/*");
        while (pos != -1) {
            cssclean.append(css.substring(0, pos));
            css = css.substring(pos + 2);
            int pos2 = css.indexOf("*/");
            if (pos2 != -1) {
                css = css.substring(pos2 + 2);
            }
            pos = css.indexOf("/*");
        }
        cssclean.append(css);
        return cssclean.toString().trim();
    }
}
