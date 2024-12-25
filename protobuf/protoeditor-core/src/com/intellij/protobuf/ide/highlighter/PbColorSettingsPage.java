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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.protobuf.ide.PbIdeBundle;
import com.intellij.protobuf.ide.util.PbIcons;
import com.intellij.protobuf.ide.util.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.Map;

public class PbColorSettingsPage implements ColorSettingsPage {

  private static final AttributesDescriptor[] DESCRIPTORS =
    new AttributesDescriptor[]{
      new AttributesDescriptor(PbIdeBundle.message("attribute.descriptor.identifier"), PbSyntaxHighlighter.IDENTIFIER),
      new AttributesDescriptor(PbIdeBundle.message("attribute.descriptor.number"), PbSyntaxHighlighter.NUMBER),
      new AttributesDescriptor(PbIdeBundle.message("attribute.descriptor.keyboard"), PbSyntaxHighlighter.KEYWORD),
      new AttributesDescriptor(PbIdeBundle.message("attribute.descriptor.string"), PbSyntaxHighlighter.STRING),
      new AttributesDescriptor(PbIdeBundle.message("attribute.descriptor.enum"), PbSyntaxHighlighter.ENUM_VALUE),
      new AttributesDescriptor(PbIdeBundle.message("attribute.descriptor.block.comment"), PbSyntaxHighlighter.BLOCK_COMMENT),
      new AttributesDescriptor(PbIdeBundle.message("attribute.descriptor.line.comment"), PbSyntaxHighlighter.LINE_COMMENT),
      new AttributesDescriptor(PbIdeBundle.message("attribute.descriptor.operator"), PbSyntaxHighlighter.OPERATION_SIGN),
      new AttributesDescriptor(PbIdeBundle.message("attribute.descriptor.braces"), PbSyntaxHighlighter.BRACES),
      new AttributesDescriptor(PbIdeBundle.message("attribute.descriptor.brackets"), PbSyntaxHighlighter.BRACKETS),
      new AttributesDescriptor(PbIdeBundle.message("attribute.descriptor.parentheses"), PbSyntaxHighlighter.PARENTHESES),
      new AttributesDescriptor(PbIdeBundle.message("attribute.descriptor.dot"), PbSyntaxHighlighter.DOT),
      new AttributesDescriptor(PbIdeBundle.message("attribute.descriptor.semicolon"), PbSyntaxHighlighter.SEMICOLON),
      new AttributesDescriptor(PbIdeBundle.message("attribute.descriptor.comma"), PbSyntaxHighlighter.COMMA),
      new AttributesDescriptor(PbIdeBundle.message("attribute.descriptor.valid.escape.sequence"),
                               PbSyntaxHighlighter.VALID_STRING_ESCAPE),
      new AttributesDescriptor(PbIdeBundle.message("attribute.descriptor.invalid.escape.sequence"),
                               PbSyntaxHighlighter.INVALID_STRING_ESCAPE),
    };

  @Override
  public @Nullable Icon getIcon() {
    return PbIcons.FILE;
  }

  @Override
  public @NotNull SyntaxHighlighter getHighlighter() {
    return new PbSyntaxHighlighter(true);
  }

  @Override
  public @NotNull String getDemoText() {
    try {
      return StringUtil.convertLineSeparators(ResourceUtil.readUrlAsString(getClass().getResource("/example.proto")));
    } catch (IOException e) {
      return "Error loading example.";
    }
  }

  @Override
  public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return null;
  }

  @Override
  public @NotNull AttributesDescriptor[] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @Override
  public @NotNull ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Override
  public @NotNull String getDisplayName() {
    return PbIdeBundle.message("plugin.name");
  }
}
