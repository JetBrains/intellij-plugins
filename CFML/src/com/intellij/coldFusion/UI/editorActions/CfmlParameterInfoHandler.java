// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.editorActions;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.info.CfmlFunctionDescription;
import com.intellij.coldFusion.model.info.CfmlLangInfo;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandler;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CfmlParameterInfoHandler implements ParameterInfoHandler<PsiElement, CfmlFunctionDescription> {
  // for test purposes
  private String myText;

  private static boolean isEmbraced(@Nullable PsiElement element, int offset) {
    if (element == null) {
      return false;
    }
    ASTNode node = element.getNode();
    if (node == null) {
      return false;
    }
    final ASTNode lbrace = node.findChildByType(CfscriptTokenTypes.L_BRACKET);
    final ASTNode rbrace = node.findChildByType(CfscriptTokenTypes.R_BRACKET);

    if (lbrace == null || rbrace == null) {
      return false;
    }
    return lbrace.getStartOffset() < offset && rbrace.getStartOffset() >= offset;
  }

  private static @Nullable PsiElement findAnchorElement(int offset, PsiFile file) {
    if (offset <= 0) {
      return null;
    }
    if (file instanceof CfmlFile) {
      final CfmlArgumentList parametersList = PsiTreeUtil.findElementOfClassAtOffset(file, offset, CfmlArgumentList.class, false);
      if (parametersList == null) {
        return null;
      }
      if (isEmbraced(parametersList, offset)) {
        final PsiElement element = parametersList.getParent();
        return element instanceof CfmlFunctionCallExpression ? ((CfmlFunctionCallExpression)element).getReferenceExpression() : element;
      }
    }
    return null;
  }

  @Override
  public PsiElement findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
    return findAnchorElement(context.getEditor().getCaretModel().getOffset(), context.getFile());
  }

  @Override
  public PsiElement findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
    return findAnchorElement(context.getEditor().getCaretModel().getOffset(), context.getFile());
  }

  @Override
  public void showParameterInfo(@NotNull PsiElement element, @NotNull CreateParameterInfoContext context) {
    ResolveResult[] variants;
    if (element instanceof PsiPolyVariantReference) {
      variants = ((PsiPolyVariantReference)element).multiResolve(true);
      if (variants.length != 0) {
        context.setItemsToShow(
          ContainerUtil.map2Array(variants, CfmlFunctionDescription.class, resolveResult -> {
            final PsiElement element1 = resolveResult.getElement();
            if (CfmlPsiUtil.isFunctionDefinition(element1)) {
              CfmlFunction function = CfmlPsiUtil.getFunctionDefinition(element1);
              if (function != null) {
                return function.getFunctionInfo();
              }
            }
            else if (element1 instanceof PsiMethod function) {
              CfmlFunctionDescription javaMethodDescr =
                new CfmlFunctionDescription(function.getName(), function.getReturnType().getPresentableText());
              final PsiParameter[] psiParameters = function.getParameterList().getParameters();
              for (PsiParameter psiParameter : psiParameters) {
                javaMethodDescr.addParameter(new CfmlFunctionDescription.CfmlParameterDescription(psiParameter.getName(),
                                                                                                  psiParameter.getType()
                                                                                                    .getPresentableText(), true));
              }
              return javaMethodDescr;
            }
            return null;
          }));
        context.showHint(element, element.getTextRange().getStartOffset(), this);
        return;
      }
    }
    if (element instanceof CfmlReferenceExpression) {
      String functionName = StringUtil.toLowerCase(element.getText());
      if (ArrayUtil.find(CfmlLangInfo.getInstance(element.getProject()).getPredefinedFunctionsLowCase(), functionName) != -1) {
        context.setItemsToShow(new Object[]{CfmlLangInfo.getInstance(element.getProject()).getFunctionParameters().get(functionName)});
        context.showHint(element, element.getTextRange().getStartOffset(), this);
      }
    }
  }

  @Override
  public void updateParameterInfo(@NotNull PsiElement place, @NotNull UpdateParameterInfoContext context) {
    if (context.getParameterOwner() == null) {
      context.setParameterOwner(place);
    }
    else if (!context.getParameterOwner().equals(place)) {
      context.removeHint();
      return;
    }
    final Object[] objects = context.getObjectsToView();

    for (int i = 0; i < objects.length; i++) {
      context.setUIComponentEnabled(i, true);
    }
  }


  @Override
  public void updateUI(CfmlFunctionDescription p, @NotNull ParameterInfoUIContext context) {
    if (p == null) {
      context.setUIComponentEnabled(false);
      return;
    }

    myText = p.getParametersListPresentableText();
    context.setupUIComponentPresentation(
      myText,
      0,
      0,
      !context.isUIComponentEnabled(),
      false,
      false,
      context.getDefaultParameterColor()
    );
  }

  public String getText() {
    return myText;
  }
}
