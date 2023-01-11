// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.info.CfmlLangInfo;
import com.intellij.coldFusion.model.psi.*;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.codeInspection.ProblemHighlightType.WEAK_WARNING;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlReferenceInspection extends CfmlInspectionBase {
  private static final Map<String, Condition<PsiElement>> myDictionary = new HashMap<>();

  static {
    myDictionary.put("arguments", psiElement -> {
      CfmlFunction parentOfType = PsiTreeUtil.getParentOfType(psiElement, CfmlFunction.class);
      return parentOfType != null;
    });
  }

  @Override
  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return super.getDefaultLevel();
  }

  @Override
  public boolean isEnabledByDefault() {
    return super.isEnabledByDefault();
  }


  @Override
  protected void registerProblems(PsiElement element, ProblemsHolder holder) {
    if (!(element instanceof CfmlReference)) {
      return;
    }
    final CfmlReference ref = (CfmlReference)element;
    if (CfmlUtil.isPredefinedFunction(ref.getText(), element.getProject())) {
      return;
    }

    // skip argument names of standard functions
    if (ref instanceof CfmlArgumentNameReference) {
      String functionName = ((CfmlArgumentNameReference)ref).getFunctionName();
      if (CfmlUtil.isPredefinedFunction(functionName, element.getProject())) {
        return;
      }
    }
    String key = StringUtil.toLowerCase(ref.getText());
    if (myDictionary.containsKey(key)) {
      Condition<PsiElement> psiElementCondition = myDictionary.get(key);
      if (psiElementCondition.value(element)) {
        return;
      }
    }

    PsiElement mostDescentReferenceParent = element;
    while (mostDescentReferenceParent.getParent() instanceof CfmlReference) {
      mostDescentReferenceParent = mostDescentReferenceParent.getParent();
    }

    if (CfmlLangInfo.getInstance(element.getProject()).getPredefinedVariables()
      .containsKey(StringUtil.toLowerCase(mostDescentReferenceParent.getText()))) {
      return;
    }
    if (ref instanceof CfmlReferenceExpression && CfmlUtil.isPredefinedTagVariables((CfmlReferenceExpression)ref, element.getProject())) {
      return;
    }


    // block inspection on left part of assignment (as it can be definition)
    final PsiElement parent = ref.getParent();
    if (parent instanceof CfmlAssignmentExpression) {
      CfmlAssignmentExpression assignment = (CfmlAssignmentExpression)parent;
      CfmlVariable var = assignment.getAssignedVariable();
      if (var != null && assignment.getAssignedVariableElement() == ref) {
        return;
      }
    }

    if (ref.multiResolve(false).length == 0) {
      final String message = CfmlBundle.message("inspection.message.resolve.problem");
      holder.registerProblem(ref, message, WEAK_WARNING);
    }
  }

  @Override
  @NonNls
  @NotNull
  public String getShortName() {
    return "CfmlReferenceInspection";
  }
}
