package com.jetbrains.lang.dart.resolve;

import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;

import java.io.IOException;
import java.util.Collection;

abstract public class DartGotoDeclarationActionTestBase extends DartCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/goto/");
  }

  protected void doTest(int expectedSize) throws IOException {
    doTest(expectedSize, getTestName(false) + ".dart");
  }

  protected void doTest(int expectedSize, String... files) throws IOException {
    doTest(myFixture.configureByFiles(files), expectedSize);
  }

  protected void doTest(PsiFile[] files, int expectedSize) {
    doTest(files[0], expectedSize);
  }

  protected void doTest(PsiFile myFile, int expectedSize) {
    final Collection<PsiElement> elements =
      TargetElementUtilBase.getInstance().getTargetCandidates(myFile.findReferenceAt(myFixture.getCaretOffset()));
    assertNotNull(elements);
    assertEquals(expectedSize, elements.size());
  }
}
