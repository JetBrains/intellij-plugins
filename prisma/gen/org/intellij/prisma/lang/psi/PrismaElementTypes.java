// This is a generated file. Not intended for manual editing.
package org.intellij.prisma.lang.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.intellij.prisma.lang.psi.stubs.PrismaStubElementTypeFactory;
import org.intellij.prisma.lang.psi.impl.*;

public interface PrismaElementTypes {

  IElementType ARGUMENT = new PrismaElementType("ARGUMENT");
  IElementType ARGUMENTS_LIST = new PrismaElementType("ARGUMENTS_LIST");
  IElementType ARRAY_EXPRESSION = new PrismaElementType("ARRAY_EXPRESSION");
  IElementType BLOCK_ATTRIBUTE = new PrismaElementType("BLOCK_ATTRIBUTE");
  IElementType DATASOURCE_DECLARATION = PrismaStubElementTypeFactory.create("DATASOURCE_DECLARATION");
  IElementType ENUM_DECLARATION = PrismaStubElementTypeFactory.create("ENUM_DECLARATION");
  IElementType ENUM_DECLARATION_BLOCK = new PrismaElementType("ENUM_DECLARATION_BLOCK");
  IElementType ENUM_VALUE_DECLARATION = PrismaStubElementTypeFactory.create("ENUM_VALUE_DECLARATION");
  IElementType EXPRESSION = new PrismaElementType("EXPRESSION");
  IElementType FIELD_ATTRIBUTE = new PrismaElementType("FIELD_ATTRIBUTE");
  IElementType FIELD_DECLARATION = PrismaStubElementTypeFactory.create("FIELD_DECLARATION");
  IElementType FIELD_DECLARATION_BLOCK = new PrismaElementType("FIELD_DECLARATION_BLOCK");
  IElementType FIELD_TYPE = new PrismaElementType("FIELD_TYPE");
  IElementType FUNCTION_CALL = new PrismaElementType("FUNCTION_CALL");
  IElementType GENERATOR_DECLARATION = PrismaStubElementTypeFactory.create("GENERATOR_DECLARATION");
  IElementType KEY_VALUE = PrismaStubElementTypeFactory.create("KEY_VALUE");
  IElementType KEY_VALUE_BLOCK = new PrismaElementType("KEY_VALUE_BLOCK");
  IElementType LEGACY_LIST_TYPE = new PrismaElementType("LEGACY_LIST_TYPE");
  IElementType LEGACY_REQUIRED_TYPE = new PrismaElementType("LEGACY_REQUIRED_TYPE");
  IElementType LIST_TYPE = new PrismaElementType("LIST_TYPE");
  IElementType LITERAL_EXPRESSION = new PrismaElementType("LITERAL_EXPRESSION");
  IElementType MODEL_DECLARATION = PrismaStubElementTypeFactory.create("MODEL_DECLARATION");
  IElementType NAMED_ARGUMENT = new PrismaElementType("NAMED_ARGUMENT");
  IElementType OPTIONAL_TYPE = new PrismaElementType("OPTIONAL_TYPE");
  IElementType PATH_EXPRESSION = new PrismaElementType("PATH_EXPRESSION");
  IElementType SINGLE_TYPE = new PrismaElementType("SINGLE_TYPE");
  IElementType TYPE_ALIAS = PrismaStubElementTypeFactory.create("TYPE_ALIAS");
  IElementType TYPE_DECLARATION = PrismaStubElementTypeFactory.create("TYPE_DECLARATION");
  IElementType TYPE_REFERENCE = new PrismaElementType("TYPE_REFERENCE");
  IElementType UNSUPPORTED_OPTIONAL_LIST_TYPE = new PrismaElementType("UNSUPPORTED_OPTIONAL_LIST_TYPE");
  IElementType UNSUPPORTED_TYPE = new PrismaElementType("UNSUPPORTED_TYPE");
  IElementType VALUE_ARGUMENT = new PrismaElementType("VALUE_ARGUMENT");
  IElementType VIEW_DECLARATION = PrismaStubElementTypeFactory.create("VIEW_DECLARATION");

