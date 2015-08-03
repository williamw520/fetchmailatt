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
        return ((Map<K,V>)sTlsObj.get().get(namespace)).get(key);
    }

    /** Get a cached item.  Create and cache it if not exists. */
    public V val(K key, Object... createParams) {
        if (factory == null)
            throw new RuntimeException("No factory defined.");
    
        Map<K,V> map = (Map<K,V>)sTlsObj.get().get(namespace);
        V   value = map.get(key);
        if (value == null) {
            value = factory.create(key, createParams);
            map.put(key, value);
        }
        return value;
    }

    /** Cache an item. */
    public void put(K key, V value) {
        ((Map<K,V>)sTlsObj.get().get(namespace)).put(key, value);
    }

    /** Remove an item from cache. */
    public V remove(K key) {
        return ((Map<K,V>)sTlsObj.get().get(namespace)).remove(key);
    }

    /** Remove all items with the same namespace in the cache. */
    public void clear() {
        ((Map<K,V>)sTlsObj.get().get(namespace)).clear();
    }

}
