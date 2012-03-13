lesscss-java
============

LESS CSS Compiler for Java is a library to compile LESS sources to CSS stylesheets.

The compiler uses Rhino, Envjs (simulated browser environment written in JavaScript), and the official LESS JavaScript compiler.

Look at the simple example below to compile LESS to CSS:
 
    // Instantiate the LESS compiler
    LessCompiler lessCompiler = new LessCompiler();
    
    // Compile LESS input string to CSS output string
    String css = lessCompiler.compile("@color: #4D926F; #header { color: @color; }");
    
    // Or compile LESS input file to CSS output file
    lessCompiler.compile(new File("main.less"), new File("main.css"));


Authors
-------

**Marcel Overdijk**

+ marcel@overdijk.me
+ http://twitter.com/marceloverdijk
+ http://github.com/marceloverdijk


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
