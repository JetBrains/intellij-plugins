// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.vuejs.index.VueComponentsIndex
import org.jetbrains.vuejs.index.getVueIndexData
import org.jetbrains.vuejs.model.source.VueSourceComponent
import org.jetbrains.vuejs.model.source.VueSourceGlobal

class VueModelManager {

  companion object {

    fun getGlobal(context: PsiElement): VueGlobal? {
      val m = ModuleUtil.findModuleForPsiElement(context) ?: return null
      return CachedValuesManager.getManager(m.project).getCachedValue(m) {
        CachedValueProvider.Result.create(VueSourceGlobal(m), ModificationTracker.NEVER_CHANGED)
      }
    }

    fun findComponent(templateElement: PsiElement): VueComponent? {
      return (getGlobal(templateElement) as VueSourceGlobal?)?.findComponent(templateElement)
    }

    fun getAllComponents(context: PsiElement): Map<String, VueComponent> {
      return emptyMap()
    }

    fun getComponent(declaration: PsiElement): VueComponent? {
      val implicitElement: JSImplicitElement? = getComponentImplicitElement(declaration)
      val data = implicitElement?.let { getVueIndexData(it) }
      val context: PsiElement = implicitElement?.context ?: declaration

      return CachedValuesManager.getCachedValue(context) {
        CachedValueProvider.Result.create(VueSourceComponent(context, data), context)
      }
    }

    fun getComponentImplicitElement(declaration: PsiElement): JSImplicitElement? {
      return if (declaration is JSImplicitElement && declaration.userString == VueComponentsIndex.JS_KEY)
        declaration
      else
        (declaration as? JSImplicitElementProvider)?.indexingData?.implicitElements?.find {
          it.userString == VueComponentsIndex.JS_KEY
        }
    }

  }

}
