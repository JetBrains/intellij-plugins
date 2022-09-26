// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.pages;

import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.HbHighlighter;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import icons.HandlebarsIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;
import java.util.Set;

public class HbColorsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] ATTRS;

  static {
    ATTRS = new AttributesDescriptor[HbHighlighter.DISPLAY_NAMES.size()];
    Set<TextAttributesKey> textAttributesKeys = HbHighlighter.DISPLAY_NAMES.keySet();
    TextAttributesKey[] keys = textAttributesKeys.toArray(TextAttributesKey.EMPTY_ARRAY);
    for (int i = 0; i < keys.length; i++) {
      TextAttributesKey key = keys[i];
      String name = HbHighlighter.DISPLAY_NAMES.get(key).getFirst();
      ATTRS[i] = new AttributesDescriptor(name, key);
    }
  }

  @Override
  @NotNull
  public String getDisplayName() {
    return HbBundle.message("filetype.hb.description");
  }

  @Override
  public Icon getIcon() {
    return HandlebarsIcons.Handlebars_icon;
  }

  @Override
  public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return ATTRS;
  }

  @Override
  public ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Override
  @NotNull
  public SyntaxHighlighter getHighlighter() {
    return new HbHighlighter();
  }

  @Override
  @NotNull
  public String getDemoText() {
    return """
      {{identifier my-val=true my-other-val=42 my-string-val="a string"}}
      {{! this is a comment }}
      {{!--
          this is a Handlebars block comment,
          which can comment out mustache expressions: {{ignored}}
      --}}
      {{@data}}
      \\{{escaped}}
      """
      ;
  }

  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return null;
  }
}