// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2Component;
import org.angular2.inspections.Angular2SourceEntityListValidator.ValidationResults;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NotNull;

import static java.util.Arrays.asList;
import static org.angular2.Angular2DecoratorUtil.*;

public class Angular2InvalidEntryComponentInspection extends LocalInspectionTool {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JSElementVisitor() {
      @Override
      public void visitES6Decorator(ES6Decorator decorator) {
        if ((MODULE_DEC.equals(decorator.getDecoratorName())
             || COMPONENT_DEC.equals(decorator.getDecoratorName()))
            && Angular2LangUtil.isAngular2Context(decorator)) {
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
    protected void processNonEntityClass(@NotNull JSClass aClass) {
      registerProblem(ProblemType.INVALID_ENTRY_COMPONENT,
                      "Class '" + aClass.getName() + "' is not an Angular Component.");
    }
  }

  private static class BootstrapValidator extends Angular2SourceEntityListValidator<Angular2Component, ProblemType> {

    protected BootstrapValidator() {
      super(Angular2Component.class, BOOTSTRAP_PROP);
    }

    @Override
    protected void processNonEntityClass(@NotNull JSClass aClass) {
      registerProblem(ProblemType.INVALID_ENTRY_COMPONENT,
                      "Class '" + aClass.getName() + "' is not an Angular Component.");
    }

    @Override
    protected void processAnyElement(JSElement node) {
      registerProblem(ProblemType.INVALID_ENTRY_COMPONENT,
                      "Expression resolution contains non-class symbols.");
    }

    @Override
    protected JSElementVisitor createResolveVisitor(SmartList<PsiElement> result) {
      return new JSElementVisitor() {
        @Override
        public void visitJSArrayLiteralExpression(JSArrayLiteralExpression node) {
          result.addAll(asList(node.getExpressions()));
        }

        @Override
        public void visitJSReferenceExpression(JSReferenceExpression node) {
          ContainerUtil.addIfNotNull(result, node.resolve());
        }

        @Override
        public void visitJSVariable(JSVariable node) {
          // TODO try to use stub here
          ContainerUtil.addIfNotNull(result, node.getInitializer());
        }
      };
    }
  }
}
