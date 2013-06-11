package org.lesscss;

import java.io.File;
import java.io.IOException;

/**
 * @author Jackstf
 */
public interface LessResolver {

  String resolve(String filename) throws IOException;

  long getLastModified(String filename);

  LessResolver resolveImport(String parent);

  File file(String path);
  
}
