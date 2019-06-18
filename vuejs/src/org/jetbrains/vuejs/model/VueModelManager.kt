// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClassExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
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
      return ModuleUtil.findModuleForPsiElement(context)?.let { getGlobal(it) }
    }

    fun getGlobal(module: Module): VueGlobal? {
      return CachedValuesManager.getManager(module.project).getCachedValue(module) {
        CachedValueProvider.Result.create(VueSourceGlobal(module), ModificationTracker.NEVER_CHANGED)
      }
    }

    fun findComponent(templateElement: PsiElement): VueComponent? {
      return (getGlobal(templateElement) as VueSourceGlobal?)?.findComponent(templateElement)
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
          JSImplicitElementImpl("<anonymous>", it)
        }
      else if (declaration is TypeScriptClassExpression)
        JSImplicitElementImpl("<anonymous>", declaration)
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
