// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;

final class JdlColorSettingsPage implements ColorSettingsPage {
  private final AttributesDescriptor[] myAttributesDescriptors = new AttributesDescriptor[]{
    new AttributesDescriptor("Keyword", JdlSyntaxHighlighter.JDL_KEYWORD),
    new AttributesDescriptor("Identifier", JdlSyntaxHighlighter.JDL_IDENTIFIER),
    new AttributesDescriptor("Application base name", JdlSyntaxHighlighter.JDL_BASE_NAME),
    new AttributesDescriptor("String", JdlSyntaxHighlighter.JDL_STRING),
    new AttributesDescriptor("Number", JdlSyntaxHighlighter.JDL_NUMBER),
    new AttributesDescriptor("Boolean", JdlSyntaxHighlighter.JDL_BOOLEAN),
    new AttributesDescriptor("Enum value", JdlSyntaxHighlighter.JDL_OPTION_ENUM_VALUE),
    new AttributesDescriptor("Option name", JdlSyntaxHighlighter.JDL_OPTION_NAME),
    new AttributesDescriptor("Field name", JdlSyntaxHighlighter.JDL_FIELD_NAME),
    new AttributesDescriptor("Field constraint", JdlSyntaxHighlighter.JDL_FIELD_CONSTRAINT),
    new AttributesDescriptor("Constant", JdlSyntaxHighlighter.JDL_CONSTANT),
    new AttributesDescriptor("Comments//Line comment", JdlSyntaxHighlighter.JDL_LINE_COMMENT),
    new AttributesDescriptor("Comments//Block comment", JdlSyntaxHighlighter.JDL_BLOCK_COMMENT),
    new AttributesDescriptor("Braces and Operators//Brackets", JdlSyntaxHighlighter.JDL_BRACKETS),
    new AttributesDescriptor("Braces and Operators//Braces", JdlSyntaxHighlighter.JDL_BRACES),
    new AttributesDescriptor("Braces and Operators//Parentheses", JdlSyntaxHighlighter.JDL_PARENTHESES),
    new AttributesDescriptor("Braces and Operators//Comma", JdlSyntaxHighlighter.JDL_COMMA)
  };

  @Override
  public @NotNull Icon getIcon() {
    return JdlIconsMapping.FILE_ICON;
  }

  @Override
  public @NotNull String getDisplayName() {
    return "JHipster JDL";
  }

  @Override
  public @NotNull SyntaxHighlighter getHighlighter() {
    return new JdlSyntaxHighlighter();
  }

  @Override
  public @NonNls @NotNull String getDemoText() {
    return "application {\n" +
           "  config {\n" +
           "    <optionName>baseName</optionName> <baseName>app1</baseName>\n" +
           "    <optionName>serverPort</optionName> 8080\n" +
           "    <optionName>languages</optionName> [<enumValue>en</enumValue>, <enumValue>fr</enumValue>]\n" +
           "  }\n" +
           "  <keyword>entities</keyword> A, B, C\n" +
           "  <keyword>dto</keyword> * with <enumValue>mapstruct</enumValue>\n" +
           "}\n" +
           "\n" +
           "<const>DEFAULT_TIMEOUT</const> = 100\n" +
           "\n" +
           "application {\n" +
           "  config {\n" +
           "    <optionName>baseName</optionName> <baseName>app2</baseName>\n" +
           "    <optionName>enableTranslation</optionName> true\n" +
           "  }\n" +
           "  <keyword>entities</keyword> A, C\n" +
           "  <keyword>paginate</keyword> * with <enumValue>pagination</enumValue> except A \n" +
           "}\n" +
           "\n" +
           "entity <id>A</id> { }\n" +
           "/** This comment will be taken into account */\n" +
           "entity <id>B</id> { }\n" +
           "// This comment will be ignored\n" +
           "entity <id>C</id> {\n" +
           "  <fieldName>departmentName</fieldName> String <fieldConstraint>required</fieldConstraint>\n" +
           "}\n" +
           "\n" +
           "deployment {\n" +
           "  <optionName>deploymentType</optionName> <enumValue>docker-compose</enumValue>\n" +
           "  <optionName>dockerRepositoryName</optionName> \"YourDockerLoginName\"\n" +
           "}";
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
