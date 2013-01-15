package org.lesscss;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * @author Jackstf
 */
public class FileResolver implements LessResolver {

    private File file = null;
    private File[] searchPathes = null;

    public FileResolver(File file) {
        this.file = file;
    }

    public FileResolver(File file, File... searchPathes) {
        this.file = file;
        this.searchPathes = searchPathes;
    }

    public FileResolver() {
    }

    public boolean exists(String filename) {
        return file(filename).exists();
    }

    public String resolve(String filename) throws IOException {
        return FileUtils.readFileToString(file(filename));
    }

    public long getLastModified(String filename) {
        return file(filename).lastModified();
    }

    public FileResolver resolveImport(String parent) {
        return new FileResolver(file(parent), searchPathes);
    }

    /**
     * Internal method implementing the lookup of a file
     * for a given path in the processing context
     * of this resolver.
     *
     * @param path
     * @return
     */
    private File file(String path) {
        File pathFile = new File(path);
        if (pathFile.isAbsolute()) {
            return pathFile;
        } else if (file.getParentFile() != null) {
            File result = new File(file.getParentFile(), path);
            if (searchPathes != null && !result.exists()) {
                for (File searchPath : searchPathes) {
                    File check = new File(searchPath, path);
                    if (check.exists()) {
                        result = check;
                        break;
                    }
                }
            }
            return result;
        } else if (file.getAbsolutePath().equals(path)) {
            return file;
        } else {
            return pathFile;
        }
    }

}
