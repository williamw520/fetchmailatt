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
import java.util.logging.*;



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
            boolean             quiet = argQuiet.value;
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

    }

}
