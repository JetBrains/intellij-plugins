package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.Indent;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.jetbrains.lang.dart.util.UsefulPsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartTokenTypesSets.*;

public class DartIndentProcessor {

  public static final TokenSet EXPRESSIONS = TokenSet
    .create(ADDITIVE_EXPRESSION, ARRAY_ACCESS_EXPRESSION, ASSIGN_EXPRESSION, AS_EXPRESSION, AWAIT_EXPRESSION, BITWISE_EXPRESSION,
            CALL_EXPRESSION, CASCADE_REFERENCE_EXPRESSION, COMPARE_EXPRESSION, EXPRESSION, FUNCTION_EXPRESSION, IS_EXPRESSION,
            LIBRARY_COMPONENT_REFERENCE_EXPRESSION, LIST_LITERAL_EXPRESSION, LITERAL_EXPRESSION, LOGIC_AND_EXPRESSION, LOGIC_OR_EXPRESSION,
            MAP_LITERAL_EXPRESSION, MULTIPLICATIVE_EXPRESSION, NEW_EXPRESSION, PARAMETER_NAME_REFERENCE_EXPRESSION,
            PARENTHESIZED_EXPRESSION, PREFIX_EXPRESSION, REFERENCE_EXPRESSION, SHIFT_EXPRESSION, STRING_LITERAL_EXPRESSION,
            SUFFIX_EXPRESSION, SUPER_EXPRESSION, SYMBOL_LITERAL_EXPRESSION, TERNARY_EXPRESSION, THIS_EXPRESSION, THROW_EXPRESSION,
            VALUE_EXPRESSION, IF_NULL_EXPRESSION);

  private final CommonCodeStyleSettings settings;

  public DartIndentProcessor(CommonCodeStyleSettings settings) {
    this.settings = settings;
  }

