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

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.jvm.PbJavaGotoDeclarationContext;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;
import com.intellij.protobuf.lang.util.BuiltInType;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/** Enumerates the Java element names for java_api_version = 1. */
public class Proto1NameGenerator implements NameGenerator {

  private final PbFile file;
  private final String javaPackage;
  private final QualifiedName protoPackage;

  Proto1NameGenerator(PbFile file, String javaPackage) {
    this.file = file;
    this.javaPackage = javaPackage;
    this.protoPackage = file.getPackageQualifiedName();
  }

  @Override
  public Set<String> outerClassNames() {
    ImmutableSet.Builder<String> names = ImmutableSet.builder();
    // Messages (even nested ones) get their own outer class / file.
    // Groups and enums do not get their own class.
    // Map fields generate an outer class to represent the "Entry" as well, it seems unlikely
    // for someone to want to cross-reference that.
    for (PbSymbol symbol : file.getLocalQualifiedSymbolMap().values()) {
      if (symbol instanceof PbMessageDefinition) {
        names.addAll(messageClassNames((PbMessageDefinition) symbol));
      }
    }

    return names.build();
  }

  @Override
  public Set<String> messageClassNames(PbMessageType messageType) {
    QualifiedName messageName = messageType.getQualifiedName();
    if (messageName == null) {
      return ImmutableSet.of();
    }
    QualifiedName fileLocalQualifiers = messageName.removeHead(protoPackage.getComponentCount());
    // Group message types don't get their own file, while plain message types do get
    // their own file (and are named differently).
    String name =
        messageType instanceof PbMessageDefinition
            ? typeNameOwnFile(fileLocalQualifiers)
            : typeNameNestedInFile(fileLocalQualifiers);
    return ImmutableSet.of(name);
  }

  private String typeNameOwnFile(QualifiedName localName) {
    return javaPackage + "." + localName.join("_");
  }

  private String typeNameNestedInFile(QualifiedName localName) {
    return javaPackage + "." + localName;
  }

  @Override
  public Set<String> fieldMemberNames(PbField field) {
    if (field.getName() == null) {
      return ImmutableSet.of();
    }
    String fieldName = NameGenerator.fieldName(field);
    ImmutableSet.Builder<String> fieldNames = ImmutableSet.builder();
    // If there is no prefix, use camelName. Otherwise use capitalCamelName.
    String camelName = NameUtils.underscoreToCamelCase(fieldName);
    String capitalCamelName = NameUtils.underscoreToCapitalizedCamelCase(fieldName);

    if (field.isRepeated()) {
      BuiltInType builtInType =
          field.getTypeName() != null ? field.getTypeName().getBuiltInType() : null;
      if (builtInType != null && builtInType.getName().equals("bool")) {
        fieldNames.add("is" + capitalCamelName);
      } else {
        fieldNames.add("get" + capitalCamelName);
      }
      fieldNames.add("set" + capitalCamelName);
      fieldNames.add("clear" + capitalCamelName);

      fieldNames.add(camelName + "Iterator");
      fieldNames.add(camelName + "s");
      fieldNames.add(camelName + "Size");

      fieldNames.add("add" + capitalCamelName);
      if (builtInType != null) {
        switch (builtInType.getName()) {
          case "string":
          case "bytes":
            fieldNames.add(camelName + "sAsBytes");
            fieldNames.add(camelName + "AsBytesIterator");
            fieldNames.add("add" + capitalCamelName + "AsBytes");
            fieldNames.add("get" + capitalCamelName + "AsBytes");
            fieldNames.add("set" + capitalCamelName + "AsBytes");
            break;
          default:
            // no additional string-related field names.
            break;
        }
      } else if (PbPsiUtil.fieldIsMessage(field) || field instanceof PbMapField) {
        fieldNames.add("insert" + capitalCamelName);
        fieldNames.add("remove" + capitalCamelName);
      }
      fieldNames.add("mutable" + capitalCamelName + "s");
      fieldNames.add("getMutable" + capitalCamelName);
    } else {
      BuiltInType builtInType =
          field.getTypeName() != null ? field.getTypeName().getBuiltInType() : null;
      fieldNames.add("has" + capitalCamelName);
      if (builtInType != null && builtInType.getName().equals("bool")) {
        fieldNames.add("is" + capitalCamelName);
      } else {
        fieldNames.add("get" + capitalCamelName);
      }
      fieldNames.add("set" + capitalCamelName);
      fieldNames.add("clear" + capitalCamelName);

      if (builtInType != null) {
        switch (builtInType.getName()) {
          case "string":
          case "bytes":
            fieldNames.add("get" + capitalCamelName + "AsBytes");
            fieldNames.add("set" + capitalCamelName + "AsBytes");
            break;
          default:
            // no additional string-related field names.
            break;
        }
      } else if (PbPsiUtil.fieldIsMessage(field)) {
        fieldNames.add("getMutable" + capitalCamelName);
      }
    }

    return fieldNames.build();
  }

  @Nullable
  @Override
  public String enumClassName(PbEnumDefinition enumDefinition) {
    // TODO(jvoung): Check the various options ("option java_java5_enums").
    // By default you don't actually get a java enum.
    return null;
  }

  @Nullable
  @Override
  public String enumValueName(PbEnumValue enumValue) {
    return null;
  }

  @Override
  public Set<String> oneofMemberNames(PbOneofDefinition oneof) {
    return ImmutableSet.of();
  }

  @Nullable
  @Override
  public String oneofEnumClassName(PbOneofDefinition oneof) {
    return null;
  }

  @Nullable
  @Override
  public String oneofNotSetEnumValueName(PbOneofDefinition oneof) {
    return null;
  }

  @Nullable
  @Override
  public String oneofEnumValueName(PbField oneofField) {
    return null;
  }

  @Override
  public NameMatcher toNameMatcher(PbJavaGotoDeclarationContext context) {
    return new GeneratorBasedNameMatcher(context, this);
  }
}
