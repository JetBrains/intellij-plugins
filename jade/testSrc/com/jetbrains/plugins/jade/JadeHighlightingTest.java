// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.lang.javascript.inspections.JSAnnotatorInspection;
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection;
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssClass;
import com.intellij.psi.css.inspections.CssUnusedSymbolInspection;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.plugins.jade.validation.JadeTabsAndSpacesInspection;

public class JadeHighlightingTest extends BasePlatformTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(
      new JSUnresolvedReferenceInspection(),
      new JSValidateTypesInspection(),
      new JadeTabsAndSpacesInspection(),
      new JSAnnotatorInspection()
    );
  }

  @Override
  protected String getTestDataPath() {
    return JadeTestUtil.getBaseTestDataPath() + "/highlighting/";
  }

  public void _testInterp1() {
    myFixture.configureByFiles(getTestName(true) + ".jade", getTestName(true) + ".js");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testExtends1() {
    myFixture.configureByFiles(getTestName(true) + ".jade");
    myFixture.checkHighlighting(true, false, true);
    myFixture.launchAction(myFixture.findSingleIntention("Create file missing.jade"));
    myFixture.checkHighlighting(true, false, true);
    myFixture.checkResultByFile(getTestName(true) + "_after.jade");
    PsiDirectory dir = myFixture.getFile().getParent();
    assertNotNull(dir.findFile("missing.jade"));
  }

  public void testExtends2() {
    myFixture.configureByFiles(getTestName(true) + ".jade");
    myFixture.checkHighlighting(true, false, true);
    myFixture.launchAction(myFixture.findSingleIntention("Create directory foo"));
    PsiDirectory dir = myFixture.getFile().getParent();
    assertNotNull(dir.findSubdirectory("foo"));
  }

  public void testInclude1() {
    myFixture.configureByFiles(getTestName(true) + ".jade");
    final PsiDirectory dir = myFixture.getFile().getParent();
    PsiDirectory subdir = WriteCommandAction.runWriteCommandAction(null, (Computable<PsiDirectory>)() -> dir.createSubdirectory("subdir"));
    myFixture.checkHighlighting(true, false, true);
    myFixture.launchAction(myFixture.findSingleIntention("Create file abcde.txt"));
    assertNotNull(subdir.findFile("abcde.txt"));
  }

  public void testWrongAttributeValue() {
    myFixture.configureByFile(getTestName(true) + ".jade");
    myFixture.checkHighlighting( true, true, true);
  }

  public void testTabsAndSpaces() {
    myFixture.configureByFile(getTestName(true) + "1.jade");
    myFixture.checkHighlighting(false, false, false);
    myFixture.configureByFile(getTestName(true) + "2.jade");
    myFixture.checkHighlighting(false, false, false);
  }

  public void testCssSelector() {
    myFixture.enableInspections(new CssUnusedSymbolInspection());
    myFixture.configureByFile(getTestName(true) + ".jade");
    final PsiElement resolved =
      TargetElementUtil.findTargetElement(myFixture.getEditor(), TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED);
    assertTrue(resolved instanceof CssClass);
    myFixture.checkHighlighting(true, false, true);
  }

  public void testMultipleClasses() {
    myFixture.enableInspections(new CssUnusedSymbolInspection());
    myFixture.configureByFile(getTestName(true) + ".jade");
    myFixture.checkHighlighting(true, true, true);
  }

  public void testMixinsObjectDestructuring() {
    myFixture.configureByFile(getTestName(true) + ".jade");
    myFixture.checkHighlighting( true, true, true);
  }
}
