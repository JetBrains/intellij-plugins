package com.jetbrains.lang.dart.highlight;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

import static com.jetbrains.lang.dart.highlight.DartSyntaxHighlighterColors.*;

public class DartColorsAndFontsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] ATTRS;

  @NonNls private static final Map<String, TextAttributesKey> ourTags = new HashMap<String, TextAttributesKey>();

  static {
    ATTRS = new AttributesDescriptor[]{
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.block.comment"), BLOCK_COMMENT),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.doc.comment"), DOC_COMMENT),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.line.comment"), LINE_COMMENT),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.number"), NUMBER),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.string"), STRING),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.valid.string.escape"), VALID_STRING_ESCAPE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.invalid.string.escape"), INVALID_STRING_ESCAPE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.operator"), OPERATION_SIGN),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.parenths"), PARENTHS),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.brackets"), BRACKETS),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.braces"), BRACES),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.comma"), COMMA),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.dot"), DOT),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.semicolon"), SEMICOLON),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.bad.character"), BAD_CHARACTER),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.symbol.literal"), SYMBOL_LITERAL),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.annotation"), ANNOTATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.class"), CLASS),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.constructor"), CONSTRUCTOR),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.dynamic.local.variable.declaration"), DYNAMIC_LOCAL_VARIABLE_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.dynamic.local.variable.reference"), DYNAMIC_LOCAL_VARIABLE_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.dynamic.parameter.declaration"), DYNAMIC_PARAMETER_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.dynamic.parameter.reference"), DYNAMIC_PARAMETER_REFERENCE),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.enum"), ENUM),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.enum.constant"), ENUM_CONSTANT),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.function.type.alias"), FUNCTION_TYPE_ALIAS),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.field.declaration"), INSTANCE_FIELD_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.field.reference"), INSTANCE_FIELD_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.getter.declaration"), INSTANCE_GETTER_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.getter.reference"), INSTANCE_GETTER_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.method.declaration"), INSTANCE_METHOD_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.method.reference"), INSTANCE_METHOD_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.setter.declaration"), INSTANCE_SETTER_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.setter.reference"), INSTANCE_SETTER_REFERENCE),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.import.prefix"), IMPORT_PREFIX),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.keyword"), KEYWORD),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.label"), LABEL),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.local.function.declaration"), LOCAL_FUNCTION_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.local.function.reference"), LOCAL_FUNCTION_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.local.variable.declaration"), LOCAL_VARIABLE_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.local.variable.reference"), LOCAL_VARIABLE_REFERENCE),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.parameter.declaration"), PARAMETER_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.parameter.reference"), PARAMETER_REFERENCE),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.field.declaration"), STATIC_FIELD_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.getter.declaration"), STATIC_GETTER_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.getter.reference"), STATIC_GETTER_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.method.declaration"), STATIC_METHOD_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.method.reference"), STATIC_METHOD_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.setter.declaration"), STATIC_SETTER_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.setter.reference"), STATIC_SETTER_REFERENCE),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.top.level.function.declaration"), TOP_LEVEL_FUNCTION_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.top.level.function.reference"), TOP_LEVEL_FUNCTION_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.top.level.getter.declaration"), TOP_LEVEL_GETTER_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.top.level.getter.reference"), TOP_LEVEL_GETTER_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.top.level.setter.declaration"), TOP_LEVEL_SETTER_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.top.level.setter.reference"), TOP_LEVEL_SETTER_REFERENCE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.top.level.variable.declaration"), TOP_LEVEL_VARIABLE_DECLARATION),

      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.type.name.dynamic"), TYPE_NAME_DYNAMIC),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.type.parameter"), TYPE_PARAMETER),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.unresolved.instance.member.reference"), UNRESOLVED_INSTANCE_MEMBER_REFERENCE),
    };

    ourTags.put("bad.escape", INVALID_STRING_ESCAPE);
    ourTags.put("escape", VALID_STRING_ESCAPE);
    ourTags.put("symbol", SYMBOL_LITERAL);

    ourTags.put("annotation", ANNOTATION);
    ourTags.put("class", CLASS);
    ourTags.put("constructor", CONSTRUCTOR);
    
    ourTags.put("dynamic.local.variable.declaration", DYNAMIC_LOCAL_VARIABLE_DECLARATION);
    ourTags.put("dynamic.local.variable.reference", DYNAMIC_LOCAL_VARIABLE_REFERENCE);
    ourTags.put("dynamic.parameter.declaration", DYNAMIC_PARAMETER_DECLARATION);
    ourTags.put("dynamic.parameter.reference", DYNAMIC_PARAMETER_REFERENCE);

    ourTags.put("enum", ENUM);
    ourTags.put("enum.constant", ENUM_CONSTANT);
    ourTags.put("function.type.alias", FUNCTION_TYPE_ALIAS);

    ourTags.put("instance.field.declaration", INSTANCE_FIELD_DECLARATION);
    ourTags.put("instance.field.reference", INSTANCE_FIELD_REFERENCE);
    ourTags.put("instance.getter.declaration", INSTANCE_GETTER_DECLARATION);
    ourTags.put("instance.getter.reference", INSTANCE_GETTER_REFERENCE);
    ourTags.put("instance.method.declaration", INSTANCE_METHOD_DECLARATION);
    ourTags.put("instance.method.reference", INSTANCE_METHOD_REFERENCE);
    ourTags.put("instance.getter.declaration", INSTANCE_SETTER_DECLARATION);
    ourTags.put("instance.getter.reference", INSTANCE_SETTER_REFERENCE);

    ourTags.put("import.prefix", IMPORT_PREFIX);
    ourTags.put("keyword", KEYWORD);
    ourTags.put("label", LABEL);

    ourTags.put("instance.function.declaration", LOCAL_FUNCTION_DECLARATION);
    ourTags.put("instance.function.reference", LOCAL_FUNCTION_REFERENCE);
    ourTags.put("instance.local.variable.declaration", LOCAL_VARIABLE_DECLARATION);
    ourTags.put("instance.local.variable.reference", LOCAL_VARIABLE_REFERENCE);

    ourTags.put("parameter.declaration", PARAMETER_DECLARATION);
    ourTags.put("parameter.reference", PARAMETER_REFERENCE);

    ourTags.put("static.field.declaration", STATIC_FIELD_DECLARATION);
    ourTags.put("static.getter.declaration", STATIC_GETTER_DECLARATION);
    ourTags.put("static.getter.reference", STATIC_GETTER_REFERENCE);
    ourTags.put("static.method.declaration", STATIC_METHOD_DECLARATION);
    ourTags.put("static.method.reference", STATIC_METHOD_REFERENCE);
    ourTags.put("static.getter.declaration", STATIC_SETTER_DECLARATION);
    ourTags.put("static.getter.reference", STATIC_SETTER_REFERENCE);

    ourTags.put("top.level.function.declaration", TOP_LEVEL_FUNCTION_DECLARATION);
    ourTags.put("top.level.function.reference", TOP_LEVEL_FUNCTION_REFERENCE);
    ourTags.put("top.level.getter.declaration", TOP_LEVEL_GETTER_DECLARATION);
    ourTags.put("top.level.getter.reference", TOP_LEVEL_GETTER_REFERENCE);
    ourTags.put("top.level.getter.declaration", TOP_LEVEL_SETTER_DECLARATION);
    ourTags.put("top.level.getter.reference", TOP_LEVEL_SETTER_REFERENCE);
    ourTags.put("top.level.variable.declaration", TOP_LEVEL_VARIABLE_DECLARATION);

    ourTags.put("type.name.dynamic", TYPE_NAME_DYNAMIC);
    ourTags.put("type.parameter", TYPE_PARAMETER);
    ourTags.put("unresolved.instance.member.reference", UNRESOLVED_INSTANCE_MEMBER_REFERENCE);
  }

  @NotNull
  public String getDisplayName() {
    return DartBundle.message("dart.title");
  }

  public Icon getIcon() {
    return DartFileType.INSTANCE.getIcon();
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
    return new DartSyntaxHighlighter();
  }

  @NotNull
  public String getDemoText() {
    return "/**\n" +
           " * documentation\n" +
           " */\n" +
           "<metadata>@Metadata</metadata>('text')\n" +
           "class <class>SomeClass</class> extends BaseClass <keyword>implements</keyword> <class>OtherClass</class> {\n" +
           "  /// documentation\n" +
           "  var <instance.member.variable>someField</instance.member.variable> = null; // line comment\n" +
           "  var <instance.member.variable>someString</instance.member.variable> = \"Escape sequences: <escape>\\n</escape> <escape>\\xFF</escape> <escape>\\u1234</escape> <escape>\\u{2F}</escape>\"\n" +
           "  <class>String</class> <instance.member.variable>otherString</instance.member.variable> = \"Invalid escape sequences: <bad.escape>\\xZZ</bad.escape> <bad.escape>\\uXYZZ</bad.escape> <bad.escape>\\u{XYZ}</bad.escape>\"\n" +
           "  <keyword>static</keyword> <builtin>num</builtin> <static.member.variable>staticField</static.member.variable> = 12345.67890;\n" +
           "\n" +
           "  <keyword>static</keyword> <static.member.function>staticFunction</static.member.function>() {\n" +
           "    <label>label</label>: <static.member.variable>staticField</static.member.variable>++; /* block comment */\n" +
           "  }\n\n" +
           "  <constructor.decl>SomeClass</constructor.decl>(this.someString);\n" +
           "\n" +
           "  <instance.member.function>foo</instance.member.function>(<builtin>dynamic</builtin> <parameter>param</parameter>) {\n" +
           "    <top.level.func.call>print</top.level.func.call>(<instance.member.variable>someString</instance.member.variable> + <parameter>param</parameter>);\n" +
           "    var <local.variable>localVar</local.variable> = <class>SomeClass</class>.<static.member.variable>staticField</static.member.variable>; \n" +
           "    var <local.variable>localVar2</local.variable> = new <constructor.call>SomeClass</constructor.call>('content').<instance.member.function.call>bar</instance.member.function.call>();\n" +
           "    <local.variable>localVar</local.variable>++; \n" +
           "    <function>localFunction</function>() {\n" +
           "      <local.variable>localVar</local.variable> = ```; // bad character\n" +
           "    };\n" +
           "  }\n" +
           "  <builtin>int</builtin> <instance.member.function>f</instance.member.function>() => 13;\n" +
           "}\n\n" +
           "<keyword>abstract</keyword> class BaseClass {\n" +
           "  <builtin>int</builtin> g() => <abstract.call>f</abstract.call>();\n" +
           "  <builtin>int</builtin> f();\n" +
           "}\n\n" +
           "const className = <symbol>#MyClass</symbol>;\n\n" +
           "var <top.level.var.decl>topLevelVar</top.level.var.decl> = new SomeClass(null).<inherited.call>g</inherited.call>();\n\n" +
           "<top.level.func.decl>main</top.level.func.decl>() {\n" +
           "  <top.level.func.call>print</top.level.func.call>(<top.level.var.call>topLevelVar</top.level.var.call>);\n" +
           "}\n";
  }

  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ourTags;
  }
}
