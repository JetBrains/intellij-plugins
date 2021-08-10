// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.findUsages

import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement
import com.intellij.lang.javascript.psi.util.JSUtils
import com.intellij.lang.typescript.psi.TypeScriptPsiUtil
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiElement
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
import org.jetbrains.vuejs.codeInsight.declaredName
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueRegularComponent
import org.jetbrains.vuejs.refactoring.VueRefactoringUtils

class VueJSReferenceSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {

  override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
    val element = queryParameters.elementToSearch
    val elementName = (element as? JSPsiNamedElementBase)?.declaredName

    // Script setup import/export
    if (elementName != null) {
      val scriptTag = PsiTreeUtil.getContextOfType(element, XmlTag::class.java, false, PsiFile::class.java)
      if (scriptTag?.getAttribute(SETUP_ATTRIBUTE_NAME) != null) {
        val template = (VueModelManager.findEnclosingContainer(scriptTag) as? VueRegularComponent)?.template?.source
        if (template != null) {
          sequenceOf(elementName, fromAsset(elementName)).forEach {
            queryParameters.optimizer.searchWord(
              it,
              LocalSearchScope(template),
              UsageSearchContext.IN_CODE,
              true, element)
          }
          return
        }
      }
    }

    val component = VueRefactoringUtils.getComponent(element)

    if (component != null) {
      // TODO migrate to use VueModelManager.findEnclosingComponent()
      // TODO support script setup syntax
      val content = findModule(element, false) ?: return
      val defaultExport = ES6PsiUtil.findDefaultExport(content) as? PsiElement ?: return
      val collector = SearchRequestCollector(queryParameters.optimizer.searchSession)
      queryParameters.optimizer.searchQuery(
        QuerySearchRequest(ReferencesSearch.search(defaultExport, queryParameters.effectiveSearchScope), collector,
                           false, PairProcessor { reference, _ -> consumer.process(reference) }))
      //We are searching for <component-a> and <ComponentA> tags
      //Original component name can't be fromAsset (name: "component-a")
      sequenceOf(component.name, toAsset(component.name)).forEach {
        queryParameters.optimizer.searchWord(it, queryParameters.effectiveSearchScope, false,
                                             component)
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
