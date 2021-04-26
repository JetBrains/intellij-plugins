// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.intellij.protobuf.lang.psi.PbTextTypes.*;
import static com.intellij.protobuf.lang.parser.PbParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.parser.GeneratedParserUtilBase.Parser;
import static com.intellij.protobuf.lang.psi.ProtoTokenTypes.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class PbTextParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType type, PsiBuilder builder) {
    parseLight(type, builder);
    return builder.getTreeBuilt();
  }

  public void parseLight(IElementType type, PsiBuilder builder) {
    boolean result;
    builder = adapt_builder_(type, builder, this, null);
    Marker marker = enter_section_(builder, 0, _COLLAPSE_, null);
    result = parse_root_(type, builder);
    exit_section_(builder, 0, marker, type, result, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType type, PsiBuilder builder) {
    return parse_root_(type, builder, 0);
  }

  static boolean parse_root_(IElementType type, PsiBuilder builder, int level) {
    boolean result;
    if (type == DOMAIN) {
      result = Domain(builder, level + 1);
    }
    else if (type == EXTENSION_NAME) {
      result = ExtensionName(builder, level + 1);
    }
    else if (type == FIELD) {
      result = Field(builder, level + 1);
    }
    else if (type == FIELD_NAME) {
      result = FieldName(builder, level + 1);
    }
    else if (type == IDENTIFIER_VALUE) {
      result = IdentifierValue(builder, level + 1);
    }
    else if (type == MESSAGE_VALUE) {
      result = MessageValue(builder, level + 1);
    }
    else if (type == NUMBER_VALUE) {
      result = NumberValue(builder, level + 1);
    }
    else if (type == STRING_PART) {
      result = StringPart(builder, level + 1);
    }
    else if (type == STRING_VALUE) {
      result = StringValue(builder, level + 1);
    }
    else if (type == SYMBOL_PATH) {
      result = SymbolPath(builder, level + 1, -1);
    }
    else if (type == VALUE_LIST) {
      result = ValueList(builder, level + 1);
    }
    else {
      result = Root(builder, level + 1);
    }
    return result;
  }

  /* ********************************************************** */
  // '{' BraceMessageEntry* '}'
  static boolean BraceMessage(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "BraceMessage")) return false;
    if (!nextTokenIs(builder, LBRACE)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = consumeToken(builder, LBRACE);
    pinned = result; // pin = 1
    result = result && report_error_(builder, BraceMessage_1(builder, level + 1));
    result = pinned && consumeToken(builder, RBRACE) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // BraceMessageEntry*
  private static boolean BraceMessage_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "BraceMessage_1")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!BraceMessageEntry(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "BraceMessage_1", pos)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // !'}' Field
  static boolean BraceMessageEntry(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "BraceMessageEntry")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = BraceMessageEntry_0(builder, level + 1);
    pinned = result; // pin = 1
    result = result && Field(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, PbTextParser::MessageRecovery);
    return result || pinned;
  }

  // !'}'
  private static boolean BraceMessageEntry_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "BraceMessageEntry_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !consumeToken(builder, RBRACE);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // Identifier ('.' Identifier)*
  public static boolean Domain(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Domain")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, DOMAIN, "<domain>");
    result = Identifier(builder, level + 1);
    result = result && Domain_1(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // ('.' Identifier)*
  private static boolean Domain_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Domain_1")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!Domain_1_0(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "Domain_1", pos)) break;
    }
    return true;
  }

  // '.' Identifier
  private static boolean Domain_1_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Domain_1_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, DOT);
    result = result && Identifier(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // '[' (Domain '/')? SymbolPath ']'
  public static boolean ExtensionName(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtensionName")) return false;
    if (!nextTokenIs(builder, LBRACK)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, EXTENSION_NAME, null);
    result = consumeToken(builder, LBRACK);
    pinned = result; // pin = 1
    result = result && report_error_(builder, ExtensionName_1(builder, level + 1));
    result = pinned && report_error_(builder, SymbolPath(builder, level + 1, -1)) && result;
    result = pinned && consumeToken(builder, RBRACK) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // (Domain '/')?
  private static boolean ExtensionName_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtensionName_1")) return false;
    ExtensionName_1_0(builder, level + 1);
    return true;
  }

  // Domain '/'
  private static boolean ExtensionName_1_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtensionName_1_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = Domain(builder, level + 1);
    result = result && consumeToken(builder, SLASH);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // FieldName ((':' Value) | (':'? (MessageValue | ValueList))) (';' | ',')?
  public static boolean Field(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Field")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, FIELD, "<field>");
    result = FieldName(builder, level + 1);
    pinned = result; // pin = 1
    result = result && report_error_(builder, Field_1(builder, level + 1));
    result = pinned && Field_2(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // (':' Value) | (':'? (MessageValue | ValueList))
  private static boolean Field_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Field_1")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = Field_1_0(builder, level + 1);
    if (!result) result = Field_1_1(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // ':' Value
  private static boolean Field_1_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Field_1_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, COLON);
    result = result && Value(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // ':'? (MessageValue | ValueList)
  private static boolean Field_1_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Field_1_1")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = Field_1_1_0(builder, level + 1);
    result = result && Field_1_1_1(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // ':'?
  private static boolean Field_1_1_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Field_1_1_0")) return false;
    consumeToken(builder, COLON);
    return true;
  }

  // MessageValue | ValueList
  private static boolean Field_1_1_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Field_1_1_1")) return false;
    boolean result;
    result = MessageValue(builder, level + 1);
    if (!result) result = ValueList(builder, level + 1);
    return result;
  }

  // (';' | ',')?
  private static boolean Field_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Field_2")) return false;
    Field_2_0(builder, level + 1);
    return true;
  }

  // ';' | ','
  private static boolean Field_2_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Field_2_0")) return false;
    boolean result;
    result = consumeToken(builder, SEMI);
    if (!result) result = consumeToken(builder, COMMA);
    return result;
  }

  /* ********************************************************** */
  // ExtensionName | Identifier
  public static boolean FieldName(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "FieldName")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, FIELD_NAME, "<field name>");
    result = ExtensionName(builder, level + 1);
    if (!result) result = Identifier(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // IDENTIFIER_LITERAL | <<parseKeywordIdentifier>>
  static boolean Identifier(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Identifier")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, IDENTIFIER_LITERAL);
    if (!result) result = parseKeywordIdentifier(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // Identifier
  public static boolean IdentifierValue(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "IdentifierValue")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, IDENTIFIER_VALUE, "<identifier value>");
    result = Identifier(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // !(FieldName | '}' | '>')
  static boolean MessageRecovery(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "MessageRecovery")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !MessageRecovery_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // FieldName | '}' | '>'
  private static boolean MessageRecovery_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "MessageRecovery_0")) return false;
    boolean result;
    result = FieldName(builder, level + 1);
    if (!result) result = consumeToken(builder, RBRACE);
    if (!result) result = consumeToken(builder, GT);
    return result;
  }

  /* ********************************************************** */
  // BraceMessage | PointyMessage
  public static boolean MessageValue(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "MessageValue")) return false;
    if (!nextTokenIs(builder, "<message value>", LBRACE, LT)) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, MESSAGE_VALUE, "<message value>");
    result = BraceMessage(builder, level + 1);
    if (!result) result = PointyMessage(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // '-'? (INTEGER_LITERAL | FLOAT_LITERAL | "inf" | "infinity" | "nan")
  public static boolean NumberValue(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "NumberValue")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, NUMBER_VALUE, "<number value>");
    result = NumberValue_0(builder, level + 1);
    result = result && NumberValue_1(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // '-'?
  private static boolean NumberValue_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "NumberValue_0")) return false;
    consumeToken(builder, MINUS);
    return true;
  }

  // INTEGER_LITERAL | FLOAT_LITERAL | "inf" | "infinity" | "nan"
  private static boolean NumberValue_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "NumberValue_1")) return false;
    boolean result;
    result = consumeToken(builder, INTEGER_LITERAL);
    if (!result) result = consumeToken(builder, FLOAT_LITERAL);
    if (!result) result = consumeToken(builder, "inf");
    if (!result) result = consumeToken(builder, "infinity");
    if (!result) result = consumeToken(builder, "nan");
    return result;
  }

  /* ********************************************************** */
  // '<' PointyMessageEntry* '>'
  static boolean PointyMessage(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "PointyMessage")) return false;
    if (!nextTokenIs(builder, LT)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = consumeToken(builder, LT);
    pinned = result; // pin = 1
    result = result && report_error_(builder, PointyMessage_1(builder, level + 1));
    result = pinned && consumeToken(builder, GT) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // PointyMessageEntry*
  private static boolean PointyMessage_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "PointyMessage_1")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!PointyMessageEntry(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "PointyMessage_1", pos)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // !'>' Field
  static boolean PointyMessageEntry(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "PointyMessageEntry")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = PointyMessageEntry_0(builder, level + 1);
    pinned = result; // pin = 1
    result = result && Field(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, PbTextParser::MessageRecovery);
    return result || pinned;
  }

  // !'>'
  private static boolean PointyMessageEntry_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "PointyMessageEntry_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !consumeToken(builder, GT);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // RootMessageEntry*
  static boolean Root(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Root")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!RootMessageEntry(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "Root", pos)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // !<<eof>> Field
  static boolean RootMessageEntry(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "RootMessageEntry")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = RootMessageEntry_0(builder, level + 1);
    pinned = result; // pin = 1
    result = result && Field(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, PbTextParser::RootMessageRecovery);
    return result || pinned;
  }

  // !<<eof>>
  private static boolean RootMessageEntry_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "RootMessageEntry_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !eof(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // !FieldName
  static boolean RootMessageRecovery(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "RootMessageRecovery")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !FieldName(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // STRING_LITERAL
  public static boolean StringPart(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "StringPart")) return false;
    if (!nextTokenIs(builder, STRING_LITERAL)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, STRING_LITERAL);
    exit_section_(builder, marker, STRING_PART, result);
    return result;
  }

  /* ********************************************************** */
  // StringPart+
  public static boolean StringValue(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "StringValue")) return false;
    if (!nextTokenIs(builder, STRING_LITERAL)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = StringPart(builder, level + 1);
    while (result) {
      int pos = current_position_(builder);
      if (!StringPart(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "StringValue", pos)) break;
    }
    exit_section_(builder, marker, STRING_VALUE, result);
    return result;
  }

  /* ********************************************************** */
  // Identifier
  static boolean SymbolPathAtom(PsiBuilder builder, int level) {
    return Identifier(builder, level + 1);
  }

  /* ********************************************************** */
  // IdentifierValue | NumberValue | StringValue
  static boolean Value(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Value")) return false;
    boolean result;
    result = IdentifierValue(builder, level + 1);
    if (!result) result = NumberValue(builder, level + 1);
    if (!result) result = StringValue(builder, level + 1);
    return result;
  }

  /* ********************************************************** */
  // '[' ValueListInner? ']'
  public static boolean ValueList(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ValueList")) return false;
    if (!nextTokenIs(builder, LBRACK)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, VALUE_LIST, null);
    result = consumeToken(builder, LBRACK);
    pinned = result; // pin = 1
    result = result && report_error_(builder, ValueList_1(builder, level + 1));
    result = pinned && consumeToken(builder, RBRACK) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // ValueListInner?
  private static boolean ValueList_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ValueList_1")) return false;
    ValueListInner(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // !']' ValueOrMessage (!']' ',' ValueOrMessage)*
  static boolean ValueListInner(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ValueListInner")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = ValueListInner_0(builder, level + 1);
    pinned = result; // pin = 1
    result = result && report_error_(builder, ValueOrMessage(builder, level + 1));
    result = pinned && ValueListInner_2(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // !']'
  private static boolean ValueListInner_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ValueListInner_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !consumeToken(builder, RBRACK);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // (!']' ',' ValueOrMessage)*
  private static boolean ValueListInner_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ValueListInner_2")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!ValueListInner_2_0(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "ValueListInner_2", pos)) break;
    }
    return true;
  }

  // !']' ',' ValueOrMessage
  private static boolean ValueListInner_2_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ValueListInner_2_0")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = ValueListInner_2_0_0(builder, level + 1);
    pinned = result; // pin = 1
    result = result && report_error_(builder, consumeToken(builder, COMMA));
    result = pinned && ValueOrMessage(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // !']'
  private static boolean ValueListInner_2_0_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ValueListInner_2_0_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !consumeToken(builder, RBRACK);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // Value | MessageValue
  static boolean ValueOrMessage(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ValueOrMessage")) return false;
    boolean result;
    result = Value(builder, level + 1);
    if (!result) result = MessageValue(builder, level + 1);
    return result;
  }

  /* ********************************************************** */
  // Expression root: SymbolPath
  // Operator priority table:
  // 0: POSTFIX(SymbolPathTuple)
  // 1: ATOM(SymbolPathUnit)
  public static boolean SymbolPath(PsiBuilder builder, int level, int priority) {
    if (!recursion_guard_(builder, level, "SymbolPath")) return false;
    addVariant(builder, "<symbol path>");
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, "<symbol path>");
    result = SymbolPathUnit(builder, level + 1);
    pinned = result;
    result = result && SymbolPath_0(builder, level + 1, priority);
    exit_section_(builder, level, marker, null, result, pinned, null);
    return result || pinned;
  }

  public static boolean SymbolPath_0(PsiBuilder builder, int level, int priority) {
    if (!recursion_guard_(builder, level, "SymbolPath_0")) return false;
    boolean result = true;
    while (true) {
      Marker marker = enter_section_(builder, level, _LEFT_, null);
      if (priority < 0 && SymbolPathTuple_0(builder, level + 1)) {
        result = true;
        exit_section_(builder, level, marker, SYMBOL_PATH, result, true, null);
      }
      else {
        exit_section_(builder, level, marker, null, false, false, null);
        break;
      }
    }
    return result;
  }

  // '.' SymbolPathAtom
  private static boolean SymbolPathTuple_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "SymbolPathTuple_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeTokenSmart(builder, DOT);
    result = result && SymbolPathAtom(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // SymbolPathAtom
  public static boolean SymbolPathUnit(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "SymbolPathUnit")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _COLLAPSE_, SYMBOL_PATH, "<symbol path unit>");
    result = SymbolPathAtom(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

}
