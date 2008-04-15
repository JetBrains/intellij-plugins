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
import com.intellij.javaee.web.CustomServletReferenceAdapter;
import com.intellij.javaee.web.ServletMappingInfo;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides links to Action-URLs in all places where Servlet-URLs are processed.
 *
 * @author Yann CŽbron
 */
public class ActionLinkReferenceProvider extends CustomServletReferenceAdapter {

  protected PsiReference[] createReferences(final @NotNull PsiElement psiElement,
                                            final int offset,
                                            final String text,
                                            final @Nullable ServletMappingInfo info,
                                            final boolean soft) {
    final StrutsModel strutsModel = StrutsManager.getInstance(psiElement.getProject()).
            getCombinedModel(ModuleUtil.findModuleForPsiElement(psiElement));

    if (strutsModel == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    return new PsiReference[]{new ActionLinkReference((XmlElement) psiElement, offset, text, soft, strutsModel)};
  }

  @Nullable
  public PathReference createWebPath(final String path,
                                     @NotNull final PsiElement psiElement,
                                     final ServletMappingInfo servletMappingInfo) {
    final StrutsManager strutsManager = StrutsManager.getInstance(psiElement.getProject());
    if (strutsManager.getCombinedModel(ModuleUtil.findModuleForPsiElement(psiElement)) == null) {
      return null;
    }

    return new PathReference(path, new PathReference.ConstFunction(StrutsIcons.ACTION)); /*{
TODO not needed so far ?!
   public PsiElement resolve() {
        return action.getXmlTag();
      }
    };*/
  }


  private static class ActionLinkReference extends PsiReferenceBase<XmlElement> implements EmptyResolveMessageProvider {

    private final StrutsModel strutsModel;

    private ActionLinkReference(final XmlElement element,
                                final int offset,
                                final String text,
                                final boolean soft,
                                final StrutsModel strutsModel) {
      super(element, new TextRange(offset, offset + text.length()), soft);
      this.strutsModel = strutsModel;
    }

    public PsiElement resolve() {
      final String fullActionPath = PathReference.trimPath(getValue());
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
      final String fullActionPath = PathReference.trimPath(getValue());
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

      // no slash, use fake "root" for resolving "myAction.action"
      if (slashIndex == -1) {
        return "/";
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
      final int extensionIndex = fullActionPath.lastIndexOf(".");
      return fullActionPath.substring(slashIndex + 1, extensionIndex);
    }

  }

}