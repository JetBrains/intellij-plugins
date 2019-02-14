// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.lang.javascript.LightPlatformMultiFileFixtureTestCase;
import com.intellij.lang.resharper.ReSharperTestUtil;
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedFunctionInspection;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PsiTestUtil;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.javascript.modules.ES6ImportAction.NAME_TO_IMPORT;

public class Angular2NgModuleImportQuickFixesTest extends LightPlatformMultiFileFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  @NotNull
  @Override
  protected String getTestRoot() {
    return "ngModuleImport/";
  }

  public void testNgFor() {
    doMultiFileTest("angular-commons",
                    "test.html",
                    "*ng<caret>For",
                    "Import NgModule...",
                    "\"@angular/common\"");
  }

  public void testNgClass() {
    doMultiFileTest("angular-commons",
                    "test.html",
                    "[ng<caret>Class]",
                    "Import NgModule...",
                    "\"@angular/common\"");
  }

  public void testLowercasePipe() {
    doMultiFileTest("angular-commons",
                    "test.html",
                    "lower<caret>case",
                    "Import NgModule...",
                    "\"@angular/common\"");
  }

  public void testImportDirective() {
    doMultiFileTest("test.html",
                    "Import Module2");
  }

  public void testUndeclaredDirective() {
    doMultiFileTest("test.html",
                    "Declare MyDirective in MyModule");
  }

  public void testUndeclaredDirectiveDifferentModule() {
    doMultiFileTest("test.html",
                    "Declare MyDirective in an NgModule",
                    "( module2.ts )");
  }

  public void testNotExportedDirectiveNoModuleImport() {
    doMultiFileTest("test.html",
                    "Export MyDirective");
  }

  public void testNotExportedDirectiveSingleModuleImport() {
    doMultiFileTest("test.html",
                    "Export MyDirective");
  }

  public void testNotExportedDirectiveMultiModuleImport() {
    doMultiFileTest("test.html",
                    "Export MyDirective",
                    "\"./module3\"");
  }

  public void testInlineTemplate() {
    doMultiFileTest("component.ts",
                    "Declare MyDirective in MyModule");
  }

  private void doMultiFileTest(@NotNull String mainFile,
                               @NotNull String intention) {
    doMultiFileTest(mainFile, intention, null);
  }

  private void doMultiFileTest(@NotNull String mainFile,
                               @NotNull String intention,
                               @Nullable String importName) {
    doMultiFileTest(getTestName(true), mainFile, null,
                    intention, importName);
  }

  private void doMultiFileTest(@NotNull String testName,
                               @NotNull String mainFile,
                               @Nullable String signature,
                               @NotNull String intention,
                               @Nullable String importName) {
    doTest((rootDir, rootAfter) -> {
      initInspections();
      initNodeModules();
      myFixture.configureFromTempProjectFile(mainFile);
      if (signature != null) {
        AngularTestUtil.moveToOffsetBySignature(signature, myFixture);
      }
      if (importName != null) {
        myFixture.getEditor().putUserData(NAME_TO_IMPORT, importName);
      }
      myFixture.launchAction(myFixture.findSingleIntention(intention));
    }, testName);
  }

  private void initInspections() {
    myFixture.enableInspections(
      Angular2BindingsInspection.class,
      Angular2TagsInspection.class,
      Angular2TemplateReferenceVariableInspection.class,
      TypeScriptUnresolvedFunctionInspection.class
    );
  }

  private void initNodeModules() {
    VirtualFile nodeModules = getNodeModules();
    PsiTestUtil.addSourceContentToRoots(myModule, nodeModules);
    Disposer.register(myFixture.getTestRootDisposable(),
                      () -> PsiTestUtil.removeContentEntry(myModule, nodeModules));
  }

  @NotNull
  private VirtualFile getNodeModules() {
    VirtualFile nodeModules = ReSharperTestUtil.fetchVirtualFile(
      getTestDataPath(), getTestRoot() + "/node_modules", getTestRootDisposable());
    assert nodeModules != null;
    return nodeModules;
  }
}
