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
import java.text.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.logging.Formatter;


public class Dbg {
	private static long     sStartTime = System.currentTimeMillis();
	private static long     sLastTime = System.currentTimeMillis();


    public static void error(Logger log, String msg, Throwable thrown) {
        log.log(Level.SEVERE, msg, thrown);
    }

    public static void error(Logger log, Throwable thrown) {
        log.log(Level.SEVERE, "", thrown);
    }


    public static String getStackTrace(Throwable e) {
		try ( StringWriter strWriter = new StringWriter();
              PrintWriter  printWriter = new PrintWriter(strWriter);
            ) {
			e.printStackTrace(printWriter);
			printWriter.flush();
			return strWriter.toString();
		} catch (Exception ignored) {
        }
		return "";
	}

    public static String formatExceptions(Throwable e, Function<Throwable, String> getter) {
		try ( StringWriter strWriter = new StringWriter();
              PrintWriter  printWriter = new PrintWriter(strWriter);
            ) {
            while (e != null) {
                printWriter.print("\n    ");
                printWriter.println(getter.apply(e));
                e = e.getCause();
            }
			printWriter.flush();
			return strWriter.toString();
		} catch (Exception ignored) {
        }
		return "";
	}

    public static String formatExceptions(Throwable e) {
        return formatExceptions(e, (t) -> t.toString());
	}

    public static String formatExceptionMsg(Throwable e) {
        return formatExceptions(e, (t) -> t.getMessage());
	}


    public static void timeStart() {
        sLastTime = sStartTime = System.currentTimeMillis();
    }

	public static String timeDuration(String msg)
	{
		long    now = System.currentTimeMillis();
		long    fromLast  = now - sLastTime;
		long    fromStart = now - sStartTime;
		sLastTime = now;
		return "Elapsed from start " + fromStart + ", from last " + fromLast + ", " + msg;
	}

    /** Set the logging level of the package logger.
     * If logLevel is not passed in, get it from system property.
     * logLevel: "SEVERE", "WARNING", "INFO", "FINE", "FINER", etc
     */
    public static void setPkgLogLevel(String pkg, String logLevel, Formatter formatter) {
        if (logLevel == null)
            logLevel = System.getProperty("logLevel");

        Level   level = logLevel == null || logLevel.isEmpty() ? Level.INFO : Level.parse(logLevel);
        Logger  pkgLogger = Logger.getLogger(pkg);

        pkgLogger.setLevel(level);
        for (Handler handler : pkgLogger.getHandlers()) {
            handler.setLevel(level);
            if (formatter != null)
                handler.setFormatter(formatter);
        }
    }


    public static class CompactLogFormatter extends Formatter {
        private static final String LF = System.getProperty("line.separator");

        private static final TlsCache<String, SimpleDateFormat> sFormat = new TlsCache("LogDateFormat", new TlsCache.Factory<String, SimpleDateFormat>() {
                public SimpleDateFormat create(String key, Object... createParams) {
                    return new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");
                }
            });

        private static final TlsCache<String, StringBuilder>    sSB = new TlsCache("LogSB", new TlsCache.Factory<String, StringBuilder>() {
                public StringBuilder create(String key, Object... createParams) {
                    return new StringBuilder();
                }
            });

        private static String getName(LogRecord r) {
            String  name = r.getLoggerName();
            int     len = name.length() < 30 ? name.length() : 30;
            return name.substring(name.length() - len, name.length());
        }

        public String format(LogRecord r) {
            StringBuilder sb = sSB.val();
            sb.setLength(0);
            sb.append(sFormat.val().format(new Date(r.getMillis()))).append(" ").append(r.getLevel().getName()).append(": ")
                .append(getName(r)).append(" ").append(formatMessage(r)).append(LF);
            if (r.getThrown() != null) {
                sb.append(Dbg.getStackTrace(r.getThrown()));
            }
            return sb.toString();
        }
    }

}
