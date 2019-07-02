// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClassExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.codeInsight.refs.findScriptWithExport
import org.jetbrains.vuejs.index.VueComponentsIndex
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.getVueIndexData
import org.jetbrains.vuejs.model.source.VueComponents
import org.jetbrains.vuejs.model.source.VueSourceComponent
import org.jetbrains.vuejs.model.source.VueSourceGlobal
import org.jetbrains.vuejs.model.source.VueSourceMixin

class VueModelManager {

  companion object {

    fun getGlobal(context: PsiElement): VueGlobal? {
      return getGlobal(context.project)
    }

    fun getGlobal(project: Project): VueGlobal? {
      return CachedValuesManager.getManager(project).getCachedValue(project) {
        CachedValueProvider.Result.create(VueSourceGlobal(project), ModificationTracker.NEVER_CHANGED)
      }
    }

    fun findEnclosingContainer(templateElement: PsiElement): VueEntitiesContainer? {
      return findComponent(templateElement) as? VueEntitiesContainer
             ?: findVueApp(templateElement)
             ?: getGlobal(templateElement)
    }

    private fun findComponent(templateElement: PsiElement): VueComponent? {
      return templateElement.containingFile.originalFile.let { templateFile ->
        (findModule(templateFile)
           ?.let { content -> ES6PsiUtil.findDefaultExport(content) as? JSExportAssignment }
           ?.let { defaultExport -> VueComponents.getExportedDescriptor(defaultExport) }
           ?.obj
         ?: findScriptWithExport(templateFile)?.second?.stubSafeElement as? JSObjectLiteralExpression
        )?.let { getComponent(it) }
      }
    }

    private fun findVueApp(templateElement: PsiElement): VueApp? {
      val global = getGlobal(templateElement) ?: return null
      val xmlElement = if (templateElement is XmlElement)
        templateElement
      else
        InjectedLanguageManager.getInstance(templateElement.project).getInjectionHost(templateElement)
        ?: return null
      var result: VueApp? = null
      PsiTreeUtil.findFirstParent(xmlElement, Condition {
        if (it is PsiFile) return@Condition true
        val idValue = (it as? XmlTag)?.getAttribute("id")?.valueElement?.value
                      ?: return@Condition false
        if (!StringUtil.isEmptyOrSpaces(idValue)) {
          val idReference = "#$idValue"
          global.apps.find { app -> idReference == app.element }?.let { app ->
            result = app
            return@Condition true
          }
        }
        false
      })
      return result
    }

    fun getComponent(element: PsiElement): VueComponent? {
      val context: PsiElement = getComponentImplicitElement(element)?.context ?: element
      if (!context.isValid)
        return null
      return CachedValuesManager.getCachedValue(context) {
        val data = getComponentImplicitElement(context)?.let { getVueIndexData(it) }
        var declaration: PsiElement = findModule(context)
                                        ?.let { content -> ES6PsiUtil.findDefaultExport(content) as? JSExportAssignment }
                                        ?.let { defaultExport -> VueComponents.getExportedDescriptor(defaultExport) }
                                        ?.obj
                                      ?: context
        if (declaration is JSImplicitElement) {
          declaration = declaration.context ?: declaration
        }
        if (declaration is JSProperty) {
          declaration = declaration.parent ?: declaration
        }
        else if (declaration is JSCallExpression) {
          data?.descriptorRef
            ?.let { VueComponents.resolveReferenceToVueComponent(context, it) }
            ?.obj
            ?.let { declaration = it }
        }
        CachedValueProvider.Result.create(VueSourceComponent(context, declaration as? JSObjectLiteralExpression, data), context,
                                          declaration)
      }
    }

    fun getComponentImplicitElement(declaration: PsiElement): JSImplicitElement? {
      return if (declaration is JSImplicitElement && declaration.userString == VueComponentsIndex.JS_KEY)
        declaration
      else if (declaration is JSObjectLiteralExpression)
        declaration.findProperty("name")?.let { getComponentImplicitElement(it) } ?: declaration.firstProperty?.let {
          JSImplicitElementImpl(JSImplicitElementImpl.Builder("<anonymous>", it).forbidAstAccess())
        }
      else if (declaration is TypeScriptClassExpression)
        JSImplicitElementImpl(JSImplicitElementImpl.Builder("<anonymous>", declaration).forbidAstAccess())
      else
        (declaration as? JSImplicitElementProvider)?.indexingData?.implicitElements?.find {
          it.userString == VueComponentsIndex.JS_KEY
        }
    }

    fun getMixin(mixinDeclaration: JSObjectLiteralExpression): VueMixin {
      return CachedValuesManager.getCachedValue(mixinDeclaration) {
        CachedValueProvider.Result.create(VueSourceMixin(mixinDeclaration), mixinDeclaration)
      }
    }

  }

}
