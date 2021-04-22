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
package com.intellij.protobuf.ide.highlighter;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.protobuf.ide.util.PbIcons;
import com.intellij.protobuf.ide.util.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.Map;

public class PbColorSettingsPage implements ColorSettingsPage {

  private static final AttributesDescriptor[] DESCRIPTORS =
      new AttributesDescriptor[] {
        new AttributesDescriptor("Identifier", PbSyntaxHighlighter.IDENTIFIER),
        new AttributesDescriptor("Number", PbSyntaxHighlighter.NUMBER),
        new AttributesDescriptor("Keyword", PbSyntaxHighlighter.KEYWORD),
        new AttributesDescriptor("String", PbSyntaxHighlighter.STRING),
        new AttributesDescriptor("Enum Value", PbSyntaxHighlighter.ENUM_VALUE),
        new AttributesDescriptor("Block Comment", PbSyntaxHighlighter.BLOCK_COMMENT),
        new AttributesDescriptor("Line Comment", PbSyntaxHighlighter.LINE_COMMENT),
        new AttributesDescriptor("Operator", PbSyntaxHighlighter.OPERATION_SIGN),
        new AttributesDescriptor("Braces", PbSyntaxHighlighter.BRACES),
        new AttributesDescriptor("Brackets", PbSyntaxHighlighter.BRACKETS),
        new AttributesDescriptor("Parentheses", PbSyntaxHighlighter.PARENTHESES),
        new AttributesDescriptor("Dot", PbSyntaxHighlighter.DOT),
        new AttributesDescriptor("Semicolon", PbSyntaxHighlighter.SEMICOLON),
        new AttributesDescriptor("Comma", PbSyntaxHighlighter.COMMA),
        new AttributesDescriptor("Valid Escape Sequence", PbSyntaxHighlighter.VALID_STRING_ESCAPE),
        new AttributesDescriptor(
            "Invalid Escape Sequence", PbSyntaxHighlighter.INVALID_STRING_ESCAPE),
      };

  @Nullable
  @Override
  public Icon getIcon() {
    return PbIcons.FILE;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return new PbSyntaxHighlighter(true);
  }

  @NotNull
  @Override
  public String getDemoText() {
    try {
      return ResourceUtil.readUrlAsString(getClass().getResource("/example.proto"));
    } catch (IOException e) {
      return "Error loading example.";
    }
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return null;
  }

  @NotNull
  @Override
  public AttributesDescriptor[] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @NotNull
  @Override
  public ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Protocol Buffer";
  }
}
