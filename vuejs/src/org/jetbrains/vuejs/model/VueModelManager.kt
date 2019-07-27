// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.ecmascript6.psi.ES6ClassExpression
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSClass
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
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.codeInsight.refs.getContainingXmlFile
import org.jetbrains.vuejs.index.VueComponentsIndex
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.getVueIndexData
import org.jetbrains.vuejs.model.source.*
import org.jetbrains.vuejs.model.source.VueComponents.Companion.getExportedDescriptor

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
      return getContainingXmlFile(templateElement)
        ?.originalFile
        ?.let { getEnclosingComponentDescriptor(it) }
        ?.let { getComponent(it.obj ?: it.clazz!!) }
    }

    private fun getEnclosingComponentDescriptor(element: PsiElement): VueComponentDescriptor? {
      var context: PsiElement? = element
      if (context is JSProperty) {
        context = context.context
      }
      if (context is JSObjectLiteralExpression) {
        val parentContext = PsiTreeUtil.getContextOfType(context, JSObjectLiteralExpression::class.java,
                                                         ES6Decorator::class.java)
        if (parentContext is ES6Decorator) {
          context = parentContext
        }
      }

      return when (context) {
        is JSObjectLiteralExpression -> VueComponentDescriptor(obj = context)
        is ES6Decorator -> {
          val clazz = when (val parentContext = context.context?.context) {
            is JSExportAssignment -> parentContext.stubSafeElement as? JSClass<*>
            is JSClass<*> -> parentContext
            else -> null
          }
          val obj = VueComponents.getDescriptorFromDecorator(context)
          if (clazz != null || obj != null)
            VueComponentDescriptor(obj = obj, clazz = clazz)
          else
            null
        }
        is JSClass<*> -> {
          val decorator = VueComponents.getElementComponentDecorator(when (val decoratorContext = context.context) {
                                                                       is JSExportAssignment -> decoratorContext
                                                                       else -> context
                                                                     })
          VueComponentDescriptor(
            obj = decorator?.let { VueComponents.getDescriptorFromDecorator(it) },
            clazz = context)
        }
        else -> findModule(context)
          ?.let { content -> ES6PsiUtil.findDefaultExport(content) as? JSExportAssignment }
          ?.let { defaultExport -> getExportedDescriptor(defaultExport) }
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
        val implicitElement = getComponentImplicitElement(context)
        val data = implicitElement?.let { getVueIndexData(it) }
        val descriptor = getEnclosingComponentDescriptor(context)
        var declaration: PsiElement = descriptor?.obj ?: context
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
        CachedValueProvider.Result.create(VueSourceComponent(implicitElement ?: buildImplicitElement(context),
                                                             descriptor?.clazz,
                                                             declaration as? JSObjectLiteralExpression,
                                                             data),
                                          context, declaration)
      }
    }

    fun getMixin(mixin: JSObjectLiteralExpression): VueMixin {
      return getMixin(mixin as PsiElement)!!
    }

    fun getMixin(mixin: PsiElement): VueMixin? {
      val context = when (mixin) {
        is JSObjectLiteralExpression -> mixin
        is ES6ImportedBinding -> StreamEx.of(mixin.findReferencedElements())
          .select(JSExportAssignment::class.java)
          .map { getExportedDescriptor(it) }
          .nonNull()
          .map { it!!.clazz ?: it.obj!! }
          .findFirst()
          .orElse(null)
        else -> getEnclosingComponentDescriptor(mixin)?.let { it.clazz ?: it.obj!! }
                ?: return null
      }
      return CachedValuesManager.getCachedValue(context) {
        val descriptor = getEnclosingComponentDescriptor(context)
        val declaration: PsiElement = descriptor?.obj ?: context

        CachedValueProvider.Result.create(VueSourceMixin(getComponentImplicitElement(context)
                                                         ?: buildImplicitElement(context),
                                                         descriptor?.clazz,
                                                         descriptor?.obj),
                                          context, declaration)
      }
    }

    fun getApp(appDeclaration: JSObjectLiteralExpression): VueApp {
      return CachedValuesManager.getCachedValue(appDeclaration) {
        CachedValueProvider.Result.create(VueSourceApp(getComponentImplicitElement(appDeclaration)
                                                       ?: buildImplicitElement(appDeclaration),
                                                       appDeclaration),
                                          appDeclaration)
      }
    }

    private fun getComponentImplicitElement(declaration: PsiElement): JSImplicitElement? {
      return if (declaration is JSImplicitElement && declaration.userString == VueComponentsIndex.JS_KEY)
        declaration
      else if (declaration is JSObjectLiteralExpression) {
        val implicitElement = declaration.findProperty("name")
                                ?.let { getComponentImplicitElement(it) as? JSImplicitElementImpl }
                              ?: declaration.firstProperty
                                ?.let { buildImplicitElement(it) }
        var parent = PsiTreeUtil.getContextOfType(declaration, ES6ClassExpression::class.java, JSExportAssignment::class.java)
        if (parent is JSExportAssignment) {
          parent = parent.stubSafeElement as? ES6ClassExpression
        }
        if (parent is ES6ClassExpression)
          (implicitElement?.toBuilder() ?: JSImplicitElementImpl.Builder("<anonymous>", parent))
            .setProvider(parent)
            .forbidAstAccess()
            .toImplicitElement()
        else
          implicitElement
      }
      else if (declaration is ES6ClassExpression)
        buildImplicitElement(declaration, declaration.name ?: "<anonymous>")
      else
        (declaration as? JSImplicitElementProvider)?.indexingData?.implicitElements?.find {
          it.userString == VueComponentsIndex.JS_KEY
        }
    }

    private fun buildImplicitElement(parent: PsiElement, name: String = "<anonymous>") =
      JSImplicitElementImpl(JSImplicitElementImpl.Builder(name, parent).forbidAstAccess())

  }

}
