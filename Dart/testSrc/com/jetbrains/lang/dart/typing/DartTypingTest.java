package com.jetbrains.lang.dart.typing;

import com.intellij.openapi.actionSystem.IdeActions;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.DartFileType;

public class DartTypingTest extends DartCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return "/typing";
  }

  protected void doTest(char charToType) throws Throwable {
    myFixture.configureByFiles(getTestName(false) + ".dart");
    myFixture.type(charToType);
    myFixture.checkResultByFile(getTestName(false) + "_after.dart");
  }

  private void doTypeAndCheck(char charToType, String expected) {
    myFixture.type(charToType);
    myFixture.checkResult(expected);
  }

  private void doBackspaceTest(String expected) {
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_BACKSPACE);
    myFixture.checkResult(expected);
  }

  public void testDocComment() throws Throwable {
    doTest('\n');
  }

  public void testDocComment2() throws Throwable {
    doTest('\n');
  }

  public void testDocComment3() throws Throwable {
    doTest('\n');
  }

  public void testGenericBraceWithMultiCaret() throws Throwable {
    doTest('<');
  }

  public void testGenericBraceWithMultiCaretInDifferentContexts() throws Throwable {
    doTest('<');
  }

  public void testGenericBrace1() throws Throwable {
    doTest('<');
  }

  public void testGenericBrace2() throws Throwable {
    doTest('<');
  }

  public void testGenericBrace3() throws Throwable {
    doTest('<');
  }

  public void testLess() throws Throwable {
    doTest('<');
  }

  public void testStringWithMultiCaret() throws Throwable {
    doTest('{');
  }

  public void testStringWithMultiCaretInDifferentContexts() throws Throwable {
    doTest('{');
  }

  public void testString1() throws Throwable {
    doTest('{');
  }

  public void testString2() throws Throwable {
    doTest('{');
  }

  public void testString3() throws Throwable {
    doTest('{');
  }

  public void testQuote1() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = <caret>");
    doTypeAndCheck('\'', "var foo = '<caret>'");
  }

  public void testQuote2() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = <caret>");
    doTypeAndCheck('"', "var foo = \"<caret>\"");
  }

  public void testQuote3() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = '<caret>'");
    doTypeAndCheck('"', "var foo = '\"<caret>'");
  }

  public void testQuote4() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = \"<caret>\"");
    doTypeAndCheck('\'', "var foo = \"'<caret>\"");
  }

  public void testQuote5() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = \"bar<caret>\"");
    doTypeAndCheck('\'', "var foo = \"bar'<caret>\"");
  }

  public void testQuote6() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "import <caret>");
    doTypeAndCheck('\'', "import '<caret>'");
  }

  public void testQuote7() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "import <caret>");
    doTypeAndCheck('"', "import \"<caret>\"");
  }

  public void testQuote8() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = '<caret>'");
    doTypeAndCheck('\'', "var foo = ''<caret>");
  }

  public void testQuote9() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = \"<caret>\"");
    doTypeAndCheck('\"', "var foo = \"\"<caret>");
  }

  public void testQuote10() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = 'bar<caret>'");
    doTypeAndCheck('\'', "var foo = 'bar'<caret>");
  }

  public void testQuote11() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = \"bar<caret>\"");
    doTypeAndCheck('\"', "var foo = \"bar\"<caret>");
  }

  public void testQuote13() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = 'bar' <caret>");
    doTypeAndCheck('\'', "var foo = 'bar' '<caret>'");
  }

  public void testQuote14() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = \"\" <caret>");
    doTypeAndCheck('\"', "var foo = \"\" \"<caret>\"");
  }

  public void testBackspace1() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = \"<caret> \"");
    doBackspaceTest("var foo = <caret> \"");
  }

  public void testBackspace2() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = \"<caret>\"");
    doBackspaceTest("var foo = <caret>");
  }

  public void testBackspace3() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = '<caret>a'");
    doBackspaceTest("var foo = <caret>a'");
  }

  public void testBackspace4() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "import '<caret>'");
    doBackspaceTest("import <caret>");
  }

  public void testBackspace5() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = \"\"<caret>");
    doBackspaceTest("var foo = \"<caret>");
  }

  public void testBackspace6() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = \" '<caret>' \"");
    doBackspaceTest("var foo = \" <caret>' \"");
  }

  public void testBackspace7() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "var foo = '\"<caret>\"'");
    doBackspaceTest("var foo = '<caret>\"'");
  }

  public void testWEB_8315() throws Throwable {
    myFixture.configureByText(DartFileType.INSTANCE, "class X {\n" +
                                                     "  num x;<caret>\n" +
                                                     "}");
    doTypeAndCheck('\n', "class X {\n" +
                         "  num x;\n" +
                         "  <caret>\n" +
                         "}");
  }

  public void testCaseAlignAfterColon1() throws Throwable {
    myFixture.configureByText(
      DartFileType.INSTANCE,
      "class X {\n" +
      "  void doit(x) {\n" +
      "    switch (x) {\n" +
      "      case 1<caret>\n" +
      "    }\n" +
      "  }\n" +
      "}");
    doTypeAndCheck(
      ':',
      "class X {\n" +
      "  void doit(x) {\n" +
      "    switch (x) {\n" +
      "      case 1:<caret>\n" +
      "    }\n" +
      "  }\n" +
      "}");
  }

  public void testCaseAlignAfterColon2() throws Throwable {
    myFixture.configureByText(
      DartFileType.INSTANCE,
      "class X {\n" +
      "  void doit(x) {\n" +
      "    switch (x) {\n" +
      "      case 1:\n" +
      "    case 2<caret>\n" +
      "    }\n" +
      "  }\n" +
      "}");
    doTypeAndCheck(
      ':',
      "class X {\n" +
      "  void doit(x) {\n" +
      "    switch (x) {\n" +
      "      case 1:\n" +
      "      case 2:<caret>\n" +
      "    }\n" +
      "  }\n" +
      "}");
  }

  public void testDefaultAlignAfterColon() throws Throwable {
    myFixture.configureByText(
      DartFileType.INSTANCE,
      "class X {\n" +
      "  void doit(x) {\n" +
      "    switch (x) {\n" +
      "      case 1:\n" +
      "    default<caret>\n" +
      "    }\n" +
      "  }\n" +
      "}");
    doTypeAndCheck(
      ':',
      "class X {\n" +
      "  void doit(x) {\n" +
      "    switch (x) {\n" +
      "      case 1:\n" +
      "      default:<caret>\n" +
      "    }\n" +
      "  }\n" +
      "}");
  }

  public void testCaseStringAlignAfterColon() throws Throwable {
    myFixture.configureByText(
      DartFileType.INSTANCE,
      "class X {\n" +
      "  void doit(x) {\n" +
      "    switch (x) {\n" +
      "      case 1:\n" +
      "    case '<caret>'\n" +
      "    }\n" +
      "  }\n" +
      "}");
    doTypeAndCheck(
      ':',
      "class X {\n" +
      "  void doit(x) {\n" +
      "    switch (x) {\n" +
      "      case 1:\n" +
      "    case ':<caret>'\n" +
      "    }\n" +
      "  }\n" +
      "}");
  }
}
