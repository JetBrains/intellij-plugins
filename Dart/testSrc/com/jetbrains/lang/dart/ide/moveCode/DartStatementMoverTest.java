package com.jetbrains.lang.dart.ide.moveCode;

public class DartStatementMoverTest extends DartCodeMoverTest {

  protected String getBasePath() {
    return "/statementMover/";
  }

  public void testIfVarWhile() {
    doTest(); // Move IF above VAR and below DO
  }

  public void testVarIfDo() {
    doTest(); // Move VAR into IF and below DO
  }

  public void testDoIfBody() {
    doTest(); // Move DO above IF but not out of class body
  }

  public void testVarIfCall() {
    doTest(); // Move VAR above CALL and into IF
  }

  public void testNestedIf1() {
    doTest(); // Move markForCheck() up one and down one
  }

  public void testNestedIf2() {
    doTest(); // Move selectedItem up one and below nested IF
  }

  public void testNestedIf3() {
    doTest(); // Move detectChanges() up above nexted IF and down one
  }

  public void testMinimalMain() {
    doTest(); // Ensure nothing happens even when destLine == 0
  }
}
