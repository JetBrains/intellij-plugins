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

package com.intellij.struts2.dom.struts.impl;

import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FilePathReferenceProvider;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.ConverterUtil;
import com.intellij.struts2.dom.struts.IncludeFileResolvingConverter;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.GenericDomValue;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Gregory.Shrago
 * @author Yann C&eacute;bron
 */
public class IncludeFileResolvingConverterImpl extends IncludeFileResolvingConverter {

  @Override
  public PsiFile fromString(@Nullable @NonNls final String value, final ConvertContext context) {
    if (value == null) {
      return null;
    }

    final XmlElement xmlElement = context.getReferenceXmlElement();
    if (xmlElement == null) {
      return null;
    }

    final PsiReference[] references = createReferences((GenericDomValue) context.getInvocationElement(),
                                                       xmlElement,
                                                       context);
    if (references.length == 0) {
      return null;
    }

    final PsiElement element = references[references.length - 1].resolve();
    return element instanceof PsiFile ? (PsiFile) element : null;
  }

  @Override
  @NotNull
  public Collection<? extends PsiFile> getVariants(final ConvertContext context) {
    return Collections.emptyList();
  }

  @Override
  public PsiElement resolve(final PsiFile psiFile, final ConvertContext context) {
    // recursive self-inclusion
    if (context.getFile().equals(psiFile)) {
      return null;
    }

    final StrutsModel model = ConverterUtil.getStrutsModel(context);
    if (model == null) {
      return null;
    }

    return isFileAccepted(model, psiFile) ? super.resolve(psiFile, context) : null;
  }

  @Override
  public PsiReference @NotNull [] createReferences(@NotNull final GenericDomValue genericDomValue,
                                                   @NotNull final PsiElement element,
                                                   @NotNull final ConvertContext context) {
    final String s = genericDomValue.getStringValue();
    if (s == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    final StrutsModel model = ConverterUtil.getStrutsModel(context);
    if (model == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    final int offset = ElementManipulators.getOffsetInElement(element);
    return new FilePathReferenceProvider() {
      @Override
      protected boolean isPsiElementAccepted(final PsiElement element) {
        return super.isPsiElementAccepted(element) &&
               (!(element instanceof PsiFile) || isFileAccepted(model, (PsiFile) element));
      }
    }.getReferencesByElement(element, s, offset, true);
  }

  @Override
  public String getErrorMessage(@Nullable final String value, final ConvertContext context) {
    // check if user tries to include current file
    if (Objects.equals(context.getFile().getName(), value)) {
      return "Recursive inclusion of current file";
    }

    // TODO check for cyclic include

    return "Cannot resolve file ''" + value + "'' (not in file set of including file?)";
  }

  private static boolean isFileAccepted(@NotNull final StrutsModel model, @NotNull final PsiFile file) {
    if (!(file instanceof XmlFile xmlFile)) {
      return false;
    }

    final Set<XmlFile> files = model.getConfigFiles();
    return files.contains(xmlFile);
  }

}