package com.jetbrains.lang.dart.editor;

import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;

/// Tests DartEnterInDocLineCommentHandler
public class DartCommentLineBreakingTest extends DartCodeInsightFixtureTestCase {

  public void testCommentLineBreak1() {
    myFixture.configureByText("test.dart", "/// Hello Dart");
    myFixture.getEditor().getCaretModel().moveToOffset("/// Hello".length());
    myFixture.type("\n");
    assertEquals("/// Hello\n/// Dart", myFixture.getEditor().getDocument().getCharsSequence().toString());
  }

}
