/* Copyright 2011 The Apache Software Foundation.
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.lesscss.LessSource;

@PrepareForTest({Context.class, FileUtils.class, LessCompiler.class})
@RunWith(PowerMockRunner.class)
public class LessCompilerTest {

    private static final String COMPILE_STRING = "var result; var parser = new(less.Parser); parser.parse(input, function (e, tree) { if (e instanceof Object) { throw e } result = tree.toCSS({compress: false}) });";
    
    private LessCompiler lessCompiler;
    
    @Mock private Log log;
    
    @Mock private Context cx;
    @Mock private Global global;
    @Mock private Scriptable scope;
    
    @Mock private URL envJsFile;
    @Mock private URLConnection envJsURLConnection;
    @Mock private InputStream envJsInputStream;
    @Mock private InputStreamReader envJsInputStreamReader;
    
    @Mock private URL lessJsFile;
    @Mock private URLConnection lessJsURLConnection;
    @Mock private InputStream lessJsInputStream;
    @Mock private InputStreamReader lessJsInputStreamReader;
    
    @Mock private File inputFile;
    @Mock private File outputFile;
    @Mock private LessSource lessSource;
    
    private String less = "less";
    private String css = "css";
    
    @Before
    public void setUp() throws Exception {
        lessCompiler = new LessCompiler();
        
        when(log.isDebugEnabled()).thenReturn(false);
        FieldUtils.writeField(lessCompiler, "log", log, true);
    }
    
    @Test
    public void testNewLessCompiler() throws Exception {
        assertEquals(LessCompiler.class.getClassLoader().getResource("META-INF/env.rhino.js"), FieldUtils.readField(lessCompiler, "envJs", true));
        assertEquals(LessCompiler.class.getClassLoader().getResource("META-INF/less.js"), FieldUtils.readField(lessCompiler, "lessJs", true));
        assertEquals(Collections.EMPTY_LIST, FieldUtils.readField(lessCompiler, "customJs", true));
    }
    
    @Test
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
        
        when(envJsFile.openConnection()).thenReturn(envJsURLConnection);
        when(envJsURLConnection.getInputStream()).thenReturn(envJsInputStream);
        whenNew(InputStreamReader.class).withArguments(envJsInputStream).thenReturn(envJsInputStreamReader);
        
        when(lessJsFile.openConnection()).thenReturn(lessJsURLConnection);
        when(lessJsURLConnection.getInputStream()).thenReturn(lessJsInputStream);
        whenNew(InputStreamReader.class).withArguments(lessJsInputStream).thenReturn(lessJsInputStreamReader);

        lessCompiler.setEnvJs(envJsFile);
        lessCompiler.setLessJs(lessJsFile);
        lessCompiler.init();
        
        verifyStatic();
        Context.enter();
        verify(cx).setOptimizationLevel(-1);
        verify(cx).setLanguageVersion(Context.VERSION_1_7);
        
        verifyNew(Global.class).withNoArguments();
        verify(global).init(cx);
        
        // verify(envJsFile).openConnection();
        verify(envJsURLConnection).getInputStream();
        verifyNew(InputStreamReader.class).withArguments(envJsInputStream);
        verify(cx).evaluateReader(scope, envJsInputStreamReader, "env.rhino.js", 1, null);
        
        // verify(lessJsFile).openConnection();
        verify(lessJsURLConnection).getInputStream();
        verifyNew(InputStreamReader.class).withArguments(lessJsInputStream);
        verify(cx).evaluateReader(scope, lessJsInputStreamReader, "less.js", 1, null);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testInitThrowsIllegalStateExceptionWhenNotAbleToInitilize() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        
        whenNew(Global.class).withNoArguments().thenReturn(global);
        
        when(cx.initStandardObjects(global)).thenReturn(scope);
        
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
        
        verify(log).error(anyString());
    }
    
    @Test
    public void testCompileStringWhenNotInitialized() throws Exception {
        mockStatic(Context.class);
        when(Context.enter()).thenReturn(cx);
        
        whenNew(Global.class).withNoArguments().thenReturn(global);
        
        when(cx.initStandardObjects(global)).thenReturn(scope);
        
        when(envJsFile.openConnection()).thenReturn(envJsURLConnection);
        when(envJsURLConnection.getInputStream()).thenReturn(envJsInputStream);
        whenNew(InputStreamReader.class).withArguments(envJsInputStream).thenReturn(envJsInputStreamReader);
        
        when(lessJsFile.openConnection()).thenReturn(lessJsURLConnection);
        when(lessJsURLConnection.getInputStream()).thenReturn(lessJsInputStream);
        whenNew(InputStreamReader.class).withArguments(lessJsInputStream).thenReturn(lessJsInputStreamReader);
        
        when(scope.get("result", scope)).thenReturn(css);
        
        lessCompiler.setEnvJs(envJsFile);
        lessCompiler.setLessJs(lessJsFile);
        
        assertEquals(css, lessCompiler.compile(less));
        
        verifyStatic();
        Context.enter();
        verify(cx).setOptimizationLevel(-1);
        verify(cx).setLanguageVersion(Context.VERSION_1_7);
        
        verifyNew(Global.class).withNoArguments();
        verify(global).init(cx);
        
        // verify(envJsFile).openConnection();
        verify(envJsURLConnection).getInputStream();
        verifyNew(InputStreamReader.class).withArguments(envJsInputStream);
        verify(cx).evaluateReader(scope, envJsInputStreamReader, "env.rhino.js", 1, null);
        
        // verify(lessJsFile).openConnection();
        verify(lessJsURLConnection).getInputStream();
        verifyNew(InputStreamReader.class).withArguments(lessJsInputStream);
        verify(cx).evaluateReader(scope, lessJsInputStreamReader, "less.js", 1, null);
        
        verify(scope).put("input", scope, less);
        verify(scope).put("result", scope, "");
        
        verify(cx).evaluateString(scope, COMPILE_STRING, "compile.js", 1, null);
        
        verify(scope).get("result", scope);
    }
    
    @Test
    public void testCompileStringToString() throws Exception {
        FieldUtils.writeField(lessCompiler, "cx", cx, true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        
        when(scope.get("result", scope)).thenReturn(css);
        
        assertEquals(css, lessCompiler.compile(less));
        
        verify(scope).put("input", scope, less);
        verify(scope).put("result", scope, "");
        
        verify(cx).evaluateString(scope, COMPILE_STRING, "compile.js", 1, null);
        
        verify(scope).get("result", scope);
    }
    
    @Test
    public void testCompileFileToString() throws Exception {
        FieldUtils.writeField(lessCompiler, "cx", cx, true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        
        whenNew(LessSource.class).withArguments(inputFile).thenReturn(lessSource);
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
        when(scope.get("result", scope)).thenReturn(css);
        
        assertEquals(css, lessCompiler.compile(inputFile));
        
        verifyNew(LessSource.class).withArguments(inputFile);
        verify(lessSource).getNormalizedContent();

        verify(scope).put("input", scope, less);
        verify(scope).put("result", scope, "");
        
        verify(cx).evaluateString(scope, COMPILE_STRING, "compile.js", 1, null);
        
        verify(scope).get("result", scope);
    }
    
    @Test
    public void testCompileFileToFile() throws Exception {
        FieldUtils.writeField(lessCompiler, "cx", cx, true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        
        whenNew(LessSource.class).withArguments(inputFile).thenReturn(lessSource);
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
        when(scope.get("result", scope)).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(inputFile, outputFile);
        
        verifyNew(LessSource.class).withArguments(inputFile);
        verify(lessSource).getNormalizedContent();

        verify(scope).put("input", scope, less);
        verify(scope).put("result", scope, "");
        
        verify(cx).evaluateString(scope, COMPILE_STRING, "compile.js", 1, null);
        
        verify(scope).get("result", scope);
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, null);
    }
    
    @Test
    public void testCompileFileToFileWithForceTrue() throws Exception {
        FieldUtils.writeField(lessCompiler, "cx", cx, true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        
        whenNew(LessSource.class).withArguments(inputFile).thenReturn(lessSource);
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
        when(scope.get("result", scope)).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(inputFile, outputFile, true);
        
        verifyNew(LessSource.class).withArguments(inputFile);
        verify(lessSource).getNormalizedContent();

        verify(scope).put("input", scope, less);
        verify(scope).put("result", scope, "");
        
        verify(cx).evaluateString(scope, COMPILE_STRING, "compile.js", 1, null);
        
        verify(scope).get("result", scope);
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, null);
    }
    
    @Test
    public void testCompileFileToFileWithForceFalseAndOutputNotExists() throws Exception {
        FieldUtils.writeField(lessCompiler, "cx", cx, true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        
        when(outputFile.exists()).thenReturn(false);
        
        whenNew(LessSource.class).withArguments(inputFile).thenReturn(lessSource);
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
        when(scope.get("result", scope)).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(inputFile, outputFile, false);
        
        verifyNew(LessSource.class).withArguments(inputFile);
        
        verify(outputFile).exists();
        
        verify(lessSource).getNormalizedContent();

        verify(scope).put("input", scope, less);
        verify(scope).put("result", scope, "");
        
        verify(cx).evaluateString(scope, COMPILE_STRING, "compile.js", 1, null);
        
        verify(scope).get("result", scope);
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, null);
    }
    
    @Test
    public void testCompileFileToFileWithForceFalseAndOutputExistsAndLessSourceModified() throws Exception {
        FieldUtils.writeField(lessCompiler, "cx", cx, true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        
        whenNew(LessSource.class).withArguments(inputFile).thenReturn(lessSource);
        
        when(outputFile.exists()).thenReturn(true);
        when(outputFile.lastModified()).thenReturn(1l);
        
        when(lessSource.getLastModifiedIncludingImports()).thenReturn(2l);
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
        when(scope.get("result", scope)).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(inputFile, outputFile, false);
        
        verifyNew(LessSource.class).withArguments(inputFile);
        
        verify(outputFile).exists();
        verify(outputFile).lastModified();
        
        verify(lessSource).getLastModifiedIncludingImports();
        verify(lessSource).getNormalizedContent();

        verify(scope).put("input", scope, less);
        verify(scope).put("result", scope, "");
        
        verify(cx).evaluateString(scope, COMPILE_STRING, "compile.js", 1, null);
        
        verify(scope).get("result", scope);
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, null);
    }
    
    @Test
    public void testCompileFileToFileWithForceFalseAndOutputExistsAndLessSourceNotModified() throws Exception {
        FieldUtils.writeField(lessCompiler, "cx", cx, true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        
        whenNew(LessSource.class).withArguments(inputFile).thenReturn(lessSource);
        
        when(outputFile.exists()).thenReturn(true);
        when(outputFile.lastModified()).thenReturn(2l);
        
        when(lessSource.getLastModifiedIncludingImports()).thenReturn(1l);
        
        lessCompiler.compile(inputFile, outputFile, false);
        
        verifyNew(LessSource.class).withArguments(inputFile);
        
        verify(outputFile).exists();
        verify(outputFile).lastModified();
        
        verify(lessSource).getLastModifiedIncludingImports();
    }
    
    @Test
    public void testCompileLessSourceToString() throws Exception {
        FieldUtils.writeField(lessCompiler, "cx", cx, true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
        when(scope.get("result", scope)).thenReturn(css);
        
        assertEquals(css, lessCompiler.compile(lessSource));
        
        verify(lessSource).getNormalizedContent();

        verify(scope).put("input", scope, less);
        verify(scope).put("result", scope, "");
        
        verify(cx).evaluateString(scope, COMPILE_STRING, "compile.js", 1, null);
        
        verify(scope).get("result", scope);
    }
    
    @Test
    public void testCompileLessSourceToFile() throws Exception {
        FieldUtils.writeField(lessCompiler, "cx", cx, true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
        when(scope.get("result", scope)).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(lessSource, outputFile);
        
        verify(lessSource).getNormalizedContent();

        verify(scope).put("input", scope, less);
        verify(scope).put("result", scope, "");
        
        verify(cx).evaluateString(scope, COMPILE_STRING, "compile.js", 1, null);
        
        verify(scope).get("result", scope);
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, null);
    }

    @Test
    public void testCompileLessSourceToFileWithForceTrue() throws Exception {
        FieldUtils.writeField(lessCompiler, "cx", cx, true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
        when(scope.get("result", scope)).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(lessSource, outputFile, true);
        
        verify(lessSource).getNormalizedContent();

        verify(scope).put("input", scope, less);
        verify(scope).put("result", scope, "");
        
        verify(cx).evaluateString(scope, COMPILE_STRING, "compile.js", 1, null);
        
        verify(scope).get("result", scope);
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, null);
    }
    
    @Test
    public void testCompileLessSourceToFileWithForceFalseAndOutputNotExists() throws Exception {
        FieldUtils.writeField(lessCompiler, "cx", cx, true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        
        when(outputFile.exists()).thenReturn(false);
        
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
        when(scope.get("result", scope)).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(lessSource, outputFile, false);
        
        verify(outputFile).exists();
        
        verify(lessSource).getNormalizedContent();

        verify(scope).put("input", scope, less);
        verify(scope).put("result", scope, "");
        
        verify(cx).evaluateString(scope, COMPILE_STRING, "compile.js", 1, null);
        
        verify(scope).get("result", scope);
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, null);
    }
    
    @Test
    public void testCompileLessSourceToFileWithForceFalseAndOutputExistsAndLessSourceModified() throws Exception {
        FieldUtils.writeField(lessCompiler, "cx", cx, true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        
        when(outputFile.exists()).thenReturn(true);
        when(outputFile.lastModified()).thenReturn(1l);
        
        when(lessSource.getLastModifiedIncludingImports()).thenReturn(2l);
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
        when(scope.get("result", scope)).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(lessSource, outputFile, false);
        
        verify(outputFile).exists();
        verify(outputFile).lastModified();
        
        verify(lessSource).getLastModifiedIncludingImports();
        verify(lessSource).getNormalizedContent();

        verify(scope).put("input", scope, less);
        verify(scope).put("result", scope, "");
        
        verify(cx).evaluateString(scope, COMPILE_STRING, "compile.js", 1, null);
        
        verify(scope).get("result", scope);
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, null);
    }
    
    @Test
    public void testCompileLessSourceToFileWithForceFalseAndOutputExistsAndLessSourceNotModified() throws Exception {
        FieldUtils.writeField(lessCompiler, "cx", cx, true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        
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
        FieldUtils.writeField(lessCompiler, "cx", cx, true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        
        when(cx.evaluateString(scope, COMPILE_STRING, "compile.js", 1, null)).thenThrow(new JavaScriptException(null, null, 0));
        
        assertEquals(css, lessCompiler.compile(less));
        
        verify(scope).put("input", scope, less);
        verify(scope).put("result", scope, "");
        
        verify(cx).evaluateString(scope, COMPILE_STRING, "compile.js", 1, null);
    }
    
    @Test
    public void testCompress() throws Exception {
        FieldUtils.writeField(lessCompiler, "cx", cx, true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        lessCompiler.setCompress(true);
        
        when(scope.get("result", scope)).thenReturn(css);
        
        assertEquals(css, lessCompiler.compile(less));
        
        verify(scope).put("input", scope, less);
        verify(scope).put("result", scope, "");
        
        verify(cx).evaluateString(scope, "var result; var parser = new(less.Parser); parser.parse(input, function (e, tree) { if (e instanceof Object) { throw e } result = tree.toCSS({compress: true}) });", "compile.js", 1, null);
        
        verify(scope).get("result", scope);
    }
    
    @Test
    public void testEncoding() throws Exception {
        FieldUtils.writeField(lessCompiler, "cx", cx, true);
        FieldUtils.writeField(lessCompiler, "scope", scope, true);
        lessCompiler.setEncoding("utf-8");
        
        whenNew(LessSource.class).withArguments(inputFile).thenReturn(lessSource);
        when(lessSource.getNormalizedContent()).thenReturn(less);
        
        when(scope.get("result", scope)).thenReturn(css);
        
        mockStatic(FileUtils.class);
        
        lessCompiler.compile(inputFile, outputFile);
        
        verifyNew(LessSource.class).withArguments(inputFile);
        verify(lessSource).getNormalizedContent();

        verify(scope).put("input", scope, less);
        verify(scope).put("result", scope, "");
        
        verify(cx).evaluateString(scope, COMPILE_STRING, "compile.js", 1, null);
        
        verify(scope).get("result", scope);
        
        verifyStatic();
        FileUtils.writeStringToFile(outputFile, css, "utf-8");
    }
}
