package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * @author: Fedor.Korotkov
 */
public class DartArgumentNameContributor extends CompletionContributor {
  public DartArgumentNameContributor() {
    final PsiElementPattern.Capture<PsiElement> idInExpression =
      psiElement().withSuperParent(1, DartId.class).withSuperParent(2, DartReference.class);
    extend(CompletionType.BASIC,
           idInExpression.withSuperParent(3, DartArgumentList.class),
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               DartExpression reference = findExpressionFromCallOrNew(parameters);
               PsiElement target = reference instanceof DartReference ? ((DartReference)reference).resolve() : null;
               PsiElement targetComponent = target != null ? target.getParent() : null;
               DartFormalParameterList parameterList = PsiTreeUtil.getChildOfType(targetComponent, DartFormalParameterList.class);
               if (parameterList != null) {
                 for (DartNormalFormalParameter parameter : parameterList.getNormalFormalParameterList()) {
                   String parameterName = DartPresentableUtil.getParameterName(parameter);
                   addParameterName(result, parameterName);
                 }
                 DartNamedFormalParameters namedFormalParameters = parameterList.getNamedFormalParameters();
                 List<DartDefaultFormalNamedParameter> namedParameterList =
                   namedFormalParameters != null
                   ? namedFormalParameters.getDefaultFormalNamedParameterList()
                   : Collections.<DartDefaultFormalNamedParameter>emptyList();
                 for (DartDefaultFormalNamedParameter parameterDescription : namedParameterList) {
                   String parameterName = DartPresentableUtil.getParameterName(parameterDescription.getNormalFormalParameter());
                   addParameterName(result, parameterName);
                 }
               }
             }

             private void addParameterName(CompletionResultSet result, @Nullable String parameterName) {
               if (parameterName != null) {
                 result.addElement(LookupElementBuilder.create(parameterName));
               }
             }
           });
  }

  @Nullable
  private static DartExpression findExpressionFromCallOrNew(CompletionParameters parameters) {
    DartCallExpression callExpression = PsiTreeUtil.getParentOfType(parameters.getPosition(), DartCallExpression.class);
    if (callExpression != null) {
      return callExpression.getExpression();
    }
    DartNewExpression newExpression = PsiTreeUtil.getParentOfType(parameters.getPosition(), DartNewExpression.class);
    if (newExpression != null) {
      final DartExpression expression = newExpression.getReferenceExpression();
      if (expression != null) {
        return expression;
      }
      final DartType type = newExpression.getType();
      return type != null ? type.getReferenceExpression() : null;
    }
    return null;
  }
}
