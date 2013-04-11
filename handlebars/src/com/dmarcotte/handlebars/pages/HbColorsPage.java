package com.dmarcotte.handlebars.pages;

import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.HbHighlighter;
import com.dmarcotte.handlebars.HbIcons;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;
import java.util.Set;

public class HbColorsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] ATTRS;

  static {
    ATTRS = new AttributesDescriptor[HbHighlighter.DISPLAY_NAMES.size()];
    Set<TextAttributesKey> textAttributesKeys = HbHighlighter.DISPLAY_NAMES.keySet();
    TextAttributesKey[] keys = textAttributesKeys.toArray(new TextAttributesKey[textAttributesKeys.size()]);
    for (int i = 0; i < keys.length; i++) {
      TextAttributesKey key = keys[i];
      String name = HbHighlighter.DISPLAY_NAMES.get(key).getFirst();
      ATTRS[i] = new AttributesDescriptor(name, key);
    }
  }

  @NotNull
  public String getDisplayName() {
    return HbBundle.message("hb.files.file.type.description");
  }

  public Icon getIcon() {
    return HbIcons.FILE_ICON;
  }

  @NotNull
  public AttributesDescriptor[] getAttributeDescriptors() {
    return ATTRS;
  }

  @NotNull
  public ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  public SyntaxHighlighter getHighlighter() {
    return new HbHighlighter();
  }

  @NotNull
  public String getDemoText() {
    return "{{identifier my-val=true my-other-val=42 my-string-val=\"a string\"}}\n" +
           "{{! this is a comment }}\n" +
           "{{!--\n" +
           "    this is a Handlebars block comment,\n" +
           "    which can comment out mustache expressions: {{ignored}}\n" +
           "--}}\n" +
           "{{@data}}\n" +
           "\\{{escaped}}\n"
      ;
  }

  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return null;
  }
}