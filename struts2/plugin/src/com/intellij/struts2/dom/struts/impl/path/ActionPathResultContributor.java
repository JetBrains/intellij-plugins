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

package com.intellij.struts2.dom.struts.impl.path;

import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.openapi.paths.PathReference;
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
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides paths to "/XYZ.action".
 *
 * @author Yann C&eacute;bron
 */
public class ActionPathResultContributor implements StrutsResultContributor {

  public boolean createReferences(@NotNull final PsiElement psiElement,
                                  @NotNull final List<PsiReference> references,
                                  final boolean soft) {
    final StrutsModel model = StrutsManager.getInstance(psiElement.getProject())
            .getModelByFile((XmlFile) psiElement.getContainingFile());
    if (model == null) {
      return false;
    }


    final DomElement resultElement = DomUtil.getDomElement(psiElement);
    if (resultElement == null) {
      return false; // XML syntax error
    }
    final StrutsPackage strutsPackage = resultElement.getParentOfType(StrutsPackage.class, true);
    if (strutsPackage == null) {
      return false; // XML syntax error
    }

    final XmlTag resultTag = resultElement.getXmlTag();
    if (resultTag == null) {
      return false; // XML syntax error
    }

    // skip "chain" results
    final String dispatcherType = resultTag.getAttributeValue("type");
    if (ActionChainOrRedirectResultContributor.isActionChainOrRedirectResult(dispatcherType)) {
      return false;
    }

    final String currentPackage = strutsPackage.searchNamespace();

    final String actionExtension = ".action";

    final PsiReference actionReference = new PsiReferenceBase<PsiElement>(psiElement, soft) {

      public PsiElement resolve() {
        final XmlTagValue tagValue = ((XmlTag) psiElement).getValue();
        final String path = tagValue.getText();
        final int extensionIndex = path.lastIndexOf(actionExtension);
        if (extensionIndex == -1) {
          return null;
        }

        // use given namespace or current if none given
        final int namespacePrefixIndex = path.lastIndexOf("/");
        final String namespace;
        if (namespacePrefixIndex != -1) {
          namespace = path.substring(0, namespacePrefixIndex);
        } else {
          namespace = currentPackage;
        }

        // "/XX/" behind ".action" --> not parseable
        if (namespacePrefixIndex > extensionIndex) {
          return null;
        }

        final String strippedPath = path.substring(namespacePrefixIndex != -1 ? namespacePrefixIndex + 1 : 0,
                                                   extensionIndex);
        final List<Action> actions = model.findActionsByName(strippedPath, namespace);
        if (actions.size() == 1) {
          final Action action = actions.get(0);
          return action.getXmlTag();
        }

        return null;
      }

      public Object[] getVariants() {
        final List<LookupItem<ActionLookupItem>> variants = new ArrayList<LookupItem<ActionLookupItem>>();

        final List<Action> allActions = model.getActionsForNamespace(null);
        for (final Action action : allActions) {
          final String actionPath = action.getName().getStringValue();
          if (actionPath != null) {
            final boolean isInCurrentPackage = action.getNamespace().equals(currentPackage);
            final ActionLookupItem actionItem = new ActionLookupItem(action, isInCurrentPackage);

            // prepend package-name if not default ("/") or "current" package
            final String actionNamespace = action.getNamespace();
            final String fullPath;
            if (!actionNamespace.equals(StrutsPackage.DEFAULT_NAMESPACE) && !isInCurrentPackage) {
              fullPath = actionNamespace + "/" + actionPath + actionExtension;
            } else {
              fullPath = actionPath + actionExtension;
            }

            final LookupItem<ActionLookupItem> item = new LookupItem<ActionLookupItem>(actionItem, fullPath);
            item.setAttribute(LookupItem.OVERWRITE_ON_AUTOCOMPLETE_ATTR, Boolean.TRUE); // TODO does not work
            variants.add(item);
          }
        }

        return variants.toArray(new Object[variants.size()]);
      }
    };

    references.add(actionReference);
    return false;
  }

  @Nullable
  public PathReference getPathReference(@NotNull final String path, @NotNull final PsiElement element) {
    return new PathReference(path, new PathReference.ConstFunction(StrutsIcons.ACTION));
  }

}