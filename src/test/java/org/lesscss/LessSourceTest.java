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

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({FileUtils.class, LessSource.class})
@RunWith(PowerMockRunner.class)
public class LessSourceTest {

    private LessSource lessSource;
    
    @Mock private File file;
    
    @Mock private LessSource import1;
    @Mock private LessSource import2;
    @Mock private LessSource import3;
    
    private Map<String, LessSource> imports;
    
    private String content = "content";
    private String absolutePath = "path";
    private long lastModified = 1l;
        
    @Before
    public void setUp() throws Exception {
        imports = new LinkedHashMap<String, LessSource>();
        imports.put("import1", import1);
        imports.put("import2", import2);
        imports.put("import3", import3);
    }
    
    @Test
    public void testNewLessSourceWithoutImports() throws Exception {
        when(file.exists()).thenReturn(true);
        mockStatic(FileUtils.class);
        when(FileUtils.readFileToString(file)).thenReturn(content);
        when(file.getAbsolutePath()).thenReturn(absolutePath);
        when(file.lastModified()).thenReturn(lastModified);
        
        lessSource = new LessSource(file);
        
        assertEquals(absolutePath, lessSource.getAbsolutePath());
        assertEquals(content, lessSource.getContent());
        assertEquals(content, lessSource.getNormalizedContent());
        assertEquals(lastModified, lessSource.getLastModified());
        assertEquals(lastModified, lessSource.getLastModifiedIncludingImports());
        assertEquals(0, lessSource.getImports().size());
        
        verifyStatic();
        FileUtils.readFileToString(file);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNewLessSourceFileNull() throws Exception {
        lessSource = new LessSource((File) null); 
    }
    
    @Test(expected = FileNotFoundException.class)
    public void testNewLessSourceFileNotFound() throws Exception {
        when(file.exists()).thenReturn(false);
        lessSource = new LessSource(file); 
    }
    
    @Test
    public void testLastModifiedIncludingImportsWhenNoImportModifiedLater() throws Exception {
        when(file.exists()).thenReturn(true);
        mockStatic(FileUtils.class);
        when(FileUtils.readFileToString(file)).thenReturn(content);
        when(file.getAbsolutePath()).thenReturn(absolutePath);
        when(file.lastModified()).thenReturn(1l);
        
        when(import1.getLastModifiedIncludingImports()).thenReturn(0l);
        when(import2.getLastModifiedIncludingImports()).thenReturn(0l);
        when(import3.getLastModifiedIncludingImports()).thenReturn(0l);
        
        lessSource = new LessSource(file);
        FieldUtils.writeField(lessSource, "imports", imports, true);
        
        assertEquals(1l, lessSource.getLastModifiedIncludingImports());
    }
    
    @Test
    public void testLastModifiedIncludingImportsWhenImportModifiedLater() throws Exception {
        when(file.exists()).thenReturn(true);
        mockStatic(FileUtils.class);
        when(FileUtils.readFileToString(file)).thenReturn(content);
        when(file.getAbsolutePath()).thenReturn(absolutePath);
        when(file.lastModified()).thenReturn(1l);
        
        when(import1.getLastModifiedIncludingImports()).thenReturn(0l);
        when(import2.getLastModifiedIncludingImports()).thenReturn(2l);
        when(import3.getLastModifiedIncludingImports()).thenReturn(0l);
        
        lessSource = new LessSource(file);
        FieldUtils.writeField(lessSource, "imports", imports, true);
        
        assertEquals(2l, lessSource.getLastModifiedIncludingImports());
    }
}
