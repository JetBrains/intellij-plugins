/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.model.formatter;

import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.formatting.Block;
import com.intellij.formatting.Spacing;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 */
public class CfmlSpacingProcessor extends CfmlFormatterUtil {

  private final CommonCodeStyleSettings mySettings;
  private final CfmlCodeStyleSettings myCfmlSettings;
  private final ASTNode myNode;

  public CfmlSpacingProcessor(ASTNode node, CommonCodeStyleSettings settings,
                              CfmlCodeStyleSettings cfmlSettings) {
    mySettings = settings;
    myCfmlSettings = cfmlSettings;
    myNode = node;
  }

  private static Spacing getSpacesInsideAttribute(final IElementType type1, final IElementType type2) {
    if (type1 == CfmlTokenTypes.ASSIGN || type2 == CfmlTokenTypes.ASSIGN) {
      return Spacing
        .createSpacing(0, 0, 0, true, 1);
    }
    else {
      return Spacing.createSpacing(0, Integer.MAX_VALUE, 0, false, 1);
    }
  }

  public Spacing getSpacing(Block child1, Block child2) {
    if (!(child1 instanceof AbstractBlock) || !(child2 instanceof AbstractBlock)) {
      return null;
    }

    final IElementType elementType = myNode.getElementType();
    final ASTNode node1 = ((AbstractBlock)child1).getNode();
    final IElementType type1 = node1.getElementType();
    final ASTNode node2 = ((AbstractBlock)child2).getNode();
    final IElementType type2 = node2.getElementType();
    //ColdFusion Tag Spacing//
    if (type2 == CfmlElementTypes.TAG || type2 == CfmlElementTypes.FORTAGEXPRESSION ||
        type2 == CfmlElementTypes.ARGUMENT_TAG ||
        type2 == CfmlElementTypes.FUNCTION_DEFINITION ||
        type2 == CfmlElementTypes.SCRIPT_TAG) {
      return Spacing.createSpacing(0, 0, 1, false, 1);
    }
    else if (type1 == CfmlTokenTypes.OPENER || type2 == CfmlTokenTypes.R_ANGLEBRACKET || type2 == CfmlTokenTypes.CLOSER) {
      return Spacing.createSpacing(0, 0, 0, false, 0);
    }
    else if (elementType == CfmlElementTypes.ATTRIBUTE) {
      return getSpacesInsideAttribute(type1, type2);
    }

    else if (type1 == CfmlTokenTypes.CF_TAG_NAME && node1.getTreePrev().getElementType() == CfmlTokenTypes.LSLASH_ANGLEBRACKET) {
      return Spacing.createSpacing(0, 0, 0, false, 0);
    }

    else if (type1 == CfmlTokenTypes.CF_TAG_NAME && type2 == CfmlElementTypes.ASSIGNMENT) {
      return Spacing.createSpacing(1, 1, 0, false, 0);
    }

    //Cfscript spacing
    //
    // Spacing before parentheses
    //
    if (type2 == CfmlElementTypes.ARGUMENT_LIST) {
      if ((elementType == CfmlElementTypes.FUNCTION_CALL_EXPRESSION) &&
          type1 == CfmlElementTypes.REFERENCE_EXPRESSION) {
        return addSingleSpaceIf(mySettings.SPACE_BEFORE_METHOD_CALL_PARENTHESES);
      }
    }
    else if (type2 == CfscriptTokenTypes.L_BRACKET) {
      if ((elementType == CfmlElementTypes.IFEXPRESSION) &&
          type1 == CfscriptTokenTypes.IF_KEYWORD) {
        return addSingleSpaceIf(mySettings.SPACE_BEFORE_IF_PARENTHESES);
      }
      else if ((elementType == CfmlElementTypes.WHILEEXPRESSION) &&
               type1 == CfscriptTokenTypes.WHILE_KEYWORD) {
        return addSingleSpaceIf(mySettings.SPACE_BEFORE_WHILE_PARENTHESES);
      }
      else if ((elementType == CfmlElementTypes.FOREXPRESSION) &&
               type1 == CfscriptTokenTypes.FOR_KEYWORD) {
        return addSingleSpaceIf(mySettings.SPACE_BEFORE_FOR_PARENTHESES);
      }
      else if ((elementType == CfmlElementTypes.SWITCHEXPRESSION) &&
               type1 == CfscriptTokenTypes.SWITCH_KEYWORD) {
        return addSingleSpaceIf(mySettings.SPACE_BEFORE_SWITCH_PARENTHESES);
      }
      else if ((elementType == CfmlElementTypes.CATCHEXPRESSION) &&
               type1 == CfscriptTokenTypes.CATCH_KEYWORD) {
        return addSingleSpaceIf(mySettings.SPACE_BEFORE_CATCH_PARENTHESES);
      }
    }

    else if ((elementType == CfmlElementTypes.FUNCTION_DEFINITION) &&
             type1 == CfscriptTokenTypes.IDENTIFIER && type2 == CfmlElementTypes.PARAMETERS_LIST) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_METHOD_PARENTHESES);
    }

    //
    // Spacing around assignment operators (=, -=, etc.)
    //
    if (type1 == CfmlTokenTypes.ATTRIBUTE || (type1 == CfmlTokenTypes.ASSIGN && type2 == CfmlTokenTypes.DOUBLE_QUOTE)) {
      return Spacing.createSpacing(0, 0, 0, false, 0);
    }
    if (isAssignmentOperator(type1) || isAssignmentOperator(type2)) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_ASSIGNMENT_OPERATORS);
    }

    //
    // Spacing around  logical operators (&&, OR, etc.)
    //
    if (isLogicalOperator(type1) || isLogicalOperator(type2)) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_LOGICAL_OPERATORS);
    }
    //
    // Spacing around  equality operators (==, != etc.)
    //
    if (isEqualityOperator(type1) || isEqualityOperator(type2)) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_EQUALITY_OPERATORS);
    }
    //
    // Spacing around  relational operators (<, <= etc.)
    //
    if (isRelationalOperator(type1) || isRelationalOperator(type2)) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_RELATIONAL_OPERATORS);
    }
    //
    // Spacing around  additive operators ( |, &, etc.)
    //
    if (isAdditiveOperator(type1) || isAdditiveOperator(type2)) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_ADDITIVE_OPERATORS);
    }
    //
    // Spacing around  multiplicative operators ( |, &, etc.)
    //
    if (isMultiplicativeOperator(type1) || isMultiplicativeOperator(type2)) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS);
    }
    //
    // Spacing around  unary operators ( NOT, ++, etc.)
    //
    if (isUnaryOperator(type1) || isUnaryOperator(type2)) {
      return addSingleSpaceIf(mySettings.SPACE_AROUND_UNARY_OPERATOR);
    }
    //
    // Spacing around  concatenation operator ( &, etc.)
    //
    if (type1 == CfscriptTokenTypes.CONCAT || type2 == CfscriptTokenTypes.CONCAT) {
      return addSingleSpaceIf(myCfmlSettings.CONCAT_SPACES);
    }
    //
    //
    //Spacing before left braces
    //
    if (type1 == CfscriptTokenTypes.R_BRACKET) {
      if (type2 == CfscriptTokenTypes.L_CURLYBRACKET && elementType == CfmlElementTypes.SWITCHEXPRESSION) {
        return setBraceSpace(mySettings.SPACE_BEFORE_SWITCH_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
      }

      else if (type2 == CfmlElementTypes.BLOCK_OF_STATEMENTS) {
        if (elementType == CfmlElementTypes.IFEXPRESSION) {
          return setBraceSpace(mySettings.SPACE_BEFORE_IF_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
        }
        else if (elementType == CfmlElementTypes.FOREXPRESSION) {
          return setBraceSpace(mySettings.SPACE_BEFORE_FOR_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
        }
        else if (elementType == CfmlElementTypes.WHILEEXPRESSION) {
          return setBraceSpace(mySettings.SPACE_BEFORE_WHILE_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
        }
      }
    }
    else if (type1 == CfscriptTokenTypes.ELSE_KEYWORD) {
      if (type2 == CfmlElementTypes.IFEXPRESSION) {
        return Spacing.createSpacing(1, 1, mySettings.SPECIAL_ELSE_IF_TREATMENT ? 0 : 1, false, mySettings.KEEP_BLANK_LINES_IN_CODE);
      }
      else if (type2 == CfmlElementTypes.BLOCK_OF_STATEMENTS) {
        return setBraceSpace(mySettings.SPACE_BEFORE_ELSE_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
      }
      return Spacing.createSpacing(1, 1, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }
    else if (type1 == CfscriptTokenTypes.TRY_KEYWORD) {
      return setBraceSpace(mySettings.SPACE_BEFORE_TRY_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
    }
    else if (type1 == CfscriptTokenTypes.CATCH_KEYWORD) {
      return setBraceSpace(mySettings.SPACE_BEFORE_CATCH_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
    }
    else if (type1 == CfscriptTokenTypes.DO_KEYWORD) {
      return setBraceSpace(mySettings.SPACE_BEFORE_DO_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange());
    }
    else if (type1 == CfmlElementTypes.PARAMETERS_LIST && type2 == CfmlElementTypes.FUNCTIONBODY) {
      return setBraceSpace(mySettings.SPACE_BEFORE_METHOD_LBRACE, mySettings.METHOD_BRACE_STYLE, child1.getTextRange());
    }
    //
    //Spacing before keyword (else, catch, etc)
    //
    if (type2 == CfscriptTokenTypes.ELSE_KEYWORD) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_ELSE_KEYWORD, mySettings.ELSE_ON_NEW_LINE);
    }
    else if (type2 == CfscriptTokenTypes.WHILE_KEYWORD) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_WHILE_KEYWORD, mySettings.WHILE_ON_NEW_LINE);
    }
    else if (type2 == CfmlElementTypes.CATCHEXPRESSION) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_CATCH_KEYWORD, mySettings.CATCH_ON_NEW_LINE);
    }

    //
    //Spacing within
    //
    if ((type1 == CfscriptTokenTypes.L_BRACKET || type2 == CfscriptTokenTypes.R_BRACKET)) {
      if (elementType == CfmlElementTypes.ARGUMENT_LIST) {
        if (type1 == CfscriptTokenTypes.L_BRACKET) {
          return addSingleSpaceIf(mySettings.SPACE_WITHIN_METHOD_CALL_PARENTHESES,
                                  mySettings.CALL_PARAMETERS_LPAREN_ON_NEXT_LINE);
        }
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_METHOD_CALL_PARENTHESES,
                                mySettings.CALL_PARAMETERS_RPAREN_ON_NEXT_LINE);
      }
      else if (elementType == CfmlElementTypes.PARAMETERS_LIST) {
        if (type1 == CfscriptTokenTypes.L_BRACKET) {
          return addSingleSpaceIf(mySettings.SPACE_WITHIN_METHOD_PARENTHESES,
                                  mySettings.METHOD_PARAMETERS_LPAREN_ON_NEXT_LINE
          );
        }
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_METHOD_PARENTHESES,
                                mySettings.METHOD_PARAMETERS_RPAREN_ON_NEXT_LINE
        );
      }
      else if (elementType == CfmlElementTypes.IFEXPRESSION) {
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_IF_PARENTHESES);
      }
      else if (elementType == CfmlElementTypes.FOREXPRESSION) {
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_FOR_PARENTHESES);
      }
      else if (elementType == CfmlElementTypes.WHILEEXPRESSION) {
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_WHILE_PARENTHESES);
      }
      else if (elementType == CfmlElementTypes.SWITCHEXPRESSION) {
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_SWITCH_PARENTHESES);
      }
      else if (elementType == CfmlElementTypes.CATCHEXPRESSION) {
        return addSingleSpaceIf(mySettings.SPACE_WITHIN_CATCH_PARENTHESES);
      }

      if (mySettings.BINARY_OPERATION_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP && type2 == CfmlElementTypes.BINARY_EXPRESSION) {
        return Spacing.createSpacing(0, 0, mySettings.PARENTHESES_EXPRESSION_LPAREN_WRAP ? 1 : 0, mySettings.KEEP_LINE_BREAKS,
                                     mySettings.KEEP_BLANK_LINES_IN_CODE);
      }
      else if (mySettings.BINARY_OPERATION_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP && type1 == CfmlElementTypes.BINARY_EXPRESSION) {
        return Spacing.createSpacing(0, 0, mySettings.PARENTHESES_EXPRESSION_RPAREN_WRAP ? 1 : 0, mySettings.KEEP_LINE_BREAKS,
                                     mySettings.KEEP_BLANK_LINES_IN_CODE);
      }
    }
    //
    //Spacing in ternary operator(?:)
    //
    if (elementType == CfmlElementTypes.TERNARY_EXPRESSION) {
      if (type2 == CfscriptTokenTypes.QUESTION) {
        return addSingleSpaceIf(mySettings.SPACE_BEFORE_QUEST);
      }
      else if (type1 == CfscriptTokenTypes.QUESTION) {
        return addSingleSpaceIf(mySettings.SPACE_AFTER_QUEST);
      }
      else if (type2 == CfscriptTokenTypes.DOTDOT) {
        return addSingleSpaceIf(mySettings.SPACE_BEFORE_COLON);
      }
      else if (type1 == CfscriptTokenTypes.DOTDOT) {
        return addSingleSpaceIf(mySettings.SPACE_AFTER_COLON);
      }
    }
    //
    //Other spacing (',',';')
    //

    if (type2 == CfscriptTokenTypes.COMMA) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_COMMA);
    }
    if (type1 == CfscriptTokenTypes.COMMA) {
      return addSingleSpaceIf(mySettings.SPACE_AFTER_COMMA);
    }
    if (type2 == CfscriptTokenTypes.SEMICOLON) {
      return addSingleSpaceIf(mySettings.SPACE_BEFORE_SEMICOLON);
    }
    if (type1 == CfscriptTokenTypes.SEMICOLON) {
      return addSingleSpaceIf(mySettings.SPACE_AFTER_SEMICOLON);
    }

    return Spacing.createSpacing(0, 1, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE);
  }


  private Spacing addSingleSpaceIf(boolean condition) {
    int spaces = condition ? 1 : 0;
    return Spacing.createSpacing(spaces, spaces, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
  }

  private Spacing addSingleSpaceIf(boolean condition, boolean linesFeed) {
    int spaces = condition ? 1 : 0;
    int lines = linesFeed ? 1 : 0;
    return Spacing.createSpacing(spaces, spaces, lines, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
  }

  private Spacing setBraceSpace(boolean needSpaceSetting,
                                @CommonCodeStyleSettings.BraceStyleConstant int braceStyleSetting,
                                TextRange textRange) {
    int spaces = needSpaceSetting ? 1 : 0;
    if (braceStyleSetting == CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED && textRange != null) {
      return Spacing.createDependentLFSpacing(spaces, spaces, textRange, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
    }
    else {
      int lineBreaks = braceStyleSetting == CommonCodeStyleSettings.END_OF_LINE ||
                       braceStyleSetting == CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED ? 0 : 1;
      return Spacing.createSpacing(spaces, spaces, lineBreaks, false, 0);
    }
  }
}
