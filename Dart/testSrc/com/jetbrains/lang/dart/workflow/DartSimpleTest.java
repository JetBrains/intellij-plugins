// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.workflow;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.jetbrains.lang.dart.ide.actions.DartStyleAction;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceEvaluator;
import com.jetbrains.lang.dart.util.DartPsiImplUtil;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

public class DartSimpleTest extends TestCase {
  private static void doTestUnquoteDartString(@NotNull final String inputString,
                                              @NotNull final String expectedUnquoted,
                                              final int expectedStartOffset,
                                              final int expectedEndOffset) {
    final Pair<String, TextRange> result = DartPsiImplUtil.getUnquotedDartStringAndItsRange(inputString);
    assertEquals(expectedUnquoted, result.first);
    assertEquals(expectedStartOffset, result.second.getStartOffset());
    assertEquals(expectedEndOffset, result.second.getEndOffset());
  }

  private static void doTestDebuggerErrorText(@NotNull final String rawErrorText, @NotNull final String expected) {
    assertEquals(expected, DartVmServiceEvaluator.getPresentableError(rawErrorText));
  }

  public void testUnquoteDartString() {
    doTestUnquoteDartString("", "", 0, 0); // not valid string
    doTestUnquoteDartString("r", "r", 0, 1); // not valid string
    doTestUnquoteDartString("rr'", "rr'", 0, 3); // not valid string
    doTestUnquoteDartString("x'", "x'", 0, 2); // not valid string
    doTestUnquoteDartString("x\"", "x\"", 0, 2); // not valid string
    doTestUnquoteDartString("r'", "", 2, 2); // not closed string
    doTestUnquoteDartString("r\"", "", 2, 2); // not closed string
    doTestUnquoteDartString("r'''", "", 4, 4); // not closed string
    doTestUnquoteDartString("r\"\"\"", "", 4, 4); // not closed string
    doTestUnquoteDartString("r'''''", "''", 4, 6); // not closed string
    doTestUnquoteDartString("r\"\"\"\"", "\"", 4, 5); // not closed string
    doTestUnquoteDartString("'''", "", 3, 3); // not closed string
    doTestUnquoteDartString("\"\"\"", "", 3, 3); // not closed string
    doTestUnquoteDartString("'''''", "''", 3, 5); // not closed string
    doTestUnquoteDartString("\"\"\"\"", "\"", 3, 4); // not closed string
    doTestUnquoteDartString("'", "", 1, 1); // not closed string
    doTestUnquoteDartString("\"", "", 1, 1); // not closed string
    doTestUnquoteDartString("'a", "a", 1, 2); // not closed string
    doTestUnquoteDartString("\"a", "a", 1, 2); // not closed string
    doTestUnquoteDartString("r'", "", 2, 2); // not closed string
    doTestUnquoteDartString("r\"", "", 2, 2); // not closed string
    doTestUnquoteDartString("r'a", "a", 2, 3); // not closed string
    doTestUnquoteDartString("r\"a", "a", 2, 3); // not closed string
    doTestUnquoteDartString("''", "", 1, 1);
    doTestUnquoteDartString("\"\"", "", 1, 1);
    doTestUnquoteDartString("''''''", "", 3, 3);
    doTestUnquoteDartString("\"\"\"\"\"\"", "", 3, 3);
    doTestUnquoteDartString("r''", "", 2, 2);
    doTestUnquoteDartString("r\"\"", "", 2, 2);
    doTestUnquoteDartString("r''''''", "", 4, 4);
    doTestUnquoteDartString("r\"\"\"\"\"\"", "", 4, 4);
    doTestUnquoteDartString("r'''a'''", "a", 4, 5);
    doTestUnquoteDartString("r\"\"\"a\"\"\"", "a", 4, 5);
    doTestUnquoteDartString("r'a'", "a", 2, 3);
    doTestUnquoteDartString("r\"a\"", "a", 2, 3);
    doTestUnquoteDartString("'''a'''", "a", 3, 4);
    doTestUnquoteDartString("\"\"\"a\"\"\"", "a", 3, 4);
    doTestUnquoteDartString("'abc'", "abc", 1, 4);
    doTestUnquoteDartString("\"abc\"", "abc", 1, 4);
  }

