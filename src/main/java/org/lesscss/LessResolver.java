package org.lesscss;

import java.io.IOException;

/**
 * @author Jackstf
 */
public interface LessResolver {

  boolean exists(String filename);

  String resolve(String filename) throws IOException;

  long getLastModified(String filename);

  LessResolver resolveImport(String parent);
  
}
