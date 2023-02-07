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

package com.intellij.struts2.reference.web;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.javaee.model.CommonParamValue;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReference;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.model.constant.StrutsConstantKey;
import com.intellij.struts2.model.constant.StrutsConstantManager;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.xml.*;
import com.intellij.util.xml.impl.ConvertContextFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Delegates {@link com.intellij.struts2.model.constant.StrutsConstant#getConverter()} references.
 *
 * @author Yann C&eacute;bron
 */
class StrutsConstantValueReference extends PsiReferenceBase<XmlTag> implements EmptyResolveMessageProvider {

  @Nullable
  private final Pair<DomElement, Converter> elementConverterPair;

  StrutsConstantValueReference(@NotNull final XmlTag xmlTag) {
    super(xmlTag, false);
    elementConverterPair = getElementConverterPair();
  }

  @Override
  public PsiElement resolve() {
    if (elementConverterPair == null) {
      return myElement;
    }

    final Converter converter = elementConverterPair.getSecond();
    final ConvertContext convertContext = ConvertContextFactory.createConvertContext(elementConverterPair.first);

    // additional variants (String only)
    if (converter instanceof ResolvingConverter) {
      final Set additionalVariants = ((ResolvingConverter<?>) converter).getAdditionalVariants(convertContext);
      if (additionalVariants.contains(getValue())) {
        return myElement;
      }
    }

    // "normal" reference
    final Object resolveObject = converter.fromString(getValue(), convertContext);
    if (resolveObject == null) {
      return null;
    }

    // DomElement
    if (resolveObject instanceof DomElement) {
      return ((DomElement) resolveObject).getXmlTag();
    }

    // fake self-reference (e.g. String value from Converter)
    if (!(resolveObject instanceof PsiElement)) {
      return myElement;
    }

    // "real" reference
    return (PsiElement) resolveObject;
  }

  @Override
  @NotNull
  public String getUnresolvedMessagePattern() {
    assert elementConverterPair != null;

    return elementConverterPair.second
        .getErrorMessage(getValue(), ConvertContextFactory.createConvertContext(elementConverterPair.first));
  }

  @Override
  @SuppressWarnings({"unchecked"})
  public Object @NotNull [] getVariants() {
    if (elementConverterPair == null) {
      return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
    }

    final Converter converter = elementConverterPair.second;
    if (!(converter instanceof ResolvingConverter resolvingConverter)) {
      return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
    }

    // merge "normal" + additional variants
    final DomElement paramValueElement = elementConverterPair.first;
    final ConvertContext convertContext = ConvertContextFactory.createConvertContext(paramValueElement);

    // wrap explicitly for empty list
    final Collection converterVariants = new ArrayList(resolvingConverter.getVariants(convertContext));

    final Collection variants;
    if (!converterVariants.isEmpty() &&
        converterVariants.iterator().next() instanceof DomElement) {
      variants = Arrays.asList(ElementPresentationManager.getInstance().createVariants(converterVariants));
    } else {
      variants = converterVariants;
    }

    variants.addAll(resolvingConverter.getAdditionalVariants(convertContext));

    // add custom created references
    if (resolvingConverter instanceof CustomReferenceConverter) {
      final PsiReference[] references = ((CustomReferenceConverter) resolvingConverter).
          createReferences((GenericDomValue) paramValueElement,
                           myElement,
                           convertContext);
      for (final PsiReference customReference : references) {
        if (customReference instanceof JavaClassReference javaClassReference) {
          @NotNull List<String> names = javaClassReference.getSuperClasses();
          PsiElement context = javaClassReference.getCompletionContext();
          if (!names.isEmpty() && context instanceof PsiPackage) {
            javaClassReference.processSubclassVariants((PsiPackage)context, ArrayUtil.toStringArray(names), element -> variants.add(element));
            continue;
          }
        }
        Collections.addAll(variants, customReference.getVariants());
      }
    }

    return ArrayUtil.toObjectArray(variants);
  }

  /**
   * Gets the DomElement and corresponding converter.
   *
   * @return {@code null} on errors or if one of both could not be resolved.
   */
  @Nullable
  private Pair<DomElement, Converter> getElementConverterPair() {
    final DomElement paramValueElement = DomUtil.getDomElement(myElement);
    assert paramValueElement != null;

    final DomElement domElement = paramValueElement.getParent();
    assert domElement instanceof CommonParamValue;

    final CommonParamValue initParamElement = (CommonParamValue) domElement;
    final String paramName = initParamElement.getParamName().getStringValue();
    if (StringUtil.isEmpty(paramName)) {
      return null;
    }

    final StrutsConstantManager constantManager = StrutsConstantManager.getInstance(myElement.getProject());

    final Converter converter = constantManager.findConverter(myElement, StrutsConstantKey.create(paramName));
    if (converter == null) {
      return null;
    }

    return Pair.create(paramValueElement, converter);
  }

}