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

import java.util.Objects;

/** Basic name matcher that just uses the supplied name generator to match. */
class GeneratorBasedNameMatcher implements NameMatcher {

  private final String fullyQualifiedClassName;
  private final String elementName;
  private final NameGenerator generator;

  GeneratorBasedNameMatcher(PbJavaGotoDeclarationContext context, NameGenerator generator) {
    this.fullyQualifiedClassName = context.javaClass.getQualifiedName();
    this.elementName = context.resolvedElement.getName();
    this.generator = generator;
  }

  @Override
  public boolean matchesMessage(PbMessageType messageType) {
    return generator.messageClassNames(messageType).contains(fullyQualifiedClassName);
  }

  @Override
  public boolean matchesField(PbField field) {
    return generator.fieldMemberNames(field).contains(elementName);
  }

  @Override
  public boolean matchesEnum(PbEnumDefinition enumDefinition) {
    return Objects.equals(fullyQualifiedClassName, generator.enumClassName(enumDefinition));
  }

  @Override
  public boolean matchesEnumValue(PbEnumValue enumValue) {
    return Objects.equals(generator.enumValueName(enumValue), elementName);
  }

  @Override
  public boolean matchesOneofMember(PbOneofDefinition oneof) {
    return generator.oneofMemberNames(oneof).contains(elementName);
  }

  @Override
  public boolean matchesOneofEnum(PbOneofDefinition oneof) {
    return Objects.equals(fullyQualifiedClassName, generator.oneofEnumClassName(oneof));
  }

  @Override
  public boolean matchesOneofNotSetEnumValue(PbOneofDefinition oneof) {
    return Objects.equals(elementName, generator.oneofNotSetEnumValueName(oneof));
  }

  @Override
  public boolean matchesOneofEnumValue(PbField oneofField) {
    return Objects.equals(elementName, generator.oneofEnumValueName(oneofField));
  }
}
