package com.jetbrains.lang.dart.completion.editor;

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

  public void testDocComment() throws Throwable {
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
}
