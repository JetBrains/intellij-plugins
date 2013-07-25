/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.UI.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.info.CfmlLangInfo;
import com.intellij.coldFusion.model.psi.*;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.intellij.codeInspection.ProblemHighlightType.WEAK_WARNING;

/**
 * Created by Lera Nikolaenko
 * Date: 17.02.2009
 */
public class CfmlReferenceInspection extends CfmlInspectionBase {
  private static final Map<String, Condition<PsiElement>> myDictionary = new HashMap<String, Condition<PsiElement>>();

  static {
    myDictionary.put("arguments", new Condition<PsiElement>() {
      @Override
      public boolean value(PsiElement psiElement) {
        CfmlFunction parentOfType = PsiTreeUtil.getParentOfType(psiElement, CfmlFunction.class);
        return parentOfType != null;
      }
    });
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }

  public boolean isEnabledByDefault() {
    return true;
  }


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
    String key = ref.getText().toLowerCase();
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
      .containsKey(mostDescentReferenceParent.getText().toLowerCase())) {
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
      final String message = "Can't resolve";
      holder.registerProblem(ref, message, WEAK_WARNING);
    }
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return CfmlBundle.message("cfml.references.inspection");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "CfmlReferenceInspection";
  }
}
