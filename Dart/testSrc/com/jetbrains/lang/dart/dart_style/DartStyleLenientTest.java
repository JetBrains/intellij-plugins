// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.dart_style;

import gnu.trove.THashSet;

import java.util.Set;

/**
 * Run the dart_style test suite using the expected output as the input.
 * This quickly identifies those areas that the formatter changes
 * properly formatted code into improperly formatted code.
 */
public class DartStyleLenientTest extends DartStyleTest {
  /**
   * Run a test defined in "*.unit" or "*.stmt" file inside directory {@code dirName}.
   */
  @Override
  protected void runTestInDirectory(String dirName) throws Exception {
    Set<String> fail = new THashSet<>();
    fail.addAll(KNOWN_TO_FAIL);
    runTestInDirectory(dirName, fail);
  }

  @Override
  protected SourceCode extractSourceSelection(String input, String expectedOutput, boolean isCompilationUnit) {
    return extractSelection(expectedOutput, isCompilationUnit);
  }
}
