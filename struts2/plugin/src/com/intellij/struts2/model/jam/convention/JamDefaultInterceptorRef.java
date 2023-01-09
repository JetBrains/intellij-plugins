/*
 * Copyright 2014 The authors
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

package com.intellij.struts2.model.jam.convention;

import com.intellij.jam.JamCommonModelElement;
import com.intellij.jam.JamConverter;
import com.intellij.jam.JamElement;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.jam.reflect.JamPackageMeta;
import com.intellij.jam.reflect.JamStringAttributeMeta;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElementRef;
import com.intellij.psi.PsiPackage;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorOrStackBase;
import org.jetbrains.annotations.NonNls;

/**
 * {@code org.apache.struts2.convention.annotation.DefaultInterceptorRef} (package-level only).
 *
 * @author Yann C&eacute;bron
 */
public final class JamDefaultInterceptorRef extends JamCommonModelElement<PsiPackage> implements JamElement {
  @NonNls
  public static final String ANNOTATION_NAME = "org.apache.struts2.convention.annotation.DefaultInterceptorRef";

  private static final JamConverter<InterceptorOrStackBase> VALUE_CONVERTER =
      new InterceptorJamReferenceConverter();

  private static final JamStringAttributeMeta.Single<InterceptorOrStackBase> VALUE_ATTRIBUTE =
      JamAttributeMeta.singleString(PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME, VALUE_CONVERTER);

  private static final JamAnnotationMeta INTERCEPTOR_REF_META =
      new JamAnnotationMeta(ANNOTATION_NAME)
          .addAttribute(VALUE_ATTRIBUTE);

  public static final JamPackageMeta<JamDefaultInterceptorRef> META_PACKAGE =
    new JamPackageMeta<>(JamDefaultInterceptorRef.class, JamDefaultInterceptorRef::new).addAnnotation(INTERCEPTOR_REF_META);

  private JamDefaultInterceptorRef(PsiElementRef<?> ref) {
    super(ref);
  }

  /**
   * Returns "value" attribute.
   *
   * @return JAM-Attribute.
   */
  public JamStringAttributeElement<InterceptorOrStackBase> getValue() {
    return INTERCEPTOR_REF_META.getAttribute(getPsiElement(), VALUE_ATTRIBUTE);
  }
}