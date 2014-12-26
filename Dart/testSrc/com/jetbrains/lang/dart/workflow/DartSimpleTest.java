package com.jetbrains.lang.dart.workflow;

import com.jetbrains.lang.dart.util.DartPsiImplUtil;
import junit.framework.TestCase;

public class DartSimpleTest extends TestCase {
  private static void doTestUnquoteDartString(final String inputString, final String expectedUnquoted) {
    assertEquals(expectedUnquoted, DartPsiImplUtil.unquoteDartString(inputString));
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
}
