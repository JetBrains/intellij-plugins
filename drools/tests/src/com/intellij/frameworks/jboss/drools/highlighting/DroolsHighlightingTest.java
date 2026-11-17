// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.frameworks.jboss.drools.highlighting;

import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightTestFixture;
import com.intellij.testFramework.fixtures.LightJavaProject;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.testFramework.fixtures.LightJavaFixturesKt.lightJavaCodeInsightFixture;
import static com.intellij.testFramework.fixtures.LightJavaFixturesKt.lightJavaProjectFixture;

@TestApplication
public class DroolsHighlightingTest {
  private static final TestFixture<LightJavaProject> projectFixture = lightJavaProjectFixture();
  private final TestFixture<LightJavaCodeInsightTestFixture> codeInsightFixture =
    lightJavaCodeInsightFixture(projectFixture, "/contrib/drools/tests/testData/highlighting");

  @BeforeEach
  void setUp() {
    codeInsightFixture.get().allowTreeAccessForAllFiles();
  }

  @Test
  public void testImportedClasses() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.copyFileToProject("examples/fibonacci/FibonacciExample.java");
    myFixture.testHighlighting(false, false, false, "importedClassHighlighting.drl");
  }

  @Test
  public void testImportedAsteriskClasses() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.copyFileToProject("examples/com/acme/objects/Fact.java");
    myFixture.testHighlighting(false, false, false, "importedAsterixClassHighlighting.drl");
  }

  @Test
  public void testLhsQueryExpressionProcessing() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.copyFileToProject("examples/fibonacci/FibonacciExample.java");
    myFixture.testHighlighting(false, false, false, "queryHighlighting.drl");
  }

  @Test
  public void testFunctionResolveInEval() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.testHighlighting(false, false, false, "functionsInEval.drl");
  }

  @Test
  public void testGlobalVarsInWhen() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.copyFileToProject("examples/fibonacci/FibonacciExample.java");
    myFixture.testHighlighting(false, false, false, "globalVarsHighlighting.drl");
  }

  @Test
  public void testDeclaredTypesHighlighting() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.testHighlighting(false, false, false, "declaredTypesHighlighting.drl");
  }

  @Test
  public void testDeclaredTypesRename() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testRename("declaredTypes_before.drl", "declaredTypes_after.drl", "Student_new"));
  }

  @Test
  public void testDeclaredTypesRename2() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testRename("declaredTypes_before2.drl", "declaredTypes_after2.drl", "Person_new"));
  }

  @Test
  public void testDeclaredFieldsRename() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testRename("declaredFieldsRename_before.drl", "declaredFieldsRename_after.drl", "name_new"));
  }

  @Test
  public void testNotStatement() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.testHighlighting(false, false, false, "notStatementHighlighting.drl");
  }

  @Test
  public void testMvelJavaStatementsResolve() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.copyFileToProject("examples/resolve/Foo.java");
    myFixture.testHighlighting(false, false, false, "MvelJavaResolve.drl");
  }

  @Test
  public void testOOPaths() {
    LightJavaCodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.addClass("package org.drools.ruleunits.api;\n" +
                       "public interface DataStore<T> extends DataSource<T> {}");
    myFixture.copyFileToProject("examples/kogito/LoanUnit.java");
    myFixture.copyFileToProject("examples/kogito/Applicant.java");
    myFixture.copyFileToProject("examples/kogito/LoanApplication.java");

    myFixture.testHighlighting(false, false, false, "RuleUnitQuery.drl");
  }

}