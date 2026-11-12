// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.lang.javascript.inspections.JSAnnotatorInspection;
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection;
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssClass;
import com.intellij.psi.css.inspections.CssUnusedSymbolInspection;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.jetbrains.plugins.jade.validation.JadeTabsAndSpacesInspection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
public class JadeHighlightingTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private String myTestName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myTestName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), true);
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.setTestDataPath(JadeTestUtil.getBaseTestDataPath() + "/highlighting/");
    myFixture.enableInspections(
      new JSUnresolvedReferenceInspection(),
      new JSValidateTypesInspection(),
      new JadeTabsAndSpacesInspection(),
      new JSAnnotatorInspection()
    );
  }

  public void _testInterp1() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFiles(myTestName + ".jade", myTestName + ".js");
    myFixture.checkHighlighting(true, false, true);
  }

  @Test
  public void testExtends1() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFiles(myTestName + ".jade");
    myFixture.checkHighlighting(true, false, true);
    EdtTestUtil.runInEdtAndWait(() -> myFixture.launchAction(myFixture.findSingleIntention("Create file missing.jade")));
    myFixture.checkHighlighting(true, false, true);
    myFixture.checkResultByFile(myTestName + "_after.jade");
    PsiDirectory dir = ReadAction.computeBlocking(() -> myFixture.getFile().getParent());
    assertNotNull(ReadAction.computeBlocking(() -> dir.findFile("missing.jade")));
  }

  @Test
  public void testExtends2() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFiles(myTestName + ".jade");
    myFixture.checkHighlighting(true, false, true);
    EdtTestUtil.runInEdtAndWait(() -> myFixture.launchAction(myFixture.findSingleIntention("Create directory foo")));
    PsiDirectory dir = ReadAction.computeBlocking(() -> myFixture.getFile().getParent());
    assertNotNull(ReadAction.computeBlocking(() -> dir.findSubdirectory("foo")));
  }

  @Test
  public void testInclude1() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFiles(myTestName + ".jade");
    final PsiDirectory dir = ReadAction.computeBlocking(() -> myFixture.getFile().getParent());
    PsiDirectory subdir = WriteCommandAction.runWriteCommandAction(null, (Computable<PsiDirectory>)() -> dir.createSubdirectory("subdir"));
    myFixture.checkHighlighting(true, false, true);
    EdtTestUtil.runInEdtAndWait(() -> myFixture.launchAction(myFixture.findSingleIntention("Create file abcde.txt")));
    assertNotNull(ReadAction.computeBlocking(() -> subdir.findFile("abcde.txt")));
  }

  @Test
  public void testWrongAttributeValue() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFile(myTestName + ".jade");
    myFixture.checkHighlighting( true, true, true);
  }

  @Test
  public void testTabsAndSpaces() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFile(myTestName + "1.jade");
    myFixture.checkHighlighting(false, false, false);
    myFixture.configureByFile(myTestName + "2.jade");
    myFixture.checkHighlighting(false, false, false);
  }

  @Test
  public void testCssSelector() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.enableInspections(new CssUnusedSymbolInspection());
    myFixture.configureByFile(myTestName + ".jade");
    final PsiElement resolved = ReadAction.computeBlocking(
      () -> TargetElementUtil.findTargetElement(myFixture.getEditor(), TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED));
    assertTrue(resolved instanceof CssClass);
    myFixture.checkHighlighting(true, false, true);
  }

  @Test
  public void testMultipleClasses() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.enableInspections(new CssUnusedSymbolInspection());
    myFixture.configureByFile(myTestName + ".jade");
    myFixture.checkHighlighting(true, true, true);
  }

  @Test
  public void testMixinsObjectDestructuring() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFile(myTestName + ".jade");
    myFixture.checkHighlighting( true, true, true);
  }
}
