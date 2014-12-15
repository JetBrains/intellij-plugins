package com.jetbrains.lang.dart.editor;

import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;

/// Tests DartEnterInDocLineCommentHandler
public class DartCommentLineBreakingTest extends DartCodeInsightFixtureTestCase {

  public void testCommentLineBreak1() {
    myFixture.configureByText("test.dart", "  /// HelloDart \n//");
    myFixture.getEditor().getCaretModel().moveToOffset("  /// Hello".length());
    myFixture.type("\n");
    final String text = myFixture.getEditor().getDocument().getText();
    assertEquals("  /// Hello\n  /// Dart \n//", text);
    myFixture.getEditor().getCaretModel().moveToOffset("  /// Hello\n  /// Dart".length());
    myFixture.type("\n");
    assertEquals("  /// Hello\n  /// Dart\n  \n//", myFixture.getEditor().getDocument().getText());
  }

}
