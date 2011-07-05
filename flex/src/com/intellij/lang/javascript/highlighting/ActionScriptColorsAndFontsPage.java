package com.intellij.lang.javascript.highlighting;

import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rustam Vishnyakov
 */
public class ActionScriptColorsAndFontsPage implements ColorSettingsPage {

  private static final AttributesDescriptor[] ATTRS =
    new AttributesDescriptor[]{
      new AttributesDescriptor(JSBundle.message("javascript.keyword"), ECMAL4Highlighter.ECMAL4_KEYWORD),
      new AttributesDescriptor(JSBundle.message("javascript.metadata"), ECMAL4Highlighter.ECMAL4_METADATA),
      new AttributesDescriptor(JSBundle.message("javascript.string"), ECMAL4Highlighter.ECMAL4_STRING),
      new AttributesDescriptor(JSBundle.message("javascript.valid.string.escape"), ECMAL4Highlighter.ECMAL4_VALID_STRING_ESCAPE),
      new AttributesDescriptor(JSBundle.message("javascript.invalid.string.escape"), ECMAL4Highlighter.ECMAL4_INVALID_STRING_ESCAPE),
      new AttributesDescriptor(JSBundle.message("javascript.number"), ECMAL4Highlighter.ECMAL4_NUMBER),
      new AttributesDescriptor(JSBundle.message("javascript.regexp"), ECMAL4Highlighter.ECMAL4_REGEXP),
      new AttributesDescriptor(JSBundle.message("javascript.linecomment"), ECMAL4Highlighter.ECMAL4_LINE_COMMENT),
      new AttributesDescriptor(JSBundle.message("javascript.blockcomment"), ECMAL4Highlighter.ECMAL4_BLOCK_COMMENT),
      new AttributesDescriptor(JSBundle.message("javascript.doccomment"), ECMAL4Highlighter.ECMAL4_DOC_COMMENT),
      new AttributesDescriptor(JSBundle.message("javascript.operation"), ECMAL4Highlighter.ECMAL4_OPERATION_SIGN),
      new AttributesDescriptor(JSBundle.message("javascript.parens"), ECMAL4Highlighter.ECMAL4_PARENTHS),
      new AttributesDescriptor(JSBundle.message("javascript.brackets"), ECMAL4Highlighter.ECMAL4_BRACKETS),
      new AttributesDescriptor(JSBundle.message("javascript.braces"), ECMAL4Highlighter.ECMAL4_BRACES),
      new AttributesDescriptor(JSBundle.message("javascript.comma"), ECMAL4Highlighter.ECMAL4_COMMA),
      new AttributesDescriptor(JSBundle.message("javascript.dot"), ECMAL4Highlighter.ECMAL4_DOT),
      new AttributesDescriptor(JSBundle.message("javascript.semicolon"), ECMAL4Highlighter.ECMAL4_SEMICOLON),
      new AttributesDescriptor(JSBundle.message("javascript.badcharacter"), ECMAL4Highlighter.ECMAL4_BAD_CHARACTER),
      new AttributesDescriptor(JSBundle.message("javascript.docmarkup"), ECMAL4Highlighter.ECMAL4_DOC_MARKUP),
      new AttributesDescriptor(JSBundle.message("javascript.doctag"), ECMAL4Highlighter.ECMAL4_DOC_TAG),
      new AttributesDescriptor(JSBundle.message("javascript.parameter"), ECMAL4Highlighter.ECMAL4_PARAMETER),
      new AttributesDescriptor(JSBundle.message("javascript.local.variable"), ECMAL4Highlighter.ECMAL4_LOCAL_VARIABLE),
      new AttributesDescriptor(JSBundle.message("javascript.global.variable"), ECMAL4Highlighter.ECMAL4_GLOBAL_VARIABLE),
      new AttributesDescriptor(JSBundle.message("javascript.global.function"), ECMAL4Highlighter.ECMAL4_GLOBAL_FUNCTION),
      new AttributesDescriptor(JSBundle.message("javascript.instance.member.function"), ECMAL4Highlighter.ECMAL4_INSTANCE_MEMBER_FUNCTION),
      new AttributesDescriptor(JSBundle.message("javascript.instance.member.variable"), ECMAL4Highlighter.ECMAL4_INSTANCE_MEMBER_VARIABLE),
      new AttributesDescriptor(JSBundle.message("javascript.static.member.function"), ECMAL4Highlighter.ECMAL4_STATIC_MEMBER_FUNCTION),
      new AttributesDescriptor(JSBundle.message("javascript.static.member.variable"), ECMAL4Highlighter.ECMAL4_STATIC_MEMBER_VARIABLE)
    };

