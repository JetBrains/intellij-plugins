// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.parsers;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.psi.*;
import com.intellij.coldFusion.model.psi.impl.*;
import com.intellij.coldFusion.model.psi.stubs.CfmlStubElementTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiElement;
import com.intellij.psi.templateLanguages.TemplateDataElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.OuterLanguageElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * Created by Lera Nikolaenko
 */
public interface CfmlElementTypes extends CfmlStubElementTypes {
  // IElementType SOME = new CfmlElementType("SOME");
  IElementType CF_SCRIPT = new CfmlElementType("CF_SCRIPT");

  IElementType TEMPLATE_TEXT = new CfmlLeafElementType("CFML_TEMPLATE_TEXT");

  IElementType OUTER_ELEMENT_TYPE = new OuterLanguageElementType("CFML_FRAGMENT", CfmlLanguage.INSTANCE);

  IElementType SQL = new CfmlElementType("SQL");

  IElementType SQL_DATA = new TemplateDataElementType("SQL_DATA", CfmlLanguage.INSTANCE, SQL,
                                                      OUTER_ELEMENT_TYPE);
  TemplateDataElementType TEMPLATE_DATA =
    new TemplateDataElementType("CFML_TEMPLATE_DATA", CfmlLanguage.INSTANCE, TEMPLATE_TEXT, OUTER_ELEMENT_TYPE);

