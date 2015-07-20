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
 * Thread local storage map
 */
@SuppressWarnings("unchecked")
public class TlsMap
{
	private static final ThreadLocal<Map>   sTlsObj = new ThreadLocal<Map>() {
		protected Map initialValue() {
			return new HashMap();
		}
	};

	private TlsMap() {
	}

	public static Object get(Object key) {
		return sTlsObj.get().get(key);
	}

	public static <T> T get(Object key, T defaultVal) {
		T   value = (T)sTlsObj.get().get(key);
        return value != null ? value : defaultVal;
	}

	public static <T> T get(Object key, TlsMap.FactoryEx<T> factory, Object... createParams) {
        Map map = sTlsObj.get();
        T   value = (T)map.get(key);
        if (value == null) {
            value = factory.create(key, createParams);
            map.put(key, value);
        }
        return value;
	}

	public static <T> T get(Object key, TlsMap.Factory<T> factory) {
        Map map = sTlsObj.get();
        T   value = (T)map.get(key);
        if (value == null) {
            value = factory.create(key);
            map.put(key, value);
        }
        return value;
	}

	public static void put(Object key, Object value) {
		sTlsObj.get().put(key, value);
	}

	public static Object remove(Object key) {
		return sTlsObj.get().remove(key);
	}

	public static void clear() {
		sTlsObj.remove();
	}


    public static interface Factory<T> {
        public T create(Object key);
    }

    public static interface FactoryEx<T> {
        public T create(Object key, Object... createParams);
    }

}

