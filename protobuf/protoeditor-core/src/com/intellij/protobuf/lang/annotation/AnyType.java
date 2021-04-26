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
package com.intellij.protobuf.lang.annotation;

import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.util.BuiltInType;

import java.util.Objects;

/** A class that represents the google.protobuf.Any type. */
public final class AnyType {
  private final PbMessageType anyMessage;
  private final PbField typeUrlField;
  private final PbField valueField;

  /**
   * Returns an AnyType instance if the given type looks like a google.protobuf.Any message, or
   * <code>null</code> otherwise.
   *
   * <p>This logic is adapted from GetAnyFieldDescriptors at
   * https://github.com/google/protobuf/blob/master/src/google/protobuf/any.cc.
   *
   * @param element the message type to test
   * @return An instanceof AnyType if the given message type looks like google.protobuf.Any
   */
  public static AnyType forElement(PbNamedTypeElement element) {
    if (!(element instanceof PbMessageType)) {
      return null;
    }
    PbMessageType message = (PbMessageType) element;
    QualifiedName qualifiedName = message.getQualifiedName();
    if (qualifiedName == null || !"google.protobuf.Any".equals(qualifiedName.toString())) {
      return null;
    }
    PbField typeUrlField = null;
    PbField valueField = null;
    for (PbField field : message.getSymbols(PbField.class)) {
      PbNumberValue fieldNumberValue = field.getFieldNumber();
      Long fieldNumber = fieldNumberValue != null ? fieldNumberValue.getLongValue() : null;
      PbTypeName fieldType = field.getTypeName();
      if (fieldNumber != null
          && fieldNumber == 1
          && fieldType != null
          && fieldType.getBuiltInType() == BuiltInType.STRING) {
        typeUrlField = field;
      } else if (fieldNumber != null
          && fieldNumber == 2
          && fieldType != null
          && fieldType.getBuiltInType() == BuiltInType.BYTES) {
        valueField = field;
      }
      if (typeUrlField != null && valueField != null) {
        return new AnyType(message, typeUrlField, valueField);
      }
    }
    return null;
  }

  private AnyType(PbMessageType anyMessage, PbField typeUrlField, PbField valueField) {
    this.anyMessage = anyMessage;
    this.typeUrlField = typeUrlField;
    this.valueField = valueField;
  }

  public PbMessageType getMessage() {
    return this.anyMessage;
  }

  public PbField getTypeUrlField() {
    return this.typeUrlField;
  }

  public PbField getValueField() {
    return this.valueField;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.anyMessage);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof AnyType && anyMessage.equals(((AnyType) other).anyMessage);
  }
}
