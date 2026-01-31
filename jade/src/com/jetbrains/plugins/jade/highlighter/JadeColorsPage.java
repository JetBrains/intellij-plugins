// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.highlighter;

import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.OptionsBundle;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ResourceUtil;
import com.jetbrains.plugins.jade.JadeBundle;
import com.jetbrains.plugins.jade.psi.JadeFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public final class JadeColorsPage implements ColorSettingsPage {
  public static final String DEMO_TEXT;

  static {
    try {
      String text = ResourceUtil.loadText(Objects.requireNonNull(JadeColorsPage.class.getClassLoader()
                                                                   .getResourceAsStream("misc/SampleText.jade")));
      DEMO_TEXT = StringUtil.convertLineSeparators(text);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static final AttributesDescriptor[] ATTRS = new AttributesDescriptor[]{
    new AttributesDescriptor(JadeBundle.message("color.settings.doctype"), JadeHighlighter.DOCTYPE_KEYWORD),
    new AttributesDescriptor(JadeBundle.message("color.settings.keyword"), JadeHighlighter.KEYWORD),
    new AttributesDescriptor(JadeBundle.message("color.settings.comment"), JadeHighlighter.COMMENT),
    new AttributesDescriptor(JadeBundle.message("color.settings.unbuf.comment"), JadeHighlighter.UNBUF_COMMENT),
    new AttributesDescriptor(JadeBundle.message("color.settings.text"), JadeHighlighter.TEXT),
    new AttributesDescriptor(JadeBundle.message("color.settings.tag"), JadeHighlighter.TAG_NAME),
    new AttributesDescriptor(JadeBundle.message("color.settings.tag.id"), JadeHighlighter.TAG_ID),
    new AttributesDescriptor(JadeBundle.message("color.settings.tag.class"), JadeHighlighter.TAG_CLASS),
    new AttributesDescriptor(JadeBundle.message("color.settings.bad.character"), JadeHighlighter.BAD_CHARACTER),
    new AttributesDescriptor(JadeBundle.message("color.settings.number"), JadeHighlighter.NUMBER),
    new AttributesDescriptor(JadeBundle.message("color.settings.pipe"), JadeHighlighter.PIPE),
    new AttributesDescriptor(JadeBundle.message("color.settings.comma"), JadeHighlighter.COMMA),
    new AttributesDescriptor(JadeBundle.message("color.settings.parenthesis"), JadeHighlighter.PARENTS),
    new AttributesDescriptor(JadeBundle.message("color.settings.attribute.name"), JadeHighlighter.ATTRIBUTE_NAME),
    new AttributesDescriptor(JadeBundle.message("color.settings.colon"), JadeHighlighter.COLON),
    new AttributesDescriptor(JadeBundle.message("color.settings.operation"), JadeHighlighter.OPERATION_SIGN),
    new AttributesDescriptor(JadeBundle.message("color.settings.javascript.block"), JadeHighlighter.JS_BLOCK),
    new AttributesDescriptor(JadeBundle.message("color.settings.statement"), JadeHighlighter.STATEMENTS),
    new AttributesDescriptor(JadeBundle.message("color.settings.file.path"), JadeHighlighter.FILE_PATH),
    new AttributesDescriptor(JadeBundle.message("color.settings.filter.name"), JadeHighlighter.FILTER_NAME),
    new AttributesDescriptor(JadeBundle.message("color.settings.embedded.content"), JadeHighlighter.EMBEDDED_CONTENT),
    new AttributesDescriptor(OptionsBundle.message("options.any.color.descriptor.injected.language.fragment"), JadeHighlighter.INJECTED_LANGUAGE_FRAGMENT),
  };

  @Override
  public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return null;
  }

  @Override
  public @NotNull String getDisplayName() {
    return JadeBundle.message("color.settings.name");
  }

  @Override
  public @NotNull Icon getIcon() {
    return JadeFileType.INSTANCE.getIcon();
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
  public @NotNull SyntaxHighlighter getHighlighter() {
    return new JadeSyntaxHighlighter(CodeStyle.getDefaultSettings());
  }

  @Override
  public @NotNull String getDemoText() {
    return DEMO_TEXT;
  }
}
