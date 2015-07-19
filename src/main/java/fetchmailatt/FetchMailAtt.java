
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
    private static final String APP = "FetchMailAtt";
    private static final String VERSION = "1.0";

    
    private static final Logger log = Logger.getLogger(FetchMailAtt.class.getName());

    static {
        Dbg.setRootLogLevel("WARNING");
    }

    private static void usage() {
        System.out.println("Usage: java FetchMailAtt -c config [-q | --quiet] [-t | --test] [-r | --reset] [-d | --dump] [-v | --version] [-h | --help] [-l LOG_LEVEL]");
    }

    public static void main(String[] args) {
        long                    startMS = System.currentTimeMillis();
        CmdLine                 cl = new CmdLine();
        CmdLine.Arg<String>     argConf = cl.arg("").flag("c").name("conf");
        CmdLine.Arg<Boolean>    argQuiet = cl.arg(false).flag("q").name("quiet");
        CmdLine.Arg<Boolean>    argTest = cl.arg(false).flag("t").name("test");
        CmdLine.Arg<Boolean>    argReset = cl.arg(false).flag("r").name("reset");
        CmdLine.Arg<Boolean>    argDump = cl.arg(false).flag("d").name("dump");
        CmdLine.Arg<Boolean>    argVersion = cl.arg(false).flag("v").name("version");
        CmdLine.Arg<Boolean>    argHelp = cl.arg(false).flag("h").name("help");
        CmdLine.Arg<String>     argLogLevel = cl.arg("WARNING").flag("l");

        try {
            cl.process(args);
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            usage();
            return;
        }

        boolean                 quiet = argQuiet.value;

        if (argLogLevel.has) {
            Dbg.setRootLogLevel(argLogLevel.value);
        }
        if (argHelp.value) {
            usage();
            return;
        }
        if (argVersion.value) {
            System.out.println(APP + " " + VERSION);
            return;
        }

        try {
            Properties          conf;
            String              cfgName = argConf.value;

            if (argConf.has) {
                if ((conf = Util.loadProperties(Paths.get(cfgName))) == null) {
                    conf = Util.loadResourceProperties(cfgName);
                    if (!quiet && conf != null) System.out.println("Using config resource " + cfgName);
                } else {
                    if (!quiet) System.out.println("Using config file " + cfgName);
                }
                if (conf == null) {
                    System.out.println("Config file " + cfgName + " not exists");
                    return;
                }
            } else {
                cfgName = "default.conf";
                if ((conf = Util.loadResourceProperties(cfgName)) == null) {
                    System.out.println("conf/default.conf not exists");
                    return;
                } else {
                    if (!quiet) System.out.println("Using config resource conf/default.conf");
                }
            }

            String              stateFilename = Util.removeLastPart(Util.lastPart(Util.lastPart(cfgName, '/'), '\\'), '.');
            if (argReset.value) {
                Files.delete(Util.getStateFile(stateFilename));
            }

            if (argDump.value) {
                MailService.dumpMessages(Util.toMap(conf));
            } else {
                MailService.fetchAttachments(Util.toMap(conf), stateFilename, quiet, argTest.value);
            }

        } catch (Exception e) {
            Dbg.error(log, e);
        }

        double  durationSec = (double)((System.currentTimeMillis() - startMS) / 100 * 100) / 1000;
        if (!quiet) System.out.println(FetchMailAtt.class.getName() + " ended.  duration: " + durationSec  + "s");
    }

}
