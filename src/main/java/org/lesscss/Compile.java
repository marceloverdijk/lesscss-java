package org.lesscss;

import org.lesscss.logging.LessLogger;
import org.lesscss.logging.LessLoggerFactory;

import java.io.File;
import java.util.Arrays;

public class Compile {

    private static final LessLogger logger = LessLoggerFactory.getLogger( Compile.class );

	public static void main(String[] args) throws Exception {
		if( args.length < 1 ) {
		    logger.info("usage: org.lesscss.Compile <less_filename>");
            System.exit(-1);
		}

        File output = new File( args[0] + ".css" );
        logger.info("Compiler output = %s", output.getCanonicalPath() );

        long start = System.currentTimeMillis();
		LessCompiler lessCompiler = new LessCompiler(Arrays.asList("-ru"));
		
		lessCompiler.compile( new File( args[0] ), output );
		
        long duration = System.currentTimeMillis() - start;
        logger.info("Done. %,d ms", duration);
	}
}