package integration;

import org.junit.Test;

public class ReuseIT extends AbstractCompileIT {

    /**	This test tests if one instance of LessCompiler can properly handle multiple invocations
     * @throws Exception
     */
    @Test
    public void testImport() throws Exception {
    	String[] filenames = new String[]{
    			"comments",
    			"css-3",
    			"css-escapes"
    	};
    	for(String filename : filenames){
    		testCompile(filename);
    	}
    }
    
    private void testCompile(String filename) throws Exception {
        testCompile(toFile("compatibility/less/" + filename + ".less"), toFile("compatibility/css/" + filename + ".css"));
    }
}
