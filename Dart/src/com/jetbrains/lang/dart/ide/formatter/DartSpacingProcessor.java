package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.Block;
import com.intellij.formatting.Spacing;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartTokenTypesSets.*;

public class DartSpacingProcessor {
  private static final TokenSet KEYWORDS_WITH_SPACE_AFTER = TokenSet.create(
    VAR,
    FINAL,
    STATIC,
    EXTERNAL,
    ABSTRACT,
    GET,
    SET,
    FACTORY,
    OPERATOR,
    METADATA,
    PART,
    EXPORT,
    AS,
    SHOW,
    HIDE
  );

  private static final TokenSet KEYWORDS_WITH_SPACE_BEFORE = TokenSet.create(
    GET,
    SET,
    EXTENDS,
    IMPLEMENTS,
    AS
  );

  private final ASTNode myNode;
  private final CommonCodeStyleSettings mySettings;

  public DartSpacingProcessor(ASTNode node, CommonCodeStyleSettings settings) {
    myNode = node;
    mySettings = settings;
  }

  public Spacing getSpacing(final Block child1, final Block child2) {
    if (!(child1 instanceof AbstractBlock) || !(child2 instanceof AbstractBlock)) {
      return null;
    }

    final IElementType elementType = myNode.getElementType();
    final IElementType parentType = myNode.getTreeParent() == null ? null : myNode.getTreeParent().getElementType();
    final ASTNode node1 = ((AbstractBlock)child1).getNode();
    final IElementType type1 = node1.getElementType();
    final ASTNode node2 = ((AbstractBlock)child2).getNode();
    final IElementType type2 = node2.getElementType();

    if (FUNCTION_DEFINITION.contains(type2)) {
      return Spacing.createSpacing(0, 0, 2, false, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }
    if (DOC_COMMENT_CONTENTS.contains(type2)) {
      return Spacing.createSpacing(0, Integer.MAX_VALUE, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }
    if (type2 != SEMICOLON && BLOCKS.contains(elementType)) {
      boolean topLevel = elementType == DART_FILE || elementType == EMBEDDED_CONTENT;
      int lineFeeds = 1;
      if (!COMMENTS.contains(type1) && (elementType == CLASS_MEMBERS || topLevel && DECLARATIONS.contains(type2))) {
        lineFeeds = 2;
      }
      return Spacing.createSpacing(0, 0, lineFeeds, false, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }
    if (type2 != SEMICOLON && elementType == STATEMENTS && (parentType == SWITCH_CASE || parentType == DEFAULT_CASE)) {
      return Spacing.createSpacing(0, 0, 1, false, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }
    if (type1 == SEMICOLON && !COMMENTS.contains(type2) && parentType == BLOCK) {
      return addLineBreak();
    }
    if (type1 == STATEMENTS || type2 == STATEMENTS) {
      return addLineBreak();
    }
    if (type1 == CLASS_MEMBERS || type2 == CLASS_MEMBERS) {
      return addSingleSpaceIf(false, true);
    }

    if (type1 == CLASS_MEMBERS) {
      return addLineBreak();
    }

    if (type2 == LPAREN) {
      if (elementType == IF_STATEMENT) {
        return addSingleSpaceIf(mySettings.SPACE_BEFORE_IF_PARENTHESES);
      }
      else if (elementType == WHILE_STATEMENT || elementType == DO_WHILE_STATEMENT) {
        return addSingleSpaceIf(mySettings.SPACE_BEFORE_WHILE_PARENTHESES);
      }
      else if (elementType == SWITCH_STATEMENT) {
        return addSingleSpaceIf(mySettings.SPACE_BEFORE_SWITCH_PARENTHESES);
      }
      else if (elementType == TRY_STATEMENT) {
        return addSingleSpaceIf(mySettings.SPACE_BEFORE_TRY_PARENTHESES);
      }
      else if (elementType == CATCH_PART) {
        return addSingleSpaceIf(mySettings.SPACE_BEFORE_CATCH_PARENTHESES);
      }
    }

    if (type2 == FOR_LOOP_PARTS_IN_BRACES) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_FOR_PARENTHESES);
    }

    if (type2 == FORMAL_PARAMETER_LIST && (FUNCTION_DEFINITION.contains(elementType) || elementType == FUNCTION_EXPRESSION)) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_METHOD_PARENTHESES);
    }

    if (type2 == ARGUMENTS && elementType == CALL_EXPRESSION) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_METHOD_CALL_PARENTHESES);
    }

    //
    //Spacing before left braces
    //
    if (type2 == BLOCK) {
      if (elementType == IF_STATEMENT && type1 != ELSE) {
        return setBraceSpace(mySettings.SPACE_BEFORE_IF_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
      }
      else if (elementType == IF_STATEMENT && type1 == ELSE) {
        return setBraceSpace(mySettings.SPACE_BEFORE_ELSE_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
      }
      else if (elementType == WHILE_STATEMENT || elementType == DO_WHILE_STATEMENT) {
        return setBraceSpace(mySettings.SPACE_BEFORE_WHILE_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
      }
      else if (elementType == FOR_STATEMENT) {
        return setBraceSpace(mySettings.SPACE_BEFORE_FOR_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
      }
      else if (elementType == TRY_STATEMENT) {
        return setBraceSpace(mySettings.SPACE_BEFORE_TRY_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
      }
      else if (elementType == CATCH_PART) {
        return setBraceSpace(mySettings.SPACE_BEFORE_CATCH_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
      }
    }

    if (type2 == LBRACE && elementType == SWITCH_STATEMENT) {
      return setBraceSpace(mySettings.SPACE_BEFORE_SWITCH_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
    }

    if (FUNCTION_DEFINITION.contains(elementType) && type2 == FUNCTION_BODY) {
      return setBraceSpace(mySettings.SPACE_BEFORE_METHOD_LBRACE, mySettings.METHOD_BRACE_STYLE, child1.getTextRange());
    }

    if (elementType == FUNCTION_EXPRESSION && type2 == FUNCTION_EXPRESSION_BODY) {
      return setBraceSpace(mySettings.SPACE_BEFORE_METHOD_LBRACE, mySettings.METHOD_BRACE_STYLE, child1.getTextRange());
    }

    if (elementType == CLASS_DEFINITION && type2 == CLASS_BODY) {
      return setBraceSpace(mySettings.SPACE_BEFORE_CLASS_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
    }

    if (type1 == LPAREN || type2 == RPAREN) {
      if (elementType == IF_STATEMENT) {
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_IF_PARENTHESES);
      }
      else if (elementType == WHILE_STATEMENT || elementType == DO_WHILE_STATEMENT) {
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_WHILE_PARENTHESES);
      }
      else if (elementType == FOR_LOOP_PARTS_IN_BRACES) {
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_FOR_PARENTHESES);
      }
      else if (elementType == SWITCH_STATEMENT) {
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_SWITCH_PARENTHESES);
      }
      else if (elementType == TRY_STATEMENT) {
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_TRY_PARENTHESES);
      }
      else if (elementType == CATCH_PART) {
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_CATCH_PARENTHESES);
      }
      else if (elementType == FORMAL_PARAMETER_LIST) {
        final boolean newLineNeeded = type1 == LPAREN ?
                                      mySettings.METHOD_PARAMETERS_LPAREN_ON_NEXT_LINE :
                                      mySettings.METHOD_PARAMETERS_RPAREN_ON_NEXT_LINE;
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_METHOD_PARENTHESES, newLineNeeded);
      }
      else if (elementType == ARGUMENTS) {
        final boolean newLineNeeded = type1 == LPAREN ?
                                      mySettings.CALL_PARAMETERS_LPAREN_ON_NEXT_LINE :
                                      mySettings.CALL_PARAMETERS_RPAREN_ON_NEXT_LINE;
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_METHOD_CALL_PARENTHESES, newLineNeeded);
      }
      else if (mySettings.BINARY_OPERATION_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP && elementType == PARENTHESIZED_EXPRESSION) {
        final boolean newLineNeeded = type1 == LPAREN ?
                                      mySettings.PARENTHESES_EXPRESSION_LPAREN_WRAP :
                                      mySettings.PARENTHESES_EXPRESSION_RPAREN_WRAP;
        return addSingleSpaceIf(false, newLineNeeded);
      }
    }

    if (elementType == TERNARY_EXPRESSION) {
      if (type2 == QUEST) {
        return addSingleSpaceIf(mySettings.SPACE_BEFORE_QUEST);
      }
      else if (type2 == COLON) {
        return addSingleSpaceIf(mySettings.SPACE_BEFORE_COLON);
      }
      else if (type1 == QUEST) {
        return addSingleSpaceIf(mySettings.SPACE_AFTER_QUEST);
      }
      else if (type1 == COLON) {
        return addSingleSpaceIf(mySettings.SPACE_AFTER_COLON);
      }
    }

    //
    // Spacing around assignment operators (=, -=, etc.)
    //

    if (type1 == ASSIGNMENT_OPERATOR || type2 == ASSIGNMENT_OPERATOR) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_ASSIGNMENT_OPERATORS);
    }

    if (type1 == EQ && elementType == VAR_INIT) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_ASSIGNMENT_OPERATORS);
    }

    if (type2 == VAR_INIT) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_ASSIGNMENT_OPERATORS);
    }

    //
    // Spacing around  logical operators (&&, OR, etc.)
    //
    if (LOGIC_OPERATORS.contains(type1) || LOGIC_OPERATORS.contains(type2)) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_LOGICAL_OPERATORS);
    }
    //
    // Spacing around  equality operators (==, != etc.)
    //
    if (type1 == EQUALITY_OPERATOR || type2 == EQUALITY_OPERATOR) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_EQUALITY_OPERATORS);
    }
    //
    // Spacing around  relational operators (<, <= etc.)
    //
    if (type1 == RELATIONAL_OPERATOR || type2 == RELATIONAL_OPERATOR) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_RELATIONAL_OPERATORS);
    }
    //
    // Spacing around  additive operators ( &, |, ^, etc.)
    //
    if (BITWISE_OPERATORS.contains(type1) || BITWISE_OPERATORS.contains(type2)) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_BITWISE_OPERATORS);
    }
    //
    // Spacing around  additive operators ( +, -, etc.)
    //
    if ((type1 == ADDITIVE_OPERATOR || type2 == ADDITIVE_OPERATOR) &&
        elementType != PREFIX_EXPRESSION) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_ADDITIVE_OPERATORS);
    }
    //
    // Spacing around  multiplicative operators ( *, /, %, etc.)
    //
    if (type1 == MULTIPLICATIVE_OPERATOR || type2 == MULTIPLICATIVE_OPERATOR) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS);
    }
    //
    // Spacing around  unary operators ( NOT, ++, etc.)
    //
    if (type1 == PREFIX_OPERATOR || type2 == PREFIX_OPERATOR) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_UNARY_OPERATOR);
    }
    //
    // Spacing around  shift operators ( <<, >>, >>>, etc.)
    //
    if (type1 == SHIFT_OPERATOR || type2 == SHIFT_OPERATOR) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_SHIFT_OPERATORS);
    }

    //
    //Spacing before keyword (else, catch, etc)
    //
    if (type2 == ELSE) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_ELSE_KEYWORD, mySettings.ELSE_ON_NEW_LINE);
    }
    if (type2 == WHILE) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_WHILE_KEYWORD, mySettings.WHILE_ON_NEW_LINE);
    }
    if (type2 == CATCH_PART) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_CATCH_KEYWORD, mySettings.CATCH_ON_NEW_LINE);
    }

    //
    //Other
    //

    if (type1 == ELSE && type2 == IF_STATEMENT) {
      return Spacing.createSpacing(1, 1, mySettings.SPECIAL_ELSE_IF_TREATMENT ? 0 : 1, false, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }

    if (type1 == COMMA &&
        (elementType == FORMAL_PARAMETER_LIST || elementType == ARGUMENT_LIST || elementType == NORMAL_FORMAL_PARAMETER)) {
      return addSingleSpaceIf(mySettings.SPACE_AFTER_COMMA_IN_TYPE_ARGUMENTS);
    }

    if (type1 == COMMA) {
      return addSingleSpaceIf(mySettings.SPACE_AFTER_COMMA);
    }

    if (type2 == COMMA) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_COMMA);
    }

    //todo: customize in settings

    if (type1 == EXPRESSION_BODY_DEF || type2 == EXPRESSION_BODY_DEF) {
      return addSingleSpaceIf(true);
    }

    if (type1 == FOR_LOOP_PARTS_IN_BRACES && !BLOCKS.contains(type2)) {
      return addLineBreak();
    }

    if (type1 == IF_STATEMENT ||
        type1 == SWITCH_STATEMENT ||
        type1 == TRY_STATEMENT ||
        type1 == DO_WHILE_STATEMENT ||
        type1 == FOR_STATEMENT ||
        type1 == SWITCH_CASE ||
        type1 == DEFAULT_CASE ||
        type1 == WHILE_STATEMENT) {
      return addLineBreak();
    }

    boolean isBraces = type1 == LBRACE || type2 == RBRACE;
    if ((isBraces && elementType != NAMED_FORMAL_PARAMETERS) ||
        BLOCKS.contains(type1) ||
        FUNCTION_DEFINITION.contains(type1) ||
        COMMENTS.contains(type1)) {
      return addLineBreak();
    }

    if (COMMENTS.contains(type2)) {
      return Spacing.createSpacing(0, 1, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }

    if (KEYWORDS_WITH_SPACE_AFTER.contains(type1) || KEYWORDS_WITH_SPACE_BEFORE.contains(type2)) {
      return addSingleSpaceIf(true);
    }

    if (type1 != DOT && type2 == DOT && elementType == CASCADE_REFERENCE_EXPRESSION) {
      return addLineBreak();
    }

    return Spacing.createSpacing(0, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
  }

  private Spacing addLineBreak() {
    return Spacing.createSpacing(0, 0, 1, false, mySettings.KEEP_BLANK_LINES_IN_CODE);
  }

  private Spacing addSingleSpaceIf(boolean condition) {
    return addSingleSpaceIf(condition, false);
  }

  private Spacing addSingleSpaceIf(boolean condition, boolean linesFeed) {
    final int spaces = condition ? 1 : 0;
    final int lines = linesFeed ? 1 : 0;
    return Spacing.createSpacing(spaces, spaces, lines, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
  }

  private Spacing setBraceSpace(boolean needSpaceSetting,
                                @CommonCodeStyleSettings.BraceStyleConstant int braceStyleSetting,
                                TextRange textRange) {
    final int spaces = needSpaceSetting ? 1 : 0;
    if (braceStyleSetting == CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED && textRange != null) {
      return Spacing.createDependentLFSpacing(spaces, spaces, textRange, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }
    else {
      final int lineBreaks = braceStyleSetting == CommonCodeStyleSettings.END_OF_LINE ||
                             braceStyleSetting == CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED ? 0 : 1;
      return Spacing.createSpacing(spaces, spaces, lineBreaks, false, 0);
    }
  }
}
