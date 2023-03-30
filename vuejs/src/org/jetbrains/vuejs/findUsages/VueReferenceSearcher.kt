// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.findUsages

import com.intellij.lang.ecmascript6.findUsages.JSFindReferencesResultProcessor
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6Property
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSUtils
import com.intellij.lang.typescript.psi.TypeScriptPsiUtil
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.util.PairProcessor
import com.intellij.util.Processor
import com.intellij.util.asSafely
import com.intellij.webSymbols.search.PsiSourcedWebSymbolRequestResultProcessor
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.findDefaultExport
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.isScriptSetupTag
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.psi.VueRefAttribute
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueRegularComponent
import org.jetbrains.vuejs.model.source.VueComponents
import java.util.*

class VueReferenceSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {

  override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
    val element = queryParameters.elementToSearch
    val elementName = (element as? JSPsiNamedElementBase)?.name

    if (elementName != null) {
      // Script setup local vars
      getFullComponentScopeIfInsideScriptSetup(element)?.let { scope ->
        alternateNames(elementName).forEach {
          queryParameters.optimizer.searchWord(
            it,
            scope,
            UsageSearchContext.IN_CODE,
            false,
            element,
            PsiSourcedWebSymbolRequestResultProcessor(element, true)
          )
        }

        if (element is JSVariable) {
          // JSReferenceExpression optimizes in case of JSVariables, so we need to search for our implicit element instead to find
          // references to `ref` attributes qualified on `$refs` in script setup
          queryParameters.optimizer.searchWord(elementName, LocalSearchScope(element.containingFile),
                                               UsageSearchContext.ANY, true, element, object : RequestResultProcessor() {
            override fun processTextOccurrence(occurence: PsiElement, offsetInElement: Int, consumer: Processor<in PsiReference>): Boolean {
              val implicitElement = (occurence as? XmlAttributeValue)
                ?.parent?.asSafely<VueRefAttribute>()
                ?.implicitElement
              if (implicitElement != null && implicitElement.context == element) {
                val collector = queryParameters.optimizer
                collector.searchWord(elementName, LocalSearchScope(element.containingFile),
                                     UsageSearchContext.ANY, true, implicitElement)
                return PsiSearchHelper.getInstance(element.getProject()).processRequests(collector, consumer)
              }
              return true
            }
          })
        }
        return
      }

      // Vue Scope Elements imported into script setup (as is & aliased)
      queryParameters.optimizer.searchWord(
        elementName,
        queryParameters.effectiveSearchScope,
        UsageSearchContext.IN_CODE,
        true,
        element,
        ScriptSetupImportProcessor(element, queryParameters)
      )

      // Components (will also mistakenly find directives)
      val component = if (element is JSImplicitElement && element.context is JSLiteralExpression)
        VueModelManager.findEnclosingComponent(element)?.takeIf { (it as? VueRegularComponent)?.nameElement == element.context }
      else
        VueComponents.getComponentDescriptor(element)?.let { VueModelManager.getComponent(it) }

      // Extend search scope to the whole Vue file if needed
      val searchScope = queryParameters.effectiveSearchScope.let { scope ->
        val embeddedContents = (scope as? LocalSearchScope)?.scope
          ?.filterIsInstance<JSEmbeddedContent>()
          ?.filter { it.containingFile.virtualFile?.fileType == VueFileType.INSTANCE }
        if (!embeddedContents.isNullOrEmpty()) {
          scope.union(LocalSearchScope(embeddedContents.map { it.containingFile }.toTypedArray()))
        }
        else scope
      }

      if (component != null && isVueContext(component.source ?: element)) {
        // Add search for default export if present
        if (element is JSImplicitElement) {
          findDefaultExport(findModule(component.source?.containingFile, false))?.let { defaultExport ->
            val optimizer = queryParameters.optimizer
            val collector = SearchRequestCollector(optimizer.searchSession)
            optimizer.searchQuery(
              QuerySearchRequest(ReferencesSearch.search(defaultExport, queryParameters.effectiveSearchScope), collector,
                                 false, PairProcessor { reference, _ ->
                if (reference is JSReferenceExpression && reference.parent.let { it is ES6Property && it.isShorthanded }) {
                  val innerCollector = SearchRequestCollector(optimizer.searchSession)
                  optimizer.searchQuery(
                    QuerySearchRequest(ReferencesSearch.search(reference.parent, LocalSearchScope(reference.containingFile)),
                                       innerCollector, false,
                                       PairProcessor { propRef, _ -> consumer.process(propRef) }))
                  if (!PsiSearchHelper.getInstance(element.getProject()).processRequests(optimizer, consumer)) return@PairProcessor false
                }
                consumer.process(reference)
              }))
          }
        }

        val searchTarget = if (element is JSImplicitElement) component.source ?: element else element
        alternateNames(elementName).forEach {
          queryParameters.optimizer.searchWord(
            it,
            searchScope,
            (UsageSearchContext.IN_CODE + UsageSearchContext.IN_FOREIGN_LANGUAGES + UsageSearchContext.IN_COMMENTS).toShort(),
            false,
            searchTarget,
            PsiSourcedWebSymbolRequestResultProcessor(element, true)
          )
        }
        return
      }

      if (queryParameters.effectiveSearchScope != searchScope) {
        alternateNames(elementName).forEach {
          queryParameters.optimizer.searchWord(
            it,
            searchScope,
            (UsageSearchContext.IN_CODE + UsageSearchContext.IN_FOREIGN_LANGUAGES + UsageSearchContext.IN_COMMENTS).toShort(),
            false,
            element,
            PsiSourcedWebSymbolRequestResultProcessor(element, true)
          )
        }
        return
      }
    }

