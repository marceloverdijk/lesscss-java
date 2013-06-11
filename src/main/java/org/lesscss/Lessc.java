package org.lesscss;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: martinstolz
 * Date: 15.01.13
 */
public class Lessc {

    /**
     * A main method for invoking the <code>LessCompiler</code> from somewhere else.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            usage();
        }

        String in = args[0];
        // default filename is same as input name.
        String out = args[0].replace(".less", ".css");
        if (args.length > 1) {
            out = args[1];
        }

        List<File> inc = new ArrayList<File>();
        if (args.length > 2) {
            for(int i = 2; i < args.length; i++) {
                inc.add(new File(args[i]));
            }
        }

        LessCompiler compiler = new LessCompiler();
        if (inc.size() != 0) {
            compiler.compile(new File(in), new File(out), true, inc);
        } else {
            compiler.compile(new File(in), new File(out), true);
        }

    }

    /**
     * Print some usage information.
     */
    private static void usage() {
        System.out.println("Use the JAVA Lessc like this:");
        System.out.println("  java -jar lesscss-java.jar main.less");
        System.out.println("Or specify the target file:");
        System.out.println("  java -jar lesscss-java.jar main.less somewhere/out.css");
        System.out.println("Or specify the target file and the path to search for includes in:");
        System.out.println("  java -jar lesscss-java.jar main.less somewhere/out.css ./overrides ./base ");
    }


}
