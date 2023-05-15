// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.navigation

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.navigation.GotoRelatedItem
import com.intellij.navigation.GotoRelatedProvider
import com.intellij.openapi.util.NlsContexts
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testIntegration.TestFinderHelper
import com.intellij.util.SmartList
import org.angular2.Angular2InjectionUtils.getFirstInjectedFile
import org.angular2.entities.Angular2Component
import org.angular2.entities.Angular2ComponentLocator.findComponentClasses
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil
import org.jetbrains.annotations.Nls

class Angular2GotoRelatedProvider : GotoRelatedProvider() {
  override fun getItems(psiElement: PsiElement): List<GotoRelatedItem> {
    val file = psiElement.containingFile
    if (file == null || !Angular2LangUtil.isAngular2Context(file)) {
      return emptyList()
    }
    val componentClasses: MutableList<TypeScriptClass> = SmartList()
    if (DialectDetector.isTypeScript(file)) {
      PsiTreeUtil.getParentOfType(psiElement, TypeScriptClass::class.java)?.let { componentClasses.add(it) }
      if (componentClasses.isEmpty()) {
        for (el in TestFinderHelper.findClassesForTest(file)) {
          if (el is JSFile) {
            componentClasses.addAll(PsiTreeUtil.getStubChildrenOfTypeAsList(el, TypeScriptClass::class.java))
          }
        }
      }
    }
    else {
      componentClasses.addAll(findComponentClasses(file))
    }
    val filter = getFirstInjectedFile(PsiTreeUtil.getParentOfType(psiElement, JSExpression::class.java)) ?: file
    val components = componentClasses.mapNotNull { Angular2EntitiesProvider.getComponent(it) }
    return when (components.size) {
      0 -> emptyList()
      1 -> getRelatedItems(components[0]).filter { filter != it.element?.containingFile }
      else -> components.map { GotoRelatedItem(it.typeScriptClass!!, groupName) }
    }
  }

  private class Angular2GoToRelatedItem(element: PsiElement,
                                        mnemonic: Int,
                                        inlineable: Boolean,
                                        private val myName: @NlsContexts.ListItem String?)
    : GotoRelatedItem(element, groupName, if (mnemonic > 9) -1 else mnemonic) {

    private val myContainerName: @Nls String?

    init {
      myContainerName = if (inlineable && InjectedLanguageManager.getInstance(element.project)
          .getTopLevelFile(element) !== element.containingFile) Angular2Bundle.message("angular.action.goto-related.inline")
      else null
    }

    override fun getCustomName(): String? {
      return myName
    }

    override fun getCustomContainerName(): String? {
      return myContainerName
    }
  }

  companion object {
    private const val COMPONENT_INDEX = 1
    private const val TEMPLATE_INDEX = 2
    private const val TEST_INDEX = 3
    private const val STYLES_INDEX_START = 4
    private const val MODULE_INDEX = 5
    private fun getRelatedItems(component: Angular2Component): List<GotoRelatedItem> {
      val result: MutableList<GotoRelatedItem> = SmartList()
      val cls = component.typeScriptClass
      if (cls != null && cls.name != null) {
        result.add(Angular2GoToRelatedItem(cls, COMPONENT_INDEX, false,
                                           Angular2Bundle.message("angular.action.goto-related.component-class")))
      }
      val file = component.templateFile
      if (file != null) {
        result.add(Angular2GoToRelatedItem(file, TEMPLATE_INDEX, true,
                                           Angular2Bundle.message("angular.action.goto-related.template")))
      }
      var first = true
      var count = 1
      val tests = TestFinderHelper.findTestsForClass(component.sourceElement)
      for (el in tests) {
        result.add(Angular2GoToRelatedItem(el, if (first) TEST_INDEX else -1, false,
                                           Angular2Bundle.message("angular.action.goto-related.tests",
                                                                  if (tests.size == 1) "" else " " + count++)))
        first = false
      }
      val cssFiles = component.cssFiles
      var mnemonic = STYLES_INDEX_START
      count = 1
      for (cssFile in cssFiles) {
        result.add(Angular2GoToRelatedItem(cssFile, mnemonic++, true,
                                           Angular2Bundle.message("angular.action.goto-related.styles",
                                                                  if (cssFiles.size == 1) "" else " " + count++)))
      }
      first = true
      for (moduleClass in component.allDeclaringModules.mapNotNull { it.typeScriptClass }) {
        if (moduleClass.name != null) {
          result.add(Angular2GoToRelatedItem(moduleClass, if (first) MODULE_INDEX else -1, false,
                                             Angular2Bundle.message("angular.action.goto-related.module")))
          first = false
        }
      }
      return result
    }

    private val groupName: @NlsContexts.Separator String
      get() = Angular2Bundle.message("angular.action.goto-related.group-name")
  }
}