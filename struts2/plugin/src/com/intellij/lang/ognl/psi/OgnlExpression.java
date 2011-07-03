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

package com.intellij.lang.ognl.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 */
public interface OgnlExpression extends PsiElement {

  /**
   * (Result) type of expression.
   *
   * @return {@code null} if not determinable.
   */
  @Nullable
  PsiType getType();

  /**
   * Constant value of expression (if any).
   *
   * @return {@code null} if non-constant or not determinable.
   */
  @Nullable
  Object getConstantValue();

}