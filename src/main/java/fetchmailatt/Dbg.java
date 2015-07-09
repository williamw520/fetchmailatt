
package fetchmailatt;

import java.io.*;
import java.util.logging.*;


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
		StringWriter	strWriter = null;
		PrintWriter		printWriter = null;
		String			stackTrace = "";

		try {
			strWriter = new StringWriter();
			printWriter = new PrintWriter(strWriter);

			e.printStackTrace(printWriter);
			printWriter.flush();
			stackTrace = strWriter.toString();
		} catch (Exception e1) {
        } finally {
			try {
				printWriter.close();
				strWriter.close();
			} catch(Exception e2) {
			}
		}
		return stackTrace;
	}

    public static String formatExceptions(Throwable e) {
		StringWriter	strWriter = null;
		PrintWriter		printWriter = null;
		String			stackTrace = "";

		try {
			strWriter = new StringWriter();
			printWriter = new PrintWriter(strWriter);

            while (e != null) {
                printWriter.print("\n    ");
                printWriter.println(e.toString());
                e = e.getCause();
            }
			printWriter.flush();
			stackTrace = strWriter.toString();
		} catch (Exception e1) {
        } finally {
			try {
				printWriter.close();
				strWriter.close();
			} catch(Exception e2) {
			}
		}
		return stackTrace;
	}

    public static String formatExceptionText(Throwable e) {
		StringWriter	strWriter = null;
		PrintWriter		printWriter = null;
		String			stackTrace = "";

		try {
			strWriter = new StringWriter();
			printWriter = new PrintWriter(strWriter);

            while (e != null) {
                printWriter.print("\n    ");
                printWriter.println(e.getMessage());
                e = e.getCause();
            }
			printWriter.flush();
			stackTrace = strWriter.toString();
		} catch (Exception e1) {
        } finally {
			try {
				printWriter.close();
				strWriter.close();
			} catch(Exception e2) {
			}
		}
		return stackTrace;
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

    /** Set the logging level of the root package logger.
     * If logLevel is not passed in, get it from system property.
     * logLevel: "SEVERE", "WARNING", "INFO", "FINE", "FINER", etc
     */
    public static void setRootLogLevel(String logLevel) {
        if (logLevel == null)
            logLevel = System.getProperty("logLevel");

        Level   level = logLevel == null || logLevel.isEmpty() ? Level.INFO : Level.parse(logLevel);
        Logger  rootLogger = Logger.getLogger("");

        rootLogger.setLevel(level);
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(level);
        }
    }

}
