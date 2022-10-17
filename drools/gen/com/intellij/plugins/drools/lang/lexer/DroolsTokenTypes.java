// This is a generated file. Not intended for manual editing.
package com.intellij.plugins.drools.lang.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.plugins.drools.lang.psi.impl.*;

public interface DroolsTokenTypes {

  IElementType ACCUMULATE_FUNCTION = new DroolsElementType("ACCUMULATE_FUNCTION");
  IElementType ACCUMULATE_FUNCTION_BINDING = new DroolsElementType("ACCUMULATE_FUNCTION_BINDING");
  IElementType ACCUMULATE_PARAMETERS = new DroolsElementType("ACCUMULATE_PARAMETERS");
  IElementType ADDITIVE_EXPR = new DroolsElementType("ADDITIVE_EXPR");
  IElementType AND_EXPR = new DroolsElementType("AND_EXPR");
  IElementType ANNOTATION = new DroolsElementType("ANNOTATION");
  IElementType ARGUMENTS = new DroolsElementType("ARGUMENTS");
  IElementType ARRAY_CREATOR_REST = new DroolsElementType("ARRAY_CREATOR_REST");
  IElementType ARRAY_INITIALIZER = new DroolsElementType("ARRAY_INITIALIZER");
  IElementType ASSIGNMENT_EXPR = new DroolsElementType("ASSIGNMENT_EXPR");
  IElementType ASSIGNMENT_OPERATOR = new DroolsElementType("ASSIGNMENT_OPERATOR");
  IElementType ATTRIBUTE = new DroolsElementType("ATTRIBUTE");
  IElementType BLOCK = new DroolsElementType("BLOCK");
  IElementType BOOLEAN_LITERAL = new DroolsElementType("BOOLEAN_LITERAL");
  IElementType CAST_EXPR = new DroolsElementType("CAST_EXPR");
  IElementType CHUNK = new DroolsElementType("CHUNK");
  IElementType CLASS_CREATOR_REST = new DroolsElementType("CLASS_CREATOR_REST");
  IElementType CONDITIONAL_AND_EXPR = new DroolsElementType("CONDITIONAL_AND_EXPR");
  IElementType CONDITIONAL_ELEMENT = new DroolsElementType("CONDITIONAL_ELEMENT");
  IElementType CONDITIONAL_EXPR = new DroolsElementType("CONDITIONAL_EXPR");
  IElementType CONDITIONAL_OR_EXPR = new DroolsElementType("CONDITIONAL_OR_EXPR");
  IElementType CONSEQUENCE_ID = new DroolsElementType("CONSEQUENCE_ID");
  IElementType CONSTRAINT = new DroolsElementType("CONSTRAINT");
  IElementType CREATED_QUALIFIED_IDENTIFIER = new DroolsElementType("CREATED_QUALIFIED_IDENTIFIER");
  IElementType CREATOR = new DroolsElementType("CREATOR");
  IElementType DECIMAL = new DroolsElementType("DECIMAL");
  IElementType DECLARE_STATEMENT = new DroolsElementType("DECLARE_STATEMENT");
  IElementType ELEMENT_VALUE = new DroolsElementType("ELEMENT_VALUE");
  IElementType ELEMENT_VALUE_ARRAY_INITIALIZER = new DroolsElementType("ELEMENT_VALUE_ARRAY_INITIALIZER");
  IElementType ELEMENT_VALUE_PAIR = new DroolsElementType("ELEMENT_VALUE_PAIR");
  IElementType ELEMENT_VALUE_PAIRS = new DroolsElementType("ELEMENT_VALUE_PAIRS");
  IElementType ENTRY_POINT_DECLARATION = new DroolsElementType("ENTRY_POINT_DECLARATION");
  IElementType ENTRY_POINT_NAME = new DroolsElementType("ENTRY_POINT_NAME");
  IElementType ENUMERATIVE = new DroolsElementType("ENUMERATIVE");
  IElementType ENUM_DECLARATION = new DroolsElementType("ENUM_DECLARATION");
  IElementType EQUALITY_EXPR = new DroolsElementType("EQUALITY_EXPR");
  IElementType EXCLUSIVE_OR_EXPR = new DroolsElementType("EXCLUSIVE_OR_EXPR");
  IElementType EXPLICIT_GENERIC_INVOCATION = new DroolsElementType("EXPLICIT_GENERIC_INVOCATION");
  IElementType EXPLICIT_GENERIC_INVOCATION_SUFFIX = new DroolsElementType("EXPLICIT_GENERIC_INVOCATION_SUFFIX");
  IElementType EXPRESSION = new DroolsElementType("EXPRESSION");
  IElementType FIELD = new DroolsElementType("FIELD");
  IElementType FIELD_NAME = new DroolsElementType("FIELD_NAME");
  IElementType FIELD_TYPE = new DroolsElementType("FIELD_TYPE");
  IElementType FILTER_DEF = new DroolsElementType("FILTER_DEF");
  IElementType FROM_ACCUMULATE = new DroolsElementType("FROM_ACCUMULATE");
  IElementType FROM_COLLECT = new DroolsElementType("FROM_COLLECT");
  IElementType FROM_ENTRY_POINT = new DroolsElementType("FROM_ENTRY_POINT");
  IElementType FROM_EXPRESSION = new DroolsElementType("FROM_EXPRESSION");
  IElementType FROM_WINDOW = new DroolsElementType("FROM_WINDOW");
  IElementType FUNCTION_STATEMENT = new DroolsElementType("FUNCTION_STATEMENT");
  IElementType GLOBAL_STATEMENT = new DroolsElementType("GLOBAL_STATEMENT");
  IElementType IDENTIFIER = new DroolsElementType("IDENTIFIER");
  IElementType IDENTIFIER_SUFFIX = new DroolsElementType("IDENTIFIER_SUFFIX");
  IElementType IMPORT_QUALIFIER = new DroolsElementType("IMPORT_QUALIFIER");
  IElementType IMPORT_STATEMENT = new DroolsElementType("IMPORT_STATEMENT");
  IElementType INCLUSIVE_OR_EXPR = new DroolsElementType("INCLUSIVE_OR_EXPR");
  IElementType INNER_CREATOR = new DroolsElementType("INNER_CREATOR");
  IElementType INSERT_LOGICAL_RHS_STATEMENT = new DroolsElementType("INSERT_LOGICAL_RHS_STATEMENT");
  IElementType INSERT_RHS_STATEMENT = new DroolsElementType("INSERT_RHS_STATEMENT");
  IElementType INSTANCE_OF_EXPR = new DroolsElementType("INSTANCE_OF_EXPR");
  IElementType IN_EXPR = new DroolsElementType("IN_EXPR");
  IElementType JAVA_RHS_STATEMENT = new DroolsElementType("JAVA_RHS_STATEMENT");
  IElementType LABEL = new DroolsElementType("LABEL");
  IElementType LHS = new DroolsElementType("LHS");
  IElementType LHS_ACCUMULATE = new DroolsElementType("LHS_ACCUMULATE");
  IElementType LHS_AND = new DroolsElementType("LHS_AND");
  IElementType LHS_EVAL = new DroolsElementType("LHS_EVAL");
  IElementType LHS_EXISTS = new DroolsElementType("LHS_EXISTS");
  IElementType LHS_EXPRESSION = new DroolsElementType("LHS_EXPRESSION");
  IElementType LHS_FORALL = new DroolsElementType("LHS_FORALL");
  IElementType LHS_NAMED_CONSEQUENCE = new DroolsElementType("LHS_NAMED_CONSEQUENCE");
  IElementType LHS_NOT = new DroolsElementType("LHS_NOT");
  IElementType LHS_OR = new DroolsElementType("LHS_OR");
  IElementType LHS_PAREN = new DroolsElementType("LHS_PAREN");
  IElementType LHS_PATTERN = new DroolsElementType("LHS_PATTERN");
  IElementType LHS_PATTERN_BIND = new DroolsElementType("LHS_PATTERN_BIND");
  IElementType LHS_PATTERN_TYPE = new DroolsElementType("LHS_PATTERN_TYPE");
  IElementType LHS_UNARY = new DroolsElementType("LHS_UNARY");
  IElementType MAP_ENTRY = new DroolsElementType("MAP_ENTRY");
  IElementType MAP_EXPRESSION_LIST = new DroolsElementType("MAP_EXPRESSION_LIST");
  IElementType MODIFY_PAR_EXPR = new DroolsElementType("MODIFY_PAR_EXPR");
  IElementType MODIFY_RHS_STATEMENT = new DroolsElementType("MODIFY_RHS_STATEMENT");
  IElementType MULTIPLICATIVE_EXPR = new DroolsElementType("MULTIPLICATIVE_EXPR");
  IElementType NAMESPACE = new DroolsElementType("NAMESPACE");
  IElementType NAME_ID = new DroolsElementType("NAME_ID");
  IElementType NON_WILDCARD_TYPE_ARGUMENTS = new DroolsElementType("NON_WILDCARD_TYPE_ARGUMENTS");
  IElementType NULL_LITERAL = new DroolsElementType("NULL_LITERAL");
  IElementType NUMBER_LITERAL = new DroolsElementType("NUMBER_LITERAL");
  IElementType OPERATOR = new DroolsElementType("OPERATOR");
  IElementType PACKAGE_STATEMENT = new DroolsElementType("PACKAGE_STATEMENT");
  IElementType PARAMETER = new DroolsElementType("PARAMETER");
  IElementType PARAMETERS = new DroolsElementType("PARAMETERS");
  IElementType PARENT_RULE = new DroolsElementType("PARENT_RULE");
  IElementType PAR_EXPR = new DroolsElementType("PAR_EXPR");
  IElementType PATTERN_FILTER = new DroolsElementType("PATTERN_FILTER");
  IElementType PATTERN_SOURCE = new DroolsElementType("PATTERN_SOURCE");
  IElementType PRIMARY_EXPR = new DroolsElementType("PRIMARY_EXPR");
  IElementType PRIMITIVE_TYPE = new DroolsElementType("PRIMITIVE_TYPE");
  IElementType QUALIFIED_IDENTIFIER = new DroolsElementType("QUALIFIED_IDENTIFIER");
  IElementType QUALIFIED_NAME = new DroolsElementType("QUALIFIED_NAME");
  IElementType QUERY_EXPRESSION = new DroolsElementType("QUERY_EXPRESSION");
  IElementType QUERY_STATEMENT = new DroolsElementType("QUERY_STATEMENT");
  IElementType RELATIONAL_EXPR = new DroolsElementType("RELATIONAL_EXPR");
  IElementType RELATIONAL_OPERATOR = new DroolsElementType("RELATIONAL_OPERATOR");
  IElementType RETRACT_RHS_STATEMENT = new DroolsElementType("RETRACT_RHS_STATEMENT");
  IElementType RHS = new DroolsElementType("RHS");
  IElementType RULE_ATTRIBUTES = new DroolsElementType("RULE_ATTRIBUTES");
  IElementType RULE_NAME = new DroolsElementType("RULE_NAME");
  IElementType RULE_STATEMENT = new DroolsElementType("RULE_STATEMENT");
  IElementType SELECTOR = new DroolsElementType("SELECTOR");
  IElementType SHIFT_EXPR = new DroolsElementType("SHIFT_EXPR");
  IElementType SIMPLE_NAME = new DroolsElementType("SIMPLE_NAME");
  IElementType SQUARE_ARGUMENTS = new DroolsElementType("SQUARE_ARGUMENTS");
  IElementType STRING_ID = new DroolsElementType("STRING_ID");
  IElementType STRING_LITERAL = new DroolsElementType("STRING_LITERAL");
  IElementType SUPER_SUFFIX = new DroolsElementType("SUPER_SUFFIX");
  IElementType SUPER_TYPE = new DroolsElementType("SUPER_TYPE");
  IElementType TRAITABLE = new DroolsElementType("TRAITABLE");
  IElementType TYPE = new DroolsElementType("TYPE");
  IElementType TYPE_ARGUMENT = new DroolsElementType("TYPE_ARGUMENT");
  IElementType TYPE_ARGUMENTS = new DroolsElementType("TYPE_ARGUMENTS");
  IElementType TYPE_DECLARATION = new DroolsElementType("TYPE_DECLARATION");
  IElementType TYPE_NAME = new DroolsElementType("TYPE_NAME");
  IElementType UNARY_2_EXPR = new DroolsElementType("UNARY_2_EXPR");
  IElementType UNARY_ASSIGN_EXPR = new DroolsElementType("UNARY_ASSIGN_EXPR");
  IElementType UNARY_EXPR = new DroolsElementType("UNARY_EXPR");
  IElementType UNARY_NOT_PLUS_MINUS_EXPR = new DroolsElementType("UNARY_NOT_PLUS_MINUS_EXPR");
  IElementType UPDATE_RHS_STATEMENT = new DroolsElementType("UPDATE_RHS_STATEMENT");
  IElementType VARIABLE_INITIALIZER = new DroolsElementType("VARIABLE_INITIALIZER");
  IElementType VAR_TYPE = new DroolsElementType("VAR_TYPE");
  IElementType WINDOW_DECLARATION = new DroolsElementType("WINDOW_DECLARATION");

