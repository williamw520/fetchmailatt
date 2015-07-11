
/**
 * FetchMailAtt
 */


package fetchmailatt;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;



/**
 *
 */
public class FetchMailAtt {
    private static final String PRODUCT = "FetchMailAtt";
    private static final String VERSION = "1.0";

    
    private static final Logger log = Logger.getLogger(FetchMailAtt.class.getName());

    static {
        Dbg.setRootLogLevel(null);
    }

    private static void usage() {
        System.out.println("Usage: java FetchMailAtt -c config [-d | --verbose] [-t | --test] [-v | --version] [-h | --help]");
    }

    public static void main(String[] args) {
        long                    startMS = System.currentTimeMillis();
        CmdLine                 cl = new CmdLine();
        CmdLine.Arg<String>     argConf = cl.arg("").flag("c").name("conf");
        CmdLine.Arg<Boolean>    argTest = cl.arg(false).flag("t").name("test");
        CmdLine.Arg<Boolean>    argQuiet = cl.arg(false).flag("q").name("quiet");
        CmdLine.Arg<Boolean>    argVersion = cl.arg(false).flag("v").name("version");
        CmdLine.Arg<Boolean>    argHelp = cl.arg(false).flag("h").name("help");

        try {
            cl.process(args);
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            usage();
            return;
        }

        boolean                 quiet = argQuiet.value;

        if (argHelp.value) {
            usage();
            return;
        }
        if (argVersion.value) {
            System.out.println(PRODUCT + " " + VERSION);
            return;
        }

        try {
            Properties          conf;

            if (argConf.has) {
                if ((conf = Util.loadProperties(argConf.value)) == null) {
                    conf = Util.loadResourceProperties(argConf.value);
                    if (conf != null && !quiet) System.out.println("Using config resource " + argConf.value);
                } else {
                    if (!quiet) System.out.println("Using config file " + argConf.value);
                }
                if (conf == null) {
                    System.out.println("Config file " + argConf.value + " not exists");
                    return;
                }
            } else {
                if ((conf = Util.loadResourceProperties("default.conf")) == null) {
                    System.out.println("conf/default.conf not exists");
                    return;
                } else {
                    if (!quiet) System.out.println("Using config resource conf/default.conf");
                }
            }

            if (!argTest.value) {
                MailService.fetchAttachments(Util.toMap(conf), quiet);
            } else {
                MailService.dumpMessages(Util.toMap(conf));
            }

        } catch (Exception e) {
            Dbg.error(log, e);
        }

        double  durationSec = (double)((System.currentTimeMillis() - startMS) / 100 * 100) / 1000;
        if (!quiet) System.out.println(FetchMailAtt.class.getName() + " ended.  duration: " + durationSec  + "s");
    }

}