  public Indent getChildIndent(final ASTNode node) {
    final IElementType elementType = node.getElementType();
    final ASTNode prevSibling = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(node);
    final IElementType prevSiblingType = prevSibling == null ? null : prevSibling.getElementType();
    final ASTNode parent = node.getTreeParent();
    final IElementType parentType = parent != null ? parent.getElementType() : null;
    final ASTNode superParent = parent == null ? null : parent.getTreeParent();
    final IElementType superParentType = superParent == null ? null : superParent.getElementType();

    final int braceStyle = superParentType == FUNCTION_BODY ? settings.METHOD_BRACE_STYLE : settings.BRACE_STYLE;

    if (parent == null || parent.getTreeParent() == null || parentType == EMBEDDED_CONTENT) {
      return Indent.getNoneIndent();
    }

    if (elementType == MULTI_LINE_COMMENT_BODY) {
      return Indent.getContinuationIndent();
    }
    if (elementType == DOC_COMMENT_LEADING_ASTERISK || elementType == MULTI_LINE_COMMENT_END) {
      return Indent.getSpaceIndent(1, true);
    }

    if (settings.KEEP_FIRST_COLUMN_COMMENT && (elementType == SINGLE_LINE_COMMENT || elementType == MULTI_LINE_COMMENT)) {
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

    if (parentType == MAP_LITERAL_EXPRESSION || parentType == LIST_LITERAL_EXPRESSION) {
      if (elementType == LBRACE || elementType == RBRACE || elementType == LBRACKET || elementType == RBRACKET) {
        return Indent.getNoneIndent();
      }
      if (elementType == TYPE_ARGUMENTS) {
        return Indent.getNoneIndent();
      }
      // Be careful to preserve typing behavior.
      if (elementType == MAP_LITERAL_ENTRY || elementType == EXPRESSION_LIST || elementType == COMMA) {
        return Indent.getNormalIndent();
      }
      if (COMMENTS.contains(elementType)) {
        return Indent.getNormalIndent();
      }
      return Indent.getNoneIndent();
    }

    if (elementType == LBRACE || elementType == RBRACE) {
      switch (braceStyle) {
        case CommonCodeStyleSettings.END_OF_LINE:
          if (elementType == LBRACE && FormatterUtil.isPrecededBy(parent, SINGLE_LINE_COMMENT, WHITE_SPACE)) {
            // Use Nystrom style rather than Allman.
            return Indent.getContinuationIndent();
          } // FALL THROUGH
        case CommonCodeStyleSettings.NEXT_LINE:
        case CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED:
          return Indent.getNoneIndent();
        case CommonCodeStyleSettings.NEXT_LINE_SHIFTED:
        case CommonCodeStyleSettings.NEXT_LINE_SHIFTED2:
          return Indent.getNormalIndent();
        default:
          return Indent.getNoneIndent();
      }
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
    if (parentType == BLOCK) {
      final PsiElement psi = node.getPsi();
      if (psi.getParent() instanceof PsiFile) {
        return Indent.getNoneIndent();
      }
      return Indent.getNormalIndent();
    }
    if (parentType == ARGUMENT_LIST) {
      // TODO In order to handle some dart_style examples we need to set indent for each arg.
      // The formatter does not appear to honor indent on individual argument expressions
      // when KEEP_LINE_BREAKS==true. That means two DartFormatterTest tests fail. Those tests
      // have been changed since the recommended setting for Dart programming is
      // KEEP_LINE_BREAKS==false. The two tests are testAlignment() and testWrappingMeth().
      if (EXPRESSIONS.contains(elementType) && elementType != FUNCTION_EXPRESSION) {
        return Indent.getContinuationIndent();
      }
    }
    if (parentType == FORMAL_PARAMETER_LIST) {
      return Indent.getContinuationIndent();
    }
    if (parentType == FOR_STATEMENT && prevSiblingType == FOR_LOOP_PARTS_IN_BRACES && elementType != BLOCK) {
      return Indent.getNormalIndent();
    }
    if (parentType == SWITCH_STATEMENT && (elementType == SWITCH_CASE || elementType == DEFAULT_CASE)) {
      return Indent.getNormalIndent();
    }
    if ((parentType == SWITCH_CASE || parentType == DEFAULT_CASE) && elementType == STATEMENTS) {
      return Indent.getNormalIndent();
    }
    if (parentType == WHILE_STATEMENT && prevSiblingType == RPAREN && elementType != BLOCK) {
      return Indent.getNormalIndent();
    }
    if (parentType == DO_WHILE_STATEMENT && prevSiblingType == DO && elementType != BLOCK) {
      return Indent.getNormalIndent();
    }
    if ((parentType == RETURN_STATEMENT) &&
        prevSiblingType == RETURN &&
        elementType != BLOCK) {
      return Indent.getNormalIndent();
    }
    if (parentType == IF_STATEMENT && elementType != BLOCK &&
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
    if (elementType == NAMED_ARGUMENT) {
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
        FormatterUtil.isPrecededBy(node, ASSIGNMENT_OPERATOR)) {
      return Indent.getContinuationIndent();
    }
    if (elementType == VAR_DECLARATION_LIST_PART) {
      return Indent.getContinuationIndent();
    }

    if (elementType == SUPER_CALL_OR_FIELD_INITIALIZER) {
      return Indent.getContinuationIndent();
    }
    if (parentType == SUPER_CALL_OR_FIELD_INITIALIZER && elementType != COLON) {
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

    if (elementType == OPEN_QUOTE && parentType == STRING_LITERAL_EXPRESSION && superParentType == VAR_INIT) {
      return Indent.getContinuationIndent();
    }

    return Indent.getNoneIndent();
  }

  private static boolean isBetweenBraces(@NotNull final ASTNode node) {
    final IElementType elementType = node.getElementType();
    if (elementType == LBRACE || elementType == RBRACE) return false;

    for (ASTNode sibling = node.getTreePrev(); sibling != null; sibling = sibling.getTreePrev()) {
      if (sibling.getElementType() == LBRACE) return true;
    }

    return false;
  }
}
