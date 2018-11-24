/*
 * Copyright 2016 The authors
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

import com.intellij.jam.JamConverter;
import com.intellij.jam.JamElement;
import com.intellij.jam.JamPomTarget;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.annotations.JamPsiValidity;
import com.intellij.jam.model.common.CommonModelElement;
import com.intellij.jam.reflect.*;
import com.intellij.pom.PomTarget;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMember;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorOrStackBase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@code org.apache.struts2.convention.annotation.InterceptorRef(s)}.
 *
 * @author Yann C&eacute;bron
 */
public abstract class JamInterceptorRef extends CommonModelElement.PsiBase implements JamElement {

  @NonNls
  public static final String ANNOTATION_NAME = "org.apache.struts2.convention.annotation.InterceptorRef";

  @NonNls
  public static final String ANNOTATION_NAME_LIST = "org.apache.struts2.convention.annotation.InterceptorRefs";

  private static final JamConverter<InterceptorOrStackBase> VALUE_CONVERTER = new InterceptorJamReferenceConverter();

  private static final JamStringAttributeMeta.Single<InterceptorOrStackBase> VALUE_ATTRIBUTE =
    JamAttributeMeta.singleString(PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME, VALUE_CONVERTER);

  private static final JamAnnotationMeta INTERCEPTOR_REF_META =
    new JamAnnotationMeta(ANNOTATION_NAME)
      .addAttribute(VALUE_ATTRIBUTE);

  public static final JamClassMeta<JamInterceptorRef> META_CLASS =
    new JamClassMeta<>(JamInterceptorRef.class).addAnnotation(INTERCEPTOR_REF_META);


  private static final JamAnnotationAttributeMeta.Collection<JamInterceptorRef> INTERCEPTOR_REFS_VALUE_ATTRIBUTE =
    JamAttributeMeta.annoCollection(PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME, INTERCEPTOR_REF_META, JamInterceptorRef.class)
      .addPomTargetProducer((named, pomTargetConsumer) -> {
        final PomTarget target = named.getPomTarget();
        if (target != null) pomTargetConsumer.consume(target);
      });

  private static final JamAnnotationMeta INTERCEPTOR_REFS_META =
    new JamAnnotationMeta(ANNOTATION_NAME_LIST).addAttribute(INTERCEPTOR_REFS_VALUE_ATTRIBUTE);

  public static final JamClassMeta<JamInterceptorRef> META_CLASS_LIST =
    new JamClassMeta<>(JamInterceptorRef.class).addAnnotation(INTERCEPTOR_REFS_META);

  @JamPsiConnector
  public abstract PsiMember getOwner();

  @JamPsiValidity
  @Override
  public abstract boolean isValid();

  @NotNull
  @Override
  public PsiElement getPsiElement() {
    return getOwner();
  }

  @Nullable
  private PomTarget getPomTarget() {
    final JamStringAttributeElement<InterceptorOrStackBase> valueAttribute = getValue();
    if (valueAttribute.getPsiLiteral() == null) {
      return null;
    }
    return new JamPomTarget(this, valueAttribute);
  }

  /**
   * Returns "value" attribute.
   *
   * @return JAM-Attribute.
   */
  public JamStringAttributeElement<InterceptorOrStackBase> getValue() {
    return INTERCEPTOR_REF_META.getAttribute(getOwner(), VALUE_ATTRIBUTE);
  }

}
