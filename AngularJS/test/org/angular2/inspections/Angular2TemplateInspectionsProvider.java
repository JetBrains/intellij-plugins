// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.htmlInspections.*;
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlDeprecatedAttributeInspection;
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlDeprecatedTagInspection;
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection;
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection;
import com.intellij.lang.typescript.inspections.TypeScriptValidateJSTypesInspection;
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection;
import com.intellij.xml.util.CheckEmptyTagInspection;
import com.sixrr.inspectjs.validity.ThisExpressionReferencesGlobalObjectJSInspection;
import org.jetbrains.annotations.NotNull;

public class Angular2TemplateInspectionsProvider implements InspectionToolProvider {
  @Override
  public Class<? extends LocalInspectionTool> @NotNull [] getInspectionClasses() {
    //noinspection unchecked
    return new Class[]{
      // Angular
      AngularIncorrectTemplateDefinitionInspection.class,
      AngularInsecureBindingToEventInspection.class,
      AngularInvalidAnimationTriggerAssignmentInspection.class,
      AngularInvalidTemplateReferenceVariableInspection.class,
      AngularMissingEventHandlerInspection.class,
      AngularMissingRequiredDirectiveInputBindingInspection.class,
      AngularMultipleStructuralDirectivesInspection.class,
      AngularNonEmptyNgContentInspection.class,
      AngularUndefinedBindingInspection.class,
      AngularUndefinedTagInspection.class,
      AngularAmbiguousComponentTagInspection.class,
      AngularNgOptimizedImageInspection.class,
      // TS
      TypeScriptUnresolvedReferenceInspection.class,
      TypeScriptValidateTypesInspection.class,
      TypeScriptValidateJSTypesInspection.class,
      // JS
      ThisExpressionReferencesGlobalObjectJSInspection.class,
      JSUnusedGlobalSymbolsInspection.class,
      JSUnusedLocalSymbolsInspection.class,
      JSUnresolvedReferenceInspection.class,
      JSValidateTypesInspection.class,
      // HTML
      HtmlUnknownAttributeInspection.class,
      HtmlUnknownTagInspection.class,
      HtmlUnknownBooleanAttributeInspection.class,
      HtmlDeprecatedTagInspection.class,
      HtmlDeprecatedAttributeInspection.class,
      HtmlWrongAttributeValueInspection.class,
      RequiredAttributesInspection.class,
      // XML
      CheckEmptyTagInspection.class
    };
  }
}