  IElementType AT = new PrismaTokenType("@");
  IElementType ATAT = new PrismaTokenType("@@");
  IElementType BLOCK_COMMENT = new PrismaTokenType("BLOCK_COMMENT");
  IElementType COLON = new PrismaTokenType(":");
  IElementType COMMA = new PrismaTokenType(",");
  IElementType DATASOURCE = new PrismaTokenType("datasource");
  IElementType DOT = new PrismaTokenType(".");
  IElementType DOUBLE_COMMENT = new PrismaTokenType("DOUBLE_COMMENT");
  IElementType ENUM = new PrismaTokenType("enum");
  IElementType EQ = new PrismaTokenType("=");
  IElementType EXCL = new PrismaTokenType("!");
  IElementType GENERATOR = new PrismaTokenType("generator");
  IElementType IDENTIFIER = new PrismaTokenType("IDENTIFIER");
  IElementType LBRACE = new PrismaTokenType("{");
  IElementType LBRACKET = new PrismaTokenType("[");
  IElementType LPAREN = new PrismaTokenType("(");
  IElementType MODEL = new PrismaTokenType("model");
  IElementType NUMERIC_LITERAL = new PrismaTokenType("NUMERIC_LITERAL");
  IElementType QUEST = new PrismaTokenType("?");
  IElementType RBRACE = new PrismaTokenType("}");
  IElementType RBRACKET = new PrismaTokenType("]");
  IElementType RPAREN = new PrismaTokenType(")");
  IElementType STRING_LITERAL = new PrismaTokenType("STRING_LITERAL");
  IElementType TRIPLE_COMMENT = new PrismaTokenType("TRIPLE_COMMENT");
  IElementType TYPE = new PrismaTokenType("type");
  IElementType UNSUPPORTED = new PrismaTokenType("Unsupported");
  IElementType VIEW = new PrismaTokenType("view");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ARGUMENTS_LIST) {
        return new PrismaArgumentsListImpl(node);
      }
      else if (type == ARRAY_EXPRESSION) {
        return new PrismaArrayExpressionImpl(node);
      }
      else if (type == BLOCK_ATTRIBUTE) {
        return new PrismaBlockAttributeImpl(node);
      }
      else if (type == DATASOURCE_DECLARATION) {
        return new PrismaDatasourceDeclarationImpl(node);
      }
      else if (type == ENUM_DECLARATION) {
        return new PrismaEnumDeclarationImpl(node);
      }
      else if (type == ENUM_DECLARATION_BLOCK) {
        return new PrismaEnumDeclarationBlockImpl(node);
      }
      else if (type == ENUM_VALUE_DECLARATION) {
        return new PrismaEnumValueDeclarationImpl(node);
      }
      else if (type == FIELD_ATTRIBUTE) {
        return new PrismaFieldAttributeImpl(node);
      }
      else if (type == FIELD_DECLARATION) {
        return new PrismaFieldDeclarationImpl(node);
      }
      else if (type == FIELD_DECLARATION_BLOCK) {
        return new PrismaFieldDeclarationBlockImpl(node);
      }
      else if (type == FUNCTION_CALL) {
        return new PrismaFunctionCallImpl(node);
      }
      else if (type == GENERATOR_DECLARATION) {
        return new PrismaGeneratorDeclarationImpl(node);
      }
      else if (type == KEY_VALUE) {
        return new PrismaKeyValueImpl(node);
      }
      else if (type == KEY_VALUE_BLOCK) {
        return new PrismaKeyValueBlockImpl(node);
      }
      else if (type == LEGACY_LIST_TYPE) {
        return new PrismaLegacyListTypeImpl(node);
      }
      else if (type == LEGACY_REQUIRED_TYPE) {
        return new PrismaLegacyRequiredTypeImpl(node);
      }
      else if (type == LIST_TYPE) {
        return new PrismaListTypeImpl(node);
      }
      else if (type == LITERAL_EXPRESSION) {
        return new PrismaLiteralExpressionImpl(node);
      }
      else if (type == MODEL_DECLARATION) {
        return new PrismaModelDeclarationImpl(node);
      }
      else if (type == NAMED_ARGUMENT) {
        return new PrismaNamedArgumentImpl(node);
      }
      else if (type == OPTIONAL_TYPE) {
        return new PrismaOptionalTypeImpl(node);
      }
      else if (type == PATH_EXPRESSION) {
        return new PrismaPathExpressionImpl(node);
      }
      else if (type == SINGLE_TYPE) {
        return new PrismaSingleTypeImpl(node);
      }
      else if (type == TYPE_ALIAS) {
        return new PrismaTypeAliasImpl(node);
      }
      else if (type == TYPE_DECLARATION) {
        return new PrismaTypeDeclarationImpl(node);
      }
      else if (type == TYPE_REFERENCE) {
        return new PrismaTypeReferenceImpl(node);
      }
      else if (type == UNSUPPORTED_OPTIONAL_LIST_TYPE) {
        return new PrismaUnsupportedOptionalListTypeImpl(node);
      }
      else if (type == UNSUPPORTED_TYPE) {
        return new PrismaUnsupportedTypeImpl(node);
      }
      else if (type == VALUE_ARGUMENT) {
        return new PrismaValueArgumentImpl(node);
      }
      else if (type == VIEW_DECLARATION) {
        return new PrismaViewDeclarationImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
