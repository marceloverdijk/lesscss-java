package org.lesscss;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * File based implementation of {@link Resource}.
 *
 * @author Anton Pechinsky
 */
public class FileResource implements Resource {

    private File file;

    public FileResource(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null!");
        }
        this.file = file;
    }

    public boolean exists() {
        return file.exists();
    }

    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    public long lastModified() {
        return file.lastModified();
    }

    public Resource createRelative(String relativePath) {
        File relativeFile = new File(file.getParentFile(), relativePath);
        return new FileResource(relativeFile);
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }

    public String getName() {
        return file.getAbsolutePath();
    }
}
