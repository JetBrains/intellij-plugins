// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.resolve;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.runner.DartExecutionHelper;

import static com.jetbrains.dart.analysisServer.DartServerResolverTest.doTest;

public class DartResolveTest extends DartCodeInsightFixtureTestCase {

  public void testResolveAndUseScope() {
    final VirtualFile inContent = myFixture.addFileToProject("inContentOutsideDartRoot.dart", "").getVirtualFile();

    myFixture.addFileToProject("DartProject3/pubspec.yaml", "name: DartProject3");
    final VirtualFile inProject3Web = myFixture.addFileToProject("DartProject3/web/inProject3Web.dart", "").getVirtualFile();
    final VirtualFile inProject3Lib = myFixture.addFileToProject("DartProject3/lib/inProject3Lib.dart", "").getVirtualFile();

    myFixture.addFileToProject("DartProject2/pubspec.yaml", "name: DartProject2");
    final VirtualFile inProject2Web = myFixture.addFileToProject("DartProject2/web/inProject2Web.dart", "").getVirtualFile();
    final VirtualFile inProject2Lib = myFixture.addFileToProject("DartProject2/lib/inProject2Lib.dart", "").getVirtualFile();

    myFixture.addFileToProject("DartProject1/pubspec.yaml", "name: DartProject1").getVirtualFile();
    final VirtualFile inProject1Root = myFixture.addFileToProject("DartProject1/inProject1Root.dart", "").getVirtualFile();
    final VirtualFile inProject1Lib = myFixture.addFileToProject("DartProject1/lib/inLib.dart", "").getVirtualFile();
    final VirtualFile inProject1Web = myFixture.addFileToProject("DartProject1/web/inWeb.dart", "").getVirtualFile();
    final VirtualFile inProject1WebSub = myFixture.addFileToProject("DartProject1/web/sub/inWebSub.dart", "").getVirtualFile();
    final VirtualFile inProject1Test = myFixture.addFileToProject("DartProject1/test/inTest.dart", "").getVirtualFile();
    final VirtualFile inProject1Example = myFixture.addFileToProject("DartProject1/example/inExample.dart", "").getVirtualFile();

    doTestResolveScope(inContent,
                       new VirtualFile[]{inContent, inProject2Web, inProject2Lib, inProject3Web, inProject3Lib, inProject1Root,
                         inProject1Lib, inProject1Web, inProject1WebSub, inProject1Test, inProject1Example},
                       VirtualFile.EMPTY_ARRAY);
    doTestResolveScope(inProject1Lib,
                       new VirtualFile[]{inProject1Lib},
                       new VirtualFile[]{inContent, inProject2Web, inProject2Lib, inProject3Web, inProject3Lib, inProject1Root,
                         inProject1Web, inProject1WebSub, inProject1Test, inProject1Example});
    doTestResolveScope(inProject1Web,
                       new VirtualFile[]{inProject1Lib, inProject1Web, inProject1WebSub},
                       new VirtualFile[]{inContent, inProject2Web, inProject2Lib, inProject3Web, inProject3Lib, inProject1Root,
                         inProject1Test, inProject1Example});
    doTestResolveScope(inProject1WebSub,
                       new VirtualFile[]{inProject1Lib, inProject1Web, inProject1WebSub},
                       new VirtualFile[]{inContent, inProject2Web, inProject2Lib, inProject3Web, inProject3Lib, inProject1Root,
                         inProject1Test, inProject1Example});
    doTestResolveScope(inProject1Example,
                       new VirtualFile[]{inProject1Lib, inProject1Example},
                       new VirtualFile[]{inContent, inProject2Web, inProject2Lib, inProject3Web, inProject3Lib, inProject1Root,
                         inProject1Test, inProject1Web, inProject1WebSub});
    doTestResolveScope(inProject1Root,
                       new VirtualFile[]{inProject1Root, inProject1Lib, inProject1Web, inProject1WebSub, inProject1Test,
                         inProject1Example},
                       new VirtualFile[]{inContent, inProject2Web, inProject2Lib, inProject3Web, inProject3Lib});
    // DartExecutionHelper.getScopeOfFilesThatMayAffectExecution() is not called for tests in production, so we don't handle 'test' folder in a special way
    doTestResolveScope(inProject1Test,
                       new VirtualFile[]{inProject1Lib, inProject1Test},
                       new VirtualFile[]{inProject1Root, inProject1Web, inProject1WebSub, inProject1Example, inContent, inProject2Web,
                         inProject2Lib, inProject3Web, inProject3Lib});
  }

  private void doTestResolveScope(final VirtualFile contextFile,
                                  final VirtualFile[] expectedInScope,
                                  final VirtualFile[] expectedOutsideScope) {
    final GlobalSearchScope scope = DartExecutionHelper.getScopeOfFilesThatMayAffectExecution(getProject(), contextFile);

    if (scope == null) {
      assertNull("Null scope not expected for " + contextFile.getPath(), expectedInScope);
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

  public void testPackageReferencesInHtml() {
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

  public void testPackageReferencesInHtmlViaDotPackages() {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName");
    myFixture.addFileToProject("lib/projectFile.dart", "");
    myFixture.addFileToProject("local_package/lib/localPackageFile.html", "");
    myFixture.addFileToProject("global_package/browser/dart.js", "");
    myFixture.addFileToProject(".packages", "browser:global_package/browser/\n" +
                                            "ProjectName:lib/\n" +
                                            "PathPackage:local_package/lib/");
    final PsiFile psiFile = myFixture.addFileToProject("web/file.html",
                                                       "<script src='<caret expected='.packages'>packages/<caret expected='lib'>ProjectName/<caret expected='lib/projectFile.dart'>projectFile.dart'/>\n" +
                                                       "<script src='packages<caret expected='.packages'>/PathPackage<caret expected='local_package/lib'>/localPackageFile.html<caret expected='local_package/lib/localPackageFile.html'>'/>\n" +
                                                       "<script src='<caret expected='.packages'>packages/<caret expected='global_package/browser'>browser/<caret expected='global_package/browser/dart.js'>dart.js'/>\n");
    myFixture.openFileInEditor(psiFile.getVirtualFile());
    doTest(myFixture);
  }
}
