// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.completion.JSCompletionUtil
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.completion.JSLookupUtilImpl
import com.intellij.lang.javascript.completion.JSSmartCompletionContributor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.VueMethod
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.VueProperty
import java.util.*

class VueInsideTemplateCompletionContributor : JSSmartCompletionContributor() {
  override fun getSmartCompletionVariants(location: JSReferenceExpression): List<LookupElement>? {
    val basicVariants = super.getSmartCompletionVariants(location)

    val container = VueModelManager.findEnclosingContainer(location) ?: return basicVariants
    val vueVariants = ArrayList<LookupElement>()
    container.acceptPropertiesAndMethods(object : VueModelVisitor() {
      override fun visitProperty(property: VueProperty, proximity: Proximity): Boolean {
        add(property.name, property.source)
        return true
      }

      override fun visitMethod(method: VueMethod, proximity: Proximity): Boolean {
        add(method.name, method.source)
        return true
      }

      fun add(name: String, declaration: PsiElement?) {
        val builder = if (declaration == null)
          LookupElementBuilder.create(name)
        else
          JSLookupUtilImpl.createLookupElement(declaration, name)

        vueVariants.add(JSCompletionUtil.withJSLookupPriority(builder, JSLookupPriority.MAX_PRIORITY))
      }
    }, onlyPublic = false)
    return mergeVariants(basicVariants, vueVariants)
  }
}
