// This is a generated file. Not intended for manual editing.
package org.intellij.prisma.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.intellij.prisma.lang.psi.PrismaElementTypes.*;
import static org.intellij.prisma.lang.parser.PrismaParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class PrismaParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return Schema(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(ARGUMENT, NAMED_ARGUMENT, VALUE_ARGUMENT),
    create_token_set_(ARRAY_EXPRESSION, EXPRESSION, FUNCTION_CALL, LITERAL_EXPRESSION,
      PATH_EXPRESSION),
    create_token_set_(FIELD_TYPE, LEGACY_LIST_TYPE, LEGACY_REQUIRED_TYPE, LIST_TYPE,
      OPTIONAL_TYPE, SINGLE_TYPE, UNSUPPORTED_OPTIONAL_LIST_TYPE),
  };

  /* ********************************************************** */
  // NamedArgument | ValueArgument
  public static boolean Argument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Argument")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, ARGUMENT, "<argument>");
    r = NamedArgument(b, l + 1);
    if (!r) r = ValueArgument(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '(' [Argument (',' Argument)*] ','? ')'
  public static boolean ArgumentsList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArgumentsList")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && ArgumentsList_1(b, l + 1);
    r = r && ArgumentsList_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, ARGUMENTS_LIST, r);
    return r;
  }

  // [Argument (',' Argument)*]
  private static boolean ArgumentsList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArgumentsList_1")) return false;
    ArgumentsList_1_0(b, l + 1);
    return true;
  }

  // Argument (',' Argument)*
  private static boolean ArgumentsList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArgumentsList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Argument(b, l + 1);
    r = r && ArgumentsList_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' Argument)*
  private static boolean ArgumentsList_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArgumentsList_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ArgumentsList_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ArgumentsList_1_0_1", c)) break;
    }
    return true;
  }

  // ',' Argument
  private static boolean ArgumentsList_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArgumentsList_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && Argument(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean ArgumentsList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArgumentsList_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // '[' [Expression (',' Expression)* ','?] ']'
  public static boolean ArrayExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayExpression")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ARRAY_EXPRESSION, null);
    r = consumeToken(b, LBRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, ArrayExpression_1(b, l + 1));
    r = p && consumeToken(b, RBRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [Expression (',' Expression)* ','?]
  private static boolean ArrayExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayExpression_1")) return false;
    ArrayExpression_1_0(b, l + 1);
    return true;
  }

  // Expression (',' Expression)* ','?
  private static boolean ArrayExpression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayExpression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Expression(b, l + 1);
    r = r && ArrayExpression_1_0_1(b, l + 1);
    r = r && ArrayExpression_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' Expression)*
  private static boolean ArrayExpression_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayExpression_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ArrayExpression_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ArrayExpression_1_0_1", c)) break;
    }
    return true;
  }

  // ',' Expression
  private static boolean ArrayExpression_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayExpression_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && Expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean ArrayExpression_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayExpression_1_0_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // '@@' !<<isWhiteSpace>> PathExpression ArgumentsList?
  public static boolean BlockAttribute(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockAttribute")) return false;
    if (!nextTokenIs(b, ATAT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, BLOCK_ATTRIBUTE, null);
    r = consumeToken(b, ATAT);
    p = r; // pin = 1
    r = r && report_error_(b, BlockAttribute_1(b, l + 1));
    r = p && report_error_(b, PathExpression(b, l + 1)) && r;
    r = p && BlockAttribute_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // !<<isWhiteSpace>>
  private static boolean BlockAttribute_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockAttribute_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !isWhiteSpace(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ArgumentsList?
  private static boolean BlockAttribute_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockAttribute_3")) return false;
    ArgumentsList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // DATASOURCE Identifier KeyValueBlock
  public static boolean DatasourceDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DatasourceDeclaration")) return false;
    if (!nextTokenIs(b, DATASOURCE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DATASOURCE_DECLARATION, null);
    r = consumeToken(b, DATASOURCE);
    p = r; // pin = 1
    r = r && report_error_(b, Identifier(b, l + 1));
    r = p && KeyValueBlock(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ModelDeclaration
  //     | EnumDeclaration
  //     | DatasourceDeclaration
  //     | GeneratorDeclaration
  //     | TypeAlias
  //     | TypeDeclaration
  static boolean Declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Declaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = ModelDeclaration(b, l + 1);
    if (!r) r = EnumDeclaration(b, l + 1);
    if (!r) r = DatasourceDeclaration(b, l + 1);
    if (!r) r = GeneratorDeclaration(b, l + 1);
    if (!r) r = TypeAlias(b, l + 1);
    if (!r) r = TypeDeclaration(b, l + 1);
    exit_section_(b, l, m, r, false, PrismaParser::Declaration_recover);
    return r;
  }

  /* ********************************************************** */
  // !TopLevelKeywords
  static boolean Declaration_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Declaration_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !TopLevelKeywords(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ENUM Identifier EnumDeclarationBlock
  public static boolean EnumDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumDeclaration")) return false;
    if (!nextTokenIs(b, ENUM)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENUM_DECLARATION, null);
    r = consumeToken(b, ENUM);
    p = r; // pin = 1
    r = r && report_error_(b, Identifier(b, l + 1));
    r = p && EnumDeclarationBlock(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '{' (EnumValueDeclaration | BlockAttribute)* '}'
  public static boolean EnumDeclarationBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumDeclarationBlock")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENUM_DECLARATION_BLOCK, null);
    r = consumeToken(b, LBRACE);
    p = r; // pin = 1
    r = r && report_error_(b, EnumDeclarationBlock_1(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (EnumValueDeclaration | BlockAttribute)*
  private static boolean EnumDeclarationBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumDeclarationBlock_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!EnumDeclarationBlock_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "EnumDeclarationBlock_1", c)) break;
    }
    return true;
  }

  // EnumValueDeclaration | BlockAttribute
  private static boolean EnumDeclarationBlock_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumDeclarationBlock_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = EnumValueDeclaration(b, l + 1);
    if (!r) r = BlockAttribute(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Identifier FieldAttribute*
  public static boolean EnumValueDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumValueDeclaration")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENUM_VALUE_DECLARATION, "<enum value declaration>");
    r = Identifier(b, l + 1);
    p = r; // pin = 1
    r = r && EnumValueDeclaration_1(b, l + 1);
    exit_section_(b, l, m, r, p, PrismaParser::UntilNewLine_recover);
    return r || p;
  }

  // FieldAttribute*
  private static boolean EnumValueDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumValueDeclaration_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FieldAttribute(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "EnumValueDeclaration_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // FunctionCall
  //     | ArrayExpression
  //     | LiteralExpression
  //     | PathExpression
  public static boolean Expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, EXPRESSION, "<expression>");
    r = FunctionCall(b, l + 1);
    if (!r) r = ArrayExpression(b, l + 1);
    if (!r) r = LiteralExpression(b, l + 1);
    if (!r) r = PathExpression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '@' !<<isWhiteSpace>> PathExpression ArgumentsList?
  public static boolean FieldAttribute(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldAttribute")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD_ATTRIBUTE, null);
    r = consumeToken(b, AT);
    p = r; // pin = 1
    r = r && report_error_(b, FieldAttribute_1(b, l + 1));
    r = p && report_error_(b, PathExpression(b, l + 1)) && r;
    r = p && FieldAttribute_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // !<<isWhiteSpace>>
  private static boolean FieldAttribute_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldAttribute_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !isWhiteSpace(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ArgumentsList?
  private static boolean FieldAttribute_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldAttribute_3")) return false;
    ArgumentsList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER ':'? FieldType? FieldAttribute*
  public static boolean FieldDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDeclaration")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD_DECLARATION, "<field declaration>");
    r = consumeToken(b, IDENTIFIER);
    p = r; // pin = 1
    r = r && report_error_(b, FieldDeclaration_1(b, l + 1));
    r = p && report_error_(b, FieldDeclaration_2(b, l + 1)) && r;
    r = p && FieldDeclaration_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PrismaParser::UntilNewLine_recover);
    return r || p;
  }

  // ':'?
  private static boolean FieldDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDeclaration_1")) return false;
    consumeToken(b, COLON);
    return true;
  }

  // FieldType?
  private static boolean FieldDeclaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDeclaration_2")) return false;
    FieldType(b, l + 1);
    return true;
  }

  // FieldAttribute*
  private static boolean FieldDeclaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDeclaration_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FieldAttribute(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "FieldDeclaration_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '{' FieldDeclarationBlockItem* '}'
  public static boolean FieldDeclarationBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDeclarationBlock")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD_DECLARATION_BLOCK, null);
    r = consumeToken(b, LBRACE);
    p = r; // pin = 1
    r = r && report_error_(b, FieldDeclarationBlock_1(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // FieldDeclarationBlockItem*
  private static boolean FieldDeclarationBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDeclarationBlock_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FieldDeclarationBlockItem(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "FieldDeclarationBlock_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // FieldDeclaration
  //     | BlockAttribute
  //     | <<consumeWithError '@' "parser.unexpected.field.attr">>
  static boolean FieldDeclarationBlockItem(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDeclarationBlockItem")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = FieldDeclaration(b, l + 1);
    if (!r) r = BlockAttribute(b, l + 1);
    if (!r) r = consumeWithError(b, l + 1, AT_parser_, "parser.unexpected.field.attr");
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // UnsupportedOptionalListType
  //     | ListType
  //     | OptionalType
  //     | LegacyRequiredType
  //     | LegacyListType
  //     | SingleType
  public static boolean FieldType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, FIELD_TYPE, "<field type>");
    r = UnsupportedOptionalListType(b, l + 1);
    if (!r) r = ListType(b, l + 1);
    if (!r) r = OptionalType(b, l + 1);
    if (!r) r = LegacyRequiredType(b, l + 1);
    if (!r) r = LegacyListType(b, l + 1);
    if (!r) r = SingleType(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PathExpression ArgumentsList
  public static boolean FunctionCall(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionCall")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_CALL, "<function call>");
    r = PathExpression(b, l + 1);
    r = r && ArgumentsList(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // GENERATOR Identifier KeyValueBlock
  public static boolean GeneratorDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GeneratorDeclaration")) return false;
    if (!nextTokenIs(b, GENERATOR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, GENERATOR_DECLARATION, null);
    r = consumeToken(b, GENERATOR);
    p = r; // pin = 1
    r = r && report_error_(b, Identifier(b, l + 1));
    r = p && KeyValueBlock(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // IDENTIFIER
  static boolean Identifier(PsiBuilder b, int l) {
    return consumeToken(b, IDENTIFIER);
  }

  /* ********************************************************** */
  // Identifier '=' Expression
  public static boolean KeyValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "KeyValue")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEY_VALUE, "<key value>");
    r = Identifier(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, EQ));
    r = p && Expression(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PrismaParser::UntilNewLine_recover);
    return r || p;
  }

  /* ********************************************************** */
  // '{' KeyValue* '}'
  public static boolean KeyValueBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "KeyValueBlock")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEY_VALUE_BLOCK, null);
    r = consumeToken(b, LBRACE);
    p = r; // pin = 1
    r = r && report_error_(b, KeyValueBlock_1(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // KeyValue*
  private static boolean KeyValueBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "KeyValueBlock_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!KeyValue(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "KeyValueBlock_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '[' TypeReference ']'
  public static boolean LegacyListType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LegacyListType")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && TypeReference(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, LEGACY_LIST_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // TypeReference '!'
  public static boolean LegacyRequiredType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LegacyRequiredType")) return false;
    if (!nextTokenIs(b, "<legacy required type>", IDENTIFIER, UNSUPPORTED)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LEGACY_REQUIRED_TYPE, "<legacy required type>");
    r = TypeReference(b, l + 1);
    r = r && consumeToken(b, EXCL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // TypeReference '[' ']'
  public static boolean ListType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListType")) return false;
    if (!nextTokenIs(b, "<list type>", IDENTIFIER, UNSUPPORTED)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LIST_TYPE, "<list type>");
    r = TypeReference(b, l + 1);
    r = r && consumeTokens(b, 0, LBRACKET, RBRACKET);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // NUMERIC_LITERAL | STRING_LITERAL
  public static boolean LiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LiteralExpression")) return false;
    if (!nextTokenIs(b, "<literal expression>", NUMERIC_LITERAL, STRING_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL_EXPRESSION, "<literal expression>");
    r = consumeToken(b, NUMERIC_LITERAL);
    if (!r) r = consumeToken(b, STRING_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // MODEL Identifier FieldDeclarationBlock
  public static boolean ModelDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ModelDeclaration")) return false;
    if (!nextTokenIs(b, MODEL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MODEL_DECLARATION, null);
    r = consumeToken(b, MODEL);
    p = r; // pin = 1
    r = r && report_error_(b, Identifier(b, l + 1));
    r = p && FieldDeclarationBlock(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // Identifier ':' Expression
  public static boolean NamedArgument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamedArgument")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, NAMED_ARGUMENT, null);
    r = Identifier(b, l + 1);
    r = r && consumeToken(b, COLON);
    p = r; // pin = 2
    r = r && Expression(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // TypeReference '?'
  public static boolean OptionalType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OptionalType")) return false;
    if (!nextTokenIs(b, "<optional type>", IDENTIFIER, UNSUPPORTED)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OPTIONAL_TYPE, "<optional type>");
    r = TypeReference(b, l + 1);
    r = r && consumeToken(b, QUEST);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // Identifier
  public static boolean Path(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Path")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Identifier(b, l + 1);
    exit_section_(b, m, PATH_EXPRESSION, r);
    return r;
  }

  /* ********************************************************** */
  // !<<isNewLine>> Path PathMemberAccess*
  public static boolean PathExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PathExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, PATH_EXPRESSION, "<path expression>");
    r = PathExpression_0(b, l + 1);
    r = r && Path(b, l + 1);
    r = r && PathExpression_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // !<<isNewLine>>
  private static boolean PathExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PathExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !isNewLine(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // PathMemberAccess*
  private static boolean PathExpression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PathExpression_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!PathMemberAccess(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "PathExpression_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '.' Identifier?
  public static boolean PathMemberAccess(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PathMemberAccess")) return false;
    if (!nextTokenIs(b, DOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, PATH_EXPRESSION, null);
    r = consumeToken(b, DOT);
    r = r && PathMemberAccess_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // Identifier?
  private static boolean PathMemberAccess_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PathMemberAccess_1")) return false;
    Identifier(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // Declaration*
  static boolean Schema(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Schema")) return false;
    while (true) {
      int c = current_position_(b);
      if (!Declaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "Schema", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // TypeReference
  public static boolean SingleType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SingleType")) return false;
    if (!nextTokenIs(b, "<single type>", IDENTIFIER, UNSUPPORTED)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SINGLE_TYPE, "<single type>");
    r = TypeReference(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // MODEL | TYPE | ENUM | GENERATOR | DATASOURCE
  static boolean TopLevelKeywords(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TopLevelKeywords")) return false;
    boolean r;
    r = consumeToken(b, MODEL);
    if (!r) r = consumeToken(b, TYPE);
    if (!r) r = consumeToken(b, ENUM);
    if (!r) r = consumeToken(b, GENERATOR);
    if (!r) r = consumeToken(b, DATASOURCE);
    return r;
  }

  /* ********************************************************** */
  // TYPE Identifier '=' FieldType FieldAttribute*
  public static boolean TypeAlias(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeAlias")) return false;
    if (!nextTokenIs(b, TYPE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TYPE_ALIAS, null);
    r = consumeToken(b, TYPE);
    r = r && Identifier(b, l + 1);
    r = r && consumeToken(b, EQ);
    p = r; // pin = 3
    r = r && report_error_(b, FieldType(b, l + 1));
    r = p && TypeAlias_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // FieldAttribute*
  private static boolean TypeAlias_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeAlias_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FieldAttribute(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TypeAlias_4", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // TYPE Identifier FieldDeclarationBlock
  public static boolean TypeDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDeclaration")) return false;
    if (!nextTokenIs(b, TYPE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TYPE_DECLARATION, null);
    r = consumeToken(b, TYPE);
    p = r; // pin = 1
    r = r && report_error_(b, Identifier(b, l + 1));
    r = p && FieldDeclarationBlock(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // UnsupportedType | Identifier
  public static boolean TypeReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeReference")) return false;
    if (!nextTokenIs(b, "<type reference>", IDENTIFIER, UNSUPPORTED)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_REFERENCE, "<type reference>");
    r = UnsupportedType(b, l + 1);
    if (!r) r = Identifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // TypeReference '[' ']' '?'
  public static boolean UnsupportedOptionalListType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnsupportedOptionalListType")) return false;
    if (!nextTokenIs(b, "<unsupported optional list type>", IDENTIFIER, UNSUPPORTED)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNSUPPORTED_OPTIONAL_LIST_TYPE, "<unsupported optional list type>");
    r = TypeReference(b, l + 1);
    r = r && consumeTokens(b, 0, LBRACKET, RBRACKET, QUEST);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // UNSUPPORTED '(' STRING_LITERAL ')'
  public static boolean UnsupportedType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnsupportedType")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, UNSUPPORTED_TYPE, "<unsupported type>");
    r = consumeTokens(b, 1, UNSUPPORTED, LPAREN, STRING_LITERAL, RPAREN);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, PrismaParser::UnsupportedType_recover);
    return r || p;
  }

  /* ********************************************************** */
  // !(')' | '}' | '@' | '@@' | IDENTIFIER | TopLevelKeywords)
  static boolean UnsupportedType_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnsupportedType_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !UnsupportedType_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ')' | '}' | '@' | '@@' | IDENTIFIER | TopLevelKeywords
  private static boolean UnsupportedType_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnsupportedType_recover_0")) return false;
    boolean r;
    r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, RBRACE);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, ATAT);
    if (!r) r = consumeToken(b, IDENTIFIER);
    if (!r) r = TopLevelKeywords(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // !<<isNewLine>>
  static boolean UntilNewLine_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UntilNewLine_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !isNewLine(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // Expression
  public static boolean ValueArgument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ValueArgument")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VALUE_ARGUMENT, "<value argument>");
    r = Expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  static final Parser AT_parser_ = (b, l) -> consumeTokenFast(b, AT);
}
