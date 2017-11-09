package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.Block;
import com.intellij.formatting.Spacing;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.containers.SortedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartTokenTypesSets.*;

public class DartSpacingProcessor {
  private static final TokenSet TOKENS_WITH_SPACE_AFTER = TokenSet
    .create(VAR, FINAL, STATIC, EXTERNAL, ABSTRACT, GET, SET, FACTORY, OPERATOR, PART, EXPORT, DEFERRED, AS, SHOW, HIDE, RETURN_TYPE,
            COVARIANT);

  private static final TokenSet KEYWORDS_WITH_SPACE_BEFORE =
    TokenSet.create(GET, SET, EXTENDS, IMPLEMENTS, DEFERRED, AS, SHOW_COMBINATOR, HIDE_COMBINATOR, COVARIANT);

  private static final TokenSet CASCADE_REFERENCE_EXPRESSION_SET = TokenSet.create(CASCADE_REFERENCE_EXPRESSION);
  private static final TokenSet REFERENCE_EXPRESSION_SET = TokenSet.create(REFERENCE_EXPRESSION);
  private static final TokenSet ID_SET = TokenSet.create(ID);
  private static final TokenSet PREFIX_OPERATOR_SET = TokenSet.create(PREFIX_OPERATOR);
  private static final TokenSet SIMPLE_LITERAL_SET =
    TokenSet.create(STRING_LITERAL_EXPRESSION, NUMBER, TRUE, FALSE, NULL, THIS, LIST_LITERAL_EXPRESSION, MAP_LITERAL_EXPRESSION);
  private static final TokenSet SKIP_COMMA = TokenSet.create(COMMA);
  private static final TokenSet DIRECTIVE_GROUPS = TokenSet.create(IMPORT_STATEMENT, EXPORT_STATEMENT, PART_STATEMENT);

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

