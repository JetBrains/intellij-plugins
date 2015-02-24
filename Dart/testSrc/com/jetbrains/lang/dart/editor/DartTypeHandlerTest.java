package com.jetbrains.lang.dart.editor;

import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;

public class DartTypeHandlerTest extends DartCodeInsightFixtureTestCase {

  public void testGT() {
    myFixture.configureByText("test.dart", "List<String>");
    myFixture.getEditor().getCaretModel().moveToOffset("List<String".length());
    myFixture.type(">");
    final String text = myFixture.getEditor().getDocument().getText();
    assertEquals("List<String>", text);
  }
  public void testLT() {
    myFixture.configureByText("test.dart", "List");
    myFixture.getEditor().getCaretModel().moveToOffset("List".length());
    myFixture.type("<");
    final String text = myFixture.getEditor().getDocument().getText();
    assertEquals("List<>", text);
  }
  public void testDollarFollowedWithBrace() {
    myFixture.configureByText("test.dart", "String string = '$'");
    myFixture.getEditor().getCaretModel().moveToOffset("String string = '$".length());
    myFixture.type("{");
    final String text = myFixture.getEditor().getDocument().getText();
    assertEquals("String string = '${}'", text);
  }
}