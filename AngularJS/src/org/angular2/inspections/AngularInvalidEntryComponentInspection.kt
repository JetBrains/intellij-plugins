// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.PsiElementVisitor;
import org.angular2.entities.Angular2Component;
import org.angular2.inspections.Angular2SourceEntityListValidator.ValidationResults;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;

import static org.angular2.Angular2DecoratorUtil.*;

public class AngularInvalidEntryComponentInspection extends LocalInspectionTool {

  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JSElementVisitor() {
      @Override
      public void visitES6Decorator(@NotNull ES6Decorator decorator) {
        if (isAngularEntityDecorator(decorator, MODULE_DEC, COMPONENT_DEC)) {
          ValidationResults<ProblemType> results = new ValidationResults<>();
          new EntryComponentsValidator().validate(decorator, results);
          if (MODULE_DEC.equals(decorator.getDecoratorName())) {
            new BootstrapValidator().validate(decorator, results);
          }
          results.registerProblems(ProblemType.INVALID_ENTRY_COMPONENT, holder);
        }
      }
    };
  }

  private enum ProblemType {
    INVALID_ENTRY_COMPONENT
  }

  private static class EntryComponentsValidator extends Angular2SourceEntityListValidator<Angular2Component, ProblemType> {

    protected EntryComponentsValidator() {
      super(Angular2Component.class, ENTRY_COMPONENTS_PROP);
    }

    @Override
    protected void processNonAcceptableEntityClass(@NotNull JSClass aClass) {
      registerProblem(ProblemType.INVALID_ENTRY_COMPONENT,
                      Angular2Bundle.message("angular.inspection.invalid-entry-component.message.not-component",
                                             aClass.getName()));
    }
  }

  private static class BootstrapValidator extends Angular2SourceEntityListValidator<Angular2Component, ProblemType> {

    protected BootstrapValidator() {
      super(Angular2Component.class, BOOTSTRAP_PROP);
    }

    @Override
    protected void processNonAcceptableEntityClass(@NotNull JSClass aClass) {
      registerProblem(ProblemType.INVALID_ENTRY_COMPONENT,
                      Angular2Bundle.message("angular.inspection.invalid-entry-component.message.not-component",
                                             aClass.getName()));
    }

    @Override
    protected void processAnyElement(JSElement node) {
      registerProblem(ProblemType.INVALID_ENTRY_COMPONENT,
                      Angular2Bundle.message("angular.inspection.invalid-entry-component.message.not-array-of-class-types"));
    }
  }
}
