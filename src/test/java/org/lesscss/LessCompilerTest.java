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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lesscss.logging.LessLogger;
import org.mockito.Mock;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.lesscss.LessSource;

@PrepareForTest({Context.class, FileUtils.class, LessCompiler.class})
@RunWith(PowerMockRunner.class)
public class LessCompilerTest {

	private static final String COMPILE_STRING = "function doIt(input, compress) { var result; var parser = new less.Parser(); parser.parse(input, function(e, tree) { if (e instanceof Object) { throw e; } ; result = tree.toCSS({compress: compress}); }); return result; }";
    
    private LessCompiler lessCompiler;
    
    @Mock private LessLogger logger;
    
    @Mock private Context cx;
    @Mock private Global global;
    @Mock private Scriptable scope;
    @Mock private InterpretedFunction compiler;
    
    @Mock private URL envJsFile;
    @Mock private URLConnection envJsURLConnection;
    private static final String envJsURLToString = "env.rhino.js";
    @Mock private InputStream envJsInputStream;
    @Mock private InputStreamReader envJsInputStreamReader;
    
    @Mock private URL lessJsFile;
    @Mock private URLConnection lessJsURLConnection;
    private static final String lessJsURLToString = "less-rhino-1.7.0.js";
    @Mock private InputStream lessJsInputStream;
    @Mock private InputStreamReader lessJsInputStreamReader;
    
    @Mock private File inputFile;
    @Mock private File outputFile;
    @Mock private LessSource lessSource;
    
    @Mock private ScriptableObject compileScope;
    @Mock private ByteArrayOutputStream out;
    
    private String less = "less";
    private String css = "css";
    
    public abstract static class InterpretedFunction implements Script, Function {}
    
    @Before
    public void setUp() throws Exception {
        lessCompiler = new LessCompiler();
        
        when(logger.isDebugEnabled()).thenReturn(false);
        FieldUtils.writeField(lessCompiler, "logger", logger, true);
    }
    
