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

package com.intellij.struts2.reference.jsp;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInsight.lookup.LookupValueFactory;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProviderBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.jsp.TaglibUtil;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom tags attribute "action".
 *
 * @author Yann CŽbron
 */
public class ActionReferenceProvider extends PsiReferenceProviderBase {

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull final PsiElement psiElement, @NotNull final ProcessingContext context) {
    final StrutsManager strutsManager = StrutsManager.getInstance(psiElement.getProject());
    final StrutsModel strutsModel = strutsManager.getCombinedModel(ModuleUtil.findModuleForPsiElement(psiElement));

    if (strutsModel == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    return new PsiReference[]{new ActionReference((XmlAttributeValue) psiElement, strutsModel)
    };
  }

  private static class ActionReference extends PsiReferenceBase<XmlAttributeValue> implements EmptyResolveMessageProvider {

    private final StrutsModel strutsModel;

    private ActionReference(final XmlAttributeValue psiElement, final StrutsModel strutsModel) {
      super(psiElement);
      this.strutsModel = strutsModel;
    }

    public PsiElement resolve() {
      final String actionName = myElement.getValue();
      final String namespace = getNamespace();

      if (TaglibUtil.isDynamicExpression(actionName)) {
        return myElement;
      }

      final List<Action> actions = strutsModel.findActionsByName(actionName, namespace);
      if (actions.isEmpty()) {
        return null;
      }

      // TODO return polys
      final Action myAction = actions.get(0);
      return myAction.getXmlTag();
    }

    public Object[] getVariants() {
      final List<Action> actionList = strutsModel.getActionsForNamespace(getNamespace());

      final List<Object> variants = new ArrayList<Object>(actionList.size());
      for (final Action action : actionList) {
        final String actionPath = action.getName().getStringValue();
        if (actionPath != null) {
          final Object variant =
              LookupValueFactory.createLookupValueWithHint(actionPath, StrutsIcons.ACTION,
                                                           action.getNamespace());
          variants.add(variant);
        }
      }
      return variants.toArray(new Object[variants.size()]);
    }

    public String getUnresolvedMessagePattern() {
      return "Cannot resolve action ''" + getCanonicalText() + "''";
    }

    @Nullable
    private String getNamespace() {
      final XmlTag tag = PsiTreeUtil.getParentOfType(myElement, XmlTag.class);
      if (tag == null) {
        return null;
      }

      final XmlAttribute namespaceAttribute = tag.getAttribute("namespace");
      return namespaceAttribute != null ? namespaceAttribute.getValue() : null;
    }

  }

}