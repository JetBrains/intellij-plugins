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

package com.intellij.struts2.dom.struts.impl.path;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagValue;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ConstantFunction;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides paths to "actionName" for results with type="chain" and "redirectAction".
 *
 * @author Yann C&eacute;bron
 */
public class ActionChainOrRedirectResultContributor extends StrutsResultContributor {

  @Override
  public boolean matchesResultType(@NotNull @NonNls final String resultType) {
    return ResultTypeResolver.isChainOrRedirectType(resultType);
  }

  public boolean createReferences(@NotNull final PsiElement psiElement,
                                  @NotNull final List<PsiReference> references,
                                  final boolean soft) {
    final StrutsModel model = StrutsManager.getInstance(psiElement.getProject())
      .getModelByFile((XmlFile) psiElement.getContainingFile());
    if (model == null) {
      return false;
    }

    final String currentPackage = getNamespace(psiElement);
    if (currentPackage == null) {
      return false;
    }

    final PsiReference chainReference = new ActionChainReference(psiElement, currentPackage, model);
    references.add(chainReference);
    return true;
  }

  @Nullable
  public PathReference getPathReference(@NotNull final String path, @NotNull final PsiElement element) {
    final StrutsModel model = StrutsManager.getInstance(element.getProject())
      .getModelByFile((XmlFile) element.getContainingFile());
    if (model == null) {
      return null;
    }

    final String currentPackage = getNamespace(element);
    if (currentPackage == null) {
      return null;
    }

    final PsiElement actionTag = resolveActionPath(element, currentPackage, model);
    if (actionTag == null) {
      return null;
    }

    return new PathReference(path, new ConstantFunction<PathReference, Icon>(StrutsIcons.ACTION)) {
      @Override
      public PsiElement resolve() {
        return actionTag;
      }
    };
  }

  @Nullable
  private static PsiElement resolveActionPath(@NotNull final PsiElement psiElement,
                                              @NotNull @NonNls final String currentPackage,
                                              @NotNull final StrutsModel model) {
    final XmlTagValue tagValue = ((XmlTag) psiElement).getValue();
    final String path = PathReference.trimPath(tagValue.getText());

    // use given namespace or current if none given
    final int namespacePrefixIndex = path.lastIndexOf("/");
    final String namespace;
    if (namespacePrefixIndex != -1) {
      namespace = path.substring(0, namespacePrefixIndex);
    } else {
      namespace = currentPackage;
    }

    final String strippedPath = path.substring(namespacePrefixIndex != -1 ? namespacePrefixIndex + 1 : 0);
    final List<Action> actions = model.findActionsByName(strippedPath, namespace);
    if (actions.size() == 1) {
      final Action action = actions.get(0);
      return action.getXmlTag();
    }

    return null;
  }


  private static class ActionChainReference extends PsiReferenceBase<PsiElement> implements EmptyResolveMessageProvider {

    private final PsiElement psiElement;
    private final String currentPackage;
    private final StrutsModel model;

    private ActionChainReference(final PsiElement psiElement,
                                 final String currentPackage,
                                 final StrutsModel model) {
      super(psiElement, true);
      this.psiElement = psiElement;
      this.currentPackage = currentPackage;
      this.model = model;
    }

    public PsiElement resolve() {
      return resolveActionPath(psiElement, currentPackage, model);
    }

    @NotNull
    public Object[] getVariants() {
      final List<Action> allActions = model.getActionsForNamespace(null);
      final List<LookupElementBuilder> variants = new ArrayList<LookupElementBuilder>(allActions.size());
      for (final Action action : allActions) {
        final String actionPath = action.getName().getStringValue();
        if (actionPath != null) {
          final boolean isInCurrentPackage = Comparing.equal(action.getNamespace(), currentPackage);

          // prepend package-name if not default ("/") or "current" package
          final String actionNamespace = action.getNamespace();
          final String fullPath;
          if (!Comparing.equal(actionNamespace, StrutsPackage.DEFAULT_NAMESPACE) &&
              !isInCurrentPackage) {
            fullPath = actionNamespace + "/" + actionPath;
          } else {
            fullPath = actionPath;
          }

          final LookupElementBuilder builder = LookupElementBuilder.create(action.getXmlTag(), fullPath)
            .setBold(isInCurrentPackage)
            .setIcon(StrutsIcons.ACTION)
            .setTypeText(action.getNamespace());
          variants.add(builder);
        }
      }

      return ArrayUtil.toObjectArray(variants);
    }

    @Override
    public String getUnresolvedMessagePattern() {
      return "Cannot resolve Action ''" + getValue() + "''";
    }

  }

}
