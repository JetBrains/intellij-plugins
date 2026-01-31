// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.util.Map;

final class JdlColorSettingsPage implements ColorSettingsPage {
  private final AttributesDescriptor[] myAttributesDescriptors = new AttributesDescriptor[]{
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.keyword"), JdlSyntaxHighlighter.JDL_KEYWORD),
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.identifier"), JdlSyntaxHighlighter.JDL_IDENTIFIER),
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.application.base.name"), JdlSyntaxHighlighter.JDL_BASE_NAME),
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.string"), JdlSyntaxHighlighter.JDL_STRING),
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.number"), JdlSyntaxHighlighter.JDL_NUMBER),
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.boolean"), JdlSyntaxHighlighter.JDL_BOOLEAN),
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.enum.value"), JdlSyntaxHighlighter.JDL_OPTION_ENUM_VALUE),
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.option.name"), JdlSyntaxHighlighter.JDL_OPTION_NAME),
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.field.name"), JdlSyntaxHighlighter.JDL_FIELD_NAME),
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.field.constraint"), JdlSyntaxHighlighter.JDL_FIELD_CONSTRAINT),
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.constant"), JdlSyntaxHighlighter.JDL_CONSTANT),
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.comments.line.comment"), JdlSyntaxHighlighter.JDL_LINE_COMMENT),
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.comments.block.comment"), JdlSyntaxHighlighter.JDL_BLOCK_COMMENT),
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.braces.operators.brackets"), JdlSyntaxHighlighter.JDL_BRACKETS),
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.braces.operators.braces"), JdlSyntaxHighlighter.JDL_BRACES),
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.braces.operators.parentheses"), JdlSyntaxHighlighter.JDL_PARENTHESES),
    new AttributesDescriptor(JdlBundle.message("attribute.descriptor.braces.operators.comma"), JdlSyntaxHighlighter.JDL_COMMA)
  };

  @Override
  public @NotNull Icon getIcon() {
    return JdlIconsMapping.FILE_ICON;
  }

  @Override
  public @NotNull String getDisplayName() {
    return JdlBundle.message("configurable.name.jhipster.jdl");
  }

  @Override
  public @NotNull SyntaxHighlighter getHighlighter() {
    return new JdlSyntaxHighlighter();
  }

  @Override
  public @NonNls @NotNull String getDemoText() {
    return """
      application {
        config {
          <optionName>baseName</optionName> <baseName>app1</baseName>
          <optionName>serverPort</optionName> 8080
          <optionName>languages</optionName> [<enumValue>en</enumValue>, <enumValue>fr</enumValue>]
        }
        <keyword>entities</keyword> A, B, C
        <keyword>dto</keyword> * with <enumValue>mapstruct</enumValue>
      }

      <const>DEFAULT_TIMEOUT</const> = 100

      application {
        config {
          <optionName>baseName</optionName> <baseName>app2</baseName>
          <optionName>enableTranslation</optionName> true
        }
        <keyword>entities</keyword> A, C
        <keyword>paginate</keyword> * with <enumValue>pagination</enumValue> except A\s
      }

      entity <id>A</id> { }
      /** This comment will be taken into account */
      entity <id>B</id> { }
      // This comment will be ignored
      entity <id>C</id> {
        <fieldName>departmentName</fieldName> String <fieldConstraint>required</fieldConstraint>
      }

      deployment {
        <optionName>deploymentType</optionName> <enumValue>docker-compose</enumValue>
        <optionName>dockerRepositoryName</optionName> "YourDockerLoginName"
      }""";
  }

  @Override
  public @NotNull Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return Map.of(
      "optionName", JdlSyntaxHighlighter.JDL_OPTION_NAME,
      "enumValue", JdlSyntaxHighlighter.JDL_OPTION_ENUM_VALUE,
      "keyword", JdlSyntaxHighlighter.JDL_KEYWORD,
      "fieldName", JdlSyntaxHighlighter.JDL_FIELD_NAME,
      "fieldConstraint", JdlSyntaxHighlighter.JDL_FIELD_CONSTRAINT,
      "const", JdlSyntaxHighlighter.JDL_CONSTANT,
      "baseName", JdlSyntaxHighlighter.JDL_BASE_NAME,
      "id", JdlSyntaxHighlighter.JDL_IDENTIFIER
    );
  }

  @Override
  public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return myAttributesDescriptors;
  }

  @Override
  public ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }
}
