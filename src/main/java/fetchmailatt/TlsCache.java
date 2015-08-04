/******************************************************************************
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0.  If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Software distributed under the License is distributed on an "AS IS" basis, 
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for 
 * the specific language governing rights and limitations under the License.
 *
 * The Original Code is: FetchMailAtt
 * The Initial Developer of the Original Code is: William Wong (williamw520@gmail.com)
 * Portions created by William Wong are Copyright (C) 2015 William Wong, All Rights Reserved.
 *
 ******************************************************************************/

package fetchmailatt;

import java.util.*;


/**
 * Shared thread local storage cache.  All instances share the same TLS cache.
 * Using the same key, cached item can be accessed by different instances with the same namespace.
 */
public class TlsCache<K,V> {

    private static final ThreadLocal<Map>   sTlsObj = new ThreadLocal<Map>() {
        protected Map initialValue() {
            return new HashMap();
        }
    };

    public static interface Factory<K,V> {
        public V create(K key, Object... createParams);
    }

    private static final String     NULLKEY = "_nullkey";

    private String                  namespace;
    private TlsCache.Factory<K,V>   factory;

    /** Create a cache access object on the namespace, with no factory. */
    public TlsCache(String namespace) {
        this.namespace = namespace;
        sTlsObj.get().put(namespace, new HashMap<K,V>());
    }

    /** Create a cache access object on the namespace, with factory to create missing item. */
    public TlsCache(String namespace, TlsCache.Factory<K,V> factory) {
        this.namespace = namespace;
        this.factory = factory;
        sTlsObj.get().put(namespace, new HashMap<K,V>());
    }

    /** Get a cached item.  Return null if not exists. */
    public V get(K key) {
        if (key != null) {
            return ((Map<K, V>)sTlsObj.get().get(namespace)).get(key);
        } else {
            return ((Map<String, V>)sTlsObj.get().get(NULLKEY)).get(namespace); // for null key, use namespace as key on the NULLKEY map.
        }
    }

    public V get() {
        return get(null);   // Treat no key as null key.
    }

    /** Get a cached item.  Create and cache it if not exists. */
    public V val(K key, Object... createParams) {
        if (factory == null)
            throw new RuntimeException("No factory defined.");

        V   value;
        if (key != null) {
            Map<K, V>       map = (Map<K, V>)sTlsObj.get().get(namespace);
            if ((value = map.get(key)) == null) {
                value = factory.create(key, createParams);
                map.put(key, value);
            }
        } else {
            Map<String, V>  nullmap = (Map<String, V>)sTlsObj.get().get(NULLKEY);
            if ((value = nullmap.get(namespace)) == null) {         // for null key, use namespace as key on the NULLKEY map.
                value = factory.create(key, createParams);
                nullmap.put(namespace, value);
            }
        }
        return value;
    }

    public V val() {
        return val(null);   // Treat no key as null key.
    }

    /** Cache an item. */
    public void put(K key, V value) {
        if (key != null) {
            ((Map<K, V>)sTlsObj.get().get(namespace)).put(key, value);
        } else {
            ((Map<String, V>)sTlsObj.get().get(NULLKEY)).put(namespace, value);
        }
    }

    /** Remove an item from cache. */
    public V remove(K key) {
        if (key != null) {
            return ((Map<K, V>)sTlsObj.get().get(namespace)).remove(key);
        } else {
            return ((Map<String, V>)sTlsObj.get().get(NULLKEY)).remove(namespace);
        }
    }

    /** Remove all items with the same namespace in the cache. */
    public void clear() {
        ((Map<K, V>)sTlsObj.get().get(namespace)).clear();
        ((Map<String, V>)sTlsObj.get().get(NULLKEY)).remove(namespace);
    }

}

