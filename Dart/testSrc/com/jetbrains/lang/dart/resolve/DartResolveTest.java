package com.jetbrains.lang.dart.resolve;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.DartProjectComponent;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.util.DartTestUtils;

import static com.jetbrains.dart.analysisServer.DartServerResolverTest.doTest;

public class DartResolveTest extends DartCodeInsightFixtureTestCase {

  public void testResolveAndUseScope() throws Exception {
    try {
      final VirtualFile inSdk1 = DartLibraryIndex.getSdkLibByUri(getProject(), "dart:collection");
      final VirtualFile inSdk2 = DartLibraryIndex.getSdkLibByUri(getProject(), "dart:math");

      final VirtualFile inIdeLib1 = myFixture.addFileToProject("library/inLibrary1.dart", "").getVirtualFile();
      final VirtualFile inIdeLib2 = myFixture.addFileToProject("library/inLibrary2.dart", "").getVirtualFile();
      configureLibrary(inIdeLib1.getParent());

      final VirtualFile inContent = myFixture.addFileToProject("inContentOutsideDartRoot.dart", "").getVirtualFile();

      myFixture.addFileToProject("DartProject3/pubspec.yaml", "name: DartProject3");
      final VirtualFile inProject3Web = myFixture.addFileToProject("DartProject3/web/inProject3Web.dart", "").getVirtualFile();
      final VirtualFile inProject3Lib = myFixture.addFileToProject("DartProject3/lib/inProject3Lib.dart", "").getVirtualFile();

      myFixture.addFileToProject("DartProject2/pubspec.yaml", "name: DartProject2\n" +
                                                              "dependencies:\n" +
                                                              "  DartProject3:\n" +
                                                              "    path: ../DartProject3\n");
      final VirtualFile inProject2Web = myFixture.addFileToProject("DartProject2/web/inProject2Web.dart", "").getVirtualFile();
      final VirtualFile inProject2Lib = myFixture.addFileToProject("DartProject2/lib/inProject2Lib.dart", "").getVirtualFile();

      final VirtualFile pubspec = myFixture.addFileToProject("DartProject1/pubspec.yaml", "name: DartProject1\n" +
                                                                                          "dependencies:\n" +
                                                                                          "  DartProject2:\n" +
                                                                                          "    path: ../DartProject2\n").getVirtualFile();
      final VirtualFile inProject1Root = myFixture.addFileToProject("DartProject1/inProject1Root.dart", "").getVirtualFile();
      final VirtualFile inLib = myFixture.addFileToProject("DartProject1/lib/inLib.dart", "").getVirtualFile();
      //final VirtualFile inPackages = myFixture.addFileToProject("DartProject1/packages/inPackages.dart", "").getVirtualFile();
      final VirtualFile inWeb = myFixture.addFileToProject("DartProject1/web/inWeb.dart", "").getVirtualFile();
      final VirtualFile inWebSub = myFixture.addFileToProject("DartProject1/web/sub/inWebSub.dart", "").getVirtualFile();
      final VirtualFile inExcluded = myFixture.addFileToProject("DartProject1/web/packages/inExcluded.dart", "").getVirtualFile();
      final VirtualFile inTest = myFixture.addFileToProject("DartProject1/test/inTest.dart", "").getVirtualFile();
      final VirtualFile inExample = myFixture.addFileToProject("DartProject1/example/inExample.dart", "").getVirtualFile();

      DartProjectComponent.excludeBuildAndPackagesFolders(myModule, pubspec);

      doTestResolveScope(inExcluded, null, null, true);
      doTestResolveScope(new VirtualFile[]{inSdk1, inSdk2},
                         new VirtualFile[]{inSdk1, inSdk2},
                         new VirtualFile[]{inIdeLib1, inIdeLib2, inContent, inProject2Web, inProject2Lib, inProject3Web, inProject3Lib,
                           inProject1Root, inLib, /*inPackages,*/ inWeb, inWebSub, inExcluded, inTest, inExample});
      doTestResolveScope(new VirtualFile[]{inIdeLib1, inIdeLib2},
                         new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2},
                         new VirtualFile[]{inContent, inProject2Web, inProject2Lib, inProject3Web, inProject3Lib,
                           inProject1Root, inLib, /*inPackages,*/ inWeb, inWebSub, inExcluded, inTest, inExample});
      doTestResolveScope(new VirtualFile[]{inContent},
                         new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inContent, inProject2Web, inProject2Lib, inProject3Web,
                           inProject3Lib, inProject1Root, inLib, /*inPackages,*/ inWeb, inWebSub, inTest, inExample},
                         new VirtualFile[]{inExcluded});
      doTestResolveScope(inLib,
                         new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inProject2Lib, inProject3Lib, inLib, /*inPackages*/},
                         new VirtualFile[]{inContent, inProject2Web, inProject3Web, inExcluded, inProject1Root, inWeb, inWebSub, inTest,
                           inExample},
                         true);
      doTestResolveScope(VirtualFile.EMPTY_ARRAY /*inPackages*/,
                         new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inProject2Lib, inProject3Lib, inLib, /*inPackages*/},
                         new VirtualFile[]{inContent, inProject2Web, inProject3Web, inExcluded, inProject1Root, inWeb, inWebSub, inTest,
                           inExample});
      doTestResolveScope(new VirtualFile[]{inWeb, inWebSub},
                         new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inProject2Lib, inProject3Lib, inLib, /*inPackages,*/ inWeb,
                           inWebSub},
                         new VirtualFile[]{inContent, inProject2Web, inProject3Web, inExcluded, inProject1Root, inTest, inExample},
                         true);
      doTestResolveScope(inExample,
                         new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inProject2Lib, inProject3Lib, inLib, /*inPackages,*/
                           inExample},
                         new VirtualFile[]{inContent, inProject2Web, inProject3Web, inExcluded, inProject1Root, inTest, inWeb, inWebSub},
                         true);
      doTestResolveScope(new VirtualFile[]{inProject1Root, inTest},
                         new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inProject2Lib, inProject3Lib,
                           inProject1Root, inLib, /*inPackages,*/ inWeb, inWebSub, inTest, inExample},
                         new VirtualFile[]{inContent, inProject2Web, inProject3Web, inExcluded},
                         true);
      doTestResolveScope(new VirtualFile[]{inProject1Root, inLib, inWeb, inWebSub, inTest, inExample},
                         new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inProject2Lib, inProject3Lib,
                           inProject1Root, inLib, /*inPackages,*/ inWeb, inWebSub, inTest, inExample},
                         new VirtualFile[]{inContent, inProject2Web, inProject3Web, inExcluded},
                         false);

      doTestUseScope(new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2},
                     new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inContent, inProject2Web, inProject2Lib, inProject3Web,
                       inProject3Lib, inProject1Root, inLib, /*inPackages,*/ inWeb, inWebSub, inTest, inExample},
                     new VirtualFile[]{inExcluded});
      doTestUseScope(new VirtualFile[]{inContent},
                     new VirtualFile[]{inContent},
                     new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inProject2Web, inProject2Lib, inProject3Web,
                       inProject3Lib, inProject1Root, inLib, /*inPackages,*/ inWeb, inWebSub, inTest, inExample});
      doTestUseScope(new VirtualFile[]{/*inPackages,*/ inLib},
                     new VirtualFile[]{inContent, inProject1Root, inLib, /*inPackages,*/ inWeb, inWebSub, inTest, inExample},
                     new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inProject2Web, inProject2Lib, inProject3Web,
                       inProject3Lib});
      doTestUseScope(new VirtualFile[]{inProject1Root, inWeb, inWebSub, inTest, inExample},
                     new VirtualFile[]{inContent, inProject1Root, inLib, inWeb, inWebSub, inTest, inExample},
                     new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, /*inPackages,*/ inProject2Web, inProject2Lib, inProject3Web,
                       inProject3Lib});
      doTestUseScope(new VirtualFile[]{inProject2Lib},
                     new VirtualFile[]{inContent, /*inPackages,*/ inProject1Root, inLib, inWeb, inWebSub, inTest, inExample, inProject2Web,
                       inProject2Lib},
                     new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inProject3Web, inProject3Lib});
      doTestUseScope(new VirtualFile[]{inProject2Web},
                     new VirtualFile[]{inProject2Web, inProject2Lib, inContent},
                     new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, /*inPackages,*/ inProject1Root, inLib, inWeb, inWebSub,
                       inTest, inExample, inProject3Web, inProject3Lib});
      doTestUseScope(new VirtualFile[]{inProject3Lib},
                     new VirtualFile[]{inContent, /*inPackages,*/ inProject1Root, inLib, inWeb, inWebSub, inTest, inExample, inProject2Web,
                       inProject2Lib, inProject3Web, inProject3Lib},
                     new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2});
      doTestUseScope(new VirtualFile[]{inProject3Web},
                     new VirtualFile[]{inContent, inProject3Web, inProject3Lib},
                     new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, /*inPackages,*/ inProject1Root, inLib, inWeb, inWebSub, inTest,
                       inExample, inProject2Web, inProject2Lib});
    }
    finally {
      DartTestUtils.resetModuleRoots(myModule);
    }
  }

  private void doTestResolveScope(final VirtualFile[] contextFiles,
                                  final VirtualFile[] expectedInScope,
                                  final VirtualFile[] expectedOutsideScope) {
    for (VirtualFile file : contextFiles) {
      doTestResolveScope(file, expectedInScope, expectedOutsideScope, true);
      doTestResolveScope(file, expectedInScope, expectedOutsideScope, false);
    }
  }

  private void doTestResolveScope(final VirtualFile[] contextFiles,
                                  final VirtualFile[] expectedInScope,
                                  final VirtualFile[] expectedOutsideScope,
                                  final boolean strictScope) {
    for (VirtualFile file : contextFiles) {
      doTestResolveScope(file, expectedInScope, expectedOutsideScope, strictScope);
    }
  }

  private void doTestResolveScope(final VirtualFile contextFile,
                                  final VirtualFile[] expectedInScope,
                                  final VirtualFile[] expectedOutsideScope,
                                  final boolean strictScope) {
    final GlobalSearchScope scope = DartResolveScopeProvider.getDartScope(getProject(), contextFile, strictScope);

    if (scope == null) {
      assertTrue("Null scope not expected for " + contextFile.getPath(), expectedInScope == null);
      return;
    }

    if (expectedInScope == null) {
      fail("Null scope expected for " + contextFile.getPath());
      return;
    }

    for (VirtualFile file : expectedInScope) {
      assertTrue("Expected to be in scope: " + file.getPath(), scope.contains(file));
    }

    for (VirtualFile file : expectedOutsideScope) {
      assertFalse("Expected to be out of scope: " + file.getPath(), scope.contains(file));
    }
  }

  private void doTestUseScope(final VirtualFile[] contextFiles,
                              final VirtualFile[] expectedInScope,
                              final VirtualFile[] expectedOutsideScope) {
    for (VirtualFile file : contextFiles) {
      final PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(file);
      assertNotNull(psiFile);

      final GlobalSearchScope scope = (GlobalSearchScope)psiFile.getUseScope();

      for (VirtualFile file1 : expectedInScope) {
        assertTrue("Expected to be in scope: " + file1.getPath(), scope.contains(file1));
      }

      for (VirtualFile file1 : expectedOutsideScope) {
        assertFalse("Expected to be out of scope: " + file1.getPath(), scope.contains(file1));
      }
    }
  }

  private void configureLibrary(final VirtualFile root) {
    ApplicationManager.getApplication().runWriteAction(() -> {
      final ModifiableRootModel model = ModuleRootManager.getInstance(myModule).getModifiableModel();
      final Library library = model.getModuleLibraryTable().createLibrary();
      final Library.ModifiableModel libModel = library.getModifiableModel();
      libModel.addRoot(root, OrderRootType.CLASSES);
      libModel.commit();

      model.getContentEntries()[0].addExcludeFolder(root);
      model.commit();
    });
  }

  public void testPackageReferencesInHtml() throws Exception {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n" +
                                               "dependencies:\n" +
                                               "  PathPackage:\n" +
                                               "    path: local_package\n");
    myFixture.addFileToProject("lib/projectFile.dart", "");
    myFixture.addFileToProject("local_package/lib/localPackageFile.html", "");
    myFixture.addFileToProject("packages/browser/dart.js", "");
    final PsiFile psiFile = myFixture.addFileToProject("web/file.html",
                                                       "<script src='<caret expected='packages'>packages/<caret expected='lib'>ProjectName/<caret expected='lib/projectFile.dart'>projectFile.dart'/>\n" +
                                                       "<script src='packages<caret expected='packages'>/PathPackage<caret expected='local_package/lib'>/localPackageFile.html<caret expected='local_package/lib/localPackageFile.html'>'/>\n" +
                                                       "<script src='<caret expected='packages'>packages/<caret expected='packages/browser'>browser/<caret expected='packages/browser/dart.js'>dart.js'/>\n");
    myFixture.openFileInEditor(psiFile.getVirtualFile());
    doTest(myFixture);
  }
}
