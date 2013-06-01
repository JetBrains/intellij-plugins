package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.Indent;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.util.UsefulPsiTreeUtil;
import org.jetbrains.annotations.Nullable;

import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartTokenTypesSets.COMMENTS;
import static com.jetbrains.lang.dart.DartTokenTypesSets.EMBEDDED_CONTENT;

/**
 * @author: Fedor.Korotkov
 */
public class DartIndentProcessor {
  private final CommonCodeStyleSettings settings;

  public DartIndentProcessor(CommonCodeStyleSettings settings) {
    this.settings = settings;
  }

  public Indent getChildIndent(ASTNode node) {
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
    if (COMMENTS.contains(elementType) && settings.KEEP_FIRST_COLUMN_COMMENT) {
      return Indent.getAbsoluteNoneIndent();
    }
    if (COMMENTS.contains(elementType) &&
        prevSiblingType == LBRACE &&
        (parentType == CLASS_DEFINITION || parentType == INTERFACE_DEFINITION)) {
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
    if (parentType == MAP_LITERAL_EXPRESSION) {
      if (elementType == LBRACE || elementType == RBRACE) {
        return Indent.getNoneIndent();
      }
      return Indent.getContinuationIndent();
    }
    if (parentType == LIST_LITERAL_EXPRESSION) {
      if (elementType == LBRACKET || elementType == RBRACKET) {
        return Indent.getNoneIndent();
      }
      return Indent.getContinuationIndent();
    }
    if (needIndent(parentType)) {
      final PsiElement psi = node.getPsi();
      if (psi.getParent() instanceof PsiFile) {
        return Indent.getNoneIndent();
      }
      return Indent.getNormalIndent();
    }
    if (parentType == ARGUMENTS && elementType == ARGUMENT_LIST) {
      return Indent.getContinuationIndent();
    }
    if (parentType == FORMAL_PARAMETER_LIST) {
      return Indent.getContinuationIndent();
    }
    if (parentType == FOR_STATEMENT && prevSiblingType == FOR_LOOP_PARTS_IN_BRACES && elementType != BLOCK) {
      return Indent.getNormalIndent();
    }
    if (parentType == SWITCH_STATEMENT && prevSiblingType == RPAREN) {
      return Indent.getNormalIndent();
    }
    if (superParentType == SWITCH_CASE && parentType == STATEMENTS) {
      return Indent.getNormalIndent();
    }
    if (superParentType == DEFAULT_CASE && parentType == STATEMENTS) {
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
    if (parentType == IF_STATEMENT && (prevSiblingType == RPAREN || prevSiblingType == ELSE) && elementType != BLOCK) {
      return Indent.getNormalIndent();
    }
    return Indent.getNoneIndent();
  }

  private static boolean needIndent(@Nullable IElementType type) {
    if (type == null) {
      return false;
    }
    boolean result = type == BLOCK;
    result = result || type == CLASS_BODY;
    result = result || type == INTERFACE_BODY;
    result = result || type == SWITCH_CASE;
    result = result || type == DEFAULT_CASE;
    return result;
  }
}
