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
package integration;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class CompatibilityIT extends AbstractCompileIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        lessCompiler.setCustomJs(Arrays.asList(
                toURL("compatibility/custom.math.js"), 
                toURL("compatibility/custom.color.js"), 
                toURL("compatibility/custom.process.title.js")));
    }
    
    @Test
    public void testColors() throws Exception {
        testCompile("colors");
    }
    
    @Test
    public void testComments() throws Exception {
        testCompile("comments");
    }
    
    @Test
    public void testCss3() throws Exception {
        testCompile("css-3");
    }
    
    @Test
    public void testCssEscapes() throws Exception {
        testCompile("css-escapes");
    }
    
    @Test
    public void testCss() throws Exception {
        testCompile("css");
    }
    
    @Test
    public void testFunctions() throws Exception {
        testCompile("functions");
    }
    
    @Test
    public void testIeFilters() throws Exception {
        testCompile("ie-filters");
    }
    
    @Test
    public void testImport() throws Exception {
    	testCompile("import");
    }

    @Test
    public void testImportCustom() throws Exception {
        testCompile("import_custom");
    }
    
    @Test
    public void testJavascript() throws Exception {
        testCompile("javascript");
    }
    
    @Test
    public void testLazyEval() throws Exception {
        testCompile("lazy-eval");
    }
    
    @Test
    public void testMedia() throws Exception {
        testCompile("media");
    }
    
    @Test
    public void testMixinsArgs() throws Exception {
        testCompile("mixins-args");
    }
    
    @Test
    public void testMixinsClosure() throws Exception {
        testCompile("mixins-closure");
    }
    
    @Test
    public void testMixinsGuards() throws Exception {
        testCompile("mixins-guards");
    }
    
    @Test
    public void testMixinsImportant() throws Exception {
        testCompile("mixins-important");
    }
    
    @Test
    public void testMixinsNested() throws Exception {
        testCompile("mixins-nested");
    }
    
    @Test
    public void testMixinsPattern() throws Exception {
        testCompile("mixins-pattern");
    }
    
    @Test
    public void testMixins() throws Exception {
        testCompile("mixins");
    }
    
    @Test
    public void testOperations() throws Exception {
        testCompile("operations");
    }
    
    @Test
    public void testParens() throws Exception {
        testCompile("parens");
    }
    
    @Test
    public void testRulesets() throws Exception {
        testCompile("rulesets");
    }
    
    @Test
    public void testScope() throws Exception {
        testCompile("scope");
    }
    
    @Test
    public void testSelectors() throws Exception {
        testCompile("selectors");
    }
    
    @Test
    public void testStrings() throws Exception {
        testCompile("strings");
    }
    
    @Test
    public void testVariables() throws Exception {
        testCompile("variables");
    }
    
    @Test
    public void testWhitespace() throws Exception {
        testCompile("whitespace");
    }
    
    private void testCompile(String filename) throws Exception {
        testCompile(toFile("compatibility/less/" + filename + ".less"), toFile("compatibility/css/" + filename + ".css"));
    }
}