    if (element is JSQualifiedNamedElement
        && element.accessType === JSAttributeList.AccessType.PRIVATE
        && (element is JSField
            || (element is JSFunction && element.context is JSClass)
            || (element is JSParameter && TypeScriptPsiUtil.isFieldParameter(element)))) {
      val name = element.name
      if (name != null && isVueContext(element)) {
        JSUtils.getMemberContainingClass(element)
          ?.let { VueModelManager.getComponent(it) }
          ?.asSafely<VueRegularComponent>()
          ?.template
          ?.source
          ?.let {
            val localSearchScope = LocalSearchScope(arrayOf(it), VueBundle.message("vue.search.scope.template.name"), false)
            queryParameters.optimizer.searchWord(name, localSearchScope, true, element)
          }
      }
    }
  }
}

private class ScriptSetupImportProcessor(target: PsiElement?, queryParameters: ReferencesSearch.SearchParameters?)
  : JSFindReferencesResultProcessor(target, queryParameters) {
  override fun proceedWithReference(element: PsiElement, collector: SearchRequestCollector): Boolean {
    if (element !is ES6ImportSpecifier) return false

    val localElement: PsiElement
    val localName: String?

    run {
      val alias = element.alias
      if (alias != null) {
        localElement = alias
        localName = alias.name
      }
      else {
        localElement = element
        localName = element.declaredName
      }
    }

    if (localName == null) return false

    getFullComponentScopeIfInsideScriptSetup(localElement)?.let { scope ->
      alternateNames(localName).forEach {
        collector.searchWord(
          it,
          scope,
          UsageSearchContext.IN_CODE,
          false,
          localElement,
          PsiSourcedWebSymbolRequestResultProcessor(localElement, true)
        )
      }

      return true
    }

    return false
  }

}

private fun getFullComponentScopeIfInsideScriptSetup(element: PsiElement): LocalSearchScope? {
  val scriptTag = PsiTreeUtil.getContextOfType(element, XmlTag::class.java, false, PsiFile::class.java)

  if (scriptTag.isScriptSetupTag() && scriptTag.containingFile.virtualFile?.fileType == VueFileType.INSTANCE) {
    return LocalSearchScope(scriptTag.containingFile)
  }

  return null
}

private fun alternateNames(elementName: String): List<String> {
  return sequenceOf(elementName, fromAsset(elementName), fromAsset(elementName.removePrefix("v")))
    .map { it.lowercase(Locale.US) }
    .distinct()
    .toList()
}