  IElementType ACCUMULATE = DroolsElementFactory.getTokenType("accumulate");
  IElementType ACTION = DroolsElementFactory.getTokenType("action");
  IElementType ACTIVATION_GROUP = DroolsElementFactory.getTokenType("activation-group");
  IElementType AGENDA_GROUP = DroolsElementFactory.getTokenType("agenda-group");
  IElementType AND = DroolsElementFactory.getTokenType("and");
  IElementType ATTRIBUTES = DroolsElementFactory.getTokenType("attributes");
  IElementType AUTO_FOCUS = DroolsElementFactory.getTokenType("auto-focus");
  IElementType BLOCK_EXPRESSION = DroolsElementFactory.getTokenType("BLOCK_EXPRESSION");
  IElementType BOOLEAN = DroolsElementFactory.getTokenType("boolean");
  IElementType BREAK = DroolsElementFactory.getTokenType("break");
  IElementType BYTE = DroolsElementFactory.getTokenType("byte");
  IElementType CALENDARS = DroolsElementFactory.getTokenType("calendars");
  IElementType CHAR = DroolsElementFactory.getTokenType("char");
  IElementType CHARACTER_LITERAL = DroolsElementFactory.getTokenType("CHARACTER_LITERAL");
  IElementType CHUNK_BLOCK = DroolsElementFactory.getTokenType("CHUNK_BLOCK");
  IElementType COLLECT = DroolsElementFactory.getTokenType("collect");
  IElementType COLON = DroolsElementFactory.getTokenType(":");
  IElementType COMMA = DroolsElementFactory.getTokenType(",");
  IElementType CONTAINS = DroolsElementFactory.getTokenType("contains");
  IElementType DATE_EFFECTIVE = DroolsElementFactory.getTokenType("date-effective");
  IElementType DATE_EXPIRES = DroolsElementFactory.getTokenType("date-expires");
  IElementType DECLARE = DroolsElementFactory.getTokenType("declare");
  IElementType DIALECT = DroolsElementFactory.getTokenType("dialect");
  IElementType DO = DroolsElementFactory.getTokenType("do");
  IElementType DOT = DroolsElementFactory.getTokenType(".");
  IElementType DOUBLE = DroolsElementFactory.getTokenType("double");
  IElementType DURATION = DroolsElementFactory.getTokenType("duration");
  IElementType ENABLED = DroolsElementFactory.getTokenType("enabled");
  IElementType END = DroolsElementFactory.getTokenType("end");
  IElementType ENTRY_POINT = DroolsElementFactory.getTokenType("entry-point");
  IElementType EQ = DroolsElementFactory.getTokenType("EQ");
  IElementType EVAL = DroolsElementFactory.getTokenType("eval");
  IElementType EXISTS = DroolsElementFactory.getTokenType("exists");
  IElementType EXTENDS = DroolsElementFactory.getTokenType("extends");
  IElementType FALSE = DroolsElementFactory.getTokenType("false");
  IElementType FLOAT = DroolsElementFactory.getTokenType("float");
  IElementType FLOAT_TOKEN = DroolsElementFactory.getTokenType("FLOAT_TOKEN");
  IElementType FORALL = DroolsElementFactory.getTokenType("forall");
  IElementType FROM = DroolsElementFactory.getTokenType("from");
  IElementType FUNCTION = DroolsElementFactory.getTokenType("function");
  IElementType GLOBAL = DroolsElementFactory.getTokenType("global");
  IElementType IF = DroolsElementFactory.getTokenType("if");
  IElementType IMPORT = DroolsElementFactory.getTokenType("import");
  IElementType IN = DroolsElementFactory.getTokenType("in");
  IElementType INIT = DroolsElementFactory.getTokenType("init");
  IElementType INSERT = DroolsElementFactory.getTokenType("insert");
  IElementType INSERT_LOGICAL = DroolsElementFactory.getTokenType("insertLogical");
  IElementType INT = DroolsElementFactory.getTokenType("int");
  IElementType INT_TOKEN = DroolsElementFactory.getTokenType("INT_TOKEN");
  IElementType IS_A = DroolsElementFactory.getTokenType("isA");
  IElementType JAVA_IDENTIFIER = DroolsElementFactory.getTokenType("JAVA_IDENTIFIER");
  IElementType JAVA_STATEMENT = DroolsElementFactory.getTokenType("JAVA_STATEMENT");
  IElementType LBRACE = DroolsElementFactory.getTokenType("{");
  IElementType LBRACKET = DroolsElementFactory.getTokenType("[");
  IElementType LOCK_ON_ACTIVE = DroolsElementFactory.getTokenType("lock-on-active");
  IElementType LONG = DroolsElementFactory.getTokenType("long");
  IElementType LPAREN = DroolsElementFactory.getTokenType("(");
  IElementType MATCHES = DroolsElementFactory.getTokenType("matches");
  IElementType MEMBEROF = DroolsElementFactory.getTokenType("memberOf");
  IElementType MODIFY = DroolsElementFactory.getTokenType("modify");
  IElementType NOT = DroolsElementFactory.getTokenType("not");
  IElementType NO_LOOP = DroolsElementFactory.getTokenType("no-loop");
  IElementType NULL = DroolsElementFactory.getTokenType("null");
  IElementType OP_ASSIGN = DroolsElementFactory.getTokenType("=");
  IElementType OP_AT = DroolsElementFactory.getTokenType("@");
  IElementType OP_BIT_AND = DroolsElementFactory.getTokenType("&");
  IElementType OP_BIT_AND_ASSIGN = DroolsElementFactory.getTokenType("&=");
  IElementType OP_BIT_OR = DroolsElementFactory.getTokenType("|");
  IElementType OP_BIT_OR_ASSIGN = DroolsElementFactory.getTokenType("|=");
  IElementType OP_BIT_XOR = DroolsElementFactory.getTokenType("^");
  IElementType OP_BIT_XOR_ASSIGN = DroolsElementFactory.getTokenType("^=");
  IElementType OP_BSR_ASSIGN = DroolsElementFactory.getTokenType(">>>=");
  IElementType OP_COMPLEMENT = DroolsElementFactory.getTokenType("~");
  IElementType OP_COND_AND = DroolsElementFactory.getTokenType("&&");
  IElementType OP_COND_OR = DroolsElementFactory.getTokenType("||");
  IElementType OP_DIV = DroolsElementFactory.getTokenType("/");
  IElementType OP_DIV_ASSIGN = DroolsElementFactory.getTokenType("/=");
  IElementType OP_EQ = DroolsElementFactory.getTokenType("==");
  IElementType OP_GREATER = DroolsElementFactory.getTokenType(">");
  IElementType OP_GREATER_OR_EQUAL = DroolsElementFactory.getTokenType(">=");
  IElementType OP_LESS = DroolsElementFactory.getTokenType("<");
  IElementType OP_LESS_OR_EQUAL = DroolsElementFactory.getTokenType("<=");
  IElementType OP_MINUS = DroolsElementFactory.getTokenType("-");
  IElementType OP_MINUS_ASSIGN = DroolsElementFactory.getTokenType("-=");
  IElementType OP_MINUS_MINUS = DroolsElementFactory.getTokenType("--");
  IElementType OP_MUL = DroolsElementFactory.getTokenType("*");
  IElementType OP_MUL_ASSIGN = DroolsElementFactory.getTokenType("*=");
  IElementType OP_NOT = DroolsElementFactory.getTokenType("!");
  IElementType OP_NOT_EQ = DroolsElementFactory.getTokenType("!=");
  IElementType OP_PLUS = DroolsElementFactory.getTokenType("+");
  IElementType OP_PLUS_ASSIGN = DroolsElementFactory.getTokenType("+=");
  IElementType OP_PLUS_PLUS = DroolsElementFactory.getTokenType("++");
  IElementType OP_REMAINDER = DroolsElementFactory.getTokenType("%");
  IElementType OP_REMAINDER_ASSIGN = DroolsElementFactory.getTokenType("%=");
  IElementType OP_SL_ASSIGN = DroolsElementFactory.getTokenType("<<=");
  IElementType OP_SR_ASSIGN = DroolsElementFactory.getTokenType(">>=");
  IElementType OR = DroolsElementFactory.getTokenType("or");
  IElementType OVER = DroolsElementFactory.getTokenType("over");
  IElementType PACKAGE = DroolsElementFactory.getTokenType("package");
  IElementType QUERY = DroolsElementFactory.getTokenType("query");
  IElementType QUEST = DroolsElementFactory.getTokenType("?");
  IElementType RBRACE = DroolsElementFactory.getTokenType("}");
  IElementType RBRACKET = DroolsElementFactory.getTokenType("]");
  IElementType RESULT = DroolsElementFactory.getTokenType("result");
  IElementType RETRACT = DroolsElementFactory.getTokenType("retract");
  IElementType REVERSE = DroolsElementFactory.getTokenType("reverse");
  IElementType RPAREN = DroolsElementFactory.getTokenType(")");
  IElementType RULE = DroolsElementFactory.getTokenType("rule");
  IElementType RULEFLOW_GROUP = DroolsElementFactory.getTokenType("ruleflow-group");
  IElementType SALIENCE = DroolsElementFactory.getTokenType("salience");
  IElementType SEMICOLON = DroolsElementFactory.getTokenType(";");
  IElementType SHORT = DroolsElementFactory.getTokenType("short");
  IElementType SOUNDSLIKE = DroolsElementFactory.getTokenType("soundslike");
  IElementType STRING_TOKEN = DroolsElementFactory.getTokenType("STRING_TOKEN");
  IElementType TEMPLATE = DroolsElementFactory.getTokenType("template");
  IElementType THEN = DroolsElementFactory.getTokenType("then");
  IElementType THIS = DroolsElementFactory.getTokenType("this");
  IElementType TIMER = DroolsElementFactory.getTokenType("timer");
  IElementType TRUE = DroolsElementFactory.getTokenType("true");
  IElementType UPDATE = DroolsElementFactory.getTokenType("update");
  IElementType VOID = DroolsElementFactory.getTokenType("void");
  IElementType WHEN = DroolsElementFactory.getTokenType("when");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ACCUMULATE_FUNCTION) {
        return new DroolsAccumulateFunctionImpl(node);
      }
      else if (type == ACCUMULATE_FUNCTION_BINDING) {
        return new DroolsAccumulateFunctionBindingImpl(node);
      }
      else if (type == ACCUMULATE_PARAMETERS) {
        return new DroolsAccumulateParametersImpl(node);
      }
      else if (type == ADDITIVE_EXPR) {
        return new DroolsAdditiveExprImpl(node);
      }
      else if (type == AND_EXPR) {
        return new DroolsAndExprImpl(node);
      }
      else if (type == ANNOTATION) {
        return new DroolsAnnotationImpl(node);
      }
      else if (type == ARGUMENTS) {
        return new DroolsArgumentsImpl(node);
      }
      else if (type == ARRAY_CREATOR_REST) {
        return new DroolsArrayCreatorRestImpl(node);
      }
      else if (type == ARRAY_INITIALIZER) {
        return new DroolsArrayInitializerImpl(node);
      }
      else if (type == ASSIGNMENT_EXPR) {
        return new DroolsAssignmentExprImpl(node);
      }
      else if (type == ASSIGNMENT_OPERATOR) {
        return new DroolsAssignmentOperatorImpl(node);
      }
      else if (type == ATTRIBUTE) {
        return new DroolsAttributeImpl(node);
      }
      else if (type == BLOCK) {
        return new DroolsBlockImpl(node);
      }
      else if (type == BOOLEAN_LITERAL) {
        return new DroolsBooleanLiteralImpl(node);
      }
      else if (type == CAST_EXPR) {
        return new DroolsCastExprImpl(node);
      }
      else if (type == CHUNK) {
        return new DroolsChunkImpl(node);
      }
      else if (type == CLASS_CREATOR_REST) {
        return new DroolsClassCreatorRestImpl(node);
      }
      else if (type == CONDITIONAL_AND_EXPR) {
        return new DroolsConditionalAndExprImpl(node);
      }
      else if (type == CONDITIONAL_ELEMENT) {
        return new DroolsConditionalElementImpl(node);
      }
      else if (type == CONDITIONAL_EXPR) {
        return new DroolsConditionalExprImpl(node);
      }
      else if (type == CONDITIONAL_OR_EXPR) {
        return new DroolsConditionalOrExprImpl(node);
      }
      else if (type == CONSEQUENCE_ID) {
        return new DroolsConsequenceIdImpl(node);
      }
      else if (type == CONSTRAINT) {
        return new DroolsConstraintImpl(node);
      }
      else if (type == CREATED_QUALIFIED_IDENTIFIER) {
        return new DroolsCreatedQualifiedIdentifierImpl(node);
      }
      else if (type == CREATOR) {
        return new DroolsCreatorImpl(node);
      }
      else if (type == DECIMAL) {
        return new DroolsDecimalImpl(node);
      }
      else if (type == DECLARE_STATEMENT) {
        return new DroolsDeclareStatementImpl(node);
      }
      else if (type == ELEMENT_VALUE) {
        return new DroolsElementValueImpl(node);
      }
      else if (type == ELEMENT_VALUE_ARRAY_INITIALIZER) {
        return new DroolsElementValueArrayInitializerImpl(node);
      }
      else if (type == ELEMENT_VALUE_PAIR) {
        return new DroolsElementValuePairImpl(node);
      }
      else if (type == ELEMENT_VALUE_PAIRS) {
        return new DroolsElementValuePairsImpl(node);
      }
      else if (type == ENTRY_POINT_DECLARATION) {
        return new DroolsEntryPointDeclarationImpl(node);
      }
      else if (type == ENTRY_POINT_NAME) {
        return new DroolsEntryPointNameImpl(node);
      }
      else if (type == ENUMERATIVE) {
        return new DroolsEnumerativeImpl(node);
      }
      else if (type == ENUM_DECLARATION) {
        return new DroolsEnumDeclarationImpl(node);
      }
      else if (type == EQUALITY_EXPR) {
        return new DroolsEqualityExprImpl(node);
      }
      else if (type == EXCLUSIVE_OR_EXPR) {
        return new DroolsExclusiveOrExprImpl(node);
      }
      else if (type == EXPLICIT_GENERIC_INVOCATION) {
        return new DroolsExplicitGenericInvocationImpl(node);
      }
      else if (type == EXPLICIT_GENERIC_INVOCATION_SUFFIX) {
        return new DroolsExplicitGenericInvocationSuffixImpl(node);
      }
      else if (type == FIELD) {
        return new DroolsFieldImpl(node);
      }
      else if (type == FIELD_NAME) {
        return new DroolsFieldNameImpl(node);
      }
      else if (type == FIELD_TYPE) {
        return new DroolsFieldTypeImpl(node);
      }
      else if (type == FILTER_DEF) {
        return new DroolsFilterDefImpl(node);
      }
      else if (type == FROM_ACCUMULATE) {
        return new DroolsFromAccumulateImpl(node);
      }
      else if (type == FROM_COLLECT) {
        return new DroolsFromCollectImpl(node);
      }
      else if (type == FROM_ENTRY_POINT) {
        return new DroolsFromEntryPointImpl(node);
      }
      else if (type == FROM_EXPRESSION) {
        return new DroolsFromExpressionImpl(node);
      }
      else if (type == FROM_WINDOW) {
        return new DroolsFromWindowImpl(node);
      }
      else if (type == FUNCTION_STATEMENT) {
        return new DroolsFunctionStatementImpl(node);
      }
      else if (type == GLOBAL_STATEMENT) {
        return new DroolsGlobalStatementImpl(node);
      }
      else if (type == IDENTIFIER) {
        return new DroolsIdentifierImpl(node);
      }
      else if (type == IDENTIFIER_SUFFIX) {
        return new DroolsIdentifierSuffixImpl(node);
      }
      else if (type == IMPORT_QUALIFIER) {
        return new DroolsImportQualifierImpl(node);
      }
      else if (type == IMPORT_STATEMENT) {
        return new DroolsImportStatementImpl(node);
      }
      else if (type == INCLUSIVE_OR_EXPR) {
        return new DroolsInclusiveOrExprImpl(node);
      }
      else if (type == INNER_CREATOR) {
        return new DroolsInnerCreatorImpl(node);
      }
      else if (type == INSERT_LOGICAL_RHS_STATEMENT) {
        return new DroolsInsertLogicalRhsStatementImpl(node);
      }
      else if (type == INSERT_RHS_STATEMENT) {
        return new DroolsInsertRhsStatementImpl(node);
      }
      else if (type == INSTANCE_OF_EXPR) {
        return new DroolsInstanceOfExprImpl(node);
      }
      else if (type == IN_EXPR) {
        return new DroolsInExprImpl(node);
      }
      else if (type == JAVA_RHS_STATEMENT) {
        return new DroolsJavaRhsStatementImpl(node);
      }
      else if (type == LABEL) {
        return new DroolsLabelImpl(node);
      }
      else if (type == LHS) {
        return new DroolsLhsImpl(node);
      }
      else if (type == LHS_ACCUMULATE) {
        return new DroolsLhsAccumulateImpl(node);
      }
      else if (type == LHS_AND) {
        return new DroolsLhsAndImpl(node);
      }
      else if (type == LHS_EVAL) {
        return new DroolsLhsEvalImpl(node);
      }
      else if (type == LHS_EXISTS) {
        return new DroolsLhsExistsImpl(node);
      }
      else if (type == LHS_EXPRESSION) {
        return new DroolsLhsExpressionImpl(node);
      }
      else if (type == LHS_FORALL) {
        return new DroolsLhsForallImpl(node);
      }
      else if (type == LHS_NAMED_CONSEQUENCE) {
        return new DroolsLhsNamedConsequenceImpl(node);
      }
      else if (type == LHS_NOT) {
        return new DroolsLhsNotImpl(node);
      }
      else if (type == LHS_OR) {
        return new DroolsLhsOrImpl(node);
      }
      else if (type == LHS_PAREN) {
        return new DroolsLhsParenImpl(node);
      }
      else if (type == LHS_PATTERN) {
        return new DroolsLhsPatternImpl(node);
      }
      else if (type == LHS_PATTERN_BIND) {
        return new DroolsLhsPatternBindImpl(node);
      }
      else if (type == LHS_PATTERN_TYPE) {
        return new DroolsLhsPatternTypeImpl(node);
      }
      else if (type == LHS_UNARY) {
        return new DroolsLhsUnaryImpl(node);
      }
      else if (type == MAP_ENTRY) {
        return new DroolsMapEntryImpl(node);
      }
      else if (type == MAP_EXPRESSION_LIST) {
        return new DroolsMapExpressionListImpl(node);
      }
      else if (type == MODIFY_RHS_STATEMENT) {
        return new DroolsModifyRhsStatementImpl(node);
      }
      else if (type == MULTIPLICATIVE_EXPR) {
        return new DroolsMultiplicativeExprImpl(node);
      }
      else if (type == NAMESPACE) {
        return new DroolsNamespaceImpl(node);
      }
      else if (type == NAME_ID) {
        return new DroolsNameIdImpl(node);
      }
      else if (type == NON_WILDCARD_TYPE_ARGUMENTS) {
        return new DroolsNonWildcardTypeArgumentsImpl(node);
      }
      else if (type == NULL_LITERAL) {
        return new DroolsNullLiteralImpl(node);
      }
      else if (type == NUMBER_LITERAL) {
        return new DroolsNumberLiteralImpl(node);
      }
      else if (type == OPERATOR) {
        return new DroolsOperatorImpl(node);
      }
      else if (type == PACKAGE_STATEMENT) {
        return new DroolsPackageStatementImpl(node);
      }
      else if (type == PARAMETER) {
        return new DroolsParameterImpl(node);
      }
      else if (type == PARAMETERS) {
        return new DroolsParametersImpl(node);
      }
      else if (type == PARENT_RULE) {
        return new DroolsParentRuleImpl(node);
      }
      else if (type == PAR_EXPR) {
        return new DroolsParExprImpl(node);
      }
      else if (type == PATTERN_FILTER) {
        return new DroolsPatternFilterImpl(node);
      }
      else if (type == PATTERN_SOURCE) {
        return new DroolsPatternSourceImpl(node);
      }
      else if (type == PRIMARY_EXPR) {
        return new DroolsPrimaryExprImpl(node);
      }
      else if (type == PRIMITIVE_TYPE) {
        return new DroolsPrimitiveTypeImpl(node);
      }
      else if (type == QUALIFIED_IDENTIFIER) {
        return new DroolsQualifiedIdentifierImpl(node);
      }
      else if (type == QUALIFIED_NAME) {
        return new DroolsQualifiedNameImpl(node);
      }
      else if (type == QUERY_EXPRESSION) {
        return new DroolsQueryExpressionImpl(node);
      }
      else if (type == QUERY_STATEMENT) {
        return new DroolsQueryStatementImpl(node);
      }
      else if (type == RELATIONAL_EXPR) {
        return new DroolsRelationalExprImpl(node);
      }
      else if (type == RELATIONAL_OPERATOR) {
        return new DroolsRelationalOperatorImpl(node);
      }
      else if (type == RETRACT_RHS_STATEMENT) {
        return new DroolsRetractRhsStatementImpl(node);
      }
      else if (type == RHS) {
        return new DroolsRhsImpl(node);
      }
      else if (type == RULE_ATTRIBUTES) {
        return new DroolsRuleAttributesImpl(node);
      }
      else if (type == RULE_NAME) {
        return new DroolsRuleNameImpl(node);
      }
      else if (type == RULE_STATEMENT) {
        return new DroolsRuleStatementImpl(node);
      }
      else if (type == SELECTOR) {
        return new DroolsSelectorImpl(node);
      }
      else if (type == SHIFT_EXPR) {
        return new DroolsShiftExprImpl(node);
      }
      else if (type == SIMPLE_NAME) {
        return new DroolsSimpleNameImpl(node);
      }
      else if (type == SQUARE_ARGUMENTS) {
        return new DroolsSquareArgumentsImpl(node);
      }
      else if (type == STRING_ID) {
        return new DroolsStringIdImpl(node);
      }
      else if (type == STRING_LITERAL) {
        return new DroolsStringLiteralImpl(node);
      }
      else if (type == SUPER_SUFFIX) {
        return new DroolsSuperSuffixImpl(node);
      }
      else if (type == SUPER_TYPE) {
        return new DroolsSuperTypeImpl(node);
      }
      else if (type == TRAITABLE) {
        return new DroolsTraitableImpl(node);
      }
      else if (type == TYPE) {
        return new DroolsTypeImpl(node);
      }
      else if (type == TYPE_ARGUMENT) {
        return new DroolsTypeArgumentImpl(node);
      }
      else if (type == TYPE_ARGUMENTS) {
        return new DroolsTypeArgumentsImpl(node);
      }
      else if (type == TYPE_DECLARATION) {
        return new DroolsTypeDeclarationImpl(node);
      }
      else if (type == TYPE_NAME) {
        return new DroolsTypeNameImpl(node);
      }
      else if (type == UNARY_2_EXPR) {
        return new DroolsUnary2ExprImpl(node);
      }
      else if (type == UNARY_ASSIGN_EXPR) {
        return new DroolsUnaryAssignExprImpl(node);
      }
      else if (type == UNARY_EXPR) {
        return new DroolsUnaryExprImpl(node);
      }
      else if (type == UNARY_NOT_PLUS_MINUS_EXPR) {
        return new DroolsUnaryNotPlusMinusExprImpl(node);
      }
      else if (type == UPDATE_RHS_STATEMENT) {
        return new DroolsUpdateRhsStatementImpl(node);
      }
      else if (type == VARIABLE_INITIALIZER) {
        return new DroolsVariableInitializerImpl(node);
      }
      else if (type == VAR_TYPE) {
        return new DroolsVarTypeImpl(node);
      }
      else if (type == WINDOW_DECLARATION) {
        return new DroolsWindowDeclarationImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
