package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.formatter.WrappingUtil;
import com.intellij.psi.tree.IElementType;

import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartTokenTypesSets.BINARY_EXPRESSIONS;
import static com.jetbrains.lang.dart.DartTokenTypesSets.BINARY_OPERATORS;

/**
 * @author: Fedor.Korotkov
 */
public class DartWrappingProcessor {
  private final ASTNode myNode;
  private final CommonCodeStyleSettings mySettings;

  public DartWrappingProcessor(ASTNode node, CommonCodeStyleSettings settings) {
    myNode = node;
    mySettings = settings;
  }

  Wrap createChildWrap(ASTNode child, Wrap defaultWrap, Wrap childWrap) {
    final IElementType childType = child.getElementType();
    final IElementType elementType = myNode.getElementType();
    if (childType == COMMA || childType == SEMICOLON) return defaultWrap;

    //
    // Function definition/call
    //
    if (elementType == ARGUMENT_LIST) {
      if (mySettings.CALL_PARAMETERS_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP) {
        if (myNode.getFirstChildNode() == child) {
          return createWrap(true); // Allow first arg to wrap.
        }
        if (!mySettings.PREFER_PARAMETERS_WRAP && childWrap != null) {
          // Not used; PREFER_PARAMETERS_WRAP cannot be changed in the UI.
          return Wrap.createChildWrap(childWrap, WrappingUtil.getWrapType(mySettings.CALL_PARAMETERS_WRAP), true);
        }
        return Wrap.createWrap(WrappingUtil.getWrapType(mySettings.CALL_PARAMETERS_WRAP), false);
      }
    }

    if (elementType == FORMAL_PARAMETER_LIST) {
      if (mySettings.METHOD_PARAMETERS_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP) {
        if (myNode.getFirstChildNode() == child) {
          return createWrap(mySettings.METHOD_PARAMETERS_LPAREN_ON_NEXT_LINE);
        }
        if (childType == RPAREN) {
          return createWrap(mySettings.METHOD_PARAMETERS_RPAREN_ON_NEXT_LINE);
        }
        return Wrap.createWrap(WrappingUtil.getWrapType(mySettings.METHOD_PARAMETERS_WRAP), true);
      }
    }

    //
    // Wrap after arrows.
    //
    if (elementType == FUNCTION_BODY) {
      if (FormatterUtil.isPrecededBy(child, EXPRESSION_BODY_DEF)) {
        return createWrap(true);
      }
    }
    if (childType == CALL_EXPRESSION) {
      if (FormatterUtil.isPrecededBy(child, EXPRESSION_BODY_DEF)) {
        return createWrap(true);
      }
      if (mySettings.CALL_PARAMETERS_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP) {
        if (childType == RPAREN) {
          return createWrap(mySettings.CALL_PARAMETERS_RPAREN_ON_NEXT_LINE);
        }
      }
    }

    //
    // If
    //
    if (elementType == IF_STATEMENT && childType == ELSE) {
      return createWrap(mySettings.ELSE_ON_NEW_LINE);
    }

    //
    //Binary expressions
    //
    if (BINARY_EXPRESSIONS.contains(elementType) && mySettings.BINARY_OPERATION_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP) {
      if ((mySettings.BINARY_OPERATION_SIGN_ON_NEXT_LINE && BINARY_OPERATORS.contains(childType)) ||
          (!mySettings.BINARY_OPERATION_SIGN_ON_NEXT_LINE && isRightOperand(child))) {
        return Wrap.createWrap(WrappingUtil.getWrapType(mySettings.BINARY_OPERATION_WRAP), true);
      }
    }

    //
    // Assignment
    //
    if (elementType == ASSIGN_EXPRESSION && mySettings.ASSIGNMENT_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP) {
      if (childType != ASSIGNMENT_OPERATOR) {
        if (FormatterUtil.isPrecededBy(child, ASSIGNMENT_OPERATOR) &&
            mySettings.PLACE_ASSIGNMENT_SIGN_ON_NEXT_LINE) {
          return Wrap.createWrap(WrapType.NONE, true);
        }
        return Wrap.createWrap(WrappingUtil.getWrapType(mySettings.ASSIGNMENT_WRAP), true);
      }
      else if (mySettings.PLACE_ASSIGNMENT_SIGN_ON_NEXT_LINE) {
        return Wrap.createWrap(WrapType.NORMAL, true);
      }
    }

    //
    // Ternary expressions
    //
    if (elementType == TERNARY_EXPRESSION) {
      if (myNode.getFirstChildNode() != child) {
        if (mySettings.TERNARY_OPERATION_SIGNS_ON_NEXT_LINE) {
          //if (!FormatterUtil.isPrecededBy(child, QUEST) &&
          //    !FormatterUtil.isPrecededBy(child, COLON)) {
          // TODO Allow ? and : to align on two continuation lines.
          if (childType == QUEST || childType == COLON) {
            return Wrap.createWrap(WrappingUtil.getWrapType(mySettings.TERNARY_OPERATION_WRAP), true);
          }
        }
        else if (childType != QUEST && childType != COLON) {
          return Wrap.createWrap(WrappingUtil.getWrapType(mySettings.TERNARY_OPERATION_WRAP), true);
        }
      }
      return Wrap.createWrap(WrapType.NONE, true);
    }

    if (childType == HIDE_COMBINATOR || childType == SHOW_COMBINATOR) {
      return createWrap(true);
    }

    return defaultWrap;
  }

  private boolean isRightOperand(ASTNode child) {
    return myNode.getLastChildNode() == child;
  }

  private static Wrap createWrap(boolean isNormal) {
    return Wrap.createWrap(isNormal ? WrapType.NORMAL : WrapType.NONE, true);
  }

  private static Wrap createChildWrap(ASTNode child, int parentWrap, boolean newLineAfterLBrace, boolean newLineBeforeRBrace) {
    IElementType childType = child.getElementType();
    if (childType != LPAREN && childType != RPAREN) {
      if (FormatterUtil.isPrecededBy(child, LBRACKET)) {
        if (newLineAfterLBrace) {
          return Wrap.createChildWrap(Wrap.createWrap(parentWrap, true), WrapType.ALWAYS, true);
        }
        else {
          return Wrap.createWrap(WrapType.NONE, true);
        }
      }
      return Wrap.createWrap(WrappingUtil.getWrapType(parentWrap), true);
    }
    if (childType == RBRACKET && newLineBeforeRBrace) {
      return Wrap.createWrap(WrapType.ALWAYS, true);
    }
    return Wrap.createWrap(WrapType.NONE, true);
  }
}
