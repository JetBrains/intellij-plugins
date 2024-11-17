// This is a generated file. Not intended for manual editing.
package com.intellij.tsr.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.tsr.psi.impl.*;

public interface TslTokenTypes {

  IElementType BOOLEAN_LITERAL = new TslElementType("BOOLEAN_LITERAL");
  IElementType FALLBACK_STRING_LITERAL = new TslElementType("FALLBACK_STRING_LITERAL");
  IElementType LIST = new TslElementType("LIST");
  IElementType MAP = new TslElementType("MAP");
  IElementType MAP_ITEM = new TslElementType("MAP_ITEM");
  IElementType MAP_KEY = new TslElementType("MAP_KEY");
  IElementType NULL_LITERAL = new TslElementType("NULL_LITERAL");
  IElementType NUMBER_LITERAL = new TslElementType("NUMBER_LITERAL");
  IElementType OBJECT_BRACE = new TslElementType("OBJECT_BRACE");
  IElementType OBJECT_BRACKET = new TslElementType("OBJECT_BRACKET");
  IElementType OBJECT_ID = new TslElementType("OBJECT_ID");
  IElementType OBJECT_NAME = new TslElementType("OBJECT_NAME");
  IElementType OBJECT_PARENTH = new TslElementType("OBJECT_PARENTH");
  IElementType OBJECT_REF = new TslElementType("OBJECT_REF");
  IElementType PROPERTY_KEY = new TslElementType("PROPERTY_KEY");
  IElementType PROPERTY_KEY_VALUE = new TslElementType("PROPERTY_KEY_VALUE");
  IElementType STRING_LITERAL = new TslElementType("STRING_LITERAL");
  IElementType VALUE = new TslElementType("VALUE");

  IElementType ASSIGN = new TslTokenType("=");
  IElementType BACKSLASH = new TslTokenType("\\");
  IElementType COLON = new TslTokenType(":");
  IElementType COMMA = new TslTokenType(",");
  IElementType DASH = new TslTokenType("-");
  IElementType DOLLAR = new TslTokenType("$");
  IElementType DOT = new TslTokenType(".");
  IElementType DOUBLE_QUOTED_STRING = new TslTokenType("DOUBLE_QUOTED_STRING");
  IElementType FALSE = new TslTokenType("false");
  IElementType IDENTIFIER = new TslTokenType("IDENTIFIER");
  IElementType LBRACE = new TslTokenType("{");
  IElementType LBRACKET = new TslTokenType("[");
  IElementType LPARENTH = new TslTokenType("(");
  IElementType NULL = new TslTokenType("null");
  IElementType NUMBER = new TslTokenType("NUMBER");
  IElementType PERCENT = new TslTokenType("%");
  IElementType PLUS = new TslTokenType("+");
  IElementType RBRACE = new TslTokenType("}");
  IElementType RBRACKET = new TslTokenType("]");
  IElementType RPARENTH = new TslTokenType(")");
  IElementType SEMICOLON = new TslTokenType(";");
  IElementType SHARP = new TslTokenType("#");
  IElementType SINGLE_QUOTED_STRING = new TslTokenType("SINGLE_QUOTED_STRING");
  IElementType SLASH = new TslTokenType("/");
  IElementType STAR = new TslTokenType("*");
  IElementType STRUDEL_HEX = new TslTokenType("STRUDEL_HEX");
  IElementType TRUE = new TslTokenType("true");
  IElementType VALUE_12_0 = new TslTokenType("value_12_0");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == BOOLEAN_LITERAL) {
        return new TslBooleanLiteralImpl(node);
      }
      else if (type == FALLBACK_STRING_LITERAL) {
        return new TslFallbackStringLiteralImpl(node);
      }
      else if (type == LIST) {
        return new TslListImpl(node);
      }
      else if (type == MAP) {
        return new TslMapImpl(node);
      }
      else if (type == MAP_ITEM) {
        return new TslMapItemImpl(node);
      }
      else if (type == MAP_KEY) {
        return new TslMapKeyImpl(node);
      }
      else if (type == NULL_LITERAL) {
        return new TslNullLiteralImpl(node);
      }
      else if (type == NUMBER_LITERAL) {
        return new TslNumberLiteralImpl(node);
      }
      else if (type == OBJECT_BRACE) {
        return new TslObjectBraceImpl(node);
      }
      else if (type == OBJECT_BRACKET) {
        return new TslObjectBracketImpl(node);
      }
      else if (type == OBJECT_ID) {
        return new TslObjectIdImpl(node);
      }
      else if (type == OBJECT_NAME) {
        return new TslObjectNameImpl(node);
      }
      else if (type == OBJECT_PARENTH) {
        return new TslObjectParenthImpl(node);
      }
      else if (type == OBJECT_REF) {
        return new TslObjectRefImpl(node);
      }
      else if (type == PROPERTY_KEY) {
        return new TslPropertyKeyImpl(node);
      }
      else if (type == PROPERTY_KEY_VALUE) {
        return new TslPropertyKeyValueImpl(node);
      }
      else if (type == STRING_LITERAL) {
        return new TslStringLiteralImpl(node);
      }
      else if (type == VALUE) {
        return new TslValueImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
