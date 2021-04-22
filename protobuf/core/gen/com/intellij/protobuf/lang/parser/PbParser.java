// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.intellij.protobuf.lang.psi.PbTypes.*;
import static com.intellij.protobuf.lang.parser.PbParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.parser.GeneratedParserUtilBase.Parser;
import static com.intellij.protobuf.lang.psi.ProtoTokenTypes.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class PbParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType type, PsiBuilder builder) {
    parseLight(type, builder);
    return builder.getTreeBuilt();
  }

  public void parseLight(IElementType type, PsiBuilder builder) {
    boolean result;
    builder = adapt_builder_(type, builder, this, EXTENDS_SETS_);
    Marker marker = enter_section_(builder, 0, _COLLAPSE_, null);
    result = parse_root_(type, builder);
    exit_section_(builder, 0, marker, type, result, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType type, PsiBuilder builder) {
    return parse_root_(type, builder, 0);
  }

  static boolean parse_root_(IElementType type, PsiBuilder builder, int level) {
    boolean result;
    if (type == AGGREGATE_VALUE) {
      result = AggregateValue(builder, level + 1);
    }
    else if (type == ENUM_BODY) {
      result = EnumBody(builder, level + 1);
    }
    else if (type == ENUM_DEFINITION) {
      result = EnumDefinition(builder, level + 1);
    }
    else if (type == ENUM_RESERVED_RANGE) {
      result = EnumReservedRange(builder, level + 1);
    }
    else if (type == ENUM_RESERVED_STATEMENT) {
      result = EnumReservedStatement(builder, level + 1);
    }
    else if (type == ENUM_VALUE) {
      result = EnumValue(builder, level + 1);
    }
    else if (type == EXTEND_BODY) {
      result = ExtendBody(builder, level + 1);
    }
    else if (type == EXTEND_DEFINITION) {
      result = ExtendDefinition(builder, level + 1);
    }
    else if (type == EXTENSION_NAME) {
      result = ExtensionName(builder, level + 1);
    }
    else if (type == EXTENSION_RANGE) {
      result = ExtensionRange(builder, level + 1);
    }
    else if (type == EXTENSIONS_STATEMENT) {
      result = ExtensionsStatement(builder, level + 1);
    }
    else if (type == FIELD_LABEL) {
      result = FieldLabel(builder, level + 1);
    }
    else if (type == GROUP_DEFINITION) {
      result = GroupDefinition(builder, level + 1);
    }
    else if (type == GROUP_OPTION_CONTAINER) {
      result = GroupOptionContainer(builder, level + 1);
    }
    else if (type == IDENTIFIER_VALUE) {
      result = IdentifierValue(builder, level + 1);
    }
    else if (type == IMPORT_NAME) {
      result = ImportName(builder, level + 1);
    }
    else if (type == IMPORT_STATEMENT) {
      result = ImportStatement(builder, level + 1);
    }
    else if (type == MAP_FIELD) {
      result = MapField(builder, level + 1);
    }
    else if (type == MESSAGE_BODY) {
      result = MessageBody(builder, level + 1);
    }
    else if (type == MESSAGE_DEFINITION) {
      result = MessageDefinition(builder, level + 1);
    }
    else if (type == MESSAGE_TYPE_NAME) {
      result = MessageTypeName(builder, level + 1);
    }
    else if (type == METHOD_OPTIONS) {
      result = MethodOptions(builder, level + 1);
    }
    else if (type == NUMBER_VALUE) {
      result = NumberValue(builder, level + 1);
    }
    else if (type == ONEOF_BODY) {
      result = OneofBody(builder, level + 1);
    }
    else if (type == ONEOF_DEFINITION) {
      result = OneofDefinition(builder, level + 1);
    }
    else if (type == OPTION_EXPRESSION) {
      result = OptionExpression(builder, level + 1);
    }
    else if (type == OPTION_LIST) {
      result = OptionList(builder, level + 1);
    }
    else if (type == OPTION_NAME) {
      result = OptionName(builder, level + 1, -1);
    }
    else if (type == OPTION_STATEMENT) {
      result = OptionStatement(builder, level + 1);
    }
    else if (type == PACKAGE_NAME) {
      result = PackageName(builder, level + 1, -1);
    }
    else if (type == PACKAGE_STATEMENT) {
      result = PackageStatement(builder, level + 1);
    }
    else if (type == RESERVED_RANGE) {
      result = ReservedRange(builder, level + 1);
    }
    else if (type == RESERVED_STATEMENT) {
      result = ReservedStatement(builder, level + 1);
    }
    else if (type == SERVICE_BODY) {
      result = ServiceBody(builder, level + 1);
    }
    else if (type == SERVICE_DEFINITION) {
      result = ServiceDefinition(builder, level + 1);
    }
    else if (type == SERVICE_METHOD) {
      result = ServiceMethod(builder, level + 1);
    }
    else if (type == SERVICE_METHOD_TYPE) {
      result = ServiceMethodType(builder, level + 1);
    }
    else if (type == SERVICE_STREAM) {
      result = ServiceStream(builder, level + 1);
    }
    else if (type == SIMPLE_FIELD) {
      result = SimpleField(builder, level + 1);
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
    else if (type == SYNTAX_STATEMENT) {
      result = SyntaxStatement(builder, level + 1);
    }
    else if (type == TYPE_NAME) {
      result = TypeName(builder, level + 1);
    }
    else {
      result = Root(builder, level + 1);
    }
    return result;
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(MESSAGE_TYPE_NAME, TYPE_NAME),
  };

  /* ********************************************************** */
  // <<Block <<BlockBodyOptional AggregateValueEntry>>>>
  public static boolean AggregateValue(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "AggregateValue")) return false;
    if (!nextTokenIs(builder, LBRACE)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = Block(builder, level + 1, AggregateValue_0_0_parser_);
    exit_section_(builder, marker, AGGREGATE_VALUE, result);
    return result;
  }

  /* ********************************************************** */
  // <<parseTextField>>
  static boolean AggregateValueEntry(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "AggregateValueEntry")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = parseTextField(builder, level + 1);
    exit_section_(builder, level, marker, result, false, PbParser::AggregateValueRecovery);
    return result;
  }

  /* ********************************************************** */
  // !('}' | <<parseTextFieldName>>)
  static boolean AggregateValueRecovery(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "AggregateValueRecovery")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !AggregateValueRecovery_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // '}' | <<parseTextFieldName>>
  private static boolean AggregateValueRecovery_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "AggregateValueRecovery_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, RBRACE);
    if (!result) result = parseTextFieldName(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // '{' <<p>> '}'
  static boolean Block(PsiBuilder builder, int level, Parser aP) {
    if (!recursion_guard_(builder, level, "Block")) return false;
    if (!nextTokenIs(builder, LBRACE)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = consumeToken(builder, LBRACE);
    pinned = result; // pin = 1
    result = result && report_error_(builder, aP.parse(builder, level));
    result = pinned && consumeToken(builder, RBRACE) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  static Parser BlockBodyOptional_$(Parser aP) {
    return (builder, level) -> BlockBodyOptional(builder, level + 1, aP);
  }

  // <<BlockEntry <<p>> >>*
  static boolean BlockBodyOptional(PsiBuilder builder, int level, Parser aP) {
    if (!recursion_guard_(builder, level, "BlockBodyOptional")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!BlockEntry(builder, level + 1, aP)) break;
      if (!empty_element_parsed_guard_(builder, "BlockBodyOptional", pos)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // !'}' <<p>>
  static boolean BlockEntry(PsiBuilder builder, int level, Parser aP) {
    if (!recursion_guard_(builder, level, "BlockEntry")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = BlockEntry_0(builder, level + 1);
    pinned = result; // pin = 1
    result = result && aP.parse(builder, level);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // !'}'
  private static boolean BlockEntry_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "BlockEntry_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !consumeToken(builder, RBRACE);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // !<<term>> <<entry>> (!<<term>> ',' <<entry>>)*
  static boolean CommaSeparatedList(PsiBuilder builder, int level, Parser aTerm, Parser aEntry) {
    if (!recursion_guard_(builder, level, "CommaSeparatedList")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = CommaSeparatedList_0(builder, level + 1, aTerm);
    pinned = result; // pin = 1
    result = result && report_error_(builder, aEntry.parse(builder, level));
    result = pinned && CommaSeparatedList_2(builder, level + 1, aTerm, aEntry) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // !<<term>>
  private static boolean CommaSeparatedList_0(PsiBuilder builder, int level, Parser aTerm) {
    if (!recursion_guard_(builder, level, "CommaSeparatedList_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !aTerm.parse(builder, level);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // (!<<term>> ',' <<entry>>)*
  private static boolean CommaSeparatedList_2(PsiBuilder builder, int level, Parser aTerm, Parser aEntry) {
    if (!recursion_guard_(builder, level, "CommaSeparatedList_2")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!CommaSeparatedList_2_0(builder, level + 1, aTerm, aEntry)) break;
      if (!empty_element_parsed_guard_(builder, "CommaSeparatedList_2", pos)) break;
    }
    return true;
  }

  // !<<term>> ',' <<entry>>
  private static boolean CommaSeparatedList_2_0(PsiBuilder builder, int level, Parser aTerm, Parser aEntry) {
    if (!recursion_guard_(builder, level, "CommaSeparatedList_2_0")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = CommaSeparatedList_2_0_0(builder, level + 1, aTerm);
    pinned = result; // pin = 1
    result = result && report_error_(builder, consumeToken(builder, COMMA));
    result = pinned && aEntry.parse(builder, level) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // !<<term>>
  private static boolean CommaSeparatedList_2_0_0(PsiBuilder builder, int level, Parser aTerm) {
    if (!recursion_guard_(builder, level, "CommaSeparatedList_2_0_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !aTerm.parse(builder, level);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // <<Block <<BlockBodyOptional EnumEntry>> >>
  public static boolean EnumBody(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumBody")) return false;
    if (!nextTokenIs(builder, LBRACE)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = Block(builder, level + 1, EnumBody_0_0_parser_);
    exit_section_(builder, marker, ENUM_BODY, result);
    return result;
  }

  /* ********************************************************** */
  // enum Identifier EnumBody
  public static boolean EnumDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumDefinition")) return false;
    if (!nextTokenIs(builder, ENUM)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ENUM_DEFINITION, null);
    result = consumeToken(builder, ENUM);
    pinned = result; // pin = 1
    result = result && report_error_(builder, Identifier(builder, level + 1));
    result = pinned && EnumBody(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // OptionStatement | EnumReservedStatement | EnumValue | ';'
  static boolean EnumEntry(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumEntry")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = OptionStatement(builder, level + 1);
    if (!result) result = EnumReservedStatement(builder, level + 1);
    if (!result) result = EnumValue(builder, level + 1);
    if (!result) result = consumeToken(builder, SEMI);
    exit_section_(builder, level, marker, result, false, PbParser::EnumRecovery);
    return result;
  }

  /* ********************************************************** */
  // !(option | reserved | EnumValue | ';' | '}')
  static boolean EnumRecovery(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumRecovery")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !EnumRecovery_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // option | reserved | EnumValue | ';' | '}'
  private static boolean EnumRecovery_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumRecovery_0")) return false;
    boolean result;
    result = consumeToken(builder, OPTION);
    if (!result) result = consumeToken(builder, RESERVED);
    if (!result) result = EnumValue(builder, level + 1);
    if (!result) result = consumeToken(builder, SEMI);
    if (!result) result = consumeToken(builder, RBRACE);
    return result;
  }

  /* ********************************************************** */
  // &StringValue <<CommaSeparatedList ';' StringValue>>
  static boolean EnumReservedNames(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumReservedNames")) return false;
    if (!nextTokenIs(builder, STRING_LITERAL)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = EnumReservedNames_0(builder, level + 1);
    result = result && CommaSeparatedList(builder, level + 1, SEMI_parser_, PbParser::StringValue);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // &StringValue
  private static boolean EnumReservedNames_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumReservedNames_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _AND_);
    result = StringValue(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // &IntegerValue <<CommaSeparatedList ';' EnumReservedRange>>
  static boolean EnumReservedNumbers(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumReservedNumbers")) return false;
    if (!nextTokenIs(builder, "", INTEGER_LITERAL, MINUS)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = EnumReservedNumbers_0(builder, level + 1);
    result = result && CommaSeparatedList(builder, level + 1, SEMI_parser_, PbParser::EnumReservedRange);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // &IntegerValue
  private static boolean EnumReservedNumbers_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumReservedNumbers_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _AND_);
    result = IntegerValue(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // IntegerValue (to (IntegerValue | max))?
  public static boolean EnumReservedRange(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumReservedRange")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ENUM_RESERVED_RANGE, "<enum reserved range>");
    result = IntegerValue(builder, level + 1);
    pinned = result; // pin = 1
    result = result && EnumReservedRange_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, PbParser::EnumReservedRangeRecovery);
    return result || pinned;
  }

  // (to (IntegerValue | max))?
  private static boolean EnumReservedRange_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumReservedRange_1")) return false;
    EnumReservedRange_1_0(builder, level + 1);
    return true;
  }

  // to (IntegerValue | max)
  private static boolean EnumReservedRange_1_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumReservedRange_1_0")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = consumeToken(builder, TO);
    pinned = result; // pin = 1
    result = result && EnumReservedRange_1_0_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // IntegerValue | max
  private static boolean EnumReservedRange_1_0_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumReservedRange_1_0_1")) return false;
    boolean result;
    result = IntegerValue(builder, level + 1);
    if (!result) result = consumeToken(builder, MAX);
    return result;
  }

  /* ********************************************************** */
  // !(IntegerValue | ',') EnumRecovery
  static boolean EnumReservedRangeRecovery(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumReservedRangeRecovery")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = EnumReservedRangeRecovery_0(builder, level + 1);
    result = result && EnumRecovery(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // !(IntegerValue | ',')
  private static boolean EnumReservedRangeRecovery_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumReservedRangeRecovery_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !EnumReservedRangeRecovery_0_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // IntegerValue | ','
  private static boolean EnumReservedRangeRecovery_0_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumReservedRangeRecovery_0_0")) return false;
    boolean result;
    result = IntegerValue(builder, level + 1);
    if (!result) result = consumeToken(builder, COMMA);
    return result;
  }

  /* ********************************************************** */
  // reserved (EnumReservedNames | EnumReservedNumbers) ';'
  public static boolean EnumReservedStatement(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumReservedStatement")) return false;
    if (!nextTokenIs(builder, RESERVED)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ENUM_RESERVED_STATEMENT, null);
    result = consumeToken(builder, RESERVED);
    pinned = result; // pin = 1
    result = result && report_error_(builder, EnumReservedStatement_1(builder, level + 1));
    result = pinned && consumeToken(builder, SEMI) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // EnumReservedNames | EnumReservedNumbers
  private static boolean EnumReservedStatement_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumReservedStatement_1")) return false;
    boolean result;
    result = EnumReservedNames(builder, level + 1);
    if (!result) result = EnumReservedNumbers(builder, level + 1);
    return result;
  }

  /* ********************************************************** */
  // Identifier '=' IntegerValue OptionList? ';'
  public static boolean EnumValue(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumValue")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ENUM_VALUE, "<enum value>");
    result = Identifier(builder, level + 1);
    pinned = result; // pin = 1
    result = result && report_error_(builder, consumeToken(builder, ASSIGN));
    result = pinned && report_error_(builder, IntegerValue(builder, level + 1)) && result;
    result = pinned && report_error_(builder, EnumValue_3(builder, level + 1)) && result;
    result = pinned && consumeToken(builder, SEMI) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // OptionList?
  private static boolean EnumValue_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "EnumValue_3")) return false;
    OptionList(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // <<Block <<BlockBodyOptional ExtendEntry>> >>
  public static boolean ExtendBody(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtendBody")) return false;
    if (!nextTokenIs(builder, LBRACE)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = Block(builder, level + 1, ExtendBody_0_0_parser_);
    exit_section_(builder, marker, EXTEND_BODY, result);
    return result;
  }

  /* ********************************************************** */
  // extend MessageTypeName ExtendBody
  public static boolean ExtendDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtendDefinition")) return false;
    if (!nextTokenIs(builder, EXTEND)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, EXTEND_DEFINITION, null);
    result = consumeToken(builder, EXTEND);
    pinned = result; // pin = 1
    result = result && report_error_(builder, MessageTypeName(builder, level + 1));
    result = pinned && ExtendBody(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // GroupDefinition | SimpleField
  static boolean ExtendEntry(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtendEntry")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = GroupDefinition(builder, level + 1);
    if (!result) result = SimpleField(builder, level + 1);
    exit_section_(builder, level, marker, result, false, PbParser::ExtendRecovery);
    return result;
  }

  /* ********************************************************** */
  // !(FieldLabel | group | TypeName | ';' | '}')
  static boolean ExtendRecovery(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtendRecovery")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !ExtendRecovery_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // FieldLabel | group | TypeName | ';' | '}'
  private static boolean ExtendRecovery_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtendRecovery_0")) return false;
    boolean result;
    result = FieldLabel(builder, level + 1);
    if (!result) result = consumeToken(builder, GROUP);
    if (!result) result = TypeName(builder, level + 1);
    if (!result) result = consumeToken(builder, SEMI);
    if (!result) result = consumeToken(builder, RBRACE);
    return result;
  }

  /* ********************************************************** */
  // QualifiedName
  public static boolean ExtensionName(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtensionName")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, EXTENSION_NAME, "<extension name>");
    result = QualifiedName(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // IntegerValue (to (IntegerValue | max))?
  public static boolean ExtensionRange(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtensionRange")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, EXTENSION_RANGE, "<extension range>");
    result = IntegerValue(builder, level + 1);
    pinned = result; // pin = 1
    result = result && ExtensionRange_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, PbParser::ExtensionRangeRecovery);
    return result || pinned;
  }

  // (to (IntegerValue | max))?
  private static boolean ExtensionRange_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtensionRange_1")) return false;
    ExtensionRange_1_0(builder, level + 1);
    return true;
  }

  // to (IntegerValue | max)
  private static boolean ExtensionRange_1_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtensionRange_1_0")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = consumeToken(builder, TO);
    pinned = result; // pin = 1
    result = result && ExtensionRange_1_0_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // IntegerValue | max
  private static boolean ExtensionRange_1_0_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtensionRange_1_0_1")) return false;
    boolean result;
    result = IntegerValue(builder, level + 1);
    if (!result) result = consumeToken(builder, MAX);
    return result;
  }

  /* ********************************************************** */
  // !(IntegerValue | ',' | '[' | ';') MessageRecovery
  static boolean ExtensionRangeRecovery(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtensionRangeRecovery")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = ExtensionRangeRecovery_0(builder, level + 1);
    result = result && MessageRecovery(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // !(IntegerValue | ',' | '[' | ';')
  private static boolean ExtensionRangeRecovery_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtensionRangeRecovery_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !ExtensionRangeRecovery_0_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // IntegerValue | ',' | '[' | ';'
  private static boolean ExtensionRangeRecovery_0_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtensionRangeRecovery_0_0")) return false;
    boolean result;
    result = IntegerValue(builder, level + 1);
    if (!result) result = consumeToken(builder, COMMA);
    if (!result) result = consumeToken(builder, LBRACK);
    if (!result) result = consumeToken(builder, SEMI);
    return result;
  }

  /* ********************************************************** */
  // extensions <<CommaSeparatedList ('[' | ';') ExtensionRange>> OptionList? ';'
  public static boolean ExtensionsStatement(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtensionsStatement")) return false;
    if (!nextTokenIs(builder, EXTENSIONS)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, EXTENSIONS_STATEMENT, null);
    result = consumeToken(builder, EXTENSIONS);
    pinned = result; // pin = 1
    result = result && report_error_(builder, CommaSeparatedList(builder, level + 1, PbParser::ExtensionsStatement_1_0, PbParser::ExtensionRange));
    result = pinned && report_error_(builder, ExtensionsStatement_2(builder, level + 1)) && result;
    result = pinned && consumeToken(builder, SEMI) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // '[' | ';'
  private static boolean ExtensionsStatement_1_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtensionsStatement_1_0")) return false;
    boolean result;
    result = consumeToken(builder, LBRACK);
    if (!result) result = consumeToken(builder, SEMI);
    return result;
  }

  // OptionList?
  private static boolean ExtensionsStatement_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ExtensionsStatement_2")) return false;
    OptionList(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // optional | required | repeated
  public static boolean FieldLabel(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "FieldLabel")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, FIELD_LABEL, "<field label>");
    result = consumeToken(builder, OPTIONAL);
    if (!result) result = consumeToken(builder, REQUIRED);
    if (!result) result = consumeToken(builder, REPEATED);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // FieldLabel? group Identifier '=' IntegerValue GroupOptionContainer? MessageBody
  public static boolean GroupDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "GroupDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, GROUP_DEFINITION, "<group definition>");
    result = GroupDefinition_0(builder, level + 1);
    result = result && consumeToken(builder, GROUP);
    pinned = result; // pin = 2
    result = result && report_error_(builder, Identifier(builder, level + 1));
    result = pinned && report_error_(builder, consumeToken(builder, ASSIGN)) && result;
    result = pinned && report_error_(builder, IntegerValue(builder, level + 1)) && result;
    result = pinned && report_error_(builder, GroupDefinition_5(builder, level + 1)) && result;
    result = pinned && MessageBody(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // FieldLabel?
  private static boolean GroupDefinition_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "GroupDefinition_0")) return false;
    FieldLabel(builder, level + 1);
    return true;
  }

  // GroupOptionContainer?
  private static boolean GroupDefinition_5(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "GroupDefinition_5")) return false;
    GroupOptionContainer(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // OptionList
  public static boolean GroupOptionContainer(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "GroupOptionContainer")) return false;
    if (!nextTokenIs(builder, LBRACK)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = OptionList(builder, level + 1);
    exit_section_(builder, marker, GROUP_OPTION_CONTAINER, result);
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
  // StringValue
  public static boolean ImportName(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ImportName")) return false;
    if (!nextTokenIs(builder, STRING_LITERAL)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = StringValue(builder, level + 1);
    exit_section_(builder, marker, IMPORT_NAME, result);
    return result;
  }

  /* ********************************************************** */
  // import (public | weak)? ImportName ';'
  public static boolean ImportStatement(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ImportStatement")) return false;
    if (!nextTokenIs(builder, IMPORT)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, IMPORT_STATEMENT, null);
    result = consumeToken(builder, IMPORT);
    pinned = result; // pin = 1
    result = result && report_error_(builder, ImportStatement_1(builder, level + 1));
    result = pinned && report_error_(builder, ImportName(builder, level + 1)) && result;
    result = pinned && consumeToken(builder, SEMI) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // (public | weak)?
  private static boolean ImportStatement_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ImportStatement_1")) return false;
    ImportStatement_1_0(builder, level + 1);
    return true;
  }

  // public | weak
  private static boolean ImportStatement_1_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ImportStatement_1_0")) return false;
    boolean result;
    result = consumeToken(builder, PUBLIC);
    if (!result) result = consumeToken(builder, WEAK);
    return result;
  }

  /* ********************************************************** */
  // '-'? INTEGER_LITERAL
  public static boolean IntegerValue(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "IntegerValue")) return false;
    if (!nextTokenIs(builder, "<integer value>", INTEGER_LITERAL, MINUS)) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, NUMBER_VALUE, "<integer value>");
    result = IntegerValue_0(builder, level + 1);
    result = result && consumeToken(builder, INTEGER_LITERAL);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // '-'?
  private static boolean IntegerValue_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "IntegerValue_0")) return false;
    consumeToken(builder, MINUS);
    return true;
  }

  /* ********************************************************** */
  // FieldLabel? map '<' TypeName ',' TypeName '>' Identifier '=' IntegerValue OptionList? ';'
  public static boolean MapField(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "MapField")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, MAP_FIELD, "<map field>");
    result = MapField_0(builder, level + 1);
    result = result && consumeTokens(builder, 2, MAP, LT);
    pinned = result; // pin = 3
    result = result && report_error_(builder, TypeName(builder, level + 1));
    result = pinned && report_error_(builder, consumeToken(builder, COMMA)) && result;
    result = pinned && report_error_(builder, TypeName(builder, level + 1)) && result;
    result = pinned && report_error_(builder, consumeToken(builder, GT)) && result;
    result = pinned && report_error_(builder, Identifier(builder, level + 1)) && result;
    result = pinned && report_error_(builder, consumeToken(builder, ASSIGN)) && result;
    result = pinned && report_error_(builder, IntegerValue(builder, level + 1)) && result;
    result = pinned && report_error_(builder, MapField_10(builder, level + 1)) && result;
    result = pinned && consumeToken(builder, SEMI) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // FieldLabel?
  private static boolean MapField_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "MapField_0")) return false;
    FieldLabel(builder, level + 1);
    return true;
  }

  // OptionList?
  private static boolean MapField_10(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "MapField_10")) return false;
    OptionList(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // <<Block <<BlockBodyOptional MessageEntry>> >>
  public static boolean MessageBody(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "MessageBody")) return false;
    if (!nextTokenIs(builder, LBRACE)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = Block(builder, level + 1, MessageBody_0_0_parser_);
    exit_section_(builder, marker, MESSAGE_BODY, result);
    return result;
  }

  /* ********************************************************** */
  // message Identifier MessageBody
  public static boolean MessageDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "MessageDefinition")) return false;
    if (!nextTokenIs(builder, MESSAGE)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, MESSAGE_DEFINITION, null);
    result = consumeToken(builder, MESSAGE);
    pinned = result; // pin = 1
    result = result && report_error_(builder, Identifier(builder, level + 1));
    result = pinned && MessageBody(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // MessageDefinition
  //   | EnumDefinition
  //   | ExtensionsStatement
  //   | ReservedStatement
  //   | ExtendDefinition
  //   | OptionStatement
  //   | OneofDefinition
  //   | MapField
  //   | GroupDefinition
  //   | SimpleField
  //   | ';'
  static boolean MessageEntry(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "MessageEntry")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = MessageDefinition(builder, level + 1);
    if (!result) result = EnumDefinition(builder, level + 1);
    if (!result) result = ExtensionsStatement(builder, level + 1);
    if (!result) result = ReservedStatement(builder, level + 1);
    if (!result) result = ExtendDefinition(builder, level + 1);
    if (!result) result = OptionStatement(builder, level + 1);
    if (!result) result = OneofDefinition(builder, level + 1);
    if (!result) result = MapField(builder, level + 1);
    if (!result) result = GroupDefinition(builder, level + 1);
    if (!result) result = SimpleField(builder, level + 1);
    if (!result) result = consumeToken(builder, SEMI);
    exit_section_(builder, level, marker, result, false, PbParser::MessageRecovery);
    return result;
  }

  /* ********************************************************** */
  // !(message | enum | extensions | reserved | extend | option | oneof
  //   | FieldLabel | group | map | TypeName | '}' | ';')
  static boolean MessageRecovery(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "MessageRecovery")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !MessageRecovery_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // message | enum | extensions | reserved | extend | option | oneof
  //   | FieldLabel | group | map | TypeName | '}' | ';'
  private static boolean MessageRecovery_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "MessageRecovery_0")) return false;
    boolean result;
    result = consumeToken(builder, MESSAGE);
    if (!result) result = consumeToken(builder, ENUM);
    if (!result) result = consumeToken(builder, EXTENSIONS);
    if (!result) result = consumeToken(builder, RESERVED);
    if (!result) result = consumeToken(builder, EXTEND);
    if (!result) result = consumeToken(builder, OPTION);
    if (!result) result = consumeToken(builder, ONEOF);
    if (!result) result = FieldLabel(builder, level + 1);
    if (!result) result = consumeToken(builder, GROUP);
    if (!result) result = consumeToken(builder, MAP);
    if (!result) result = TypeName(builder, level + 1);
    if (!result) result = consumeToken(builder, RBRACE);
    if (!result) result = consumeToken(builder, SEMI);
    return result;
  }

  /* ********************************************************** */
  // QualifiedName
  public static boolean MessageTypeName(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "MessageTypeName")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, MESSAGE_TYPE_NAME, "<message type name>");
    result = QualifiedName(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // <<Block <<BlockBodyOptional MethodOptionsEntry>> >>
  public static boolean MethodOptions(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "MethodOptions")) return false;
    if (!nextTokenIs(builder, LBRACE)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = Block(builder, level + 1, MethodOptions_0_0_parser_);
    exit_section_(builder, marker, METHOD_OPTIONS, result);
    return result;
  }

  /* ********************************************************** */
  // OptionStatement | ';'
  static boolean MethodOptionsEntry(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "MethodOptionsEntry")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = OptionStatement(builder, level + 1);
    if (!result) result = consumeToken(builder, SEMI);
    exit_section_(builder, level, marker, result, false, PbParser::MethodOptionsRecovery);
    return result;
  }

  /* ********************************************************** */
  // !(option | ';' | '}')
  static boolean MethodOptionsRecovery(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "MethodOptionsRecovery")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !MethodOptionsRecovery_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // option | ';' | '}'
  private static boolean MethodOptionsRecovery_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "MethodOptionsRecovery_0")) return false;
    boolean result;
    result = consumeToken(builder, OPTION);
    if (!result) result = consumeToken(builder, SEMI);
    if (!result) result = consumeToken(builder, RBRACE);
    return result;
  }

  /* ********************************************************** */
  // '-'? (INTEGER_LITERAL | FLOAT_LITERAL | "inf" | "nan")
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

  // INTEGER_LITERAL | FLOAT_LITERAL | "inf" | "nan"
  private static boolean NumberValue_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "NumberValue_1")) return false;
    boolean result;
    result = consumeToken(builder, INTEGER_LITERAL);
    if (!result) result = consumeToken(builder, FLOAT_LITERAL);
    if (!result) result = consumeToken(builder, "inf");
    if (!result) result = consumeToken(builder, "nan");
    return result;
  }

  /* ********************************************************** */
  // <<Block <<BlockBodyOptional OneofEntry>> >>
  public static boolean OneofBody(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OneofBody")) return false;
    if (!nextTokenIs(builder, LBRACE)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = Block(builder, level + 1, OneofBody_0_0_parser_);
    exit_section_(builder, marker, ONEOF_BODY, result);
    return result;
  }

  /* ********************************************************** */
  // oneof Identifier OneofBody
  public static boolean OneofDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OneofDefinition")) return false;
    if (!nextTokenIs(builder, ONEOF)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ONEOF_DEFINITION, null);
    result = consumeToken(builder, ONEOF);
    pinned = result; // pin = 1
    result = result && report_error_(builder, Identifier(builder, level + 1));
    result = pinned && OneofBody(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // OptionStatement | GroupDefinition | SimpleField
  static boolean OneofEntry(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OneofEntry")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = OptionStatement(builder, level + 1);
    if (!result) result = GroupDefinition(builder, level + 1);
    if (!result) result = SimpleField(builder, level + 1);
    exit_section_(builder, level, marker, result, false, PbParser::OneofRecovery);
    return result;
  }

  /* ********************************************************** */
  // !(FieldLabel | option | group | TypeName | ';' | '}')
  static boolean OneofRecovery(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OneofRecovery")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !OneofRecovery_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // FieldLabel | option | group | TypeName | ';' | '}'
  private static boolean OneofRecovery_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OneofRecovery_0")) return false;
    boolean result;
    result = FieldLabel(builder, level + 1);
    if (!result) result = consumeToken(builder, OPTION);
    if (!result) result = consumeToken(builder, GROUP);
    if (!result) result = TypeName(builder, level + 1);
    if (!result) result = consumeToken(builder, SEMI);
    if (!result) result = consumeToken(builder, RBRACE);
    return result;
  }

  /* ********************************************************** */
  // OptionName '=' (IdentifierValue | NumberValue | StringValue | AggregateValue)
  public static boolean OptionExpression(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OptionExpression")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, OPTION_EXPRESSION, "<option expression>");
    result = OptionName(builder, level + 1, -1);
    pinned = result; // pin = 1
    result = result && report_error_(builder, consumeToken(builder, ASSIGN));
    result = pinned && OptionExpression_2(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // IdentifierValue | NumberValue | StringValue | AggregateValue
  private static boolean OptionExpression_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OptionExpression_2")) return false;
    boolean result;
    result = IdentifierValue(builder, level + 1);
    if (!result) result = NumberValue(builder, level + 1);
    if (!result) result = StringValue(builder, level + 1);
    if (!result) result = AggregateValue(builder, level + 1);
    return result;
  }

  /* ********************************************************** */
  // '(' ExtensionName ')'
  static boolean OptionExtensionName(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OptionExtensionName")) return false;
    if (!nextTokenIs(builder, LPAREN)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = consumeToken(builder, LPAREN);
    result = result && ExtensionName(builder, level + 1);
    pinned = result; // pin = 2
    result = result && consumeToken(builder, RPAREN);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // '[' <<CommaSeparatedList ']' OptionListEntry>> ']'
  public static boolean OptionList(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OptionList")) return false;
    if (!nextTokenIs(builder, LBRACK)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, OPTION_LIST, null);
    result = consumeToken(builder, LBRACK);
    pinned = result; // pin = 1
    result = result && report_error_(builder, CommaSeparatedList(builder, level + 1, RBRACK_parser_, PbParser::OptionListEntry));
    result = pinned && consumeToken(builder, RBRACK) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // OptionExpression
  static boolean OptionListEntry(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OptionListEntry")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = OptionExpression(builder, level + 1);
    exit_section_(builder, level, marker, result, false, PbParser::OptionListEntryRecovery);
    return result;
  }

  /* ********************************************************** */
  // !(OptionName | ',' | ']') MessageRecovery
  static boolean OptionListEntryRecovery(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OptionListEntryRecovery")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = OptionListEntryRecovery_0(builder, level + 1);
    result = result && MessageRecovery(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // !(OptionName | ',' | ']')
  private static boolean OptionListEntryRecovery_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OptionListEntryRecovery_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !OptionListEntryRecovery_0_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // OptionName | ',' | ']'
  private static boolean OptionListEntryRecovery_0_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OptionListEntryRecovery_0_0")) return false;
    boolean result;
    result = OptionName(builder, level + 1, -1);
    if (!result) result = consumeToken(builder, COMMA);
    if (!result) result = consumeToken(builder, RBRACK);
    return result;
  }

  /* ********************************************************** */
  // OptionExtensionName | Identifier
  static boolean OptionNameAtom(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OptionNameAtom")) return false;
    boolean result;
    result = OptionExtensionName(builder, level + 1);
    if (!result) result = Identifier(builder, level + 1);
    return result;
  }

  /* ********************************************************** */
  // option OptionExpression ';'
  public static boolean OptionStatement(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OptionStatement")) return false;
    if (!nextTokenIs(builder, OPTION)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, OPTION_STATEMENT, null);
    result = consumeToken(builder, OPTION);
    pinned = result; // pin = 1
    result = result && report_error_(builder, OptionExpression(builder, level + 1));
    result = pinned && consumeToken(builder, SEMI) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // Identifier
  static boolean PackageNameAtom(PsiBuilder builder, int level) {
    return Identifier(builder, level + 1);
  }

  /* ********************************************************** */
  // package PackageName ';'
  public static boolean PackageStatement(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "PackageStatement")) return false;
    if (!nextTokenIs(builder, PACKAGE)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, PACKAGE_STATEMENT, null);
    result = consumeToken(builder, PACKAGE);
    pinned = result; // pin = 1
    result = result && report_error_(builder, PackageName(builder, level + 1, -1));
    result = pinned && consumeToken(builder, SEMI) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // '.'? SymbolPath
  static boolean QualifiedName(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "QualifiedName")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = QualifiedName_0(builder, level + 1);
    result = result && SymbolPath(builder, level + 1, -1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // '.'?
  private static boolean QualifiedName_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "QualifiedName_0")) return false;
    consumeToken(builder, DOT);
    return true;
  }

  /* ********************************************************** */
  // &StringValue <<CommaSeparatedList ';' StringValue>>
  static boolean ReservedNames(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ReservedNames")) return false;
    if (!nextTokenIs(builder, STRING_LITERAL)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = ReservedNames_0(builder, level + 1);
    result = result && CommaSeparatedList(builder, level + 1, SEMI_parser_, PbParser::StringValue);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // &StringValue
  private static boolean ReservedNames_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ReservedNames_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _AND_);
    result = StringValue(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // &IntegerValue <<CommaSeparatedList ';' ReservedRange>>
  static boolean ReservedNumbers(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ReservedNumbers")) return false;
    if (!nextTokenIs(builder, "", INTEGER_LITERAL, MINUS)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = ReservedNumbers_0(builder, level + 1);
    result = result && CommaSeparatedList(builder, level + 1, SEMI_parser_, PbParser::ReservedRange);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // &IntegerValue
  private static boolean ReservedNumbers_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ReservedNumbers_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _AND_);
    result = IntegerValue(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // IntegerValue (to (IntegerValue | max))?
  public static boolean ReservedRange(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ReservedRange")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, RESERVED_RANGE, "<reserved range>");
    result = IntegerValue(builder, level + 1);
    pinned = result; // pin = 1
    result = result && ReservedRange_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, PbParser::ReservedRangeRecovery);
    return result || pinned;
  }

  // (to (IntegerValue | max))?
  private static boolean ReservedRange_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ReservedRange_1")) return false;
    ReservedRange_1_0(builder, level + 1);
    return true;
  }

  // to (IntegerValue | max)
  private static boolean ReservedRange_1_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ReservedRange_1_0")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = consumeToken(builder, TO);
    pinned = result; // pin = 1
    result = result && ReservedRange_1_0_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // IntegerValue | max
  private static boolean ReservedRange_1_0_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ReservedRange_1_0_1")) return false;
    boolean result;
    result = IntegerValue(builder, level + 1);
    if (!result) result = consumeToken(builder, MAX);
    return result;
  }

  /* ********************************************************** */
  // !(IntegerValue | ',') MessageRecovery
  static boolean ReservedRangeRecovery(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ReservedRangeRecovery")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = ReservedRangeRecovery_0(builder, level + 1);
    result = result && MessageRecovery(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // !(IntegerValue | ',')
  private static boolean ReservedRangeRecovery_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ReservedRangeRecovery_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !ReservedRangeRecovery_0_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // IntegerValue | ','
  private static boolean ReservedRangeRecovery_0_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ReservedRangeRecovery_0_0")) return false;
    boolean result;
    result = IntegerValue(builder, level + 1);
    if (!result) result = consumeToken(builder, COMMA);
    return result;
  }

  /* ********************************************************** */
  // reserved (ReservedNames | ReservedNumbers) ';'
  public static boolean ReservedStatement(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ReservedStatement")) return false;
    if (!nextTokenIs(builder, RESERVED)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, RESERVED_STATEMENT, null);
    result = consumeToken(builder, RESERVED);
    pinned = result; // pin = 1
    result = result && report_error_(builder, ReservedStatement_1(builder, level + 1));
    result = pinned && consumeToken(builder, SEMI) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // ReservedNames | ReservedNumbers
  private static boolean ReservedStatement_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ReservedStatement_1")) return false;
    boolean result;
    result = ReservedNames(builder, level + 1);
    if (!result) result = ReservedNumbers(builder, level + 1);
    return result;
  }

  /* ********************************************************** */
  // SyntaxStatement? RootEntry*
  static boolean Root(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Root")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = Root_0(builder, level + 1);
    result = result && Root_1(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // SyntaxStatement?
  private static boolean Root_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Root_0")) return false;
    SyntaxStatement(builder, level + 1);
    return true;
  }

  // RootEntry*
  private static boolean Root_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "Root_1")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!RootEntry(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "Root_1", pos)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // !<<eof>> TopLevelEntry
  static boolean RootEntry(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "RootEntry")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = RootEntry_0(builder, level + 1);
    pinned = result; // pin = 1
    result = result && TopLevelEntry(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // !<<eof>>
  private static boolean RootEntry_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "RootEntry_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !eof(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // <<Block <<BlockBodyOptional ServiceEntry>> >>
  public static boolean ServiceBody(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ServiceBody")) return false;
    if (!nextTokenIs(builder, LBRACE)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = Block(builder, level + 1, ServiceBody_0_0_parser_);
    exit_section_(builder, marker, SERVICE_BODY, result);
    return result;
  }

  /* ********************************************************** */
  // service Identifier ServiceBody
  public static boolean ServiceDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ServiceDefinition")) return false;
    if (!nextTokenIs(builder, SERVICE)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, SERVICE_DEFINITION, null);
    result = consumeToken(builder, SERVICE);
    pinned = result; // pin = 1
    result = result && report_error_(builder, Identifier(builder, level + 1));
    result = pinned && ServiceBody(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // OptionStatement | ServiceStream | ServiceMethod | ';'
  static boolean ServiceEntry(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ServiceEntry")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = OptionStatement(builder, level + 1);
    if (!result) result = ServiceStream(builder, level + 1);
    if (!result) result = ServiceMethod(builder, level + 1);
    if (!result) result = consumeToken(builder, SEMI);
    exit_section_(builder, level, marker, result, false, PbParser::ServiceRecovery);
    return result;
  }

  /* ********************************************************** */
  // rpc Identifier '(' ServiceMethodType ')' returns '(' ServiceMethodType ')' (MethodOptions | ';')
  public static boolean ServiceMethod(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ServiceMethod")) return false;
    if (!nextTokenIs(builder, RPC)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, SERVICE_METHOD, null);
    result = consumeToken(builder, RPC);
    pinned = result; // pin = 1
    result = result && report_error_(builder, Identifier(builder, level + 1));
    result = pinned && report_error_(builder, consumeToken(builder, LPAREN)) && result;
    result = pinned && report_error_(builder, ServiceMethodType(builder, level + 1)) && result;
    result = pinned && report_error_(builder, consumeTokens(builder, -1, RPAREN, RETURNS, LPAREN)) && result;
    result = pinned && report_error_(builder, ServiceMethodType(builder, level + 1)) && result;
    result = pinned && report_error_(builder, consumeToken(builder, RPAREN)) && result;
    result = pinned && ServiceMethod_9(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // MethodOptions | ';'
  private static boolean ServiceMethod_9(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ServiceMethod_9")) return false;
    boolean result;
    result = MethodOptions(builder, level + 1);
    if (!result) result = consumeToken(builder, SEMI);
    return result;
  }

  /* ********************************************************** */
  // stream? MessageTypeName
  public static boolean ServiceMethodType(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ServiceMethodType")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, SERVICE_METHOD_TYPE, "<service method type>");
    result = ServiceMethodType_0(builder, level + 1);
    result = result && MessageTypeName(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // stream?
  private static boolean ServiceMethodType_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ServiceMethodType_0")) return false;
    consumeToken(builder, STREAM);
    return true;
  }

  /* ********************************************************** */
  // !(option | stream | rpc | ';' | '}')
  static boolean ServiceRecovery(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ServiceRecovery")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !ServiceRecovery_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // option | stream | rpc | ';' | '}'
  private static boolean ServiceRecovery_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ServiceRecovery_0")) return false;
    boolean result;
    result = consumeToken(builder, OPTION);
    if (!result) result = consumeToken(builder, STREAM);
    if (!result) result = consumeToken(builder, RPC);
    if (!result) result = consumeToken(builder, SEMI);
    if (!result) result = consumeToken(builder, RBRACE);
    return result;
  }

  /* ********************************************************** */
  // stream Identifier '(' MessageTypeName ',' MessageTypeName ')' (MethodOptions | ';')
  public static boolean ServiceStream(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ServiceStream")) return false;
    if (!nextTokenIs(builder, STREAM)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, SERVICE_STREAM, null);
    result = consumeToken(builder, STREAM);
    pinned = result; // pin = 1
    result = result && report_error_(builder, Identifier(builder, level + 1));
    result = pinned && report_error_(builder, consumeToken(builder, LPAREN)) && result;
    result = pinned && report_error_(builder, MessageTypeName(builder, level + 1)) && result;
    result = pinned && report_error_(builder, consumeToken(builder, COMMA)) && result;
    result = pinned && report_error_(builder, MessageTypeName(builder, level + 1)) && result;
    result = pinned && report_error_(builder, consumeToken(builder, RPAREN)) && result;
    result = pinned && ServiceStream_7(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // MethodOptions | ';'
  private static boolean ServiceStream_7(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ServiceStream_7")) return false;
    boolean result;
    result = MethodOptions(builder, level + 1);
    if (!result) result = consumeToken(builder, SEMI);
    return result;
  }

  /* ********************************************************** */
  // FieldLabel? TypeName Identifier '=' IntegerValue OptionList? ';'
  public static boolean SimpleField(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "SimpleField")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, SIMPLE_FIELD, "<simple field>");
    result = SimpleField_0(builder, level + 1);
    result = result && TypeName(builder, level + 1);
    pinned = result; // pin = 2
    result = result && report_error_(builder, Identifier(builder, level + 1));
    result = pinned && report_error_(builder, consumeToken(builder, ASSIGN)) && result;
    result = pinned && report_error_(builder, IntegerValue(builder, level + 1)) && result;
    result = pinned && report_error_(builder, SimpleField_5(builder, level + 1)) && result;
    result = pinned && consumeToken(builder, SEMI) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // FieldLabel?
  private static boolean SimpleField_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "SimpleField_0")) return false;
    FieldLabel(builder, level + 1);
    return true;
  }

  // OptionList?
  private static boolean SimpleField_5(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "SimpleField_5")) return false;
    OptionList(builder, level + 1);
    return true;
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
  // syntax '=' StringValue ';'
  public static boolean SyntaxStatement(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "SyntaxStatement")) return false;
    if (!nextTokenIs(builder, SYNTAX)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, SYNTAX_STATEMENT, null);
    result = consumeTokens(builder, 1, SYNTAX, ASSIGN);
    pinned = result; // pin = 1
    result = result && report_error_(builder, StringValue(builder, level + 1));
    result = pinned && consumeToken(builder, SEMI) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // MessageDefinition
  //   | EnumDefinition
  //   | ServiceDefinition
  //   | ExtendDefinition
  //   | ImportStatement
  //   | PackageStatement
  //   | OptionStatement
  //   | ';'
  static boolean TopLevelEntry(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "TopLevelEntry")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = MessageDefinition(builder, level + 1);
    if (!result) result = EnumDefinition(builder, level + 1);
    if (!result) result = ServiceDefinition(builder, level + 1);
    if (!result) result = ExtendDefinition(builder, level + 1);
    if (!result) result = ImportStatement(builder, level + 1);
    if (!result) result = PackageStatement(builder, level + 1);
    if (!result) result = OptionStatement(builder, level + 1);
    if (!result) result = consumeToken(builder, SEMI);
    exit_section_(builder, level, marker, result, false, PbParser::TopLevelRecovery);
    return result;
  }

  /* ********************************************************** */
  // !(message | enum | service | extend | import | package | option | ';')
  static boolean TopLevelRecovery(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "TopLevelRecovery")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !TopLevelRecovery_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // message | enum | service | extend | import | package | option | ';'
  private static boolean TopLevelRecovery_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "TopLevelRecovery_0")) return false;
    boolean result;
    result = consumeToken(builder, MESSAGE);
    if (!result) result = consumeToken(builder, ENUM);
    if (!result) result = consumeToken(builder, SERVICE);
    if (!result) result = consumeToken(builder, EXTEND);
    if (!result) result = consumeToken(builder, IMPORT);
    if (!result) result = consumeToken(builder, PACKAGE);
    if (!result) result = consumeToken(builder, OPTION);
    if (!result) result = consumeToken(builder, SEMI);
    return result;
  }

  /* ********************************************************** */
  // QualifiedName
  public static boolean TypeName(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "TypeName")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, TYPE_NAME, "<type name>");
    result = QualifiedName(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // Expression root: OptionName
  // Operator priority table:
  // 0: POSTFIX(OptionNameTuple)
  // 1: ATOM(OptionNameUnit)
  public static boolean OptionName(PsiBuilder builder, int level, int priority) {
    if (!recursion_guard_(builder, level, "OptionName")) return false;
    addVariant(builder, "<option name>");
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, "<option name>");
    result = OptionNameUnit(builder, level + 1);
    pinned = result;
    result = result && OptionName_0(builder, level + 1, priority);
    exit_section_(builder, level, marker, null, result, pinned, null);
    return result || pinned;
  }

  public static boolean OptionName_0(PsiBuilder builder, int level, int priority) {
    if (!recursion_guard_(builder, level, "OptionName_0")) return false;
    boolean result = true;
    while (true) {
      Marker marker = enter_section_(builder, level, _LEFT_, null);
      if (priority < 0 && OptionNameTuple_0(builder, level + 1)) {
        result = true;
        exit_section_(builder, level, marker, OPTION_NAME, result, true, null);
      }
      else {
        exit_section_(builder, level, marker, null, false, false, null);
        break;
      }
    }
    return result;
  }

  // '.' OptionNameAtom
  private static boolean OptionNameTuple_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OptionNameTuple_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeTokenSmart(builder, DOT);
    result = result && OptionNameAtom(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // OptionNameAtom
  public static boolean OptionNameUnit(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "OptionNameUnit")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _COLLAPSE_, OPTION_NAME, "<option name unit>");
    result = OptionNameAtom(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // Expression root: PackageName
  // Operator priority table:
  // 0: POSTFIX(PackageNameTuple)
  // 1: ATOM(PackageNameUnit)
  public static boolean PackageName(PsiBuilder builder, int level, int priority) {
    if (!recursion_guard_(builder, level, "PackageName")) return false;
    addVariant(builder, "<package name>");
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, "<package name>");
    result = PackageNameUnit(builder, level + 1);
    pinned = result;
    result = result && PackageName_0(builder, level + 1, priority);
    exit_section_(builder, level, marker, null, result, pinned, null);
    return result || pinned;
  }

  public static boolean PackageName_0(PsiBuilder builder, int level, int priority) {
    if (!recursion_guard_(builder, level, "PackageName_0")) return false;
    boolean result = true;
    while (true) {
      Marker marker = enter_section_(builder, level, _LEFT_, null);
      if (priority < 0 && PackageNameTuple_0(builder, level + 1)) {
        result = true;
        exit_section_(builder, level, marker, PACKAGE_NAME, result, true, null);
      }
      else {
        exit_section_(builder, level, marker, null, false, false, null);
        break;
      }
    }
    return result;
  }

  // '.' PackageNameAtom
  private static boolean PackageNameTuple_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "PackageNameTuple_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeTokenSmart(builder, DOT);
    result = result && PackageNameAtom(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // PackageNameAtom
  public static boolean PackageNameUnit(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "PackageNameUnit")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _COLLAPSE_, PACKAGE_NAME, "<package name unit>");
    result = PackageNameAtom(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
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

  static final Parser RBRACK_parser_ = (builder, level) -> consumeToken(builder, RBRACK);
  static final Parser SEMI_parser_ = (builder, level) -> consumeToken(builder, SEMI);

  private static final Parser AggregateValue_0_0_parser_ = BlockBodyOptional_$(PbParser::AggregateValueEntry);
  private static final Parser EnumBody_0_0_parser_ = BlockBodyOptional_$(PbParser::EnumEntry);
  private static final Parser ExtendBody_0_0_parser_ = BlockBodyOptional_$(PbParser::ExtendEntry);
  private static final Parser MessageBody_0_0_parser_ = BlockBodyOptional_$(PbParser::MessageEntry);
  private static final Parser MethodOptions_0_0_parser_ = BlockBodyOptional_$(PbParser::MethodOptionsEntry);
  private static final Parser OneofBody_0_0_parser_ = BlockBodyOptional_$(PbParser::OneofEntry);
  private static final Parser ServiceBody_0_0_parser_ = BlockBodyOptional_$(PbParser::ServiceEntry);
}
