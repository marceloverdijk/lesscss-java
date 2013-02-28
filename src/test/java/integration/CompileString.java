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

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.lesscss.LessException;
import org.lesscss.LessSource;

public class CompileString extends AbstractCompileIT {

	@Test(expected = LessException.class)
	public void testCompileStringWithImports() throws Exception {
		lessCompiler.compile(FileUtils
				.readFileToString(toFile("import/less/import.less")));
	}

	@Test
	public void testCompileStringWithImportsProvidingAllImports()
			throws Exception {
		String expected = FileUtils
				.readFileToString(toFile("import/css/import.css"));

		Map<String, LessSource> imports = new LinkedHashMap<String, LessSource>();
		imports.put(
				"import1.less",
				new LessSource(FileUtils
						.readFileToString(toFile("import/less/import1.less"))));
		imports.put(
				"import4.less",
				new LessSource(FileUtils
						.readFileToString(toFile("import/less/import4.less"))));

		imports.put(
				"import1/import1a.less",
				new LessSource(
						FileUtils
								.readFileToString(toFile("import/less/import1/import1a.less"))));

		imports.put(
				"import1/import1b.less",
				new LessSource(
						FileUtils
								.readFileToString(toFile("import/less/import1/import1b.less"))));

		String actual = lessCompiler.compile(new LessSource(FileUtils
				.readFileToString(toFile("import/less/import.less")), imports));

		assertEquals(expected.replace("\r\n", "\n"), actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCompileStringWithImportsProvidingFewImports()
			throws Exception {
		Map<String, LessSource> imports = new LinkedHashMap<String, LessSource>();
		imports.put(
				"import1.less",
				new LessSource(FileUtils
						.readFileToString(toFile("import/less/import1.less"))));
		lessCompiler.compile(new LessSource(FileUtils
				.readFileToString(toFile("import/less/import.less")), imports));
	}
}
