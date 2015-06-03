package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.Indent;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.util.UsefulPsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartTokenTypesSets.*;

public class DartIndentProcessor {
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
      return Indent.getNormalIndent();
    }

    if (elementType == LBRACE || elementType == RBRACE) {
      switch (braceStyle) {
        case CommonCodeStyleSettings.END_OF_LINE:
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
    if (parentType == ARGUMENTS && elementType == ARGUMENT_LIST) {
      return Indent.getContinuationIndent();
    }
    if (parentType == ARGUMENT_LIST) {
      // TODO In order to handle some dart_style examples we need to set indent for each arg.
      // However, it conflicts with the previous statement, causing too much indent.
      // Removing the previous statement in favor of this one is actually a
      // net win, but breaks existing tests.
      //return Indent.getContinuationIndent();
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
    if (parentType == TERNARY_EXPRESSION && elementType == QUEST || elementType == COLON) {
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
