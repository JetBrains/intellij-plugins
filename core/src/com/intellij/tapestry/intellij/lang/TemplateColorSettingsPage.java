package com.intellij.tapestry.intellij.lang;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.JspHighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.XmlHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.tapestry.intellij.util.Icons;
import com.intellij.tapestry.lang.TmlHighlighter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Tapestry template color settings.
 */
public class TemplateColorSettingsPage implements ColorSettingsPage {

  public static final TextAttributesKey TAG_NAME = createTextAttributesKey("TAPESTRY_COMPONENT_TAG", XmlHighlighterColors.HTML_TAG_NAME);
  public static final TextAttributesKey ATTR_NAME = createTextAttributesKey("TAPESTRY_COMPONENT_PARAMATER", XmlHighlighterColors.HTML_ATTRIBUTE_NAME);
  public static final TextAttributesKey TEL_BOUNDS = createTextAttributesKey("TEL_BOUNDS", SyntaxHighlighterColors.KEYWORD);
  public static final TextAttributesKey TEL_IDENT = createTextAttributesKey("TEL_IDENT", CodeInsightColors.LOCAL_VARIABLE_ATTRIBUTES);
  public static final TextAttributesKey TEL_DOT = createTextAttributesKey("TEL_DOT", SyntaxHighlighterColors.DOT);
  public static final TextAttributesKey TEL_NUMBER = createTextAttributesKey("TEL_NUMBER", SyntaxHighlighterColors.NUMBER);
  public static final TextAttributesKey TEL_PARENTHS = createTextAttributesKey("TEL_PARENTHS", SyntaxHighlighterColors.PARENTHS);
  public static final TextAttributesKey TEL_BRACKETS = createTextAttributesKey("TEL_BRACKETS", SyntaxHighlighterColors.BRACKETS);
  public static final TextAttributesKey TEL_STRING = createTextAttributesKey("TEL_STRING", SyntaxHighlighterColors.STRING);
  public static final TextAttributesKey TEL_BACKGROUND = createTextAttributesKey("TEL_BACKGROUND", JspHighlighterColors.JSP_SCRIPTING_BACKGROUND);
  public static final TextAttributesKey TEL_BAD_CHAR = createTextAttributesKey("TEL_BAD_CHAR", HighlighterColors.BAD_CHARACTER);

  private static TextAttributesKey createTextAttributesKey(@NonNls String externalName, TextAttributesKey defaultTextAttr) {
    return TextAttributesKey.createTextAttributesKey(externalName, defaultTextAttr.getDefaultAttributes());
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

  @NotNull
  public String getDisplayName() {
    return "Tapestry";
  }

  public Icon getIcon() {
    return Icons.TAPESTRY_LOGO_SMALL;
  }

  @NotNull
  public AttributesDescriptor[] getAttributeDescriptors() {
    return ourAttributeDescriptors;
  }

  @NotNull
  public ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  public SyntaxHighlighter getHighlighter() {
    return new TmlHighlighter();
  }

  @NotNull
  public String getDemoText() {
    return ourColorSettingsText;
  }

  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ourAdditionalHighlightingTagToDescriptorMap;
  }

  private static final Map<String, TextAttributesKey> ourAdditionalHighlightingTagToDescriptorMap;
  private static final String ourColorSettingsText;
  static {
    Logger logger = Logger.getInstance(TemplateColorSettingsPage.class.getName());
    byte[] templateBytes = null;
    try {
      InputStream templateStream = TemplateColorSettingsPage.class.getResourceAsStream("templateColorSettingsText.html");
      templateBytes = new byte[templateStream.available()];
      if (templateStream.read(templateBytes) == 0) {
        logger.warn("Template color settings demo text is empty");
      }
    }
    catch (IOException ex) {
      logger.error(ex);
    }
    ourColorSettingsText = new String(templateBytes != null ? templateBytes : new byte[0]).replace('\r', ' ');
    ourAdditionalHighlightingTagToDescriptorMap = new HashMap<String, TextAttributesKey>();
    ourAdditionalHighlightingTagToDescriptorMap.put("componenTagName", TAG_NAME);
    ourAdditionalHighlightingTagToDescriptorMap.put("componenTagAttribute", ATTR_NAME);
  }

}