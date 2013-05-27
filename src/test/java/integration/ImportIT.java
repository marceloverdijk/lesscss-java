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

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.lesscss.LessCompiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ImportIT extends AbstractCompileIT {

    @Test
    public void testImport() throws Exception {
        testCompile(toFile("import/less/import.less"), toFile("import/css/import.css"));
    }

    @Test
    public void testImportEndsInLess() throws Exception {
        testCompile(toFile("import/endsinless/less/import.less"), toFile("import/endsinless/css/import.css"));
    }

    @Test
    public void testImportFallbacks() throws Exception {
        String expected = FileUtils.readFileToString(toFile("import/fallback/css/fallbacktest.css"));
        LessCompiler lessCompiler1 = new LessCompiler();
        List<File> imports = new ArrayList<File>();
        imports.add(toFile("import/fallback/override"));
        imports.add(toFile("import/fallback/base"));
        String actual = lessCompiler1.compile(toFile("import/fallback/fallbacktest.less"), imports);
        assertEquals(expected.replace("\r\n", "\n"), actual);
    }

    @Test
    public void testImportSingleQuotes() throws Exception {
        testCompile(toFile("import/less/import_quotes.less"), toFile("import/css/import.css"));
    }
}
