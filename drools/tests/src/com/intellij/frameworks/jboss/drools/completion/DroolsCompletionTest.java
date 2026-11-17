// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.frameworks.jboss.drools.completion;

import com.intellij.testFramework.fixtures.LightJavaCodeInsightTestFixture;
import com.intellij.testFramework.fixtures.LightJavaProject;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.testFramework.fixtures.LightJavaFixturesKt.lightJavaCodeInsightFixture;
import static com.intellij.testFramework.fixtures.LightJavaFixturesKt.lightJavaProjectFixture;
import static org.assertj.core.api.Assertions.assertThat;

@TestApplication
public class DroolsCompletionTest {
  private static final TestFixture<LightJavaProject> projectFixture = lightJavaProjectFixture();
  private final TestFixture<LightJavaCodeInsightTestFixture> codeInsightFixture =
    lightJavaCodeInsightFixture(projectFixture, "/contrib/drools/tests/testData/completion");

  @BeforeEach
  void setUp() {
    codeInsightFixture.get().allowTreeAccessForAllFiles();
  }

  @Test
  public void testTopLevelAttributesCompletion() {
    assertCompletionContains("topLevelAttributesCompletion.drl", "activation-group \"\"" , "agenda-group \"\"" , "auto-focus" , "date-effective \"\"" , "date-expires \"\"" , "dialect \"\"" , "duration" , "enabled" , "lock-on-active" , "no-loop",  "ruleflow-group \"\"" , "salience" , "timer");
  }

  @Test
  public void testModifyCompletion() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.testCompletion("modifyCompletion.drl", "modifyCompletion_after.drl");
  }

  @Test
  public void testInsertCompletion() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.testCompletionVariants("insertCompletion.drl", "insert", "insertLogical");
  }

  @Test
  public void testUpdateCompletion() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.testCompletion("updateCompletion.drl", "updateCompletion_after.drl");
  }

  @Test
  public void testRetractCompletion() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.testCompletion("retractCompletion.drl", "retractCompletion_after.drl");
  }

  @Test
  public void testEmptyThenStatementCompletion() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.copyFileToProject("examples/fibonacci/FibonacciExample.java");
    assertCompletionContains("emptyThenStatementCompletion.drl", "insert", "modify", "update", "insertLogical", "retract", "System",
                             "String", "Fibonacci");
  }

  @Test
  public void testJavaStatementInsideThenStatementCompletion() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.copyFileToProject("examples/fibonacci/FibonacciExample.java");
    assertCompletionContains("javaStatementInsideThenStatementCompletion.drl", "insert", "modify", "update", "insertLogical", "retract",
                             "System",
                             "String", "Fibonacci");
  }

  @Test
  public void testJavaStatementInsideThenStatementCompletion2() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.copyFileToProject("/examples/fibonacci/FibonacciExample.java");
    assertThat(myFixture.getCompletionVariants("javaStatementInsideThenStatementCompletion2.drl")).doesNotContain("insert", "modify", "update",
                        "insertLogical", "retract", "System",
                        "String", "Fibonacci");
  }

  @Test
  public void testModifyPairStatementCompletion() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.copyFileToProject("/examples/fibonacci/FibonacciExample.java");
    assertCompletionContains("modifyPairStatementCompletion.drl", "f1", "s1");
  }

  @Test
  public void testModifyPairStatementCompletion2() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.copyFileToProject("/examples/fibonacci/FibonacciExample.java");
    assertThat(myFixture.getCompletionVariants("modifyPairStatementCompletion.drl")).doesNotContain("insert", "modify", "update",
                        "insertLogical", "retract", "System",
                        "String", "Fibonacci");
  }

  @Test
  public void testModifyConstraintsStatementCompletion() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.copyFileToProject("/examples/fibonacci/FibonacciExample.java");
    assertCompletionContains("modifyConstraintsCompletion.drl", "setValue");
  }

  @Test
  public void testModifyConstraintsStatementCompletion2() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.copyFileToProject("/examples/fibonacci/FibonacciExample.java");
    myFixture.testCompletion("modifyConstraintsCompletion_before.drl", "modifyConstraintsCompletion_after.drl");
  }

  private void assertCompletionContains(String fileName, String... variants) {
    assertThat(codeInsightFixture.get().getCompletionVariants(fileName)).contains(variants);
  }
}
