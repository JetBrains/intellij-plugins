// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.javascript.webSymbols.types.TypeScriptSymbolTypeSupport
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.util.contextOfType
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolApiStatus
import com.intellij.webSymbols.html.WebSymbolHtmlAttributeValue
import com.intellij.webSymbols.search.SearchTargetWebSymbol
import com.intellij.webSymbols.search.WebSymbolSearchTarget
import org.angular2.codeInsight.documentation.Angular2ElementDocumentationTarget
import org.angular2.entities.Angular2EntityUtils.jsTypeFromAcceptInputType
import org.angular2.lang.types.Angular2TypeUtils
import org.angular2.web.Angular2Symbol
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_OUTPUTS

interface Angular2DirectiveProperty : Angular2Symbol, Angular2Element, SearchTargetWebSymbol {

  override val name: String

  override val required: Boolean

  val rawJsType: JSType?

  val virtualProperty: Boolean

  override val searchTarget: WebSymbolSearchTarget?
    get() = WebSymbolSearchTarget.create(this)

  override val project: Project
    get() = sourceElement.project

  override val namespace: String
    get() = WebSymbol.NAMESPACE_JS

  override val priority: WebSymbol.Priority?
    get() = WebSymbol.Priority.LOW

  override val type: JSType?
    get() = if (kind == KIND_NG_DIRECTIVE_OUTPUTS) {
      Angular2TypeUtils.extractEventVariableType(rawJsType)
    }
    else {
      jsTypeFromAcceptInputType(owner, name) ?: rawJsType
    }

  val owner: TypeScriptClass?

  override val attributeValue: WebSymbolHtmlAttributeValue?
    get() = if (TypeScriptSymbolTypeSupport.isBoolean(type)) {
      WebSymbolHtmlAttributeValue.create(null, null, false, null, null)
    }
    else {
      null
    }

  override val apiStatus: WebSymbolApiStatus

  override fun createPointer(): Pointer<out Angular2DirectiveProperty>

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget =
    Angular2ElementDocumentationTarget.create(
      name, location, this,
      Angular2EntitiesProvider.getEntity(sourceElement.contextOfType<TypeScriptClass>(true)))
    ?: super<Angular2Symbol>.getDocumentationTarget(location)
}
