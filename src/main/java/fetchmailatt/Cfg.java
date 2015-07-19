
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
