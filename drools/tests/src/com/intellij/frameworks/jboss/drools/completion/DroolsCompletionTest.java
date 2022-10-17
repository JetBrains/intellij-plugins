// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.frameworks.jboss.drools.completion;

import com.intellij.frameworks.jboss.drools.DroolsLightTestCase;

public class DroolsCompletionTest extends DroolsLightTestCase {

  @Override
  protected String getTestDirectory() {
    return "completion";
  }

  public void testTopLevelAttributesCompletion() {
    assertCompletionContains("topLevelAttributesCompletion.drl", "activation-group \"\"" , "agenda-group \"\"" , "auto-focus" , "date-effective \"\"" , "date-expires \"\"" , "dialect \"\"" , "duration" , "enabled" , "lock-on-active" , "no-loop",  "ruleflow-group \"\"" , "salience" , "timer");
  }

  public void testModifyCompletion() {
    myFixture.testCompletion("modifyCompletion.drl", "modifyCompletion_after.drl");
  }

  public void testInsertCompletion() {
    myFixture.testCompletionVariants("insertCompletion.drl", "insert", "insertLogical");
  }

  public void testUpdateCompletion() {
    myFixture.testCompletion("updateCompletion.drl", "updateCompletion_after.drl");
  }

  public void testRetractCompletion() {
    myFixture.testCompletion("retractCompletion.drl", "retractCompletion_after.drl");
  }

  public void testEmptyThenStatementCompletion() {
    myFixture.copyFileToProject("examples/fibonacci/FibonacciExample.java");
    assertCompletionContains("emptyThenStatementCompletion.drl", "insert", "modify", "update", "insertLogical", "retract", "System",
                             "String", "Fibonacci");
  }

  public void testJavaStatementInsideThenStatementCompletion() {
    myFixture.copyFileToProject("examples/fibonacci/FibonacciExample.java");
    assertCompletionContains("javaStatementInsideThenStatementCompletion.drl", "insert", "modify", "update", "insertLogical", "retract",
                             "System",
                             "String", "Fibonacci");
  }

  public void testJavaStatementInsideThenStatementCompletion2() {
    myFixture.copyFileToProject("/examples/fibonacci/FibonacciExample.java");
    assertDoesntContain(myFixture.getCompletionVariants("javaStatementInsideThenStatementCompletion2.drl"), "insert", "modify", "update",
                        "insertLogical", "retract", "System",
                        "String", "Fibonacci");
  }

  public void testModifyPairStatementCompletion() {
    myFixture.copyFileToProject("/examples/fibonacci/FibonacciExample.java");
    assertCompletionContains("modifyPairStatementCompletion.drl", "f1", "s1");
  }

  public void testModifyPairStatementCompletion2() {
    myFixture.copyFileToProject("/examples/fibonacci/FibonacciExample.java");
    assertDoesntContain(myFixture.getCompletionVariants("modifyPairStatementCompletion.drl"), "insert", "modify", "update",
                        "insertLogical", "retract", "System",
                        "String", "Fibonacci");
  }

  public void testModifyConstraintsStatementCompletion() {
    myFixture.copyFileToProject("/examples/fibonacci/FibonacciExample.java");
    assertCompletionContains("modifyConstraintsCompletion.drl", "setValue");
  }

  public void testModifyConstraintsStatementCompletion2() {
    myFixture.copyFileToProject("/examples/fibonacci/FibonacciExample.java");
    myFixture.testCompletion("modifyConstraintsCompletion_before.drl", "modifyConstraintsCompletion_after.drl");
  }

  public void assertCompletionContains(String fileName, String... variants) {
    assertContainsElements(myFixture.getCompletionVariants(fileName), variants);
  }
}
