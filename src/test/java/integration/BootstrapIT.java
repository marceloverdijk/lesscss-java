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

import org.junit.Before;
import org.junit.Test;

public class BootstrapIT extends AbstractCompileIT {

	@Before
	public void setUp() throws Exception {
		super.setUp();
		// set explicit less version if latest version of less gets incompatible with bootstrap
		// lessCompiler.setLessJs(Bootstrap201IT.class.getClassLoader().getResource("bootstrap/less-1.3.0.js"));
	}

	@Test
	public void testBootstrap() throws Exception {
		testCompile("bootstrap");
	}

	@Test
	public void testBootstrapMin() throws Exception {
		testCompile("bootstrap", "bootstrap.min", true);
	}

	private void testCompile(String filename) throws Exception {
		testCompile(filename, filename);
	}

	private void testCompile(String lessFilename, String cssFilename) throws Exception {
		testCompile(lessFilename, cssFilename, false);
	}

	private void testCompile(String lessFilename, String cssFilename, boolean compress) throws Exception {
		testCompile(toFile("bootstrap-3.1.1/less/" + lessFilename + ".less"), toFile("bootstrap-3.1.1/css/" + cssFilename + ".css"), compress);
	}
}
