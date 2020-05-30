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

package com.intellij.struts2.reference.common;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.ReferenceSetBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds one or multiple {@link BeanPropertyPathReference}s denoting the full path to a bean property.
 * <p/>
 * Based on Spring plugin.
 *
 * @author Yann C&eacute;bron
 */
public class BeanPropertyPathReferenceSet extends ReferenceSetBase<BeanPropertyPathReference> {

  private final PsiClass beanClass;
  private final boolean supportsReadOnlyProperties;

  public BeanPropertyPathReferenceSet(@NotNull final PsiElement psiElement,
                                      @Nullable final PsiClass beanClass,
                                      final boolean supportsReadOnlyProperties) {
    super(psiElement);
    this.beanClass = beanClass;
    this.supportsReadOnlyProperties = supportsReadOnlyProperties;
  }

  public BeanPropertyPathReferenceSet(final String text,
                                      @NotNull final PsiElement element,
                                      final int offset,
                                      final char separator,
                                      final PsiClass beanClass,
                                      final boolean supportsReadOnlyProperties) {
    super(text, element, offset, separator);
    this.beanClass = beanClass;
    this.supportsReadOnlyProperties = supportsReadOnlyProperties;
  }

  @Override
  @NotNull
  protected BeanPropertyPathReference createReference(final TextRange range, final int index) {
    return createBeanPropertyPathReference(range, index);
  }

  protected BeanPropertyPathReference createBeanPropertyPathReference(final TextRange range, final int index) {
    return new BeanPropertyPathReference(this, range, index);
  }

  @Override
  public BeanPropertyPathReference @NotNull [] getPsiReferences() {
    return getReferences().toArray(new BeanPropertyPathReference[0]);
  }

  @Nullable
  public PsiClass getBeanClass() {
    return beanClass;
  }

  public boolean isSupportsReadOnlyProperties() {
    return supportsReadOnlyProperties;
  }

}