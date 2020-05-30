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

package com.intellij.struts2.reference.jsp;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.reference.TaglibUtil;
import com.intellij.struts2.reference.common.BeanPropertyPathReferenceSet;
import com.intellij.util.ProcessingContext;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Refers to bean property of "enclosing" Action-class.
 *
 * @author Yann C&eacute;bron
 */
public class ActionPropertyReferenceProvider extends PsiReferenceProvider {

  private final boolean supportsReadOnlyProperties;

  public ActionPropertyReferenceProvider(final boolean supportsReadOnlyProperties) {
    this.supportsReadOnlyProperties = supportsReadOnlyProperties;
  }

  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement psiElement,
                                                         @NotNull final ProcessingContext processingContext) {
    if (TaglibUtil.isDynamicExpression(((XmlAttributeValue) psiElement).getValue())) {
      return PsiReference.EMPTY_ARRAY;
    }

    final XmlTag tag = PsiTreeUtil.getParentOfType(psiElement, XmlTag.class);
    assert tag != null;

    final XmlTag actionTag = findEnclosingTag(tag, tag.getNamespacePrefix());
    if (actionTag == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    final String actionName = Objects.equals(actionTag.getLocalName(), "action") ?
                              actionTag.getAttributeValue("name") : actionTag.getAttributeValue("action");
    if (actionName == null || TaglibUtil.isDynamicExpression(actionName)) {
      return PsiReference.EMPTY_ARRAY;
    }

    final StrutsManager strutsManager = StrutsManager.getInstance(psiElement.getProject());
    final StrutsModel strutsModel = strutsManager.getCombinedModel(psiElement);
    if (strutsModel == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    final List<Action> actions = strutsModel.findActionsByName(actionName, actionTag.getAttributeValue("namespace"));
    if (actions.size() != 1) {
      return PsiReference.EMPTY_ARRAY;
    }

    final Action action = actions.get(0);
    return new BeanPropertyPathReferenceSet(psiElement, action.searchActionClass(), supportsReadOnlyProperties) {

      // TODO CTOR creates references eagerly, so we have to subclass here
      @Override
      public boolean isSoft() {
        return false;
      }

    }.getPsiReferences();
  }

  @NonNls
  private static final String[] TAGS_WITH_ACTION_ATTRIBUTE = new String[]{"a", "action", "form", "reset", "submit", "url"};

  @Nullable
  private static XmlTag findEnclosingTag(@NotNull final XmlTag xmlTag,
                                         @NotNull final String namespacePrefix) {
    final XmlTag tag = PsiTreeUtil.getParentOfType(xmlTag, XmlTag.class);
    if (tag == null) {
      return null;
    }

    if (tag.getNamespacePrefix().equals(namespacePrefix) &&
        Arrays.binarySearch(TAGS_WITH_ACTION_ATTRIBUTE, tag.getLocalName()) > -1) {
      return tag;
    }

    return findEnclosingTag(tag, namespacePrefix);
  }

}