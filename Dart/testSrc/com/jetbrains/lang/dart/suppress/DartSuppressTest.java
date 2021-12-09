// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.suppress;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.annotator.DartProblemGroup;

public class DartSuppressTest extends DartCodeInsightFixtureTestCase {

  private boolean isSuppressActionAvailable(final boolean eolComment) {
    return new DartProblemGroup.DartSuppressAction("", eolComment)
      .isAvailable(getProject(), myFixture.getEditor(), myFixture.getElementAtCaret());
  }

  public void testActionAvailability() {
    myFixture.configureByText("foo.dart", "var <caret>a;");
    assertTrue(isSuppressActionAvailable(false));
    assertTrue(isSuppressActionAvailable(true));

    myFixture.configureByText("foo.dart", "//ignore something\n var <caret>a; // ignore :something");
    assertTrue(isSuppressActionAvailable(false));
    assertTrue(isSuppressActionAvailable(true));

    myFixture.configureByText("foo.dart", "//ignore: something\nvar <caret>a;");
    assertTrue(isSuppressActionAvailable(false));
    assertFalse(isSuppressActionAvailable(true));

    myFixture.configureByText("foo.dart", "     //      ignore: something // else\nvar <caret>a;");
    assertTrue(isSuppressActionAvailable(false));
    assertFalse(isSuppressActionAvailable(true));

    myFixture.configureByText("foo.dart", "var <caret>a; // ignore: something");
    assertFalse(isSuppressActionAvailable(false));
    assertTrue(isSuppressActionAvailable(true));

    myFixture.configureByText("foo.dart", "var <caret>a;    //    ignore: something // else");
    assertFalse(isSuppressActionAvailable(false));
    assertTrue(isSuppressActionAvailable(true));

    myFixture.configureByText("foo.dart", "// ignore: something\n var <caret>a; // ignore: something");
    assertTrue(isSuppressActionAvailable(false));
    assertTrue(isSuppressActionAvailable(true));
  }

  public void testAddPrevLineComment() {
    myFixture.configureByText("foo.dart", "var <caret>a;");
    CommandProcessor.getInstance().executeCommand(getProject(), () -> ApplicationManager.getApplication().runWriteAction(
      () -> new DartProblemGroup.DartSuppressAction("x", false)
        .invoke(getProject(), myFixture.getEditor(), myFixture.getElementAtCaret())), null, null);

    myFixture.checkResult("// ignore: x\nvar <caret>a;");
  }

  public void testAddPrevLineCommentWithOffset() {
    myFixture.configureByText("foo.dart", "//a\n   var <caret>a;");
    CommandProcessor.getInstance().executeCommand(getProject(), () -> ApplicationManager.getApplication().runWriteAction(
      () -> new DartProblemGroup.DartSuppressAction("x", false)
        .invoke(getProject(), myFixture.getEditor(), myFixture.getElementAtCaret())), null, null);

    myFixture.checkResult("//a\n   // ignore: x\n   var <caret>a;");
  }

  public void testUpdatePrevLineComment() {
    myFixture.configureByText("foo.dart", "     //     ignore:     y     \n   var <caret>a;");
    CommandProcessor.getInstance().executeCommand(getProject(), () -> ApplicationManager.getApplication().runWriteAction(
      () -> new DartProblemGroup.DartSuppressAction("x", false)
        .invoke(getProject(), myFixture.getEditor(), myFixture.getElementAtCaret())), null, null);

    myFixture.checkResult("     //     ignore:     y, x\n   var <caret>a;");
  }

  public void testAddEolComment() {
    myFixture.configureByText("foo.dart", "var <caret>a;");
    CommandProcessor.getInstance().executeCommand(getProject(), () -> ApplicationManager.getApplication().runWriteAction(
      () -> new DartProblemGroup.DartSuppressAction("x", true)
        .invoke(getProject(), myFixture.getEditor(), myFixture.getElementAtCaret())), null, null);

    myFixture.checkResult("var <caret>a; // ignore: x");
  }

  public void testAddEolCommentTrimSpaces() {
    myFixture.configureByText("foo.dart", "var <caret>a;     ");
    CommandProcessor.getInstance().executeCommand(getProject(), () -> ApplicationManager.getApplication().runWriteAction(
      () -> new DartProblemGroup.DartSuppressAction("x", true)
        .invoke(getProject(), myFixture.getEditor(), myFixture.getElementAtCaret())), null, null);

    myFixture.checkResult("var <caret>a; // ignore: x");
  }

  public void testAddEolCommentBeforeOtherComment() {
    myFixture.configureByText("foo.dart", "var <caret>a; //comment");
    CommandProcessor.getInstance().executeCommand(getProject(), () -> ApplicationManager.getApplication().runWriteAction(
      () -> new DartProblemGroup.DartSuppressAction("x", true)
        .invoke(getProject(), myFixture.getEditor(), myFixture.getElementAtCaret())), null, null);

    myFixture.checkResult("var <caret>a; // ignore: x, //comment");
  }

  public void testUpdateEolComment() {
    myFixture.configureByText("foo.dart", "var <caret>a;     //     ignore:     y     \n");
    CommandProcessor.getInstance().executeCommand(getProject(), () -> ApplicationManager.getApplication().runWriteAction(
      () -> new DartProblemGroup.DartSuppressAction("x", true)
        .invoke(getProject(), myFixture.getEditor(), myFixture.getElementAtCaret())), null, null);

    myFixture.checkResult("var <caret>a;     //     ignore:     y, x\n");
  }
}
