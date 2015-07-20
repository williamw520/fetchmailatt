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
import java.util.stream.*;
import java.util.function.*;
import java.text.*;



public class Util {

    public static Path getAppDir() throws IOException {
        Path    path = Paths.get(System.getProperty("user.home"));
        path = path.resolve(".fetchmailatt");
        if (!Files.exists(path))
            Files.createDirectories(path);
        return path;
    }

    public static Path getStateFile(String stateFilename) throws IOException {
        return getAppDir().resolve(stateFilename + ".state");
    }

    /** Return list literals as list and return array as list; also handle null array. */
    public static <T> List<T> asList(T... objs) {
        return objs != null ? Arrays.asList(objs) : new ArrayList<T>();
    }

    public static <T> Stream<T> asStream(T... objs) {
        return asList(objs).stream();
    }

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
        return props == null ? new HashMap<String,String>() : new HashMap<String, String>((Map)props);
    }

    public static Properties loadProperties(Path filePath) throws IOException {
        if (!Files.exists(filePath))
            return null;
        try (Reader r = Files.newBufferedReader(filePath)) {
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

	public static void saveProperties(Path filePath, Properties prop) throws IOException {
        if (Files.exists(filePath))
            Files.delete(filePath);
		try (Writer w = Files.newBufferedWriter(filePath)) {
            prop.store(w, "");
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

    /** Return the later date of two.  null being considered earlier than a Date. */
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


    private final static int[] INVALID_PATH_CHARS = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                                                     21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};
    static {
        Arrays.sort(INVALID_PATH_CHARS);
    }

    public static String cleanFilename(String filename) {
        StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < filename.length(); i++) {
            int c = (int)filename.charAt(i);
            if (Arrays.binarySearch(INVALID_PATH_CHARS, c) < 0) {
                cleanName.append((char)c);
            }
        }
        return cleanName.toString();
    }

    public static String maxStr(String str, int max) {
        return str.substring(0, (str.length() < max) ? str.length() : max);
    }

    public static long parseByteSize(String size, long defaultVal) {
        try {
            size = size.replaceAll(",", "").replaceAll(" ", "").toUpperCase();
            String  part[] = size.split("(?<=\\d)(?=\\p{L})");
            if (part.length < 2)
                return (long)(Double.parseDouble(part[0]));
            if (part[1].equals("PB") || part[1].equals("P"))
                return (long)(Double.parseDouble(part[0]) * (1024L*1024*1024*1024*1024));
            if (part[1].equals("TB") || part[1].equals("T"))
                return (long)(Double.parseDouble(part[0]) * (1024L*1024*1024*1024));
            if (part[1].equals("GB") || part[1].equals("G"))
                return (long)(Double.parseDouble(part[0]) * (1024L*1024*1024));
            if (part[1].equals("MB") || part[1].equals("M"))
                return (long)(Double.parseDouble(part[0]) * (1024L*1024));
            if (part[1].equals("K"))
                return (long)(Double.parseDouble(part[0]) * (1024L));
            if (part[1].equals("B"))
                return (long)(Double.parseDouble(part[0]));
        } catch(Exception e) {}
        return defaultVal;
    }

    public static String formatByteSize(long bytes, int decimals) {
        decimals = Math.min(Math.max(decimals, 0), 4);
        if (bytes < 1024) return bytes + "B";
        int     exp = (int)(Math.log(bytes) / Math.log(1024));
        String  pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%." + decimals + "f%s", bytes / Math.pow(1024, exp), pre);
    }

    public static Stream<String> splitParts(String str, String delimiter) {
        return Arrays.stream(str.split(delimiter)).map(String::trim).map(String::toLowerCase).distinct();
    }

    public static <T> Stream<T> flatOptionals(Stream<Optional<T>> list) {
        return list.filter(Optional::isPresent).map(opt -> opt.get());
    }
    
    public static <T> List<T> flatOptionals(List<Optional<T>> list) {
        return list.stream().filter(Optional::isPresent).map(opt -> opt.get()).collect(Collectors.toList());
    }

    public static String lastPart(String str, char delimiter) {
        return str.substring(str.lastIndexOf(delimiter) + 1);
    }

    public static String removeLastPart(String str, char delimiter) {
        int index = str.lastIndexOf(delimiter);
        return index == -1 ? str : str.substring(0, index);
    }

    public static void sleep(long sec) {
        try {
            Thread.sleep(sec * 1000);
        } catch(InterruptedException e) {
        }
    }
    

}