    if (type2 == SINGLE_LINE_COMMENT && !isDirectlyPrecededByNewline(node2)) {
      // line comment after code on the same line: do not add line break here, it may be used to ignore warning
      // but after '{' in class or function definition Dart Style inserts line break, so let's do the same
      if (type1 != LBRACE || (elementType != CLASS_BODY && (!BLOCKS.contains(elementType) || parentType != FUNCTION_BODY))) {
        return Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE, 0);
      }
    }

    if (elementType == DART_FILE && COMMENTS.contains(type1) && !COMMENTS.contains(type2)) {
      final ASTNode prev = getPrevSiblingOnTheSameLineSkipCommentsAndWhitespace(((AbstractBlock)child1).getNode());
      if (prev != null) {
        final int lineBreaks = getMinLineBreaksBetweenTopLevelNodes(prev.getElementType(), type2);
        return Spacing.createSpacing(0, 0, lineBreaks, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
      }
    }
    if (type1 == SINGLE_LINE_COMMENT) {
      return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }
    if (type2 == SINGLE_LINE_DOC_COMMENT) {
      int nsp = 2;
      if (type1 == SINGLE_LINE_DOC_COMMENT || (elementType != DART_FILE && type1 == LBRACE)) nsp = 1;
      return Spacing.createSpacing(0, 0, nsp, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }

    if (DIRECTIVE_GROUPS.contains(type1)) {
      if (type2 == MULTI_LINE_COMMENT) {
        ASTNode next = FormatterUtil.getNextNonWhitespaceSibling(node2);
        if (next != null &&
            next.getElementType() == type1) {
          boolean needsNewline = isEmbeddedComment(type2, child2) && !isDirectlyPrecededByNewline(next);
          int space = needsNewline ? 0 : 1;
          int newline = needsNewline ? 1 : 0;
          return Spacing.createSpacing(0, space, newline, true, mySettings.KEEP_BLANK_LINES_IN_CODE);
        }
      }
      if (type2 != IMPORT_STATEMENT && type2 != EXPORT_STATEMENT && !isEmbeddedComment(type2, child2)) {
        int numNewlines = COMMENTS.contains(type2) && isBlankLineAfterComment(node2) ? 1 : 2;
        return Spacing.createSpacing(0, 0, numNewlines, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
      }
    }
    if (type1 == LIBRARY_STATEMENT) {
      int newlines = COMMENTS.contains(type2) && isBlankLineAfterComment(node2) ? 1 : 2;
      return Spacing.createSpacing(0, 0, newlines, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }

    if (elementType == LIBRARY_STATEMENT || parentType == LIBRARY_STATEMENT) {
      if (isEmbeddedComment(type2, child2)) {
        return Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
      }
      if (type1 == MULTI_LINE_COMMENT && isEmbeddedComment(type1, child1)) {
        return Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
      }
    }
    if (parentType == LIBRARY_STATEMENT) {
      return noSpace();
    }

    if (SEMICOLON == type2) {
      if (type1 == SEMICOLON && elementType == STATEMENTS) {
        return addSingleSpaceIf(false, true); // Empty statement on new line.
      }
      return Spacing.createSpacing(0, 0, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }

    if (AT == type1) return Spacing.createSpacing(0, 0, 0, false, 0);

    if (METADATA == type1) {
      if (parentType == TYPE_PARAMETERS) {
        // Metadata on type parameters must be inlined.
        return Spacing.createSpacing(1, 1, 0, false, 0);
      }
      if (COMMENTS.contains(type2)) {
        return Spacing.createSpacing(1, 1, 0, true, 0);
      }
      if (parentType == DART_FILE) {
        // Metadata on top-level declarations must be on its own line.
        return Spacing.createSpacing(0, 0, 1, false, 0); //false here
      }
      if (parentType == CLASS_MEMBERS || FUNCTION_DEFINITION.contains(parentType)) {
        if (type2 == METADATA || FormatterUtil.isPrecededBy(node1, METADATA, WHITE_SPACE)) {
          // Multiple metadata each goes on its own line.
          return Spacing.createSpacing(0, 0, 1, false, 0);
        }
        // Metadata on constructors and methods may be inlined.
        return Spacing.createSpacing(1, 1, 0, true, 0);
      }
      if (parentType == VAR_DECLARATION_LIST) {
        if (myNode.getTreeParent().getTreeParent().getElementType() == STATEMENTS) {
          // Metadata on local variables must be on its own line.
          return Spacing.createSpacing(0, 0, 1, false, 0);
        }
        if (type2 == METADATA || FormatterUtil.isPrecededBy(node1, METADATA, WHITE_SPACE)) {
          // Multiple metadata each goes on its own line.
          return Spacing.createSpacing(0, 0, 1, false, 0);
        }
        // Metadata on fields may be inlined.
        return Spacing.createSpacing(1, 1, 0, true, 0);
      }
      if (parentType == NORMAL_FORMAL_PARAMETER) {
        // Metadata on parameter declarations must be inlined.
        return Spacing.createSpacing(1, 1, 0, false, 0);
      }
      // Other metadata occurrences may be inlined.
      return Spacing.createSpacing(1, 1, 0, true, 0);
    }

    if (FUNCTION_DEFINITION.contains(type2)) {
      boolean needsBlank = needsBlankLineBeforeFunction(elementType);
      if (needsBlank && !mySettings.KEEP_LINE_BREAKS) {
        if (parentType == CLASS_BODY || elementType == DART_FILE) {
          if (type1 == SEMICOLON || hasEmptyBlock(node1)) {
            needsBlank = false;
          }
        }
      }
      final int lineFeeds = COMMENTS.contains(type1) || !needsBlank ? 1 : 2;
      return Spacing.createSpacing(0, 0, lineFeeds, needsBlank, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }
    if (DOC_COMMENT_CONTENTS.contains(type2)) {
      return Spacing.createSpacing(0, Integer.MAX_VALUE, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }
    if (BLOCKS_EXT.contains(elementType)) {
      boolean topLevel = elementType == DART_FILE || elementType == EMBEDDED_CONTENT;
      int lineFeeds = 1;
      int spaces = 0;
      int blanks = mySettings.KEEP_BLANK_LINES_IN_CODE;
      boolean keepBreaks = false;
      if (!COMMENTS.contains(type1) && (elementType == CLASS_MEMBERS || topLevel && DECLARATIONS.contains(type2))) {
        if (type1 == SEMICOLON && type2 == VAR_DECLARATION_LIST) {
          final ASTNode node1TreePrev = node1.getTreePrev();
          if (node1TreePrev == null || node1TreePrev.getElementType() != VAR_DECLARATION_LIST) {
            lineFeeds = 2;
          }
        }
        else {
          if (type2 == VAR_DECLARATION_LIST && hasEmptyBlock(node1) ||
              type1 == FUNCTION_TYPE_ALIAS && type2 == FUNCTION_TYPE_ALIAS) {
            lineFeeds = 1;
          }
          else {
            lineFeeds = 2;
          }
        }
      }
      else if (type1 == LBRACE && type2 == RBRACE) {
        if (parentType == ON_PART && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
          lineFeeds = 0;
        }
        else if (parentType == FUNCTION_BODY) {
          if ((myNode.getTreeParent().getTreeParent() != null) &&
              (myNode.getTreeParent().getTreeParent().getElementType() == METHOD_DECLARATION) &&
              mySettings.KEEP_SIMPLE_METHODS_IN_ONE_LINE) {
            lineFeeds = 0; // Empty method.
            keepBreaks = mySettings.KEEP_LINE_BREAKS;
            blanks = keepBreaks ? mySettings.KEEP_BLANK_LINES_IN_CODE : 0;
          }
          else if (mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
            lineFeeds = 0; // Empty function, either top-level or statement.
            keepBreaks = mySettings.KEEP_LINE_BREAKS;
            blanks = keepBreaks ? mySettings.KEEP_BLANK_LINES_IN_CODE : 0;
          }
        }
        else if (parentType == IF_STATEMENT && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
          lineFeeds = 0;
        }
        else if (parentType == FOR_STATEMENT && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
          lineFeeds = 0;
        }
        else if (parentType == WHILE_STATEMENT && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
          lineFeeds = 0;
        }
        else if (parentType == DO_WHILE_STATEMENT && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
          lineFeeds = 0;
        }
        else if (parentType == TRY_STATEMENT && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
          lineFeeds = 0;
        }
        else if (parentType == FINALLY_PART && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
          lineFeeds = 0;
        }
        else if (parentType == FUNCTION_EXPRESSION_BODY && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
          lineFeeds = 0;
        }
        else if (parentType == STATEMENTS && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
          lineFeeds = 0;
        }
      }
      else if (topLevel && COMMENTS.contains(type2)) {
        lineFeeds = 0;
        spaces = 1;
        keepBreaks = true;
      }
      else if (type1 != LBRACE && isEmbeddedComment(type2, child2)) {
        lineFeeds = 0;
        spaces = 1;
        keepBreaks = false;
      }
      else if ((type1 == LBRACE && type2 == STATEMENTS) || (type2 == RBRACE && type1 == STATEMENTS)) {
        lineFeeds = 1;
        keepBreaks = false;
        blanks = 0;
      }
      else if (type1 == LBRACE && type2 == SINGLE_LINE_COMMENT) {
        lineFeeds = 1;
        keepBreaks = false;
        blanks = 0;
      }
      else if (type1 == MULTI_LINE_COMMENT && type2 == STATEMENTS) {
        spaces = 1;
        lineFeeds = 0;
        keepBreaks = true;
      }
      return Spacing.createSpacing(spaces, spaces, lineFeeds, keepBreaks, blanks);
    }
    if (elementType == STATEMENTS && (parentType == SWITCH_CASE || parentType == DEFAULT_CASE)) {
      return Spacing.createSpacing(0, 0, 1, false, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }
    if (!COMMENTS.contains(type2) && BLOCKS.contains(parentType) && !(node1.getTreeNext() instanceof PsiErrorElement)) {
      return addLineBreak();
    }

    // Special checks for switch formatting according to dart_style, which conflicts with settings.
    if (type2 == RBRACE && (type1 == SWITCH_CASE || type1 == DEFAULT_CASE)) {
      // No blank line before closing brace in switch statement.
      return Spacing.createSpacing(0, 0, 1, false, 0);
    }
    if (type1 == COLON && (elementType == SWITCH_CASE || elementType == DEFAULT_CASE)) {
      // No blank line before first statement of a case.
      return Spacing.createSpacing(0, 0, 1, false, 0);
    }
    if (elementType == SWITCH_STATEMENT && type1 == LBRACE) {
      // No blank line before first case of a switch.
      return Spacing.createSpacing(0, 0, 1, false, 0);
    }

    if (type1 == STATEMENTS || type2 == STATEMENTS) {
      return addLineBreak();
    }
    if (type1 == CLASS_MEMBERS || type2 == CLASS_MEMBERS) {
      if (type1 == MULTI_LINE_COMMENT) {
        return addSingleSpaceIf(true, false);
      }
      else {
        return addSingleSpaceIf(false, true);
      }
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
      else if (elementType == ON_PART || elementType == CATCH_PART) {
        return addSingleSpaceIf(mySettings.SPACE_BEFORE_CATCH_PARENTHESES);
      }
    }
    if (elementType == IF_STATEMENT) {
      if (type1 == RPAREN && mySettings.BRACE_STYLE == CommonCodeStyleSettings.END_OF_LINE) {
        // Always have a single space following the closing paren of an if-condition.
        int nsp = mySettings.SPACE_BEFORE_IF_LBRACE ? 1 : 0;
        int lf = 0;
        if (!BLOCKS.contains(type2) && mySettings.SPECIAL_ELSE_IF_TREATMENT) {
          if (FormatterUtil.isFollowedBy(node2, ELSE, SEMICOLON)) lf = 1;
        }
        return Spacing.createSpacing(nsp, nsp, lf, !BLOCKS.contains(type2) && mySettings.KEEP_LINE_BREAKS, 0);
      }
      if (type1 == SEMICOLON && type2 == ELSE) {
        // If the then-part is on the line with the condition put the else-part on the next line.
        return Spacing.createSpacing(0, 0, 1, false, 0);
      }
    }

    if (type2 == FOR_LOOP_PARTS_IN_BRACES) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_FOR_PARENTHESES);
    }

    if (type2 == FORMAL_PARAMETER_LIST && (FUNCTION_DEFINITION.contains(elementType) || elementType == FUNCTION_EXPRESSION)) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_METHOD_PARENTHESES);
    }

    if (elementType == DEFAULT_FORMAL_NAMED_PARAMETER && (type1 == EQ || type2 == EQ)) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_ASSIGNMENT_OPERATORS);
    }

    if (type2 == ARGUMENTS && elementType == CALL_EXPRESSION) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_METHOD_CALL_PARENTHESES);
    }

    //
    //Spacing before left braces
    //
    if (BLOCKS.contains(type2)) {
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
      else if (elementType == ON_PART) {
        return setBraceSpace(mySettings.SPACE_BEFORE_CATCH_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
      }
      else if (elementType == FINALLY_PART) {
        return setBraceSpace(mySettings.SPACE_BEFORE_FINALLY_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
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

    if (elementType == CLASS_DEFINITION) {
      if (type2 == CLASS_BODY) {
        return setBraceSpace(mySettings.SPACE_BEFORE_CLASS_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
      }
      if (type2 == TYPE_PARAMETERS) {
        return noSpace();
      }
      if (type2 == INTERFACES || type2 == MIXINS) {
        ASTNode typeNameNode = FormatterUtil.getNextNonWhitespaceSibling(myNode.getFirstChildNode());
        ASTNode bodyNode = myNode.getLastChildNode();
        if (typeNameNode != null && bodyNode != null) {
          // For some reason we need to start at the beginning of the type name, not the end.
          TextRange range = TextRange.create(typeNameNode.getTextRange().getStartOffset(), bodyNode.getStartOffset());
          return Spacing.createDependentLFSpacing(1, 1, range, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
        }
      }
      return Spacing.createSpacing(1, 1, 0, false, 0);
    }
    if (elementType == MIXIN_APPLICATION) {
      return Spacing.createSpacing(1, 1, 0, false, 0);
    }

    if (elementType == ENUM_DEFINITION) {
      if (mySettings.BRACE_STYLE == CommonCodeStyleSettings.END_OF_LINE) {
        if (type1 == LBRACE && type2 == RBRACE) {
          return noSpace();
        }
        if (type1 == LBRACE || type2 == RBRACE) {
          return Spacing.createDependentLFSpacing(1, 1, textRangeFollowingMetadata(), false, 0);
        }
        if (type2 == ENUM_CONSTANT_DECLARATION) {
          return Spacing.createDependentLFSpacing(1, 1, textRangeFollowingMetadata(), false, 0);
        }
      }
      if (type2 == LBRACE) {
        return setBraceSpace(mySettings.SPACE_BEFORE_CLASS_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
      }
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
      else if (elementType == CATCH_PART) {
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_CATCH_PARENTHESES);
      }
      else if (elementType == FORMAL_PARAMETER_LIST) {
        final boolean newLineNeeded =
          type1 == LPAREN ? mySettings.METHOD_PARAMETERS_LPAREN_ON_NEXT_LINE : mySettings.METHOD_PARAMETERS_RPAREN_ON_NEXT_LINE;
        if (newLineNeeded || mySettings.SPACE_WITHIN_METHOD_PARENTHESES) {
          return addSingleSpaceIf(mySettings.SPACE_WITHIN_METHOD_PARENTHESES, newLineNeeded);
        }
        return Spacing.createSpacing(0, 0, 0, false, 0);
      }
      else if (elementType == ARGUMENTS) {
        final boolean newLineNeeded =
          type1 == LPAREN ? mySettings.CALL_PARAMETERS_LPAREN_ON_NEXT_LINE : mySettings.CALL_PARAMETERS_RPAREN_ON_NEXT_LINE;
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_METHOD_CALL_PARENTHESES, newLineNeeded);
      }
      else if (mySettings.BINARY_OPERATION_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP && elementType == PARENTHESIZED_EXPRESSION) {
        final boolean newLineNeeded =
          type1 == LPAREN ? mySettings.PARENTHESES_EXPRESSION_LPAREN_WRAP : mySettings.PARENTHESES_EXPRESSION_RPAREN_WRAP;
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
    // Spacing around  bitwise operators ( &, |, ^, etc.)
    //
    if (BITWISE_OPERATORS.contains(type1) || BITWISE_OPERATORS.contains(type2)) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_BITWISE_OPERATORS);
    }
    //
    // Spacing around  additive operators ( +, -, etc.)
    //
    if ((type1 == ADDITIVE_OPERATOR || type2 == ADDITIVE_OPERATOR) && elementType != PREFIX_EXPRESSION) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_ADDITIVE_OPERATORS);
    }
    //
    // Spacing around  multiplicative operators ( *, /, %, etc.)
    //
    if (type1 == MULTIPLICATIVE_OPERATOR || type2 == MULTIPLICATIVE_OPERATOR) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS);
    }
    //
    // Spacing between successive unary operators ( -, + )
    //
    if (type1 == PREFIX_OPERATOR && type2 == PREFIX_EXPRESSION) {
      ASTNode[] childs = node2.getChildren(PREFIX_OPERATOR_SET);
      if (childs.length > 0) {
        return addSingleSpaceIf(isSpaceNeededBetweenPrefixOps(node1, childs[0]));
      }
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
    if (type2 == ON_PART) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_CATCH_KEYWORD, mySettings.CATCH_ON_NEW_LINE);
    }
    if (type2 == FINALLY_PART) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_FINALLY_KEYWORD, mySettings.CATCH_ON_NEW_LINE);
    }

    //
    //Other
    //

    if (type1 == ELSE) {
      if (type2 == IF_STATEMENT) {
        return Spacing.createSpacing(1, 1, mySettings.SPECIAL_ELSE_IF_TREATMENT ? 0 : 1, false, mySettings.KEEP_BLANK_LINES_IN_CODE);
      }
      if (type2 != LBRACE) {
        // Keep single-statement else-part on same line?
        int lf = mySettings.SPECIAL_ELSE_IF_TREATMENT ? 1 : 0;
        return Spacing.createSpacing(1, 1, lf, !BLOCKS.contains(type2) && mySettings.KEEP_LINE_BREAKS, 0);
      }
    }

    if (type1 == LBRACE && type2 == RBRACE) {
      // Empty class.
      if (elementType == CLASS_BODY && mySettings.KEEP_SIMPLE_CLASSES_IN_ONE_LINE) return noSpace();
      // Empty MAP_LITERAL_EXPRESSION or LIST_LITERAL_EXPRESSION.
      if (mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) return noSpace();
    }
    boolean isBraces = type1 == LBRACE || type2 == RBRACE;
    if ((isBraces &&
         elementType != OPTIONAL_FORMAL_PARAMETERS &&
         elementType != OPTIONAL_PARAMETER_TYPES &&
         elementType != MAP_LITERAL_EXPRESSION) ||
        BLOCKS_EXT.contains(type1) ||
        FUNCTION_DEFINITION.contains(type1)) {
      return addLineBreak();
    }
    if (COMMENTS.contains(type1)) {
      if (isBraces || type2 == SEMICOLON) {
        return addLineBreak();
      }
      if (parentType == DART_FILE &&
          FUNCTION_DEFINITION.contains(elementType) &&
          !(type1 == MULTI_LINE_COMMENT && type2 == COMPONENT_NAME)) {
        return addLineBreak();
      }
      if (type2 == RBRACKET && elementType != OPTIONAL_FORMAL_PARAMETERS) {
        return addLineBreak();
      }
      if (type2 == ARGUMENT_LIST || type2 == COMPONENT_NAME) {
        if (type1 == MULTI_LINE_COMMENT && isEmbeddedComment(type1, child1)) {
          if (!hasNewlineInText(node1)) {
            return addSingleSpaceIf(true);
          }
        }
        return addLineBreak();
      }
    }

    if ((elementType == INTERFACES || elementType == MIXINS) && type2 == TYPE_LIST) {
      return Spacing
        .createDependentLFSpacing(1, 1, myNode.getTextRange(), mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }
    if (elementType == TYPE_LIST && type2 == TYPE) {
      return Spacing.createDependentLFSpacing(1, 1, myNode.getTreeParent().getTextRange(), mySettings.KEEP_LINE_BREAKS,
                                              mySettings.KEEP_BLANK_LINES_IN_CODE);
    }

    if (type1 == LBRACKET && type2 == RBRACKET) {
      return noSpace();
    }
    if (type1 == COMMA && (elementType == FORMAL_PARAMETER_LIST || elementType == ARGUMENT_LIST)) {
      return addSingleSpaceIf(mySettings.SPACE_AFTER_COMMA);
    }

    if (type1 == COMMA) {
      if (type2 == RBRACKET) {
        TextRange range = myNode.getTextRange();
        return Spacing.createDependentLFSpacing(0, 0, range, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
      }
      return addSingleSpaceIf(mySettings.SPACE_AFTER_COMMA && type2 != RBRACE && type2 != RBRACKET);
    }

    if (type2 == COMMA) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_COMMA);
    }

    //todo: customize in settings

    if (type1 == EXPRESSION_BODY_DEF) { // =>
      if (type2 == STRING_LITERAL_EXPRESSION) {
        // We might want to add a check that the string contains a newline as in regression/0000/0036.unit
        return addSingleSpaceIf(true);
      }
      TextRange range = node2.getTextRange();
      return Spacing.createDependentLFSpacing(1, 1, range, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }
    if (type2 == EXPRESSION_BODY_DEF) {
      return addSingleSpaceIf(true);
    }

    if (type1 == FOR_LOOP_PARTS_IN_BRACES && !BLOCKS_EXT.contains(type2)) {
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

    if (COMMENTS.contains(type2)) {
      int forceSpace = 1;
      if (type2 == MULTI_LINE_COMMENT && (type1 == COMPONENT_NAME || type1 == TYPE || type1 == VAR || type1 == RETURN_TYPE)) {
        forceSpace = 0;
      }
      return Spacing.createSpacing(forceSpace, 1, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }

    if (TOKENS_WITH_SPACE_AFTER.contains(type1) || KEYWORDS_WITH_SPACE_BEFORE.contains(type2)) {
      return addSingleSpaceIf(true);
    }

    if (elementType == FOR_LOOP_PARTS && type1 == SEMICOLON) {
      return addSingleSpaceIf(true);
    }

    if (elementType == VALUE_EXPRESSION && type2 == CASCADE_REFERENCE_EXPRESSION) {
      if (type1 == CASCADE_REFERENCE_EXPRESSION) {
        if (cascadesAreSameMethod(((AbstractBlock)child1).getNode(), ((AbstractBlock)child2).getNode())) {
          return Spacing.createSpacing(0, 0, 0, false, 0);
        }
      }
      else if (type1 == REFERENCE_EXPRESSION || isSimpleLiteral(type1)) {
        CompositeElement elem = (CompositeElement)myNode;
        ASTNode[] childs = elem.getChildren(CASCADE_REFERENCE_EXPRESSION_SET);
        if (childs.length == 1) {
          return Spacing.createDependentLFSpacing(0, 0, myNode.getTextRange(), true, 0);
        }
        if (allCascadesAreSameMethod(childs)) {
          return Spacing.createSpacing(0, 0, 0, false, 0);
        }
      }
      else if (type1 == NEW_EXPRESSION && parentType == ARGUMENT_LIST) {
        return Spacing.createDependentLFSpacing(0, 0, myNode.getTextRange(), true, 0);
      }
      return addLineBreak();
    }

    if (type1 == CLOSING_QUOTE && type2 == OPEN_QUOTE && elementType == STRING_LITERAL_EXPRESSION) {
      ASTNode sib = node1;
      int preserveNewline = 0;
      // Adjacent strings on the same line should not be split.
      while ((sib = sib.getTreeNext()) != null) {
        // Comments are handled elsewhere.
        // TODO Create a test for this loop after adjacent-string wrapping is implemented.
        if (sib.getElementType() == WHITE_SPACE) {
          String ws = sib.getText();
          if (ws.contains("\n")) {
            preserveNewline++;
            break;
          }
          continue;
        }
        break;
      }
      // Adjacent strings on separate lines should not include blank lines.
      return Spacing.createSpacing(0, 1, preserveNewline, true, 0);
    }

    // Put the constructor colon on the next line unless only one initializer.
    if (type2 == INITIALIZERS) {
      if (hasMultipleInitializers(node2)) {
        return addSingleSpaceIf(false, true);
      }
      else {
        return addSingleSpaceIf(true, false);
      }
    }

    if (elementType == LIST_LITERAL_EXPRESSION && type2 == RBRACKET) {
      return Spacing.createDependentLFSpacing(0, 0, node1.getTextRange(), mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }

    if (elementType == NAMED_ARGUMENT || elementType == DEFAULT_FORMAL_NAMED_PARAMETER || elementType == MAP_LITERAL_ENTRY) {
      if (type1 == COLON) {
        return addSingleSpaceIf(true);
      }
      if (type2 == COLON) {
        return noSpace();
      }
    }
    if (elementType == TYPE_ARGUMENTS || elementType == TYPE_PARAMETERS) {
      if (type1 == LT || type2 == GT || type2 == LT || type1 == GT) {
        return noSpace(); // Might want a user setting to control space within type
      }
    }
    if (elementType == IS_EXPRESSION) {
      if (type1 == NOT) {
        return addSingleSpaceIf(true);
      }
      if (type2 == NOT) {
        return noSpace();
      }
    }
    if (type1 == TYPE_ARGUMENTS && (type2 == LBRACKET || type2 == LBRACE)) {
      return noSpace(); // Might want a user setting to control space before/after type
    }

    if (type2 == RBRACE && type1 == MAP_LITERAL_ENTRY) {
      return noSpace();
    }

    if (type2 == RBRACKET && type1 == LIST_LITERAL_EXPRESSION) {
      return noSpace();
    }

    if (type2 == RBRACKET && type1 == EXPRESSION_LIST) {
      return noSpace();
    }

    // Spacing in async functions.
    if (elementType == FUNCTION_BODY || elementType == FUNCTION_EXPRESSION_BODY) {
      if (type1 == ASYNC || type1 == SYNC) {
        if (type2 == MUL) return noSpace();
        return addSingleSpaceIf(true);
      }
      if (type1 == MUL) return addSingleSpaceIf(true);
    }

    if (elementType == REFERENCE_EXPRESSION && (type2 == DOT || type2 == QUEST_DOT)) {
      return createSpacingForCallChain(collectSurroundingMessageSends(), node2);
    }
    if (type1 == DOT || type1 == QUEST_DOT || type1 == HASH) {
      return noSpace(); // Seems odd that no plugin has a setting for spaces around DOT -- need a Lisp mode!
    }
    if (type2 == HASH) {
      return addSingleSpaceIf(parentType == SYMBOL_LITERAL_EXPRESSION); // No space before closurization.
    }

    if (type1 == RETURN && type2 != SEMICOLON) {
      return addSingleSpaceIf(true);
    }

    return Spacing.createSpacing(0, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
  }

  private static int getMinLineBreaksBetweenTopLevelNodes(@NotNull final IElementType type1, @NotNull final IElementType type2) {
    /*
       libraryStatement
     | partOfStatement
     | importStatement
     | exportStatement
     | partStatement
     | classDefinition
     | enumDefinition
     | functionTypeAlias
     | getterOrSetterDeclaration
     | functionDeclarationWithBodyOrNative
     | varDeclarationListWithSemicolon
     */
    if (type1 == LIBRARY_STATEMENT) return 2;
    if (type1 == PART_OF_STATEMENT) return 2;
    if (type1 == IMPORT_STATEMENT || type1 == EXPORT_STATEMENT) {
      if (type2 != IMPORT_STATEMENT && type2 != EXPORT_STATEMENT) return 2;
    }
    if (type1 == PART) return 2;
    if (type1 == CLASS_DEFINITION) return 2;
    if (type1 == ENUM_DEFINITION) return 2;
    if (type1 == GETTER_DECLARATION) return 2;
    if (type1 == SETTER_DECLARATION) return 2;
    if (type1 == FUNCTION_DECLARATION_WITH_BODY_OR_NATIVE) return 2;

    return 1;
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

  private Spacing noSpace() {
    return Spacing.createSpacing(0, 0, 0, mySettings.KEEP_LINE_BREAKS, 0);
  }

  private Spacing setBraceSpace(boolean needSpaceSetting,
                                @CommonCodeStyleSettings.BraceStyleConstant int braceStyleSetting,
                                TextRange textRange) {
    final int spaces = needSpaceSetting ? 1 : 0;
    if (braceStyleSetting == CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED && textRange != null) {
      return Spacing.createDependentLFSpacing(spaces, spaces, textRange, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }
    else {
      final int lineBreaks =
        braceStyleSetting == CommonCodeStyleSettings.END_OF_LINE || braceStyleSetting == CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED
        ? 0
        : 1;
      return Spacing.createSpacing(spaces, spaces, lineBreaks, false, 0);
    }
  }

  private static boolean doesMessageHaveArguments(ASTNode node) {
    // node is a DOT
    ASTNode parent = node.getTreeParent().getTreeParent();
    if (parent == null) return false;
    if (parent.getElementType() != CALL_EXPRESSION) return false;
    ASTNode args = parent.getLastChildNode();
    if (args == null) return false;
    return args.getElementType() == ARGUMENTS;
  }

  private static Comparator<ASTNode> textRangeSorter() {
    return Comparator.comparingInt(o -> o.getTextRange().getStartOffset());
  }

  private CallChain collectSurroundingMessageSends() {
    CallChain calls = new CallChain();
    collectPredecessorMessageSends(calls);
    collectSuccessorMessageSends(calls);
    return calls;
  }

  private void collectPredecessorMessageSends(CallChain calls) {
    ASTNode node = myNode;
    while (node != null) {
      IElementType type = node.getElementType();
      if (type == REFERENCE_EXPRESSION) {
        collectDotIfMessageSend(calls, node);
        node = node.getTreeParent();
      }
      else if (type == CALL_EXPRESSION) {
        if (hasMultilineFunctionArgument(node)) {
          calls.isFollowedByHardNewline = true;
          break;
        }
        node = node.getTreeParent();
      }
      else {
        break;
      }
    }
  }

  private void collectSuccessorMessageSends(CallChain calls) {
    ASTNode node = myNode;
    while (node != null) {
      IElementType type = node.getElementType();
      if (type == CALL_EXPRESSION) {
        if (hasMultilineFunctionArgument(node)) {
          calls.isPrecededByHardNewline = true;
          break;
        }
        node = node.getFirstChildNode();
      }
      else if (type == REFERENCE_EXPRESSION) {
        collectDotIfMessageSend(calls, node);
        node = node.getFirstChildNode();
      }
      else {
        break;
      }
    }
  }

  private static void collectDotIfMessageSend(CallChain calls, ASTNode node) {
    ASTNode child = node.getFirstChildNode();
    child = FormatterUtil.getNextNonWhitespaceSibling(child);
    if (child != null) {
      IElementType childType = child.getElementType();
      if (childType == DOT || childType == QUEST_DOT || childType == HASH) {
        calls.add(child);
      }
    }
  }

  private static boolean hasMultilineFunctionArgument(ASTNode node) {
    ASTNode args = node.getLastChildNode();
    ASTNode first = args == null ? null : args.getFirstChildNode();
    args = first == null ? null : first.getTreeNext();
    if (args != null && args.getElementType() == ARGUMENT_LIST) {
      ASTNode arg = args.getFirstChildNode();
      int n = 1;
      while (arg != null) {
        // TODO Max 9 args is totally arbitrary, possibly not even desirable.
        if (n++ == 10 || arg.getElementType() == FUNCTION_EXPRESSION) {
          if (arg.getText().indexOf('\n') >= 0) {
            return true;
          }
        }
        arg = arg.getTreeNext();
      }
    }
    return false;
  }

  private Spacing createSpacingForCallChain(CallChain calls, ASTNode node2) {
    // The rules involving call chains, like m.a.b().c.d(), are complex.
    if (calls.list.size() < 2) {
      return noSpace();
    }
    //if (calls.isPrecededByHardNewline) {
    //  // Rule: allow an inline chain before a hard newline but not after.
    //  return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
    //}
    boolean isAllProperties = true;
    boolean mustSplit = false;
    boolean mustStopAtNextMethod = false;
    List<TextRange> ranges = new ArrayList<>();
    for (ASTNode node : calls.list) {
      if (doesMessageHaveArguments(node)) {
        if (mustStopAtNextMethod) {
          return Spacing.createDependentLFSpacing(0, 0, ranges, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
        }
        isAllProperties = false;
      }
      else {
        if (!isAllProperties) {
          // Rule: split properties in a method chain.
          mustSplit = true;
        }
      }
      TextRange range = node.getTextRange();
      ranges.add(new TextRange(range.getStartOffset() - 1, range.getEndOffset()));
      if (node2 == node && isAllProperties) {
        // Rule: do not split leading properties (unless too long to fit).
        mustStopAtNextMethod = true;
      }
    }
    // Not sure how to implement rule: split before all properties if they don't fit on two lines. TWO lines !?
    if (isAllProperties && ranges.size() > 7) {
      mustSplit = true;
    }
    if (mustSplit) {
      return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }
    else {
      return Spacing.createDependentLFSpacing(0, 0, ranges, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }
  }

  private TextRange textRangeFollowingMetadata() {
    TextRange range = myNode.getTextRange();
    ASTNode child = myNode.getFirstChildNode();
    if (child == null || child.getElementType() != METADATA) return range;
    while (child != null && (child.getElementType() == METADATA || child.getElementType() == WHITE_SPACE)) {
      child = child.getTreeNext();
    }
    if (child == null) return range; // Avoid nullable warning.
    return new TextRange(child.getTextRange().getStartOffset(), range.getEndOffset());
  }

  private static boolean allCascadesAreSameMethod(ASTNode[] children) {
    for (int i = 1; i < children.length; i++) {
      if (!cascadesAreSameMethod(children[i - 1], children[i])) {
        return false;
      }
    }
    return true;
  }

  private static boolean cascadesAreSameMethod(ASTNode child1, ASTNode child2) {
    ASTNode call1 = child1.getLastChildNode();
    if (call1.getElementType() == CALL_EXPRESSION) {
      ASTNode call2 = child2.getLastChildNode();
      if (call2.getElementType() == CALL_EXPRESSION) {
        String name1 = getImmediateCallName(call1);
        if (name1 != null) {
          String name2 = getImmediateCallName(call2);
          if (name1.equals(name2)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static String getImmediateCallName(ASTNode callNode) {
    ASTNode[] childs = callNode.getChildren(REFERENCE_EXPRESSION_SET);
    if (childs.length != 1) return null;
    ASTNode child = childs[0];
    childs = child.getChildren(ID_SET);
    if (childs.length != 1) return null;
    child = childs[0];
    return child.getText();
  }

  private static boolean isSpaceNeededBetweenPrefixOps(ASTNode node1, ASTNode node2) {
    String op1 = node1.getText();
    String op2 = node2.getText();
    return op1.endsWith(op2.substring(op2.length() - 1));
  }

  private static boolean isSimpleLiteral(IElementType nodeType) {
    // Literals that can be cascade receivers, excluding map and list.
    return SIMPLE_LITERAL_SET.contains(nodeType);
  }

  private static boolean needsBlankLineBeforeFunction(IElementType elementType) {
    return elementType == DART_FILE ||
           elementType == CLASS_MEMBERS ||
           elementType instanceof DartEmbeddedContentElementType;
  }

  private static boolean isEmbeddedComment(IElementType type, Block block) {
    return COMMENTS.contains(type) && (!isDirectlyPrecededByNewline(block) || isDirectlyPrecededByBlockComment(block));
  }

  private static boolean isDirectlyPrecededByNewline(Block child) {
    // The child is a line comment whose parent is the DART_FILE.
    // Return true if it is (or will be) at the beginning of the line, or
    // following a block comment that is at the beginning of the line.
    ASTNode node = ((DartBlock)child).getNode();
    return isDirectlyPrecededByNewline(node);
  }

  private static boolean isDirectlyPrecededByNewline(ASTNode node) {
    while ((node = node.getTreePrev()) != null) {
      if (node.getElementType() == WHITE_SPACE) {
        if (node.getText().contains("\n")) return true;
        continue;
      }
      if (node.getElementType() == MULTI_LINE_COMMENT) {
        if (node.getTreePrev() == null) {
          return true;
        }
        continue;
      }
      break;
    }
    return false;
  }

  @Nullable
  private static ASTNode getPrevSiblingOnTheSameLineSkipCommentsAndWhitespace(@NotNull ASTNode node) {
    while ((node = node.getTreePrev()) != null) {
      if (node.getElementType() == WHITE_SPACE || COMMENTS.contains(node.getElementType())) {
        if (node.getText().contains("\n")) {
          return null;
        }
        else {
          continue;
        }
      }
      return node;
    }

    return null;
  }

  private static boolean isDirectlyPrecededByBlockComment(Block child) {
    ASTNode node = ((DartBlock)child).getNode();
    return isDirectlyPrecededByBlockComment(node);
  }

  private static boolean isDirectlyPrecededByBlockComment(ASTNode node) {
    while ((node = node.getTreePrev()) != null) {
      if (node.getElementType() == WHITE_SPACE) {
        if (node.getText().contains("\n")) return false;
        continue;
      }
      if (node.getElementType() == MULTI_LINE_COMMENT) {
        return true;
      }
      break;
    }
    return false;
  }

  private static boolean isScriptTag(Block child) {
    // The VM accepts any kind of whtespace prior to #! even though unix shells do not.
    ASTNode node = ((DartBlock)child).getNode();
    if (!node.getText().trim().startsWith("#!")) return false;
    while ((node = node.getTreePrev()) != null) {
      if (node.getElementType() != WHITE_SPACE) return false;
    }
    return true;
  }

  public static boolean hasMultipleInitializers(ASTNode node) {
    return FormatterUtil.isPrecededBy(node.getLastChildNode(), SUPER_CALL_OR_FIELD_INITIALIZER, SKIP_COMMA);
  }

  // dart_style recognizes three forms of "empty block":
  //   e() {}
  //   f() => expr;
  //   M() : super();
  private static boolean hasEmptyBlock(ASTNode node) {
    if (node.getElementType() == CLASS_DEFINITION) return false;
    ASTNode child = node;
    while (true) {
      child = child.getLastChildNode();
      if (child == null) return false;
      if (child.getElementType() == WHITE_SPACE) child = FormatterUtil.getPreviousNonWhitespaceSibling(child);
      if (child == null) return false;
      if (child.getElementType() == FUNCTION_BODY) {
        ASTNode next = child.getLastChildNode();
        if (next.getElementType() == WHITE_SPACE) next = FormatterUtil.getPreviousNonWhitespaceSibling(next);
        if (next != null && next.getElementType() == SEMICOLON) {
          next = FormatterUtil.getPreviousNonWhitespaceSibling(next);
          if (next != null && DartIndentProcessor.EXPRESSIONS.contains(next.getElementType())) {
            ASTNode arrow = FormatterUtil.getPreviousNonWhitespaceSibling(next);
            if (arrow != null && arrow.getElementType() == EXPRESSION_BODY_DEF) {
              return true;
            }
          }
          return false;
        }
        // Look inside the function body.
        continue;
      }
      if (child.getElementType() == SEMICOLON) {
        ASTNode prev = FormatterUtil.getPreviousNonWhitespaceSibling(child);
        return (prev != null && prev.getElementType() == INITIALIZERS);
      }
      if (!BLOCKS.contains(child.getElementType())) continue;
      child = child.getLastChildNode();
      if (child == null) return false;
      if (child.getElementType() == WHITE_SPACE) child = FormatterUtil.getPreviousNonWhitespaceSibling(child);
      if (child == null) return false;
      if (child.getElementType() != RBRACE) return false;
      child = FormatterUtil.getPreviousNonWhitespaceSibling(child);
      return child != null && child.getElementType() == LBRACE;
    }
  }

  private static boolean hasNewlineInText(ASTNode node) {
    String comment = node.getText();
    return comment.indexOf('\n') > 0;
  }

  private static boolean isBlankLineAfterComment(ASTNode node) {
    // Assumes whitespace has been normalized.
    ASTNode next = node.getTreeNext();
    if (next == null || next.getElementType() != WHITE_SPACE) return false;
    String comment = next.getText();
    int n = comment.indexOf('\n');
    return comment.indexOf('\n', n + 1) > 0;
  }

  private static class CallChain {
    SortedList<ASTNode> list = new SortedList<>(textRangeSorter());
    boolean isPrecededByHardNewline = false;
    boolean isFollowedByHardNewline = false;

    void add(ASTNode node) {
      if (!list.contains(node)) {
        list.add(node);
      }
    }
  }
}
