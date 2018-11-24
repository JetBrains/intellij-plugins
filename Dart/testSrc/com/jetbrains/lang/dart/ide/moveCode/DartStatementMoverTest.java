package com.jetbrains.lang.dart.ide.moveCode;

public class DartStatementMoverTest extends DartCodeMoverTest {

  protected String getBasePath() {
    return "/statementMover/";
  }

  public void testIfVarWhile() {
    doTest(); // Move IF above VAR and below DO
  }

  public void testVarIfDo() {
    doTest(); // Move VAR above IF and below DO
  }

  public void testDoIfBody() {
    doTest(); // Move DO above IF but not out of class body
  }

  public void testVarIfCall() {
    doTest(); // Move VAR above CALL and below IF
  }
}
