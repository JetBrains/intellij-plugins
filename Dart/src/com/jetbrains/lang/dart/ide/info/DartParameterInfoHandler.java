// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.info;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.List;

public class DartParameterInfoHandler implements ParameterInfoHandler<PsiElement, DartFunctionDescription> {
  private String myParametersListPresentableText = "";

  @Override
  public boolean couldShowInLookup() {
    return true;
  }

  @Override
  public Object[] getParametersForLookup(LookupElement item, ParameterInfoContext context) {
    final Object o = item.getObject();
    if (o instanceof PsiElement) {
      final PsiElement element = (PsiElement)o;
      final DartComponentType type = DartComponentType.typeOf(element.getParent());
      if (type == DartComponentType.METHOD || type == DartComponentType.CONSTRUCTOR) {
        return new Object[]{element.getParent()};
      }
    }
    return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
  }

  @Nullable
  @Override
  public PsiElement findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
    final PsiElement contextElement = context.getFile().findElementAt(context.getEditor().getCaretModel().getOffset());
    return findElementForParameterInfo(contextElement);
  }

  @Nullable
  public static PsiElement findElementForParameterInfo(@Nullable final PsiElement contextElement) {
    final DartArguments arguments = PsiTreeUtil.getParentOfType(contextElement, DartArguments.class);
    final PsiElement parent = arguments == null ? null : arguments.getParent();
    if (parent instanceof DartCallExpression || parent instanceof DartNewExpression || parent instanceof DartMetadata) {
      return parent;
    }
    return null;
  }

  @Override
  public PsiElement findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
    return context.getFile().findElementAt(context.getEditor().getCaretModel().getOffset());
  }

  @Override
  public void showParameterInfo(@NotNull PsiElement element, @NotNull CreateParameterInfoContext context) {
    DartFunctionDescription functionDescription = null;
    if (element instanceof DartCallExpression) {
      functionDescription = DartFunctionDescription.tryGetDescription((DartCallExpression)element);
    }
    else if (element instanceof DartNewExpression) {
      final DartNewExpression newExpression = (DartNewExpression)element;
      final DartType type = newExpression.getType();
      final DartClassResolveResult classResolveResult = DartResolveUtil.resolveClassByType(type);
      List<DartReferenceExpression> expressionList = ((DartNewExpression)element).getReferenceExpressionList();
      DartReference psiElement = expressionList.isEmpty() && type != null ? type.getReferenceExpression()
                                                                          : ContainerUtil.getLastItem(expressionList);
      final PsiElement target = psiElement != null ? psiElement.resolve() : null;
      if (target instanceof DartComponentName) {
        functionDescription = DartFunctionDescription.createDescription((DartComponent)target.getParent(), classResolveResult);
      }
    }
    else if (element instanceof DartMetadata) {
      final DartReferenceExpression refExpression = ((DartMetadata)element).getReferenceExpression();
      final PsiElement target = refExpression.resolve();
      if (target instanceof DartComponentName) {
        functionDescription =
          DartFunctionDescription.createDescription((DartComponent)target.getParent(), refExpression.resolveDartClass());
      }
    }

    if (functionDescription != null) {
      context.setItemsToShow(new Object[]{functionDescription});
      context.showHint(element, element.getTextRange().getStartOffset(), this);
    }
  }

  @Override
  public void updateParameterInfo(@NotNull PsiElement place, @NotNull UpdateParameterInfoContext context) {
    int parameterIndex = DartResolveUtil.getArgumentIndex(place);
    context.setCurrentParameter(parameterIndex);

    if (context.getParameterOwner() == null) {
      context.setParameterOwner(place);
    }
    else if (context.getParameterOwner() != findElementForParameterInfo(place)) {
      context.removeHint();
      return;
    }
    final Object[] objects = context.getObjectsToView();

    for (int i = 0; i < objects.length; i++) {
      context.setUIComponentEnabled(i, true);
    }
  }

  @Override
  public void updateUI(DartFunctionDescription p, @NotNull ParameterInfoUIContext context) {
    if (p == null) {
      context.setUIComponentEnabled(false);
      return;
    }
    myParametersListPresentableText = p.getParametersListPresentableText();
    if (myParametersListPresentableText != null && myParametersListPresentableText.length() == 0) {
      myParametersListPresentableText = CodeInsightBundle.message("parameter.info.no.parameters");
    }
    context.setupUIComponentPresentation(
      myParametersListPresentableText,
      p.getParameterRange(context.getCurrentParameterIndex()).getStartOffset(),
      p.getParameterRange(context.getCurrentParameterIndex()).getEndOffset(),
      !context.isUIComponentEnabled(),
      false,
      false,
      context.getDefaultParameterColor()
    );
  }

  @TestOnly
  public String getParametersListPresentableText() {
    assert ApplicationManager.getApplication().isUnitTestMode();
    return myParametersListPresentableText;
  }
}
