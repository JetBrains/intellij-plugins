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

/** Enumerates the Java element for java_api_version = 2. */
public class Proto2NameGenerator implements JavaNameGenerator {

  private static final String CLASS_PREFIX = ".";
  private final Proto2DefinitionClassNames proto2DefinitionClassNames;

  public Proto2NameGenerator(
      PbFile file, String javaPackage, String outerClassName, boolean isMultipleFiles) {
    proto2DefinitionClassNames =
        new Proto2DefinitionClassNames(
            file,
            file.getPackageQualifiedName(),
            javaPackage,
            javaPackage,
            CLASS_PREFIX,
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
    return baseName != null ? ImmutableSet.of(baseName, baseName + "OrBuilder") : ImmutableSet.of();
  }

  /**
   * Specialized format string for proto2 immutable API generated names. Format should consists of
   * two substitution place holders. (1) the base (e.g., "Builder") and (2) the field name.
   */
  private static final class FieldFormat {
    private final String format;

    FieldFormat(String format) {
      this.format = format;
    }

    String getName(String builderOrMessageBase, String fieldName) {
      return String.format(format, builderOrMessageBase, fieldName);
    }
  }

  /** Formats are split into fields for the immutable class and for the builder (mutable view). */
  private static final class SingularFields {
    private static final FieldFormat[] IMMUTABLE_FIELD_FORMATS = {
      new FieldFormat("%shas%s"), new FieldFormat("%sget%s"),
    };

    // Assume the immutable interface applies, plus:
    private static final FieldFormat[] MUTABLE_FIELD_FORMATS = {
      new FieldFormat("%sset%s"), new FieldFormat("%sclear%s"),
    };

    private static final FieldFormat[] IMMUTABLE_MESSAGE_FIELD_FORMATS = {
      new FieldFormat("%sget%sOrBuilder"),
    };

    private static final FieldFormat[] MUTABLE_MESSAGE_FIELD_FORMATS = {
      new FieldFormat("%sget%sBuilder"),
      new FieldFormat("%sget%sFieldBuilder"),
      new FieldFormat("%smerge%s"),
    };
  }

  private static final class RepeatedFields {
    private static final FieldFormat[] IMMUTABLE_FIELD_FORMATS = {
      new FieldFormat("%sget%s"), new FieldFormat("%sget%sCount"), new FieldFormat("%sget%sList"),
    };

    // Assume the immutable interface applies, plus:
    private static final FieldFormat[] MUTABLE_FIELD_FORMATS = {
      new FieldFormat("%sset%s"),
      new FieldFormat("%sadd%s"),
      new FieldFormat("%sadd%sBuilder"),
      new FieldFormat("%saddAll%s"),
      new FieldFormat("%sclear%s"),
    };

    private static final FieldFormat[] IMMUTABLE_MESSAGE_FIELD_FORMATS = {
      new FieldFormat("%sget%sOrBuilder"), new FieldFormat("%sget%sOrBuilderList"),
    };

    private static final FieldFormat[] MUTABLE_MESSAGE_FIELD_FORMATS = {
      new FieldFormat("%sget%sBuilder"),
      new FieldFormat("%sget%sFieldBuilder"),
      new FieldFormat("%sget%sBuilderList"),
      new FieldFormat("%sremove%s"),
    };
  }

  private static final class MapFields {
    private static final FieldFormat[] IMMUTABLE_FIELD_FORMATS = {
      new FieldFormat("%sget%sOrDefault"),
      new FieldFormat("%sget%sOrThrow"),
      new FieldFormat("%scontains%s"),
      new FieldFormat("%sget%sCount"),
      new FieldFormat("%sget%sMap"),
      // NOTE: plain get%s() is deprecated in favor of get%sMap
      new FieldFormat("%sget%s"),
    };

    private static final FieldFormat[] MUTABLE_FIELD_FORMATS = {
      new FieldFormat("%sclear%s"),
      new FieldFormat("%sremove%s"),
      new FieldFormat("%sput%s"),
      new FieldFormat("%sputAll%s"),
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
    String fieldName = JavaNameGenerator.fieldName(field);
    ImmutableSet.Builder<String> fieldNames = ImmutableSet.builder();
    String capitalCamelName = NameUtils.underscoreToCapitalizedCamelCase(fieldName);
    String messageQualifier = "";
    String builderQualifier = "Builder.";

    if (field.isRepeated()) {
      if (field instanceof PbMapField) {
        for (FieldFormat fmt : MapFields.IMMUTABLE_FIELD_FORMATS) {
          fieldNames.add(fmt.getName(messageQualifier, capitalCamelName));
          fieldNames.add(fmt.getName(builderQualifier, capitalCamelName));
        }

        for (FieldFormat fmt : MapFields.MUTABLE_FIELD_FORMATS) {
          fieldNames.add(fmt.getName(builderQualifier, capitalCamelName));
        }
      } else {
        for (FieldFormat fmt : RepeatedFields.IMMUTABLE_FIELD_FORMATS) {
          fieldNames.add(fmt.getName(messageQualifier, capitalCamelName));
          fieldNames.add(fmt.getName(builderQualifier, capitalCamelName));
        }

        for (FieldFormat fmt : RepeatedFields.MUTABLE_FIELD_FORMATS) {
          fieldNames.add(fmt.getName(builderQualifier, capitalCamelName));
        }

        BuiltInType builtInType =
            field.getTypeName() != null ? field.getTypeName().getBuiltInType() : null;
        if (builtInType != null && builtInType.equals(BuiltInType.STRING)) {
          fieldNames.add(messageQualifier + "get" + capitalCamelName + "Bytes");
          fieldNames.add(builderQualifier + "get" + capitalCamelName + "Bytes");

          fieldNames.add(builderQualifier + "set" + capitalCamelName + "Bytes");
        } else if (PbPsiUtil.fieldIsMessage(field)) {
          for (FieldFormat fmt : RepeatedFields.IMMUTABLE_MESSAGE_FIELD_FORMATS) {
            fieldNames.add(fmt.getName(messageQualifier, capitalCamelName));
            fieldNames.add(fmt.getName(builderQualifier, capitalCamelName));
          }
          for (FieldFormat fmt : RepeatedFields.MUTABLE_MESSAGE_FIELD_FORMATS) {
            fieldNames.add(fmt.getName(builderQualifier, capitalCamelName));
          }
        }
      }
    } else {
      for (FieldFormat fmt : SingularFields.IMMUTABLE_FIELD_FORMATS) {
        fieldNames.add(fmt.getName(messageQualifier, capitalCamelName));
        fieldNames.add(fmt.getName(builderQualifier, capitalCamelName));
      }

      for (FieldFormat fmt : SingularFields.MUTABLE_FIELD_FORMATS) {
        fieldNames.add(fmt.getName(builderQualifier, capitalCamelName));
      }

      BuiltInType builtInType =
          field.getTypeName() != null ? field.getTypeName().getBuiltInType() : null;
      if (builtInType != null && builtInType.equals(BuiltInType.STRING)) {
        fieldNames.add(messageQualifier + "get" + capitalCamelName + "Bytes");
        fieldNames.add(builderQualifier + "get" + capitalCamelName + "Bytes");

        fieldNames.add(builderQualifier + "set" + capitalCamelName + "Bytes");
      } else if (PbPsiUtil.fieldIsMessage(field)) {
        for (FieldFormat fmt : SingularFields.IMMUTABLE_MESSAGE_FIELD_FORMATS) {
          fieldNames.add(fmt.getName(messageQualifier, capitalCamelName));
          fieldNames.add(fmt.getName(builderQualifier, capitalCamelName));
        }
        for (FieldFormat fmt : SingularFields.MUTABLE_MESSAGE_FIELD_FORMATS) {
          fieldNames.add(fmt.getName(builderQualifier, capitalCamelName));
        }
      }
    }
    fieldNames.add(messageQualifier + fieldConstantName(fieldName));

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
        String.format("get%sCase", capitalCamelName),
        String.format("Builder.clear%s", capitalCamelName));
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
    return new Proto2NameMatcher(context, this);
  }
}
