package com.jetbrains.lang.dart.completion.editor;

import com.intellij.openapi.actionSystem.IdeActions;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.completion.base.DartCompletionTestBase;

public class DartEditorCompletionTest extends DartCompletionTestBase {
  public DartEditorCompletionTest() {
    super("completion", "editor");
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
}
