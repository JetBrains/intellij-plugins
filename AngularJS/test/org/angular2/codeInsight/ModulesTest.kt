// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import one.util.streamex.StreamEx;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.entities.*;
import org.angular2.modules.Angular2TestModule;
import org.angular2.web.Angular2Symbol;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.angular2.modules.Angular2TestModule.*;

public class ModulesTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "modules";
  }

  public void testCommonModuleResolution() {
    doResolutionTest("common",
                     "common_module.ts",
                     "export class Common<caret>Module",
                     "check.txt");
  }

  public void testCommonModuleResolutionMetadata() {
    doResolutionTest("common-metadata",
                     "myModule.ts",
                     "export class Common<caret>ModuleMetadataTest {",
                     "check.txt");
  }

  public void testRouterModuleResolution() {
    doResolutionTest("router",
                     "myModule.ts",
                     "class AppRouting<caret>Module {",
                     "check-full.txt");
  }

  public void testRouterModuleResolutionMetadata() {
    doResolutionTest("router-metadata",
                     "myModule.ts",
                     "class AppRouting<caret>Module {",
                     "check-full.txt");
  }

  public void testRouterModuleResolutionNotFull() {
    doResolutionTest("router",
                     "myModule.ts",
                     "export class AppRoutingModule<caret>NotFullyResolved {",
                     "check-not-full.txt");
  }

  public void testRouterModuleResolutionNotFullMetadata() {
    doResolutionTest("router-metadata",
                     "myModule.ts",
                     "export class AppRoutingModule<caret>NotFullyResolved {",
                     "check-not-full.txt");
  }

  public void testBrowserModuleResolutionNotFull() {
    doResolutionTest("browser",
                     "myModule.ts",
                     "class BrowserModule<caret>Test {",
                     "check.txt");
  }

  public void testIonicResolutionMetadata() {
    doResolutionTest("ionic-metadata",
                     "myIonicModule.ts",
                     "export class MyIonic<caret>Module {",
                     "check-no-common.txt");
  }

  public void testIonicResolutionMetadataWithCommon() {
    myFixture.copyDirectoryToProject("common-metadata/common", "/common");
    doResolutionTest("ionic-metadata",
                     "myIonicModule.ts",
                     "export class MyIonic<caret>Module {",
                     "check-with-common.txt");
  }

  public void testSourceForRootResolution() {
    doResolutionTest("source-forRoot",
                     "mainModule.ts",
                     "export class Main<caret>Module {",
                     "check.txt",
                     ANGULAR_CORE_8_2_14);
  }

  public void testJsonMetadataForRootResolution() {
    doResolutionTest("metadata-forRoot",
                     "mainModule.ts",
                     "export class Main<caret>Module {",
                     "check.txt",
                     ANGULAR_CORE_8_2_14);
  }

  public void testIvyMetadataForRootResolution() {
    doResolutionTest("ivy-forRoot",
                     "mainModule.ts",
                     "export class Main<caret>Module {",
                     "check.txt",
                     ANGULAR_CORE_8_2_14);
  }

  public void testFormsResolution() {
    doResolutionTest("forms",
                     "myModule.ts",
                     "export class FormsModuleMetadata<caret>Test {",
                     "check.txt",
                     ANGULAR_FORMS_8_2_14);
  }

  public void testNgModuleWithConstant() {
    doResolutionTest("ng-module-with-constant",
                     "module.ts",
                     "export class My<caret>Module {",
                     "check.txt");
  }

  public void testAgmCore() {
    doResolutionTest("agm-core",
                     "module.ts",
                     "export class Main<caret>Module {",
                     "check.txt",
                     AGM_CORE_1_0_0_BETA_5);
  }

  public void testFunctionCalls() {
    doResolutionTest("function-calls",
                     "my-test-lib.module.ts",
                     "MyTestLib<caret>Module",
                     "check.txt");
  }

  public void testEvoUiKit() {
    doResolutionTest("evo-ui-kit",
                     "module.ts",
                     "export class Main<caret>Module {",
                     "check.txt",
                     EVO_UI_KIT_1_17_0);
  }

  public void testCommonNgClassModules() {
    doDeclarationModulesCheckText("common",
                                  "directives/ng_class.ts",
                                  "export class Ng<caret>Class ",
                                  "CommonModule");
  }

  public void testCommonDatePipeModules() {
    doDeclarationModulesCheckText("common",
                                  "pipes/date_pipe.ts",
                                  "export class Date<caret>Pipe ",
                                  "CommonModule");
  }

  public void testAsyncPipeModulesMetadata() {
    doDeclarationModulesCheckText("common-metadata",
                                  "common/src/pipes/async_pipe.d.ts",
                                  "export declare class Async<caret>Pipe ",
                                  "CommonModule",
                                  "CommonModuleMetadataTest");
  }

  public void testCommonNg12() {
    doResolutionTest("ng12-common",
                     "mainModule.ts",
                     "export class App<caret>Module {",
                     "check.txt");
  }

  public void testCommonNg13() {
    doResolutionTest("ng13-common",
                     "app.module.ts",
                     "export class App<caret>Module {",
                     "check.txt");
  }

  public void testPrivateModuleExportMetadata() {
    doResolutionTest("private-module-export-metadata",
                     "module.ts",
                     "export class Amount<caret>Module {",
                     "check.txt",
                     ANGULAR_CORE_8_2_14,
                     NGXS_STORE_3_6_2);
  }

  public void testPrivateModuleExportIvy() {
    doResolutionTest("private-module-export-ivy",
                     "module.ts",
                     "export class Amount<caret>Module {",
                     "check.txt",
                     ANGULAR_CORE_9_1_1_MIXED,
                     NGXS_STORE_3_6_2_MIXED);
  }

  public void testModuleReexport() {
    doResolutionTest("module-reexport",
                     "app.module.ts",
                     "export class App<caret>Module {",
                     "check.txt",
                     ANGULAR_CORE_9_1_1_MIXED);
  }

  public void testRequiredProperties() {
    doResolutionTest("required-properties",
                     "app.module.ts",
                     "export class App<caret>Module {",
                     "check.txt",
                     true,
                     ANGULAR_CORE_16_0_0_NEXT_4, ANGULAR_COMMON_16_0_0_NEXT_4);
  }

  private void doResolutionTest(@NotNull String directory,
                                @NotNull String moduleFile,
                                @NotNull String signature,
                                @NotNull String checkFile,
                                @NotNull Angular2TestModule @NotNull ... modules) {
    doResolutionTest(directory, moduleFile, signature, checkFile, false, modules);
  }

  private void doResolutionTest(@NotNull String directory,
                                @NotNull String moduleFile,
                                @NotNull String signature,
                                @NotNull String checkFile,
                                Boolean printDirectives,
                                @NotNull Angular2TestModule @NotNull ... modules) {
    VirtualFile testDir = myFixture.copyDirectoryToProject(directory, "/");
    configureCopy(myFixture, modules);
    myFixture.openFileInEditor(testDir.findFileByRelativePath(moduleFile));
    int moduleOffset = AngularTestUtil.findOffsetBySignature(signature, myFixture.getFile());
    PsiElement el = myFixture.getFile().findElementAt(moduleOffset);
    assert el != null;
    TypeScriptClass moduleClass = PsiTreeUtil.getParentOfType(el, TypeScriptClass.class);
    assert moduleClass != null;
    Angular2Module module = Angular2EntitiesProvider.getModule(moduleClass);
    assert module != null;

    StringBuilder result = new StringBuilder();
    printEntity(0, module, result, printDirectives, new HashSet<>());
    myFixture.configureByText("__my-check.txt", result.toString());
    myFixture.checkResultByFile(directory + "/" + checkFile, true);
  }

  private void doDeclarationModulesCheckText(@NotNull String directory,
                                             @NotNull String declarationFile,
                                             @NotNull String signature,
                                             String... modules) {
    VirtualFile testDir = myFixture.copyDirectoryToProject(directory, "/");
    configureLink(myFixture);
    myFixture.openFileInEditor(testDir.findFileByRelativePath(declarationFile));
    int moduleOffset = AngularTestUtil.findOffsetBySignature(signature, myFixture.getFile());
    PsiElement el = myFixture.getFile().findElementAt(moduleOffset);
    assert el != null;
    TypeScriptClass declarationClass = PsiTreeUtil.getParentOfType(el, TypeScriptClass.class);
    assert declarationClass != null;
    Angular2Declaration declaration = (Angular2Declaration)Angular2EntitiesProvider.getEntity(declarationClass);
    assert declaration != null;
    assertEquals(ContainerUtil.sorted(Arrays.asList(modules), String::compareToIgnoreCase),
                 StreamEx.of(declaration.getAllDeclaringModules())
                   .map(m -> m.getName())
                   .sorted(String::compareToIgnoreCase)
                   .toList());
  }

  private static void printEntity(int level,
                                  @NotNull Angular2Entity entity,
                                  @NotNull StringBuilder result,
                                  Boolean printDirectives,
                                  @NotNull Set<Angular2Entity> printed) {
    withIndent(level, result)
      .append(entity.getName())
      .append(": ")
      .append(entity.getClass().getSimpleName())
      .append('\n');
    if (entity instanceof Angular2Module module) {
      if (!printed.add(entity)) {
        withIndent(level + 1, result)
          .append("<printed above>\n");
        return;
      }
      level++;
      printEntityList(level, "imports", module.getImports(), printDirectives, result, printed);
      printEntityList(level, "declarations", module.getDeclarations(), printDirectives, result, printed);
      printEntityList(level, "exports", module.getExports(), printDirectives, result, printed);
      printEntityList(level, "all-exported-declarations", module.getAllExportedDeclarations(), printDirectives, result, printed);
      printEntityList(level, "scope", module.getDeclarationsInScope(), printDirectives, result, printed);
      withIndent(level, result)
        .append("scope fully resolved: ")
        .append(module.isScopeFullyResolved())
        .append('\n');
      withIndent(level, result)
        .append("exports fully resolved: ")
        .append(module.areExportsFullyResolved())
        .append('\n');
      withIndent(level, result)
        .append("declarations fully resolved: ")
        .append(module.areDeclarationsFullyResolved())
        .append('\n');
    }
    else if (entity instanceof Angular2Directive directive) {
      if ((printDirectives || directive.isStandalone()) && !printed.add(entity)) {
        withIndent(level + 1, result)
          .append("<printed above>\n");
        return;
      }

      if (printDirectives) {
        level++;
        withIndent(level, result)
          .append("standalone: ")
          .append(directive.isStandalone())
          .append("\n");
        withIndent(level, result)
          .append("selector: ")
          .append(directive.getSelector())
          .append("\n");
        withIndent(level, result)
          .append("kind: ")
          .append(directive.getDirectiveKind())
          .append("\n");
        if (!directive.getExportAsList().isEmpty()) {
          withIndent(level, result)
            .append("exportAs list: ")
            .append(directive.getExportAsList())
            .append("\n");
        }
        printSymbolList(level, "inputs", directive.getInputs(), result);
        printSymbolList(level, "outputs", directive.getOutputs(), result);
        printSymbolList(level, "inOuts", directive.getInOuts(), result);
        printSymbolList(level, "attributes", directive.getAttributes(), result);
      }
      if (directive.isStandalone() && directive instanceof Angular2ImportsOwner importsOwner) {
        printEntityList(level, "imports", importsOwner.getImports(), printDirectives, result, printed);
        printEntityList(level, "scope", importsOwner.getDeclarationsInScope(), printDirectives, result, printed);
        withIndent(level, result)
          .append("scope fully resolved: ")
          .append(importsOwner.isScopeFullyResolved())
          .append('\n');
      }
    }
  }

  private static void printEntityList(int level,
                                      @NotNull String name,
                                      @NotNull Set<? extends Angular2Entity> entities,
                                      Boolean printDirectives,
                                      @NotNull StringBuilder result,
                                      @NotNull Set<Angular2Entity> printed) {
    withIndent(level, result)
      .append(name)
      .append(":\n");
    ContainerUtil.sorted(entities, Comparator.comparing(Angular2Entity::getName))
      .forEach(m -> printEntity(level + 1, m, result, printDirectives, printed));
  }

  private static void printSymbolList(int level,
                                      @NotNull String name,
                                      @NotNull Collection<? extends Angular2Symbol> symbols,
                                      @NotNull StringBuilder result) {
    if (symbols.isEmpty()) return;
    withIndent(level, result)
      .append(name)
      .append(":\n");
    ContainerUtil.sorted(symbols, Comparator.comparing(Angular2Symbol::getName))
      .forEach(m -> withIndent(level + 1, result).append(m).append("\n"));
  }

  @NotNull
  private static StringBuilder withIndent(int level, @NotNull StringBuilder result) {
    return result.append("  ".repeat(Math.max(0, level)));
  }
}