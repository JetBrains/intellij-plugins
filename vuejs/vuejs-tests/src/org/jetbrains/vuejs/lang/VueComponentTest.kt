// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.psi.JSParameterTypeDecorator
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.vfs.VirtualFileFilter
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.psi.impl.PsiManagerImpl
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.webSymbols.DebugOutputPrinter
import com.intellij.webSymbols.checkTextByFile
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.model.*

/**
 * Checks highlighting, then checks the AST-based component model, then compares it with the Stub-based component model.
 * Runs the above both for TS default and strict modes.
 *
 * Reasons for this test to exist:
 * * in some cases we need to check how the Component Model contributes both file-internal & file-external references
 * * self-referenced component could be used, but it has some problems, e.g. it places warnings in a single line
 * * in some cases it makes sense to verify core JS/TS highlighting at the same time
 *
 * Loosely inspired by [Vue SFC Playground](https://sfc.vuejs.org/) that prepends analyzed bindings to output file.
 *
 * Example Vue script content:
 * ```ts
 * import { ref } from 'vue'
 * const msg = ref('Hello World!')
 * defineProps({a: String, b: Boolean});
 * ```
 *
 * Debug info of SFC compiler:
 * ```ts
 * /* Analyzed bindings: {
 *   "a": "props",
 *   "b": "props",
 *   "ref": "setup-const",
 *   "msg": "setup-ref"
 * } */
 * ```
 */
class VueComponentTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/component"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(VueInspectionsProvider())
  }

  fun testOptionsApiRuntimeDeclarationJS() = doTest()

  fun testOptionsApiRuntimeDeclarationPropTypeTS() = doTest()

  fun testOptionsApiRuntimeDeclarationArrayJS() = doTest()

  fun testDefinePropsRuntimeDeclarationTS() = doTest(true)

  fun testDefinePropsRuntimeDeclarationWithAssignmentTS() = doTest(true)

  fun testDefinePropsRuntimeDeclarationArrayTS() = doTest()

  fun testDefinePropsTypeDeclarationTS() = doTest(true)

  fun testDefinePropsTypeDeclarationWithAssignmentTS() = doTest(true)

  fun testDefinePropsTypeDeclarationInterfaceTS() = doTest(true)

  fun testDefinePropsTypeDeclarationTypeAliasTS() = doTest(true)

  fun testWithDefaultsTypeDeclarationTS() = doSingleStrictnessTest(false)
  fun testWithDefaultsTypeDeclarationTSNullChecks() = doSingleStrictnessTest(true)

  fun testWithDefaultsTypeDeclarationWithAssignmentTS() = doSingleStrictnessTest(false)
  fun testWithDefaultsTypeDeclarationWithAssignmentTSNullChecks() = doSingleStrictnessTest(true)

  fun testWithDefaultsTypeDeclarationPartialTS() = doTest()

  fun testWithDefaultsTypeDeclarationLocalReferencesTS() = doTest(true)

  fun testPropsDestructureTypeDeclarationTS() = doSingleStrictnessTest(false)
  fun testPropsDestructureTypeDeclarationTSNullChecks() = doSingleStrictnessTest(true)

  fun testPropsDestructureRuntimeDeclarationJS() = doTest()

  fun testBothScriptsJS() = doTest()

  fun testDefineEmits() = doTest()

  fun testDefineEmitsObjectLiteral() = doTest()

  fun testDefineEmitsExplicitType() = doTest()

  fun testDefineComponentWithEmits() = doTest()

  fun testScriptSetupGeneric() = doTest(true, addNodeModules = listOf(VueTestModule.VUE_3_3_0_ALPHA5))

  fun testPropsConstructorsAndGenerics() = doTest(true)

  /**
   * Runs `doTestInner` twice: once for default TS config, once for strict TS config
   */
  private fun doTest(strictNullChecksDiffer: Boolean = false, addNodeModules: List<VueTestModule> = listOf(VueTestModule.VUE_3_2_2)) {
    var file = configureTestProject(addNodeModules) as PsiFileImpl

    val textWithMarkers = file.text

    // first, check the component model with TSConfig(strictNullChecks=false)
    TypeScriptTestUtil.forceDefaultTsConfig(project, testRootDisposable)
    file = doTestInner(file, false)

    // reset file contents
    runWriteAction {
      PsiDocumentManager.getInstance(project).getDocument(file)!!.setText(textWithMarkers)
    }

    // check another time, with TSConfig(strictNullChecks=true)
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    PsiManager.getInstance(project).dropPsiCaches()
    file = doTestInner(file, strictNullChecksDiffer)
  }

  private fun doSingleStrictnessTest(strictNullChecks: Boolean, addNodeModules: List<VueTestModule> = listOf(VueTestModule.VUE_3_2_2)) {
    val file = configureTestProject(addNodeModules) as PsiFileImpl

    if (strictNullChecks) {
      TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    }
    else {
      TypeScriptTestUtil.forceDefaultTsConfig(project, testRootDisposable)
    }

    doTestInner(file, false)
  }

  /**
   * Checks highlighting, then checks the AST-based component model, then compares it with the Stub-based component model.
   */
  private fun doTestInner(_file: PsiFileImpl, appendSuffixToExpected: Boolean): PsiFileImpl {
    var file = _file

    file.node // ensure that the AST is loaded
    assertNull(file.stub)

    myFixture.checkHighlighting()

    val newModel = buildComponentModel(file)
    val expectedFile = "${getTestName(false)}.${if (appendSuffixToExpected) "strictNullChecks." else ""}expected.txt"
    myFixture.checkTextByFile(newModel, expectedFile)

    assertNull(file.stub)
    file = unloadAst(file)
    assertNotNull(file.stub)

    PsiManagerEx.getInstanceEx(project).setAssertOnFileLoadingFilter(VirtualFileFilter.ALL, testRootDisposable)
    assertNotNull(file.stub)
    PsiManagerEx.getInstanceEx(project).setAssertOnFileLoadingFilter(VirtualFileFilter.NONE, testRootDisposable)

    myFixture.checkTextByFile(buildComponentModel(file), expectedFile)

    return file
  }

  private fun buildComponentModel(file: PsiFile): String =
    ComponentModelDebugOutputPrinter(false)
      .printValue(VueModelManager.findEnclosingContainer(file))

  private fun configureTestProject(addNodeModules: List<VueTestModule> = emptyList(), extension: String = "vue"): PsiFile {
    if (addNodeModules.isNotEmpty()) {
      myFixture.configureVueDependencies(*addNodeModules.toTypedArray())
    }
    return myFixture.configureByFile(getTestName(false) + "." + extension)
  }

  /**
   * Inspired by SqlModelBuilderTest.unloadAst
   */
  private fun unloadAst(file: PsiFile): PsiFileImpl {
    val vFile = file.viewProvider.virtualFile
    (psiManager as PsiManagerImpl).cleanupForNextTest()
    val newFile = psiManager.findFile(vFile) as PsiFileImpl
    assertNull(newFile.treeElement)
    assertFalse(file.isValid)
    return newFile
  }

  private class ComponentModelDebugOutputPrinter(val printSources: Boolean) : DebugOutputPrinter() {
    override fun printValueImpl(builder: StringBuilder, level: Int, value: Any?): StringBuilder =
      when (value) {
        is VueSourceElement -> builder.printVueSourceElement(level, value)
        is VueModelDirectiveProperties -> builder.printVueModelDirectiveProperties(level, value)
        is VueTemplate<*> -> builder.printVueTemplate(level, value)
        is JSType -> builder.printJSType(level, value)
        is JSParameterTypeDecorator -> builder.printParameter(level, value)
        else -> super.printValueImpl(builder, level, value)
      }

    private fun StringBuilder.printVueTemplate(topLevel: Int, template: VueTemplate<*>): StringBuilder =
      printObject(topLevel) { level ->
        printProperty(level, "source", template.source)
      }

    private fun StringBuilder.printVueModelDirectiveProperties(topLevel: Int, model: VueModelDirectiveProperties): StringBuilder =
      printObject(topLevel) { level ->
        printProperty(level, "prop", model.prop)
        printProperty(level, "event", model.event)
      }

    private fun StringBuilder.printJSType(level: Int, type: JSType): StringBuilder =
      this.printValue(level, type.substitute().getTypeText(JSType.TypeTextFormat.PRESENTABLE))

    private fun StringBuilder.printParameter(topLevel: Int, param: JSParameterTypeDecorator): StringBuilder =
      printObject(topLevel) { level ->
        printProperty(level, "name", param.name)
        printProperty(level, "type", param.inferredType?.substitute()?.getTypeText(JSType.TypeTextFormat.PRESENTABLE))
      }

    private fun StringBuilder.printVueSourceElement(topLevel: Int, sourceElement: VueSourceElement): StringBuilder =
      printObject(topLevel) { level ->
        printProperty(level, "class", sourceElement.javaClass.simpleName)
        if (sourceElement is VueNamedSymbol)
          printProperty(level, "name", sourceElement.name)
        if (sourceElement is VueNamedEntity)
          printProperty(level, "defaultName", sourceElement.defaultName)
        if (sourceElement is VueDocumentedItem)
          printProperty(level, "description", sourceElement.description)
        if (printSources) {
          printProperty(level, "source", sourceElement.source)
          printProperty(level, "rawSource", sourceElement.rawSource.takeIf { it != sourceElement.source })
        }
        if (sourceElement is VueDirective) {
          printProperty(level, "jsType", sourceElement.jsType)
          printProperty(level, "modifiers", sourceElement.modifiers.takeIf { it.isNotEmpty() })
          printProperty(level, "argument", sourceElement.argument)
        }
        if (sourceElement is VueDirectiveArgument) {
          printProperty(level, "pattern", sourceElement.pattern)
          printProperty(level, "required", sourceElement.required)
        }
        if (sourceElement is VueDirectiveModifier) {
          printProperty(level, "pattern", sourceElement.pattern)
        }
        if (sourceElement is VueEntitiesContainer) {
          printProperty(level, "components", sourceElement.components.takeIf { it.isNotEmpty() }
            ?.filterOutLowercaseScriptSetupVariables()?.toSortedMap())
          printProperty(level, "directives", sourceElement.directives.takeIf { it.isNotEmpty() }?.toSortedMap())
          printProperty(level, "mixins", sourceElement.mixins.takeIf { it.isNotEmpty() })
        }
        if (sourceElement is VueContainer) {
          printProperty(level, "data", sourceElement.data.takeIf { it.isNotEmpty() }?.sortedWith(Comparator.comparing { it.name }))
          printProperty(level, "computed", sourceElement.computed.takeIf { it.isNotEmpty() }?.sortedWith(Comparator.comparing { it.name }))
          printProperty(level, "methods", sourceElement.methods.takeIf { it.isNotEmpty() }?.sortedWith(Comparator.comparing { it.name }))
          printProperty(level, "props", sourceElement.props.takeIf { it.isNotEmpty() }?.sortedWith(Comparator.comparing { it.name }))
          printProperty(level, "emits", sourceElement.emits.takeIf { it.isNotEmpty() }?.sortedWith(Comparator.comparing { it.name }))
          printProperty(level, "slots", sourceElement.slots.takeIf { it.isNotEmpty() }?.sortedWith(Comparator.comparing { it.name }))
          if (printSources) {
            printProperty(level, "template", sourceElement.template)
          }
          printProperty(level, "element", sourceElement.element)
          printProperty(level, "extends", sourceElement.extends.takeIf { it.isNotEmpty() })
          printProperty(level, "delimiters", sourceElement.delimiters)
          printProperty(level, "model", sourceElement.model?.takeIf { it.event != null && it.prop != null })
        }
        if (sourceElement is VueProperty) {
          printProperty(level, "jsType", sourceElement.jsType)
        }
        if (sourceElement is VueInputProperty) {
          printProperty(level, "required", sourceElement.required)
          printProperty(level, "defaultValue", sourceElement.defaultValue)
        }
        if (sourceElement is VueSlot) {
          printProperty(level, "scope", sourceElement.scope)
          printProperty(level, "pattern", sourceElement.pattern)
        }
        if (sourceElement is VueEmitCall) {
          printProperty(level, "eventJSType", sourceElement.eventJSType)
          printProperty(level, "params", sourceElement.params)
          printProperty(level, "hasStrictSignature", sourceElement.hasStrictSignature)
          printProperty(level, "callSignature", sourceElement.callSignature)
        }
      }
  }

}

private fun Map<String, VueComponent>.filterOutLowercaseScriptSetupVariables(): Map<String, VueComponent> =
  filter { (_, component) ->
    component.rawSource.let {
      it !is JSVariable
      || findModule(it, true) == null
      || it.name?.getOrNull(0)?.isUpperCase() == true
    }
  }


