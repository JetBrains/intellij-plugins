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

import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.lang.descriptor.Descriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Implemented by elements that support options (fields, messages, files, etc.) */
public interface PbOptionOwner extends PbElement {

  @NotNull
  List<PbOptionExpression> getOptions();

  @NotNull
  QualifiedName getDescriptorOptionsTypeName(Descriptor descriptor);

  /**
   * Returns the scope to start searching for an option extension field.
   *
   * @return the scope to search.
   */
  @Nullable
  QualifiedName getExtensionOptionScope();
}
