// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.templateLoader

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.vfs.VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.xml.XmlFile
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.libraries.templateLoader.TemplateLoaderFrameworkHandler.Companion.WITH_RENDER
import org.jetbrains.vuejs.model.VueFileTemplate
import org.jetbrains.vuejs.model.VueTemplate
import org.jetbrains.vuejs.model.source.VueSourceEntityDescriptor
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider.VueContainerInfo

class TemplateLoaderComponentInfoProvider : VueContainerInfoProvider {

  override fun getInfo(descriptor: VueSourceEntityDescriptor): VueContainerInfo? {
    return when {
      descriptor.clazz != null -> TemplateLoaderClassInfo(descriptor.clazz)
      descriptor.initializer != null -> TemplateLoaderInitializerInfo(descriptor.initializer)
      else -> null
    }
  }

  private class TemplateLoaderClassInfo(private val clazz: JSClass) : VueContainerInfo {
    override val template: VueTemplate<*>?
      get() = CachedValuesManager.getCachedValue(clazz) {
        clazz.attributeList
          ?.decorators
          ?.asSequence()
          ?.plus(clazz.parent.castSafelyTo<ES6ExportDefaultAssignment>()
                   ?.attributeList?.decorators ?: emptyArray<ES6Decorator>())
          ?.find { it.decoratorName?.equals(WITH_RENDER, ignoreCase = true) == true }
          ?.expression
          ?.let { resolveToFile(it) }
          ?.let { CachedValueProvider.Result.create(VueFileTemplate(it), clazz, it) }
        ?: CachedValueProvider.Result.create(null as VueFileTemplate?, clazz, VFS_STRUCTURE_MODIFICATIONS)
      }

    override fun equals(other: Any?): Boolean {
      return (other as? TemplateLoaderClassInfo)?.clazz == clazz
    }

    override fun hashCode(): Int {
      return clazz.hashCode()
    }
  }

  private class TemplateLoaderInitializerInfo(private val initializer: JSObjectLiteralExpression) : VueContainerInfo {
    override val template: VueTemplate<*>?
      get() = CachedValuesManager.getCachedValue(initializer) {
        initializer.context
          ?.let {
            when (it) {
              is JSArgumentList -> it.context as? JSCallExpression
              is JSCallExpression -> it
              else -> null
            }
          }
          ?.let { resolveToFile(it) }
          ?.let { CachedValueProvider.Result.create(VueFileTemplate(it), initializer, it) }
        ?: CachedValueProvider.Result.create(null as VueFileTemplate?, initializer, VFS_STRUCTURE_MODIFICATIONS)
      }

    override fun equals(other: Any?): Boolean {
      return (other as? TemplateLoaderInitializerInfo)?.initializer == initializer
    }

    override fun hashCode(): Int {
      return initializer.hashCode()
    }
  }

  companion object {
    private fun resolveToFile(expression: JSExpression): XmlFile? {
      return when (expression) {
        is JSCallExpression -> expression.methodExpression as? JSReferenceExpression
        is JSReferenceExpression -> expression
        else -> null
      }
        ?.takeIf { it.referenceName.equals(WITH_RENDER, ignoreCase = true) }
        ?.resolve()
        ?.let {
          JSStubBasedPsiTreeUtil.calculateMeaningfulElements(it)
        }
        ?.find { it is XmlFile }
        ?.castSafelyTo<XmlFile>()
    }
  }
}
