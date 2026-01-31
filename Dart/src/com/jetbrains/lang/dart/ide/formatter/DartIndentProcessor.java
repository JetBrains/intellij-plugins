// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.Indent;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.jetbrains.lang.dart.util.UsefulPsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.lang.dart.DartTokenTypes.ADDITIVE_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.ARGUMENTS;
import static com.jetbrains.lang.dart.DartTokenTypes.ARGUMENT_LIST;
import static com.jetbrains.lang.dart.DartTokenTypes.ARRAY_ACCESS_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.ASSIGNMENT_OPERATOR;
import static com.jetbrains.lang.dart.DartTokenTypes.ASSIGN_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.AS_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.AWAIT_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.BITWISE_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.CALL_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.CASCADE_REFERENCE_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.CLASS_BODY;
import static com.jetbrains.lang.dart.DartTokenTypes.CLASS_DEFINITION;
import static com.jetbrains.lang.dart.DartTokenTypes.CLASS_MEMBERS;
import static com.jetbrains.lang.dart.DartTokenTypes.CLOSING_QUOTE;
import static com.jetbrains.lang.dart.DartTokenTypes.COLON;
import static com.jetbrains.lang.dart.DartTokenTypes.COMMA;
import static com.jetbrains.lang.dart.DartTokenTypes.COMPARE_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.DEFAULT_CASE;
import static com.jetbrains.lang.dart.DartTokenTypes.DO;
import static com.jetbrains.lang.dart.DartTokenTypes.DOT;
import static com.jetbrains.lang.dart.DartTokenTypes.DO_WHILE_STATEMENT;
import static com.jetbrains.lang.dart.DartTokenTypes.ELEMENT;
import static com.jetbrains.lang.dart.DartTokenTypes.ELSE;
import static com.jetbrains.lang.dart.DartTokenTypes.ENUM_DEFINITION;
import static com.jetbrains.lang.dart.DartTokenTypes.EQ;
import static com.jetbrains.lang.dart.DartTokenTypes.EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.EXPRESSION_BODY_DEF;
import static com.jetbrains.lang.dart.DartTokenTypes.FORMAL_PARAMETER_LIST;
import static com.jetbrains.lang.dart.DartTokenTypes.FOR_ELEMENT;
import static com.jetbrains.lang.dart.DartTokenTypes.FOR_LOOP_PARTS_IN_BRACES;
import static com.jetbrains.lang.dart.DartTokenTypes.FOR_STATEMENT;
import static com.jetbrains.lang.dart.DartTokenTypes.FUNCTION_BODY;
import static com.jetbrains.lang.dart.DartTokenTypes.FUNCTION_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.HIDE_COMBINATOR;
import static com.jetbrains.lang.dart.DartTokenTypes.IF_ELEMENT;
import static com.jetbrains.lang.dart.DartTokenTypes.IF_NULL_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.IF_STATEMENT;
import static com.jetbrains.lang.dart.DartTokenTypes.INTERFACES;
import static com.jetbrains.lang.dart.DartTokenTypes.IS_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.LBRACE;
import static com.jetbrains.lang.dart.DartTokenTypes.LBRACKET;
import static com.jetbrains.lang.dart.DartTokenTypes.LIBRARY_COMPONENT_REFERENCE_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.LIBRARY_NAME_ELEMENT;
import static com.jetbrains.lang.dart.DartTokenTypes.LIST_LITERAL_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.LITERAL_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.LOGIC_AND_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.LOGIC_OR_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.LONG_TEMPLATE_ENTRY;
import static com.jetbrains.lang.dart.DartTokenTypes.LPAREN;
import static com.jetbrains.lang.dart.DartTokenTypes.METADATA;
import static com.jetbrains.lang.dart.DartTokenTypes.MIXINS;
import static com.jetbrains.lang.dart.DartTokenTypes.MIXIN_APPLICATION;
import static com.jetbrains.lang.dart.DartTokenTypes.MULTIPLICATIVE_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.NEW_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.OPEN_QUOTE;
import static com.jetbrains.lang.dart.DartTokenTypes.OPTIONAL_FORMAL_PARAMETERS;
import static com.jetbrains.lang.dart.DartTokenTypes.PARAMETER_NAME_REFERENCE_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.PARAMETER_TYPE_LIST;
import static com.jetbrains.lang.dart.DartTokenTypes.PARENTHESIZED_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.PREFIX_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.QUEST;
import static com.jetbrains.lang.dart.DartTokenTypes.QUEST_DOT;
import static com.jetbrains.lang.dart.DartTokenTypes.RAW_SINGLE_QUOTED_STRING;
import static com.jetbrains.lang.dart.DartTokenTypes.RBRACE;
import static com.jetbrains.lang.dart.DartTokenTypes.RBRACKET;
import static com.jetbrains.lang.dart.DartTokenTypes.REFERENCE_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.RETURN;
import static com.jetbrains.lang.dart.DartTokenTypes.RETURN_STATEMENT;
import static com.jetbrains.lang.dart.DartTokenTypes.RPAREN;
import static com.jetbrains.lang.dart.DartTokenTypes.SEMICOLON;
import static com.jetbrains.lang.dart.DartTokenTypes.SET_OR_MAP_LITERAL_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.SHIFT_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.SHOW_COMBINATOR;
import static com.jetbrains.lang.dart.DartTokenTypes.STATEMENTS;
import static com.jetbrains.lang.dart.DartTokenTypes.STRING_LITERAL_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.SUFFIX_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.SUPERCLASS;
import static com.jetbrains.lang.dart.DartTokenTypes.SUPER_CALL_OR_FIELD_INITIALIZER;
import static com.jetbrains.lang.dart.DartTokenTypes.SUPER_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.SWITCH_CASE;
import static com.jetbrains.lang.dart.DartTokenTypes.SWITCH_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.SWITCH_EXPRESSION_CASE;
import static com.jetbrains.lang.dart.DartTokenTypes.SWITCH_STATEMENT;
import static com.jetbrains.lang.dart.DartTokenTypes.SYMBOL_LITERAL_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.TERNARY_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.THIS_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.THROW_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.TYPE;
import static com.jetbrains.lang.dart.DartTokenTypes.TYPE_ARGUMENTS;
import static com.jetbrains.lang.dart.DartTokenTypes.TYPE_LIST;
import static com.jetbrains.lang.dart.DartTokenTypes.VALUE_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.VAR_DECLARATION_LIST_PART;
import static com.jetbrains.lang.dart.DartTokenTypes.VAR_INIT;
import static com.jetbrains.lang.dart.DartTokenTypes.WHILE_STATEMENT;
import static com.jetbrains.lang.dart.DartTokenTypesSets.BINARY_EXPRESSIONS;
import static com.jetbrains.lang.dart.DartTokenTypesSets.BLOCKS;
import static com.jetbrains.lang.dart.DartTokenTypesSets.COMMENTS;
import static com.jetbrains.lang.dart.DartTokenTypesSets.DOC_COMMENT_LEADING_ASTERISK;
import static com.jetbrains.lang.dart.DartTokenTypesSets.EMBEDDED_CONTENT;
import static com.jetbrains.lang.dart.DartTokenTypesSets.MULTI_LINE_COMMENT;
import static com.jetbrains.lang.dart.DartTokenTypesSets.MULTI_LINE_COMMENT_BODY;
import static com.jetbrains.lang.dart.DartTokenTypesSets.MULTI_LINE_COMMENT_END;
import static com.jetbrains.lang.dart.DartTokenTypesSets.SINGLE_LINE_COMMENT;
import static com.jetbrains.lang.dart.DartTokenTypesSets.WHITE_SPACE;

