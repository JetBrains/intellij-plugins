package com.google.jstestdriver.idea.util;

import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.config.ParsedConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class JstdUtils {

  private static final String JASMINE_ADAPTER_PREFIX = "JasmineAdapter".toLowerCase();

  private JstdUtils() {}

  public static boolean isJasmineTests(@NotNull ParsedConfiguration configuration) {
    for (FileInfo info : configuration.getFilesList()) {
      File file = new File(info.getFilePath());
      if (isJasmineAdapter(file)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isJasmineAdapter(@NotNull File file) {
    String fileName = file.getName().toLowerCase();
    return fileName.startsWith(JASMINE_ADAPTER_PREFIX) && fileName.endsWith(".js");
  }

}
