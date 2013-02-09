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
import org.apache.commons.io.FileUtils;

/**
 * Represents the metadata and content of a LESS source.
 * 
 * @author Marcel Overdijk
 */
public class LessSource {

	/**
	 * The <code>Pattern</code> used to match imported files.
	 */
	private static final Pattern IMPORT_PATTERN = Pattern
			.compile(
					"^(?!\\s*//\\s*)@import\\s+(url\\()?\\s*\"(.+)\\s*\"(\\))?\\s*;.*$",
					MULTILINE);

	private File file;
	private String content;
	private String normalizedContent;
	private Map<String, LessSource> imports = new LinkedHashMap<String, LessSource>();

	/**
	 * Constructs a new <code>LessSource</code>.
	 * <p>
	 * This will read the metadata and content of the LESS source, and will
	 * automatically resolve the imports.
	 * </p>
	 * 
	 * @param file
	 *            The <code>File</code> reference to the LESS source to read.
	 * @throws FileNotFoundException
	 *             If the LESS source (or one of its imports) could not be
	 *             found.
	 * @throws IOException
	 *             If the LESS source cannot be read.
	 */
	public LessSource(File file) throws FileNotFoundException, IOException {
		if (file == null) {
			throw new IllegalArgumentException("File must not be null.");
		}
		if (!file.exists()) {
			throw new FileNotFoundException("File " + file.getAbsolutePath()
					+ " not found.");
		}
		this.file = file;
		this.content = this.normalizedContent = FileUtils
				.readFileToString(file);
		resolveImports();
	}

	public LessSource(String source) {
		if (source == null) {
			throw new IllegalArgumentException("Source must not be null.");
		}
		this.content = this.normalizedContent = source;
	}

	public LessSource(String source, Map<String, LessSource> imports)
			throws FileNotFoundException, IOException {
		if (source == null) {
			throw new IllegalArgumentException("Source must not be null.");
		}
		this.content = this.normalizedContent = source;
		this.imports.putAll(imports);
		resolveImports();
	}

	/**
	 * Returns the absolute pathname of the LESS source.
	 * 
	 * @return The absolute pathname of the LESS source.
	 */
	public String getAbsolutePath() {
		return file != null ? file.getAbsolutePath() : "";
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
	 * @return A <code>long</code> value representing the time the file was last
	 *         modified, measured in milliseconds since the epoch (00:00:00 GMT,
	 *         January 1, 1970).
	 */
	public long getLastModified() {
		return file != null ? file.lastModified() : 0;
	}

	/**
	 * Returns the time that the LESS source, or one of its imports, was last
	 * modified.
	 * 
	 * @return A <code>long</code> value representing the time the file was last
	 *         modified, measured in milliseconds since the epoch (00:00:00 GMT,
	 *         January 1, 1970).
	 */
	public long getLastModifiedIncludingImports() {
		long lastModified = getLastModified();
		for (Map.Entry<String, LessSource> entry : imports.entrySet()) {
			LessSource importedLessSource = entry.getValue();
			long importedLessSourceLastModified = importedLessSource
					.getLastModifiedIncludingImports();
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
	 * <code>Map&lt;String, LessSource&gt;</code> which contains the filename
	 * and the <code>LessSource</code>.
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
			importedFile = importedFile.matches(".*\\.(le?|c)ss$") ? importedFile
					: importedFile + ".less";
			boolean css = importedFile.matches(".*css$");
			if (!css) {
				LessSource importedLessSource = null;
				if (imports.get(importedFile) != null) {
					importedLessSource = imports.get(importedFile);
				} else if (file != null) {
					importedLessSource = new LessSource(new File(
							file.getParentFile(), importedFile));
					imports.put(importedFile, importedLessSource);
				} else {
					throw new IllegalArgumentException(
							"When source is string it should not have imports to less files or use LessSource to construct source and also provide all imports to the constructor");
				}
				normalizedContent = normalizedContent.substring(0,
						importMatcher.start())
						+ importedLessSource.getNormalizedContent()
						+ normalizedContent.substring(importMatcher.end());
				importMatcher = IMPORT_PATTERN.matcher(normalizedContent);
			}
		}
	}
}
