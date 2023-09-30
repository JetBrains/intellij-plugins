// This is a generated file. Not intended for manual editing.
package com.intellij.jhipster.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.jhipster.psi.impl.*;

public interface JdlTokenTypes {

  IElementType ANNOTATION = new JdlElementType("ANNOTATION");
  IElementType ANNOTATION_ID = new JdlElementType("ANNOTATION_ID");
  IElementType ANNOTATION_VALUE = new JdlElementType("ANNOTATION_VALUE");
  IElementType APPLICATION = new JdlElementType("APPLICATION");
  IElementType ARRAY_LITERAL = new JdlElementType("ARRAY_LITERAL");
  IElementType BOOLEAN_LITERAL = new JdlElementType("BOOLEAN_LITERAL");
  IElementType CONFIGURATION_OPTION = new JdlElementType("CONFIGURATION_OPTION");
  IElementType CONFIGURATION_OPTION_NAME = new JdlElementType("CONFIGURATION_OPTION_NAME");
  IElementType CONFIGURATION_OPTION_VALUES = new JdlElementType("CONFIGURATION_OPTION_VALUES");
  IElementType CONFIG_BLOCK = new JdlElementType("CONFIG_BLOCK");
  IElementType CONFIG_KEYWORD = new JdlElementType("CONFIG_KEYWORD");
  IElementType CONSTANT = new JdlElementType("CONSTANT");
  IElementType CONSTANT_NAME = new JdlElementType("CONSTANT_NAME");
  IElementType DEPLOYMENT = new JdlElementType("DEPLOYMENT");
  IElementType ENTITIES_LIST = new JdlElementType("ENTITIES_LIST");
  IElementType ENTITY = new JdlElementType("ENTITY");
  IElementType ENTITY_FIELD_MAPPING = new JdlElementType("ENTITY_FIELD_MAPPING");
  IElementType ENTITY_ID = new JdlElementType("ENTITY_ID");
  IElementType ENTITY_TABLE_NAME = new JdlElementType("ENTITY_TABLE_NAME");
  IElementType ENUM = new JdlElementType("ENUM");
  IElementType ENUM_ID = new JdlElementType("ENUM_ID");
  IElementType ENUM_KEY = new JdlElementType("ENUM_KEY");
  IElementType ENUM_VALUE = new JdlElementType("ENUM_VALUE");
  IElementType EXCEPT_ENTITIES = new JdlElementType("EXCEPT_ENTITIES");
  IElementType EXPLICIT_ENUM_MAPPING = new JdlElementType("EXPLICIT_ENUM_MAPPING");
  IElementType FIELD_CONSTRAINT = new JdlElementType("FIELD_CONSTRAINT");
  IElementType FIELD_CONSTRAINT_ID = new JdlElementType("FIELD_CONSTRAINT_ID");
  IElementType FIELD_CONSTRAINT_PARAMETERS = new JdlElementType("FIELD_CONSTRAINT_PARAMETERS");
  IElementType FIELD_NAME = new JdlElementType("FIELD_NAME");
  IElementType FIELD_NAME_REF = new JdlElementType("FIELD_NAME_REF");
  IElementType FIELD_TYPE = new JdlElementType("FIELD_TYPE");
  IElementType ID = new JdlElementType("ID");
  IElementType NUMBER_LITERAL = new JdlElementType("NUMBER_LITERAL");
  IElementType OPTION_NAME = new JdlElementType("OPTION_NAME");
  IElementType OPTION_NAME_VALUE = new JdlElementType("OPTION_NAME_VALUE");
  IElementType REGEX_LITERAL = new JdlElementType("REGEX_LITERAL");
  IElementType RELATIONSHIP_DETAILS = new JdlElementType("RELATIONSHIP_DETAILS");
  IElementType RELATIONSHIP_ENTITY = new JdlElementType("RELATIONSHIP_ENTITY");
  IElementType RELATIONSHIP_GROUP = new JdlElementType("RELATIONSHIP_GROUP");
  IElementType RELATIONSHIP_MAPPING = new JdlElementType("RELATIONSHIP_MAPPING");
  IElementType RELATIONSHIP_OPTION = new JdlElementType("RELATIONSHIP_OPTION");
  IElementType RELATIONSHIP_OPTION_ID = new JdlElementType("RELATIONSHIP_OPTION_ID");
  IElementType RELATIONSHIP_TYPE = new JdlElementType("RELATIONSHIP_TYPE");
  IElementType STRING_LITERAL = new JdlElementType("STRING_LITERAL");
  IElementType USE_CONFIGURATION_OPTION = new JdlElementType("USE_CONFIGURATION_OPTION");
  IElementType VALUE = new JdlElementType("VALUE");
  IElementType WILDCARD_LITERAL = new JdlElementType("WILDCARD_LITERAL");
  IElementType WITH_OPTION_VALUE = new JdlElementType("WITH_OPTION_VALUE");

