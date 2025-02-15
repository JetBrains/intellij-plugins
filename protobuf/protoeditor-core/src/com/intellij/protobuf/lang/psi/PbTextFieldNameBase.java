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

import com.intellij.protobuf.lang.util.BuiltInType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public interface PbTextFieldNameBase extends PbTextElement {

  /** Returns the {@link PbField} instance that this text field is associated with. */
  @Nullable
  PbField getDeclaredField();

  /**
   * Returns the {@link PbNamedTypeElement value type} of this field, or <code>null</code> if it's a
   * built-in type.
   *
   * <p>For regular fields, the type is from the field defined in the associated .proto file. For
   * Any fields, the type is from the type URL defined in the prototext file.
   *
   * @return an associated {@link PbNamedTypeElement type}, or <code>null</code> for built-in types.
   */
  @Nullable
  PbNamedTypeElement getDeclaredNamedType();

  @Nullable
  BuiltInType getDeclaredBuiltInType();
}