    @Test
    public void testNewLessCompiler() throws Exception {
        assertEquals(LessCompiler.class.getClassLoader().getResource("META-INF/less-rhino-1.7.0.js"), FieldUtils.readField(lessCompiler, "lessJs", true));
        assertEquals(LessCompiler.class.getClassLoader().getResource("META-INF/lessc-rhino-1.7.0.js"), FieldUtils.readField(lessCompiler, "lesscJs", true));
        assertEquals(Collections.EMPTY_LIST, FieldUtils.readField(lessCompiler, "customJs", true));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSetEnvJs() throws Exception {
        URL envJsFile = new File("my-env.js").toURI().toURL();
        lessCompiler.setEnvJs(envJsFile);
        assertEquals(envJsFile, lessCompiler.getEnvJs());
    }
    
    @Test
    public void testSetLessJs() throws Exception {
        URL lessJsFile = new File("my-less.js").toURI().toURL();
        lessCompiler.setLessJs(lessJsFile);
        assertEquals(lessJsFile, lessCompiler.getLessJs());
    }
    
    @Test
    public void testSetCustomJs() throws Exception {
        URL customJsFile = new File("my-custom.js").toURI().toURL();
        lessCompiler.setCustomJs(customJsFile);
        assertEquals(1, lessCompiler.getCustomJs().size());
        assertTrue(lessCompiler.getCustomJs().contains(customJsFile));
    }
    
    @Test
    public void testSetCustomJsList() throws Exception {
        URL customJsFile1 = new File("my-custom-1.js").toURI().toURL();
        URL customJsFile2 = new File("my-custom-2.js").toURI().toURL();
        List<URL> customJsFiles = Arrays.asList(customJsFile1, customJsFile2);
        lessCompiler.setCustomJs(customJsFiles);
        assertEquals(2, lessCompiler.getCustomJs().size());
        assertEquals(customJsFile1, lessCompiler.getCustomJs().get(0));
        assertEquals(customJsFile2, lessCompiler.getCustomJs().get(1));
    }    
    
    @Test
    public void testInit() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        
        whenNew(Global.class).withNoArguments().thenReturn(global);
        
        when(cx.initStandardObjects(global)).thenReturn(scope);
        when(cx.compileReader(lessJsInputStreamReader, lessJsURLToString, 1, null)).thenReturn(compiler);
        
        when(envJsFile.openConnection()).thenReturn(envJsURLConnection);
        when(envJsFile.toString()).thenReturn(envJsURLToString);
        when(envJsURLConnection.getInputStream()).thenReturn(envJsInputStream);
        whenNew(InputStreamReader.class).withArguments(envJsInputStream).thenReturn(envJsInputStreamReader);
        
        when(lessJsFile.openConnection()).thenReturn(lessJsURLConnection);
        when(lessJsFile.toString()).thenReturn(lessJsURLToString);
        when(lessJsURLConnection.getInputStream()).thenReturn(lessJsInputStream);
        whenNew(InputStreamReader.class).withArguments(lessJsInputStream).thenReturn(lessJsInputStreamReader);

        lessCompiler.setLessJs(lessJsFile);
        lessCompiler.init();
        
        verifyStatic();
        Context.enter();
        //verify(cx).setOptimizationLevel(-1);
        verify(cx).setLanguageVersion(Context.VERSION_1_7);
        
        verifyNew(Global.class).withNoArguments();
        verify(global).init(cx);
        
        // verify(envJsFile).openConnection();
        //verify(envJsURLConnection).getInputStream();
        //verifyNew(InputStreamReader.class).withArguments(envJsInputStream);
        //verify(cx).evaluateReader(scope, envJsInputStreamReader, envJsURLToString, 1, null);
        
        // verify(lessJsFile).openConnection();
        verify(lessJsURLConnection).getInputStream();
        verify(cx).compileReader(null, lessJsURLToString, 1, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInitThrowsIllegalArgumentExceptionWhenNotAbleToInitilize() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        
        whenNew(Global.class).withNoArguments().thenReturn(global);
        
        when(cx.initStandardObjects(global)).thenReturn(scope);
        when(cx.compileReader(lessJsInputStreamReader, lessJsURLToString, 1, null)).thenReturn(compiler);
        
        when(envJsFile.openConnection()).thenThrow(new IOException());
        
        lessCompiler.setEnvJs(envJsFile);
        lessCompiler.setLessJs(lessJsFile);
        lessCompiler.init();
        
        verifyStatic();
        Context.enter();
        verify(cx).setOptimizationLevel(-1);
        verify(cx).setLanguageVersion(Context.VERSION_1_7);
        
        verifyNew(Global.class).withNoArguments();
        verify(global).init(cx);
        
        verify(envJsFile).openConnection();
        
        verify(logger).error(anyString(), (Throwable) anyObject());
    }
    
    @Test
    public void testCompileStringWhenNotInitialized() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        
        whenNew(Global.class).withNoArguments().thenReturn(global);
        
        when(cx.initStandardObjects(global)).thenReturn(scope);
        when(cx.compileReader(null, lessJsURLToString, 1, null)).thenReturn(compiler);
        
        when(envJsFile.openConnection()).thenReturn(envJsURLConnection);
        when(envJsFile.toString()).thenReturn(envJsURLToString);
        when(envJsURLConnection.getInputStream()).thenReturn(envJsInputStream);
        
        when(lessJsFile.openConnection()).thenReturn(lessJsURLConnection);
        when(lessJsFile.toString()).thenReturn(lessJsURLToString);
        when(lessJsURLConnection.getInputStream()).thenReturn(lessJsInputStream);
        whenNew(InputStreamReader.class).withArguments(lessJsInputStream).thenReturn(lessJsInputStreamReader);        

    	when(cx.newObject(scope)).thenReturn(compileScope);
    	whenNew(ByteArrayOutputStream.class).withNoArguments().thenReturn(out);
    	when(out.toString()).thenReturn(css);
        
        lessCompiler.setLessJs(lessJsFile);
        
        assertEquals(css, lessCompiler.compile(less));
        
        //verifyStatic();
        //verify(cx).setOptimizationLevel(-1);
        verify(cx).setLanguageVersion(Context.VERSION_1_7);
        
        verifyNew(Global.class).withNoArguments();
        verify(global).init(cx);
        
        // verify(envJsFile).openConnection();
        //verify(envJsURLConnection).getInputStream();
        //verifyNew(InputStreamReader.class).withArguments(envJsInputStream);
        //verify(cx).evaluateReader(scope, envJsInputStreamReader, envJsURLToString, 1, null);
        
        // verify(lessJsFile).openConnection();
        verify(lessJsURLConnection).getInputStream();
        verify(cx).compileReader(null, lessJsURLToString, 1, null);
                
        verify(compiler).call(cx, compileScope, null, new Object[] {});
    }
    
    @Test
    public void testCompileStringToString() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        FieldUtils.writeField(lessCompiler, "compiler", compiler, true);
        FieldUtils.writeField(lessCompiler, "out", out, true);
    	when(cx.newObject(scope)).thenReturn(compileScope);
        
    	when(out.toString()).thenReturn(css);
        
        assertEquals(css, lessCompiler.compile(less));
        
