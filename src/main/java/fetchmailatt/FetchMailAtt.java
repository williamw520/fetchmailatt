
/**
 * FetchMailAtt
 */


package fetchmailatt;

import java.io.*;
import java.util.*;
import java.util.logging.*;



/**
 *
 */
public class FetchMailAtt {
    private static final Logger log = Logger.getLogger(FetchMailAtt.class.getName());

    static {
        Dbg.setRootLogLevel(null);
    }

    public static void main(String[] args) {
        log.info(FetchMailAtt.class.getName() + " starts");
        long                    startMS = System.currentTimeMillis();
        CmdLine                 cl = new CmdLine();
        CmdLine.Arg<String>     argConf = cl.arg("").flag("c").name("conf").required(true);
        CmdLine.Arg<Boolean>    argDump = cl.arg(false).flag("d").name("dump");

        try {
            cl.process(args);
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            System.out.println("Usage: java FetchMailAtt -c config [-d | --dump]");
            return;
        }

        try {
            Map<String, String> conf = Util.toMap(Util.loadProperties(argConf.value));

            if (argDump.value) {
                MailService.dumpMessages(conf);
            } else {
                MailService.fetchAttachments(conf);
            }

        } catch (Exception e) {
            Dbg.error(log, e);
        }

        double  durationSec = (double)((System.currentTimeMillis() - startMS) / 100 * 100) / 1000;
        log.info(FetchMailAtt.class.getName() + " ended.  duration: " + durationSec  + "s");
    }

}
