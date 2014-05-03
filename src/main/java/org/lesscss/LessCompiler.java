/* Copyright 2011-2012 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lesscss;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.lesscss.logging.LessLogger;
import org.lesscss.logging.LessLoggerFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;

/**
 * The LESS compiler to compile LESS sources to CSS stylesheets.
 * <p>
 * The compiler uses Rhino (JavaScript implementation written in Java), Envjs 
 * (simulated browser environment written in JavaScript), and the official LESS 
 * JavaScript compiler.<br />
 * Note that the compiler is not a Java implementation of LESS itself, but rather 
 * integrates the LESS JavaScript compiler within a Java/JavaScript browser 
 * environment provided by Rhino and Envjs.
 * </p>
 * <p>
 * The compiler comes bundled with the Envjs and LESS JavaScript, so there is 
 * no need to include them yourself. But if needed they can be overridden.
 * </p>
 * <h4>Basic code example:</h4>
 * <pre>
 * LessCompiler lessCompiler = new LessCompiler();
 * String css = lessCompiler.compile("@color: #4D926F; #header { color: @color; }");
 * </pre>
 * 
 * @author Marcel Overdijk
 * @see <a href="http://lesscss.org/">LESS - The Dynamic Stylesheet language</a>
 * @see <a href="http://www.mozilla.org/rhino/">Rhino - JavaScript for Java</a>
 * @see <a href="http://www.envjs.com/">Envjs - Bringing the Browser</a>
 */
public class LessCompiler {

    private static final LessLogger logger = LessLoggerFactory.getLogger(LessCompiler.class);

    private URL lessJs = LessCompiler.class.getClassLoader().getResource("META-INF/less-rhino-1.7.0.js");
    private URL lesscJs = LessCompiler.class.getClassLoader().getResource("META-INF/lessc-rhino-1.7.0.js");
    private List<URL> customJs = Collections.emptyList();
    private List<String> options = Collections.emptyList();
    private Boolean compress = null;
    private String encoding = null;
    
    private Scriptable scope;
    private ByteArrayOutputStream out;
    private Function compiler; 
    
    /**
     * Constructs a new <code>LessCompiler</code>.
     */
    public LessCompiler() {
    }
    
    /**
     * Constructs a new <code>LessCompiler</code>.
     */
    public LessCompiler(List<String> options) {
    	this.options = new ArrayList<String>(options);
    }
    
    public List<String> getOptions() {
		return Collections.unmodifiableList(options);
	}

	public void setOptions(List<String> options) {
        if (scope != null) {
            throw new IllegalStateException("This method can only be called before init()");
        }
		
		this.options = new ArrayList<String>(options);
	}

	/**
     * Returns the Envjs JavaScript file used by the compiler.
     * 
     * @return The Envjs JavaScript file used by the compiler.
     */
    public URL getEnvJs() {
    	throw new IllegalArgumentException("EnvJs is no longer supported.  You don't need this if you use a less-rhino-<version>.js build like the default.");
    }
    
    /**
     * Sets the Envjs JavaScript file used by the compiler.
     * Must be set before {@link #init()} is called.
     * 
     * @param envJs The Envjs JavaScript file used by the compiler.
     */
    public synchronized void setEnvJs(URL envJs) {
    	throw new IllegalArgumentException("EnvJs is no longer supported.  You don't need this if you use a less-rhino-<version>.js build like the default.");
    }
    
    /**
     * Returns the LESS JavaScript file used by the compiler.
     * COMPILE_STRING
     * @return The LESS JavaScript file used by the compiler.
     */
    public URL getLessJs() {
        return lessJs;
    }
    
    /**
     * Sets the LESS JavaScript file used by the compiler.
     * Must be set before {@link #init()} is called.
     * 
     * @param lessJs LESS JavaScript file used by the compiler.
     */
    public synchronized void setLessJs(URL lessJs) {
        if (scope != null) {
            throw new IllegalStateException("This method can only be called before init()");
        }
        this.lessJs = lessJs;
    }
    
