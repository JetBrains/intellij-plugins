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
import com.google.common.collect.Multimap;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;
import com.intellij.psi.util.QualifiedName;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Class name generation schemes common to various java_api_version = 2 variants. */
public class Proto2DefinitionClassNames {

  // A suffix that will be appended to the file's outer class name if the name
  // conflicts with some other types defined in the file.
  private static final String OUTER_CLASS_NAME_SUFFIX = "OuterClass";

  private final PbFile file;
  private final QualifiedName protoPackage;

  // The java package for generated descriptor code.
  private final String descriptorPackage;
  // The java package for generated message class code.
  private final String classPackage;
  // Any prefix to attach to class names.
  private final String classPrefix;
  private final String outerClassName;
  private final boolean isMultipleFiles;

  public Proto2DefinitionClassNames(
      PbFile file,
      QualifiedName protoPackage,
      String descriptorPackage,
      String classPackage,
      String classPrefix,
      String outerClassName,
      boolean isMultipleFiles) {
    this.file = file;
    this.protoPackage = protoPackage;
    this.descriptorPackage = descriptorPackage;
    this.classPackage = classPackage;
    this.classPrefix = classPrefix;
    this.outerClassName = outerClassName;
    this.isMultipleFiles = isMultipleFiles;
  }

  public Set<String> outerClassNames(JavaNameGenerator nameGenerator) {
    // There's at least the outer class for the descriptor (or class containing all messages)
    ImmutableSet.Builder<String> names = ImmutableSet.builder();
    names.add(descriptorPackage + classPrefix + outerClassName);
    if (isMultipleFiles) {
      collectMultipleFileOuterClassNames(nameGenerator, names);
    }
    return names.build();
  }

  private void collectMultipleFileOuterClassNames(
    JavaNameGenerator nameGenerator, ImmutableSet.Builder<String> results) {
    // Top level enums and messages (not nested ones).
    // Presence of services depends on the option java_generic_services or plugins.
    // We don't attempt to index services for now.
    for (PbSymbol symbol : file.getPackageSymbolMap(protoPackage).values()) {
      if (PbPsiUtil.isMessageElement(symbol)) {
        results.addAll(nameGenerator.messageClassNames((PbMessageType) symbol));
      } else if (PbPsiUtil.isEnumElement(symbol)) {
        String name = nameGenerator.enumClassName((PbEnumDefinition) symbol);
        if (name != null) {
          results.add(name);
        }
      }
    }
  }

  public static String getDefaultOuterClassName(PbFile file) {
    String defaultName =
        NameUtils.underscoreToCapitalizedCamelCase(
            FileUtil.getNameWithoutExtension(file.getName()));
    if (fileHasConflictingOuterClassName(file, defaultName)) {
      defaultName += OUTER_CLASS_NAME_SUFFIX;
    }
    return defaultName;
  }

  @Nullable
  public String messageClassName(PbMessageType messageType) {
    return typeClassName(messageType);
  }

  @Nullable
  public String enumClassName(PbEnumDefinition enumDefinition) {
    return typeClassName(enumDefinition);
  }

  @Nullable
  public String oneofEnumClassName(PbOneofDefinition oneof) {
    QualifiedName typeName = oneof.getQualifiedName();
    if (typeName == null) {
      return null;
    }
    String caseName = NameUtils.underscoreToCapitalizedCamelCase(oneof.getName()) + "Case";
    QualifiedName fileLocalQualifiers =
        typeName
            .removeHead(protoPackage.getComponentCount())
            .removeLastComponent()
            .append(caseName);
    return typeClassName(fileLocalQualifiers.toString());
  }

  @Nullable
  private String typeClassName(PbNamedTypeElement typeElement) {
    QualifiedName typeName = typeElement.getQualifiedName();
    if (typeName == null) {
      return null;
    }
    QualifiedName fileLocalQualifiers = typeName.removeHead(protoPackage.getComponentCount());
    return typeClassName(fileLocalQualifiers.toString());
  }

  private String typeClassName(String localName) {
    return isMultipleFiles
        ? classPackage + classPrefix + localName
        : classPackage + classPrefix + outerClassName + "." + localName;
  }

  // Checks if ANY type or service name in the file clashes with the default outer class name.
  // See: https://github.com/google/protobuf/blob/3.2.x/src/google/protobuf/compiler/java/java_name_resolver.cc#L131
  // Sadly, this includes nested type names.
  private static boolean fileHasConflictingOuterClassName(PbFile file, String outerClassName) {
    Multimap<String, PbSymbol> packageSymbolMap =
        file.getPackageSymbolMap(file.getPackageQualifiedName());
    if (packageSymbolMap.containsKey(outerClassName)) {
      return true;
    }
    List<PbMessageType> messagesToVisit =
        packageSymbolMap
            .values()
            .stream()
            .filter(PbMessageType.class::isInstance)
            .map(PbMessageType.class::cast)
            .toList();
    for (PbMessageType message : messagesToVisit) {
      if (messageHasConflictingOuterClassName(message, outerClassName)) {
        return true;
      }
    }
    return false;
  }

  private static boolean messageHasConflictingOuterClassName(
      PbMessageType message, String outerClassName) {
    Multimap<String, PbSymbol> symbolMap = message.getSymbolMap();
    Collection<PbSymbol> matches = symbolMap.get(outerClassName);
    if (ContainerUtil.exists(matches, PbNamedTypeElement.class::isInstance)) {
      return true;
    }
    List<PbMessageType> messagesToVisit =
        symbolMap
            .values()
            .stream()
            .filter(PbMessageType.class::isInstance)
            .map(PbMessageType.class::cast)
            .toList();
    for (PbMessageType nestedMessage : messagesToVisit) {
      if (messageHasConflictingOuterClassName(nestedMessage, outerClassName)) {
        return true;
      }
    }
    return false;
  }
}
