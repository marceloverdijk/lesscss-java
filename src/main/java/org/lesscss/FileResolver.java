package org.lesscss;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * @author Jackstf
 */
public class FileResolver implements LessResolver {

  private final File file;

  public FileResolver(File file) {
    this.file = file;
  }

  public FileResolver() {
    this.file = null;
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

  public FileResolver resolveImport(String parent, String importName) {
    return new FileResolver(file(parent));
  }

  private File file(String path) {
    if (new File(path).isAbsolute()) {
      return new File(path);
    }
    else if (file.getParentFile() != null) {
      return new File(file.getParentFile(), path);
    }
    else if (file.getAbsolutePath().equals(path)) {
      return file;
    }
    else {
      return new File(path);
    }
  }

}