  public void testDebuggerErrorText() {
    doTestDebuggerErrorText("", "Cannot evaluate");
    doTestDebuggerErrorText("Error:", "Cannot evaluate");
    doTestDebuggerErrorText("a\nb\nc", "Cannot evaluate");
    doTestDebuggerErrorText("Error: '': error: line 1 pos 9: receiver 'this' is not in scope\n() => 1+this.foo();",
                            "receiver 'this' is not in scope");
    doTestDebuggerErrorText("""
                              Error: Unhandled exception:

                              No top-level getter 'foo' declared.

                              NoSuchMethodError: method not found: 'foo'""",
                            "No top-level getter 'foo' declared.");
    doTestDebuggerErrorText("""
                              Unhandled exception:

                              No top-level getter 'foo' declared.

                              NoSuchMethodError: method not found: 'foo'""",
                            "No top-level getter 'foo' declared.");
  }

  public void testFormattedRegionDetection() {
    doTestFormattedRegionDetection(" ", 0, 1, "", 0, 0);
    doTestFormattedRegionDetection(" ", 0, 1, " ", 0, 1);
    doTestFormattedRegionDetection("abc", 0, 3, "abc", 0, 3);
    doTestFormattedRegionDetection(" abc ", 0, 5, " a\nb\tc ", 0, 7);
    doTestFormattedRegionDetection(" abc ", 0, 5, "abc", 0, 3);
    doTestFormattedRegionDetection(" abc ", 1, 4, "abc", 0, 3);
    doTestFormattedRegionDetection(" abc ", 1, 4, " a b\n c ", 1, 7);
    doTestFormattedRegionDetection("abc", 0, 3, " abc ", 0, 4);
    doTestFormattedRegionDetection("abc", 1, 2, "  abc  ", 3, 4);
    doTestFormattedRegionDetection(" a b c ", 2, 4, "abc", 1, 2);
    doTestFormattedRegionDetection(" a\nb\nc ", 2, 4, "  a b  c  ", 3, 5);
    doTestFormattedRegionDetection("  a  b  c  d", 3, 9, "abcd", 1, 3);
    doTestFormattedRegionDetection("  a  b  c  d", 3, 9, " a b c d ", 2, 6);
    doTestFormattedRegionDetection("  a  b  c  d", 3, 9, "  a  b  c  d  ", 3, 9);
    doTestFormattedRegionDetection("  a  b  c  d", 3, 9, "   a   b   c   d   ", 4, 12);
    doTestFormattedRegionDetection("  a  b  c  d", 5, 11, "   a   b   c   d   ", 7, 15);
    doTestFormattedRegionDetection("a    b    c", 2, 9, "abc", 1, 2);
    doTestFormattedRegionDetection("a    b    c", 2, 9, "a b c", 2, 4);
    doTestFormattedRegionDetection("a    b    c", 2, 9, "a  b  c", 2, 6);
    doTestFormattedRegionDetection("a    b    c", 2, 9, "a   b   c", 2, 8);
    doTestFormattedRegionDetection("a    b    c", 2, 9, "a    b    c", 2, 9);
    doTestFormattedRegionDetection("a    b    c", 2, 9, "a     b     c", 2, 10);
    doTestFormattedRegionDetection("a    b    c", 2, 9, "a      b      c", 2, 11);
    doTestFormattedRegionDetection("a    b    c", 2, 9, "  a      b      c  ", 4, 13);
  }

  private static void doTestFormattedRegionDetection(@NotNull String inputText,
                                                     int rangeStartInInput,
                                                     int rangeEndInInput,
                                                     @NotNull String formattedText,
                                                     int expectedRangeStartInResult,
                                                     int expectedRangeEndInResult) {
    TextRange inputRange = TextRange.create(rangeStartInInput, rangeEndInInput);
    TextRange expectedResultRange = TextRange.create(expectedRangeStartInResult, expectedRangeEndInResult);
    assertEquals(expectedResultRange, DartStyleAction.getRangeInFormattedText(inputText, inputRange, formattedText));
  }
}
