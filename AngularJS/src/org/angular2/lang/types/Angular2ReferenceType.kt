// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types

import com.intellij.javascript.web.js.WebJSTypesUtil.getHtmlElementClassType
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopeUtil
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.Companion.isTemplateTag
import org.angular2.entities.Angular2ComponentLocator.findComponentClass
import org.angular2.entities.Angular2EntityUtils.TEMPLATE_REF
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable
import org.angular2.lang.html.psi.Angular2HtmlReference
import org.angular2.lang.html.psi.impl.Angular2HtmlAttrVariableImpl

class Angular2ReferenceType : Angular2BaseType<Angular2HtmlAttrVariableImpl> {
  constructor(variable: Angular2HtmlAttrVariableImpl) : super(variable, Angular2HtmlAttrVariableImpl::class.java) {
    assert(variable.kind == Angular2HtmlAttrVariable.Kind.REFERENCE) { variable }
  }

  private constructor(source: JSTypeSource) : super(source, Angular2HtmlAttrVariableImpl::class.java) {
    assert(sourceElement.kind == Angular2HtmlAttrVariable.Kind.REFERENCE) { sourceElement }
  }

  override val typeOfText: String?
    get() = PsiTreeUtil.getContextOfType(sourceElement, XmlAttribute::class.java)?.name

  override fun copyWithNewSource(source: JSTypeSource): JSType {
    return Angular2ReferenceType(source)
  }

  override fun resolveType(context: JSTypeSubstitutionContext): JSType? {
    val reference = referenceDefinitionAttribute ?: return null
    val tag = reference.parent ?: return null
    val exportName = reference.value
    val hasExport = !exportName.isNullOrEmpty()
    return BindingsTypeResolver.get(tag)
             .resolveDirectiveExportAsType(exportName)
           ?: when {
             hasExport -> null
             isTemplateTag(tag) -> getTemplateRefType(findComponentClass(tag), Angular2TypeUtils.getNgTemplateTagContextType(tag))
             else -> getHtmlElementClassType(Angular2TypeUtils.createJSTypeSourceForXmlElement(tag), tag.name)
           }
  }

  private val referenceDefinitionAttribute: Angular2HtmlReference?
    get() = PsiTreeUtil.findFirstParent(sourceElement) { obj -> obj is Angular2HtmlReference } as Angular2HtmlReference?

  companion object {
    fun getUseScope(variable: Angular2HtmlAttrVariableImpl): SearchScope {
      val clazz: JSClass? = findComponentClass(variable)
      val localScope = if (clazz != null) {
        LocalSearchScope(arrayOf<PsiElement>(clazz, variable.containingFile))
      }
      else {
        LocalSearchScope(variable.containingFile)
      }
      return GlobalSearchScope.filesScope(variable.project, GlobalSearchScopeUtil.getLocalScopeFiles(localScope))
    }

    private fun getTemplateRefType(scope: PsiElement?, contextType: JSType?): JSType? {
      return if (scope == null) null
      else CachedValuesManager.getCachedValue(scope) {
        for (module in JSFileReferencesUtil.resolveModuleReference(scope, Angular2LangUtil.ANGULAR_CORE_PACKAGE)) {
          if (module !is JSElement) continue
          val templateRefClass = JSResolveResult.resolve(ES6PsiUtil.resolveSymbolInModule(TEMPLATE_REF, scope, module)) as? TypeScriptClass
          if (templateRefClass != null && templateRefClass.typeParameters.size == 1) {
            return@getCachedValue create(templateRefClass, PsiModificationTracker.MODIFICATION_COUNT)
          }
        }
        create(null, PsiModificationTracker.MODIFICATION_COUNT)
      }?.let { templateRefClass ->
        val baseType = templateRefClass.jsType
        JSGenericTypeImpl(baseType.source, baseType, contextType ?: JSAnyType.get(templateRefClass, true))
      }
    }
  }
}