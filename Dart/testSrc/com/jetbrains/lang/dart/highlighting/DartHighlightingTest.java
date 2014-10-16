package com.jetbrains.lang.dart.highlighting;

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection;
import com.intellij.spellchecker.inspections.SpellCheckingInspection;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.inspections.DartDeprecatedApiUsageInspection;

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

    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n" +
                                               "dependencies:\n" +
                                               "  PathPackage:\n" +
                                               "    path: local_package\n");
    myFixture.addFileToProject("lib/projectFile.dart", "");
    myFixture.addFileToProject("local_package/lib/localPackageFile.html", "");
    myFixture.addFileToProject("packages/browser/dart.js", "");
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

  public void testBuiltInIdentifiers() {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.checkHighlighting(false, true, true);
  }

  public void testDeprecatedApiUsageInspection() {
    myFixture.enableInspections(DartDeprecatedApiUsageInspection.class);
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testUnusedImports() {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testColorAnnotator() {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.checkHighlighting(true, true, true);
  }

  public void testErrorsAfterEOF() {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testPathsWithSpaces() {
    myFixture.addFileToProject("pubspec.yaml", "");
    myFixture.addFileToProject("packages/pack name/pack file.dart", "library pack;");
    myFixture.addFileToProject("other file.dart", "library other;");
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testSimplePolymer() {
    myFixture.enableInspections(HtmlUnknownTagInspection.class);
    myFixture.addFileToProject("pubspec.yaml", "");
    addStandardPackage("polymer");
    addStandardPackage("core_elements");
    myFixture.configureByFile(getTestName(false) + "/web/" + getTestName(false) + ".html");
    myFixture.checkHighlighting(true, false, true);
  }
}
