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

/**
 * An exception that provides information on a LESS compilation error.
 * 
 * @author Marcel Overdijk
 */
@SuppressWarnings("serial")
public class LessException extends Exception {

    /**
     * Constructs a new <code>LessException</code>.
     * 
     * @param cause The cause.
     */
    public LessException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new <code>LessException</code>.
     * 
     * @param message The message.
     * @param cause The cause.
     */
    public LessException(String message, Throwable cause) {
        super(message, cause);
    }
}
