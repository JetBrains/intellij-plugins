package com.jetbrains.lang.dart.highlighting;

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiFile;
import com.intellij.spellchecker.inspections.SpellCheckingInspection;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.inspections.DartDeprecatedApiUsageInspection;
import com.jetbrains.lang.dart.ide.inspections.DartPathPackageReferenceInspection;

public class DartHighlightingTest extends DartCodeInsightFixtureTestCase {
  protected String getBasePath() {
    return "/highlighting";
  }

  protected boolean isWriteActionRequired() {
    return false;
  }

  private void excludeFolder(final String relPath) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        final ModifiableRootModel model = ModuleRootManager.getInstance(myModule).getModifiableModel();
        try {
          final ContentEntry[] contentEntries = model.getContentEntries();
          contentEntries[0].addExcludeFolder(contentEntries[0].getUrl() + "/" + relPath);
          model.commit();
        }
        finally {
          if (!model.isDisposed()) {
            model.dispose();
          }
        }
      }
    });
  }

  private void unexcludeFolder(final String relPath) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        final ModifiableRootModel model = ModuleRootManager.getInstance(myModule).getModifiableModel();
        try {
          final ContentEntry[] contentEntries = model.getContentEntries();
          contentEntries[0].removeExcludeFolder(contentEntries[0].getUrl() + "/" + relPath);
          model.commit();
        }
        finally {
          if (!model.isDisposed()) {
            model.dispose();
          }
        }
      }
    });
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

  public void testColorAnnotator() {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.checkHighlighting(true, true, true);
  }

  public void _testPathsWithSpaces() { // DartInProcessAnnotator-specific test
    myFixture.addFileToProject("pubspec.yaml", "");
    myFixture.addFileToProject("packages/pack name/pack file.dart", "library pack;");
    myFixture.addFileToProject("other file.dart", "library other;");
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testSimplePolymer() {
    myFixture.enableInspections(HtmlUnknownTagInspection.class);
    myFixture.addFileToProject("pubspec.yaml", "name: ThisProject\n" +
                                               "dependencies:\n" +
                                               "  PathPackage:\n" +
                                               "    path: PathPackage\n");
    myFixture.addFileToProject("lib/custom_element.html", "<polymer-element name='custom-element'/>");
    myFixture.addFileToProject("PathPackage/lib/in_path_package.html", "<polymer-element name='path-package-element'/>");
    addStandardPackage("polymer");
    addStandardPackage("core_elements");
    myFixture.configureByFile(getTestName(false) + "/web/" + getTestName(false) + ".html");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testSyncAsyncAwaitYield() {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.checkHighlighting(true, true, true);
  }

  public void testPathPackageReferenceInspection() {
    myFixture.enableInspections(new DartPathPackageReferenceInspection());
    myFixture.copyDirectoryToProject(getTestName(false), "");
    myFixture.openFileInEditor(ModuleRootManager.getInstance(myModule).getContentRoots()[0].findChild("pubspec.yaml"));

    excludeFolder("other_project");
    try {
      myFixture.checkHighlighting(true, false, true);
    }
    finally {
      unexcludeFolder("other_project");
    }
  }

  public void testRenameImportedFile() {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n");
    final PsiFile libFile = myFixture.addFileToProject("lib/libFile.dart", "");
    final PsiFile libFile2 = myFixture.addFileToProject("lib/sub/libFile2.dart", "import '''../libFile.dart''';\n" +
                                                                                 "import '''package:ProjectName/libFile.dart''';");
    final PsiFile libFile3 = myFixture.addFileToProject("lib/libFile3.dart", "part '../lib/libFile.dart';\n" +
                                                                             "part 'package:ProjectName/sub/../libFile.dart';");
    final PsiFile webFile = myFixture.addFileToProject("web/webFile.dart", "import r'../lib/libFile.dart'\n" +
                                                                           "import r'package:ProjectName/libFile.dart'");

    myFixture.renameElement(libFile, "renamed.dart");

    myFixture.openFileInEditor(libFile2.getVirtualFile());
    myFixture.checkResult("import '''../renamed.dart''';\n" +
                          "import '''package:ProjectName/renamed.dart''';");

    myFixture.openFileInEditor(libFile3.getVirtualFile());
    myFixture.checkResult("part 'renamed.dart';\n" +
                          "part 'package:ProjectName/renamed.dart';");

    myFixture.openFileInEditor(webFile.getVirtualFile());
    myFixture.checkResult("import r'../lib/renamed.dart'\n" +
                          "import r'package:ProjectName/renamed.dart'");
  }
}
