// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedFunctionInspection;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import org.angular2.Angular2MultiFileFixtureTestCase;
import org.angular2.inspections.quickfixes.Angular2FixesFactory;
import org.angular2.modules.Angular2TestModule;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static com.intellij.lang.javascript.modules.imports.JSImportAction.NAME_TO_IMPORT;
import static com.intellij.lang.javascript.ui.NodeModuleNamesUtil.PACKAGE_JSON;

/**
 * Also tests completion InsertHandlers.
 */
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
                    "Import Angular entity...",
                    "CommonModule - \"@angular/common\"");
  }

  public void testNgForCompletion() {
    doCompletionTest("angular-commons",
                     "test.html",
                     "*ngFor=\"let item of items\"",
                     "*ngFo\nlet item of items",
                     "CommonModule - \"@angular/common\"");
  }

  public void testNgClass() {
    doMultiFileTest("angular-commons",
                    "test.html",
                    "[ng<caret>Class]",
                    "Import Angular entity...",
                    "CommonModule - \"@angular/common\"");
  }

  public void testNgClassCompletion() {
    doCompletionTest("angular-commons",
                     "test.html",
                     "[ngClass]=\"'my'\"",
                     "[ngCl\n'my'",
                     "CommonModule - \"@angular/common\"");
  }

  public void testLowercasePipe() {
    doMultiFileTest("angular-commons",
                    "test.html",
                    "lower<caret>case",
                    "Import Angular entity...",
                    "CommonModule - \"@angular/common\"");
  }

  public void testLowercasePipeCompletion() {
    doCompletionTest("angular-commons",
                     "test.html",
                     "lowercase",
                     "lo\n",
                     "CommonModule - \"@angular/common\"");
  }

  public void testImportDirective() {
    doMultiFileTest("test.html",
                    "Import Module2");
  }

  public void testImportDirectiveCompletion() {
    doTagCompletionTest("test.html", "Module2 - \"./module2\"");
  }

  public void testUndeclaredDirective() {
    doMultiFileTest("test.html",
                    "Declare MyDirective in MyModule");
  }

  public void testUndeclaredDirectiveCompletion() {
    doTagCompletionTest("test.html", "MyModule - \"./module\"");
  }

  public void testUndeclaredDirectiveDifferentModule() {
    doMultiFileTest("test.html",
                    "Declare MyDirective in Angular module",
                    "Module2 - \"./module2\"");
  }

  public void testUndeclaredDirectiveDifferentModuleCompletion() {
    doTagCompletionTest("test.html",
                        "Module2 - \"./module2\"");
  }

  public void testNotExportedDirectiveNoModuleImport() {
    doMultiFileTest("test.html",
                    "Export MyDirective");
  }

  public void testNotExportedDirectiveNoModuleImportCompletion() {
    doTagCompletionTest("test.html", null);
  }

  public void testNotExportedDirectiveSingleModuleImport() {
    doMultiFileTest("test.html",
                    "Export MyDirective");
  }

  public void testNotExportedDirectiveSingleModuleImportCompletion() {
    doTagCompletionTest("test.html", "Module2 - \"./module2\"");
  }

  public void testNotExportedDirectiveMultiModuleImport() {
    doMultiFileTest("test.html",
                    "Export MyDirective",
                    "Module3 - \"./module3\"");
  }

  public void testNotExportedDirectiveMultiModuleImportCompletion() {
    doTagCompletionTest("test.html",
                        "Module3 - \"./module3\"");
  }

  public void testInlineTemplate() {
    doMultiFileTest("component.ts",
                    "Declare MyDirective in MyModule");
  }

  public void testInlineTemplateCompletion() {
    doTagCompletionTest("component.ts", "MyModule - \"./module\"");
  }

  public void testFormsModule1() {
    doMultiFileTest("formsModule",
                    "test.html",
                    "[ngValue<caret>]",
                    "Import Angular entity...",
                    "FormsModule - \"@angular/forms\"",
                    Angular2TestModule.ANGULAR_FORMS_8_2_14);
  }

  public void testFormsModule2() {
    doMultiFileTest("formsModule",
                    "test.html",
                    "ng<caret>Model",
                    "Import Angular entity...",
                    "FormsModule - \"@angular/forms\"",
                    Angular2TestModule.ANGULAR_FORMS_8_2_14);
  }

  public void testFormsModule3() {
    doMultiFileTest("formsModule",
                    "test.html",
                    "[ng<caret>Model]",
                    "Import FormsModule",
                    null,
                    Angular2TestModule.ANGULAR_FORMS_8_2_14);
  }

  public void testFormsModule4() {
    doMultiFileTest("formsModule",
                    "test.html",
                    "[(ng<caret>Model)]",
                    "Import FormsModule",
                    null,
                    Angular2TestModule.ANGULAR_FORMS_8_2_14);
  }

  public void testFormsModuleCompletion1() {
    doCompletionTest("formsModule",
                     "test.html",
                     "[ngValue]=\"foo\"",
                     "[ngVal\nfoo",
                     "FormsModule - \"@angular/forms\"",
                     Angular2TestModule.ANGULAR_FORMS_8_2_14);
  }

  public void testFormsModuleCompletion2() {
    doCompletionTest("formsModule",
                     "test.html",
                     "ngModel ",
                     "ngMod\n ",
                     "FormsModule - \"@angular/forms\"",
                     Angular2TestModule.ANGULAR_FORMS_8_2_14);
  }

  public void testFormsModuleCompletion3() {
    doCompletionTest("formsModule",
                     "test.html",
                     "[ngModel]=\"foo\"",
                     "[ngMod\nfoo",
                     "FormsModule - \"@angular/forms\"",
                     Angular2TestModule.ANGULAR_FORMS_8_2_14);
  }

  public void testFormsModuleCompletion4() {
    doCompletionTest("formsModule",
                     "test.html",
                     "[(ngModel)]=\"foo\"",
                     "[(ngMod\nfoo",
                     "FormsModule - \"@angular/forms\"",
                     Angular2TestModule.ANGULAR_FORMS_8_2_14);
  }

  public void testReactiveFormsModule1() {
    doMultiFileTest("reactiveFormsModule",
                    "test.html",
                    "[ngValue<caret>]",
                    "Import Angular entity...",
                    "ReactiveFormsModule - \"@angular/forms\"",
                    Angular2TestModule.ANGULAR_FORMS_8_2_14);
  }

  public void testReactiveFormsModule2() {
    doMultiFileTest("reactiveFormsModule",
                    "test.html",
                    "ng<caret>Model",
                    "Import Angular entity...",
                    "ReactiveFormsModule - \"@angular/forms\"",
                    Angular2TestModule.ANGULAR_FORMS_8_2_14);
  }

  public void testReactiveFormsModuleCompletion1() {
    doCompletionTest("reactiveFormsModule",
                     "test.html",
                     "[ngValue]=\"foo\"",
                     "[ngVal\nfoo",
                     "ReactiveFormsModule - \"@angular/forms\"",
                     Angular2TestModule.ANGULAR_FORMS_8_2_14);
  }

  public void testReactiveFormsModuleCompletion2() {
    doCompletionTest("reactiveFormsModule",
                     "test.html",
                     "ngModel ",
                     "ngMod\n ",
                     "ReactiveFormsModule - \"@angular/forms\"",
                     Angular2TestModule.ANGULAR_FORMS_8_2_14);
  }

  public void testLocalLib() {
    doMultiFileTest("src/app/app.component.html",
                    "Import MyLibModule");
  }

  public void testLocalLibCompletion() {
    doCompletionTest("localLib", "src/app/app.component.html",
                     "lib-my-lib", "lib-my-l\n",
                     "MyLibModule - \"../../dist/my-lib\"");
  }

  public void testImportStandaloneComponentToStandaloneComponent() {
    doMultiFileTest("standaloneComponent",
                    "test.ts",
                    "app-<caret>standalone",
                    "Import StandaloneComponent",
                    "StandaloneComponent - \"./standalone.component\"");
  }

  public void testImportStandalonePipeToStandaloneComponent() {
    doMultiFileTest("standalonePipe",
                    "test.ts",
                    "stand<caret>alone",
                    "Import StandalonePipe",
                    "StandalonePipe - \"./standalone.pipe\"");
  }

  public void testImportStandaloneComponentToModule() {
    doMultiFileTest("standaloneComponentToModule",
                    "test.ts",
                    "app-<caret>standalone",
                    "Import StandaloneComponent",
                    "StandaloneComponent - \"./standalone.component\"");
  }

  public void testImportStandaloneComponentImportModule() {
    doMultiFileTest("standaloneComponentImportModule",
                    "test.ts",
                    "app-<caret>classic",
                    "Import ClassicModule",
                    "ClassicModule - \"./classic\"");
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
                               @Nullable String importName,
                               @NotNull Angular2TestModule @NotNull ... modules) {
    initInspections();
    doTest((rootDir, rootAfter) -> configureTestAndRun(mainFile, signature, importName, modules, () -> {
      myFixture.launchAction(myFixture.findSingleIntention(intention));
    }), testName);
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
                                @Nullable String importToSelect,
                                @NotNull Angular2TestModule @NotNull ... modules) {
    doTest((rootDir, rootAfter) -> configureTestAndRun(mainFile, "<caret>" + toRemove, importToSelect, modules, () -> {
      myFixture.getEditor().putUserData(Angular2FixesFactory.DECLARATION_TO_CHOOSE, "MyDirective");
      myFixture.getEditor().getSelectionModel().setSelection(myFixture.getCaretOffset(),
                                                             myFixture.getCaretOffset() + toRemove.length());
      myFixture.type("\b");
      myFixture.completeBasic();
      myFixture.type(toType);
    }), testName);
  }

  private void configureTestAndRun(@NotNull String mainFile, @Nullable String signature, @Nullable String importName,
                                   @NotNull Angular2TestModule @NotNull [] modules, Runnable runnable) throws IOException {
    boolean hasPkgJson = myFixture.getTempDirFixture().getFile(PACKAGE_JSON) != null;
    Angular2TestModule.configureLink(myFixture, ArrayUtil.mergeArrays(
      modules, new Angular2TestModule[]{Angular2TestModule.ANGULAR_CORE_4_0_0, Angular2TestModule.ANGULAR_COMMON_4_0_0,
        Angular2TestModule.ANGULAR_PLATFORM_BROWSER_4_0_0}));
    myFixture.configureFromTempProjectFile(mainFile);
    if (signature != null) {
      AngularTestUtil.moveToOffsetBySignature(signature, myFixture);
    }
    if (importName != null) {
      myFixture.getEditor().putUserData(NAME_TO_IMPORT, importName);
    }
    runnable.run();
    if (!hasPkgJson) {
      WriteAction.runAndWait(() -> {
        myFixture.getTempDirFixture().getFile(PACKAGE_JSON).delete(null);
      });
    }
  }

  private void initInspections() {
    myFixture.enableInspections(
      AngularUndefinedBindingInspection.class,
      AngularUndefinedTagInspection.class,
      AngularInvalidTemplateReferenceVariableInspection.class,
      TypeScriptUnresolvedFunctionInspection.class
    );
  }
}
