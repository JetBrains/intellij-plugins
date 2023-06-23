// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.frameworks.jboss.drools.highlighting;

import com.intellij.frameworks.jboss.drools.DroolsLightTestCase;

public class DroolsHighlightingTest extends DroolsLightTestCase {
  @Override
  protected String getTestDirectory() {
    return "highlighting";
  }

  public void testImportedClasses() {
    myFixture.copyFileToProject("examples/fibonacci/FibonacciExample.java");
    myFixture.testHighlighting(false, false, false, "importedClassHighlighting.drl");
  }

  public void testImportedAsteriskClasses() {
    myFixture.copyFileToProject("examples/com/acme/objects/Fact.java");
    myFixture.testHighlighting(false, false, false, "importedAsterixClassHighlighting.drl");
  }

  public void testLhsQueryExpressionProcessing() {
    myFixture.copyFileToProject("examples/fibonacci/FibonacciExample.java");
    myFixture.testHighlighting(false, false, false, "queryHighlighting.drl");
  }

  public void testFunctionResolveInEval() {
    myFixture.testHighlighting(false, false, false, "functionsInEval.drl");
  }

  public void testGlobalVarsInWhen() {
    myFixture.copyFileToProject("examples/fibonacci/FibonacciExample.java");
    myFixture.testHighlighting(false, false, false, "globalVarsHighlighting.drl");
  }

  public void testDeclaredTypesHighlighting() {
    myFixture.testHighlighting(false, false, false, "declaredTypesHighlighting.drl");
  }

  public void testDeclaredTypesRename() {
    myFixture.testRename("declaredTypes_before.drl", "declaredTypes_after.drl", "Student_new");
  }

 public void testDeclaredTypesRename2() {
    myFixture.testRename("declaredTypes_before2.drl", "declaredTypes_after2.drl", "Person_new");
  }

  public void testDeclaredFieldsRename() {
    myFixture.testRename("declaredFieldsRename_before.drl", "declaredFieldsRename_after.drl", "name_new");
  }

  public void testNotStatement() {
    myFixture.testHighlighting(false, false, false, "notStatementHighlighting.drl");
  }

  public void testMvelJavaStatementsResolve() {
    myFixture.copyFileToProject("examples/resolve/Foo.java");
    myFixture.testHighlighting(false, false, false, "MvelJavaResolve.drl");
  }

  public void testOOPaths() {
    myFixture.addClass("package org.drools.ruleunits.api;\n" +
                       "public interface DataStore<T> extends DataSource<T> {}");
    myFixture.copyFileToProject("examples/kogito/LoanUnit.java");
    myFixture.copyFileToProject("examples/kogito/Applicant.java");
    myFixture.copyFileToProject("examples/kogito/LoanApplication.java");

    myFixture.testHighlighting(false, false, false, "RuleUnitQuery.drl");
  }

}