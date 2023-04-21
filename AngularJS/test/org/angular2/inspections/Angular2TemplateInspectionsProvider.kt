// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections

import com.intellij.codeInspection.InspectionToolProvider
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.htmlInspections.*
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlDeprecatedAttributeInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlDeprecatedTagInspection
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.lang.typescript.inspections.TypeScriptValidateJSTypesInspection
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection
import com.intellij.xml.util.CheckEmptyTagInspection
import com.sixrr.inspectjs.validity.ThisExpressionReferencesGlobalObjectJSInspection

class Angular2TemplateInspectionsProvider : InspectionToolProvider {
  override fun getInspectionClasses(): Array<Class<out LocalInspectionTool>> {
    return arrayOf(
      // Angular
      AngularIncorrectTemplateDefinitionInspection::class.java,
      AngularInsecureBindingToEventInspection::class.java,
      AngularInvalidAnimationTriggerAssignmentInspection::class.java,
      AngularInvalidTemplateReferenceVariableInspection::class.java,
      AngularMissingEventHandlerInspection::class.java,
      AngularMissingRequiredDirectiveInputBindingInspection::class.java,
      AngularMultipleStructuralDirectivesInspection::class.java,
      AngularNonEmptyNgContentInspection::class.java,
      AngularUndefinedBindingInspection::class.java,
      AngularUndefinedTagInspection::class.java,
      AngularAmbiguousComponentTagInspection::class.java,
      AngularNgOptimizedImageInspection::class.java,
      // TS
      TypeScriptUnresolvedReferenceInspection::class.java,
      TypeScriptValidateTypesInspection::class.java,
      TypeScriptValidateJSTypesInspection::class.java,
      // JS
      ThisExpressionReferencesGlobalObjectJSInspection::class.java,
      JSUnusedGlobalSymbolsInspection::class.java,
      JSUnusedLocalSymbolsInspection::class.java,
      JSUnresolvedReferenceInspection::class.java,
      JSValidateTypesInspection::class.java,
      // HTML
      HtmlUnknownAttributeInspection::class.java,
      HtmlUnknownTagInspection::class.java,
      HtmlUnknownBooleanAttributeInspection::class.java,
      HtmlDeprecatedTagInspection::class.java,
      HtmlDeprecatedAttributeInspection::class.java,
      HtmlWrongAttributeValueInspection::class.java,
      RequiredAttributesInspection::class.java,
      // XML
      CheckEmptyTagInspection::class.java
    )
  }
}
