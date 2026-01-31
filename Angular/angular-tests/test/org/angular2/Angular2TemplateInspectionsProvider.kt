// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2

import com.intellij.codeInspection.InspectionToolProvider
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownBooleanAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.codeInspection.htmlInspections.HtmlWrongAttributeValueInspection
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlDeprecatedAttributeInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlDeprecatedTagInspection
import com.intellij.lang.javascript.inspections.JSConstantReassignmentInspection
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection
import com.intellij.lang.javascript.inspections.TypeScriptCheckImportInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection
import com.intellij.psi.css.inspections.CssUnknownPropertyInspection
import com.intellij.psi.css.inspections.invalid.CssUnresolvedCustomPropertyInspection
import com.intellij.xml.util.CheckEmptyTagInspection
import com.sixrr.inspectjs.validity.ThisExpressionReferencesGlobalObjectJSInspection
import org.angular2.inspections.AngularAmbiguousComponentTagInspection
import org.angular2.inspections.AngularBindingTypeMismatchInspection
import org.angular2.inspections.AngularForBlockNonIterableVarInspection
import org.angular2.inspections.AngularIllegalForLoopTrackAccess
import org.angular2.inspections.AngularInaccessibleSymbolInspection
import org.angular2.inspections.AngularIncorrectBlockUsageInspection
import org.angular2.inspections.AngularIncorrectLetUsageInspection
import org.angular2.inspections.AngularIncorrectTemplateDefinitionInspection
import org.angular2.inspections.AngularInsecureBindingToEventInspection
import org.angular2.inspections.AngularInvalidAnimationTriggerAssignmentInspection
import org.angular2.inspections.AngularInvalidTemplateReferenceVariableInspection
import org.angular2.inspections.AngularMissingEventHandlerInspection
import org.angular2.inspections.AngularMissingRequiredDirectiveInputBindingInspection
import org.angular2.inspections.AngularMultipleStructuralDirectivesInspection
import org.angular2.inspections.AngularNgOptimizedImageInspection
import org.angular2.inspections.AngularNonEmptyNgContentInspection
import org.angular2.inspections.AngularNonStandaloneComponentImportsInspection
import org.angular2.inspections.AngularUncalledSignalLengthPropertyAccessInspection
import org.angular2.inspections.AngularUndefinedBindingInspection
import org.angular2.inspections.AngularUndefinedTagInspection
import org.angular2.inspections.AngularUnresolvedPipeInspection
import org.angular2.inspections.AngularUnsupportedSyntaxInspection
import org.angular2.inspections.AngularUnusedComponentImportInspection

class Angular2TemplateInspectionsProvider(private val strict: Boolean = false) : InspectionToolProvider {
  override fun getInspectionClasses(): Array<Class<out LocalInspectionTool>> {
    val inspections = arrayOf(
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
      AngularNonStandaloneComponentImportsInspection::class.java,
      AngularBindingTypeMismatchInspection::class.java,
      AngularInaccessibleSymbolInspection::class.java,
      AngularIncorrectBlockUsageInspection::class.java,
      AngularForBlockNonIterableVarInspection::class.java,
      AngularUnresolvedPipeInspection::class.java,
      AngularIncorrectLetUsageInspection::class.java,
      AngularUnusedComponentImportInspection::class.java,
      AngularUnsupportedSyntaxInspection::class.java,
      AngularIllegalForLoopTrackAccess::class.java,
      AngularUncalledSignalLengthPropertyAccessInspection::class.java,
      // TS
      TypeScriptUnresolvedReferenceInspection::class.java,
      TypeScriptValidateTypesInspection::class.java,
      // JS
      ThisExpressionReferencesGlobalObjectJSInspection::class.java,
      JSUnusedGlobalSymbolsInspection::class.java,
      JSUnusedLocalSymbolsInspection::class.java,
      JSUnresolvedReferenceInspection::class.java,
      JSValidateTypesInspection::class.java,
      JSConstantReassignmentInspection::class.java,
      // HTML
      HtmlUnknownAttributeInspection::class.java,
      HtmlUnknownTagInspection::class.java,
      HtmlUnknownBooleanAttributeInspection::class.java,
      HtmlDeprecatedTagInspection::class.java,
      HtmlDeprecatedAttributeInspection::class.java,
      HtmlWrongAttributeValueInspection::class.java,
      RequiredAttributesInspection::class.java,
      // XML
      CheckEmptyTagInspection::class.java,
      // CSS
      CssUnknownPropertyInspection::class.java,
      CssUnresolvedCustomPropertyInspection::class.java,
    )

    if (!strict) return inspections

    return inspections + arrayOf(
      TypeScriptCheckImportInspection::class.java
    )
  }
}
