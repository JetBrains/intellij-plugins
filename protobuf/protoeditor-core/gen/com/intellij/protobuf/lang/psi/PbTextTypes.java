// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.protobuf.lang.psi.impl.*;

public interface PbTextTypes {

  IElementType DOMAIN = new PbTextElementType("DOMAIN");
  IElementType EXTENSION_NAME = new PbTextElementType("EXTENSION_NAME");
  IElementType FIELD = new PbTextElementType("FIELD");
  IElementType FIELD_NAME = new PbTextElementType("FIELD_NAME");
  IElementType IDENTIFIER_VALUE = new PbTextElementType("IDENTIFIER_VALUE");
  IElementType MESSAGE_VALUE = new PbTextElementType("MESSAGE_VALUE");
  IElementType NUMBER_VALUE = new PbTextElementType("NUMBER_VALUE");
  IElementType STRING_PART = new PbTextElementType("STRING_PART");
  IElementType STRING_VALUE = new PbTextElementType("STRING_VALUE");
  IElementType SYMBOL_PATH = new PbTextElementType("SYMBOL_PATH");
  IElementType VALUE_LIST = new PbTextElementType("VALUE_LIST");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == DOMAIN) {
        return new PbTextDomainImpl(node);
      }
      else if (type == EXTENSION_NAME) {
        return new PbTextExtensionNameImpl(node);
      }
      else if (type == FIELD) {
        return new PbTextFieldImpl(node);
      }
      else if (type == FIELD_NAME) {
        return new PbTextFieldNameImpl(node);
      }
      else if (type == IDENTIFIER_VALUE) {
        return new PbTextIdentifierValueImpl(node);
      }
      else if (type == MESSAGE_VALUE) {
        return new PbTextMessageValueImpl(node);
      }
      else if (type == NUMBER_VALUE) {
        return new PbTextNumberValueImpl(node);
      }
      else if (type == STRING_PART) {
        return new PbTextStringPartImpl(node);
      }
      else if (type == STRING_VALUE) {
        return new PbTextStringValueImpl(node);
      }
      else if (type == SYMBOL_PATH) {
        return new PbTextSymbolPathImpl(node);
      }
      else if (type == VALUE_LIST) {
        return new PbTextValueListImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
