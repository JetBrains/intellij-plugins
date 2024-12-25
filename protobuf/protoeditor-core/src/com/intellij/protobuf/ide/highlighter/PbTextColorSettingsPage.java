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
import com.intellij.protobuf.ide.PbIdeBundle;
import com.intellij.protobuf.ide.util.PbIcons;
import com.intellij.protobuf.ide.util.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.Map;

/** A {@link ColorSettingsPage} for standalone prototext files. */
public class PbTextColorSettingsPage implements ColorSettingsPage {

  private static final AttributesDescriptor[] DESCRIPTORS =
    new AttributesDescriptor[]{
      new AttributesDescriptor(PbIdeBundle.message("prototext.type.identifier"), PbTextSyntaxHighlighter.IDENTIFIER),
      new AttributesDescriptor(PbIdeBundle.message("prototext.type.keyword"), PbTextSyntaxHighlighter.KEYWORD),
      new AttributesDescriptor(PbIdeBundle.message("prototext.type.number"), PbTextSyntaxHighlighter.NUMBER),
      new AttributesDescriptor(PbIdeBundle.message("prototext.type.string"), PbTextSyntaxHighlighter.STRING),
      new AttributesDescriptor(PbIdeBundle.message("prototext.type.enum.value"), PbTextSyntaxHighlighter.ENUM_VALUE),
      new AttributesDescriptor(PbIdeBundle.message("prototext.type.line.comment"), PbTextSyntaxHighlighter.LINE_COMMENT),
      new AttributesDescriptor(PbIdeBundle.message("prototext.type.operator"), PbTextSyntaxHighlighter.OPERATION_SIGN),
      new AttributesDescriptor(PbIdeBundle.message("prototext.type.braces"), PbTextSyntaxHighlighter.BRACES),
      new AttributesDescriptor(PbIdeBundle.message("prototext.type.brackets"), PbTextSyntaxHighlighter.BRACKETS),
      new AttributesDescriptor(PbIdeBundle.message("prototext.type.dot"), PbTextSyntaxHighlighter.DOT),
      new AttributesDescriptor(PbIdeBundle.message("prototext.type.semicolon"), PbTextSyntaxHighlighter.SEMICOLON),
      new AttributesDescriptor(PbIdeBundle.message("prototext.type.comma"), PbTextSyntaxHighlighter.COMMA),
      new AttributesDescriptor(PbIdeBundle.message("prototext.type.valid.escape.sequence"), PbTextSyntaxHighlighter.VALID_STRING_ESCAPE),
      new AttributesDescriptor(PbIdeBundle.message("prototext.type.invalid.escape.sequence"),
                               PbTextSyntaxHighlighter.INVALID_STRING_ESCAPE),
      new AttributesDescriptor(PbIdeBundle.message("prototext.type.comment.directive"), PbTextSyntaxHighlighter.COMMENT_DIRECTIVE),
    };

  @Override
  public @Nullable Icon getIcon() {
    return PbIcons.TEXT_FILE;
  }

  @Override
  public @NotNull SyntaxHighlighter getHighlighter() {
    return new PbTextSyntaxHighlighter();
  }

  @Override
  public @NotNull String getDemoText() {
    try {
      return ResourceUtil.readUrlAsString(getClass().getResource("/example.pb"))
        .replace("\r\n", "\n");
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
    return PbIdeBundle.message("prototext.name");
  }
}
