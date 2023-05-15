// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopeUtil
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable
import org.angular2.lang.html.psi.impl.Angular2HtmlAttrVariableImpl

class Angular2LetType : Angular2BaseType<Angular2HtmlAttrVariableImpl> {
  constructor(variable: Angular2HtmlAttrVariableImpl) : super(variable, Angular2HtmlAttrVariableImpl::class.java) {
    assert(variable.kind == Angular2HtmlAttrVariable.Kind.LET) { variable }
  }

  private constructor(source: JSTypeSource) : super(source, Angular2HtmlAttrVariableImpl::class.java) {
    assert(sourceElement.kind == Angular2HtmlAttrVariable.Kind.LET) { sourceElement }
  }

  override val typeOfText: String?
    get() = PsiTreeUtil.getContextOfType(sourceElement, XmlAttribute::class.java)?.name

  override fun copyWithNewSource(source: JSTypeSource): JSType {
    return Angular2LetType(source)
  }

  override fun resolveType(context: JSTypeSubstitutionContext): JSType? {
    val attribute = PsiTreeUtil.getContextOfType(sourceElement, XmlAttribute::class.java)
    val tag = PsiTreeUtil.getContextOfType(sourceElement, XmlTag::class.java)
    if (attribute == null || tag == null) {
      return null
    }
    val contextItemName = attribute.value ?: Angular2LangUtil.`$IMPLICIT`
    val templateContext = Angular2TypeUtils.getNgTemplateTagContextType(tag)
    return if (templateContext != null) JSCompositeTypeFactory.createIndexedAccessType(
      templateContext, JSStringLiteralTypeImpl(contextItemName, false, source), source).substitute(context)
    else null
  }

  companion object {
    fun getUseScope(variable: Angular2HtmlAttrVariableImpl): SearchScope {
      return GlobalSearchScope.filesScope(variable.project,
                                          GlobalSearchScopeUtil.getLocalScopeFiles(LocalSearchScope(variable.containingFile)))
    }
  }
}