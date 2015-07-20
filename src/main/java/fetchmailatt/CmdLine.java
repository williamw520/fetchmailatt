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

import java.text.*;
import java.util.*;


public class CmdLine
{
    public List<Arg>    arguments = new ArrayList<>();


    public <T> Arg arg(T defaultValue) {
        this.arguments.add(new Arg(defaultValue));
        return this.arguments.get(arguments.size()-1);
    }

    public String[] process(String[] args) {
        int         i = 0;

        for (i = 0; i < args.length; i++) {
            String  token = args[i];

            if (token.equals("--")) {
                i++;
                break;                  // End of arguments is marked with --; rest of args are returned.
            }

            Arg     found = null;
            if (token.startsWith("--")) {
                found = findArg(token.substring(2));
            } else if (token.startsWith("-")) {
                found = findArg(token.substring(1));
            }

            if (found == null)
                throw new IllegalArgumentException(token + " is not recognized.");

            String  val = null;         // argument value starts with not set.
            if (i < args.length - 1) {
                if (!args[i+1].startsWith("-"))
                    val = args[++i];
            }
            found.setValue(token, val);
        }

        checkRequired();
        
        return i < args.length ? Arrays.copyOfRange(args, i, args.length) : new String[0];
    }

    private void checkRequired() {
        for (Arg a : arguments) {
            if (a.required && !a.has)
                throw new IllegalArgumentException((a.flag != null ? "-" + a.flag : "--" + a.name) + " is required.");
        }
    }

    private Arg findArg(String name) {
        for (Arg a : arguments) {
            if (Objects.equals(a.flag, name) || Objects.equals(a.name, name))
                return a;
        }
        return null;
    }


    public static class Arg<T> {
        public String   flag;               // -f
        public String   name;               // --name
        public boolean  required = false;   // argument required?
        public T        value;              // argument value passed in from command line.
        public boolean  has = false;        // command line has the argument.

        private Arg(T defaultValue) {       // this sets up the type of the argument value.
            this.value = defaultValue;
        }

        public Arg flag(String flag) {
            this.flag = flag;
            return this;
        }

        public Arg name(String name) {
            this.name = name;
            return this;
        }

        public Arg required(boolean required) {
            this.required = required;
            return this;
        }

        private void setValue(String token, String val) {
            if (this.value instanceof Boolean) {
                if (val == null) {
                    this.value = (T)Boolean.TRUE;   // No argument value is given; assume setting the boolean flag to true.
                } else {
                    this.value = (T)new Boolean(val.toLowerCase().equals("true") || val.toLowerCase().equals("yes"));
                }
            } else {
                if (val == null) {
                    throw new IllegalArgumentException("Value is missing for argument " + token);
                } else if (this.value instanceof Integer) {
                    this.value = (T)Integer.valueOf(val);
                } else if (this.value instanceof Long) {
                    this.value = (T)Long.valueOf(val);
                } else if (this.value instanceof Float) {
                    this.value = (T)Float.valueOf(val);
                } else if (this.value instanceof Double) {
                    this.value = (T)Double.valueOf(val);
                } else if (this.value instanceof Character) {
                    this.value = (T)new Character(val.charAt(0));
                } else if (this.value instanceof String) {
                    this.value = (T)val;
                }
            }
            this.has = true;
        }

    }


    
    // Quick test
    public static void main(String[] args) {
        CmdLine                 cl = new CmdLine();
        CmdLine.Arg<Boolean>    arg1 = cl.arg(false).flag("b").name("bool").required(true); // required Boolean argument with -b flag or --bool name
        CmdLine.Arg<Integer>    arg2 = cl.arg(25).flag("i").name("age");                    // optional Integer argument with -i flag or --age name
        CmdLine.Arg<Double>     arg3 = cl.arg(150d).flag("f").name("weight");               // optional Double argument with -f flag or --weight name
        CmdLine.Arg<Character>  arg4 = cl.arg('a').flag("c");                               // optional Character argument with -c flag
        CmdLine.Arg<String>     arg5 = cl.arg("").flag("s");                                // optional String argument with -s flag
        String[]                restOfArgs = new String[0];                                 // -- in command line stops processing and returns the rest of args.

        try {
            restOfArgs = cl.process(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Argument -" + arg1.flag + " is set: " + arg1.has + "  Value: " + arg1.value);
        System.out.println("Argument -" + arg2.flag + " is set: " + arg2.has + "  Value: " + arg2.value);
        System.out.println("Argument -" + arg3.flag + " is set: " + arg3.has + "  Value: " + arg3.value);
        System.out.println("Argument -" + arg4.flag + " is set: " + arg4.has + "  Value: " + arg4.value);
        System.out.println("Argument -" + arg5.flag + " is set: " + arg5.has + "  Value: " + arg5.value);

        System.out.print("Rest of arguments after -- ");
        for (String r : restOfArgs)
            System.out.print(" " + r);
        System.out.println("");

    }


}

