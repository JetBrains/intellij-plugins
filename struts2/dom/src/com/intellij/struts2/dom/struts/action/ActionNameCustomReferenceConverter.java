/*
 * Copyright 2011 The authors
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
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

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

  private final Function<String, Object> ACTION_NAME_FUNCTION = s -> StringUtil.endsWithIgnoreCase(s, ACTION_SUFFIX) ?
                                                                 StringUtil.replaceIgnoreCase(s, ACTION_SUFFIX, "") : s;

  @Override
  public PsiReference @NotNull [] createReferences(final GenericDomValue<String> genericDomValue,
                                                   final PsiElement psiElement,
                                                   final ConvertContext convertContext) {
    final PsiReferenceBase<PsiElement> ref = new PsiReferenceBase<>(psiElement) {

      @Override
      public PsiElement resolve() {
        return genericDomValue.getParent().getXmlTag();
      }

      @Override
      public boolean isSoft() {
        return true;
      }

      // do nothing. the element will be renamed via PsiMetaData
      @Override
      public PsiElement handleElementRename(@NotNull final String newElementName) throws IncorrectOperationException {
        return getElement();
      }

      @Override
      public Object @NotNull [] getVariants() {
        final DomElement invocationElement = convertContext.getInvocationElement();
        final Action action = invocationElement.getParentOfType(Action.class, true);
        assert action != null;

        final PsiClass psiClass = action.searchActionClass();
        if (psiClass == null) {
          return EMPTY_ARRAY;
        }

        final Project project = psiClass.getProject();
        final PsiClassType classType = PsiTypesUtil.getClassType(psiClass);
        final JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance(project);
        final SuggestedNameInfo info = codeStyleManager.suggestVariableName(VariableKind.LOCAL_VARIABLE, null, null, classType);

        final Set<String> variants = ContainerUtil.newHashSet(info.names);
        variants.remove(ACTION_SUFFIX);

        // remove existing action-names
        final List<Action> actions = action.getStrutsPackage().getActions();
        ContainerUtil.process(actions, action1 -> {
          variants.remove(action1.getName().getStringValue());
          return true;
        });

        return ContainerUtil.map2Array(variants, ACTION_NAME_FUNCTION);
      }
    };

    return new PsiReference[]{ref};
  }

}
