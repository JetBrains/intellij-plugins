/*
 * Copyright 2010 The authors
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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.codeStyle.SuggestedNameInfo;
import com.intellij.psi.codeStyle.VariableKind;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides self-reference/variants for {@code <action> "name"} (required for proper usage search).
 *
 * @author Yann C&eacute;bron
 */
public class ActionNameCustomReferenceConverter implements CustomReferenceConverter<String> {

  @NonNls
  private static final String ACTION_SUFFIX = "action";

  private final Function<String, Object> ACTION_NAME_FUNCTION = new Function<String, Object>() {
    @Override
    public Object fun(final String s) {
      return StringUtil.endsWithIgnoreCase(s, ACTION_SUFFIX) ?
             StringUtil.replaceIgnoreCase(s, ACTION_SUFFIX, "") : s;
    }
  };

  @NotNull
  @Override
  public PsiReference[] createReferences(final GenericDomValue<String> genericDomValue,
                                         final PsiElement psiElement,
                                         final ConvertContext convertContext) {
    final PsiReferenceBase<PsiElement> ref = new PsiReferenceBase<PsiElement>(psiElement) {

      @SuppressWarnings({"ConstantConditions"})
      public PsiElement resolve() {
        return genericDomValue.getParent().getXmlTag();
      }

      public boolean isSoft() {
        return true;
      }

      // do nothing. the element will be renamed via PsiMetaData
      public PsiElement handleElementRename(final String newElementName) throws IncorrectOperationException {
        return getElement();
      }

      @NotNull
      public Object[] getVariants() {
        final DomElement invocationElement = convertContext.getInvocationElement();
        final Action action = invocationElement.getParentOfType(Action.class, true);
        assert action != null;

        final PsiClass psiClass = action.getActionClass().getValue();
        if (psiClass == null) {
          return EMPTY_ARRAY;
        }

        final Project project = psiClass.getProject();
        final PsiClassType classType = JavaPsiFacade.getInstance(project).getElementFactory().createType(psiClass);
        final JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance(project);
        final SuggestedNameInfo info = codeStyleManager.suggestVariableName(VariableKind.LOCAL_VARIABLE, null, null, classType);

        final Set<String> variants = new HashSet<String>(Arrays.asList(info.names));
        variants.remove(ACTION_SUFFIX);

        // remove existing action-names
        final List<Action> actions = action.getStrutsPackage().getActions();
        ContainerUtil.process(actions, new Processor<Action>() {
          @Override
          public boolean process(final Action action) {
            variants.remove(action.getName().getStringValue());
            return true;
          }
        });

        return ContainerUtil.map2Array(variants, ACTION_NAME_FUNCTION);
      }
    };

    return new PsiReference[]{ref};
  }

}
