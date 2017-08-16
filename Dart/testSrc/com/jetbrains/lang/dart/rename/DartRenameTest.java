package com.jetbrains.lang.dart.rename;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;

public class DartRenameTest extends DartCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/rename/");
  }

  public void testReferencedHtmlInPackage() {
    myFixture.addFileToProject("pubspec.yaml", "name: ThisProject\n");
    myFixture.configureByFile(getTestName(false) + ".html");
    final PsiFile htmlFile = myFixture.addFileToProject("lib/sub/foo.html", "");
    myFixture.renameElement(htmlFile, "bar.html", true, true);
    myFixture.checkResultByFile(getTestName(false) + "After.html");
  }
}
