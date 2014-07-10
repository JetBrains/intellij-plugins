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
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.line.comment"), LINE_COMMENT),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.block.comment"), BLOCK_COMMENT),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.doc.comment"), DOC_COMMENT),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.keyword"), KEYWORD),
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
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.parameter"), PARAMETER),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.local.function"), FUNCTION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.local.variable"), LOCAL_VARIABLE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.local.variable.access"), LOCAL_VARIABLE_ACCESS),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.label"), LABEL),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.class"), CLASS),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.metadata"), METADATA),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.builtin"), BUILTIN),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.member.function"), INSTANCE_MEMBER_FUNCTION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.member.function.call"),
                               INSTANCE_MEMBER_FUNCTION_CALL),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.member.inherited.function.call"),
                               INHERITED_MEMBER_FUNCTION_CALL),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.member.abstract.function.call"),
                               ABSTRACT_MEMBER_FUNCTION_CALL),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.member.function"), STATIC_MEMBER_FUNCTION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.member.function.call"),
                               STATIC_MEMBER_FUNCTION_CALL),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.toplevel.function"), TOP_LEVEL_FUNCTION_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.toplevel.function.call"), TOP_LEVEL_FUNCTION_CALL),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.toplevel.variable"), TOP_LEVEL_VARIABLE_DECLARATION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.toplevel.variable.access"), TOP_LEVEL_VARIABLE_ACCESS),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.member.variable"), INSTANCE_MEMBER_VARIABLE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.member.variable.access"),
                               INSTANCE_MEMBER_VARIABLE_ACCESS),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.member.variable"), STATIC_MEMBER_VARIABLE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.member.variable.access"),
                               STATIC_MEMBER_VARIABLE_ACCESS),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.constructor.call"), CONSTRUCTOR_CALL),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.constructor.decl"), CONSTRUCTOR_DECLARATION),
    };

    ourTags.put("parameter", PARAMETER);
    ourTags.put("function", FUNCTION);
    ourTags.put("local.variable", LOCAL_VARIABLE);
    ourTags.put("label", LABEL);
    ourTags.put("class", CLASS);
    ourTags.put("keyword", KEYWORD);
    ourTags.put("metadata", METADATA);
    ourTags.put("builtin", BUILTIN);
    ourTags.put("instance.member.function", INSTANCE_MEMBER_FUNCTION);
    ourTags.put("instance.member.function.call", INSTANCE_MEMBER_FUNCTION_CALL);
    ourTags.put("static.member.function", STATIC_MEMBER_FUNCTION);
    ourTags.put("instance.member.variable", INSTANCE_MEMBER_VARIABLE);
    ourTags.put("static.member.variable", STATIC_MEMBER_VARIABLE);
    ourTags.put("escape", VALID_STRING_ESCAPE);
    ourTags.put("bad.escape", INVALID_STRING_ESCAPE);
    ourTags.put("constructor.call", CONSTRUCTOR_CALL);
    ourTags.put("constructor.decl", CONSTRUCTOR_DECLARATION);
    ourTags.put("abstract.call", ABSTRACT_MEMBER_FUNCTION_CALL);
    ourTags.put("inherited.call", INHERITED_MEMBER_FUNCTION_CALL);
    ourTags.put("top.level.var.call", TOP_LEVEL_VARIABLE_ACCESS);
    ourTags.put("top.level.var.decl", TOP_LEVEL_VARIABLE_DECLARATION);
    ourTags.put("top.level.func.call", TOP_LEVEL_FUNCTION_CALL);
    ourTags.put("top.level.func.decl", TOP_LEVEL_FUNCTION_DECLARATION);
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
           "var <top.level.var.decl>topLevelVar</top.level.var.decl> = new SomeClass(null).<inherited.call>g</inherited.call>();\n\n" +
           "<top.level.func.decl>main</top.level.func.decl>() {\n" +
           "  <top.level.func.call>print</top.level.func.call>(<top.level.var.call>topLevelVar</top.level.var.call>);\n" +
           "}\n"
      ;
  }

  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ourTags;
  }
}