  IElementType VALUE = new CfmlCompositeElementType("VALUE");
  IElementType TYPE = new CfmlCompositeElementType("TYPE");
  IElementType IFEXPRESSION = new CfmlCompositeElementType("IFEXPRESSION");
  IElementType WHILEEXPRESSION = new CfmlCompositeElementType("WHILEEXPRESSION");
  IElementType DOWHILEEXPRESSION = new CfmlCompositeElementType("DOWHILEEXPRESSION");
  IElementType FORVARIABLE = new CfmlCompositeElementType("FORVARIABLE") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      return new CfmlForImpl.Variable(node);
    }
  };
  IElementType FOREXPRESSION = new CfmlCompositeElementType("FOREXPRESSION") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      return new CfmlForImpl(node);
    }
  };

  IElementType FORTAGINDEXATTRIBUTE = new CfmlCompositeElementType("FORTAGINDEXATTRIBUTE") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      return new CfmlTagLoopImpl.Variable(node);
    }
  };
  IElementType FORTAGEXPRESSION = new CfmlCompositeElementType("FORTAGEXPRESSION") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      return new CfmlTagLoopImpl(node);
    }
  };

  IElementType FUNCTIONBODY = new CfmlCompositeElementType("FUNCTIONBODY");
  IElementType PROPERTY = new CfmlCompositeElementType("PROPERTY") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      return new CfmlPropertyImpl(node);
    }
  };
  IElementType ACTION = new CfmlCompositeElementType("ACTION");
  IElementType SWITCHEXPRESSION = new CfmlCompositeElementType("SWITCHEXPRESSION");
  IElementType CASEEXPRESSION = new CfmlCompositeElementType("CASEEXPRESSION");
  IElementType TRYCATCHEXPRESSION = new CfmlCompositeElementType("TRYCATCHEXPRESSION");

  IElementType BLOCK_OF_STATEMENTS = new CfmlCompositeElementType("BLOCK_OF_STATEMENTS");
  IElementType CATCHEXPRESSION = new CfmlCompositeElementType("CATCH_EXPRESSION");
  IElementType INCLUDEEXPRESSION = new CfmlCompositeElementType("INCLUDE_EXPRESSION");
  IElementType IMPORTEXPRESSION = new CfmlCompositeElementType("IMPORT_EXPRESSION") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      return new CfmlScriptImportImpl(node);
    }
  };

  IElementType FUNCTION_ARGUMENT = new CfmlCompositeElementType("FUNCTION_ARGUMENT") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      return new CfmlFunctionParameterImpl(node);
    }
  };
  IElementType COMPONENT_REFERENCE = new CfmlCompositeElementType("COMPONENT_REFERENCE") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      return new CfmlComponentReference(node);
    }
  };
  IElementType COMPONENT_CONSTRUCTOR_CALL = new CfmlCompositeElementType("COMPONENT_CONSTRUCTOR_CALL") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      return new CfmlComponentConstructorCall(node);
    }
  };
  CfmlCompositeElementType TAG_IMPORT = new CfmlCompositeElementType("ImportTag") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      return new CfmlImportImpl(node);
    }
  };
  CfmlCompositeElementType NEW_EXPRESSION = new CfmlCompositeElementType("NewExpression") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      return new CfmlNewExpression(node);
    }
  };
  CfmlCompositeElementType SCRIPT_EXPRESSION = new CfmlCompositeElementType("SomeScriptExression");
  CfmlCompositeElementType REFERENCE_EXPRESSION = new CfmlCompositeElementType("ReferenceExpression") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      if ("super".equalsIgnoreCase(node.getText())) {
        return new CfmlSuperComponentReference(node);
      }
      if ("this".equalsIgnoreCase(node.getText())) {
        return new CfmlThisComponentReference(node);
      }
      return new CfmlReferenceExpression(node);
    }
  };
  CfmlCompositeElementType FUNCTION_CALL_EXPRESSION = new CfmlCompositeElementType("FunctionCallExpression") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new CfmlFunctionCallExpression(node);
    }
  };
  CfmlCompositeElementType FUNCTION_DEFINITION = new CfmlCompositeElementType("FunctionDefinition") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new CfmlFunctionImpl(node);
    }
  };
  CfmlCompositeElementType TAG_FUNCTION_CALL = new CfmlCompositeElementType("FunctionInvoke") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new CfmlTagInvokeImpl(node);
    }
  };
  CfmlCompositeElementType PARAMETERS_LIST = new CfmlCompositeElementType("ParametersList") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new CfmlParametersList(node);
    }
  };
  CfmlCompositeElementType ARGUMENT_LIST = new CfmlCompositeElementType("ArgumentList") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new CfmlArgumentList(node);
    }
  };
  CfmlCompositeElementType PROPERTY_TAG = new CfmlCompositeElementType("PropertyTag") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new CfmlTagPropertyImpl(node);
    }
  };
  CfmlCompositeElementType FUNCTION_TAG = new CfmlCompositeElementType("Tag") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new CfmlTagFunctionImpl(node);
    }
  };
  CfmlCompositeElementType INVOKE_TAG = new CfmlCompositeElementType("Tag") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new CfmlTagInvokeImpl(node);
    }
  };
  CfmlCompositeElementType ARGUMENT_TAG = new CfmlCompositeElementType("Tag") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new CfmlTagFunctionParameterImpl(node);
    }
  };
  CfmlCompositeElementType SCRIPT_TAG = new CfmlCompositeElementType("Tag") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new CfmlTagScriptImpl(node);
    }
  };
  CfmlCompositeElementType TAG = new CfmlCompositeElementType("Tag") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new CfmlTagImpl(node);
    }
  };
  CfmlCompositeElementType ATTRIBUTE = new CfmlCompositeElementType("Attribute") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new CfmlAttributeImpl(node);
    }
  };
  CfmlCompositeElementType UNARY_EXPRESSION = new CfmlCompositeElementType("UnaryExpression") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      return new CfmlOperatorExpression(node, false);
    }
  };
  CfmlCompositeElementType TERNARY_EXPRESSION = new CfmlCompositeElementType("TernaryExpression");
  CfmlCompositeElementType BINARY_EXPRESSION = new CfmlCompositeElementType("BinaryExpression") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      return new CfmlOperatorExpression(node, true);
    }
  };
  CfmlCompositeElementType NAMED_ATTRIBUTE = new CfmlCompositeElementType("NamedAttribute") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      return new CfmlNamedAttributeImpl(node);
    }
  };
  CfmlCompositeElementType ATTRIBUTE_NAME = new CfmlCompositeElementType("AttributeName") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      return new CfmlAttributeNameImpl(node);
    }
  };
  CfmlCompositeElementType ATTRIBUTE_VALUE = new CfmlCompositeElementType("AttributeValue");
  CfmlCompositeElementType NONE = new CfmlCompositeElementType("None");
  CfmlCompositeElementType ASSIGNMENT = new CfmlCompositeElementType("Assignment") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      return new CfmlAssignmentExpression(node);
    }
  };
  CfmlCompositeElementType INTEGER_LITERAL = new CfmlLiteralExpressionType("IntegerLiteral", CommonClassNames.JAVA_LANG_INTEGER);
  CfmlCompositeElementType DOUBLE_LITERAL = new CfmlLiteralExpressionType("DoubleLiteral", CommonClassNames.JAVA_LANG_DOUBLE);
  CfmlCompositeElementType BOOLEAN_LITERAL = new CfmlLiteralExpressionType("BooleanLiteral", CommonClassNames.JAVA_LANG_BOOLEAN);
  CfmlCompositeElementType STRING_LITERAL = new CfmlStringLiteralExpressionType();
  CfmlCompositeElementType ARGUMENT_NAME = new CfmlCompositeElementType("ArgumentName") {
    @Override
    public PsiElement createPsiElement(ASTNode node) {
      if ("argumentCollection".equalsIgnoreCase(node.getText())) {
        return new CfmlCompositeElement(node);
      }
      return new CfmlArgumentNameReference(node);
    }
  };
  TokenSet EXPRESSIONS = TokenSet.create(
    INTEGER_LITERAL, REFERENCE_EXPRESSION, DOUBLE_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL, BINARY_EXPRESSION, CfscriptTokenTypes.L_BRACKET,
    CfscriptTokenTypes.R_BRACKET);
}
