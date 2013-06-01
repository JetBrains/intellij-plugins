package com.jetbrains.lang.dart.ide.info;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class DartParameterInfoHandler implements ParameterInfoHandler<PsiElement, DartFunctionDescription> {
  String myParametersListPresentableText = "";

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
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  @Override
  public Object[] getParametersForDocumentation(DartFunctionDescription p, ParameterInfoContext context) {
    return p.getParameters();
  }

  @Override
  public PsiElement findElementForParameterInfo(CreateParameterInfoContext context) {
    final PsiElement at = context.getFile().findElementAt(context.getEditor().getCaretModel().getOffset());
    final DartArguments arguments = PsiTreeUtil.getParentOfType(at, DartArguments.class);
    return arguments == null ? null : PsiTreeUtil.getParentOfType(arguments, DartCallExpression.class, DartNewExpression.class);
  }

  @Override
  public PsiElement findElementForUpdatingParameterInfo(UpdateParameterInfoContext context) {
    return context.getFile().findElementAt(context.getEditor().getCaretModel().getOffset());
  }

  @Override
  public void showParameterInfo(@NotNull PsiElement element, CreateParameterInfoContext context) {
    DartFunctionDescription functionDescription = null;
    if (element instanceof DartCallExpression) {
      functionDescription = DartFunctionDescription.tryGetDescription((DartCallExpression)element);
    }
    else if (element instanceof DartNewExpression) {
      final DartNewExpression newExpression = (DartNewExpression)element;
      final DartType type = newExpression.getType();
      final DartClassResolveResult classResolveResult = DartResolveUtil.resolveClassByType(type);
      PsiElement psiElement = ((DartNewExpression)element).getReferenceExpression();
      psiElement = psiElement == null && type != null ? type.getReferenceExpression() : psiElement;
      final PsiElement target = psiElement != null ? ((DartReference)psiElement).resolve() : null;
      if (target instanceof DartComponentName) {
        functionDescription = DartFunctionDescription.createDescription((DartComponent)target.getParent(), classResolveResult);
      }
    }

    if (functionDescription != null && functionDescription.getParameters().length > 0) {
      context.setItemsToShow(new Object[]{functionDescription});
      context.showHint(element, element.getTextRange().getStartOffset(), this);
    }
  }

  @Override
  public void updateParameterInfo(@NotNull PsiElement place, UpdateParameterInfoContext context) {
    int parameterIndex = DartResolveUtil.getArgumentIndex(place);
    context.setCurrentParameter(parameterIndex);

    if (context.getParameterOwner() == null) {
      context.setParameterOwner(place);
    }
    else if (context.getParameterOwner() != PsiTreeUtil.getParentOfType(place, DartCallExpression.class, DartNewExpression.class)) {
      context.removeHint();
      return;
    }
    final Object[] objects = context.getObjectsToView();

    for (int i = 0; i < objects.length; i++) {
      context.setUIComponentEnabled(i, true);
    }
  }

  @Override
  public String getParameterCloseChars() {
    return ",){}";
  }

  @Override
  public boolean tracksParameterIndex() {
    return true;
  }

  @Override
  public void updateUI(DartFunctionDescription p, ParameterInfoUIContext context) {
    if (p == null) {
      context.setUIComponentEnabled(false);
      return;
    }
    myParametersListPresentableText = p.getParametersListPresentableText();
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
}
