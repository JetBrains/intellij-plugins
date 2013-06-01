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

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: 10/13/11
 * Time: 2:38 PM
 */
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
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.label"), LABEL),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.class"), CLASS),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.builtin"), BUILTIN),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.interface"), INTERFACE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.member.function"), INSTANCE_MEMBER_FUNCTION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.member.function"), STATIC_MEMBER_FUNCTION),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.instance.member.variable"), INSTANCE_MEMBER_VARIABLE),
      new AttributesDescriptor(DartBundle.message("dart.color.settings.description.static.member.variable"), STATIC_MEMBER_VARIABLE)
    };

    ourTags.put("parameter", PARAMETER);
    ourTags.put("function", FUNCTION);
    ourTags.put("local.variable", LOCAL_VARIABLE);
    ourTags.put("label", LABEL);
    ourTags.put("class", CLASS);
    ourTags.put("builtin", BUILTIN);
    ourTags.put("interface", INTERFACE);
    ourTags.put("instance.member.function", INSTANCE_MEMBER_FUNCTION);
    ourTags.put("static.member.function", STATIC_MEMBER_FUNCTION);
    ourTags.put("instance.member.variable", INSTANCE_MEMBER_VARIABLE);
    ourTags.put("static.member.variable", STATIC_MEMBER_VARIABLE);
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
    return "/* Block comment */\n" +
           "\n" +
           "/**\n" +
           " Document comment\n" +
           "**/\n" +
           "class <class>SomeClass</class> implements <interface>IOther</interface> { // some comment\n" +
           "  var <instance.member.variable>field</instance.member.variable> = null;\n" +
           "  <builtin>num</builtin> <instance.member.variable>unusedField</instance.member.variable> = 12345.67890;\n" +
           "  <builtin>String</builtin> <instance.member.variable>anotherString</instance.member.variable> = \"Another\\nStrin\\g\";\n" +
           "  static var <static.member.variable>staticField</static.member.variable> = 0;\n" +
           "\n" +
           "  static <static.member.function>inc</static.member.function>() {\n" +
           "    <static.member.variable>staticField</static.member.variable>++;\n" +
           "  }\n" +
           "  <instance.member.function>foo</instance.member.function>(<interface>AnInterface</interface> <parameter>param</parameter>) {\n" +
           "    print(<instance.member.variable>anotherString</instance.member.variable> + <parameter>param</parameter>);\n" +
           "    var <local.variable>reassignedValue</local.variable> = <class>SomeClass</class>.<static.member.variable>staticField</static.member.variable>; \n" +
           "    <local.variable>reassignedValue</local.variable> ++; \n" +
           "    <function>localFunction</function>() {\n" +
           "      var <local.variable>a</local.variable> = @@@;// bad character\n" +
           "    };\n" +
           "    parseData(<label>fileName</label>:'file.txt');\n" +
           "  }\n" +
           "}";
  }

  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ourTags;
  }
}
