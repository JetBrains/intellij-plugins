package com.jetbrains.lang.dart.ide.moveCode;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;

public class DartCommentIdentifierTest extends DartCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/componentMover/comments/";
  }

  private void doTest(int nodeCount, int textLength) {
    final String testName = getTestName(false);
    myFixture.configureByFile(testName + ".dart");
    PsiElement element = getElementAtCaret();
    Pair<PsiElement, PsiElement> pair = DartComponentMover.findCommentRange(element);
    assertNotNull("No comment found", pair);
    assertEquals("Wrong node count", nodeCount, countNodes(pair));
    assertEquals("Wrong text", textLength, makeString(pair).length());
  }

  private PsiElement getElementAtCaret() {
    int offset = myFixture.getCaretOffset();
    if (offset < 0) fail("No <caret> found");
    return myFixture.getFile().findElementAt(offset);
  }

  private static int countNodes(Pair<PsiElement, PsiElement> pair) {
    PsiElement first = pair.first;
    PsiElement last = pair.second;
    int count = 1;
    while (first != last) {
      if (first == null) return 0;
      count += 1;
      first = first.getNextSibling();
    }
    return count;
  }

  private static String makeString(Pair<PsiElement, PsiElement> pair) {
    StringBuilder buffer = new StringBuilder();
    PsiElement first = pair.first;
    PsiElement last = pair.second;
    buffer.append(first.getText());
    while (first != last) {
      first = first.getNextSibling();
      if (first == null) break;
      buffer.append(first.getText());
    }
    return buffer.toString();
  }

  public void testEmptyDocBlock() {
    doTest(1, 5);
  }

  public void testEmptyBlock() {
    doTest(1, 4);
  }

  public void testWithinDocBlock() {
    doTest(1, 44);
  }

  public void testWithinBlock() {
    doTest(1, 37);
  }

  public void testThreeSingleLine() {
    doTest(5, 8);
  }

  public void testThreeSingleLineDoc() {
    doTest(5, 11);
  }

  public void testMixedLines1() {
    doTest(3, 14);
  }

  public void testMixedLines2() {
    // At end of sequential line doc comments.
    doTest(3, 26);
  }

  public void testSplitSingleLines1() {
    doTest(5, 28);
  }

  public void testSplitSingleLines2() {
    // At beginning of sequential line comments.
    doTest(5, 28);
  }

  public void testSplitSingleLines3() {
    doTest(1, 2); // No comment, just two newlines.
  }

  public void testVarSingleLine1() {
    doTest(7, 19);
  }

  public void testVarSingleLine2() {
    doTest(7, 19);
  }

  public void testVarSingleLine3() {
    doTest(7, 19);
  }

  public void testVarSingleLine4() {
    doTest(1, 2);
  }

  public void testLineDocVar1() {
    doTest(1, 5);
  }

  public void testLineDocVar2() {
    doTest(1, 5);
  }

  public void testClassFirst() {
    doTest(1, 3);
  }

  public void testClassLast() {
    doTest(1, 3);
  }

  public void testClassMixed() {
    doTest(5, 15);
  }
}
