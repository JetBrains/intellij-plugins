// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hil;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.intellij.terraform.hil.psi.impl.*;

public interface HILElementTypes {

  IElementType BAD_TAG = new HILElementType("BAD_TAG");
  IElementType ELSE_CONDITION = new HILElementType("ELSE_CONDITION");
  IElementType END_FOR = new HILElementType("END_FOR");
  IElementType END_IF = new HILElementType("END_IF");
  IElementType FOR_CONDITION = new HILElementType("FOR_CONDITION");
  IElementType FOR_VARIABLE = new HILElementType("FOR_VARIABLE");
  IElementType IF_CONDITION = new HILElementType("IF_CONDITION");
  IElementType IL_ARRAY = new HILElementType("IL_ARRAY");
  IElementType IL_BINARY_ADDITION_EXPRESSION = new HILElementType("IL_BINARY_ADDITION_EXPRESSION");
  IElementType IL_BINARY_AND_EXPRESSION = new HILElementType("IL_BINARY_AND_EXPRESSION");
  IElementType IL_BINARY_EQUALITY_EXPRESSION = new HILElementType("IL_BINARY_EQUALITY_EXPRESSION");
  IElementType IL_BINARY_MULTIPLY_EXPRESSION = new HILElementType("IL_BINARY_MULTIPLY_EXPRESSION");
  IElementType IL_BINARY_OR_EXPRESSION = new HILElementType("IL_BINARY_OR_EXPRESSION");
  IElementType IL_BINARY_RELATIONAL_EXPRESSION = new HILElementType("IL_BINARY_RELATIONAL_EXPRESSION");
  IElementType IL_COLLECTION_VALUE = new HILElementType("IL_COLLECTION_VALUE");
  IElementType IL_CONDITIONAL_EXPRESSION = new HILElementType("IL_CONDITIONAL_EXPRESSION");
  IElementType IL_DEFINED_METHOD_EXPRESSION = new HILElementType("IL_DEFINED_METHOD_EXPRESSION");
  IElementType IL_EXPRESSION = new HILElementType("IL_EXPRESSION");
  IElementType IL_EXPRESSION_HOLDER = new HILElementType("IL_EXPRESSION_HOLDER");
  IElementType IL_INDEX_SELECT_EXPRESSION = new HILElementType("IL_INDEX_SELECT_EXPRESSION");
  IElementType IL_LITERAL_EXPRESSION = new HILElementType("IL_LITERAL_EXPRESSION");
  IElementType IL_METHOD_CALL_EXPRESSION = new HILElementType("IL_METHOD_CALL_EXPRESSION");
  IElementType IL_OBJECT = new HILElementType("IL_OBJECT");
  IElementType IL_PARAMETER_LIST = new HILElementType("IL_PARAMETER_LIST");
  IElementType IL_PARENTHESIZED_EXPRESSION = new HILElementType("IL_PARENTHESIZED_EXPRESSION");
  IElementType IL_PROPERTY = new HILElementType("IL_PROPERTY");
  IElementType IL_SELECT_EXPRESSION = new HILElementType("IL_SELECT_EXPRESSION");
  IElementType IL_SIMPLE_EXPRESSION = new HILElementType("IL_SIMPLE_EXPRESSION");
  IElementType IL_TEMPLATE_BLOCK_BODY = new HILElementType("IL_TEMPLATE_BLOCK_BODY");
  IElementType IL_TEMPLATE_FOR_BLOCK_EXPRESSION = new HILElementType("IL_TEMPLATE_FOR_BLOCK_EXPRESSION");
  IElementType IL_TEMPLATE_HOLDER = new HILElementType("IL_TEMPLATE_HOLDER");
  IElementType IL_TEMPLATE_IF_BLOCK_EXPRESSION = new HILElementType("IL_TEMPLATE_IF_BLOCK_EXPRESSION");
  IElementType IL_UNARY_EXPRESSION = new HILElementType("IL_UNARY_EXPRESSION");
  IElementType IL_VARIABLE = new HILElementType("IL_VARIABLE");

