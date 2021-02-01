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

package com.intellij.struts2.dom.struts.impl.path;

import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.paths.PathReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Provides paths to static web-resources.
 *
 * @author Yann C&eacute;bron
 */
public class DispatchPathResultContributor extends StrutsResultContributor {

  @Override
  protected boolean matchesResultType(@NotNull @NonNls final String resultType) {
    return ResultTypeResolver.isDispatchType(resultType);
  }

  @Override
  public boolean createReferences(@NotNull final PsiElement psiElement,
                                  final @NotNull List<PsiReference> references,
                                  final boolean soft) {

    final String packageNamespace = getNamespace(psiElement);
    if (packageNamespace == null) {
      return false; // XML error
    }

    final WebFacet webFacet = WebUtil.getWebFacet(psiElement);
    if (webFacet == null) {
      return false; // setup error, web-facet must be present in current or dependent module
    }

    final FileReferenceSet fileReferenceSet = FileReferenceSet.createSet(psiElement, soft, false, true);
    FileReferenceSetHelper.addWebDirectoryAndCurrentNamespaceAsRoots(psiElement, packageNamespace, webFacet, fileReferenceSet);
    fileReferenceSet.setEmptyPathAllowed(false);
    Collections.addAll(references, fileReferenceSet.getAllReferences());
    return false;
  }

  @Override
  @Nullable
  public PathReference getPathReference(@NotNull final String path, @NotNull final PsiElement element) {
    return createDefaultPathReference(path, element, null);
  }

}