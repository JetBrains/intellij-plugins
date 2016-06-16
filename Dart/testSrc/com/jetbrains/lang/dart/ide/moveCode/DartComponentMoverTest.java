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
}
