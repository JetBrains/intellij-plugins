package com.jetbrains.lang.dart.dart_style;

import gnu.trove.THashSet;

import java.util.Set;

/**
 * Run the dart_style test suite.
 */
public class DartStyleStrictTest extends DartStyleTest {

  /** The set of tests that are known to fail only in strict mode. */
  private static final Set<String> KNOWN_TO_FAIL_STRICT = new THashSet<>();

  static {
    KNOWN_TO_FAIL_STRICT.add("comments/classes.unit:110  remove blank line before beginning of body");
    KNOWN_TO_FAIL_STRICT.add("comments/expressions.stmt:36  space between block comment and other tokens");
    KNOWN_TO_FAIL_STRICT.add("comments/generic_methods.unit:46  var"); // Skip because of forced removal of space
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
    KNOWN_TO_FAIL_STRICT.add("regression/0400/0488.stmt:1"); // NEW 11/16
    KNOWN_TO_FAIL_STRICT.add("regression/0500/0513.unit:1"); // NEW 11/16
    KNOWN_TO_FAIL_STRICT.add("regression/0500/0513.unit:9"); // NEW 11/16

    KNOWN_TO_FAIL_STRICT.add("splitting/maps.stmt:35  empty literal does not force outer split");
    KNOWN_TO_FAIL_STRICT.add("splitting/expressions.stmt:13  adjacent string lines all split together;");

    KNOWN_TO_FAIL_STRICT.add("whitespace/enums.unit:10  trailing comma always splits"); // NEW 11/16
    KNOWN_TO_FAIL_STRICT.add("whitespace/blocks.stmt:58  force blank line after non-empty local function"); // NEW 11/16
    KNOWN_TO_FAIL_STRICT.add("whitespace/compilation_unit.unit:38  collapse extra newlines between declarations");
    KNOWN_TO_FAIL_STRICT.add("whitespace/directives.unit:53  configuration"); // https://github.com/munificent/dep-interface-libraries
    KNOWN_TO_FAIL_STRICT.add("whitespace/directives.unit:57  configuration"); // https://github.com/munificent/dep-interface-libraries
    KNOWN_TO_FAIL_STRICT.add("whitespace/script.unit:8  multiple lines between script and library"); // NEW 11/16
    KNOWN_TO_FAIL_STRICT.add("whitespace/script.unit:23  multiple lines between script and import"); // NEW 11/16
    KNOWN_TO_FAIL_STRICT.add("whitespace/script.unit:38  multiple lines between script and line comment"); // NEW 11/16
    KNOWN_TO_FAIL_STRICT.add("whitespace/script.unit:53  multiple lines between script and block comment"); // NEW 11/16
    KNOWN_TO_FAIL_STRICT.add("whitespace/statements.stmt:2  multiple labels"); // NEW 11/16
  }

  /**
   * Run a test defined in "*.unit" or "*.stmt" file inside directory <code>dirName</code>.
   */
  protected void runTestInDirectory(String dirName) throws Exception {
    Set<String> fail = new THashSet<>();
    fail.addAll(KNOWN_TO_FAIL);
    fail.addAll(KNOWN_TO_FAIL_STRICT);
    runTestInDirectory(dirName, fail);
  }

  protected SourceCode extractSourceSelection(String input, String expectedOutput, boolean isCompilationUnit) {
    return extractSelection(input, isCompilationUnit);
  }
}
