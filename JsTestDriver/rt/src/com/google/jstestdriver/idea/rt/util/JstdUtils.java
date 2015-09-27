package com.google.jstestdriver.idea.rt.util;

import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.config.ResolvedConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;

/**
 * @author Sergey Simonchik
 */
public class JstdUtils {

  private static final String[] JASMINE_ADAPTER_PREFIXES = new String[] {
    "JasmineAdapter".toLowerCase(),
    "jasmine-adapter".toLowerCase()
  };

  private JstdUtils() {}

  public static boolean isJasmineTests(@NotNull ResolvedConfiguration configuration) {
    if (containsJasmineAdapter(configuration.getFilesList())) {
      return true;
    }
    return containsJasmineAdapter(configuration.getTests());
  }

  private static boolean containsJasmineAdapter(@NotNull Collection<FileInfo> fileInfos) {
    for (FileInfo info : fileInfos) {
      File file = new File(info.getFilePath());
      if (isJasmineAdapter(file)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isJasmineAdapter(@NotNull File file) {
    String fileName = file.getName().toLowerCase();
    if (fileName.endsWith(".js")) {
      for (String prefix : JASMINE_ADAPTER_PREFIXES) {
        if (fileName.startsWith(prefix)) {
          return true;
        }
      }
    }
    return false;
  }

}
