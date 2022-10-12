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
package com.intellij.struts2.velocity;

import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.paths.PathReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.struts2.dom.struts.impl.path.FileReferenceSetHelper;
import com.intellij.struts2.dom.struts.impl.path.StrutsResultContributor;
import com.intellij.velocity.Icons;
import com.intellij.velocity.psi.files.VtlFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Provides path to {@code .vm}-files.
 *
 * @author Yann C&eacute;bron
 */
final class VelocityStrutsResultContributor extends StrutsResultContributor {
  @NonNls
  private static final String VELOCITY = "velocity";

  @Override
  protected boolean matchesResultType(@NotNull @NonNls final String resultType) {
    return VELOCITY.equals(resultType);
  }

  @Override
  public boolean createReferences(@NotNull final PsiElement psiElement,
                                  final @NotNull List<PsiReference> references,
                                  final boolean soft) {
    final String namespace = getNamespace(psiElement);
    if (namespace == null) {
      return false;
    }

    final WebFacet webFacet = WebUtil.getWebFacet(psiElement);
    if (webFacet == null) {
      return false; // setup error, web-facet must be present in current or dependent module
    }

    final FileReferenceSet set = FileReferenceSetHelper.createRestrictedByFileType(psiElement, VtlFileType.INSTANCE);

    FileReferenceSetHelper.addWebDirectoryAndCurrentNamespaceAsRoots(psiElement, namespace, webFacet, set);
    set.setEmptyPathAllowed(false);
    Collections.addAll(references, set.getAllReferences());
    return true;
  }

  @Override
  public PathReference getPathReference(@NotNull final String path, @NotNull final PsiElement element) {
    return createDefaultPathReference(path, element, Icons.VTL_ICON);
  }

}