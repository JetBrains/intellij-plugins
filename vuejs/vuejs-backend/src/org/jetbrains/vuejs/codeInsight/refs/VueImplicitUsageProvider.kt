// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.refs

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.javascript.web.js.WebJSResolveUtil
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSFunctionItem
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.source.VueSourceComponent

class VueImplicitUsageProvider : ImplicitUsageProvider {
  override fun isImplicitUsage(element: PsiElement): Boolean {
    if (element is ES6ExportDefaultAssignment) {
      return element.context == findModule(element, false)
    }
    else if (element is JSProperty || element is JSFunctionItem) {
      val component = VueModelManager.findEnclosingComponent(element as JSElement)
      val descriptor = (component as? VueSourceComponent)?.descriptor
      val name = element.name
      if (name != null && descriptor != null
          && (descriptor.clazz == element.context
              || descriptor.initializer == element.context)) {
        return WebJSResolveUtil.resolveSymbolFromNodeModule(
          element, VUE_MODULE, "ComponentOptions", JSClass::class.java)
          ?.jsType?.asRecordType()?.findPropertySignature(name) != null
      }
    }
    return false
  }

  override fun isImplicitRead(element: PsiElement): Boolean = false

  override fun isImplicitWrite(element: PsiElement): Boolean = false
}