/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

/** An identifier literal value, such as an enum or boolean. */
public interface ProtoIdentifierValue extends PsiElement, ProtoBooleanValue {
  /** Returns this identifier as a number, such as 'inf' or 'nan'. */
  @Nullable
  ProtoNumberValue getAsNumber();

  /** Returns the leaf node that holds the identifier text. */
  @Nullable
  PsiElement getIdentifierLiteral();
}
