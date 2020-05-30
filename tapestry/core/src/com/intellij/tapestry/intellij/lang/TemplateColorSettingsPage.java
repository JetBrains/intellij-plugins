package com.intellij.tapestry.intellij.lang;

import com.intellij.ide.highlighter.JavaHighlightingColors;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.JspHighlighterColors;
import com.intellij.openapi.editor.XmlHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.tapestry.lang.TmlHighlighter;
import icons.TapestryIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Tapestry template color settings.
 */
public class TemplateColorSettingsPage implements ColorSettingsPage {
  public static final TextAttributesKey TAG_NAME =
    createTextAttributesKey("TAPESTRY_COMPONENT_TAG", XmlHighlighterColors.HTML_TAG_NAME);
  public static final TextAttributesKey ATTR_NAME =
    createTextAttributesKey("TAPESTRY_COMPONENT_PARAMATER", XmlHighlighterColors.HTML_ATTRIBUTE_NAME);
  public static final TextAttributesKey TEL_BOUNDS =
    createTextAttributesKey("TEL_BOUNDS", JavaHighlightingColors.KEYWORD);
  public static final TextAttributesKey TEL_IDENT =
    createTextAttributesKey("TEL_IDENT", JavaHighlightingColors.LOCAL_VARIABLE_ATTRIBUTES);
  public static final TextAttributesKey TEL_DOT =
    createTextAttributesKey("TEL_DOT", JavaHighlightingColors.DOT);
  public static final TextAttributesKey TEL_NUMBER =
    createTextAttributesKey("TEL_NUMBER", JavaHighlightingColors.NUMBER);
  public static final TextAttributesKey TEL_PARENTHS =
    createTextAttributesKey("TEL_PARENTHS", JavaHighlightingColors.PARENTHESES);
  public static final TextAttributesKey TEL_BRACKETS =
    createTextAttributesKey("TEL_BRACKETS", JavaHighlightingColors.BRACKETS);
  public static final TextAttributesKey TEL_STRING =
    createTextAttributesKey("TEL_STRING", JavaHighlightingColors.STRING);
  public static final TextAttributesKey TEL_BACKGROUND =
    createTextAttributesKey("TEL_BACKGROUND", JspHighlighterColors.JSP_SCRIPTING_BACKGROUND);
  public static final TextAttributesKey TEL_BAD_CHAR =
    createTextAttributesKey("TEL_BAD_CHAR", HighlighterColors.BAD_CHARACTER);

  private static TextAttributesKey createTextAttributesKey(@NonNls String externalName, TextAttributesKey defaultTextAttr) {
    return TextAttributesKey.createTextAttributesKey(externalName, defaultTextAttr);
  }

  private static AttributesDescriptor createAttributesDescriptor(String displayNameKey, TextAttributesKey textAttributesKey) {
    return new AttributesDescriptor(displayNameKey, textAttributesKey);
  }

  private static final AttributesDescriptor[] ourAttributeDescriptors =
      new AttributesDescriptor[]{
          createAttributesDescriptor("Component tag", TAG_NAME),
          createAttributesDescriptor("Component parameter", ATTR_NAME),
          createAttributesDescriptor("EL bounds", TEL_BOUNDS),
          createAttributesDescriptor("EL identifier", TEL_IDENT),
          createAttributesDescriptor("EL dot", TEL_DOT),
          createAttributesDescriptor("EL number", TEL_NUMBER),
          createAttributesDescriptor("EL parenths", TEL_PARENTHS),
          createAttributesDescriptor("EL brackets", TEL_BRACKETS),
          createAttributesDescriptor("EL string", TEL_STRING),
          createAttributesDescriptor("EL background", TEL_BACKGROUND),
          createAttributesDescriptor("Bad character", TEL_BAD_CHAR),
      };

  @Override
  @NotNull
  public String getDisplayName() {
    return "Tapestry";
  }

  @Override
  public Icon getIcon() {
    return TapestryIcons.Tapestry_logo_small;
  }

  @Override
  public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return ourAttributeDescriptors;
  }

  @Override
  public ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Override
  @NotNull
  public SyntaxHighlighter getHighlighter() {
    return new TmlHighlighter();
  }

  @Override
  @NotNull
  public String getDemoText() {
    return ourColorSettingsText;
  }

  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ourAdditionalHighlightingTagToDescriptorMap;
  }

  private static final Map<String, TextAttributesKey> ourAdditionalHighlightingTagToDescriptorMap;
  private static final String ourColorSettingsText;
  static {
    Logger logger = Logger.getInstance(TemplateColorSettingsPage.class);
    String template = "";
    try {
      template = FileUtil.loadTextAndClose(TemplateColorSettingsPage.class.getResourceAsStream("templateColorSettingsText.html"));
      if (template.isEmpty()) {
        logger.warn("Template color settings demo text is empty");
      }
    }
    catch (IOException ex) {
      logger.error(ex);
    }
    ourColorSettingsText = template.replace('\r', ' ');
    ourAdditionalHighlightingTagToDescriptorMap = new HashMap<>();
    ourAdditionalHighlightingTagToDescriptorMap.put("componenTagName", TAG_NAME);
    ourAdditionalHighlightingTagToDescriptorMap.put("componenTagAttribute", ATTR_NAME);
  }
}
