
package fetchmailatt;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.text.*;



public class Util {


    /** Turns a list of arguments (or array) into a map.  Arguments are listed as key1,value1, key2,value2, ... */
    //@SuppressWarnings("unchecked")
    public static <K,V> Map<K,V> asMap(K key1, V value1, Object... keyValPair) {
        if (keyValPair.length % 2 != 0)
            throw new IllegalArgumentException("Keys and values must be pairs.");
        Map<K,V> map = new HashMap<K,V>();
        map.put(key1, value1);
        for (int i = 0; i < keyValPair.length; i += 2) {
            map.put((K)keyValPair[i], (V)keyValPair[i+1]);
        }
        return map;
    }

    public static Properties asProperties(String... keyValPair) {
        if (keyValPair.length % 2 != 0)
            throw new IllegalArgumentException("Keys and values must be pairs.");
        Properties  props = new Properties();
        for (int i = 0; i < keyValPair.length; i += 2) {
            props.put(keyValPair[i], keyValPair[i+1]);
        }
        return props;
    }

    public static Properties toProperties(Map map) {
        Properties  props = new Properties();
        props.putAll(map);
        return props;
    }

    public static Map<String, String> toMap(Properties props) {
        return new HashMap<String, String>((Map)props);
    }

    public static Properties loadProperties(String filename) throws IOException {
        if (!Files.exists(Paths.get(filename)))
            return null;
        try (Reader r = new FileReader(filename)) {
            Properties  props = new Properties();
            props.load(r);
            return props;
        }
    }

    public static Properties loadResourceProperties(String pkgClassPath) throws IOException {
        try (InputStream is = Util.class.getClassLoader().getResourceAsStream(pkgClassPath)) {
            if (is == null)
                return null;
            Properties  props = new Properties();
            props.load(is);
            return props;
        }
    }

	public static boolean empty(String str) {
		return (str == null || str.length() == 0);
	}

	public static boolean empty(Object obj) {
		return (obj == null || obj.toString().length() == 0);
	}

    // Str is considered empty if it's null, len==0, or one of the emptyTokens
	public static boolean empty(String str, String... emptyTokens) {
        if (empty(str))
            return true;
        for (String token : emptyTokens) {
            if (str.equals(token))
                return true;
        }
        return false;
	}

    public static boolean equals(String str1, String str2) {
        return str1 == null ? str2 == null : (str2 == null ? false : str1.equals(str2));
    }

    public static boolean iequals(String str1, String str2) {
        return str1 == null ? str2 == null : (str2 == null ? false : str1.compareToIgnoreCase(str2) == 0);
	}

    public static <K,V> V ensure(Map<K,V> map, K key) {
        V   value = map.get(key);
        if (value == null)
            throw new NoSuchElementException("Entry for '" + key + "' is not defined.");
        return value;
    }

    public static <K,V> V ensure(Map<K,V> map, K key, V defaultVal) {
        V   value = map.get(key);
        return value != null ? value : defaultVal;
    }

    public static <T> T defval(T obj, Class<T> clazz) throws Exception {
        return obj != null ? obj : clazz.newInstance();
    }

    public static <T> T[] defval(T[] obj, Class<T> clazz) throws Exception {
        return obj != null ? obj : (T[])java.lang.reflect.Array.newInstance(clazz, 0);
    }

    public static String defval(String obj, String defaultVal) {
        return !empty(obj) ? obj : defaultVal;
    }

    public static <T> T defval(T obj, T defaultVal) {
        return obj != null ? obj : defaultVal;
    }


    public static int toInt(String strValue, int defVal) {
        if (strValue != null)
            try { return Integer.parseInt(strValue); } catch(Exception ignored) {}
        return defVal;
    }

    public static long toLong(String strValue, long defVal) {
        if (strValue != null)
            try { return Long.parseLong(strValue); } catch(Exception ignored) {}
        return defVal;
    }

    public static double toDouble(String strValue, double defVal) {
        if (strValue != null)
            try { return Double.parseDouble(strValue); } catch(Exception ignored) {}
        return defVal;
    }


    private static TlsMap.Factory<SimpleDateFormat>  sTimeStrFactory = new TlsMap.Factory<SimpleDateFormat>() {
        public SimpleDateFormat create(Object key) {
            String              formatStr = (String)key;            // Use the format str as the TLS cache key.
            SimpleDateFormat    sdf = new SimpleDateFormat(formatStr);
            sdf.setLenient(false);      // want strict date parsing.
            return sdf;
        }
    };

    public static String formatTime(String format, Date time) {
        return TlsMap.get(format, sTimeStrFactory).format(time);
    }

    public static SimpleDateFormat getTimeFull() {
        return TlsMap.get("yyyy-MM-dd.HH.mm.ss", sTimeStrFactory);
    }

    public static String formatTimeFull(Date time) {
        return getTimeFull().format(time);
    }

    public static SimpleDateFormat getTimeMMddHHmm() {
        return TlsMap.get("MM/dd/yyyy hh:mma", sTimeStrFactory);
    }

    public static String formatTimeMMddHHmm(Date time) {
        return getTimeMMddHHmm().format(time);
    }

    public static SimpleDateFormat getDateYYYYMMdd() {
        return TlsMap.get("yyyy-MM-dd", sTimeStrFactory);
    }

    public static String formatDateYYYYMMdd(Date time) {
        return getDateYYYYMMdd().format(time);
    }

    public static SimpleDateFormat getDateYYYYMM() {
        return TlsMap.get("yyyy-MM", sTimeStrFactory);
    }

    public static String formatDateYYYYMM(Date time) {
        return getDateYYYYMM().format(time);
    }

    public static SimpleDateFormat getDateYYYY() {
        return TlsMap.get("yyyy", sTimeStrFactory);
    }

    public static String formatDateYYYY(Date time) {
        return getDateYYYY().format(time);
    }

    public static SimpleDateFormat getDateMMddyyyy() {
        return TlsMap.get("MM/dd/yyyy", sTimeStrFactory);
    }

    public static String formatDateMMddyyyy(Date time) {
        return getDateMMddyyyy().format(time);
    }

    public static SimpleDateFormat getDateMMddyyyy2() {
        return TlsMap.get("MM-dd-yyyy", sTimeStrFactory);
    }

    public static String formatDateMMddyyyy2(Date time) {
        return getDateMMddyyyy().format(time);
    }

    public static SimpleDateFormat getDateMMddyy() {
        return TlsMap.get("MM/dd/yy", sTimeStrFactory);
    }

    public static String formatDateMMddyy(Date time) {
        return getDateMMddyy().format(time);
    }

    public static Date parseDate(String dateStr, SimpleDateFormat... formats) {
        for (SimpleDateFormat sdf : formats) {
            try {
                return sdf.parse(dateStr);
            } catch(ParseException ignored) {}
        }
        return null;
    }

    /** Return the earlier date of two.  null being considered later than a Date. */
    public static Date earlier(Date a, Date b) {
        return a == null ? b : (b == null ? a : (a.before(b) ? a : b));
    }

    /** Return the later date of two.  null being considered later than a Date. */
    public static Date later(Date a, Date b) {
        return a == null ? b : (b == null ? a : (a.after(b) ? a : b));
    }
    
    public static Date addDays(Date date, int days) {
        Calendar    cal = TlsMap.get("Calendar", (Calendar)null);
        if (cal == null) {
            cal = Calendar.getInstance();
            TlsMap.put("Calendar", cal);
        }
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

}
