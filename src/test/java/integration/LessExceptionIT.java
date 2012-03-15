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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import org.lesscss.LessException;

public class LessExceptionIT extends AbstractCompileIT {

    @Test
    public void testException() throws Exception {
        try {
            lessCompiler.compile("a { color: @linkColor; }");
            fail();
        }
        catch (LessException e) {
            System.out.println("m:" + e.getMessage());
            assertTrue(e instanceof LessException);
            assertEquals("variable @linkColor is undefined", e.getMessage());
        }
    }
}
