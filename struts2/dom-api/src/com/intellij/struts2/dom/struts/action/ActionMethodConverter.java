/*
 * Copyright 2007 The authors
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

package com.intellij.struts2.dom.struts.action;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * <code>action</code> "method"
 *
 * @author Yann CŽbron
 */
public class ActionMethodConverter extends ResolvingConverter<PsiMethod> {

  private static final Logger LOG = Logger.getInstance(ActionMethodConverter.class.getSimpleName());

  @NotNull
  public Collection<? extends PsiMethod> getVariants(final ConvertContext context) {
    final Action action = getActionElement(context);
    return action.getActionMethods();
  }

  public PsiMethod fromString(@Nullable @NonNls final String s, final ConvertContext context) {
    if (s == null) {
      return null;
    }

    final Action action = getActionElement(context);
    for (final PsiMethod method : action.getActionMethods()) {
      if (method.getName().equals(s)) {
        return method;
      }
    }

    return null;
  }


  public String toString(@Nullable final PsiMethod psiMethod, final ConvertContext context) {
    return psiMethod != null ? psiMethod.getName() : null;
  }

  public String getErrorMessage(@Nullable final String s, final ConvertContext context) {
    return "Cannot resolve action-method ''" + s + "''";
  }

  public LocalQuickFix[] getQuickFixes(final ConvertContext context) {
    final Action action = getActionElement(context);
    final String methodName = action.getMethod().getStringValue();
    final PsiClass actionClass = action.getActionClass().getValue();

    return new LocalQuickFix[]{new CreateActionMethodQuickFix(methodName, actionClass)
    };
  }

  /**
   * Gets the enclosing <code>action</code>-element for the current context.
   *
   * @param context Current context.
   *
   * @return Action-element.
   */
  @NotNull
  private static Action getActionElement(final ConvertContext context) {
    final DomElement domElement = context.getInvocationElement();
    final Action action = domElement.getParentOfType(Action.class, false);
    assert action != null : "not triggered within <action> for " + domElement.getXmlElement();
    return action;
  }


  private static class CreateActionMethodQuickFix implements LocalQuickFix {

    private final String methodName;
    private final PsiClass actionClass;

    private CreateActionMethodQuickFix(final String methodName, final PsiClass actionClass) {
      this.methodName = methodName;
      this.actionClass = actionClass;
    }

    @NotNull
    public String getName() {
      return "Create action-method '" + methodName + "'";
    }

    @NotNull
    public String getFamilyName() {
      return "Struts 2 Quickfixes";
    }

    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
      try {
        final PsiElementFactory elementFactory = JavaPsiFacade.getInstance(project).getElementFactory();

        // TODO use file template
        PsiMethod actionMethod = elementFactory.createMethodFromText(
          "public java.lang.String " + methodName + "() throws java.lang.Exception { return \"success\"; }", actionClass);

        final JavaCodeStyleManager javaCodeStyleManager = JavaCodeStyleManager.getInstance(project);
        actionMethod = (PsiMethod) javaCodeStyleManager.shortenClassReferences(actionMethod);
        final CodeStyleManager codestylemanager = CodeStyleManager.getInstance(project);
        actionMethod = (PsiMethod) codestylemanager.reformat(actionMethod);

        final PsiMethod element = (PsiMethod) actionClass.add(actionMethod);

        //noinspection ConstantConditions
        OpenSourceUtil.navigate(new Navigatable[]{(Navigatable) element.getBody().getNavigationElement()}, true);
      } catch (IncorrectOperationException e) {
        LOG.error("creation of action-method failed", e);
      }
    }
  }

}