    /**
     * Returns the LESSC JavaScript file used by the compiler.
     * COMPILE_STRING
     * @return The LESSC JavaScript file used by the compiler.
     */
    public URL getLesscJs() {
        return lesscJs;
    }
    
    /**
     * Sets the LESSC JavaScript file used by the compiler.
     * Must be set before {@link #init()} is called.
     * 
     * @param lesscJs LESSC JavaScript file used by the compiler.
     */
    public synchronized void setLesscJs(URL lesscJs) {
        if (scope != null) {
            throw new IllegalStateException("This method can only be called before init()");
        }
        this.lesscJs = lesscJs;
    }
    
	/**
     * Returns the custom JavaScript files used by the compiler.
     * 
     * @return The custom JavaScript files used by the compiler.
     */
    public List<URL> getCustomJs() {
        return Collections.unmodifiableList(customJs);
    }
    
    /**
     * Sets a single custom JavaScript file used by the compiler.
     * Must be set before {@link #init()} is called.
     * 
     * @param customJs A single custom JavaScript file used by the compiler.
     */
    public synchronized void setCustomJs(URL customJs) {
        if (scope != null) {
            throw new IllegalStateException("This method can only be called before init()");
        }
        this.customJs = Collections.singletonList(customJs);
    }
    
    /**
     * Sets the custom JavaScript files used by the compiler.
     * Must be set before {@link #init()} is called.
     * 
     * @param customJs The custom JavaScript files used by the compiler.
     */
    public synchronized void setCustomJs(List<URL> customJs) {
        if (scope != null) {
            throw new IllegalStateException("This method can only be called before init()");
        }
    	// copy the list so there's no way for anyone else who holds a reference to the list to modify it
        this.customJs = new ArrayList<URL>(customJs);
    }
    
    /**
     * Returns whether the compiler will compress the CSS. 
     * 
     * @return Whether the compiler will compress the CSS.
     */
    public boolean isCompress() {
        return (compress != null && compress.booleanValue()) ||
        		options.contains("compress") ||
        		options.contains("x");
    }
    
    /**
     * Sets the compiler to compress the CSS.
     * Must be set before {@link #init()} is called.
     * 
     * @param compress If <code>true</code>, sets the compiler to compress the CSS.
     */
    public synchronized void setCompress(boolean compress) {
        if (scope != null) {
            throw new IllegalStateException("This method can only be called before init()");
        }
        this.compress = compress;
    }
    
    /**
     * Returns the character encoding used by the compiler when writing the output <code>File</code>.
     * 
     * @return The character encoding used by the compiler when writing the output <code>File</code>.
     */
    public String getEncoding() {
        return encoding;
    }
    
    /**
     * Sets the character encoding used by the compiler when writing the output <code>File</code>.
     * If not set the platform default will be used.
     * Must be set before {@link #init()} is called.
     * 
     * @param encoding character encoding used by the compiler when writing the output <code>File</code>.
     */
    public synchronized void setEncoding(String encoding) {
        if (scope != null) {
            throw new IllegalStateException("This method can only be called before init()");
        }
        this.encoding = encoding;
    }
    
    /**
     * Initializes this <code>LessCompiler</code>.
     * <p>
     * It is not needed to call this method manually, as it is called implicitly by the compile methods if needed.
     * </p>
     */
    public synchronized void init() {
        long start = System.currentTimeMillis();

        try {
	        Context cx = Context.enter();
	        //cx.setOptimizationLevel(-1);
	        cx.setLanguageVersion(Context.VERSION_1_7);
	        
	        Global global = new Global(); 
	        global.init(cx); 	        
	        scope = cx.initStandardObjects(global);
            scope.put("logger", scope, Context.toObject(logger, scope));
            
            out = new ByteArrayOutputStream();
            global.setOut(new PrintStream(out));
            
            // Combine all of the streams (less, custom, lessc) into one big stream
            List<InputStream> streams = new ArrayList<InputStream>();
            
            // less should be first
            streams.add(lessJs.openConnection().getInputStream());
            
            // then the custom js so it has a chance to add any hooks
	        for(URL url : customJs) {
	        	streams.add(url.openConnection().getInputStream());
	        }
	        
	        // then the lessc so we can do the compile
	        streams.add(lesscJs.openConnection().getInputStream());
	        
	        InputStreamReader reader = new InputStreamReader(new SequenceInputStream(Collections.enumeration(streams)));
            
	        // Load the streams into a function we can run 
            compiler = (Function) cx.compileReader(reader, lessJs.toString(), 1, null);            
	        	        
        }
        catch (Exception e) {
            String message = "Failed to initialize LESS compiler.";
            logger.error(message, e);
            throw new IllegalStateException(message, e);
        }finally{
        	Context.exit();
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Finished initialization of LESS compiler in %,d ms.%n", System.currentTimeMillis() - start);
        }
    }
    