public class DartIndentProcessor {

  public static final TokenSet EXPRESSIONS = TokenSet
    .create(ADDITIVE_EXPRESSION, ARRAY_ACCESS_EXPRESSION, ASSIGN_EXPRESSION, AS_EXPRESSION, AWAIT_EXPRESSION, BITWISE_EXPRESSION,
            CALL_EXPRESSION, CASCADE_REFERENCE_EXPRESSION, COMPARE_EXPRESSION, EXPRESSION, FUNCTION_EXPRESSION, IS_EXPRESSION,
            LIBRARY_COMPONENT_REFERENCE_EXPRESSION, LIST_LITERAL_EXPRESSION, LITERAL_EXPRESSION, LOGIC_AND_EXPRESSION, LOGIC_OR_EXPRESSION,
            SET_OR_MAP_LITERAL_EXPRESSION, MULTIPLICATIVE_EXPRESSION, NEW_EXPRESSION, PARAMETER_NAME_REFERENCE_EXPRESSION,
            PARENTHESIZED_EXPRESSION, PREFIX_EXPRESSION, REFERENCE_EXPRESSION, SHIFT_EXPRESSION, STRING_LITERAL_EXPRESSION,
            SUFFIX_EXPRESSION, SUPER_EXPRESSION, SYMBOL_LITERAL_EXPRESSION, TERNARY_EXPRESSION, THIS_EXPRESSION, THROW_EXPRESSION,
            VALUE_EXPRESSION, IF_NULL_EXPRESSION);

