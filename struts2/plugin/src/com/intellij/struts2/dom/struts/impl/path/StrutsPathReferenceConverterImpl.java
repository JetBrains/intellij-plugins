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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.paths.PathReferenceManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlElement;
import com.intellij.struts2.dom.struts.action.StrutsPathReferenceConverter;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Converter for {@code result} location (tag body value).
 *
 * @author Yann C&eacute;bron
 * @see StrutsResultContributor
 */
public class StrutsPathReferenceConverterImpl extends StrutsPathReferenceConverter {

  @Override
  public PathReference fromString(@Nullable final String value, final ConvertContext context) {
    if (value == null) {
      return null;
    }

    final XmlElement element = context.getReferenceXmlElement();
    if (element == null) {
      return null;
    }

    final Module module = context.getModule();
    if (module == null) {
      return null;
    }
    return PathReferenceManager.getInstance().getCustomPathReference(value, module, element,
                                                                     getResultContributors());
  }

  @Override
  public PsiReference @NotNull [] createReferences(@NotNull final PsiElement psiElement, final boolean soft) {
    return PathReferenceManager.getInstance().createCustomReferences(psiElement,
                                                                     soft,
                                                                     getResultContributors());
  }

  private static StrutsResultContributor[] getResultContributors() {
    return StrutsResultContributor.EP_NAME.getExtensions();
  }

}