    /**
     * Compiles the LESS input <code>String</code> to CSS. 
     * 
     * @param input The LESS input <code>String</code> to compile. 
     * @return The CSS.
     */
    public String compile(String input) throws LessException {
    	return compile(input, "<inline>");
    }
    
    /**
     * Compiles the LESS input <code>String</code> to CSS, but specifies the source name <code>String</code>.
     *
     * @param input The LESS input <code>String</code> to compile
     * @param name The source's name <code>String</code> to provide better error messages.
     * @return the CSS.
     *
     * @throws LessException any error encountered by the compiler
     */
    public String compile(String input, String name) throws LessException {
    	File tempFile = null;
    	try {
	        tempFile = File.createTempFile("tmp", "less.tmp");
	        FileUtils.writeStringToFile(tempFile, input, this.encoding);
	    	 	    	    	
	        return compile( tempFile, name);
    	} catch (IOException e) {
            throw new LessException(e);
    		
    	} finally {
    		tempFile.delete();
    	}
    }

    /**
     * Compiles the LESS input <code>String</code> to CSS, but specifies the source name <code>String</code>. The entire
     * method is synchronized so that two threads don't read the output at the same time.
     *
     * @param input The LESS input <code>String</code> to compile
     * @param name The source's name <code>String</code> to provide better error messages.
     * @return the CSS.
     *
     * @throws LessException any error encountered by the compiler
     */
    public synchronized String compile(File input, String name) throws LessException {
        if (scope == null) {
            init();
        }
        
        long start = System.currentTimeMillis();
        
        try {        	
        	
        	Context cx = Context.enter();

        	// The scope for compiling <input>
        	ScriptableObject compileScope = (ScriptableObject)cx.newObject(scope);
        	
        	// give it a reference to the parent scope
        	compileScope.setPrototype(scope);
        	compileScope.setParentScope(null);

        	// Copy the default options
        	List<String> options = new ArrayList<String>(this.options);
        	// Set up the arguments for <input>
        	options.add(input.getAbsolutePath());
        	
        	// Add compress if the value is set for backward compatibility
        	if (this.compress != null && this.compress.booleanValue()) {
        		options.add("-x");
        	}
        	
            Scriptable argsObj = cx.newArray(compileScope, options.toArray(new Object[options.size()]));
            //Scriptable argsObj = cx.newArray(compileScope, new Object[] {"-ru", "c.less"});
       	 	compileScope.defineProperty("arguments", argsObj, ScriptableObject.DONTENUM);
       	 	
       	 	// invoke the compiler - we don't pass arguments here because its a script not a real function
       	 	// and we don't care about the result because its written to the output stream (out)
            compiler.call(cx, compileScope, null, new Object[] {});        	
        	
            if (logger.isDebugEnabled()) {
                logger.debug("Finished compilation of LESS source in %,d ms.", System.currentTimeMillis() - start );
            }
            
            return this.encoding != null && !this.encoding.equals("") ? out.toString(encoding) : out.toString();
        }
        catch (Exception e) {
            if (e instanceof JavaScriptException) {
                Scriptable value = (Scriptable)((JavaScriptException)e).getValue();
                if (value != null ) {
                    StringBuilder message = new StringBuilder();
                    if( ScriptableObject.hasProperty(value, "filename") ) {
                        message.append( ScriptableObject.getProperty(value, "filename").toString() );
                    }

                    if( ScriptableObject.hasProperty(value, "line") ) {
                        message.append( "@(" );
                        message.append( ScriptableObject.getProperty(value, "line").toString() );
                        message.append( "," );
                        message.append( ScriptableObject.getProperty(value, "column").toString() );
                        message.append( ")" );
                    }

                    if( ScriptableObject.hasProperty(value, "message") ) {
                        if( message.length() > 0 ) message.append(": ");
                        message.append( ScriptableObject.getProperty(value, "message").toString() );
                    }

                    if( ScriptableObject.hasProperty(value, "extract") ) {
                        List<String> lines = (List<String>) ScriptableObject.getProperty(value, "extract");
                        for( String line : lines ) {
                            if( line != null ) {
                                message.append("\n");
                                message.append( line );
                            }
                        }
                    }

                    throw new LessException(message.toString(), e);
                }
            }
            throw new LessException(e);
        }finally{
        	// reset our ouput stream so we don't copy data on the next invocation
        	out.reset();
        	
        	// we're done with this invocation
        	Context.exit();
        }
    }
    
