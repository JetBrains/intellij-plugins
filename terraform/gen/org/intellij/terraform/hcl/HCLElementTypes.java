// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.intellij.terraform.hcl.psi.impl.*;

public interface HCLElementTypes {

  IElementType ARRAY = new HCLElementType("ARRAY");
  IElementType BINARY_ADDITION_EXPRESSION = new HCLElementType("BINARY_ADDITION_EXPRESSION");
  IElementType BINARY_AND_EXPRESSION = new HCLElementType("BINARY_AND_EXPRESSION");
  IElementType BINARY_EQUALITY_EXPRESSION = new HCLElementType("BINARY_EQUALITY_EXPRESSION");
  IElementType BINARY_MULTIPLY_EXPRESSION = new HCLElementType("BINARY_MULTIPLY_EXPRESSION");
  IElementType BINARY_OR_EXPRESSION = new HCLElementType("BINARY_OR_EXPRESSION");
  IElementType BINARY_RELATIONAL_EXPRESSION = new HCLElementType("BINARY_RELATIONAL_EXPRESSION");
  IElementType BLOCK = new HCLElementType("BLOCK");
  IElementType BLOCK_OBJECT = new HCLElementType("BLOCK_OBJECT");
  IElementType BOOLEAN_LITERAL = new HCLElementType("BOOLEAN_LITERAL");
  IElementType COLLECTION_VALUE = new HCLElementType("COLLECTION_VALUE");
  IElementType CONDITIONAL_EXPRESSION = new HCLElementType("CONDITIONAL_EXPRESSION");
  IElementType DEFINED_METHOD_EXPRESSION = new HCLElementType("DEFINED_METHOD_EXPRESSION");
  IElementType EXPRESSION = new HCLElementType("EXPRESSION");
  IElementType FOR_ARRAY_EXPRESSION = new HCLElementType("FOR_ARRAY_EXPRESSION");
  IElementType FOR_CONDITION = new HCLElementType("FOR_CONDITION");
  IElementType FOR_INTRO = new HCLElementType("FOR_INTRO");
  IElementType FOR_OBJECT_EXPRESSION = new HCLElementType("FOR_OBJECT_EXPRESSION");
  IElementType HEREDOC_CONTENT = new HCLElementType("HEREDOC_CONTENT");
  IElementType HEREDOC_LITERAL = new HCLElementType("HEREDOC_LITERAL");
  IElementType HEREDOC_MARKER = new HCLElementType("HEREDOC_MARKER");
  IElementType IDENTIFIER = new HCLElementType("IDENTIFIER");
  IElementType INDEX_SELECT_EXPRESSION = new HCLElementType("INDEX_SELECT_EXPRESSION");
  IElementType LITERAL = new HCLElementType("LITERAL");
  IElementType METHOD_CALL_EXPRESSION = new HCLElementType("METHOD_CALL_EXPRESSION");
  IElementType NULL_LITERAL = new HCLElementType("NULL_LITERAL");
  IElementType NUMBER_LITERAL = new HCLElementType("NUMBER_LITERAL");
  IElementType OBJECT = new HCLElementType("OBJECT");
  IElementType PARAMETER_LIST = new HCLElementType("PARAMETER_LIST");
  IElementType PARENTHESIZED_EXPRESSION = new HCLElementType("PARENTHESIZED_EXPRESSION");
  IElementType PROPERTY = new HCLElementType("PROPERTY");
  IElementType SELECT_EXPRESSION = new HCLElementType("SELECT_EXPRESSION");
  IElementType STRING_LITERAL = new HCLElementType("STRING_LITERAL");
  IElementType UNARY_EXPRESSION = new HCLElementType("UNARY_EXPRESSION");
  IElementType VALUE = new HCLElementType("VALUE");
  IElementType VARIABLE = new HCLElementType("VARIABLE");

