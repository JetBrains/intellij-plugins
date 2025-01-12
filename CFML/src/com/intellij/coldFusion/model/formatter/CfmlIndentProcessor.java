// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.formatter;

import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.coldFusion.model.psi.stubs.CfmlStubElementTypes;
import com.intellij.formatting.Indent;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.tree.IElementType;

public class CfmlIndentProcessor extends CfmlFormatterUtil {
  private final CommonCodeStyleSettings mySettings;
  private final int myIndentSize;

  CfmlIndentProcessor(CommonCodeStyleSettings settings, int indentSize) {
    mySettings = settings;
    myIndentSize = indentSize;
  }


  Indent getChildIndent(final ASTNode child) {
    final IElementType myType = child.getElementType();
    final ASTNode parent = child.getTreeParent();
    final IElementType parentType = parent != null ? parent.getElementType() : null;
    final IElementType superParentType = parent != null && parent.getTreeParent() != null ? parent.getTreeParent().getElementType() : null;
    int braceStyle = myType == CfmlElementTypes.FUNCTIONBODY || superParentType == CfmlElementTypes.FUNCTIONBODY
                     ? mySettings.METHOD_BRACE_STYLE
                     : mySettings.BRACE_STYLE;
    if (parentType == CfmlStubElementTypes.CFML_FILE || myType == CfmlTokenTypes.LSLASH_ANGLEBRACKET) {
      return Indent.getNoneIndent();
    }
    else if ((parentType == CfmlElementTypes.TAG || parentType == CfmlElementTypes.FORTAGEXPRESSION ||
              parentType == CfmlStubElementTypes.COMPONENT_TAG ||
              parentType == CfmlElementTypes.FUNCTION_DEFINITION ||
              parentType == CfmlElementTypes.FUNCTION_TAG) &&
             (myType == CfmlElementTypes.TAG ||
              myType == CfmlElementTypes.FUNCTION_TAG ||
              myType == CfmlElementTypes.PROPERTY_TAG ||
              myType == CfmlElementTypes.ARGUMENT_TAG ||
              myType == CfmlElementTypes.FORTAGEXPRESSION
              || myType == CfmlElementTypes.TEMPLATE_TEXT)) {
      if (parentType == CfmlElementTypes.TAG && myType == CfmlElementTypes.TAG) {
        if ("cfif".equals(((CfmlTag)parent.getPsi()).getTagName()) && "cfelse".equals(((CfmlTag)child.getPsi()).getTagName())) {
          return Indent.getNoneIndent();
        }
      }
      return Indent.getNormalIndent();
    }
    else if (myType == CfmlTokenTypes.START_EXPRESSION) {
      return Indent.getNormalIndent();
    }
    else if ((myType == CfmlTokenTypes.COMMENT || myType == CfscriptTokenTypes.COMMENT)) {
      if (mySettings.KEEP_FIRST_COLUMN_COMMENT) {
        return Indent.getAbsoluteNoneIndent();
      }
      return Indent.getNormalIndent();
    }
    else if (myType == CfscriptTokenTypes.R_CURLYBRACKET || myType == CfscriptTokenTypes.L_CURLYBRACKET) {
      return switch (braceStyle) {
        case CommonCodeStyleSettings.END_OF_LINE, CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED, CommonCodeStyleSettings.NEXT_LINE ->
          Indent.getNoneIndent();
        case CommonCodeStyleSettings.NEXT_LINE_SHIFTED, CommonCodeStyleSettings.NEXT_LINE_SHIFTED2 ->
          Indent.getNormalIndent();
        default -> Indent.getNoneIndent();
      };
    }

    else if (myType == CfmlTokenTypes.ASSIGN) {
      return Indent.getNormalIndent();
    }

    else if (isAssignmentOperator(myType)) {
      return Indent.getSpaceIndent(myIndentSize * 2);
    }
    else {
      if (myType == CfmlElementTypes.FUNCTION_CALL_EXPRESSION &&
          FormatterUtil.isPrecededBy(child, ASSIGNMENT_OPERATORS) &&
          parentType != CfmlElementTypes.ASSIGNMENT) {
        return Indent.getSpaceIndent(myIndentSize * 2);
      }
      else {
        if ((myType == CfmlElementTypes.REFERENCE_EXPRESSION || myType == CfmlElementTypes.BINARY_EXPRESSION) &&
            FormatterUtil.isPrecededBy(child, ASSIGNMENT_OPERATORS) &&
            parentType != CfmlElementTypes.ASSIGNMENT) {
          return Indent.getSpaceIndent(myIndentSize * 2);
        }

        else if (myType == CfmlElementTypes.FUNCTION_CALL_EXPRESSION ||
                 myType == CfmlElementTypes.SWITCHEXPRESSION ||
                 myType == CfmlElementTypes.ASSIGNMENT ||
                 myType == CfmlElementTypes.FOREXPRESSION ||
                 myType == CfmlElementTypes.IFEXPRESSION ||
                 myType == CfmlElementTypes.WHILEEXPRESSION ||
                 myType == CfmlElementTypes.CASEEXPRESSION ||
                 myType == CfmlElementTypes.DOWHILEEXPRESSION ||
                 myType == CfscriptTokenTypes.BREAK_KEYWORD ||
                 myType == CfmlElementTypes.TRYCATCHEXPRESSION ||
                 myType == CfmlElementTypes.FUNCTION_DEFINITION ||
                 myType == CfscriptTokenTypes.RETURN_KEYWORD ||
                 parentType == CfmlElementTypes.BLOCK_OF_STATEMENTS
          ) {
          if (parentType != CfmlElementTypes.SCRIPT_TAG) {
            if (superParentType == CfmlElementTypes.FUNCTIONBODY &&
                mySettings.METHOD_BRACE_STYLE == CommonCodeStyleSettings.NEXT_LINE_SHIFTED2) {
              return Indent.getSpaceIndent(myIndentSize * 2);
            }
            else if (superParentType != CfmlElementTypes.FUNCTIONBODY &&
                     mySettings.BRACE_STYLE == CommonCodeStyleSettings.NEXT_LINE_SHIFTED2) {
              return Indent.getSpaceIndent(myIndentSize * 2);
            }
            else if (myType == IFEXPRESSION &&
                     mySettings.SPECIAL_ELSE_IF_TREATMENT &&
                     FormatterUtil.isPrecededBy(child, CfscriptTokenTypes.ELSE_KEYWORD)) {
              return Indent.getNoneIndent();
            }
          }
          return Indent.getNormalIndent();
        }
        else if (myType == CfmlElementTypes.BLOCK_OF_STATEMENTS ||
                 myType == CfmlElementTypes.ARGUMENT_LIST ||
                 myType == CfscriptTokenTypes.ELSE_KEYWORD ||
                 myType == CfmlElementTypes.CATCHEXPRESSION ||
                 myType == CfmlElementTypes.FUNCTIONBODY ||
                 myType == CfmlElementTypes.NONE
          ) {
          return Indent.getNoneIndent();
        }
        else if (myType == CfmlElementTypes.REFERENCE_EXPRESSION && parentType != CfmlElementTypes.FUNCTION_CALL_EXPRESSION &&
                 parentType != CfmlElementTypes.FUNCTION_DEFINITION &&
                 parentType != CfmlElementTypes.ASSIGNMENT &&
                 parentType != CfmlElementTypes.BINARY_EXPRESSION &&
                 parentType != CfmlElementTypes.NONE &&
                 parentType != CfmlElementTypes.REFERENCE_EXPRESSION) {
          return Indent.getNormalIndent();
        }
        else if (parentType == CfmlElementTypes.ASSIGNMENT) {
          return child.getTreePrev() != null ? Indent.getNormalIndent() : Indent.getNoneIndent();
        }

        else if (parentType == CfmlElementTypes.BINARY_EXPRESSION) {
          return Indent.getNoneIndent();
        }
        else if (myType == CfmlElementTypes.BINARY_EXPRESSION) {
          return Indent.getSpaceIndent(myIndentSize * 2);
        }
        else if (myType == CfmlElementTypes.PROPERTY) {
          return Indent.getNormalIndent();
        }
        else if (myType == CfmlElementTypes.SCRIPT_TAG) {
          return Indent.getNormalIndent();
        }
        else if (myType == ACTION) {
          return Indent.getNormalIndent();
        }
        else {
          return null;
        }
      }
    }
  }
}

