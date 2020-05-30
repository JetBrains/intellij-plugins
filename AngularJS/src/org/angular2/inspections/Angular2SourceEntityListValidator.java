// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.AstLoadingFilter;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.containers.TreeTraversal;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.source.Angular2SourceEntityListProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.angular2.Angular2DecoratorUtil.getProperty;

abstract class Angular2SourceEntityListValidator<T extends Angular2Entity, E extends Enum> extends Angular2SourceEntityListProcessor<T> {


  private final String myPropertyName;
  private ES6Decorator myDecorator;
  private ValidationResults<? super E> myResults;
  private TreeTraversal.TracingIt<PsiElement> myIterator;

  protected Angular2SourceEntityListValidator(@NotNull Class<T> entityClass, String propertyName) {
    super(entityClass);
    myPropertyName = propertyName;
  }

  public void validate(@NotNull ES6Decorator decorator, @NotNull ValidationResults<? super E> results) {
    myDecorator = decorator;
    myResults = results;
    JSProperty property = getProperty(myDecorator, myPropertyName);
    if (property == null) {
      return;
    }
    AstLoadingFilter.forceAllowTreeLoading(property.getContainingFile(), () -> {
      JSExpression value = property.getValue();
      if (value == null) {
        return;
      }
      Set<PsiElement> visited = new HashSet<>();
      myIterator = TreeTraversal.LEAVES_DFS
        .traversal(singletonList(value), (PsiElement element) ->
          // Protect against cyclic references or visiting same thing several times
          visited.add(element) ? resolve(element) : Collections.emptyList())
        .typedIterator();
      while (myIterator.advance()) {
        ProgressManager.checkCanceled();
        myIterator.current().accept(getResultsVisitor());
      }
    });
  }

  protected @NotNull PsiElement locateProblemElement() {
    final PsiFile file = myDecorator.getContainingFile().getOriginalFile();
    for (PsiElement el : ContainerUtil.concat(singletonList(myIterator.current()),
                                              myIterator.backtrace())) {
      if (file.equals(el.getContainingFile().getOriginalFile())
          && myDecorator.getTextRange().contains(el.getTextRange())) {
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

    LocalQuickFix @Nullable [] getFixes();
  }

  public static class ValidationResults<T extends Enum> {

    public static @NotNull <T extends Enum> ValidationResults<T> empty() {
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
        @Override
        public @NotNull String getMessage() {return message;}

        @Override
        public @NotNull PsiElement getLocation() {return element;}

        @Override
        public @NotNull ProblemHighlightType getSeverity() {return severity;}

        @Override
        public LocalQuickFix @Nullable [] getFixes() {return quickFixes;}
      });
    }
  }
}
