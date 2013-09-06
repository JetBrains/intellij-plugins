package com.jetbrains.lang.dart;

import com.intellij.lang.LighterASTNode;
import com.intellij.lang.LighterLazyParseableNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.*;
import com.intellij.util.diff.FlyweightCapableTreeStructure;

import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import static com.jetbrains.lang.dart.DartTokenTypes.*;

/**
 * @author: Fedor.Korotkov
 */
public interface DartTokenTypesSets {
  IFileElementType DART_FILE = new IFileElementType("DARTFILE", DartLanguage.INSTANCE);

  IElementType WHITE_SPACE = TokenType.WHITE_SPACE;
  IElementType BAD_CHARACTER = TokenType.BAD_CHARACTER;

  IElementType SINGLE_LINE_COMMENT = new DartElementType("SINGLE_LINE_COMMENT");
  IElementType MULTI_LINE_COMMENT = new DartElementType("MULTI_LINE_COMMENT");
  IElementType DOC_COMMENT = new DartElementType("DOC_COMMENT");

  TokenSet STRINGS = TokenSet.create(RAW_SINGLE_QUOTED_STRING, RAW_TRIPLE_QUOTED_STRING, OPEN_QUOTE, CLOSING_QUOTE, REGULAR_STRING_PART,
                                     SHORT_TEMPLATE_ENTRY_START, LONG_TEMPLATE_ENTRY_START, LONG_TEMPLATE_ENTRY_END);
  TokenSet WHITE_SPACES = TokenSet.create(WHITE_SPACE);
  TokenSet UNRESERVED = TokenSet.create(ABSTRACT, ASSERT,
                                        FACTORY, GET, IMPLEMENTS,
                                        IMPORT, INTERFACE, IS, AS, ON, LIBRARY,
                                        NEGATE, OPERATOR, SET,
                                        STATIC, TYPEDEF, WITH);

  TokenSet RESERVED = TokenSet.create(IF, ELSE, TRY, THROW, CATCH, FINALLY, TRUE,
                                      FALSE, NULL, SWITCH, CASE, DEFAULT,
                                      BREAK, CONTINUE, DO, WHILE, FOR, IN, VAR,
                                      FINAL, HASH, SHOW, HIDE, PART, OF,
                                      RETURN, THIS, SUPER, CLASS, EXTENDS, NEW, CONST);

  TokenSet OPERATORS = TokenSet.create(
    MINUS, MINUS_EQ, MINUS_MINUS, PLUS, PLUS_PLUS, PLUS_EQ, DIV, DIV_EQ, MUL, MUL_EQ, INT_DIV, INT_DIV_EQ, REM_EQ, REM, BIN_NOT, NOT,
    EQ, EQ_EQ, EQ_EQ_EQ, NEQ, NEQ_EQ, GT, GT_EQ, GT_GT_EQ, GT_GT_GT_EQ, LT, LT_EQ, LT_LT, LT_LT_EQ, OR, OR_EQ, OR_OR, XOR, XOR_EQ, AND,
    AND_EQ, AND_AND, LBRACKET, RBRACKET, NEGATE, AS
  );

  TokenSet ASSIGNMENT_OPERATORS = TokenSet.create(
    // '=' | '*=' | '/=' | '~/=' | '%=' | '+=' | '-=' | '<<=' | '>>>=' | '>>=' | '&=' | '^=' | '|='
    EQ, MUL_EQ, DIV_EQ, INT_DIV_EQ, REM_EQ, PLUS_EQ, MINUS_EQ, LT_LT_EQ, GT_GT_GT_EQ, GT_GT_EQ, AND_EQ, XOR_EQ, OR_EQ
  );

  TokenSet BINARY_EXPRESSIONS = TokenSet.create(
    LOGIC_OR_EXPRESSION,
    LOGIC_AND_EXPRESSION,
    COMPARE_EXPRESSION,
    SHIFT_EXPRESSION,
    ADDITIVE_EXPRESSION,
    MULTIPLICATIVE_EXPRESSION
  );

  TokenSet BINARY_OPERATORS = TokenSet.create(
    OR_OR, AND_AND, RELATIONAL_OPERATOR, SHIFT_RIGHT_OPERATOR, BITWISE_OPERATOR, SHIFT_OPERATOR, ADDITIVE_OPERATOR, MULTIPLICATIVE_OPERATOR
  );

  TokenSet LOGIC_OPERATORS = TokenSet.create(
    OR_OR, AND_AND
  );

  TokenSet UNARY_OPERATORS = TokenSet.create(
    PLUS_PLUS, MINUS_MINUS, NOT, MINUS
  );
  TokenSet BITWISE_OPERATORS = TokenSet.create(BITWISE_OPERATOR);
  TokenSet FUNCTION_DEFINITION = TokenSet.create(
    FUNCTION_DECLARATION,
    FUNCTION_DECLARATION_WITH_BODY,
    FUNCTION_DECLARATION_WITH_BODY_OR_NATIVE,
    METHOD_DECLARATION,
    METHOD_PROTOTYPE_DECLARATION,
    GETTER_DECLARATION,
    SETTER_DECLARATION
  );

  TokenSet COMMENTS = TokenSet.create(
    SINGLE_LINE_COMMENT, MULTI_LINE_COMMENT, DOC_COMMENT
  );

  IElementType EMBEDDED_CONTENT = new DartEmbeddedContentElementType();

  TokenSet BLOCKS = TokenSet.create(
    BLOCK,
    CLASS_MEMBERS,
    INTERFACE_MEMBERS,
    DART_FILE,
    EMBEDDED_CONTENT
  );

  TokenSet DECLARATIONS = TokenSet.create(
    CLASS_DEFINITION,
    INTERFACE_DEFINITION,
    FUNCTION_DECLARATION_WITH_BODY,
    FUNCTION_DECLARATION_WITH_BODY_OR_NATIVE,
    GETTER_DECLARATION,
    SETTER_DECLARATION,
    VAR_DECLARATION_LIST,
    FUNCTION_TYPE_ALIAS
  );

  class DartEmbeddedContentElementType extends ILazyParseableElementType implements ILightLazyParseableElementType {
    public DartEmbeddedContentElementType() {
      super("DART_EMBEDDED_CONTENT", DartInHtmlLanguage.INSTANCE);
    }

    @Override
    public FlyweightCapableTreeStructure<LighterASTNode> parseContents(LighterLazyParseableNode chameleon) {
      PsiFile file = chameleon.getContainingFile();
      assert file != null : chameleon;

      final PsiBuilder psiBuilder = PsiBuilderFactory.getInstance().createBuilder(file.getProject(), chameleon);

      final PsiBuilder builder = adapt_builder_(EMBEDDED_CONTENT, psiBuilder, new DartParser(), DartParser.EXTENDS_SETS_);

      final PsiBuilder.Marker marker = builder.mark();
      enterErrorRecordingSection(builder, 0, _SECTION_GENERAL_, "<code fragment>");
      DartParser.dartUnit(builder, 1);
      while (builder.getTokenType() != null) {
        builder.advanceLexer();
      }
      marker.done(EMBEDDED_CONTENT);

      return builder.getLightTree();
    }
  }
}
