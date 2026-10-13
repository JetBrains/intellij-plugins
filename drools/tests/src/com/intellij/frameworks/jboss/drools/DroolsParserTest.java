// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.frameworks.jboss.drools;

import com.intellij.lang.java.JavaParserDefinition;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixture;
import com.intellij.plugins.drools.lang.parser.DroolsParserDefinition;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixtureKt.parsingFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;

@TestApplication
public class DroolsParserTest {
  private static final boolean CHECK_RESULT = true;

  private static final TestFixture<Project> projectFixture = projectFixture();

  private final TestFixture<ParsingTestFixture> parsingFixture = parsingFixture(
    projectFixture,
    PathManager.getHomePath() + "/contrib/drools/tests/testData/parser",
    "drl",
    false,
    new DroolsParserDefinition(), new JavaParserDefinition());

  @BeforeEach
  public void setUp() {
    parsingFixture.get().setCheckAllPsiRoots(false);
  }

  @Test
  public void testPackageDefinition() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testChunkBlockRule() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testImportDefinition() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testRuleAttribute() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testRuleAttribute2() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testSimpleRule() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testSimpleRule2() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testFibonacci() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testSudoku() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testOverAndWindowRules() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testQueryStatement() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testJavaStatement1() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testJavaStatement2() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testNotStatement() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testDeprecatedCommenter() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testRhsNamedConsequence() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testIsAOperator() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }
  @Test
  public void testDoubleQuotes() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testSingleQuotes() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }


  @Test
  public void testWindowDeclaration() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testUnitDeclaration() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testNullChecks() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testEnumDeclarations() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testGlobalImport() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  // IDEA-319541
  @Test
  public void testOOPath() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testOOPathAccumulate() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testNotOOPath() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }

  @Test
  public void testImportedFunctions() {
    parsingFixture.get().doTest(CHECK_RESULT);
  }
}
