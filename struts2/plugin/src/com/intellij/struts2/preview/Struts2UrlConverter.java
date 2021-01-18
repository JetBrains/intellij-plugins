// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.struts2.preview;

import com.intellij.javaee.web.DeployedFileUrlConverter;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.paths.PathReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.struts2.model.constant.StrutsConstantHelper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Dmitry Avdeev
 */
final class Struts2UrlConverter extends DeployedFileUrlConverter {
  @Override
  public Collection<String> getTargetPaths(@NotNull final PsiFile sourceFile, @NotNull final WebFacet webFacet) {
    final StrutsModel combinedModel = StrutsManager.getInstance(sourceFile.getProject()).getCombinedModel(webFacet.getModule());
    if (combinedModel == null) {
      return Collections.emptyList();
    }

    final List<String> actionExtensions = StrutsConstantHelper.getActionExtensions(sourceFile);
    if (actionExtensions.isEmpty()) {
      return Collections.emptyList();
    }

    final String actionExtension = actionExtensions.get(0);

    @NonNls final ArrayList<String> list = new ArrayList<>();
    combinedModel.processActions(action -> {
      for (final Result result : action.getResults()) {
        final PathReference pathReference = result.getValue();
        if (pathReference != null) {
          final PsiElement psiElement = pathReference.resolve();
          if (psiElement != null && psiElement.equals(sourceFile)) {
            String namespace = action.getNamespace();
            if (!Objects.equals(namespace, StrutsPackage.DEFAULT_NAMESPACE)) {
              namespace += "/";
            }
            list.add(namespace + action.getName().getStringValue() + actionExtension);
          }
        }
      }
      return true;
    });

    return list;
  }
}
