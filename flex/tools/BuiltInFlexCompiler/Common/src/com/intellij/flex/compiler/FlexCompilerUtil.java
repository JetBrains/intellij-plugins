package com.intellij.flex.compiler;

import flex2.compiler.config.ConfigurationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FlexCompilerUtil {

  public static void ensureFileCanBeCreated(final File file) throws ConfigurationException {
    if (file.isDirectory()) {
      throw new ConfigurationException(file + " is a directory");
    }
    if (!file.exists()) {
      final File parent = file.getParentFile();
      if (parent != null && !parent.exists()) {
        final boolean ok = parent.mkdirs();
        if (!ok && !parent.exists()) { // check exists() once more because it could be created in another thread
          throw new ConfigurationException("Can't create directory '" + parent + "'");
        }
      }
      try {
        new FileOutputStream(file).close();
        file.delete();
      }
      catch (IOException e) {
        throw new ConfigurationException("Can't create file '" + file.getPath() + "': " + e.getMessage());
      }
    }
  }
}
