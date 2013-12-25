package com.jetbrains.lang.dart.highlighting;

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection;
import com.intellij.spellchecker.inspections.SpellCheckingInspection;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;

public class DartHighlightingTest extends DartCodeInsightFixtureTestCase {
  protected String getBasePath() {
    return "/highlighting";
  }

  protected boolean isWriteActionRequired() {
    return false;
  }

  public void testScriptSrcPathToPackagesFolder() {
    final String testName = getTestName(false);
    myFixture.enableInspections(HtmlUnknownTargetInspection.class);
    myFixture.addFileToProject("packages/browser/dart.js", "");
    myFixture.addFileToProject("pubspec.yaml", "");
    myFixture.configureByFile(testName + "/" + testName + ".html");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testSpelling() {
    myFixture.enableInspections(SpellCheckingInspection.class);
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testEscapeSequences() {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.checkHighlighting(true, true, true);
  }
}
