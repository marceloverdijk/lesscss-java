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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.MULTILINE;

import org.apache.commons.io.IOUtils;

/**
 * Represents the metadata and content of a LESS source.
 *
 * @author Marcel Overdijk
 */
public class LessSource {

    /**
     * The <code>Pattern</code> used to match imported files.
     */
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^(?!\\s*//\\s*)@import\\s+(url\\(|\\((less|css)\\))?\\s*(\"|')(.+)\\s*(\"|')(\\))?\\s*;.*$", MULTILINE);

    private Resource resource;
    private String content;
    private String normalizedContent;
    private Map<String, LessSource> imports = new LinkedHashMap<String, LessSource>();

    /**
     * Constructs a new <code>LessSource</code>.
     * <p>
     * This will read the metadata and content of the LESS source, and will automatically resolve the imports.
     * </p>
     * <p>
     * The resource is read using the default Charset of the platform
     * </p>
     *
     * @param resource The <code>File</code> reference to the LESS source to read.
     * @throws FileNotFoundException If the LESS source (or one of its imports) could not be found.
     * @throws IOException If the LESS source cannot be read.
     */
    public LessSource(Resource resource) throws FileNotFoundException, IOException {
        this(resource, Charset.defaultCharset());
    }

    /**
     * Constructs a new <code>LessSource</code>.
     * <p>
     * This will read the metadata and content of the LESS resource, and will automatically resolve the imports.
     * </p>
     *
     * @param resource The <code>File</code> reference to the LESS resource to read.
     * @param charset charset used to read the less resource.
     * @throws FileNotFoundException If the LESS resource (or one of its imports) could not be found.
     * @throws IOException If the LESS resource cannot be read.
     */
    public LessSource(Resource resource, Charset charset) throws IOException {
        if (resource == null) {
            throw new IllegalArgumentException("Resource must not be null.");
        }
        if (!resource.exists()) {
            throw new IOException("Resource " + resource + " not found.");
        }
        this.resource = resource;
        this.content = this.normalizedContent = loadResource(resource, charset);
        resolveImports();
    }

    private String loadResource(Resource resource, Charset charset) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = resource.getInputStream();
            return IOUtils.toString(inputStream, charset.name());
        }
        finally {
            inputStream.close();
        }
    }

    /**
     * Returns the absolute pathname of the LESS source.
     *
     * @return The absolute pathname of the LESS source.
     */
    public String getAbsolutePath() {
        return resource.toString();
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
     * @return A <code>long</code> value representing the time the resource was last modified, measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     */
    public long getLastModified() {
        return resource.lastModified();
    }

    /**
     * Returns the time that the LESS source, or one of its imports, was last modified.
     *
     * @return A <code>long</code> value representing the time the resource was last modified, measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
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
            String importedResource = importMatcher.group(4);
            importedResource = importedResource.matches(".*\\.(le?|c)ss$") ? importedResource : importedResource + ".less";
            String importType = importMatcher.group(2)==null ? "less" : importMatcher.group(2);
            boolean css = importedResource.matches(".*css$");
            if (importType.equals("less") || !css) {
                    LessSource importedLessSource = new LessSource(resource.createRelative(importedResource));
                    imports.put(importedResource, importedLessSource);
                    normalizedContent = normalizedContent.substring(0, importMatcher.start()) + importedLessSource.getNormalizedContent() + normalizedContent.substring(importMatcher.end());
                    importMatcher = IMPORT_PATTERN.matcher(normalizedContent);
            }
        }
    }
}
