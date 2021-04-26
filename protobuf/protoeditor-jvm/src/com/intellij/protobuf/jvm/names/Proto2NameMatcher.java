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

import com.intellij.psi.PsiClass;
import com.intellij.protobuf.jvm.PbJavaGotoDeclarationContext;
import com.intellij.protobuf.lang.psi.*;

import java.util.Objects;

/**
 * Name matcher that accounts for proto2 immutable vs mutable API (context class may be a builder).
 */
class Proto2NameMatcher implements NameMatcher {

  private final String fullyQualifiedClassName;
  private final String elementName;
  private final NameGenerator generator;

  Proto2NameMatcher(PbJavaGotoDeclarationContext context, NameGenerator generator) {
    PsiClass normalizedContextClass = normalizeClassContext(context.javaClass);
    String elementName = context.resolvedElement.getName();
    if (normalizedContextClass != context.javaClass) {
      elementName = "Builder." + elementName;
    }
    this.fullyQualifiedClassName = normalizedContextClass.getQualifiedName();
    this.elementName = elementName;
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

  /** If classContext is a Builder, convert from Builder to the actual class. */
  private static PsiClass normalizeClassContext(PsiClass classContext) {
    if (classContext.isEnum()) {
      return classContext;
    }
    String name = classContext.getName();
    if (name == null) {
      return classContext;
    }
    // It would be bad if a message was simply named "Builder".
    // We could refine the heuristic by checking that the class extends
    // from GeneratedMessageVN.Builder.
    if (name.equals("Builder")) {
      PsiClass containingClass = classContext.getContainingClass();
      return containingClass != null ? containingClass : classContext;
    }
    return classContext;
  }
}
