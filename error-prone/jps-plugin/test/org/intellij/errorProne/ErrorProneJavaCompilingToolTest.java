// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.intellij.errorProne;


import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class ErrorProneJavaCompilingToolTest {

  private static final String COMPILER_PATH_PROPERTY = "idea.error.prone.compiler.path";

  @Test
  public void testPreprocessorPath() {
    String prev = null;
    try {
      prev = System.getProperty(COMPILER_PATH_PROPERTY);

      ErrorProneJavaCompilingTool tool = new ErrorProneJavaCompilingTool();
      List<String> options = new ArrayList<>();
      options.add("-Xep:CheckerName:LEVEL");
      options.add("-Xep:AnotherChecker:LEVEL");
      options.add("-processorpath");
      options.add("/path/to/some.jar");
      System.getProperties().setProperty(COMPILER_PATH_PROPERTY, "/path/to/internal/libraries.jar");
      tool.preprocessOptions(options);

      assertThat(options).containsSubsequence(
        "-processorpath",
        "/path/to/internal/libraries.jar" + File.pathSeparator + "/path/to/some.jar",
        "-Xplugin:ErrorProne -Xep:CheckerName:LEVEL -Xep:AnotherChecker:LEVEL"
      );
    }
    finally {
      if (prev == null) {
        System.getProperties().remove(COMPILER_PATH_PROPERTY);
      }
      else {
        System.getProperties().setProperty(COMPILER_PATH_PROPERTY, prev);
      }
    }
  }
}