  IElementType BLOCK_COMMENT = new HCLTokenType("block_comment");
  IElementType COLON_COLON = new HCLTokenType("::");
  IElementType COMMA = new HCLTokenType(",");
  IElementType DOUBLE_QUOTED_STRING = new HCLTokenType("DOUBLE_QUOTED_STRING");
  IElementType EQUALS = new HCLTokenType("=");
  IElementType FALSE = new HCLTokenType("false");
  IElementType HD_EOL = new HCLTokenType("HD_EOL");
  IElementType HD_LINE = new HCLTokenType("HD_LINE");
  IElementType HD_MARKER = new HCLTokenType("HD_MARKER");
  IElementType HD_START = new HCLTokenType("HD_START");
  IElementType ID = new HCLTokenType("ID");
  IElementType LINE_C_COMMENT = new HCLTokenType("line_c_comment");
  IElementType LINE_HASH_COMMENT = new HCLTokenType("line_hash_comment");
  IElementType L_BRACKET = new HCLTokenType("[");
  IElementType L_CURLY = new HCLTokenType("{");
  IElementType L_PAREN = new HCLTokenType("(");
  IElementType NULL = new HCLTokenType("null");
  IElementType NUMBER = new HCLTokenType("NUMBER");
  IElementType OP_AND_AND = new HCLTokenType("&&");
  IElementType OP_COLON = new HCLTokenType(":");
  IElementType OP_DIV = new HCLTokenType("/");
  IElementType OP_DOT = new HCLTokenType(".");
  IElementType OP_ELLIPSIS = new HCLTokenType("...");
  IElementType OP_EQUAL = new HCLTokenType("==");
  IElementType OP_GREATER = new HCLTokenType(">");
  IElementType OP_GREATER_OR_EQUAL = new HCLTokenType(">=");
  IElementType OP_LESS = new HCLTokenType("<");
  IElementType OP_LESS_OR_EQUAL = new HCLTokenType("<=");
  IElementType OP_MAPPING = new HCLTokenType("=>");
  IElementType OP_MINUS = new HCLTokenType("-");
  IElementType OP_MOD = new HCLTokenType("%");
  IElementType OP_MUL = new HCLTokenType("*");
  IElementType OP_NOT = new HCLTokenType("!");
  IElementType OP_NOT_EQUAL = new HCLTokenType("!=");
  IElementType OP_OR_OR = new HCLTokenType("||");
  IElementType OP_PLUS = new HCLTokenType("+");
  IElementType OP_QUEST = new HCLTokenType("?");
  IElementType R_BRACKET = new HCLTokenType("]");
  IElementType R_CURLY = new HCLTokenType("}");
  IElementType R_PAREN = new HCLTokenType(")");
  IElementType SINGLE_QUOTED_STRING = new HCLTokenType("SINGLE_QUOTED_STRING");
  IElementType TRUE = new HCLTokenType("true");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ARRAY) {
        return new HCLArrayImpl(node);
      }
      else if (type == BINARY_ADDITION_EXPRESSION) {
        return new HCLBinaryAdditionExpressionImpl(node);
      }
      else if (type == BINARY_AND_EXPRESSION) {
        return new HCLBinaryAndExpressionImpl(node);
      }
      else if (type == BINARY_EQUALITY_EXPRESSION) {
        return new HCLBinaryEqualityExpressionImpl(node);
      }
      else if (type == BINARY_MULTIPLY_EXPRESSION) {
        return new HCLBinaryMultiplyExpressionImpl(node);
      }
      else if (type == BINARY_OR_EXPRESSION) {
        return new HCLBinaryOrExpressionImpl(node);
      }
      else if (type == BINARY_RELATIONAL_EXPRESSION) {
        return new HCLBinaryRelationalExpressionImpl(node);
      }
      else if (type == BLOCK) {
        return new HCLBlockImpl(node);
      }
      else if (type == BLOCK_OBJECT) {
        return new HCLBlockObjectImpl(node);
      }
      else if (type == BOOLEAN_LITERAL) {
        return new HCLBooleanLiteralImpl(node);
      }
      else if (type == CONDITIONAL_EXPRESSION) {
        return new HCLConditionalExpressionImpl(node);
      }
      else if (type == DEFINED_METHOD_EXPRESSION) {
        return new HCLDefinedMethodExpressionImpl(node);
      }
      else if (type == FOR_ARRAY_EXPRESSION) {
        return new HCLForArrayExpressionImpl(node);
      }
      else if (type == FOR_CONDITION) {
        return new HCLForConditionImpl(node);
      }
      else if (type == FOR_INTRO) {
        return new HCLForIntroImpl(node);
      }
      else if (type == FOR_OBJECT_EXPRESSION) {
        return new HCLForObjectExpressionImpl(node);
      }
      else if (type == HEREDOC_CONTENT) {
        return new HCLHeredocContentImpl(node);
      }
      else if (type == HEREDOC_LITERAL) {
        return new HCLHeredocLiteralImpl(node);
      }
      else if (type == HEREDOC_MARKER) {
        return new HCLHeredocMarkerImpl(node);
      }
      else if (type == IDENTIFIER) {
        return new HCLIdentifierImpl(node);
      }
      else if (type == INDEX_SELECT_EXPRESSION) {
        return new HCLIndexSelectExpressionImpl(node);
      }
      else if (type == METHOD_CALL_EXPRESSION) {
        return new HCLMethodCallExpressionImpl(node);
      }
      else if (type == NULL_LITERAL) {
        return new HCLNullLiteralImpl(node);
      }
      else if (type == NUMBER_LITERAL) {
        return new HCLNumberLiteralImpl(node);
      }
      else if (type == OBJECT) {
        return new HCLObjectImpl(node);
      }
      else if (type == PARAMETER_LIST) {
        return new HCLParameterListImpl(node);
      }
      else if (type == PARENTHESIZED_EXPRESSION) {
        return new HCLParenthesizedExpressionImpl(node);
      }
      else if (type == PROPERTY) {
        return new HCLPropertyImpl(node);
      }
      else if (type == SELECT_EXPRESSION) {
        return new HCLSelectExpressionImpl(node);
      }
      else if (type == STRING_LITERAL) {
        return new HCLStringLiteralImpl(node);
      }
      else if (type == UNARY_EXPRESSION) {
        return new HCLUnaryExpressionImpl(node);
      }
      else if (type == VARIABLE) {
        return new HCLVariableImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
