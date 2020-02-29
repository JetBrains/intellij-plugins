// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.template

import com.intellij.lang.javascript.psi.JSPsiElementBase
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.ResolveResult
import com.intellij.util.Processor
import org.jetbrains.vuejs.codeInsight.resolveSymbolFromNodeModule
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VUE_NAMESPACE
import java.util.function.Consumer

class VueContainerScopeProvider : VueTemplateScopesProvider() {

  override fun getScopes(element: PsiElement, hostElement: PsiElement?): List<VueTemplateScope> {
    return VueModelManager.findEnclosingContainer(element)
             ?.let { listOf(VueContainerScope(it)) }
           ?: emptyList()
  }

  private class VueContainerScope constructor(private val myEntitiesContainer: VueEntitiesContainer)
    : VueTemplateScope(VueDefaultInstanceScope(myEntitiesContainer)) {

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

  private class VueDefaultInstanceScope constructor(private val myEntitiesContainer: VueEntitiesContainer) : VueTemplateScope(null) {

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      val source = myEntitiesContainer.source ?: return
      resolveSymbolFromNodeModule(source, VUE_MODULE, VUE_NAMESPACE, TypeScriptInterface::class.java)
        ?.jsType
        ?.asRecordType()
        ?.properties
        ?.let { props ->
          props.asSequence()
            .mapNotNull { it.memberSource.singleElement }
            .map { PsiElementResolveResult(it, true) }
            .forEach { consumer.accept(it) }
          return
        }

      // Fallback to a predefined list of properties without any typings
      VUE_INSTANCE_PROPERTIES.forEach {
        consumer.accept(PsiElementResolveResult(JSImplicitElementImpl.Builder(it, source)
                                                  .forbidAstAccess().setType(JSImplicitElement.Type.Property)
                                                  .toImplicitElement()))
      }
      VUE_INSTANCE_METHODS.forEach {
        consumer.accept(PsiElementResolveResult(JSImplicitElementImpl.Builder(it, source)
                                                  .forbidAstAccess().setType(JSImplicitElement.Type.Function)
                                                  .toImplicitElement()))
      }
    }

    companion object {
      val VUE_INSTANCE_PROPERTIES: List<String> = listOf("\$el", "\$options", "\$parent", "\$root", "\$children", "\$refs", "\$slots",
                                                         "\$scopedSlots", "\$isServer", "\$data", "\$props",
                                                         "\$ssrContext", "\$vnode", "\$attrs", "\$listeners")
      val VUE_INSTANCE_METHODS: List<String> = listOf("\$mount", "\$forceUpdate", "\$destroy", "\$set", "\$delete", "\$watch", "\$on",
                                                      "\$once", "\$off", "\$emit", "\$nextTick", "\$createElement"
      )
    }
  }
}
