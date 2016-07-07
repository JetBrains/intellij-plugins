package org.intellij.plugins.postcss.highlighting;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.psi.css.CssBundle;
import icons.PostcssIcons;
import org.intellij.plugins.postcss.PostCssBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.Map;

public class PostCssColorsPage implements ColorSettingsPage {

  private static final String DEMO_TEXT = "@import \"manual.css\";\n\n" +
                                          "@font-face {\n" +
                                          "  font-family: DroidSans;\n" +
                                          "  src: url(DroidSans.ttf);\n" +
                                          "  unicode-range: U+000-5FF, U+1e00-1fff, U+2000-2300;\n" +
                                          "}\n\n" +
                                          "h1.mystyle:lang(en) {\n" +
                                          "  color:blue; /* TODO: change THIS to yellow for next version! */\n" +
                                          "  border:rgb(255,0,0);\n" +
                                          "  background-color: #FAFAFA;\n" +
                                          "  background:url(hello.jpg) !important;\n" +
                                          "  @nest span & {\n" +
                                          "    color: yellow;\n" +
                                          "  }\n" +
                                          "}\n\n" +
                                          "div > p, p ~ ul, input[type=\"radio\"] {\n" +
                                          "  color: green;\n" +
                                          "  width: 80%;\n" +
                                          "}\n\n" +
                                          "#header:after {\n" +
                                          "  color: red;\n" +
                                          "}\n\n" +
                                          "!";

  private static final AttributesDescriptor[] ATTRS = new AttributesDescriptor[]{
    new AttributesDescriptor(CssBundle.message("css.bad.character"), PostCssSyntaxHighlighter.BAD_CHARACTER),
    new AttributesDescriptor(CssBundle.message("css.braces"), PostCssSyntaxHighlighter.BRACES),
    new AttributesDescriptor(CssBundle.message("css.brackets"), PostCssSyntaxHighlighter.BRACKETS),
    new AttributesDescriptor(CssBundle.message("css.colon"), PostCssSyntaxHighlighter.COLON),
    new AttributesDescriptor(CssBundle.message("css.color"), PostCssSyntaxHighlighter.COLOR),
    new AttributesDescriptor(CssBundle.message("css.comma"), PostCssSyntaxHighlighter.COMMA),
    new AttributesDescriptor(CssBundle.message("css.comment"), PostCssSyntaxHighlighter.COMMENT),
    new AttributesDescriptor(CssBundle.message("css.dot"), PostCssSyntaxHighlighter.DOT),
    new AttributesDescriptor(CssBundle.message("css.function"), PostCssSyntaxHighlighter.FUNCTION),
    new AttributesDescriptor(CssBundle.message("css.identifier"), PostCssSyntaxHighlighter.IDENTIFIER),
    new AttributesDescriptor(CssBundle.message("css.id.selector"), PostCssSyntaxHighlighter.ID_SELECTOR),
    new AttributesDescriptor(CssBundle.message("css.important"), PostCssSyntaxHighlighter.IMPORTANT),
    new AttributesDescriptor(CssBundle.message("css.keyword"), PostCssSyntaxHighlighter.KEYWORD),
    new AttributesDescriptor(CssBundle.message("css.number"), PostCssSyntaxHighlighter.NUMBER),
    new AttributesDescriptor(CssBundle.message("css.parenthesis"), PostCssSyntaxHighlighter.PARENTHESES),
    new AttributesDescriptor(CssBundle.message("css.property.name"), PostCssSyntaxHighlighter.PROPERTY_NAME),
    new AttributesDescriptor(CssBundle.message("css.property.value"), PostCssSyntaxHighlighter.PROPERTY_VALUE),
    new AttributesDescriptor(CssBundle.message("css.pseudo.selector"), PostCssSyntaxHighlighter.PSEUDO),
    new AttributesDescriptor(CssBundle.message("css.semicolon"), PostCssSyntaxHighlighter.SEMICOLON),
    new AttributesDescriptor(CssBundle.message("css.string"), PostCssSyntaxHighlighter.STRING),
    new AttributesDescriptor(CssBundle.message("css.tag.name"), PostCssSyntaxHighlighter.TAG_NAME),
    new AttributesDescriptor(CssBundle.message("css.unicode.range"), PostCssSyntaxHighlighter.UNICODE_RANGE),
    new AttributesDescriptor(CssBundle.message("css.url"), PostCssSyntaxHighlighter.URL),
  };

  private static final ColorDescriptor[] COLORS = ColorDescriptor.EMPTY_ARRAY;

  @Nullable
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return Collections.emptyMap();
  }

  @NotNull
  public String getDisplayName() {
    return PostCssBundle.message("color.settings.postcss.name");
  }

  @NotNull
  public Icon getIcon() {
    return PostcssIcons.Postcss;
  }

  @NotNull
  public AttributesDescriptor[] getAttributeDescriptors() {
    return ATTRS;
  }

  @NotNull
  public ColorDescriptor[] getColorDescriptors() {
    return COLORS;
  }

  @NotNull
  public SyntaxHighlighter getHighlighter() {
    return new PostCssSyntaxHighlighter();
  }

  @NotNull
  public String getDemoText() {
    return DEMO_TEXT;
  }
}