  private static @NonNls final Map<String, TextAttributesKey> ADDITIONAL_HIGHLIGHT_DESCRIPTORS = new HashMap<String, TextAttributesKey>();
  static {
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("meta", ECMAL4Highlighter.ECMAL4_METADATA);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("keyword", ECMAL4Highlighter.ECMAL4_KEYWORD);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("string", ECMAL4Highlighter.ECMAL4_STRING);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("valid_escape", ECMAL4Highlighter.ECMAL4_VALID_STRING_ESCAPE);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("invalid_escape", ECMAL4Highlighter.ECMAL4_INVALID_STRING_ESCAPE);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("number", ECMAL4Highlighter.ECMAL4_NUMBER);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("regexp", ECMAL4Highlighter.ECMAL4_REGEXP);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("line_comment", ECMAL4Highlighter.ECMAL4_LINE_COMMENT);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("block_comment", ECMAL4Highlighter.ECMAL4_BLOCK_COMMENT);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("doc_comment", ECMAL4Highlighter.ECMAL4_DOC_COMMENT);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("operation", ECMAL4Highlighter.ECMAL4_OPERATION_SIGN);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("parentheses", ECMAL4Highlighter.ECMAL4_PARENTHS);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("brackets", ECMAL4Highlighter.ECMAL4_BRACKETS);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("braces", ECMAL4Highlighter.ECMAL4_BRACES);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("comma", ECMAL4Highlighter.ECMAL4_COMMA);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("dot", ECMAL4Highlighter.ECMAL4_DOT);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("semicolon", ECMAL4Highlighter.ECMAL4_SEMICOLON);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("bad_char", ECMAL4Highlighter.ECMAL4_BAD_CHARACTER);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("doc_markup", ECMAL4Highlighter.ECMAL4_DOC_MARKUP);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("doc_tag", ECMAL4Highlighter.ECMAL4_DOC_TAG);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("parameter", ECMAL4Highlighter.ECMAL4_PARAMETER);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("local_var", ECMAL4Highlighter.ECMAL4_LOCAL_VARIABLE);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("global_var", ECMAL4Highlighter.ECMAL4_GLOBAL_VARIABLE);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("global_func", ECMAL4Highlighter.ECMAL4_GLOBAL_FUNCTION);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("instance_var", ECMAL4Highlighter.ECMAL4_INSTANCE_MEMBER_VARIABLE);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("instance_func", ECMAL4Highlighter.ECMAL4_INSTANCE_MEMBER_FUNCTION);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("static_func", ECMAL4Highlighter.ECMAL4_STATIC_MEMBER_FUNCTION);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("static_var", ECMAL4Highlighter.ECMAL4_STATIC_MEMBER_VARIABLE);
  }

