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

package com.intellij.struts2.dom.params;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.ReferenceSetBase;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds one or multiple {@link ParamNameReference}s denoting the full path to a param's property.
 * <p/>
 * Based on Spring plugin.
 *
 * @author Yann C&eacute;bron
 */
class ParamNameReferenceSet extends ReferenceSetBase<ParamNameReference> {

  private final PsiClass beanClass;

  ParamNameReferenceSet(@NotNull final PsiElement psiElement, @Nullable final PsiClass beanClass) {
    super(psiElement);
    this.beanClass = beanClass;
  }

  @NotNull
  protected ParamNameReference createReference(final TextRange range, final int index) {
    return new ParamNameReference(this, range, index);
  }

  @Override
  public ParamNameReference[] getPsiReferences() {
    return ArrayUtil.toObjectArray(getReferences(), ParamNameReference.class);
  }

  @Nullable
  public PsiClass getBeanClass() {
    return beanClass;
  }

}