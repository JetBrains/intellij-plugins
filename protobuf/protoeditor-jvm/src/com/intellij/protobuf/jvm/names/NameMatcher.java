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
package com.intellij.protobuf.jvm.names;

import com.intellij.protobuf.jvm.PbJavaGotoDeclarationContext;
import com.intellij.protobuf.lang.psi.*;

/**
 * Matches java elements specified by a context ({@link
 * PbJavaGotoDeclarationContext}), to proto elements
 * defined in a given {@link PbFile}.
 */
public interface NameMatcher {

  /** Returns <code>true</code> if the message generates a class that matches the context class. */
  boolean matchesMessage(PbMessageType messageType);

  /**
   * Returns <code>true</code> if the field generates a element that matches the context element.
   */
  boolean matchesField(PbField field);

  /** Returns <code>true</code> if the enum generates java code that matches the context class. */
  boolean matchesEnum(PbEnumDefinition enumDefinition);

  /**
   * Returns <code>true</code> if the enum value generates a java element that matches the context
   * element.
   */
  boolean matchesEnumValue(PbEnumValue enumValue);

  /**
   * Returns <code>true</code> if the oneof definition generates java class members that matches the
   * context element.
   */
  boolean matchesOneofMember(PbOneofDefinition oneof);

  /**
   * Returns <code>true</code> if the oneof definition generates a java enum that matches the
   * context class.
   */
  boolean matchesOneofEnum(PbOneofDefinition oneof);

  /**
   * Returns <code>true</code> if the oneof definition generates a java enum value for "not set"
   * that matches the context element.
   */
  boolean matchesOneofNotSetEnumValue(PbOneofDefinition oneof);

  /**
   * Returns <code>true</code> if the oneof field generates a java enum value that matches the
   * context element.
   */
  boolean matchesOneofEnumValue(PbField oneofField);
}
