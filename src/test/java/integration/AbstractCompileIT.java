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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Before;

import org.lesscss.LessCompiler;

public abstract class AbstractCompileIT {

	protected LessCompiler lessCompiler;

	@Before
	public void setUp() throws Exception {
		lessCompiler = new LessCompiler();
	}

	protected void testCompile(File lessFile, File cssFile) throws Exception {
		String expected = FileUtils.readFileToString(cssFile);
		String actual = lessCompiler.compile(lessFile);
		assertEquals(expected.replace("\r\n", "\n"), actual);
	}

	protected void testCompile(File lessFile, File cssFile, boolean compress) throws Exception {
		lessCompiler.setCompress(compress);
		testCompile(lessFile, cssFile);
	}

	protected File toFile(String filename) {
		URL url = CompatibilityIT.class.getClassLoader().getResource(filename);
		File file = FileUtils.toFile(url);
		return file;
	}

	protected URL toURL(String filename) throws MalformedURLException {
		return AbstractCompileIT.class.getClassLoader().getResource(filename);
	}
}
