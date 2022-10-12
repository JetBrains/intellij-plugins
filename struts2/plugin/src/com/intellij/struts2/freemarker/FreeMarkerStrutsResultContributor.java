/*
 * Copyright 2012 The authors
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
package com.intellij.struts2.freemarker;

import com.intellij.freemarker.FreemarkerIcons;
import com.intellij.freemarker.psi.files.FtlFileType;
import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.paths.PathReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.struts2.dom.struts.impl.path.FileReferenceSetHelper;
import com.intellij.struts2.dom.struts.impl.path.StrutsResultContributor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Provides path to {@code .ftl}-files.
 */
final class FreeMarkerStrutsResultContributor extends StrutsResultContributor {
  @NonNls
  public static final String FREEMARKER = "freemarker";

  @Override
  protected boolean matchesResultType(@NotNull @NonNls final String resultType) {
    return Objects.equals(resultType, FREEMARKER);
  }

  @Override
  public boolean createReferences(@NotNull final PsiElement psiElement,
                                  final @NotNull List<PsiReference> references,
                                  final boolean soft) {
    final String namespace = getNamespace(psiElement);
    if (namespace == null) {
      return false;
    }


    final FileReferenceSet set = FileReferenceSetHelper.createRestrictedByFileType(psiElement, FtlFileType.INSTANCE);

    final WebFacet webFacet = WebUtil.getWebFacet(psiElement);
    if (webFacet != null) {
      FileReferenceSetHelper.addWebDirectoryAndCurrentNamespaceAsRoots(psiElement, namespace, webFacet, set);
    }

    set.setEmptyPathAllowed(false);
    Collections.addAll(references, set.getAllReferences());
    return true;
  }

  @Override
  public PathReference getPathReference(@NotNull final String path, @NotNull final PsiElement element) {
    return createDefaultPathReference(path, element, FreemarkerIcons.FreemarkerIcon);
  }
}
