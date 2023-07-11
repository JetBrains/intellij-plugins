// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.ContainerUtil
import one.util.streamex.StreamEx
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.entities.*
import org.angular2.entities.Angular2EntitiesProvider.getEntity
import org.angular2.entities.Angular2EntitiesProvider.getModule
import org.angular2.modules.Angular2TestModule
import org.angular2.web.Angular2Symbol
import org.angularjs.AngularTestUtil
import java.util.*
import kotlin.math.max

class ModulesTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass) + "modules"
  }

  fun testCommonModuleResolution() {
    doResolutionTest("common",
                     "common_module.ts",
                     "export class Common<caret>Module",
                     "check.txt")
  }

  fun testCommonModuleResolutionMetadata() {
    doResolutionTest("common-metadata",
                     "myModule.ts",
                     "export class Common<caret>ModuleMetadataTest {",
                     "check.txt")
  }

  fun testRouterModuleResolution() {
    doResolutionTest("router",
                     "myModule.ts",
                     "class AppRouting<caret>Module {",
                     "check-full.txt")
  }

  fun testRouterModuleResolutionMetadata() {
    doResolutionTest("router-metadata",
                     "myModule.ts",
                     "class AppRouting<caret>Module {",
                     "check-full.txt")
  }

  fun testRouterModuleResolutionNotFull() {
    doResolutionTest("router",
                     "myModule.ts",
                     "export class AppRoutingModule<caret>NotFullyResolved {",
                     "check-not-full.txt")
  }

  fun testRouterModuleResolutionNotFullMetadata() {
    doResolutionTest("router-metadata",
                     "myModule.ts",
                     "export class AppRoutingModule<caret>NotFullyResolved {",
                     "check-not-full.txt")
  }

  fun testBrowserModuleResolutionNotFull() {
    doResolutionTest("browser",
                     "myModule.ts",
                     "class BrowserModule<caret>Test {",
                     "check.txt")
  }

  fun testIonicResolutionMetadata() {
    doResolutionTest("ionic-metadata",
                     "myIonicModule.ts",
                     "export class MyIonic<caret>Module {",
                     "check-no-common.txt")
  }

  fun testIonicResolutionMetadataWithCommon() {
    myFixture.copyDirectoryToProject("common-metadata/common", "/common")
    doResolutionTest("ionic-metadata",
                     "myIonicModule.ts",
                     "export class MyIonic<caret>Module {",
                     "check-with-common.txt")
  }

  fun testSourceForRootResolution() {
    doResolutionTest("source-forRoot",
                     "mainModule.ts",
                     "export class Main<caret>Module {",
                     "check.txt", Angular2TestModule.ANGULAR_CORE_8_2_14)
  }

  fun testJsonMetadataForRootResolution() {
    doResolutionTest("metadata-forRoot",
                     "mainModule.ts",
                     "export class Main<caret>Module {",
                     "check.txt", Angular2TestModule.ANGULAR_CORE_8_2_14)
  }

  fun testIvyMetadataForRootResolution() {
    doResolutionTest("ivy-forRoot",
                     "mainModule.ts",
                     "export class Main<caret>Module {",
                     "check.txt", Angular2TestModule.ANGULAR_CORE_8_2_14)
  }

  fun testFormsResolution() {
    doResolutionTest("forms",
                     "myModule.ts",
                     "export class FormsModuleMetadata<caret>Test {",
                     "check.txt", Angular2TestModule.ANGULAR_FORMS_8_2_14)
  }

  fun testNgModuleWithConstant() {
    doResolutionTest("ng-module-with-constant",
                     "module.ts",
                     "export class My<caret>Module {",
                     "check.txt")
  }

  fun testAgmCore() {
    doResolutionTest("agm-core",
                     "module.ts",
                     "export class Main<caret>Module {",
                     "check.txt", Angular2TestModule.AGM_CORE_1_0_0_BETA_5)
  }

  fun testFunctionCalls() {
    doResolutionTest("function-calls",
                     "my-test-lib.module.ts",
                     "MyTestLib<caret>Module",
                     "check.txt")
  }

  fun testEvoUiKit() {
    doResolutionTest("evo-ui-kit",
                     "module.ts",
                     "export class Main<caret>Module {",
                     "check.txt", Angular2TestModule.EVO_UI_KIT_1_17_0)
  }

  fun testCommonNgClassModules() {
    doDeclarationModulesCheckText("common",
                                  "directives/ng_class.ts",
                                  "export class Ng<caret>Class ",
                                  "CommonModule")
  }

  fun testCommonDatePipeModules() {
    doDeclarationModulesCheckText("common",
                                  "pipes/date_pipe.ts",
                                  "export class Date<caret>Pipe ",
                                  "CommonModule")
  }

  fun testAsyncPipeModulesMetadata() {
    doDeclarationModulesCheckText("common-metadata",
                                  "common/src/pipes/async_pipe.d.ts",
                                  "export declare class Async<caret>Pipe ",
                                  "CommonModule",
                                  "CommonModuleMetadataTest")
  }

  fun testCommonNg12() {
    doResolutionTest("ng12-common",
                     "mainModule.ts",
                     "export class App<caret>Module {",
                     "check.txt")
  }

  fun testCommonNg13() {
    doResolutionTest("ng13-common",
                     "app.module.ts",
                     "export class App<caret>Module {",
                     "check.txt")
  }

  fun testPrivateModuleExportMetadata() {
    doResolutionTest("private-module-export-metadata",
                     "module.ts",
                     "export class Amount<caret>Module {",
                     "check.txt", Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.NGXS_STORE_3_6_2)
  }

  fun testPrivateModuleExportIvy() {
    doResolutionTest("private-module-export-ivy",
                     "module.ts",
                     "export class Amount<caret>Module {",
                     "check.txt", Angular2TestModule.ANGULAR_CORE_9_1_1_MIXED, Angular2TestModule.NGXS_STORE_3_6_2_MIXED)
  }

  fun testModuleReexport() {
    doResolutionTest("module-reexport",
                     "app.module.ts",
                     "export class App<caret>Module {",
                     "check.txt", Angular2TestModule.ANGULAR_CORE_9_1_1_MIXED)
  }

  fun testRequiredProperties() {
    doResolutionTest("required-properties",
                     "app.module.ts",
                     "export class App<caret>Module {",
                     "check.txt",
                     true, Angular2TestModule.ANGULAR_CORE_16_0_0_NEXT_4, Angular2TestModule.ANGULAR_COMMON_16_0_0_NEXT_4)
  }

  private fun doResolutionTest(directory: String,
                               moduleFile: String,
                               signature: String,
                               checkFile: String,
                               vararg modules: Angular2TestModule) {
    doResolutionTest(directory, moduleFile, signature, checkFile, false, *modules)
  }

  private fun doResolutionTest(directory: String,
                               moduleFile: String,
                               signature: String,
                               checkFile: String,
                               printDirectives: Boolean,
                               vararg modules: Angular2TestModule) {
    val testDir = myFixture.copyDirectoryToProject(directory, "/")
    Angular2TestModule.configureCopy(myFixture, *modules)
    myFixture.openFileInEditor(testDir.findFileByRelativePath(moduleFile)!!)
    val moduleOffset = AngularTestUtil.findOffsetBySignature(signature, myFixture.getFile())
    val el = myFixture.getFile().findElementAt(moduleOffset)!!
    val moduleClass = PsiTreeUtil.getParentOfType(el, TypeScriptClass::class.java)!!
    val module = getModule(moduleClass)!!
    val result = StringBuilder()
    printEntity(0, module, result, printDirectives, HashSet())
    myFixture.configureByText("__my-check.txt", result.toString())
    myFixture.checkResultByFile("$directory/$checkFile", true)
  }

  private fun doDeclarationModulesCheckText(directory: String,
                                            declarationFile: String,
                                            signature: String,
                                            vararg modules: String) {
    val testDir = myFixture.copyDirectoryToProject(directory, "/")
    Angular2TestModule.configureLink(myFixture)
    myFixture.openFileInEditor(testDir.findFileByRelativePath(declarationFile)!!)
    val moduleOffset = AngularTestUtil.findOffsetBySignature(signature, myFixture.getFile())
    val el = myFixture.getFile().findElementAt(moduleOffset)!!
    val declarationClass = PsiTreeUtil.getParentOfType(el, TypeScriptClass::class.java)!!
    val declaration = (getEntity(declarationClass) as Angular2Declaration?)!!
    assertEquals(ContainerUtil.sorted(Arrays.asList(*modules)) { obj: String, str: String? ->
      obj.compareTo(
        str!!, ignoreCase = true)
    },
                 StreamEx.of(declaration.allDeclaringModules)
                   .map { m: Angular2Module -> m.getName() }
                   .sorted { obj: String, str: String? -> obj.compareTo(str!!, ignoreCase = true) }
                   .toList())
  }

  companion object {
    private fun printEntity(level: Int,
                            entity: Angular2Entity,
                            result: StringBuilder,
                            printDirectives: Boolean,
                            printed: MutableSet<Angular2Entity>) {
      var level = level
      withIndent(level, result)
        .append(entity.getName())
        .append(": ")
        .append(entity.javaClass.getSimpleName())
        .append('\n')
      if (entity is Angular2Module) {
        if (!printed.add(entity)) {
          withIndent(level + 1, result)
            .append("<printed above>\n")
          return
        }
        level++
        printEntityList(level, "imports", entity.imports, printDirectives, result, printed)
        printEntityList(level, "declarations", entity.declarations, printDirectives, result, printed)
        printEntityList(level, "exports", entity.exports, printDirectives, result, printed)
        printEntityList(level, "all-exported-declarations", entity.allExportedDeclarations, printDirectives, result, printed)
        printEntityList(level, "scope", entity.declarationsInScope, printDirectives, result, printed)
        withIndent(level, result)
          .append("scope fully resolved: ")
          .append(entity.isScopeFullyResolved)
          .append('\n')
        withIndent(level, result)
          .append("exports fully resolved: ")
          .append(entity.areExportsFullyResolved())
          .append('\n')
        withIndent(level, result)
          .append("declarations fully resolved: ")
          .append(entity.areDeclarationsFullyResolved())
          .append('\n')
      }
      else if (entity is Angular2Directive) {
        if ((printDirectives || entity.isStandalone) && !printed.add(entity)) {
          withIndent(level + 1, result)
            .append("<printed above>\n")
          return
        }
        if (printDirectives) {
          level++
          withIndent(level, result)
            .append("standalone: ")
            .append(entity.isStandalone)
            .append("\n")
          withIndent(level, result)
            .append("selector: ")
            .append(entity.selector)
            .append("\n")
          withIndent(level, result)
            .append("kind: ")
            .append(entity.directiveKind)
            .append("\n")
          if (!entity.exportAsList.isEmpty()) {
            withIndent(level, result)
              .append("exportAs list: ")
              .append(entity.exportAsList)
              .append("\n")
          }
          printSymbolList(level, "inputs", entity.inputs, result)
          printSymbolList(level, "outputs", entity.outputs, result)
          printSymbolList(level, "inOuts", entity.inOuts, result)
          printSymbolList(level, "attributes", entity.attributes, result)
        }
        if (entity.isStandalone && entity is Angular2ImportsOwner) {
          printEntityList(level, "imports", entity.imports, printDirectives, result, printed)
          printEntityList(level, "scope", entity.declarationsInScope, printDirectives, result, printed)
          withIndent(level, result)
            .append("scope fully resolved: ")
            .append(entity.isScopeFullyResolved)
            .append('\n')
        }
      }
    }

    private fun printEntityList(level: Int,
                                name: String,
                                entities: Set<Angular2Entity>,
                                printDirectives: Boolean,
                                result: StringBuilder,
                                printed: MutableSet<Angular2Entity>) {
      withIndent(level, result)
        .append(name)
        .append(":\n")
      ContainerUtil.sorted(entities, Comparator.comparing { obj: Angular2Entity -> obj.getName() })
        .forEach { m: Angular2Entity -> printEntity(level + 1, m, result, printDirectives, printed) }
    }

    private fun printSymbolList(level: Int,
                                name: String,
                                symbols: Collection<Angular2Symbol?>,
                                result: StringBuilder) {
      if (symbols.isEmpty()) return
      withIndent(level, result)
        .append(name)
        .append(":\n")
      ContainerUtil.sorted(symbols, Comparator.comparing(Angular2Symbol::name))
        .forEach { m: Angular2Symbol? -> withIndent(level + 1, result).append(m).append("\n") }
    }

    private fun withIndent(level: Int, result: StringBuilder): StringBuilder {
      return result.append("  ".repeat(max(0.0, level.toDouble()).toInt()))
    }
  }
}
