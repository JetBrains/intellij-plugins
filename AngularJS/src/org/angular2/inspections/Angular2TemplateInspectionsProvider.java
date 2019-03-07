// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.lang.typescript.inspections.*;
import org.jetbrains.annotations.NotNull;

public class Angular2TemplateInspectionsProvider implements InspectionToolProvider {
  @NotNull
  @Override
  public Class[] getInspectionClasses() {
    return new Class[]{
      AngularIncorrectTemplateDefinitionInspection.class,
      AngularInsecureBindingToEventInspection.class,
      AngularInvalidAnimationTriggerAssignmentInspection.class,
      AngularInvalidExpressionResultTypeInspection.class,
      AngularInvalidTemplateReferenceVariableInspection.class,
      AngularMissingEventHandlerInspection.class,
      AngularMultipleStructuralDirectivesInspection.class,
      AngularNonEmptyNgContentInspection.class,
      AngularUndefinedBindingInspection.class,
      AngularUndefinedTagInspection.class,

      TypeScriptUnresolvedVariableInspection.class,
      TypeScriptUnresolvedFunctionInspection.class,
      TypeScriptValidateTypesInspection.class,
      TypeScriptValidateJSTypesInspection.class,
      TypeScriptAccessibilityCheckInspection.class,

      JSUnusedGlobalSymbolsInspection.class,
      JSUnusedLocalSymbolsInspection.class,
    };
  }
}
