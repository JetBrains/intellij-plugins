package org.intellij.plugins.markdown.util;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.vfs.LocalFileSystem;

import java.io.FileNotFoundException;
import java.util.Arrays;

public class MarkdownPluginUtil {
  public static String getMarkdownPluginPath() throws FileNotFoundException {
    String[] variants = {
      PathManager.getHomePath() + "/contrib",
      PathManager.getPluginsPath()
    };

    for (String variant : variants) {
      String path = variant + "/markdown";
      if (LocalFileSystem.getInstance().findFileByPath(path) != null) {
        return path;
      }
    }
    throw new FileNotFoundException("Could not set up testlib: could not find plugin paths among: " + Arrays.toString(variants));
  }
}
