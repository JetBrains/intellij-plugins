// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.highlight;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

import static com.jetbrains.lang.dart.highlight.DartSyntaxHighlighterColors.*;

public class DartColorsAndFontsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] ATTRS;

  private static final Map<String, TextAttributesKey> PREVIEW_TAGS = new HashMap<>();
  private static final String PREVIEW_TEXT =
    """
      <DART_KEYWORD>library</DART_KEYWORD> <DART_LIBRARY_NAME>libraryName</DART_LIBRARY_NAME>;
      <DART_KEYWORD>import</DART_KEYWORD> "dart:html" <DART_KEYWORD>as</DART_KEYWORD> <DART_IMPORT_PREFIX>html</DART_IMPORT_PREFIX>;
      // Comment. <ERROR>Error.</ERROR> <WARNING>Warning.</WARNING> <HINT>Hint.</HINT>
      <DART_TYPE_NAME_DYNAMIC>dynamic</DART_TYPE_NAME_DYNAMIC> <DART_TOP_LEVEL_VARIABLE_DECLARATION>topLevelVariable</DART_TOP_LEVEL_VARIABLE_DECLARATION> = "Escape sequences: <DART_VALID_STRING_ESCAPE>\\n</DART_VALID_STRING_ESCAPE> <DART_VALID_STRING_ESCAPE>\\xFF</DART_VALID_STRING_ESCAPE> <DART_VALID_STRING_ESCAPE>\\u1234</DART_VALID_STRING_ESCAPE> <DART_VALID_STRING_ESCAPE>\\u{2F}</DART_VALID_STRING_ESCAPE>";
      <DART_KEYWORD>get</DART_KEYWORD> <DART_TOP_LEVEL_GETTER_DECLARATION>topLevelGetter</DART_TOP_LEVEL_GETTER_DECLARATION> { <DART_KEYWORD>return</DART_KEYWORD> <DART_TOP_LEVEL_GETTER_REFERENCE>topLevelVariable</DART_TOP_LEVEL_GETTER_REFERENCE>; }
      <DART_KEYWORD>set</DART_KEYWORD> <DART_TOP_LEVEL_SETTER_DECLARATION>topLevelSetter</DART_TOP_LEVEL_SETTER_DECLARATION>(<DART_CLASS>bool</DART_CLASS> <DART_PARAMETER_DECLARATION>parameter</DART_PARAMETER_DECLARATION>) { <DART_TOP_LEVEL_FUNCTION_REFERENCE>print</DART_TOP_LEVEL_FUNCTION_REFERENCE>(<DART_PARAMETER_REFERENCE>parameter</DART_PARAMETER_REFERENCE>); }
      <DART_KEYWORD>void</DART_KEYWORD> <DART_TOP_LEVEL_FUNCTION_DECLARATION>topLevelFunction</DART_TOP_LEVEL_FUNCTION_DECLARATION>(<DART_DYNAMIC_PARAMETER_DECLARATION>dynamicParameter</DART_DYNAMIC_PARAMETER_DECLARATION>) {
        <DART_LOCAL_FUNCTION_DECLARATION>localFunction</DART_LOCAL_FUNCTION_DECLARATION>() {}
        <DART_CLASS>num</DART_CLASS> <DART_LOCAL_VARIABLE_DECLARATION>localVar</DART_LOCAL_VARIABLE_DECLARATION> = "Invalid escape sequences: <DART_INVALID_STRING_ESCAPE>\\xZZ</DART_INVALID_STRING_ESCAPE> <DART_INVALID_STRING_ESCAPE>\\uXYZZ</DART_INVALID_STRING_ESCAPE> <DART_INVALID_STRING_ESCAPE>\\u{XYZ}</DART_INVALID_STRING_ESCAPE>";
        <DART_KEYWORD>var</DART_KEYWORD> <DART_DYNAMIC_LOCAL_VARIABLE_DECLARATION>dynamicLocalVar</DART_DYNAMIC_LOCAL_VARIABLE_DECLARATION> = <DART_DYNAMIC_PARAMETER_REFERENCE>dynamicParameter</DART_DYNAMIC_PARAMETER_REFERENCE> + <DART_LOCAL_VARIABLE_REFERENCE>localVar</DART_LOCAL_VARIABLE_REFERENCE> + <DART_LOCAL_FUNCTION_REFERENCE>localFunction</DART_LOCAL_FUNCTION_REFERENCE>();
        <DART_TOP_LEVEL_SETTER_REFERENCE>topLevelSetter</DART_TOP_LEVEL_SETTER_REFERENCE> = <DART_DYNAMIC_LOCAL_VARIABLE_REFERENCE>dynamicLocalVar</DART_DYNAMIC_LOCAL_VARIABLE_REFERENCE> + <DART_TOP_LEVEL_GETTER_REFERENCE>topLevelGetter</DART_TOP_LEVEL_GETTER_REFERENCE> + <DART_TOP_LEVEL_FUNCTION_REFERENCE>topLevelFunction</DART_TOP_LEVEL_FUNCTION_REFERENCE>(<DART_KEYWORD>null</DART_KEYWORD>);
        <DART_LABEL>label</DART_LABEL><DART_COLON>:</DART_COLON> <DART_KEYWORD>while</DART_KEYWORD> (<DART_KEYWORD>true</DART_KEYWORD>) { <DART_KEYWORD>if</DART_KEYWORD> (<DART_IDENTIFIER>identifier</DART_IDENTIFIER>) <DART_KEYWORD>break</DART_KEYWORD> <DART_LABEL>label</DART_LABEL>; }
      }
      /* block comment */
      <DART_KEYWORD>class</DART_KEYWORD> <DART_CLASS>Foo</DART_CLASS><<DART_TYPE_PARAMETER>K</DART_TYPE_PARAMETER>, <DART_TYPE_PARAMETER>V</DART_TYPE_PARAMETER>> {
        <DART_KEYWORD>static</DART_KEYWORD> <DART_KEYWORD>var</DART_KEYWORD> <DART_STATIC_FIELD_DECLARATION>staticField</DART_STATIC_FIELD_DECLARATION> = <DART_STATIC_GETTER_REFERENCE>staticGetter</DART_STATIC_GETTER_REFERENCE>;
        <DART_CLASS>List</DART_CLASS> <DART_INSTANCE_FIELD_DECLARATION>instanceField</DART_INSTANCE_FIELD_DECLARATION> = [566];
        <DART_ANNOTATION>@<DART_TOP_LEVEL_GETTER_REFERENCE>deprecated</DART_TOP_LEVEL_GETTER_REFERENCE></DART_ANNOTATION> <DART_CLASS>Foo</DART_CLASS>.<DART_CONSTRUCTOR>constructor</DART_CONSTRUCTOR>(<DART_KEYWORD>this</DART_KEYWORD>.<DART_INSTANCE_FIELD_REFERENCE>instanceField</DART_INSTANCE_FIELD_REFERENCE>) { <DART_INSTANCE_METHOD_REFERENCE>instanceMethod</DART_INSTANCE_METHOD_REFERENCE>(); }
        <DART_INSTANCE_METHOD_DECLARATION>instanceMethod</DART_INSTANCE_METHOD_DECLARATION>() { <DART_TOP_LEVEL_FUNCTION_REFERENCE>print</DART_TOP_LEVEL_FUNCTION_REFERENCE>(<DART_INSTANCE_GETTER_REFERENCE>instanceField</DART_INSTANCE_GETTER_REFERENCE> + <DART_INSTANCE_GETTER_REFERENCE>instanceGetter</DART_INSTANCE_GETTER_REFERENCE>); }
        <DART_KEYWORD>get</DART_KEYWORD> <DART_INSTANCE_GETTER_DECLARATION>instanceGetter</DART_INSTANCE_GETTER_DECLARATION> { <DART_INSTANCE_SETTER_REFERENCE>instanceSetter</DART_INSTANCE_SETTER_REFERENCE> = <DART_KEYWORD>true</DART_KEYWORD>; }
        <DART_KEYWORD>set</DART_KEYWORD> <DART_INSTANCE_SETTER_DECLARATION>instanceSetter</DART_INSTANCE_SETTER_DECLARATION>(<DART_DYNAMIC_PARAMETER_DECLARATION>_</DART_DYNAMIC_PARAMETER_DECLARATION>) { <DART_STATIC_SETTER_REFERENCE>staticSetter</DART_STATIC_SETTER_REFERENCE> = <DART_KEYWORD>null</DART_KEYWORD>; }
        <DART_KEYWORD>static</DART_KEYWORD> <DART_STATIC_METHOD_DECLARATION>staticMethod</DART_STATIC_METHOD_DECLARATION>() <DART_FAT_ARROW>=></DART_FAT_ARROW> <DART_STATIC_GETTER_REFERENCE>staticField</DART_STATIC_GETTER_REFERENCE>.<DART_UNRESOLVED_INSTANCE_MEMBER_REFERENCE>unresolved</DART_UNRESOLVED_INSTANCE_MEMBER_REFERENCE>();
        <DART_KEYWORD>static</DART_KEYWORD> <DART_KEYWORD>get</DART_KEYWORD> <DART_STATIC_GETTER_DECLARATION>staticGetter</DART_STATIC_GETTER_DECLARATION> { <DART_KEYWORD>return</DART_KEYWORD> <DART_STATIC_METHOD_REFERENCE>staticMethod</DART_STATIC_METHOD_REFERENCE>(); }
        <DART_KEYWORD>static</DART_KEYWORD> <DART_KEYWORD>set</DART_KEYWORD> <DART_STATIC_SETTER_DECLARATION>staticSetter</DART_STATIC_SETTER_DECLARATION>(<DART_CLASS>Foo</DART_CLASS> <DART_PARAMETER_DECLARATION>param</DART_PARAMETER_DECLARATION>) { <DART_SYMBOL_LITERAL>#Enum.EnumConstant</DART_SYMBOL_LITERAL>; }
      }
      /// documentation for [<DART_ENUM>Enum</DART_ENUM>]
      <DART_KEYWORD>enum</DART_KEYWORD> <DART_ENUM>Enum</DART_ENUM> { <DART_ENUM_CONSTANT>EnumConstant</DART_ENUM_CONSTANT> }
      <DART_KEYWORD>mixin</DART_KEYWORD> <DART_MIXIN>Mixin</DART_MIXIN> {}
      <DART_KEYWORD>typedef</DART_KEYWORD> <DART_CLASS>int</DART_CLASS> <DART_FUNCTION_TYPE_ALIAS>FunctionTypeAlias</DART_FUNCTION_TYPE_ALIAS>(<DART_DYNAMIC_PARAMETER_DECLARATION>x</DART_DYNAMIC_PARAMETER_DECLARATION>, <DART_DYNAMIC_PARAMETER_DECLARATION>y</DART_DYNAMIC_PARAMETER_DECLARATION>);
      <DART_KEYWORD>extension</DART_KEYWORD> <DART_EXTENSION>Ext</DART_EXTENSION> <DART_KEYWORD>on</DART_KEYWORD> <DART_CLASS>int</DART_CLASS> {}
      ±±§§``""";

  static {
    PREVIEW_TAGS.put("ERROR", ERROR);
    PREVIEW_TAGS.put("WARNING", WARNING);
    PREVIEW_TAGS.put("HINT", HINT);

    PREVIEW_TAGS.put("DART_ANNOTATION", ANNOTATION);
    PREVIEW_TAGS.put("DART_CLASS", CLASS);
    PREVIEW_TAGS.put("DART_CONSTRUCTOR", CONSTRUCTOR);
    PREVIEW_TAGS.put("DART_CONSTRUCTOR_TEAR_OFF", CONSTRUCTOR_TEAR_OFF);

    PREVIEW_TAGS.put("DART_DYNAMIC_LOCAL_VARIABLE_DECLARATION", DYNAMIC_LOCAL_VARIABLE_DECLARATION);
    PREVIEW_TAGS.put("DART_DYNAMIC_LOCAL_VARIABLE_REFERENCE", DYNAMIC_LOCAL_VARIABLE_REFERENCE);
    PREVIEW_TAGS.put("DART_DYNAMIC_PARAMETER_DECLARATION", DYNAMIC_PARAMETER_DECLARATION);
    PREVIEW_TAGS.put("DART_DYNAMIC_PARAMETER_REFERENCE", DYNAMIC_PARAMETER_REFERENCE);

    PREVIEW_TAGS.put("DART_ENUM", ENUM);
    PREVIEW_TAGS.put("DART_ENUM_CONSTANT", ENUM_CONSTANT);
    PREVIEW_TAGS.put("DART_EXTENSION", EXTENSION);
    PREVIEW_TAGS.put("DART_FUNCTION_TYPE_ALIAS", FUNCTION_TYPE_ALIAS);
    PREVIEW_TAGS.put("DART_TYPE_ALIAS", TYPE_ALIAS);

    PREVIEW_TAGS.put("DART_IDENTIFIER", IDENTIFIER);
    PREVIEW_TAGS.put("DART_INSTANCE_FIELD_DECLARATION", INSTANCE_FIELD_DECLARATION);
    PREVIEW_TAGS.put("DART_INSTANCE_FIELD_REFERENCE", INSTANCE_FIELD_REFERENCE);
    PREVIEW_TAGS.put("DART_INSTANCE_GETTER_DECLARATION", INSTANCE_GETTER_DECLARATION);
    PREVIEW_TAGS.put("DART_INSTANCE_GETTER_REFERENCE", INSTANCE_GETTER_REFERENCE);
    PREVIEW_TAGS.put("DART_INSTANCE_METHOD_DECLARATION", INSTANCE_METHOD_DECLARATION);
    PREVIEW_TAGS.put("DART_INSTANCE_METHOD_REFERENCE", INSTANCE_METHOD_REFERENCE);
    PREVIEW_TAGS.put("DART_INSTANCE_METHOD_TEAR_OFF", INSTANCE_METHOD_TEAR_OFF);
    PREVIEW_TAGS.put("DART_INSTANCE_SETTER_DECLARATION", INSTANCE_SETTER_DECLARATION);
    PREVIEW_TAGS.put("DART_INSTANCE_SETTER_REFERENCE", INSTANCE_SETTER_REFERENCE);

    PREVIEW_TAGS.put("DART_IMPORT_PREFIX", IMPORT_PREFIX);
    PREVIEW_TAGS.put("DART_KEYWORD", KEYWORD);
    PREVIEW_TAGS.put("DART_LABEL", LABEL);
    PREVIEW_TAGS.put("DART_LIBRARY_NAME", LIBRARY_NAME);

    PREVIEW_TAGS.put("DART_LOCAL_FUNCTION_DECLARATION", LOCAL_FUNCTION_DECLARATION);
    PREVIEW_TAGS.put("DART_LOCAL_FUNCTION_REFERENCE", LOCAL_FUNCTION_REFERENCE);
    PREVIEW_TAGS.put("DART_LOCAL_FUNCTION_TEAR_OFF", LOCAL_FUNCTION_TEAR_OFF);
    PREVIEW_TAGS.put("DART_LOCAL_VARIABLE_DECLARATION", LOCAL_VARIABLE_DECLARATION);
    PREVIEW_TAGS.put("DART_LOCAL_VARIABLE_REFERENCE", LOCAL_VARIABLE_REFERENCE);

    PREVIEW_TAGS.put("DART_MIXIN", MIXIN);

    PREVIEW_TAGS.put("DART_PARAMETER_DECLARATION", PARAMETER_DECLARATION);
    PREVIEW_TAGS.put("DART_PARAMETER_REFERENCE", PARAMETER_REFERENCE);

    PREVIEW_TAGS.put("DART_STATIC_FIELD_DECLARATION", STATIC_FIELD_DECLARATION);
    PREVIEW_TAGS.put("DART_STATIC_GETTER_DECLARATION", STATIC_GETTER_DECLARATION);
    PREVIEW_TAGS.put("DART_STATIC_GETTER_REFERENCE", STATIC_GETTER_REFERENCE);
    PREVIEW_TAGS.put("DART_STATIC_METHOD_DECLARATION", STATIC_METHOD_DECLARATION);
    PREVIEW_TAGS.put("DART_STATIC_METHOD_REFERENCE", STATIC_METHOD_REFERENCE);
    PREVIEW_TAGS.put("DART_STATIC_METHOD_TEAR_OFF", STATIC_METHOD_TEAR_OFF);
    PREVIEW_TAGS.put("DART_STATIC_SETTER_DECLARATION", STATIC_SETTER_DECLARATION);
    PREVIEW_TAGS.put("DART_STATIC_SETTER_REFERENCE", STATIC_SETTER_REFERENCE);

    PREVIEW_TAGS.put("DART_TOP_LEVEL_FUNCTION_DECLARATION", TOP_LEVEL_FUNCTION_DECLARATION);
    PREVIEW_TAGS.put("DART_TOP_LEVEL_FUNCTION_REFERENCE", TOP_LEVEL_FUNCTION_REFERENCE);
    PREVIEW_TAGS.put("DART_TOP_LEVEL_FUNCTION_TEAR_OFF", TOP_LEVEL_FUNCTION_TEAR_OFF);
    PREVIEW_TAGS.put("DART_TOP_LEVEL_GETTER_DECLARATION", TOP_LEVEL_GETTER_DECLARATION);
    PREVIEW_TAGS.put("DART_TOP_LEVEL_GETTER_REFERENCE", TOP_LEVEL_GETTER_REFERENCE);
    PREVIEW_TAGS.put("DART_TOP_LEVEL_SETTER_DECLARATION", TOP_LEVEL_SETTER_DECLARATION);
    PREVIEW_TAGS.put("DART_TOP_LEVEL_SETTER_REFERENCE", TOP_LEVEL_SETTER_REFERENCE);
    PREVIEW_TAGS.put("DART_TOP_LEVEL_VARIABLE_DECLARATION", TOP_LEVEL_VARIABLE_DECLARATION);

    PREVIEW_TAGS.put("DART_TYPE_NAME_DYNAMIC", TYPE_NAME_DYNAMIC);
    PREVIEW_TAGS.put("DART_TYPE_PARAMETER", TYPE_PARAMETER);
    PREVIEW_TAGS.put("DART_UNRESOLVED_INSTANCE_MEMBER_REFERENCE", UNRESOLVED_INSTANCE_MEMBER_REFERENCE);

    PREVIEW_TAGS.put("DART_BLOCK_COMMENT", BLOCK_COMMENT);
    PREVIEW_TAGS.put("DART_DOC_COMMENT", DOC_COMMENT);
    PREVIEW_TAGS.put("DART_LINE_COMMENT", LINE_COMMENT);

    PREVIEW_TAGS.put("DART_NUMBER", NUMBER);
    PREVIEW_TAGS.put("DART_STRING", STRING);
    PREVIEW_TAGS.put("DART_VALID_STRING_ESCAPE", VALID_STRING_ESCAPE);
    PREVIEW_TAGS.put("DART_INVALID_STRING_ESCAPE", INVALID_STRING_ESCAPE);
    PREVIEW_TAGS.put("DART_OPERATION_SIGN", OPERATION_SIGN);
    PREVIEW_TAGS.put("DART_PARENTH", PARENTHS);
    PREVIEW_TAGS.put("DART_BRACKETS", BRACKETS);
    PREVIEW_TAGS.put("DART_BRACES", BRACES);
    PREVIEW_TAGS.put("DART_COMMA", COMMA);
    PREVIEW_TAGS.put("DART_DOT", DOT);
    PREVIEW_TAGS.put("DART_SEMICOLON", SEMICOLON);
    PREVIEW_TAGS.put("DART_COLON", COLON);
    PREVIEW_TAGS.put("DART_FAT_ARROW", FAT_ARROW);
    PREVIEW_TAGS.put("DART_BAD_CHARACTER", BAD_CHARACTER);
    PREVIEW_TAGS.put("DART_SYMBOL_LITERAL", SYMBOL_LITERAL);

    ATTRS = new AttributesDescriptor[]{
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.error"), ERROR),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.warning"), WARNING),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.hint"), HINT),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.block.comment"), BLOCK_COMMENT),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.doc.comment"), DOC_COMMENT),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.line.comment"), LINE_COMMENT),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.number"), NUMBER),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.string"), STRING),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.valid.string.escape"), VALID_STRING_ESCAPE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.invalid.string.escape"), INVALID_STRING_ESCAPE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.operator"), OPERATION_SIGN),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.parentheses"), PARENTHS),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.brackets"), BRACKETS),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.braces"), BRACES),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.comma"), COMMA),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.dot"), DOT),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.semicolon"), SEMICOLON),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.colon"), COLON),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.fat.arrow"), FAT_ARROW),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.bad.character"), BAD_CHARACTER),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.symbol.literal"), SYMBOL_LITERAL),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.annotation"), ANNOTATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.class"), CLASS),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.constructor"), CONSTRUCTOR),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.constructor.tearoff"), CONSTRUCTOR_TEAR_OFF),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.dynamic.local.variable.declaration"),
                               DYNAMIC_LOCAL_VARIABLE_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.dynamic.local.variable.reference"),
                               DYNAMIC_LOCAL_VARIABLE_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.dynamic.parameter.declaration"),
                               DYNAMIC_PARAMETER_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.dynamic.parameter.reference"),
                               DYNAMIC_PARAMETER_REFERENCE),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.enum"), ENUM),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.enum.constant"), ENUM_CONSTANT),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.extension"), EXTENSION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.function.type.alias"), FUNCTION_TYPE_ALIAS),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.type.alias"), TYPE_ALIAS),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.identifier"), IDENTIFIER),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.field.declaration"),
                               INSTANCE_FIELD_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.field.reference"), INSTANCE_FIELD_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.getter.declaration"),
                               INSTANCE_GETTER_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.getter.reference"), INSTANCE_GETTER_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.method.declaration"),
                               INSTANCE_METHOD_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.method.reference"), INSTANCE_METHOD_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.method.tearoff"), INSTANCE_METHOD_TEAR_OFF),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.setter.declaration"),
                               INSTANCE_SETTER_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.setter.reference"), INSTANCE_SETTER_REFERENCE),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.import.prefix"), IMPORT_PREFIX),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.keyword"), KEYWORD),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.label"), LABEL),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.library.name"), LIBRARY_NAME),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.local.function.declaration"),
                               LOCAL_FUNCTION_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.local.function.reference"), LOCAL_FUNCTION_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.local.function.tearoff"), LOCAL_FUNCTION_TEAR_OFF),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.local.variable.declaration"),
                               LOCAL_VARIABLE_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.local.variable.reference"), LOCAL_VARIABLE_REFERENCE),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.mixin"), MIXIN),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.parameter.declaration"), PARAMETER_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.parameter.reference"), PARAMETER_REFERENCE),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.field.declaration"), STATIC_FIELD_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.getter.declaration"), STATIC_GETTER_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.getter.reference"), STATIC_GETTER_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.method.declaration"), STATIC_METHOD_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.method.reference"), STATIC_METHOD_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.method.tearoff"), STATIC_METHOD_TEAR_OFF),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.setter.declaration"), STATIC_SETTER_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.setter.reference"), STATIC_SETTER_REFERENCE),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.top.level.function.declaration"),
                               TOP_LEVEL_FUNCTION_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.top.level.function.reference"),
                               TOP_LEVEL_FUNCTION_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.top.level.function.tearoff"),
                               TOP_LEVEL_FUNCTION_TEAR_OFF),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.top.level.getter.declaration"),
                               TOP_LEVEL_GETTER_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.top.level.getter.reference"),
                               TOP_LEVEL_GETTER_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.top.level.setter.declaration"),
                               TOP_LEVEL_SETTER_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.top.level.setter.reference"),
                               TOP_LEVEL_SETTER_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.top.level.variable.declaration"),
                               TOP_LEVEL_VARIABLE_DECLARATION),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.type.name.dynamic"), TYPE_NAME_DYNAMIC),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.type.parameter"), TYPE_PARAMETER),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.unresolved.instance.member.reference"),
                               UNRESOLVED_INSTANCE_MEMBER_REFERENCE),
    };
  }

  @Override
  public @NotNull String getDisplayName() {
    return DartBundle.message("dart.title");
  }

  @Override
  public Icon getIcon() {
    return DartFileType.INSTANCE.getIcon();
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
    return new DartSyntaxHighlighter();
  }

  @Override
  public @NotNull String getDemoText() {
    return PREVIEW_TEXT;
  }

  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return PREVIEW_TAGS;
  }
}
