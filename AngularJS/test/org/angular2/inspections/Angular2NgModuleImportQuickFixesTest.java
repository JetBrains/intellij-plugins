// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.lang.resharper.ReSharperTestUtil;
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedFunctionInspection;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PsiTestUtil;
import org.angular2.Angular2MultiFileFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.javascript.modules.ES6ImportAction.NAME_TO_IMPORT;

public class Angular2NgModuleImportQuickFixesTest extends Angular2MultiFileFixtureTestCase {

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

  public void testNgForCompletion() {
    doCompletionTest("angular-commons",
                     "test.html",
                     "*ngFor=\"let item of items\"",
                     "*ngFo\nlet item of items",
                     "\"@angular/common\"");
  }

  public void testNgClass() {
    doMultiFileTest("angular-commons",
                    "test.html",
                    "[ng<caret>Class]",
                    "Import NgModule...",
                    "\"@angular/common\"");
  }

  public void testNgClassCompletion() {
    doCompletionTest("angular-commons",
                     "test.html",
                     "[ngClass]=\"'my'\"",
                     "[ngCl\n'my'",
                     "\"@angular/common\"");
  }

  public void testLowercasePipe() {
    doMultiFileTest("angular-commons",
                    "test.html",
                    "lower<caret>case",
                    "Import NgModule...",
                    "\"@angular/common\"");
  }

  public void testLowercasePipeCompletion() {
    doCompletionTest("angular-commons",
                     "test.html",
                     "lowercase",
                     "lo\n",
                     "\"@angular/common\"");
  }

  public void testImportDirective() {
    doMultiFileTest("test.html",
                    "Import Module2");
  }

  public void testImportDirectiveCompletion() {
    doTagCompletionTest("test.html");
  }

  public void testUndeclaredDirective() {
    doMultiFileTest("test.html",
                    "Declare MyDirective in MyModule");
  }

  public void testUndeclaredDirectiveCompletion() {
    doTagCompletionTest("test.html");
  }

  public void testUndeclaredDirectiveDifferentModule() {
    doMultiFileTest("test.html",
                    "Declare MyDirective in an NgModule",
                    "( module2.ts )");
  }

  public void testUndeclaredDirectiveDifferentModuleCompletion() {
    doTagCompletionTest("test.html",
                        "( module2.ts )");
  }

  public void testNotExportedDirectiveNoModuleImport() {
    doMultiFileTest("test.html",
                    "Export MyDirective");
  }

  public void testNotExportedDirectiveNoModuleImportCompletion() {
    doTagCompletionTest("test.html");
  }

  public void testNotExportedDirectiveSingleModuleImport() {
    doMultiFileTest("test.html",
                    "Export MyDirective");
  }

  public void testNotExportedDirectiveSingleModuleImportCompletion() {
    doTagCompletionTest("test.html");
  }

  public void testNotExportedDirectiveMultiModuleImport() {
    doMultiFileTest("test.html",
                    "Export MyDirective",
                    "\"./module3\"");
  }

  public void testNotExportedDirectiveMultiModuleImportCompletion() {
    doTagCompletionTest("test.html",
                        "\"./module3\"");
  }

  public void testInlineTemplate() {
    doMultiFileTest("component.ts",
                    "Declare MyDirective in MyModule");
  }

  public void testInlineTemplateCompletion() {
    doTagCompletionTest("component.ts");
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

  private void doTagCompletionTest(@NotNull String mainFile) {
    doTagCompletionTest(mainFile, null);
  }

  private void doTagCompletionTest(@NotNull String mainFile,
                                   @Nullable String importToSelect) {
    doCompletionTest(StringUtil.trimEnd(getTestName(true), "Completion"),
                     mainFile, "foo", "foo\n", importToSelect);
  }

  private void doCompletionTest(@NotNull String testName,
                                @NotNull String mainFile,
                                @NotNull String toRemove,
                                @NotNull String toType,
                                @Nullable String importToSelect) {
    doTest((rootDir, rootAfter) -> {
      initNodeModules();
      myFixture.configureFromTempProjectFile(mainFile);
      if (importToSelect != null) {
        myFixture.getEditor().putUserData(NAME_TO_IMPORT, importToSelect);
      }
      AngularTestUtil.moveToOffsetBySignature("<caret>" + toRemove, myFixture);
      myFixture.getEditor().getSelectionModel().setSelection(myFixture.getCaretOffset(),
                                                             myFixture.getCaretOffset() + toRemove.length());
      myFixture.type("\b");
      myFixture.completeBasic();
      myFixture.type(toType);
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
