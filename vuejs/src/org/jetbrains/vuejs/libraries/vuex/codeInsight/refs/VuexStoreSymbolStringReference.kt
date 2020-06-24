// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.codeInsight.refs

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider
import com.intellij.codeInsight.lookup.Lookup.REPLACE_SELECT_CHAR
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.javascript.completion.JSCompletionUtil
import com.intellij.lang.javascript.completion.JSLookupElementRenderer
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.psi.resolve.CachingPolyReferenceBase
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.validation.HighlightSeverityHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.isNamespaceChild
import org.jetbrains.vuejs.libraries.vuex.model.store.*

class VuexStoreSymbolStringReference(element: PsiElement,
                                     rangeInElement: TextRange,
                                     private val accessor: VuexSymbolAccessor?,
                                     private val fullName: String,
                                     private val terminal: Boolean,
                                     private val namespace: VuexStoreNamespace,
                                     soft: Boolean,
                                     private val includeMembers: Boolean)
  : CachingPolyReferenceBase<PsiElement>(element, rangeInElement),
    EmptyResolveMessageProvider, HighlightSeverityHolder {

  init {
    mySoft = soft
  }

  override fun resolveInner(): Array<ResolveResult> {
    if (!includeMembers && fullName.contains('/')) return ResolveResult.EMPTY_ARRAY
    val name = VuexStoreContext.appendSegment(namespace.get(element), fullName)
    val result = arrayListOf<ResolveResult>()
    VuexModelManager.getVuexStoreContext(element)
      ?.visit(if (terminal) accessor else null) { qualifiedName: String, symbol: Any ->
        if (qualifiedName == name && symbol is VuexNamedSymbol) {
          result.add(JSResolveResult(symbol.getResolveTarget("", qualifiedName)))
        }
      }
    return result.toTypedArray()
  }

  override fun getVariants(): Array<Any> {
    val pathPrefix = fullName.lastIndexOf('/').let {
      if (it < 0) "" else fullName.substring(0, it + 1)
    }
    return getLookupItems(element, namespace, accessor, pathPrefix, false, includeMembers)
      .toTypedArray()
  }

  override fun getUnresolvedMessagePattern(): String {
    return if (!terminal || accessor === null)
      VueBundle.message("vuex.inspection.message.unresolved.namespace",
                        VuexStoreContext.appendSegment(namespace.get(element), fullName))
    else when (accessor) {
      VuexContainer::actions -> VueBundle.message("vuex.inspection.message.unresolved.action", value)
      VuexContainer::getters -> VueBundle.message("vuex.inspection.message.unresolved.getter", value)
      VuexContainer::mutations -> VueBundle.message("vuex.inspection.message.unresolved.mutation", value)
      VuexContainer::state -> VueBundle.message("vuex.inspection.message.unresolved.state", value)
      else -> VueBundle.message("vuex.inspection.message.unresolved.symbol", value)
    }
  }

  override fun getUnresolvedReferenceSeverity(): HighlightSeverity {
    return HighlightSeverity.WEAK_WARNING
  }

  override fun equals(other: Any?): Boolean {
    return super.equals(other)
           && other is VuexStoreSymbolStringReference
           && other.fullName == this.fullName
           && other.terminal == this.terminal
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + fullName.hashCode()
    result = 31 * result + terminal.hashCode()
    return result
  }

  companion object {

    fun getLookupItems(element: PsiElement, namespace: VuexStoreNamespace,
                       accessor: VuexSymbolAccessor?, pathPrefix: String,
                       wrapWithQuotes: Boolean, includeMembers: Boolean): List<LookupElement> {
      val namePrefix = VuexStoreContext.appendSegment(namespace.get(element), pathPrefix)
      val result = mutableListOf<LookupElement>()
      val quote = if (wrapWithQuotes) JSCodeStyleSettings.getQuote(element) else ""
      VuexModelManager.getVuexStoreContext(element)
        ?.visit(accessor) { qualifiedName: String, symbol: Any ->
          if (symbol is VuexNamedSymbol && isNamespaceChild(namePrefix, qualifiedName, !includeMembers))
            result.add(createLookupItem(symbol.getResolveTarget(namePrefix, qualifiedName),
                                        quote + qualifiedName.substring(namePrefix.length) + quote))
        }
      return result
    }

    private fun createLookupItem(value: PsiElement, name: String): LookupElement {
      ProgressManager.checkCanceled()
      val priority = JSLookupPriority.SMART_PRIORITY
      var builder = LookupElementBuilder.createWithSmartPointer(name, value)
      builder = JSLookupElementRenderer(name, priority, false, null).applyToBuilder(builder)
        .withInsertHandler { context, _ ->
          if (context.completionChar == REPLACE_SELECT_CHAR) {
            PsiDocumentManager.getInstance(context.project).commitAllDocuments()
            val elementAtOffset = context.file.findElementAt(context.startOffset)
                                  ?: return@withInsertHandler
            context.document.deleteString(context.editor.caretModel.offset, elementAtOffset.textRange.endOffset - 1)
            context.commitDocument()
          }
        }

      return JSCompletionUtil.withJSLookupPriority(builder, priority)
    }

  }

}