  IElementType COLON_COLON = new HILTokenType("::");
  IElementType COMMA = new HILTokenType(",");
  IElementType DOLLAR = new HILTokenType("$");
  IElementType DOUBLE_QUOTED_STRING = new HILTokenType("DOUBLE_QUOTED_STRING");
  IElementType ELSE_KEYWORD = new HILTokenType("else");
  IElementType ENDFOR_KEYWORD = new HILTokenType("endfor");
  IElementType ENDIF_KEYWORD = new HILTokenType("endif");
  IElementType EQUALS = new HILTokenType("=");
  IElementType FALSE = new HILTokenType("false");
  IElementType FOR_KEYWORD = new HILTokenType("for");
  IElementType ID = new HILTokenType("ID");
  IElementType IF_KEYWORD = new HILTokenType("if");
  IElementType INTERPOLATION_START = new HILTokenType("${");
  IElementType IN_KEYWORD = new HILTokenType("in");
  IElementType L_BRACKET = new HILTokenType("[");
  IElementType L_CURLY = new HILTokenType("{");
  IElementType L_PAREN = new HILTokenType("(");
  IElementType NULL = new HILTokenType("null");
  IElementType NUMBER = new HILTokenType("NUMBER");
  IElementType OP_AND_AND = new HILTokenType("&&");
  IElementType OP_COLON = new HILTokenType(":");
  IElementType OP_DIV = new HILTokenType("/");
  IElementType OP_DOT = new HILTokenType(".");
  IElementType OP_ELLIPSIS = new HILTokenType("...");
  IElementType OP_EQUAL = new HILTokenType("==");
  IElementType OP_GREATER = new HILTokenType(">");
  IElementType OP_GREATER_OR_EQUAL = new HILTokenType(">=");
  IElementType OP_LESS = new HILTokenType("<");
  IElementType OP_LESS_OR_EQUAL = new HILTokenType("<=");
  IElementType OP_MINUS = new HILTokenType("-");
  IElementType OP_MOD = new HILTokenType("%");
  IElementType OP_MUL = new HILTokenType("*");
  IElementType OP_NOT = new HILTokenType("!");
  IElementType OP_NOT_EQUAL = new HILTokenType("!=");
  IElementType OP_OR_OR = new HILTokenType("||");
  IElementType OP_PLUS = new HILTokenType("+");
  IElementType OP_QUEST = new HILTokenType("?");
  IElementType R_BRACKET = new HILTokenType("]");
  IElementType R_CURLY = new HILTokenType("}");
  IElementType R_PAREN = new HILTokenType(")");
  IElementType TEMPLATE_START = new HILTokenType("TEMPLATE_START");
  IElementType TILDA = new HILTokenType("~");
  IElementType TRUE = new HILTokenType("true");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == BAD_TAG) {
        return new BadTagImpl(node);
      }
      else if (type == ELSE_CONDITION) {
        return new ElseConditionImpl(node);
      }
      else if (type == END_FOR) {
        return new EndForImpl(node);
      }
      else if (type == END_IF) {
        return new EndIfImpl(node);
      }
      else if (type == FOR_CONDITION) {
        return new ForConditionImpl(node);
      }
      else if (type == FOR_VARIABLE) {
        return new ForVariableImpl(node);
      }
      else if (type == IF_CONDITION) {
        return new IfConditionImpl(node);
      }
      else if (type == IL_ARRAY) {
        return new ILArrayImpl(node);
      }
      else if (type == IL_BINARY_ADDITION_EXPRESSION) {
        return new ILBinaryAdditionExpressionImpl(node);
      }
      else if (type == IL_BINARY_AND_EXPRESSION) {
        return new ILBinaryAndExpressionImpl(node);
      }
      else if (type == IL_BINARY_EQUALITY_EXPRESSION) {
        return new ILBinaryEqualityExpressionImpl(node);
      }
      else if (type == IL_BINARY_MULTIPLY_EXPRESSION) {
        return new ILBinaryMultiplyExpressionImpl(node);
      }
      else if (type == IL_BINARY_OR_EXPRESSION) {
        return new ILBinaryOrExpressionImpl(node);
      }
      else if (type == IL_BINARY_RELATIONAL_EXPRESSION) {
        return new ILBinaryRelationalExpressionImpl(node);
      }
      else if (type == IL_COLLECTION_VALUE) {
        return new ILCollectionValueImpl(node);
      }
      else if (type == IL_CONDITIONAL_EXPRESSION) {
        return new ILConditionalExpressionImpl(node);
      }
      else if (type == IL_DEFINED_METHOD_EXPRESSION) {
        return new ILDefinedMethodExpressionImpl(node);
      }
      else if (type == IL_EXPRESSION_HOLDER) {
        return new ILExpressionHolderImpl(node);
      }
      else if (type == IL_INDEX_SELECT_EXPRESSION) {
        return new ILIndexSelectExpressionImpl(node);
      }
      else if (type == IL_LITERAL_EXPRESSION) {
        return new ILLiteralExpressionImpl(node);
      }
      else if (type == IL_METHOD_CALL_EXPRESSION) {
        return new ILMethodCallExpressionImpl(node);
      }
      else if (type == IL_OBJECT) {
        return new ILObjectImpl(node);
      }
      else if (type == IL_PARAMETER_LIST) {
        return new ILParameterListImpl(node);
      }
      else if (type == IL_PARENTHESIZED_EXPRESSION) {
        return new ILParenthesizedExpressionImpl(node);
      }
      else if (type == IL_PROPERTY) {
        return new ILPropertyImpl(node);
      }
      else if (type == IL_SELECT_EXPRESSION) {
        return new ILSelectExpressionImpl(node);
      }
      else if (type == IL_TEMPLATE_BLOCK_BODY) {
        return new ILTemplateBlockBodyImpl(node);
      }
      else if (type == IL_TEMPLATE_FOR_BLOCK_EXPRESSION) {
        return new ILTemplateForBlockExpressionImpl(node);
      }
      else if (type == IL_TEMPLATE_HOLDER) {
        return new ILTemplateHolderImpl(node);
      }
      else if (type == IL_TEMPLATE_IF_BLOCK_EXPRESSION) {
        return new ILTemplateIfBlockExpressionImpl(node);
      }
      else if (type == IL_UNARY_EXPRESSION) {
        return new ILUnaryExpressionImpl(node);
      }
      else if (type == IL_VARIABLE) {
        return new ILVariableImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