  private final static String DEMO_TEXT =
    "var globalVar : int = 123;\n" +
    "\n" +
    "function foo() : int {return 0;}\n" +
    "\n" +
    "package {\n" +
    "\n" +
    "public class HighlightingSample {\n" +
    "    public var field : int;\n" +
    "    public static var shared : String;\n" +
    "\n" +
    "    [Meta(name=\"abc\", type=\"def\")]\n" +
    "    public function HighlightingSample() {\n" +
    "        var strings :  Array = [\"One\", \"Two\"];\n" +
    "        // Line comment\n" +
    "        strings = \"\\u1111\\z\\n\\u22\";\n" +
    "        /* Block comment */\n" +
    "        var n : int = getField();\n" +
    "        #\n" +
    "    }\n" +
    "\n" +
    "    /**\n" +
    "     * @param url parameter <i>comment</i>\n" +
    "     */\n" +
    "    public static function adjustUrl(url : String) : String {\n" +
    "        return url.replace(/^\\s*(.*)/, \"$1\");\n" +
    "    }\n" +
    "\n" +
    "    public function getField() : int {\n" +
    "        adjustUrl(\"\");\n" +
    "        return field;\n" +
    "    }\n" +
    "}\n" +
    "}";

  @NotNull
  @Override
  public String getDisplayName() {
    return "ActionScript";
  }

  @Override
  public Icon getIcon() {
    return JavaScriptSupportLoader.JAVASCRIPT.getIcon();
  }

  @NotNull
  @Override
  public AttributesDescriptor[] getAttributeDescriptors() {
    return ATTRS;
  }

  @NotNull
  @Override
  public ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return SyntaxHighlighter.PROVIDER.create(JavaScriptSupportLoader.JAVASCRIPT, null, null);
  }

  @NotNull
  @Override
  public String getDemoText() {
    return DEMO_TEXT.
      replace("[Meta(name=\"abc\", type=\"def\")]", "_META_").
      replaceAll("(public|class|function|package|return|var|static)", "<keyword>$1</keyword>").
      replace("(url :", "(<parameter>url</parameter> :").
      replace("url.", "<parameter>url</parameter>.").
      replaceAll("(\".*\")", "<string>$1</string>").
      replaceAll("(\\\\u1111|\\\\n)", "<valid_escape>$1</valid_escape>").
      replaceAll("(\\\\z|\\\\u22)", "<invalid_escape>$1</invalid_escape>").
      replaceAll("(\\s)([0-9]+)", "$1<number>$2</number>").
      replaceAll("\\((/.*/),", "(<regexp>$1</regexp>,").
      replaceAll("(//.*)\\n", "<line_comment>$1</line_comment>\n").
      replaceAll("(/\\* .*\\*/)", "<block_comment>$1</block_comment>").
      replaceAll("(/\\*\\*(\\s*\\*.*\\n)*)", "<doc_comment>$1</doc_comment>").
      replace(" = ", " <operation>=</operation> ").
      replace(" : ", " <operation>:</operation> ").
      replaceAll("\\)([\\s;])", "<parentheses>)</parentheses>$1").
      replaceAll("([.\\s]\\w+)\\(", "$1<parentheses>(</parentheses>").
      replace("[", "<brackets>[</brackets>").
      replace("]", "<brackets>]</brackets>").
      replace("{", "<braces>{</braces>").
      replace("}", "<braces>}</braces>").
      replace(",", "<comma>,</comma>").
      replaceAll("(\\w)\\.(\\w)", "$1<dot>.</dot>$2").
      replace(";", "<semicolon>;</semicolon>").
      replace("#", "<bad_char>#</bad_char>").
      replace("@param", "<doc_tag>@param</doc_tag>").
      replace("<i>", "<doc_markup><i></doc_markup>").
      replace("</i>", "<doc_markup></i></doc_markup>").
      replace("field", "<instance_var>field</instance_var>").
      replace("strings", "<local_var>strings</local_var>").
      replace("globalVar", "<global_var>globalVar</global_var>").
      replace("foo", "<global_func>foo</global_func>").
      replace("getField", "<instance_func>getField</instance_func>").
      replace("replace", "<instance_func>replace</instance_func>").
      replace("adjustUrl", "<static_func>adjustUrl</static_func>").
      replace("shared", "<static_var>shared</static_var>").
      replace("_META_", "<meta>[Meta(name=\"abc\", type=\"def\")]</meta>");
  }

  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ADDITIONAL_HIGHLIGHT_DESCRIPTORS;
  }
}
