package com.jetbrains.lang.dart.workflow;

import com.jetbrains.lang.dart.ide.runner.server.frame.DartDebuggerEvaluator;
import com.jetbrains.lang.dart.util.DartPsiImplUtil;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

public class DartSimpleTest extends TestCase {
  private static void doTestUnquoteDartString(@NotNull final String inputString, @NotNull final String expectedUnquoted) {
    assertEquals(expectedUnquoted, DartPsiImplUtil.unquoteDartString(inputString));
  }

  private static void doTestDebuggerErrorText(@NotNull final String rawErrorText, @NotNull final String expected) {
    assertEquals(expected, DartDebuggerEvaluator.getPresentableError(rawErrorText));
  }

  public void testUnquoteDartString() throws Exception {
    doTestUnquoteDartString("", ""); // not valid string
    doTestUnquoteDartString("r", "r"); // not valid string
    doTestUnquoteDartString("rr'", "rr'"); // not valid string
    doTestUnquoteDartString("x'", "x'"); // not valid string
    doTestUnquoteDartString("x\"", "x\""); // not valid string
    doTestUnquoteDartString("r'", ""); // not closed string
    doTestUnquoteDartString("r\"", ""); // not closed string
    doTestUnquoteDartString("r'''", ""); // not closed string
    doTestUnquoteDartString("r\"\"\"", ""); // not closed string
    doTestUnquoteDartString("r'''''", "''"); // not closed string
    doTestUnquoteDartString("r\"\"\"\"", "\""); // not closed string
    doTestUnquoteDartString("'''", ""); // not closed string
    doTestUnquoteDartString("\"\"\"", ""); // not closed string
    doTestUnquoteDartString("'''''", "''"); // not closed string
    doTestUnquoteDartString("\"\"\"\"", "\""); // not closed string
    doTestUnquoteDartString("'", ""); // not closed string
    doTestUnquoteDartString("\"", ""); // not closed string
    doTestUnquoteDartString("'a", "a"); // not closed string
    doTestUnquoteDartString("\"a", "a"); // not closed string
    doTestUnquoteDartString("'a", "a"); // not closed string
    doTestUnquoteDartString("\"a", "a"); // not closed string
    doTestUnquoteDartString("r'", ""); // not closed string
    doTestUnquoteDartString("r\"", ""); // not closed string
    doTestUnquoteDartString("r'a", "a"); // not closed string
    doTestUnquoteDartString("r\"a", "a"); // not closed string
    doTestUnquoteDartString("r'a", "a"); // not closed string
    doTestUnquoteDartString("r\"a", "a"); // not closed string
    doTestUnquoteDartString("''", "");
    doTestUnquoteDartString("\"\"", "");
    doTestUnquoteDartString("''''''", "");
    doTestUnquoteDartString("\"\"\"\"\"\"", "");
    doTestUnquoteDartString("r''", "");
    doTestUnquoteDartString("r\"\"", "");
    doTestUnquoteDartString("r''''''", "");
    doTestUnquoteDartString("r\"\"\"\"\"\"", "");
    doTestUnquoteDartString("r'''a'''", "a");
    doTestUnquoteDartString("r\"\"\"a\"\"\"", "a");
    doTestUnquoteDartString("r'a'", "a");
    doTestUnquoteDartString("r\"a\"", "a");
    doTestUnquoteDartString("'''a'''", "a");
    doTestUnquoteDartString("\"\"\"a\"\"\"", "a");
    doTestUnquoteDartString("'abc'", "abc");
    doTestUnquoteDartString("\"abc\"", "abc");
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
  }
}
