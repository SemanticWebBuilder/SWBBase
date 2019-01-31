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
 * dirección electrónica: http://www.semanticwebbuilder.org.mx
 */
package org.semanticwb.base.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cached HashMap implementation. Uses a cropped linked list internally to keep cache.
 * @author javier.solis.g
 */
public class HashMapCache<K extends Object, V extends Object> implements Map<K,V> {
    private int maxSize;
    ConcurrentHashMap<K,V> map;
    LinkedList<K> linked;

    /**
     * Constructor. Creates a new {@link HashMapCache} with <code>maxSize</code> elements.
     * @param maxSize maximum number of elements in the cached Hash Map.
     */
    public HashMapCache(int maxSize) {
        map = new ConcurrentHashMap();
        linked = new LinkedList();
        this.maxSize = maxSize;
    }

    /**
     * Gets the maximum number of elements in the cached Hash Map.
     * @return maximum number of elements.
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the maximum number of elements in the cached Hash Map.
     * @param maxSize maximum number of elements.
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object obj) {
        return map.containsKey(obj);
    }

    @Override
    public boolean containsValue(Object obj) {
        return map.containsValue(obj);
    }

    @Override
    public V get(Object key) {
        if(key == null) {
            return null;
        }
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        if(key == null) {
            return null;
        }

        V r = map.put(key, value);
        linked.add(key);

        if(map.size() > maxSize) {
            crop();
        }

        return r;
    }

    /**
     * Crops elements from cache.
     */
    private synchronized void crop() {
        while(!linked.isEmpty() && map.size() > maxSize) {
            Object obj = linked.poll();
            map.remove(obj);
        }          
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        this.map.putAll(map);
    }

    @Override
    public void clear() {
        map.clear();
        linked.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K,V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public V remove(Object key) {
        return map.remove(key);
    }
}
