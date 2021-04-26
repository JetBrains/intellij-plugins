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

import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableSet;
import com.intellij.protobuf.jvm.PbJavaGotoDeclarationContext;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;
import com.intellij.protobuf.lang.util.BuiltInType;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/** Enumerates the Java element names for java_api_version = 2 with the mutable API. */
public class Proto2MutableNameGenerator implements NameGenerator {

  private final Proto2DefinitionClassNames proto2DefinitionClassNames;

  Proto2MutableNameGenerator(
      PbFile file,
      String descriptorPackage,
      String multiFilePackage,
      String outerClassName,
      boolean isMultipleFiles) {
    String classPackage;
    String classPrefix;
    // Two options need to be set for the mutable API to truly be multi-file.
    if (isMultipleFiles && !multiFilePackage.isEmpty()) {
      classPackage = multiFilePackage;
      classPrefix = ".";
    } else {
      isMultipleFiles = false;
      classPackage = descriptorPackage;
      classPrefix = ".Mutable";
    }
    proto2DefinitionClassNames =
        new Proto2DefinitionClassNames(
            file,
            file.getPackageQualifiedName(),
            descriptorPackage,
            classPackage,
            classPrefix,
            outerClassName,
            isMultipleFiles);
  }

  @Override
  public Set<String> outerClassNames() {
    return proto2DefinitionClassNames.outerClassNames(this);
  }

  @Override
  public Set<String> messageClassNames(PbMessageType messageType) {
    String baseName = proto2DefinitionClassNames.messageClassName(messageType);
    return baseName != null ? ImmutableSet.of(baseName) : ImmutableSet.of();
  }

  /**
   * Specialized format string for proto2 mutable generated names. Format should have a single
   * placeholder for the field name.
   */
  private static final class FieldFormat {
    private final String format;

    FieldFormat(String format) {
      this.format = format;
    }

    String getName(String fieldName) {
      return String.format(format, fieldName);
    }
  }

  private static final class SingularFields {
    private static final FieldFormat[] FIELD_FORMATS = {
      new FieldFormat("has%s"), new FieldFormat("get%s"),
      new FieldFormat("set%s"), new FieldFormat("clear%s"),
    };

    // Messages also get:
    private static final FieldFormat[] MESSAGE_FIELD_FORMATS = {
      new FieldFormat("getMutable%s"),
    };

    // Strings also get:
    private static final FieldFormat[] STRING_FIELD_FORMATS = {
      new FieldFormat("get%sAsBytes"), new FieldFormat("set%sAsBytes"),
    };
  }

  private static final class RepeatedFields {
    private static final FieldFormat[] FIELD_FORMATS = {
      // Non-mutating
      new FieldFormat("get%s"),
      new FieldFormat("get%sCount"),
      new FieldFormat("get%sList"),
      new FieldFormat("getMutable%sList"),
      // Mutating
      new FieldFormat("set%s"),
      new FieldFormat("add%s"),
      new FieldFormat("addAll%s"),
      new FieldFormat("clear%s"),
    };

    // Messages also get:
    private static final FieldFormat[] MESSAGE_FIELD_FORMATS = {new FieldFormat("getMutable%s")};

    // Strings also get:
    private static final FieldFormat[] STRING_FIELD_FORMATS = {
      new FieldFormat("get%sAsBytes"),
      new FieldFormat("get%sListAsBytes"),
      new FieldFormat("add%sAsBytes"),
      new FieldFormat("set%sAsBytes"),
    };
  }

  private static final class MapFields {
    private static final FieldFormat[] FIELD_FORMATS = {
      new FieldFormat("get%s"), new FieldFormat("getMutable%s"), new FieldFormat("putAll%s"),
    };
  }

  private static String fieldConstantName(String fieldName) {
    return Ascii.toUpperCase(fieldName) + "_FIELD_NUMBER";
  }

