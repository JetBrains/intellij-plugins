/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.highlight;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.thoughtworks.gauge.GaugeBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 * The page that appears in the Intellij IDEA Settings page, allowing the user to override the default appearances of
 * syntax elements (headers, comments, etc) within Gauge specification (.spec) files.
 */
final class SpecColorSettingsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
    new AttributesDescriptor(GaugeBundle.message("gauge.attribute.descriptor.specification.heading"), HighlighterTokens.SPEC_HEADING),
    new AttributesDescriptor(GaugeBundle.message("gauge.attribute.descriptor.scenario.heading"), HighlighterTokens.SCENARIO_HEADING),
    new AttributesDescriptor(GaugeBundle.message("gauge.step"), HighlighterTokens.STEP),
    new AttributesDescriptor(GaugeBundle.message("gauge.context.type.java.comment"), HighlighterTokens.COMMENT),
    new AttributesDescriptor(GaugeBundle.message("gauge.attribute.descriptor.arguments"), HighlighterTokens.ARG),
    new AttributesDescriptor(GaugeBundle.message("gauge.attribute.descriptor.dynamic.arguments"), HighlighterTokens.DYNAMIC_ARG),
    new AttributesDescriptor(GaugeBundle.message("gauge.attribute.descriptor.table.header"), HighlighterTokens.TABLE_HEADER),
    new AttributesDescriptor(GaugeBundle.message("gauge.attribute.descriptor.table.border"), HighlighterTokens.TABLE_BORDER),
    new AttributesDescriptor(GaugeBundle.message("gauge.attribute.descriptor.table.item"), HighlighterTokens.TABLE_ROW),
    new AttributesDescriptor(GaugeBundle.message("gauge.attribute.descriptor.tags"), HighlighterTokens.TAGS),
  };

  @Nullable
  @Override
  public Icon getIcon() {
    return null;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return new SpecSyntaxHighlighter();
  }

  @NotNull
  @Override
  public String getDemoText() {
    return "# Specification Heading\n" +
           "tags: first, second" +
           "This comment explains what the spec intends to test\n" +
           "in multi line\n" +
           "|name                                     |type |\n" +
           "|-----------------------------------------|-----|\n" +
           "|manifest.json                            |file |\n" +
           "|specs                                    |dir  |\n" +
           "* This is a context\n" +
           "## Scenario Heading\n" +
           "Tags: tag3, tag4" +
           "* Step 1 with \"arg\"\n" +
           "* Step 2 with <dynamic arg>\n" +
           "comments between steps\n" +
           "* Step 2\n" +
           "|id|filename|\n" +
           "|1 |foo     |\n" +
           "|2 |bar     |\n" +
           "|3 |<name>  |\n";
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return null;
  }

  @Override
  public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @Override
  public ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return GaugeBundle.message("gauge.specification");
  }
}
