/*
 * Copyright 2008 The authors
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
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.paths.PathReferenceManager;
import com.intellij.openapi.paths.PathReferenceProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProviderBase;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides links to Action-URLs for &lt;a&gt; "href".
 *
 * @author Yann CŽbron
 */
public class ActionLinkReferenceProvider extends PsiReferenceProviderBase {

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull final PsiElement element,
                                               @NotNull final ProcessingContext context) {
    final StrutsManager strutsManager = StrutsManager.getInstance(element.getProject());
    final StrutsModel strutsModel = strutsManager.getCombinedModel(ModuleUtil.findModuleForPsiElement(element));

    if (strutsModel == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    return PathReferenceManager.getInstance().createCustomReferences(element,
                                                                     false,
                                                                     new ActionLinkPathReferenceProvider(strutsModel));
  }


  private class ActionLinkPathReferenceProvider implements PathReferenceProvider {

    private final StrutsModel strutsModel;

    ActionLinkPathReferenceProvider(final StrutsModel strutsModel) {
      this.strutsModel = strutsModel;
    }

    public boolean createReferences(@NotNull final PsiElement psiElement,
                                    @NotNull final List<PsiReference> references,
                                    final boolean soft) {
      references.add(new ActionLinkReference((XmlAttributeValue) psiElement, strutsModel));
      return false;
    }

    public PathReference getPathReference(@NotNull final String path, @NotNull final PsiElement element) {
      return new PathReference(path, new PathReference.ConstFunction(StrutsIcons.ACTION));
    }
  }


  private static class ActionLinkReference extends PsiReferenceBase<XmlAttributeValue> implements EmptyResolveMessageProvider {

    private final StrutsModel strutsModel;

    private ActionLinkReference(final XmlAttributeValue psiElement, final StrutsModel strutsModel) {
      super(psiElement);
      this.strutsModel = strutsModel;
    }

    public PsiElement resolve() {
      final String fullActionPath = PathReference.trimPath(myElement.getValue());
      // TODO hardcoded extension
      if (!fullActionPath.endsWith(".action")) {
        return null;
      }

      final String actionName = getActionName(fullActionPath);
      final String namespace = getNamespace(fullActionPath);

      final List<Action> actions = strutsModel.findActionsByName(actionName, namespace);
      if (actions.isEmpty()) {
        return null;
      }

      final Action myAction = actions.get(0);
      return myAction.getXmlTag();
    }

    public Object[] getVariants() {
      final String fullActionPath = PathReference.trimPath(myElement.getValue());
      final String namespace = getNamespace(fullActionPath);
      final List<Action> actionList = strutsModel.getActionsForNamespace(namespace);

      final List<Object> variants = new ArrayList<Object>(actionList.size());
      for (final Action action : actionList) {
        final String actionPath = action.getName().getStringValue();
        if (actionPath != null) {
          final String actionNamespace = action.getNamespace();
          final Object variant =
                  LookupValueFactory.createLookupValueWithHint(
                          (actionNamespace.length() != 1 ? actionNamespace + "/" : "/") +
                          actionPath + ".action",
                          // TODO hardcoded extension
                          StrutsIcons.ACTION,
                          actionNamespace);
          variants.add(variant);
        }
      }
      return variants.toArray(new Object[variants.size()]);
    }

    public String getUnresolvedMessagePattern() {
      return "Cannot resolve action ''" + getCanonicalText() + "''";
    }

    @NotNull
    private static String getNamespace(final String fullActionPath) {
      final int slashIndex = fullActionPath.lastIndexOf("/");
      if (slashIndex == -1) {
        return "";
      }

      // root-package
      if (slashIndex == 0) {
        return "/";
      }

      return fullActionPath.substring(0, slashIndex);
    }


    @NotNull
    private static String getActionName(final String fullActionPath) {
      final int slashIndex = fullActionPath.lastIndexOf("/");
      if (slashIndex == -1) {
        return "";
      }

      final int extensionIndex = fullActionPath.lastIndexOf(".");
      return fullActionPath.substring(slashIndex + 1, extensionIndex);
    }

  }

}