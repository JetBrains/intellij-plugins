/*
 * Copyright 2013 The authors
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
package com.intellij.struts2.dom.params.custom;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.struts2.dom.params.ParamNameConverter;
import com.intellij.struts2.dom.params.ParamsElement;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.reference.common.BeanPropertyPathReference;
import com.intellij.struts2.reference.common.BeanPropertyPathReferenceSet;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SmartList;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Resolves additionally to Action class properties.
 *
 * @author Yann C&eacute;bron
 */
public class ResultParamNameCustomConverter extends ParamNameConverter.ParamNameCustomConverter {

  @Override
  public PsiReference @NotNull [] getCustomReferences(XmlAttributeValue nameAttributeValue, DomElement paramsElement) {
    if (!(paramsElement instanceof Result result)) {
      return PsiReference.EMPTY_ARRAY;
    }

    Action action = DomUtil.getParentOfType(result, Action.class, true);
    assert action != null;

    return new PsiReference[]{new MergingBeanPropertyPathReference(nameAttributeValue,
                                                                   action, result)};
  }


  private static final class MergingBeanPropertyPathReference extends PsiReferenceBase<PsiElement>
    implements EmptyResolveMessageProvider, LocalQuickFixProvider {

    private final List<BeanPropertyPathReference[]> allReferences = new SmartList<>();

    /**
     * @param element        XML element.
     * @param paramsElements First element will be used for quickfixes.
     */
    private MergingBeanPropertyPathReference(@NotNull XmlAttributeValue element,
                                             ParamsElement @NotNull ... paramsElements) {
      super(element, true);

      for (ParamsElement paramsElement : paramsElements) {
        allReferences.add(getBeanPropertyReferences(element, paramsElement));
      }
    }

    private static BeanPropertyPathReference[] getBeanPropertyReferences(XmlAttributeValue element,
                                                                         ParamsElement paramsElement) {
      return new BeanPropertyPathReferenceSet(element, paramsElement.getParamsClass(), false).getPsiReferences();
    }

    @Nullable
    @Override
    public PsiElement resolve() {
      for (BeanPropertyPathReference[] reference : allReferences) {
        for (BeanPropertyPathReference pathReference : reference) {
          final PsiMethod resolve = pathReference.resolve();
          if (resolve != null) {
            return resolve;
          }
        }
      }
      return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
      List<Object> variants = new SmartList<>();
      for (BeanPropertyPathReference[] reference : allReferences) {
        for (BeanPropertyPathReference pathReference : reference) {
          Collections.addAll(variants, pathReference.getVariants());
        }
      }
      return ArrayUtil.toObjectArray(variants);
    }

    @NotNull
    @Override
    public String getUnresolvedMessagePattern() {
      return "Cannot resolve property '" + getValue() + "'";
    }

    @Override
    public @NotNull LocalQuickFix @Nullable [] getQuickFixes() {
      List<LocalQuickFix> quickFixes = new SmartList<>();
      for (BeanPropertyPathReference[] reference : allReferences) {
        for (BeanPropertyPathReference pathReference : reference) {
          final LocalQuickFix[] fixes = pathReference.getQuickFixes();
          if (fixes != null) {
            Collections.addAll(quickFixes, fixes);
          }
        }
      }
      return quickFixes.toArray(LocalQuickFix.EMPTY_ARRAY);
    }
  }
}
