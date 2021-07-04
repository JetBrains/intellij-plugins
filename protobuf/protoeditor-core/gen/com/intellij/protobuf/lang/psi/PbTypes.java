// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.protobuf.lang.psi.type.PbCustomTypes;
import com.intellij.protobuf.lang.stub.type.PbStubElementTypes;
import com.intellij.protobuf.lang.psi.impl.*;

public interface PbTypes {

  IElementType AGGREGATE_VALUE = new PbElementType("AGGREGATE_VALUE");
  IElementType ENUM_BODY = PbCustomTypes.get("ENUM_BODY");
  IElementType ENUM_DEFINITION = PbStubElementTypes.get("ENUM_DEFINITION");
  IElementType ENUM_RESERVED_RANGE = new PbElementType("ENUM_RESERVED_RANGE");
  IElementType ENUM_RESERVED_STATEMENT = new PbElementType("ENUM_RESERVED_STATEMENT");
  IElementType ENUM_VALUE = new PbElementType("ENUM_VALUE");
  IElementType EXTEND_BODY = PbCustomTypes.get("EXTEND_BODY");
  IElementType EXTEND_DEFINITION = PbStubElementTypes.get("EXTEND_DEFINITION");
  IElementType EXTENSIONS_STATEMENT = new PbElementType("EXTENSIONS_STATEMENT");
  IElementType EXTENSION_NAME = new PbElementType("EXTENSION_NAME");
  IElementType EXTENSION_RANGE = new PbElementType("EXTENSION_RANGE");
  IElementType FIELD_LABEL = new PbElementType("FIELD_LABEL");
  IElementType GROUP_DEFINITION = PbStubElementTypes.get("GROUP_DEFINITION");
  IElementType GROUP_OPTION_CONTAINER = new PbElementType("GROUP_OPTION_CONTAINER");
  IElementType IDENTIFIER_VALUE = new PbElementType("IDENTIFIER_VALUE");
  IElementType IMPORT_NAME = new PbElementType("IMPORT_NAME");
  IElementType IMPORT_STATEMENT = new PbElementType("IMPORT_STATEMENT");
  IElementType MAP_FIELD = new PbElementType("MAP_FIELD");
  IElementType MESSAGE_BODY = PbCustomTypes.get("MESSAGE_BODY");
  IElementType MESSAGE_DEFINITION = PbStubElementTypes.get("MESSAGE_DEFINITION");
  IElementType MESSAGE_TYPE_NAME = new PbElementType("MESSAGE_TYPE_NAME");
  IElementType METHOD_OPTIONS = new PbElementType("METHOD_OPTIONS");
  IElementType NUMBER_VALUE = new PbElementType("NUMBER_VALUE");
  IElementType ONEOF_BODY = PbCustomTypes.get("ONEOF_BODY");
  IElementType ONEOF_DEFINITION = PbStubElementTypes.get("ONEOF_DEFINITION");
  IElementType OPTION_EXPRESSION = new PbElementType("OPTION_EXPRESSION");
  IElementType OPTION_LIST = new PbElementType("OPTION_LIST");
  IElementType OPTION_NAME = new PbElementType("OPTION_NAME");
  IElementType OPTION_STATEMENT = new PbElementType("OPTION_STATEMENT");
  IElementType PACKAGE_NAME = new PbElementType("PACKAGE_NAME");
  IElementType PACKAGE_STATEMENT = PbStubElementTypes.get("PACKAGE_STATEMENT");
  IElementType RESERVED_RANGE = new PbElementType("RESERVED_RANGE");
  IElementType RESERVED_STATEMENT = new PbElementType("RESERVED_STATEMENT");
  IElementType SERVICE_BODY = PbCustomTypes.get("SERVICE_BODY");
  IElementType SERVICE_DEFINITION = PbStubElementTypes.get("SERVICE_DEFINITION");
  IElementType SERVICE_METHOD = PbStubElementTypes.get("SERVICE_METHOD");
  IElementType SERVICE_METHOD_TYPE = new PbElementType("SERVICE_METHOD_TYPE");
  IElementType SERVICE_STREAM = new PbElementType("SERVICE_STREAM");
  IElementType SIMPLE_FIELD = new PbElementType("SIMPLE_FIELD");
  IElementType STRING_PART = new PbElementType("STRING_PART");
  IElementType STRING_VALUE = new PbElementType("STRING_VALUE");
  IElementType SYMBOL_PATH = new PbElementType("SYMBOL_PATH");
  IElementType SYNTAX_STATEMENT = new PbElementType("SYNTAX_STATEMENT");
  IElementType TYPE_NAME = new PbElementType("TYPE_NAME");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == AGGREGATE_VALUE) {
        return new PbAggregateValueImpl(node);
      }
      else if (type == ENUM_BODY) {
        return new PbEnumBodyImpl(node);
      }
      else if (type == ENUM_DEFINITION) {
        return new PbEnumDefinitionImpl(node);
      }
      else if (type == ENUM_RESERVED_RANGE) {
        return new PbEnumReservedRangeImpl(node);
      }
      else if (type == ENUM_RESERVED_STATEMENT) {
        return new PbEnumReservedStatementImpl(node);
      }
      else if (type == ENUM_VALUE) {
        return new PbEnumValueImpl(node);
      }
      else if (type == EXTEND_BODY) {
        return new PbExtendBodyImpl(node);
      }
      else if (type == EXTEND_DEFINITION) {
        return new PbExtendDefinitionImpl(node);
      }
      else if (type == EXTENSIONS_STATEMENT) {
        return new PbExtensionsStatementImpl(node);
      }
      else if (type == EXTENSION_NAME) {
        return new PbExtensionNameImpl(node);
      }
      else if (type == EXTENSION_RANGE) {
        return new PbExtensionRangeImpl(node);
      }
      else if (type == FIELD_LABEL) {
        return new PbFieldLabelImpl(node);
      }
      else if (type == GROUP_DEFINITION) {
        return new PbGroupDefinitionImpl(node);
      }
      else if (type == GROUP_OPTION_CONTAINER) {
        return new PbGroupOptionContainerImpl(node);
      }
      else if (type == IDENTIFIER_VALUE) {
        return new PbIdentifierValueImpl(node);
      }
      else if (type == IMPORT_NAME) {
        return new PbImportNameImpl(node);
      }
      else if (type == IMPORT_STATEMENT) {
        return new PbImportStatementImpl(node);
      }
      else if (type == MAP_FIELD) {
        return new PbMapFieldImpl(node);
      }
      else if (type == MESSAGE_BODY) {
        return new PbMessageBodyImpl(node);
      }
      else if (type == MESSAGE_DEFINITION) {
        return new PbMessageDefinitionImpl(node);
      }
      else if (type == MESSAGE_TYPE_NAME) {
        return new PbMessageTypeNameImpl(node);
      }
      else if (type == METHOD_OPTIONS) {
        return new PbMethodOptionsImpl(node);
      }
      else if (type == NUMBER_VALUE) {
        return new PbNumberValueImpl(node);
      }
      else if (type == ONEOF_BODY) {
        return new PbOneofBodyImpl(node);
      }
      else if (type == ONEOF_DEFINITION) {
        return new PbOneofDefinitionImpl(node);
      }
      else if (type == OPTION_EXPRESSION) {
        return new PbOptionExpressionImpl(node);
      }
      else if (type == OPTION_LIST) {
        return new PbOptionListImpl(node);
      }
      else if (type == OPTION_NAME) {
        return new PbOptionNameImpl(node);
      }
      else if (type == OPTION_STATEMENT) {
        return new PbOptionStatementImpl(node);
      }
      else if (type == PACKAGE_NAME) {
        return new PbPackageNameImpl(node);
      }
      else if (type == PACKAGE_STATEMENT) {
        return new PbPackageStatementImpl(node);
      }
      else if (type == RESERVED_RANGE) {
        return new PbReservedRangeImpl(node);
      }
      else if (type == RESERVED_STATEMENT) {
        return new PbReservedStatementImpl(node);
      }
      else if (type == SERVICE_BODY) {
        return new PbServiceBodyImpl(node);
      }
      else if (type == SERVICE_DEFINITION) {
        return new PbServiceDefinitionImpl(node);
      }
      else if (type == SERVICE_METHOD) {
        return new PbServiceMethodImpl(node);
      }
      else if (type == SERVICE_METHOD_TYPE) {
        return new PbServiceMethodTypeImpl(node);
      }
      else if (type == SERVICE_STREAM) {
        return new PbServiceStreamImpl(node);
      }
      else if (type == SIMPLE_FIELD) {
        return new PbSimpleFieldImpl(node);
      }
      else if (type == STRING_PART) {
        return new PbStringPartImpl(node);
      }
      else if (type == STRING_VALUE) {
        return new PbStringValueImpl(node);
      }
      else if (type == SYMBOL_PATH) {
        return new PbSymbolPathImpl(node);
      }
      else if (type == SYNTAX_STATEMENT) {
        return new PbSyntaxStatementImpl(node);
      }
      else if (type == TYPE_NAME) {
        return new PbTypeNameImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
