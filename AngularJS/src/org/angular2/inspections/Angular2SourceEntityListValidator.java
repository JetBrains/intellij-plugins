// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.JBIterable;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.containers.TreeTraversal;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.source.Angular2SourceEntityListProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.function.Supplier;

import static org.angular2.Angular2DecoratorUtil.getProperty;

abstract class Angular2SourceEntityListValidator<T extends Angular2Entity, E extends Enum> extends Angular2SourceEntityListProcessor<T> {


  private final String myPropertyName;
  private ES6Decorator myDecorator;
  private ValidationResults<E> myResults;
  private Supplier<JBIterable<PsiElement>> myBackTraceSupplier;

  protected Angular2SourceEntityListValidator(@NotNull Class<T> entityClass, String propertyName) {
    super(entityClass);
    myPropertyName = propertyName;
  }

  public void validate(@NotNull ES6Decorator decorator, @NotNull ValidationResults<E> results) {
    myDecorator = decorator;
    myResults = results;
    JSProperty property = getProperty(myDecorator, myPropertyName);
    JSExpression value;
    if (property == null || (value = property.getValue()) == null) {
      return;
    }
    TreeTraversal.TracingIt<PsiElement> it = TreeTraversal.LEAVES_DFS
      .traversal(Collections.singletonList(value), this::resolve)
      .typedIterator();
    myBackTraceSupplier = it::backtrace;
    while (it.advance()) {
      it.current().accept(getResultsVisitor());
    }
  }

  @NotNull
  protected PsiElement locateProblemElement() {
    for (PsiElement el : myBackTraceSupplier.get()) {
      if (myDecorator.getTextRange().contains(el.getTextRange())) {
        return el;
      }
    }
    return myDecorator;
  }

  protected void registerProblem(@NotNull E problemType,
                                 @NotNull String message,
                                 LocalQuickFix... quickFixes) {
    myResults.registerProblem(locateProblemElement(), problemType, message,
                              ProblemHighlightType.GENERIC_ERROR_OR_WARNING, quickFixes);
  }

  protected void registerProblem(@NotNull E problemType,
                                 @NotNull String message,
                                 @NotNull ProblemHighlightType severity,
                                 LocalQuickFix... quickFixes) {
    myResults.registerProblem(locateProblemElement(), problemType, message,
                              severity, quickFixes);
  }

  public interface ValidationProblem {
    @NotNull
    PsiElement getLocation();

    @NotNull
    String getMessage();

    @NotNull
    ProblemHighlightType getSeverity();

    @Nullable
    LocalQuickFix[] getFixes();
  }

  public static class ValidationResults<T extends Enum> {

    @NotNull
    public static <T extends Enum> ValidationResults<T> empty() {
      //noinspection unchecked
      return (ValidationResults<T>)EMPTY;
    }

    public void registerProblems(@NotNull T problemType, @NotNull ProblemsHolder holder) {
      for (ValidationProblem problem : results.get(problemType)) {
        holder.registerProblem(problem.getLocation(), problem.getMessage(), problem.getSeverity(), problem.getFixes());
      }
    }

    private static final ValidationResults<?> EMPTY = new ValidationResults() {
      @Override
      public void registerProblems(@NotNull Enum problemType, @NotNull ProblemsHolder holder) {
      }
    };

    private final MultiMap<T, ValidationProblem> results = new MultiMap<>();

    private void registerProblem(@NotNull PsiElement element,
                                 @NotNull T type,
                                 @NotNull String message,
                                 @NotNull ProblemHighlightType severity,
                                 LocalQuickFix... quickFixes) {
      results.putValue(type, new ValidationProblem() {
        @NotNull
        @Override
        public String getMessage() {return message;}

        @NotNull
        @Override
        public PsiElement getLocation() {return element;}

        @NotNull
        @Override
        public ProblemHighlightType getSeverity() {return severity;}

        @Nullable
        @Override
        public LocalQuickFix[] getFixes() {return quickFixes;}
      });
    }
  }
}