  public Indent getChildIndent(final ASTNode node) {
    final IElementType elementType = node.getElementType();
    final ASTNode prevSibling = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(node);
    final IElementType prevSiblingType = prevSibling == null ? null : prevSibling.getElementType();
    final ASTNode parent = node.getTreeParent();
    final IElementType parentType = parent != null ? parent.getElementType() : null;
    final ASTNode superParent = parent == null ? null : parent.getTreeParent();
    final IElementType superParentType = superParent == null ? null : superParent.getElementType();

    if (parent == null || parent.getTreeParent() == null || parentType == EMBEDDED_CONTENT) {
      return Indent.getNoneIndent();
    }

    if (elementType == MULTI_LINE_COMMENT_BODY) {
      return Indent.getContinuationIndent();
    }
    if (elementType == DOC_COMMENT_LEADING_ASTERISK || elementType == MULTI_LINE_COMMENT_END) {
      return Indent.getSpaceIndent(1, true);
    }

    if (elementType == SINGLE_LINE_COMMENT || elementType == MULTI_LINE_COMMENT) {
      final ASTNode previousNode = node.getTreePrev();
      if (previousNode != null && previousNode.getElementType() == WHITE_SPACE && previousNode.getText().endsWith("\n")) {
        return Indent.getAbsoluteNoneIndent();
      }
    }

    if (COMMENTS.contains(elementType) && prevSiblingType == LBRACE && parentType == CLASS_BODY) {
      return Indent.getNormalIndent();
    }

    if (parentType == ENUM_DEFINITION && isBetweenBraces(node)) {
      // instead of isBetweenBraces(node) we can parse enum block as a separate ASTNode, or build formatter blocks not tied to AST.
      return Indent.getNormalIndent();
    }

    if (parentType == SET_OR_MAP_LITERAL_EXPRESSION || parentType == LIST_LITERAL_EXPRESSION) {
      if (elementType == LBRACE || elementType == RBRACE || elementType == LBRACKET || elementType == RBRACKET) {
        return Indent.getNoneIndent();
      }
      if (elementType == TYPE_ARGUMENTS) {
        return Indent.getNoneIndent();
      }
      // Be careful to preserve typing behavior.
      if (elementType == ELEMENT || elementType == COMMA) {
        return Indent.getNormalIndent();
      }
      if (COMMENTS.contains(elementType)) {
        return Indent.getNormalIndent();
      }
      return Indent.getNoneIndent();
    }

    if (elementType == LBRACE || elementType == RBRACE) {
      if (elementType == LBRACE && FormatterUtil.isPrecededBy(parent, SINGLE_LINE_COMMENT, WHITE_SPACE)) {
        // Use Nystrom style rather than Allman.
        return Indent.getContinuationIndent();
      }
      return Indent.getNoneIndent();
    }

    if (parentType == PARENTHESIZED_EXPRESSION) {
      if (elementType == LPAREN || elementType == RPAREN) {
        return Indent.getNoneIndent();
      }
      return Indent.getContinuationIndent();
    }

    if (elementType == CLASS_MEMBERS) {
      return Indent.getNormalIndent();
    }
    if (BLOCKS.contains(parentType)) {
      final PsiElement psi = node.getPsi();
      if (psi.getParent() instanceof PsiFile) {
        return Indent.getNoneIndent();
      }
      return Indent.getNormalIndent();
    }
    if (elementType == LPAREN && (superParentType == METADATA || parentType == ARGUMENTS)) {
      return Indent.getNormalIndent();
    }
    if (parentType == ARGUMENTS) {
      if (COMMENTS.contains(elementType)) {
        return Indent.getNormalIndent();
      }
      return Indent.getNoneIndent();
    }
    if (parentType == ARGUMENT_LIST) {
      // see https://github.com/dart-lang/dart_style/issues/551
      return parent.getLastChildNode().getElementType() == COMMA ? Indent.getNormalIndent() : Indent.getContinuationIndent();
    }
    if (parentType == FORMAL_PARAMETER_LIST || parentType == PARAMETER_TYPE_LIST) {
      return Indent.getContinuationIndent();
    }
    if (parentType == OPTIONAL_FORMAL_PARAMETERS && elementType != LBRACKET && elementType != RBRACKET) {
      return Indent.getNormalIndent();
    }
    if (parentType == FOR_STATEMENT && prevSiblingType == FOR_LOOP_PARTS_IN_BRACES && !BLOCKS.contains(elementType)) {
      return Indent.getNormalIndent();
    }
    if (parentType == SWITCH_STATEMENT && (elementType == SWITCH_CASE || elementType == DEFAULT_CASE)) {
      return Indent.getNormalIndent();
    }
    if (parentType == SWITCH_EXPRESSION && elementType == SWITCH_EXPRESSION_CASE) {
      return Indent.getNormalIndent();
    }
    if ((parentType == SWITCH_CASE || parentType == DEFAULT_CASE) && elementType == STATEMENTS) {
      return Indent.getNormalIndent();
    }
    if (parentType == WHILE_STATEMENT && prevSiblingType == RPAREN && !BLOCKS.contains(elementType)) {
      return Indent.getNormalIndent();
    }
    if (parentType == DO_WHILE_STATEMENT && prevSiblingType == DO && !BLOCKS.contains(elementType)) {
      return Indent.getNormalIndent();
    }
    if ((parentType == RETURN_STATEMENT) &&
        prevSiblingType == RETURN &&
        !BLOCKS.contains(elementType)) {
      return Indent.getNormalIndent();
    }
    if (parentType == IF_STATEMENT && !BLOCKS.contains(elementType) &&
        (prevSiblingType == RPAREN || (prevSiblingType == ELSE && elementType != IF_STATEMENT))) {
      return Indent.getNormalIndent();
    }
    if (elementType == CASCADE_REFERENCE_EXPRESSION) {
      return Indent.getNormalIndent();
    }
    if (elementType == OPEN_QUOTE && prevSiblingType == CLOSING_QUOTE && parentType == STRING_LITERAL_EXPRESSION) {
      return Indent.getContinuationIndent();
    }
    if (BINARY_EXPRESSIONS.contains(parentType) && prevSibling != null) {
      return Indent.getContinuationIndent();
    }
    if (elementType == COLON || parentType == TERNARY_EXPRESSION && elementType == QUEST) {
      return Indent.getContinuationIndent();
    }
    if (elementType == HIDE_COMBINATOR || elementType == SHOW_COMBINATOR) {
      return Indent.getContinuationIndent();
    }
    if (parentType == FUNCTION_BODY) {
      if (FormatterUtil.isPrecededBy(node, EXPRESSION_BODY_DEF)) {
        return Indent.getContinuationIndent();
      }
    }
    if (elementType == CALL_EXPRESSION) {
      if (FormatterUtil.isPrecededBy(node, EXPRESSION_BODY_DEF)) {
        return Indent.getContinuationIndent();
      }
      if (FormatterUtil.isPrecededBy(node, ASSIGNMENT_OPERATOR)) {
        return Indent.getContinuationIndent();
      }
    }
    if ((elementType == REFERENCE_EXPRESSION || BINARY_EXPRESSIONS.contains(elementType)) &&
        (FormatterUtil.isPrecededBy(node, ASSIGNMENT_OPERATOR) || FormatterUtil.isPrecededBy(node, EQ))) {
      return Indent.getContinuationIndent();
    }
    if (elementType == VAR_DECLARATION_LIST_PART) {
      return Indent.getContinuationIndent();
    }

    if (elementType == SUPER_CALL_OR_FIELD_INITIALIZER) {
      return Indent.getContinuationIndent();
    }
    if (parentType == SUPER_CALL_OR_FIELD_INITIALIZER) {
      return Indent.getNormalIndent();
    }

    if (parentType == CLASS_DEFINITION) {
      if (elementType == SUPERCLASS || elementType == INTERFACES || elementType == MIXINS) {
        return Indent.getContinuationIndent();
      }
    }
    if (parentType == MIXIN_APPLICATION && elementType == MIXINS) {
      return Indent.getContinuationIndent();
    }

    if (parentType == LIBRARY_NAME_ELEMENT) {
      return Indent.getContinuationIndent();
    }

    if (elementType == SEMICOLON && FormatterUtil.isPrecededBy(node, SINGLE_LINE_COMMENT, WHITE_SPACE)) {
      return Indent.getContinuationIndent();
    }

    if (elementType == DOT || elementType == QUEST_DOT) {
      return Indent.getContinuationIndent();
    }

    if (parentType == TYPE_LIST && elementType == TYPE) {
      return Indent.getContinuationIndent();
    }

    if (elementType == OPEN_QUOTE && parentType == STRING_LITERAL_EXPRESSION && superParentType == VAR_INIT) {
      if (node.getText().length() < 3) {
        return Indent.getContinuationIndent();
      }
    }

    if (elementType == RAW_SINGLE_QUOTED_STRING && parentType == STRING_LITERAL_EXPRESSION && superParentType == VAR_INIT) {
      return Indent.getContinuationIndent();
    }

    if (parentType == LONG_TEMPLATE_ENTRY && EXPRESSIONS.contains(elementType)) {
      return Indent.getContinuationIndent();
    }

    if (parentType == FOR_ELEMENT || parentType == IF_ELEMENT) {
      if (elementType == ELEMENT) {
        return Indent.getNormalIndent();
      }
    }

    return Indent.getNoneIndent();
  }

  private static boolean isBetweenBraces(final @NotNull ASTNode node) {
    final IElementType elementType = node.getElementType();
    if (elementType == LBRACE || elementType == RBRACE) return false;

    for (ASTNode sibling = node.getTreePrev(); sibling != null; sibling = sibling.getTreePrev()) {
      if (sibling.getElementType() == LBRACE) return true;
    }

    return false;
  }
}
