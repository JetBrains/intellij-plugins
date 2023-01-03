// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.naming

import com.intellij.lang.javascript.names.JSNameSuggestionsUtil
import com.intellij.lang.javascript.names.JSNamesSuggester
import com.intellij.lang.javascript.psi.JSNamedElement
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.PsiTreeUtil

class Angular2NamesSuggester : JSNamesSuggester {
  override fun suggestFileName(namedElement: JSNamedElement, newElementName: String): String? {
    return if (namedElement !is JSClass) null
    else getAngularSpecificFileName(
      namedElement, newElementName)
  }

  companion object {
    private val AngularDecoratorEntityMap = mapOf(
      "Component" to "Component",
      "Directive" to "Directive",
      "NgModule" to "Module",
      "Injectable" to "Service",
      "Pipe" to "Pipe",
    )

    private fun getAngularSpecificFileName(jsClass: JSClass, newElementName: String): String? {
      val decorators = PsiTreeUtil.getChildrenOfType(jsClass.attributeList, ES6Decorator::class.java) ?: return null
      for (decorator in decorators) {
        val referenceName = decorator.decoratorName ?: return null
        val entityName = AngularDecoratorEntityMap[referenceName]
        if (entityName != null) {
          val name = if (newElementName.endsWith(entityName)) {
            newElementName.substring(0, newElementName.length - entityName.length)
          }
          else {
            newElementName
          }
          val parts = name.split(JSNameSuggestionsUtil.SPLIT_BY_CAMEL_CASE_REGEX.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
          val finalName = StringUtil.join(parts, { str: String? -> StringUtil.toLowerCase(str) }, "-")
          return ((if (StringUtil.isEmpty(finalName)) "" else "$finalName.")
                  + StringUtil.toLowerCase(entityName))
        }
      }
      return null
    }
  }
}