// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.template

import com.intellij.lang.javascript.psi.JSPsiElementBase
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionSignature
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import org.angular2.codeInsight.Angular2ComponentPropertyResolveResult
import org.angular2.entities.Angular2ComponentLocator
import java.util.function.Consumer

class Angular2ComponentScopeProvider : Angular2TemplateScopesProvider() {

  override fun getScopes(element: PsiElement, hostElement: PsiElement?): List<Angular2TemplateScope> {
    return Angular2ComponentLocator.findComponentClass(element)
             ?.let { listOf(Angular2ComponentScope(it)) }
           ?: emptyList()
  }

  private class Angular2ComponentScope(private val myClass: TypeScriptClass) : Angular2TemplateScope(null) {

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      for (property in TypeScriptTypeParser.buildTypeFromClass(myClass, false).properties) {
        property.memberSource.allSourceElements
          .asSequence()
          .filterIsInstance<JSPsiElementBase>()
          .filter { it !is TypeScriptFunctionSignature && (it !is TypeScriptFunction || !it.isOverloadImplementation) }
          .forEach {
            consumer.accept(Angular2ComponentPropertyResolveResult(it, property))
          }
      }
    }
  }
}
