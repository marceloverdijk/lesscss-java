package integration;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.jodah.concurrentunit.Waiter;
import org.junit.Before;
import org.junit.Test;

public class MultithreadedIT extends AbstractCompileIT {
	final Waiter waiter = new Waiter();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        lessCompiler.setCustomJs(Arrays.asList(
                toURL("compatibility/custom.math.js"), 
                toURL("compatibility/custom.color.js"), 
                toURL("compatibility/custom.process.title.js")));
    }
    
	@Test
	public void testMultithreaded() throws Throwable {
    	final String[] filenames = new String[]{
    			"colors",
    			"comments",
    			"css-3",
    			"css-escapes",
    			"css",
    			"functions",
    			"ie-filters",
    			"import_custom",
    			"import_custom",
    			"javascript",
    			"lazy-eval",
    			"media",
    			"mixins-args",
    			"mixins-closure",
    			"mixins-guards"
    	};
    	final int ITERATIONS = 5;
		for(int i=0;i<ITERATIONS;i++){
			for(final String filename : filenames){
				new Thread(new Runnable() {
					public void run() {
						try{
							testCompile(filename);
							waiter.resume();
						}catch(Throwable t){
							waiter.fail(t);
						}
					}
	
				}).start();
			}
		}
		waiter.await(60*1000, ITERATIONS * filenames.length);
	}
	
	private void testCompile(String filename) throws Exception {
        testCompile(toFile("compatibility/less/" + filename + ".less"), toFile("compatibility/css/" + filename + ".css"));
    }
	
	protected void testCompile(File lessFile, File cssFile) throws Exception {
		String expected = FileUtils.readFileToString(cssFile);
		String actual = lessCompiler.compile(lessFile);
		waiter.assertEquals(expected.replace("\r\n", "\n"), actual);
	}
}
