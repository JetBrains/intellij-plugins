package com.jetbrains.lang.dart.dart_style;

import gnu.trove.THashSet;

import java.util.Set;

/**
 * Run the dart_style test suite using the expected output as the input.
 * This quickly identifies those areas that the formatter changes
 * properly formatted code into improperly formatted code.
 */
public class DartStyleLenientTest extends DartStyleTest {

  /** The set of tests that are known to fail only in lenient mode. */
  private static final Set<String> KNOWN_TO_FAIL_LENIENT = new THashSet<>();

  static {
    //KNOWN_TO_FAIL_LENIENT.add("regression/0000/0083.unit:1");
  }

  /**
   * Run a test defined in "*.unit" or "*.stmt" file inside directory <code>dirName</code>.
   */
  protected void runTestInDirectory(String dirName) throws Exception {
    Set<String> fail = new THashSet<>();
    fail.addAll(KNOWN_TO_FAIL);
    fail.addAll(KNOWN_TO_FAIL_LENIENT);
    runTestInDirectory(dirName, fail);
  }

  protected SourceCode extractSourceSelection(String input, String expectedOutput, boolean isCompilationUnit) {
    return extractSelection(expectedOutput, isCompilationUnit);
  }
}
