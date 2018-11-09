// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.highlighting;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFilesOrDirectoriesProcessor;
import com.intellij.spellchecker.inspections.SpellCheckingInspection;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.inspections.DartPathPackageReferenceInspection;
import com.jetbrains.lang.dart.util.DartResolveUtil;

import java.util.List;

public class DartHighlightingTest extends DartCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return "/highlighting";
  }

  private void excludeFolder(final String relPath) {
    ApplicationManager.getApplication().runWriteAction(() -> {
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
    });
  }

  private void unexcludeFolder(final String relPath) {
    ApplicationManager.getApplication().runWriteAction(() -> {
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

  public void testScriptSrcPathToDotPackagesFile() {
    myFixture.enableInspections(HtmlUnknownTargetInspection.class);
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName");
    myFixture.addFileToProject("local_package/lib/src/localPackageFile.html", "");
    myFixture.addFileToProject(".packages","PathPackage:local_package/lib/");
    myFixture.configureByText("foo.html", "<link href=\"packages/PathPackage/src/localPackageFile.html\">\n" +
                                          "<link href=\"packages/PathPackage/src/<warning descr=\"Cannot resolve file 'incorrect.html'\">incorrect.html</warning> \">");
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

  public void testSyncAsyncAwaitYield() {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.checkHighlighting(true, true, true);

    // now check that reparsing because of typing inside a reparseable block doesn't loose knowledge that the method is async
    final List<HighlightInfo> oldHighlighting = myFixture.doHighlighting();
    myFixture.type(' ');
    assertSameElements(myFixture.doHighlighting(), oldHighlighting);
  }

  public void testColorAnnotatorIdePart() {
    // includes cases not covered by testSyncAsyncAwaitYield, testBuiltInIdentifiers and testEscapeSequences
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.checkHighlighting(true, true, true);
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

  public void _testRenameImportedFile() {
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

  public void _testUpdateImportsOnFileMove() {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n");
    final PsiFile libFile = myFixture.addFileToProject("lib/libFile.dart", "");
    final PsiFile libFile2 = myFixture.addFileToProject("lib/sub/libFile2.dart", "import '../libFile.dart';\n" +
                                                                                 "import 'package:ProjectName/libFile.dart';");
    new MoveFilesOrDirectoriesProcessor(getProject(), new PsiElement[]{libFile2}, libFile.getParent(), true, true, true, null, null).run();

    myFixture.openFileInEditor(libFile2.getVirtualFile());
    myFixture.checkResult("import 'libFile.dart';\n" +
                          "import 'package:ProjectName/libFile.dart';");
  }

  public void testUriInPartOf() {
    final PsiFile libFile = myFixture.addFileToProject("foo/bar/libFile.dart", "library libName;");
    final PsiFile part1File = myFixture.addFileToProject("part1.dart", "part of 'part1.dart'"); // self reference
    final PsiFile part2File = myFixture.addFileToProject("part2.dart", "part of 'foo/bar/wrong.dart'"); // wrong reference
    final PsiFile part3File = myFixture.addFileToProject("part3.dart", "part of 'foo/bar/libFile.dart"); // reference to libName
    final PsiFile part4File = myFixture.addFileToProject("part4.dart", "part of anotherLib;"); // reference to anotherLib

    assertEquals("libName", DartResolveUtil.getLibraryName(libFile));
    assertEquals("part1.dart", DartResolveUtil.getLibraryName(part1File));
    assertEquals("wrong.dart", DartResolveUtil.getLibraryName(part2File));
    assertEquals("libName", DartResolveUtil.getLibraryName(part3File));
    assertEquals("anotherLib", DartResolveUtil.getLibraryName(part4File));
  }
}
