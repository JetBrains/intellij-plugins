package com.jetbrains.lang.dart.dart_style;

import gnu.trove.THashSet;

import java.util.Set;

/**
 * Run the dart_style test suite.
 */
public class DartStyleStrictTest extends DartStyleTest {

  /** The set of tests that are known to fail only in strict mode. */
  private static final Set<String> KNOWN_TO_FAIL_STRICT = new THashSet<String>();

  static {
    KNOWN_TO_FAIL_STRICT.add("comments/classes.unit:110  remove blank line before beginning of body");
    KNOWN_TO_FAIL_STRICT.add("comments/expressions.stmt:36  space between block comment and other tokens");
    KNOWN_TO_FAIL_STRICT.add("comments/lists.stmt:10  line comment on opening line");
    KNOWN_TO_FAIL_STRICT.add("comments/lists.stmt:25  block comment with trailing newline");
    KNOWN_TO_FAIL_STRICT.add("comments/lists.stmt:43  multiple comments on opening line");
    KNOWN_TO_FAIL_STRICT.add("comments/lists.stmt:54  multiline trailing block comment");
    KNOWN_TO_FAIL_STRICT.add("comments/lists.stmt:92  remove blank line before beginning of body");
    KNOWN_TO_FAIL_STRICT.add("comments/maps.stmt:10  line comment on opening line");
    KNOWN_TO_FAIL_STRICT.add("comments/maps.stmt:25  block comment with trailing newline");
    KNOWN_TO_FAIL_STRICT.add("comments/maps.stmt:43  multiple comments on opening line");
    KNOWN_TO_FAIL_STRICT.add("comments/maps.stmt:54  multiline trailing block comment");
    KNOWN_TO_FAIL_STRICT.add("comments/maps.stmt:97  remove blank line before beginning of body");
    KNOWN_TO_FAIL_STRICT.add("comments/top_level.unit:8");

    KNOWN_TO_FAIL_STRICT.add("regression/0000/0041.stmt:1  (indent 8)");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0049.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0100/0177.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0201.stmt:1");

    KNOWN_TO_FAIL_STRICT.add("splitting/maps.stmt:35  empty literal does not force outer split");
    KNOWN_TO_FAIL_STRICT.add("splitting/expressions.stmt:13  adjacent string lines all split together;");

    KNOWN_TO_FAIL_STRICT.add("whitespace/compilation_unit.unit:38  collapse extra newlines between declarations");
  }

  /**
   * Run a test defined in "*.unit" or "*.stmt" file inside directory <code>dirName</code>.
   */
  protected void runTestInDirectory(String dirName) throws Exception {
    Set<String> fail = new THashSet<String>();
    fail.addAll(KNOWN_TO_FAIL);
    fail.addAll(KNOWN_TO_FAIL_STRICT);
    runTestInDirectory(dirName, fail);
  }

  protected SourceCode extractSourceSelection(String input, String expectedOutput, boolean isCompilationUnit) {
    return extractSelection(input, isCompilationUnit);
  }
}
