package com.jetbrains.lang.dart.ide.moveCode;

public class DartComponentMoverTest extends DartCodeMoverTest {

  protected String getBasePath() {
    return "/componentMover/";
  }

  public void testClassClass() {
    doTest(); // swap two classes in the file
  }

  public void testClassImport() {
    doTest(); // up: no change -- class does not move above import
  }

  public void testCommentImport() {
    doTest(); // multi-line comment, multi-line import, single-line import: all move correctly
  }

  public void testImportClass() {
    doTest(); // down: no change -- import does not move below class
  }

  public void testTypedefImport() {
    doTest(); // down: up change
  }

  public void testFunctionTypedef() {
    doTest();
  }

  public void testTopLevelVar() {
    doTest();
  }

  public void testBlockComment1() {
    doTest();
  }

  public void testBlockComment2() {
    doTest();
  }

  public void testBlockComment3() {
    doTest();
  }

  public void testBlockDocComment1() {
    doTest();
  }

  public void testBlockDocComment2() {
    doTest();
  }

  public void testBlockDocComment3() {
    doTest();
  }

  public void testVarComment() {
    // TODO: Fix "afterDown" case. The final block comment is a child of the class body, not the class members.
    doTest();
  }
}
