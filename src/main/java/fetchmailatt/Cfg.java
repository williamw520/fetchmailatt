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

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.text.*;


/** Optional map */
public class Cfg extends HashMap<String, String> {

    public Cfg(Map<String, String> map) {
        super.putAll(map);
    }

    public String ensure(String key) {
        String  value = super.get(key);
        if (value == null)
            throw new NoSuchElementException("Entry for '" + key + "' is not defined.");
        return value;
    }

    public Optional<String> val(String key) {
        return Optional.ofNullable(super.get(key));
    }

    public Optional<Boolean> asBoolean(String key) {
        return val(key).map(v -> v.toString().toLowerCase().equals("true") || v.toString().toLowerCase().equals("yes"));
    }

    public Optional<Short> asShort(String key) {
        return val(key).map(v -> Short.parseShort(v.toString()));
    }

    public Optional<Integer> asInt(String key) {
        return val(key).map(v -> Integer.parseInt(v.toString()));
    }

    public Optional<Long> asLong(String key) {
        return val(key).map(v -> Long.parseLong(v.toString()));
    }

    public Optional<Float> asFloat(String key) {
        return val(key).map(v -> Float.parseFloat(v.toString()));
    }

    public Optional<Double> asDouble(String key) {
        return val(key).map(v -> Double.parseDouble(v.toString()));
    }

    public Optional<String> asLowerCase(String key) {
        return val(key).map(v -> v.toString().trim().toLowerCase());
    }

}
