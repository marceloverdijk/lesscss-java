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

import static java.util.regex.Pattern.MULTILINE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the metadata and content of a LESS source.
 * 
 * @author Marcel Overdijk
 */
public class LessSource {

    /**
     * The <code>Pattern</code> used to match imported files.
     */
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^(?!\\s*//\\s*)@import\\s+(url\\()?\\s*\"(.+)\\s*\"(\\))?\\s*;.*$", MULTILINE);
    
    private String filename;
    private String content;
    private String normalizedContent;
    private Map<String, LessSource> imports = new LinkedHashMap<String, LessSource>();
    private LessResolver resolver;
    
    /**
     * Constructs a new <code>LessSource</code>.
     * <p>
     * This will read the metadata and content of the LESS source, and will automatically resolve the imports. 
     * </p>
     * 
     * @param file The <code>File</code> reference to the LESS source to read.
     * @throws FileNotFoundException If the LESS source (or one of its imports) could not be found.
     * @throws IOException If the LESS source cannot be read.
     */
    public LessSource(File file) throws FileNotFoundException, IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null.");
        }
        if (!file.exists()) { 
            throw new FileNotFoundException("File " + file.getAbsolutePath() + " not found.");
        }
        init(file.getAbsolutePath(), new FileResolver(file));
    }
    
    public LessSource(String filename) throws FileNotFoundException, IOException {
      if (filename == null) {
        throw new IllegalArgumentException("Filename not be null.");
      }
      init(filename, new FileResolver());
    }
    
    public LessSource(String filename, LessResolver resolver) throws FileNotFoundException, IOException {
      if (filename == null) {
        throw new IllegalArgumentException("Filename not be null.");
      }
      init(filename, resolver);
    }
    
    public void init(String filename, LessResolver resolver) throws FileNotFoundException, IOException {
      this.resolver = resolver;
      this.filename = filename;
      this.content = this.normalizedContent = resolver.resolve(filename);
      resolveImports();
    }
    
    /**
     * Returns the absolute pathname of the LESS source.
     * 
     * @return The absolute pathname of the LESS source.
     */
    public String getAbsolutePath() {
        return filename;
    }
    
    /**
     * Returns the content of the LESS source.
     * 
     * @return The content of the LESS source.
     */
    public String getContent() {
        return content;
    }
    
    /**
     * Returns the normalized content of the LESS source.
     * <p>
     * The normalized content represents the LESS source as a flattened source 
     * where import statements have been resolved and replaced by the actual 
     * content.  
     * </p>
     * 
     * @return The normalized content of the LESS source.
     */
    public String getNormalizedContent() {
        return normalizedContent;
    }
    
    /**
     * Returns the time that the LESS source was last modified.
     * 
     * @return A <code>long</code> value representing the time the file was last modified, measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     */
    public long getLastModified() {
      return resolver.getLastModified(filename);
    }
    
    /**
     * Returns the time that the LESS source, or one of its imports, was last modified.
     * 
     * @return A <code>long</code> value representing the time the file was last modified, measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     */
    public long getLastModifiedIncludingImports() {
        long lastModified = getLastModified();
        for (Map.Entry<String, LessSource> entry : imports.entrySet()) {
            LessSource importedLessSource = entry.getValue();
            long importedLessSourceLastModified = importedLessSource.getLastModifiedIncludingImports();
            if (importedLessSourceLastModified > lastModified) {
                lastModified = importedLessSourceLastModified;
            }
        }
        return lastModified;
    }
    
    /**
     * Returns the LESS sources imported by this LESS source.
     * <p>
     * The returned imports are represented by a 
     * <code>Map&lt;String, LessSource&gt;</code> which contains the filename and the
     * <code>LessSource</code>.
     * </p>
     * 
     * @return The LESS sources imported by this LESS source.
     */
    public Map<String, LessSource> getImports() {
        return imports;
    }
    
    private void resolveImports() throws FileNotFoundException, IOException {
        Matcher importMatcher = IMPORT_PATTERN.matcher(normalizedContent);
        while (importMatcher.find()) {
            String importedFile = importMatcher.group(2);
            importedFile = importedFile.matches(".*\\.(le?|c)ss$") ? importedFile : importedFile + ".less";
            boolean css = importedFile.matches(".*css$");
            if (!css) {
              LessSource importedLessSource = new LessSource(importedFile, resolver.resolveImport(filename));
                    imports.put(importedFile, importedLessSource);
                    normalizedContent = normalizedContent.substring(0, importMatcher.start()) + importedLessSource.getNormalizedContent() + normalizedContent.substring(importMatcher.end());
                    importMatcher = IMPORT_PATTERN.matcher(normalizedContent);
            }
        }
    }
}
