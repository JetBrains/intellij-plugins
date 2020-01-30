// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.codeInsight.refs

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.javascript.psi.resolve.CachingPolyReferenceBase
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.validation.HighlightSeverityHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexModelManager
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexStoreContext

class VuexNamespaceReference(element: PsiElement,
                             rangeInElement: TextRange,
                             private val fullName: String,
                             private val namespaceResolver: (PsiElement) -> String,
                             soft: Boolean)
  : CachingPolyReferenceBase<PsiElement>(element, rangeInElement.shiftRight(1)),
    EmptyResolveMessageProvider, HighlightSeverityHolder {

  init {
    mySoft = soft
  }

  override fun resolveInner(): Array<ResolveResult> {
    val name = VuexStoreContext.appendSegment(namespaceResolver(element), fullName)
    val result = arrayListOf<ResolveResult>()
    VuexModelManager.getVuexStoreContext(element)
      ?.visit { namespace, container ->
        if (namespace == name) {
          result.add(JSResolveResult(container.source))
        }
      }
    return result.toTypedArray()
  }

  override fun getVariants(): Array<Any> {
    val result = mutableListOf<String>()
    val prefix = VuexStoreContext.appendSegment(namespaceResolver(element),
                                                fullName.dropLastWhile { it != '/' })
    VuexModelManager.getVuexStoreContext(element)
      ?.visit { namespace, _ ->
        if (namespace.startsWith(prefix))
          result.add(namespace.substring(prefix.length))
      }
    return result.toTypedArray()
  }

  override fun getUnresolvedMessagePattern(): String {
    return "Unknown module namespace ${value}"
  }

  override fun getUnresolvedReferenceSeverity(): HighlightSeverity {
    return HighlightSeverity.WEAK_WARNING
  }

  override fun equals(other: Any?): Boolean {
    return super.equals(other)
           && other is VuexNamespaceReference
           && other.fullName == this.fullName
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + fullName.hashCode()
    return result
  }
}
