// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.ContainerUtil
import com.intellij.webSymbols.DebugOutputPrinter
import one.util.streamex.StreamEx
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.Angular2TestModule
import org.angular2.entities.*
import org.angular2.entities.Angular2EntitiesProvider.getEntity
import org.angular2.entities.Angular2EntitiesProvider.getModule
import org.angular2.web.Angular2Symbol
import org.angularjs.AngularTestUtil
import java.util.*

class ModulesTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "codeInsight/modules"
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

  fun testHostDirectives() {
    doResolutionTest(
      "host-directives",
      "app.module.ts",
      "export class App<caret>Module {",
      "check.txt",
      true,
      Angular2TestModule.ANGULAR_CORE_16_0_0_NEXT_4, Angular2TestModule.ANGULAR_COMMON_16_0_0_NEXT_4
    )
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
    val result = Angular2EntitiesDebugOutputPrinter(printDirectives).printValue(module)
    myFixture.configureByText("__my-check.txt", result)
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

  private class Angular2EntitiesDebugOutputPrinter(val printDirectives: Boolean) : DebugOutputPrinter() {

    private val printedEntities = mutableSetOf<Angular2Entity>()

    init {
      indent = "  "
    }

    @Suppress("UNCHECKED_CAST")
    override fun printValueImpl(builder: StringBuilder, level: Int, value: Any?): StringBuilder =
      when (value) {
        is Angular2Module -> builder.printNgModule(level, value)
        is Angular2Directive -> builder.printNgDirective(level, value)
        is Angular2Pipe -> builder.printNgPipe(level, value)
        is Angular2HostDirective -> builder.printNgHostDirective(level, value)
        is Collection<*> -> super.printValueImpl(builder, level, when (value.firstOrNull()) {
          is Angular2Symbol -> (value as Collection<Angular2Symbol>).sortedBy { it.name }
          is Angular2Entity -> (value as Collection<Angular2Entity>).sortedBy { it.getName() }
          else -> value.toList()
        })
        is Map<*, *> -> super.printValueImpl(builder, level, value.toSortedMap(Comparator.comparing { it.toString() }))
        else -> super.printValueImpl(builder, level, value)
      }

    override fun printRecursiveValue(builder: StringBuilder, level: Int, value: Any): java.lang.StringBuilder =
      if (value is Angular2Entity || value is Collection<*>)
        printValueImpl(builder, level, value)
      else
        super.printRecursiveValue(builder, level, value)

    private fun StringBuilder.printNgModule(topLevel: Int, module: Angular2Module): StringBuilder =
      printEntity(topLevel, module) { level ->
        printProperty(level, "imports", module.imports)
        printProperty(level, "declarations", module.declarations)
        printProperty(level, "exports", module.exports)
        printProperty(level, "all-exported-declarations", module.allExportedDeclarations)
        printProperty(level, "scope", module.declarationsInScope)
        printProperty(level, "scope fully resolved", module.isScopeFullyResolved)
        printProperty(level, "exports fully resolved", module.areExportsFullyResolved())
        printProperty(level, "declarations fully resolved", module.areDeclarationsFullyResolved())
      }


    private fun StringBuilder.printNgDirective(topLevel: Int, directive: Angular2Directive): StringBuilder =
      printEntity(topLevel, directive) { level ->
        if (printDirectives) {
          printProperty(level, "standalone", directive.isStandalone)
          printProperty(level, "selector", directive.selector)
          printProperty(level, "kind", directive.directiveKind)
          printProperty(level, "exportAs",
                        directive.exportAs.mapValues { (_, value) ->
                          Identifier(if (value.directive === directive) "<this>" else value.directive.getName())
                        }.takeIf { it.isNotEmpty() })
          printProperty(level, "inputs", directive.inputs.takeIf { it.isNotEmpty() })
          printProperty(level, "outputs", directive.outputs.takeIf { it.isNotEmpty() })
          printProperty(level, "inOuts", directive.inOuts.takeIf { it.isNotEmpty() })
          printProperty(level, "attributes", directive.attributes.takeIf { it.isNotEmpty() })
          printProperty(level, "host directives", directive.hostDirectives.takeIf { it.isNotEmpty() })
          printProperty(level, "host directives fully resolved", directive.areHostDirectivesFullyResolved())
        }
        if (directive.isStandalone && directive is Angular2ImportsOwner) {
          printProperty(level, "imports", directive.imports)
          printProperty(level, "scope", directive.declarationsInScope)
          printProperty(level, "scope fully resolved", directive.isScopeFullyResolved)
        }
      }

    private fun StringBuilder.printNgPipe(topLevel: Int, pipe: Angular2Pipe): StringBuilder =
      printEntity(topLevel, pipe) {}

    private fun StringBuilder.printNgHostDirective(level: Int, directive: Angular2HostDirective): StringBuilder {
      directive.directive?.apply {
        append(getName())
          .append('\n')
      } ?: append("<unresolved>")

      printProperty(level + 1, "host-inputs", directive.inputs.takeIf { it.isNotEmpty() })
      printProperty(level + 1, "host-outputs", directive.outputs.takeIf { it.isNotEmpty() })
      indent(level + 1)
      directive.directive?.let { printNgDirective(level + 1, it) }
      return this
    }

    private fun StringBuilder.printEntity(level: Int,
                                          entity: Angular2Entity,
                                          printer: (level: Int) -> Unit): StringBuilder {
      append(entity.getName())
        .append(": ")
        .append(entity.javaClass.getSimpleName())
        .append('\n')
      if (!printedEntities.add(entity)) {
        if (entity !is Angular2Pipe
            && (entity !is Angular2Directive || printDirectives || (entity.isStandalone && entity is Angular2ImportsOwner))) {
          indent(level + 1).append("<printed above>\n")
        }
      }
      else {
        printer(level + 1)
      }
      return this
    }

    override fun StringBuilder.printMap(level: Int, map: Map<*, *>): StringBuilder {
      append("\n")
      for (entry in map) {
        printProperty(level + 1, entry.key.toString(), entry.value)
      }
      return this
    }

    override fun StringBuilder.printList(level: Int, list: List<*>): StringBuilder {
      append('\n')
      list.forEach {
        indent(level + 1).printValue(level + 1, it)
        if (!endsWith('\n'))
          append("\n")
      }
      return this
    }

    override fun StringBuilder.printProperty(level: Int, name: String, value: Any?): StringBuilder {
      if (value == null) return this
      indent(level).append(name).append(": ")
        .printValue(level, value)
      if (!endsWith('\n')) {
        append("\n")
      }
      return this
    }

    private class Identifier(val identifier: String) {
      override fun toString(): String =
        identifier
    }

  }

}
