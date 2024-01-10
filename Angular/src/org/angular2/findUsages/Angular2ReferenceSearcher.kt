// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.findUsages

import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement
import com.intellij.lang.typescript.psi.TypeScriptPsiUtil
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import org.angular2.entities.Angular2EntitiesProvider

class Angular2ReferenceSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {

  override fun processQuery(queryParameters: ReferencesSearch.SearchParameters,
                            consumer: Processor<in PsiReference>) {
    val element = queryParameters.elementToSearch
    val pipe = Angular2EntitiesProvider.getPipe(element)
    if (pipe != null) {
      for (el in pipe.transformMembers) {
        if (queryParameters.effectiveSearchScope.contains(el.containingFile.viewProvider.virtualFile)) {
          queryParameters.optimizer.searchWord(pipe.getName(), queryParameters.effectiveSearchScope,
                                               true, el)
        }
      }
    }
    else if (element is TypeScriptField
             || (element is TypeScriptFunction && element.getContext() is JSClass)
             || (element is JSParameter && TypeScriptPsiUtil.isFieldParameter(element))) {
      val name = (element as JSAttributeListOwner).name
      if (name != null && (element as JSQualifiedNamedElement).accessType == JSAttributeList.AccessType.PRIVATE) {
        val component = Angular2EntitiesProvider.getComponent(PsiTreeUtil.getContextOfType(element, TypeScriptClass::class.java))
                        ?: return
        val template = component.templateFile
                       ?: return
        queryParameters.optimizer.searchWord(
          name, GlobalSearchScope.fileScope(template).intersectWith(queryParameters.scopeDeterminedByUser),
          false, element)
      }
    }
  }
}
