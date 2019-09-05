// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.template

import com.intellij.lang.javascript.psi.JSPsiElementBase
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.ResolveResult
import com.intellij.util.Processor
import org.jetbrains.vuejs.model.*
import java.util.function.Consumer

class VueContainerScopeProvider : VueTemplateScopesProvider() {

  override fun getScopes(element: PsiElement, hostElement: PsiElement?): List<VueTemplateScope> {
    return VueModelManager.findEnclosingContainer(element)
             ?.let { listOf(VueContainerScope(it)) }
           ?: emptyList()
  }

  private class VueContainerScope constructor(private val myEntitiesContainer: VueEntitiesContainer) : VueTemplateScope(null) {

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      throw UnsupportedOperationException()
    }

    override fun process(processor: Processor<in ResolveResult>): Boolean {
      var continueProcessing = true
      myEntitiesContainer.acceptPropertiesAndMethods(object : VueModelProximityVisitor() {
        override fun visitProperty(property: VueProperty, proximity: Proximity): Boolean {
          return process(property, proximity)
        }

        override fun visitMethod(method: VueMethod, proximity: Proximity): Boolean {
          return process(method, proximity)
        }

        private fun process(namedSymbol: VueNamedSymbol, proximity: Proximity): Boolean {
          val source = namedSymbol.source
          val resolveResult = PsiElementResolveResult(
            if (source is JSPsiElementBase)
              source
            else
              JSImplicitElementImpl
                .Builder(namedSymbol.name, source)
                .forbidAstAccess()
                .toImplicitElement())
          continueProcessing = processor.process(resolveResult) && continueProcessing
          return acceptSameProximity(proximity, !continueProcessing) {}
        }

      }, onlyPublic = false)
      return continueProcessing
    }
  }
}
