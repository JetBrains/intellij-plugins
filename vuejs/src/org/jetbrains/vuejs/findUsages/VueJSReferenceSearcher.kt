// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.findUsages

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSUtils
import com.intellij.lang.typescript.psi.TypeScriptPsiUtil
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.QuerySearchRequest
import com.intellij.psi.search.SearchRequestCollector
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.PairProcessor
import com.intellij.util.Processor
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.SETUP_ATTRIBUTE_NAME
import org.jetbrains.vuejs.codeInsight.findDefaultExport
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueRegularComponent
import org.jetbrains.vuejs.model.source.VueComponents

class VueJSReferenceSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {

  override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
    val element = queryParameters.elementToSearch
    val elementName = (element as? JSPsiNamedElementBase)?.name

    if (elementName != null) {
      // Script setup local vars
      val scriptTag = PsiTreeUtil.getContextOfType(element, XmlTag::class.java, false, PsiFile::class.java)
      if (scriptTag?.getAttribute(SETUP_ATTRIBUTE_NAME) != null &&
          scriptTag.containingFile.virtualFile?.fileType == VueFileType.INSTANCE) {
        sequenceOf(elementName, fromAsset(elementName)).forEach {
          queryParameters.optimizer.searchWord(
            it,
            LocalSearchScope(scriptTag.containingFile),
            UsageSearchContext.IN_CODE,
            false, element)
        }
        return
      }

      // Components
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
            val collector = SearchRequestCollector(queryParameters.optimizer.searchSession)
            queryParameters.optimizer.searchQuery(
              QuerySearchRequest(ReferencesSearch.search(defaultExport, queryParameters.effectiveSearchScope), collector,
                false, PairProcessor { reference, _ -> consumer.process(reference) }))
          }
        }

        val searchTarget = if (element is JSImplicitElement) component.source ?: element else element
        sequenceOf(elementName, fromAsset(elementName)).forEach {
          queryParameters.optimizer.searchWord(it, searchScope, false, searchTarget)
        }
        return
      }

      if (queryParameters.effectiveSearchScope != searchScope) {
        queryParameters.optimizer.searchWord(elementName, searchScope, true, element)
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
          ?.castSafelyTo<VueRegularComponent>()
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