    /**
     * Compiles the LESS input <code>File</code> to CSS.
     * 
     * @param input The LESS input <code>File</code> to compile.
     * @return The CSS.
     * @throws IOException If the LESS file cannot be read.
     */
    public String compile(File input) throws IOException, LessException {
        return compile(input, input.getName());
    }
    
    /**
     * Compiles the LESS input <code>File</code> to CSS and writes it to the specified output <code>File</code>.
     * 
     * @param input The LESS input <code>File</code> to compile.
     * @param output The output <code>File</code> to write the CSS to.
     * @throws IOException If the LESS file cannot be read or the output file cannot be written.
     */
    public void compile(File input, File output) throws IOException, LessException {
        this.compile(input, output, true);
    }
    
    /**
     * Compiles the LESS input <code>File</code> to CSS and writes it to the specified output <code>File</code>.
     * 
     * @param input The LESS input <code>File</code> to compile.
     * @param output The output <code>File</code> to write the CSS to.
     * @param force 'false' to only compile the LESS input file in case the LESS source has been modified (including imports) or the output file does not exists.
     * @throws IOException If the LESS file cannot be read or the output file cannot be written.
     */
    public void compile(File input, File output, boolean force) throws IOException, LessException {
        if (force || !output.exists() || output.lastModified() < input.lastModified()) {
            String data = compile(input);
            FileUtils.writeStringToFile(output, data, encoding);
        }
    }    
    
    public String compile(LessSource input) throws LessException {
        return compile(input.getNormalizedContent(), input.getName());
    }
    
    /**
     * Compiles the input <code>LessSource</code> to CSS and writes it to the specified output <code>File</code>.
     * 
     * @param input The input <code>LessSource</code> to compile.
     * @param output The output <code>File</code> to write the CSS to.
     * @throws IOException If the LESS file cannot be read or the output file cannot be written.
     */
    public void compile(LessSource input, File output) throws IOException, LessException {
        compile(input, output, true);
    }
    
    /**
     * Compiles the input <code>LessSource</code> to CSS and writes it to the specified output <code>File</code>.
     * 
     * @param input The input <code>LessSource</code> to compile.
     * @param output The output <code>File</code> to write the CSS to.
     * @param force 'false' to only compile the input <code>LessSource</code> in case the LESS source has been modified (including imports) or the output file does not exists.
     * @throws IOException If the LESS file cannot be read or the output file cannot be written.
     */
    public void compile(LessSource input, File output, boolean force) throws IOException, LessException {
        if (force || !output.exists() || output.lastModified() < input.getLastModifiedIncludingImports()) {
            String data = compile(input);
            FileUtils.writeStringToFile(output, data, encoding);
        }
    }    
}