  @Override
  public Set<String> fieldMemberNames(PbField field) {
    if (field.getName() == null) {
      return ImmutableSet.of();
    }
    String fieldName = NameGenerator.fieldName(field);
    ImmutableSet.Builder<String> fieldNames = ImmutableSet.builder();
    String capitalCamelName = NameUtils.underscoreToCapitalizedCamelCase(fieldName);

    if (field.isRepeated()) {
      if (field instanceof PbMapField) {
        for (FieldFormat fmt : MapFields.FIELD_FORMATS) {
          fieldNames.add(fmt.getName(capitalCamelName));
        }
      } else {
        for (FieldFormat fmt : RepeatedFields.FIELD_FORMATS) {
          fieldNames.add(fmt.getName(capitalCamelName));
        }

        BuiltInType builtInType =
            field.getTypeName() != null ? field.getTypeName().getBuiltInType() : null;
        if (builtInType != null && builtInType.equals(BuiltInType.STRING)) {
          for (FieldFormat fmt : RepeatedFields.STRING_FIELD_FORMATS) {
            fieldNames.add(fmt.getName(capitalCamelName));
          }
        } else if (PbPsiUtil.fieldIsMessage(field)) {
          for (FieldFormat fmt : RepeatedFields.MESSAGE_FIELD_FORMATS) {
            fieldNames.add(fmt.getName(capitalCamelName));
          }
        }
      }
    } else {
      for (FieldFormat fmt : SingularFields.FIELD_FORMATS) {
        fieldNames.add(fmt.getName(capitalCamelName));
      }
      BuiltInType builtInType =
          field.getTypeName() != null ? field.getTypeName().getBuiltInType() : null;
      if (builtInType != null && builtInType.equals(BuiltInType.STRING)) {
        for (FieldFormat fmt : SingularFields.STRING_FIELD_FORMATS) {
          fieldNames.add(fmt.getName(capitalCamelName));
        }
      } else if (PbPsiUtil.fieldIsMessage(field)) {
        for (FieldFormat fmt : SingularFields.MESSAGE_FIELD_FORMATS) {
          fieldNames.add(fmt.getName(capitalCamelName));
        }
      }
    }
    fieldNames.add(fieldConstantName(fieldName));

    return fieldNames.build();
  }

  @Nullable
  @Override
  public String enumClassName(PbEnumDefinition enumDefinition) {
    return proto2DefinitionClassNames.enumClassName(enumDefinition);
  }

  @Nullable
  @Override
  public String enumValueName(PbEnumValue enumValue) {
    return enumValue.getName();
  }

  @Override
  public Set<String> oneofMemberNames(PbOneofDefinition oneof) {
    if (oneof.getName() == null) {
      return ImmutableSet.of();
    }
    String oneofName = oneof.getName();
    String capitalCamelName = NameUtils.underscoreToCapitalizedCamelCase(oneofName);
    return ImmutableSet.of(
        String.format("get%sCase", capitalCamelName), String.format("clear%s", capitalCamelName));
  }

  @Nullable
  @Override
  public String oneofEnumClassName(PbOneofDefinition oneof) {
    return proto2DefinitionClassNames.oneofEnumClassName(oneof);
  }

  @Nullable
  @Override
  public String oneofNotSetEnumValueName(PbOneofDefinition oneof) {
    String name = oneof.getName();
    if (name == null) {
      return null;
    }
    // Camel case transform is mostly to erase the underscores before doing toUpperCase.
    String camelFullUpper = Ascii.toUpperCase(NameUtils.underscoreToCapitalizedCamelCase(name));
    return camelFullUpper + "_NOT_SET";
  }

  @Nullable
  @Override
  public String oneofEnumValueName(PbField oneofField) {
    String name = oneofField.getName();
    return name != null ? Ascii.toUpperCase(name) : null;
  }

  @Override
  public NameMatcher toNameMatcher(PbJavaGotoDeclarationContext context) {
    return new GeneratorBasedNameMatcher(context, this);
  }
}
