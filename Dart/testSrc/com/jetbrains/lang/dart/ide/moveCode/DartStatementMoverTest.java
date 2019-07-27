// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.moveCode;

public class DartStatementMoverTest extends DartCodeMoverTest {

  @Override
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

  // TODO fix WEB-37790
  public void _testListExpr1() {
    doTest();
  }

  public void _testListExpr2() {
    doTest();
  }

  public void _testListExpr3() {
    doTest();
  }

  public void _testListExpr4() {
    doTest();
  }

  public void _testListExpr5() {
    doTest();
  }

  public void testListExpr6() {
    doTest(); // Do nothing when no trailing comma
  }

  public void _testListExpr7() {
    doTest(); // Do nothing when no trailing comma
  }

  public void testNamedParam1() {
    doTest();
  }

  public void testNamedParam2() {
    doTest();
  }

  public void testNamedParam3() {
  }

  public void testIntoFor1() {
    doTest();
  }

  public void testIntoFor2() {
    doTest();
  }
}
