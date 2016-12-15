package com.jetbrains.lang.dart.workflow;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.jetbrains.lang.dart.ide.completion.DartServerCompletionContributor;
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

  public void testUnquoteDartString() throws Exception {
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

  public void testDebuggerErrorText() throws Exception {
    doTestDebuggerErrorText("", "Cannot evaluate");
    doTestDebuggerErrorText("Error:", "Cannot evaluate");
    doTestDebuggerErrorText("a\nb\nc", "Cannot evaluate");
    doTestDebuggerErrorText("Error: '': error: line 1 pos 9: receiver 'this' is not in scope\n() => 1+this.foo();",
                            "receiver 'this' is not in scope");
    doTestDebuggerErrorText("Error: Unhandled exception:\n\nNo top-level getter 'foo' declared.\n\n" +
                            "NoSuchMethodError: method not found: 'foo'",
                            "No top-level getter 'foo' declared.");
    doTestDebuggerErrorText("Unhandled exception:\n\nNo top-level getter 'foo' declared.\n\n" +
                            "NoSuchMethodError: method not found: 'foo'",
                            "No top-level getter 'foo' declared.");
  }

  public void testIdentifierInTheEndOfTheString() throws Exception {
    assertEquals("", DartServerCompletionContributor.getIdentifierInTheEndOfThisString(""));
    assertEquals("a", DartServerCompletionContributor.getIdentifierInTheEndOfThisString("a"));
    assertEquals("", DartServerCompletionContributor.getIdentifierInTheEndOfThisString("a "));
    assertEquals("", DartServerCompletionContributor.getIdentifierInTheEndOfThisString("a."));
    assertEquals("b", DartServerCompletionContributor.getIdentifierInTheEndOfThisString("a.b"));
    assertEquals("b", DartServerCompletionContributor.getIdentifierInTheEndOfThisString("a b"));
    assertEquals("b1$_", DartServerCompletionContributor.getIdentifierInTheEndOfThisString("a.b1$_"));
    assertEquals("$_", DartServerCompletionContributor.getIdentifierInTheEndOfThisString("a.$_"));
    assertEquals("_", DartServerCompletionContributor.getIdentifierInTheEndOfThisString("a.1_"));
    assertEquals("_", DartServerCompletionContributor.getIdentifierInTheEndOfThisString("1_"));
    assertEquals("abcd", DartServerCompletionContributor.getIdentifierInTheEndOfThisString("abcd"));
    assertEquals("abcd", DartServerCompletionContributor.getIdentifierInTheEndOfThisString("qwer\nabcd"));
  }
}