  IElementType APPLICATION_KEYWORD = new JdlTokenType("application");
  IElementType ASSIGN = new JdlTokenType("=");
  IElementType BLOCK_COMMENT = new JdlTokenType("BLOCK_COMMENT");
  IElementType COLON = new JdlTokenType(":");
  IElementType COMMA = new JdlTokenType(",");
  IElementType DEPLOYMENT_KEYWORD = new JdlTokenType("deployment");
  IElementType DOUBLE_NUMBER = new JdlTokenType("DOUBLE_NUMBER");
  IElementType DOUBLE_QUOTED_STRING = new JdlTokenType("DOUBLE_QUOTED_STRING");
  IElementType ENTITY_KEYWORD = new JdlTokenType("entity");
  IElementType ENUM_KEYWORD = new JdlTokenType("enum");
  IElementType EXCEPT_KEYWORD = new JdlTokenType("except");
  IElementType FALSE = new JdlTokenType("false");
  IElementType FOR_KEYWORD = new JdlTokenType("for");
  IElementType IDENTIFIER = new JdlTokenType("IDENTIFIER");
  IElementType INTEGER_NUMBER = new JdlTokenType("INTEGER_NUMBER");
  IElementType LBRACE = new JdlTokenType("{");
  IElementType LBRACKET = new JdlTokenType("[");
  IElementType LINE_COMMENT = new JdlTokenType("LINE_COMMENT");
  IElementType LPARENTH = new JdlTokenType("(");
  IElementType NEWLINE = new JdlTokenType("NEWLINE");
  IElementType RBRACE = new JdlTokenType("}");
  IElementType RBRACKET = new JdlTokenType("]");
  IElementType REGEX_STRING = new JdlTokenType("REGEX_STRING");
  IElementType RELATIONSHIP_KEYWORD = new JdlTokenType("relationship");
  IElementType RPARENTH = new JdlTokenType(")");
  IElementType STRUDEL = new JdlTokenType("@");
  IElementType TO_KEYWORD = new JdlTokenType("to");
  IElementType TRUE = new JdlTokenType("true");
  IElementType USE_KEYWORD = new JdlTokenType("use");
  IElementType WILDCARD = new JdlTokenType("*");
  IElementType WITH_KEYWORD = new JdlTokenType("with");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ANNOTATION) {
        return new JdlAnnotationImpl(node);
      }
      else if (type == ANNOTATION_ID) {
        return new JdlAnnotationIdImpl(node);
      }
      else if (type == ANNOTATION_VALUE) {
        return new JdlAnnotationValueImpl(node);
      }
      else if (type == APPLICATION) {
        return new JdlApplicationImpl(node);
      }
      else if (type == ARRAY_LITERAL) {
        return new JdlArrayLiteralImpl(node);
      }
      else if (type == BOOLEAN_LITERAL) {
        return new JdlBooleanLiteralImpl(node);
      }
      else if (type == CONFIGURATION_OPTION) {
        return new JdlConfigurationOptionImpl(node);
      }
      else if (type == CONFIGURATION_OPTION_NAME) {
        return new JdlConfigurationOptionNameImpl(node);
      }
      else if (type == CONFIGURATION_OPTION_VALUES) {
        return new JdlConfigurationOptionValuesImpl(node);
      }
      else if (type == CONFIG_BLOCK) {
        return new JdlConfigBlockImpl(node);
      }
      else if (type == CONFIG_KEYWORD) {
        return new JdlConfigKeywordImpl(node);
      }
      else if (type == CONSTANT) {
        return new JdlConstantImpl(node);
      }
      else if (type == CONSTANT_NAME) {
        return new JdlConstantNameImpl(node);
      }
      else if (type == DEPLOYMENT) {
        return new JdlDeploymentImpl(node);
      }
      else if (type == ENTITIES_LIST) {
        return new JdlEntitiesListImpl(node);
      }
      else if (type == ENTITY) {
        return new JdlEntityImpl(node);
      }
      else if (type == ENTITY_FIELD_MAPPING) {
        return new JdlEntityFieldMappingImpl(node);
      }
      else if (type == ENTITY_ID) {
        return new JdlEntityIdImpl(node);
      }
      else if (type == ENTITY_TABLE_NAME) {
        return new JdlEntityTableNameImpl(node);
      }
      else if (type == ENUM) {
        return new JdlEnumImpl(node);
      }
      else if (type == ENUM_ID) {
        return new JdlEnumIdImpl(node);
      }
      else if (type == ENUM_KEY) {
        return new JdlEnumKeyImpl(node);
      }
      else if (type == ENUM_VALUE) {
        return new JdlEnumValueImpl(node);
      }
      else if (type == EXCEPT_ENTITIES) {
        return new JdlExceptEntitiesImpl(node);
      }
      else if (type == EXPLICIT_ENUM_MAPPING) {
        return new JdlExplicitEnumMappingImpl(node);
      }
      else if (type == FIELD_CONSTRAINT) {
        return new JdlFieldConstraintImpl(node);
      }
      else if (type == FIELD_CONSTRAINT_ID) {
        return new JdlFieldConstraintIdImpl(node);
      }
      else if (type == FIELD_CONSTRAINT_PARAMETERS) {
        return new JdlFieldConstraintParametersImpl(node);
      }
      else if (type == FIELD_NAME) {
        return new JdlFieldNameImpl(node);
      }
      else if (type == FIELD_NAME_REF) {
        return new JdlFieldNameRefImpl(node);
      }
      else if (type == FIELD_TYPE) {
        return new JdlFieldTypeImpl(node);
      }
      else if (type == ID) {
        return new JdlIdImpl(node);
      }
      else if (type == NUMBER_LITERAL) {
        return new JdlNumberLiteralImpl(node);
      }
      else if (type == OPTION_NAME) {
        return new JdlOptionNameImpl(node);
      }
      else if (type == OPTION_NAME_VALUE) {
        return new JdlOptionNameValueImpl(node);
      }
      else if (type == REGEX_LITERAL) {
        return new JdlRegexLiteralImpl(node);
      }
      else if (type == RELATIONSHIP_DETAILS) {
        return new JdlRelationshipDetailsImpl(node);
      }
      else if (type == RELATIONSHIP_ENTITY) {
        return new JdlRelationshipEntityImpl(node);
      }
      else if (type == RELATIONSHIP_GROUP) {
        return new JdlRelationshipGroupImpl(node);
      }
      else if (type == RELATIONSHIP_MAPPING) {
        return new JdlRelationshipMappingImpl(node);
      }
      else if (type == RELATIONSHIP_OPTION) {
        return new JdlRelationshipOptionImpl(node);
      }
      else if (type == RELATIONSHIP_OPTION_ID) {
        return new JdlRelationshipOptionIdImpl(node);
      }
      else if (type == RELATIONSHIP_TYPE) {
        return new JdlRelationshipTypeImpl(node);
      }
      else if (type == STRING_LITERAL) {
        return new JdlStringLiteralImpl(node);
      }
      else if (type == USE_CONFIGURATION_OPTION) {
        return new JdlUseConfigurationOptionImpl(node);
      }
      else if (type == WILDCARD_LITERAL) {
        return new JdlWildcardLiteralImpl(node);
      }
      else if (type == WITH_OPTION_VALUE) {
        return new JdlWithOptionValueImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
