// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.tsr;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;

import static com.intellij.tsr.TslSyntaxHighlighter.TSL_CLASSNAME;

final class TslColorSettingsPage implements ColorSettingsPage {
  private final AttributesDescriptor[] attributesDescriptors = new AttributesDescriptor[]{
      new AttributesDescriptor("Keyword", TslSyntaxHighlighter.TSL_KEYWORD),
      new AttributesDescriptor("Class name", TSL_CLASSNAME),
      new AttributesDescriptor("String", TslSyntaxHighlighter.TSL_STRING),
      new AttributesDescriptor("Number", TslSyntaxHighlighter.TSL_NUMBER),
      new AttributesDescriptor("Boolean", TslSyntaxHighlighter.TSL_BOOLEAN),
      new AttributesDescriptor("Field name", TslSyntaxHighlighter.TSL_FIELD_NAME),
      new AttributesDescriptor("Constant", TslSyntaxHighlighter.TSL_CONSTANT),
      new AttributesDescriptor("Hashcode", TslSyntaxHighlighter.TSL_HASHCODE),
      new AttributesDescriptor("Braces and Operators//Brackets", TslSyntaxHighlighter.TSL_BRACKETS),
      new AttributesDescriptor("Braces and Operators//Braces", TslSyntaxHighlighter.TSL_BRACES),
      new AttributesDescriptor("Braces and Operators//Parentheses", TslSyntaxHighlighter.TSL_PARENTHESES),
      new AttributesDescriptor("Braces and Operators//Comma", TslSyntaxHighlighter.TSL_COMMA)
  };

  @Override
  public @NotNull Icon getIcon() {
    return TslIcons.FILE_ICON;
  }

  @Override
  public @NotNull SyntaxHighlighter getHighlighter() {
    return new TslSyntaxHighlighter();
  }

  @Override
  public @NonNls @NotNull String getDemoText() {
    return """
      [
        <className>Order</className>(
          <fieldName>productType</fieldName>=<const>USED</const>,
          <fieldName>name</fieldName>='Some',
          <fieldName>price</fieldName>=10.0,
          <fieldName>required</fieldName>=true,
          <fieldName>ref</fieldName>=null,
          <fieldName>product</fieldName>=<className>Product</className>{
            <fieldName>productType</fieldName>=<const>USED</const>,
            <fieldName>name</fieldName>='Some',
            <fieldName>user</fieldName>=com.example.User@12fea9,
            <fieldName>price</fieldName>=10.0,
            <fieldName>count</fieldName>=100
          }
        )
      ]""";
  }

  @Override
  public @NotNull Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return Map.of(
        "className", TSL_CLASSNAME,
        "fieldName", TslSyntaxHighlighter.TSL_FIELD_NAME,
        "const", TslSyntaxHighlighter.TSL_CONSTANT
    );
  }

  @Override
  public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return attributesDescriptors;
  }

  @Override
  public ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Override
  public @NotNull String getDisplayName() {
    return "ToString";
  }
}