        verify(compiler).call(cx, compileScope, null, new Object[] {});
    }
    
    @Test
    public void testCompileFileToString() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        FieldUtils.writeField(lessCompiler, "compiler", compiler, true);
        FieldUtils.writeField(lessCompiler, "out", out, true);
    	when(cx.newObject(scope)).thenReturn(compileScope);
        
        whenNew(LessSource.class).withArguments(inputFile).thenReturn(lessSource);
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
    	when(out.toString()).thenReturn(css);
        
        assertEquals(css, lessCompiler.compile(inputFile));
        
        verify(compiler).call(cx, compileScope, null, new Object[] {});
    }
    
    @Test
    public void testCompileFileToFile() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        FieldUtils.writeField(lessCompiler, "compiler", compiler, true);
        FieldUtils.writeField(lessCompiler, "out", out, true);
    	when(cx.newObject(scope)).thenReturn(compileScope);
        
        whenNew(LessSource.class).withArguments(inputFile).thenReturn(lessSource);
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
    	when(out.toString()).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(inputFile, outputFile);
                
        verify(compiler).call(cx, compileScope, null, new Object[] {});
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, (String) null);
    }
    
    @Test
    public void testCompileFileToFileWithForceTrue() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        FieldUtils.writeField(lessCompiler, "compiler", compiler, true);
        FieldUtils.writeField(lessCompiler, "out", out, true);
    	when(cx.newObject(scope)).thenReturn(compileScope);
        
        whenNew(LessSource.class).withArguments(inputFile).thenReturn(lessSource);
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
    	when(out.toString()).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(inputFile, outputFile, true);
                
        verify(compiler).call(cx, compileScope, null, new Object[] {});
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, (String) null);
    }
    
    @Test
    public void testCompileFileToFileWithForceFalseAndOutputNotExists() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        FieldUtils.writeField(lessCompiler, "compiler", compiler, true);
        FieldUtils.writeField(lessCompiler, "out", out, true);
    	when(cx.newObject(scope)).thenReturn(compileScope);
        
        when(outputFile.exists()).thenReturn(false);
        
        whenNew(LessSource.class).withArguments(inputFile).thenReturn(lessSource);
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
    	when(out.toString()).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(inputFile, outputFile, false);
        
        verify(outputFile).exists();
                
        verify(compiler).call(cx, compileScope, null, new Object[] {});
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, (String) null);
    }
    
    @Test
    public void testCompileFileToFileWithForceFalseAndOutputExistsAndLessSourceModified() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        FieldUtils.writeField(lessCompiler, "compiler", compiler, true);
        FieldUtils.writeField(lessCompiler, "out", out, true);
    	when(cx.newObject(scope)).thenReturn(compileScope);
        
        when(outputFile.exists()).thenReturn(true);
        when(outputFile.lastModified()).thenReturn(1l);
        
        when(inputFile.lastModified()).thenReturn(2l);
                
    	when(out.toString()).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(inputFile, outputFile, false);
                
        verify(outputFile).exists();
        verify(outputFile).lastModified();
                
        verify(compiler).call(cx, compileScope, null, new Object[] {});
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, (String) null);
    }
    
    @Test
    public void testCompileFileToFileWithForceFalseAndOutputExistsAndLessSourceNotModified() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        FieldUtils.writeField(lessCompiler, "compiler", compiler, true);
        FieldUtils.writeField(lessCompiler, "out", out, true);
    	when(cx.newObject(scope)).thenReturn(compileScope);
        
        whenNew(LessSource.class).withArguments(inputFile).thenReturn(lessSource);
        
        when(outputFile.exists()).thenReturn(true);
        when(outputFile.lastModified()).thenReturn(2l);
        
        when(lessSource.getLastModifiedIncludingImports()).thenReturn(1l);
        
        lessCompiler.compile(inputFile, outputFile, false);
                
        verify(outputFile).exists();
        verify(outputFile).lastModified();
        
    }
    
    @Test
    public void testCompileLessSourceToString() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        FieldUtils.writeField(lessCompiler, "compiler", compiler, true);
        FieldUtils.writeField(lessCompiler, "out", out, true);
    	when(cx.newObject(scope)).thenReturn(compileScope);
        
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
    	when(out.toString()).thenReturn(css);
        
        assertEquals(css, lessCompiler.compile(lessSource));
        
        verify(lessSource).getNormalizedContent();
        
        verify(compiler).call(cx, compileScope, null, new Object[] {});
    }
    
    @Test
    public void testCompileLessSourceToFile() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        FieldUtils.writeField(lessCompiler, "compiler", compiler, true);
        FieldUtils.writeField(lessCompiler, "out", out, true);
    	when(cx.newObject(scope)).thenReturn(compileScope);
        
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
    	when(out.toString()).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(lessSource, outputFile);
        
        verify(lessSource).getNormalizedContent();
        
        verify(compiler).call(cx, compileScope, null, new Object[] {});
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, (String) null);
    }

    @Test
    public void testCompileLessSourceToFileWithForceTrue() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        FieldUtils.writeField(lessCompiler, "compiler", compiler, true);
        FieldUtils.writeField(lessCompiler, "out", out, true);
    	when(cx.newObject(scope)).thenReturn(compileScope);
        
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
    	when(out.toString()).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(lessSource, outputFile, true);
        
        verify(lessSource).getNormalizedContent();
        
        verify(compiler).call(cx, compileScope, null, new Object[] {});
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, (String) null);
    }
    
    @Test
    public void testCompileLessSourceToFileWithForceFalseAndOutputNotExists() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        FieldUtils.writeField(lessCompiler, "compiler", compiler, true);
        FieldUtils.writeField(lessCompiler, "out", out, true);
    	when(cx.newObject(scope)).thenReturn(compileScope);
        
        when(outputFile.exists()).thenReturn(false);
        
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
    	when(out.toString()).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(lessSource, outputFile, false);
        
        verify(outputFile).exists();
        
        verify(lessSource).getNormalizedContent();
        
        verify(compiler).call(cx, compileScope, null, new Object[] {});
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, (String) null);
    }
    
    @Test
    public void testCompileLessSourceToFileWithForceFalseAndOutputExistsAndLessSourceModified() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        FieldUtils.writeField(lessCompiler, "compiler", compiler, true);
        FieldUtils.writeField(lessCompiler, "out", out, true);
    	when(cx.newObject(scope)).thenReturn(compileScope);
        
        when(outputFile.exists()).thenReturn(true);
        when(outputFile.lastModified()).thenReturn(1l);
        
        when(lessSource.getLastModifiedIncludingImports()).thenReturn(2l);
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
    	when(out.toString()).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(lessSource, outputFile, false);
        
        verify(outputFile).exists();
        verify(outputFile).lastModified();
        
        verify(lessSource).getLastModifiedIncludingImports();
        verify(lessSource).getNormalizedContent();
        
        verify(compiler).call(cx, compileScope, null, new Object[] {});
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, (String) null);
    }
    
    @Test
    public void testCompileLessSourceToFileWithForceFalseAndOutputExistsAndLessSourceNotModified() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        FieldUtils.writeField(lessCompiler, "compiler", compiler, true);
        FieldUtils.writeField(lessCompiler, "out", out, true);
    	when(cx.newObject(scope)).thenReturn(compileScope);
        
        when(outputFile.exists()).thenReturn(true);
        when(outputFile.lastModified()).thenReturn(2l);
        
        when(lessSource.getLastModifiedIncludingImports()).thenReturn(1l);
        
        lessCompiler.compile(lessSource, outputFile, false);
        
        verify(outputFile).exists();
        verify(outputFile).lastModified();
        
        verify(lessSource).getLastModifiedIncludingImports();
    }
    
    @Test(expected = LessException.class)
    public void testCompileThrowsLessExceptionWhenCompilationFails() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        FieldUtils.writeField(lessCompiler, "compiler", compiler, true);
        FieldUtils.writeField(lessCompiler, "out", out, true);
    	when(cx.newObject(scope)).thenReturn(compileScope);
        
        JavaScriptException javaScriptException = new JavaScriptException(null, null, 0);
        when(compiler.call(cx, compileScope, null, new Object[] {})).thenThrow(javaScriptException);
        
        assertEquals(css, lessCompiler.compile(less));
    }
    
    @Test
    public void testCompress() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        lessCompiler.setCompress(true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        FieldUtils.writeField(lessCompiler, "compiler", compiler, true);
        FieldUtils.writeField(lessCompiler, "out", out, true);
    	when(cx.newObject(scope)).thenReturn(compileScope);
        
    	when(out.toString()).thenReturn(css);
        
        assertEquals(css, lessCompiler.compile(less));
        
        verify(compiler).call(cx, compileScope, null, new Object[] {});
    }
    
    @Test
    public void testEncoding() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        lessCompiler.setEncoding("utf-8");
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        FieldUtils.writeField(lessCompiler, "compiler", compiler, true);
        FieldUtils.writeField(lessCompiler, "out", out, true);
    	when(cx.newObject(scope)).thenReturn(compileScope);
        
        whenNew(LessSource.class).withArguments(inputFile).thenReturn(lessSource);
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
    	when(out.toString("utf-8")).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(inputFile, outputFile);
                
        verify(compiler).call(cx, compileScope, null, new Object[] {});
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, "utf-8");
    }
}
