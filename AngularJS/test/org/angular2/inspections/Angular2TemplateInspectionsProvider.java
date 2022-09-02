// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownBooleanAttributeInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection;
import com.intellij.codeInspection.htmlInspections.HtmlWrongAttributeValueInspection;
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlDeprecatedAttributeInspection;
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlDeprecatedTagInspection;
import com.intellij.lang.javascript.inspections.*;
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedFunctionInspection;
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedVariableInspection;
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
      AngularIncorrectTemplateDefinitionInspection.class,
      AngularInsecureBindingToEventInspection.class,
      AngularInvalidAnimationTriggerAssignmentInspection.class,
      AngularInvalidTemplateReferenceVariableInspection.class,
      AngularMissingEventHandlerInspection.class,
      AngularMultipleStructuralDirectivesInspection.class,
      AngularNonEmptyNgContentInspection.class,
      AngularUndefinedBindingInspection.class,
      AngularUndefinedTagInspection.class,
      AngularAmbiguousComponentTagInspection.class,

      TypeScriptUnresolvedVariableInspection.class,
      TypeScriptUnresolvedFunctionInspection.class,
      TypeScriptValidateTypesInspection.class,
      TypeScriptValidateJSTypesInspection.class,

      ThisExpressionReferencesGlobalObjectJSInspection.class,
      JSUnusedGlobalSymbolsInspection.class,
      JSUnusedLocalSymbolsInspection.class,
      JSUnresolvedVariableInspection.class,
      JSUnresolvedFunctionInspection.class,
      JSValidateTypesInspection.class,

      HtmlUnknownAttributeInspection.class,
      HtmlUnknownTagInspection.class,
      HtmlUnknownBooleanAttributeInspection.class,
      HtmlDeprecatedTagInspection.class,
      HtmlDeprecatedAttributeInspection.class,
      HtmlWrongAttributeValueInspection.class,

      CheckEmptyTagInspection.class
    };
  }
}
