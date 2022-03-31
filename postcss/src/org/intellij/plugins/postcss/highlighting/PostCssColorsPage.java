package org.intellij.plugins.postcss.highlighting;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.psi.css.CssBundle;
import com.intellij.psi.css.impl.util.CssHighlighter;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.PostCssIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class PostCssColorsPage implements ColorSettingsPage {
  private static final Map<String, TextAttributesKey> ADDITIONAL_ATTRIBUTES_KEY_MAP = Map.of("tag", PostCssSyntaxHighlighter.TAG_NAME, "id", CssHighlighter.CSS_ID_SELECTOR, "class", CssHighlighter.CSS_CLASS_NAME, "attr", CssHighlighter.CSS_ATTRIBUTE_NAME);
  private static final String DEMO_TEXT = "@import \"manual.css\";\n\n" +
                                          "@font-face {\n" +
                                          "  font-family: DroidSans;\n" +
                                          "  src: url(DroidSans.ttf);\n" +
                                          "  unicode-range: U+000-5FF, U+1e00-1fff, U+2000-2300;\n" +
                                          "}\n\n" +
                                          "@media (500px < width <= 1200px) and (height >= 400px){\n" +
                                          "  color: red;\n" +
                                          "}\n\n" +
                                          "<tag>h1</tag>.<class>mystyle</class>:lang(en) {\n" +
                                          "  color:blue; /* TODO: change THIS to yellow for next version! */\n" +
                                          "  border:rgb(255,0,0);\n" +
                                          "  background-color: #FAFAFA;\n" +
                                          "  background:url(hello.jpg) !important;\n" +
                                          "  @nest span &, #&-id {\n" +
                                          "    color: yellow;\n" +
                                          "  }\n" +
                                          "}\n\n" +
                                          "<tag>div</tag> > <tag>p</tag>, <tag>p</tag> ~ <tag>ul</tag>, <tag>input</tag> [<attr>type</attr>=\"radio\"] {\n" +
                                          "  width: 80%;\n" +
                                          "}\n\n" +
                                          "<id>#header</id>:after {\n" +
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
    new AttributesDescriptor(CssBundle.message("css.class.name"), PostCssSyntaxHighlighter.CLASS_NAME),
    new AttributesDescriptor(CssBundle.message("css.attribute.name"), PostCssSyntaxHighlighter.ATTRIBUTE_NAME),
    new AttributesDescriptor(CssBundle.message("css.important"), PostCssSyntaxHighlighter.IMPORTANT),
    new AttributesDescriptor(CssBundle.message("css.keyword"), PostCssSyntaxHighlighter.KEYWORD),
    new AttributesDescriptor(CssBundle.message("css.number"), PostCssSyntaxHighlighter.NUMBER),
    new AttributesDescriptor(PostCssBundle.message("postcss.operators"), PostCssSyntaxHighlighter.OPERATORS),
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

  @Override
  public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ADDITIONAL_ATTRIBUTES_KEY_MAP;
  }

  @Override
  public @NotNull String getDisplayName() {
    return PostCssBundle.message("color.settings.postcss.name");
  }

  @Override
  public @NotNull Icon getIcon() {
    return PostCssIcons.Postcss;
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
    return new PostCssSyntaxHighlighter();
  }

  @Override
  public @NotNull String getDemoText() {
    return DEMO_TEXT;
  }
}