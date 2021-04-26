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
package com.intellij.protobuf.lang.descriptor;

import com.intellij.psi.util.QualifiedName;

/** Proto descriptor option type name constants. */
public enum DescriptorOptionType {
  FILE_OPTIONS("FileOptions"),
  MESSAGE_OPTIONS("MessageOptions"),
  FIELD_OPTIONS("FieldOptions"),
  ONEOF_OPTIONS("OneofOptions"),
  ENUM_OPTIONS("EnumOptions"),
  ENUM_VALUE_OPTIONS("EnumValueOptions"),
  SERVICE_OPTIONS("ServiceOptions"),
  METHOD_OPTIONS("MethodOptions"),
  STREAM_OPTIONS("StreamOptions"),
  EXTENSION_RANGE_OPTIONS("ExtensionRangeOptions");

  /**
   * A simple structure that is able to generate a {@link QualifiedName} for a {@link Descriptor}
   * member.
   */
  private final String name;

  DescriptorOptionType(String name) {
    this.name = name;
  }

  public QualifiedName forDescriptor(Descriptor descriptor) {
    return descriptor.getFile().getPackageQualifiedName().append(name);
  }
}
