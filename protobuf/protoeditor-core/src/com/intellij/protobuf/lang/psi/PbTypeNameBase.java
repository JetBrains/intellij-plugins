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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

interface PbTypeNameBase extends PbElement {

  /** Returns <code>true</code> if this is a built-in type. */
  default boolean isBuiltInType() {
    return getBuiltInType() != null;
  }

  /**
   * Returns the associated {@link BuiltInType} instance if this is a built-in type.
   *
   * @return the {@link BuiltInType} instance, or <code>null</code> if this is not a built-in type.
   */
  @Nullable
  BuiltInType getBuiltInType();

  /**
   * Returns the short name for this type. For example, if the type name is specified as
   * "foo.bar.MyType", this method will return "MyType".
   *
   * @return The short type name.
   */
  @NotNull
  String getShortName();
}
