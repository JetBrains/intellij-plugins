// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.formatter;

import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.formatter.WrappingUtil;
import com.intellij.psi.tree.IElementType;

public class CfmlWrappingProcessor extends CfmlFormatterUtil {
  private final CommonCodeStyleSettings mySettings;
  private final ASTNode myNode;

  CfmlWrappingProcessor(ASTNode node, CommonCodeStyleSettings settings) {
    myNode = node;
    mySettings = settings;
  }

  Wrap createChildWrap(ASTNode child, Wrap defaultWrap, Wrap childWrap) {


    IElementType childType = child.getElementType();
    IElementType parentType = myNode.getElementType();
    if (childType == CfscriptTokenTypes.COMMA || childType == CfscriptTokenTypes.SEMICOLON) return defaultWrap;


    //
    // Function definition/call
    //
    if (parentType == CfmlElementTypes.PARAMETERS_LIST || parentType == CfmlElementTypes.ARGUMENT_LIST) {
      ASTNode superParent = myNode.getTreeParent();
      if (superParent != null) {
        if ((superParent.getElementType() == CfmlElementTypes.FUNCTION_CALL_EXPRESSION) &&
            mySettings.CALL_PARAMETERS_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP) {
          if (myNode.getFirstChildNode() == child) {
            if (mySettings.CALL_PARAMETERS_LPAREN_ON_NEXT_LINE) {
              return Wrap.createWrap(WrapType.NORMAL, true);
            }
            else {
              return Wrap.createWrap(WrapType.NONE, true);
            }
          }
          if (!mySettings.PREFER_PARAMETERS_WRAP && childWrap != null) {
            return Wrap.createChildWrap(childWrap, WrappingUtil.getWrapType(mySettings.CALL_PARAMETERS_WRAP), true);
          }
          return Wrap.createWrap(WrappingUtil.getWrapType(mySettings.CALL_PARAMETERS_WRAP), true);
        }
        if ((superParent.getElementType() == CfmlElementTypes.FUNCTION_DEFINITION) &&
            mySettings.METHOD_PARAMETERS_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP) {
          if (myNode.getFirstChildNode() == child) {
            if (mySettings.METHOD_PARAMETERS_LPAREN_ON_NEXT_LINE) {
              return Wrap.createWrap(WrapType.NORMAL, true);
            }
            else {
              return Wrap.createWrap(WrapType.NONE, true);
            }
          }
          return Wrap.createWrap(WrappingUtil.getWrapType(mySettings.METHOD_PARAMETERS_WRAP), true);
        }
      }
    }
    if (parentType == CfmlElementTypes.FUNCTION_CALL_EXPRESSION) {
      if (mySettings.CALL_PARAMETERS_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP) {
        if (childType == CfscriptTokenTypes.R_BRACKET) {
          return Wrap.createWrap(mySettings.CALL_PARAMETERS_RPAREN_ON_NEXT_LINE ? WrapType.NORMAL : WrapType.NONE, true);
        }
      }
    }


    //
    // For
    //
    if ((parentType == CfmlElementTypes.FOREXPRESSION) &&
        mySettings.FOR_STATEMENT_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP) {
      return createChildWrap(child, mySettings.FOR_STATEMENT_WRAP, mySettings.FOR_STATEMENT_LPAREN_ON_NEXT_LINE,
                             mySettings.FOR_STATEMENT_RPAREN_ON_NEXT_LINE);
    }
    //
    // If
    //
    if (parentType == CfmlElementTypes.IFEXPRESSION && childType == CfscriptTokenTypes.ELSE_KEYWORD) {
      return Wrap.createWrap(mySettings.ELSE_ON_NEW_LINE ? WrapType.NORMAL : WrapType.NONE, true);
    }


    //
    //Binary expressions
    //

    if (mySettings.BINARY_OPERATION_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP && CfmlElementTypes.BINARY_EXPRESSION == parentType) {
      if ((mySettings.BINARY_OPERATION_SIGN_ON_NEXT_LINE && CfmlFormatterUtil.isBinaryOperator(childType)) ||
          (!mySettings.BINARY_OPERATION_SIGN_ON_NEXT_LINE && isRightOperand(child))) {
        return Wrap.createWrap(WrappingUtil.getWrapType(mySettings.BINARY_OPERATION_WRAP), true);
      }
    }
    //
    // Assignment
    //
    if (mySettings.ASSIGNMENT_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP && (parentType == CfmlElementTypes.ASSIGNMENT)) {
      if (!CfmlFormatterUtil.isAssignmentOperator(childType)) {
        if (FormatterUtil.isPrecededBy(child, CfmlFormatterUtil.ASSIGNMENT_OPERATORS) &&
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
    if (parentType == CfmlElementTypes.TERNARY_EXPRESSION) {
      if (myNode.getFirstChildNode() != child) {
        if (mySettings.TERNARY_OPERATION_SIGNS_ON_NEXT_LINE) {
          if (!FormatterUtil.isPrecededBy(child, CfscriptTokenTypes.QUESTION) &&
              !FormatterUtil.isPrecededBy(child, CfscriptTokenTypes.DOTDOT)) {
            return Wrap.createWrap(WrappingUtil.getWrapType(mySettings.TERNARY_OPERATION_WRAP), true);
          }
        }
        else if (childType != CfscriptTokenTypes.QUESTION && childType != CfscriptTokenTypes.DOTDOT) {
          return Wrap.createWrap(WrappingUtil.getWrapType(mySettings.TERNARY_OPERATION_WRAP), true);
        }
      }
      return Wrap.createWrap(WrapType.NONE, true);
    }
    return defaultWrap;
  }

  private boolean isRightOperand(ASTNode child) {
    final ASTNode secondExpression = myNode.findChildByType(CfmlElementTypes.EXPRESSIONS,
                                                            myNode.findChildByType(CfmlFormatterUtil.BINARY_OPERATORS));
    if (child == secondExpression) return true;
    return false;
  }


  private static Wrap createChildWrap(ASTNode child, int parentWrap, boolean newLineAfterLBrace, boolean newLineBeforeRBrace) {
    IElementType childType = child.getElementType();
    if (childType != CfscriptTokenTypes.L_BRACKET && childType != CfscriptTokenTypes.R_BRACKET) {
      if (FormatterUtil.isPrecededBy(child, CfscriptTokenTypes.L_BRACKET)) {
        if (newLineAfterLBrace) {
          return Wrap.createChildWrap(Wrap.createWrap(parentWrap, true), WrapType.ALWAYS, true);
        }
        else {
          return Wrap.createWrap(WrapType.NONE, true);
        }
      }
      return Wrap.createWrap(WrappingUtil.getWrapType(parentWrap), true);
    }
    if (childType == CfscriptTokenTypes.R_BRACKET && newLineBeforeRBrace) {
      return Wrap.createWrap(WrapType.ALWAYS, true);
    }
    return Wrap.createWrap(WrapType.NONE, true);
  }
}
