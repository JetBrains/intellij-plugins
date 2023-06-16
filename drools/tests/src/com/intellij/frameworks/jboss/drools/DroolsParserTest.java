// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.frameworks.jboss.drools;

import com.intellij.lang.LanguageASTFactory;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.java.JavaParserDefinition;
import com.intellij.openapi.application.PathManager;
import com.intellij.plugins.drools.lang.parser.DroolsParserDefinition;
import com.intellij.psi.impl.source.tree.JavaASTFactory;
import com.intellij.testFramework.ParsingTestCase;

public class DroolsParserTest extends ParsingTestCase {
  private static final boolean CHECK_RESULT = true;

  public DroolsParserTest() {
    super("parser", "drl", new DroolsParserDefinition(), new JavaParserDefinition());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    addExplicitExtension(LanguageASTFactory.INSTANCE, JavaLanguage.INSTANCE, new JavaASTFactory());
  }

  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + "/contrib/drools/tests/testData/";
  }

  public void testPackageDefinition() {
    doTest(CHECK_RESULT);
  }

  public void testChunkBlockRule() {
    doTest(CHECK_RESULT);
  }

  public void testImportDefinition() {
    doTest(CHECK_RESULT);
  }

  public void testRuleAttribute() {
    doTest(CHECK_RESULT);
  }

  public void testRuleAttribute2() {
    doTest(CHECK_RESULT);
  }

  public void testSimpleRule() {
    doTest(CHECK_RESULT);
  }

  public void testSimpleRule2() {
    doTest(CHECK_RESULT);
  }

  public void testFibonacci() {
    doTest(CHECK_RESULT);
  }

  public void testSudoku() {
    doTest(CHECK_RESULT);
  }

  public void testOverAndWindowRules() {
    doTest(CHECK_RESULT);
  }

  public void testQueryStatement() {
    doTest(CHECK_RESULT);
  }

  public void testJavaStatement1() {
    doTest(CHECK_RESULT);
  }

  public void testJavaStatement2() {
    doTest(CHECK_RESULT);
  }

  public void testNotStatement() {
    doTest(CHECK_RESULT);
  }

  public void testDeprecatedCommenter() {
    doTest(CHECK_RESULT);
  }

  public void testRhsNamedConsequence() {
    doTest(CHECK_RESULT);
  }

  public void testIsAOperator() {
    doTest(CHECK_RESULT);
  }
  public void testDoubleQuotes() {
    doTest(CHECK_RESULT);
  }

  public void testSingleQuotes() {
    doTest(CHECK_RESULT);
  }


  public void testWindowDeclaration() {
    doTest(CHECK_RESULT);
  }

  public void testUnitDeclaration() {
    doTest(CHECK_RESULT);
  }

  public void testNullChecks() {
    doTest(CHECK_RESULT);
  }

  public void testEnumDeclarations() {
    doTest(CHECK_RESULT);
  }

  public void testGlobalImport() {
    doTest(CHECK_RESULT);
  }

  // IDEA-319541
  public void testOOPath() {
    doTest(CHECK_RESULT);
  }

  public void testOOPathAccumulate() {
    doTest(CHECK_RESULT);
  }

  public void testNotOOPath() {
    doTest(CHECK_RESULT);
  }

  public void testImportedFunctions() {
    doTest(CHECK_RESULT);
  }

  @Override
  protected boolean checkAllPsiRoots() {
    return false;
  }
}
