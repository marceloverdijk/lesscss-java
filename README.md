Official LESS CSS Compiler for Java
===================================

**Latest release**  1.3.3 - compatible with less 1.3.3



LESS CSS Compiler for Java is a library to compile LESS sources to CSS stylesheets.

The compiler uses Rhino, Envjs (simulated browser environment written in JavaScript), and the official LESS JavaScript compiler.

Look at the simple example below to compile LESS to CSS:
 
    // Instantiate the LESS compiler
    LessCompiler lessCompiler = new LessCompiler();
    
    // Compile LESS input string to CSS output string
    String css = lessCompiler.compile("@color: #4D926F; #header { color: @color; }");
    
    // Or compile LESS input file to CSS output file
    lessCompiler.compile(new File("main.less"), new File("main.css"));

LessCompiler is thread safe. In other words, an application only needs one LessCompiler that it can reuse whenever necessary.

To learn more about LESS, please see http://lesscss.org/.


Getting Started
---------------

Maven users should add the library using the following dependency:

    <dependency>
      <groupId>org.lesscss</groupId>
      <artifactId>lesscss</artifactId>
      <version>1.3.3</version>
    </dependency>

(lesscss-java is in the Maven Central repository.)

Non-Maven users should download the latest version and add it to the project's classpath. Also the following dependencies are required:

+ <a href="http://commons.apache.org/io/">Apache Commons IO 2.4</a>
+ <a href="http://commons.apache.org/lang/">Apache Commons Lang 3.1</a>
+ <a href="http://www.mozilla.org/rhino/">Rhino: JavaScript for Java 1.7R4</a>

If [SLF4J](http://www.slf4j.org/) is present in the classpath, it will be used for logging.

Compatibility
-------------

The LESS CSS Compiler for Java contains all LESS compatibility tests. All tests pass, except the @import test case which fails partially as the compiler does not support the media query import feature (yet).

The project also contains integration tests for compiling the Twitter Bootstrap (http://twitter.github.com/bootstrap/) library. If you are using another 3th party LESS library you want to be added to the integration tests, just create a issue and provide a link to the library.


Support
-------

Have a question, or found an issue? Just create a issue: https://github.com/marceloverdijk/lesscss-java/issues


Building From Source
--------------------

Can be built with [Maven 2.2.x](http://maven.apache.org) (or later?) by using the following commands:

    mvn package

or, to install into your local Maven repository:

    mvn install
    
You may also wish to build API Documentation:

    mvn javadoc:javadoc

Authors
-------

**Marcel Overdijk**

+ marcel@overdijk.me
+ http://twitter.com/marceloverdijk
+ http://github.com/marceloverdijk

**Craig Andrews**

+ candrews@integralblue.com
+ http://candrews.integralblue.com

**Christophe Popov**
+ http://twitter.com/chpopov
+ http://uk.linkedin.com/in/hpopov/

Copyright and License
---------------------

Copyright 2012 Marcel Overdijk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
