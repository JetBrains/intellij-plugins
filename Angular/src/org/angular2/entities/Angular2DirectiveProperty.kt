// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.polySymbols.js.documentation.JSSymbolWithSubstitutor
import com.intellij.polySymbols.js.types.PROP_JS_TYPE
import com.intellij.polySymbols.js.types.TypeScriptSymbolTypeSupport
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.types.JSTypeSubstitutor
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolApiStatus
import com.intellij.polySymbols.PolySymbolModifier
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.html.PROP_HTML_ATTRIBUTE_VALUE
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue
import com.intellij.polySymbols.search.PolySymbolSearchTarget
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.css.impl.CssNamedItemPresentation
import com.intellij.psi.util.contextOfType
import com.intellij.util.ThreeState
import icons.AngularIcons
import org.angular2.codeInsight.documentation.Angular2ElementDocumentationTarget
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.types.Angular2TypeUtils
import org.angular2.lang.types.BindingsTypeResolver
import org.angular2.web.Angular2Symbol
import org.angular2.web.NG_DIRECTIVE_INPUTS
import org.angular2.web.NG_DIRECTIVE_IN_OUTS
import org.angular2.web.NG_DIRECTIVE_OUTPUTS

interface Angular2DirectiveProperty : Angular2Symbol, Angular2Element, JSSymbolWithSubstitutor {

  override val name: String

  val required: Boolean

  val fieldName: String?

  val rawJsType: JSType?

  val virtualProperty: Boolean

  val isSignalProperty: Boolean
    get() = false

  override val modifiers: Set<PolySymbolModifier>
    get() = when (required) {
      true -> setOf(PolySymbolModifier.REQUIRED)
      false -> setOf(PolySymbolModifier.OPTIONAL)
    }

  override val presentation: TargetPresentation
    get() = TargetPresentation
      .builder(name + (rawJsType?.getTypeText(JSType.TypeTextFormat.PRESENTABLE)?.let { ": $it" } ?: ""))
      .icon(AngularIcons.Angular2)
      .containerText(
        when (qualifiedKind) {
          NG_DIRECTIVE_INPUTS -> Angular2Bundle.message("angular.entity.directive.input")
          NG_DIRECTIVE_OUTPUTS -> Angular2Bundle.message("angular.entity.directive.output")
          NG_DIRECTIVE_IN_OUTS -> Angular2Bundle.message("angular.entity.directive.inout")
          else -> Angular2Bundle.message("angular.entity.directive.property")
        })
      .locationText((sourceElement as? NavigatablePsiElement)?.presentation?.locationString
                    ?: CssNamedItemPresentation.getLocationString(sourceElement))
      .presentation()

  override val searchTarget: PolySymbolSearchTarget?
    get() = PolySymbolSearchTarget.create(this)

  override val project: Project
    get() = sourceElement.project

  override val priority: PolySymbol.Priority?
    get() = PolySymbol.Priority.LOW

  val type: JSType?
    get() = if (qualifiedKind == NG_DIRECTIVE_OUTPUTS)
      Angular2TypeUtils.extractEventVariableType(rawJsType)
    else
      rawJsType

  val attributeValue: PolySymbolHtmlAttributeValue?
    get() = if (TypeScriptSymbolTypeSupport.isBoolean(type, sourceElement) != ThreeState.NO) {
      PolySymbolHtmlAttributeValue.create(null, null, false, null, null)
    }
    else {
      null
    }

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_JS_TYPE -> property.tryCast(type)
      PROP_HTML_ATTRIBUTE_VALUE -> property.tryCast(attributeValue)
      else -> super.get(property)
    }

  override val apiStatus: PolySymbolApiStatus

  override fun createPointer(): Pointer<out Angular2DirectiveProperty>

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget? =
    Angular2ElementDocumentationTarget.create(
      name, location, this,
      Angular2EntitiesProvider.getEntity(sourceElement.contextOfType<TypeScriptClass>(true)))
    ?: super<Angular2Symbol>.getDocumentationTarget(location)

  override fun getTypeSubstitutor(location: PsiElement): JSTypeSubstitutor? =
    BindingsTypeResolver.get(location)?.getTypeSubstitutorForDocumentation(
      Angular2EntitiesProvider.getDirective(sourceElement.contextOfType<TypeScriptClass>(true))
    )

}
