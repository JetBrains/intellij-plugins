package com.google.jstestdriver.idea.assertFramework.jstd;

import com.intellij.javascript.testFramework.AbstractTestFileStructureBuilder;
import com.intellij.javascript.testFramework.util.JsPsiUtils;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JstdTestFileStructureBuilder extends AbstractTestFileStructureBuilder<JstdTestFileStructure> {

  private static final JstdTestFileStructureBuilder INSTANCE = new JstdTestFileStructureBuilder();
  private static final String TEST_CASE_NAME = "TestCase";
  private static final String ASYNC_TEST_CASE_NAME = "AsyncTestCase";

  private JstdTestFileStructureBuilder() {}

  @NotNull
  @Override
  public JstdTestFileStructure buildTestFileStructure(@NotNull JSFile jsFile) {
    JstdTestFileStructure jsTestFileStructure = new JstdTestFileStructure(jsFile);
    if (JsPsiUtils.mightContainGlobalCall(jsFile, TEST_CASE_NAME, false)) {
      List<JSStatement> statements = JsPsiUtils.listStatementsInExecutionOrder(jsFile);
      for (JSStatement statement : statements) {
        fillJsTestFileStructure(jsTestFileStructure, statement);
      }
    }
    jsTestFileStructure.postProcess();
    return jsTestFileStructure;
  }

  private static void fillJsTestFileStructure(@NotNull JstdTestFileStructure jsTestFileStructure,
                                              @NotNull JSStatement statement) {
    if (statement instanceof JSExpressionStatement) {
      JSExpressionStatement jsExpressionStatement = (JSExpressionStatement) statement;
      JSExpression expressionOfStatement = jsExpressionStatement.getExpression();
      if (expressionOfStatement instanceof JSCallExpression) {
        // TestCase("testCaseName", { test1: function() {} });
        JSCallExpression callExpression = (JSCallExpression) expressionOfStatement;
        createTestCaseStructure(jsTestFileStructure, callExpression);
      }
      else if (expressionOfStatement instanceof JSAssignmentExpression) {
        // testCase = TestCase("testCaseName");
        JSAssignmentExpression jsAssignmentExpression = (JSAssignmentExpression) expressionOfStatement;
        JSCallExpression rOperandCallExpression = ObjectUtils.tryCast(jsAssignmentExpression.getROperand(), JSCallExpression.class);
        if (rOperandCallExpression != null) {
          JstdTestCaseStructure testCaseStructure = createTestCaseStructure(jsTestFileStructure, rOperandCallExpression);
          if (testCaseStructure != null) {
            JSDefinitionExpression jsDefinitionExpression = ObjectUtils.tryCast(jsAssignmentExpression.getLOperand(), JSDefinitionExpression.class);
            if (jsDefinitionExpression != null) {
              JSReferenceExpression jsReferenceExpression = ObjectUtils.tryCast(jsDefinitionExpression.getExpression(), JSReferenceExpression.class);
              if (jsReferenceExpression != null) {
                String refName = jsReferenceExpression.getReferenceName();
                if (refName != null) {
                  addPrototypeTests(testCaseStructure, refName, jsExpressionStatement);
                }
              }
            }
          }
        }
      }
    }
    if (statement instanceof JSVarStatement) {
      // var testCase = TestCase("testCaseName");
      JSVarStatement jsVarStatement = (JSVarStatement) statement;
      JSVariable[] jsVariables = ObjectUtils.notNull(jsVarStatement.getVariables(), JSVariable.EMPTY_ARRAY);
      for (JSVariable jsVariable : jsVariables) {
        JSCallExpression jsCallExpression = ObjectUtils.tryCast(jsVariable.getInitializer(), JSCallExpression.class);
        if (jsCallExpression != null) {
          JstdTestCaseStructure testCaseStructure = createTestCaseStructure(jsTestFileStructure, jsCallExpression);
          if (testCaseStructure != null) {
            String refName = jsVariable.getQualifiedName();
            if (refName != null) {
              addPrototypeTests(testCaseStructure, refName, jsVarStatement);
            }
          }
        }
      }
    }
  }

  private static void addPrototypeTests(@NotNull JstdTestCaseStructure testCaseStructure,
                                        @NotNull String referenceName,
                                        @NotNull JSStatement refStatement) {
    List<JSStatement> statements = JsPsiUtils.listStatementsInExecutionOrderNextTo(refStatement);
    for (JSStatement statement : statements) {
      JSExpressionStatement expressionStatement = ObjectUtils.tryCast(statement, JSExpressionStatement.class);
      if (expressionStatement != null) {
        JSAssignmentExpression assignmentExpr = ObjectUtils.tryCast(expressionStatement.getExpression(), JSAssignmentExpression.class);
        if (assignmentExpr != null) {
          JSDefinitionExpression wholeLeftDefExpr = ObjectUtils.tryCast(assignmentExpr.getLOperand(), JSDefinitionExpression.class);
          if (wholeLeftDefExpr != null) {
            JSReferenceExpression wholeLeftRefExpr = ObjectUtils.tryCast(wholeLeftDefExpr.getExpression(), JSReferenceExpression.class);
            if (wholeLeftRefExpr != null) {
              JSReferenceExpression testCaseAndPrototypeRefExpr = ObjectUtils.tryCast(wholeLeftRefExpr.getQualifier(), JSReferenceExpression.class);
              if (testCaseAndPrototypeRefExpr != null) {
                if ("prototype".equals(testCaseAndPrototypeRefExpr.getReferenceName())) {
                  JSReferenceExpression testCaseRefExpr = ObjectUtils.tryCast(testCaseAndPrototypeRefExpr.getQualifier(), JSReferenceExpression.class);
                  if (testCaseRefExpr != null && testCaseRefExpr.getQualifier() == null) {
                    if (referenceName.equals(testCaseRefExpr.getReferenceName())) {
                      addPrototypeTest(testCaseStructure, assignmentExpr.getROperand(), wholeLeftDefExpr);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private static void addPrototypeTest(@NotNull JstdTestCaseStructure testCaseStructure,
                                       @Nullable JSExpression rightAssignmentOperand,
                                       @NotNull JSDefinitionExpression wholeLeftDefExpr) {
    JSReferenceExpression wholeLeftRefExpr = ObjectUtils.tryCast(wholeLeftDefExpr.getExpression(), JSReferenceExpression.class);
    LeafPsiElement testMethodLeafPsiElement = null;
    if (wholeLeftRefExpr != null) {
      testMethodLeafPsiElement = ObjectUtils.tryCast(wholeLeftRefExpr.getReferenceNameElement(), LeafPsiElement.class);
    }
    if (testMethodLeafPsiElement != null && testMethodLeafPsiElement.getElementType() == JSTokenTypes.IDENTIFIER) {
      JSFunctionExpression jsFunctionExpression = JsPsiUtils.extractFunctionExpression(rightAssignmentOperand);
      JstdTestStructure jstdTestStructure = JstdTestStructure.newPrototypeBasedTestStructure(wholeLeftDefExpr,
                                                                                             testMethodLeafPsiElement,
                                                                                             jsFunctionExpression);
      if (jstdTestStructure != null) {
        testCaseStructure.addTestStructure(jstdTestStructure);
      }
    }
  }

  @Nullable
  private static JstdTestCaseStructure createTestCaseStructure(@NotNull JstdTestFileStructure jsTestFileStructure,
                                                               @NotNull JSCallExpression testCaseCallExpression) {
    JSReferenceExpression referenceExpression = ObjectUtils.tryCast(testCaseCallExpression.getMethodExpression(), JSReferenceExpression.class);
    if (referenceExpression != null) {
      String referenceName = referenceExpression.getReferenceName();
      if (TEST_CASE_NAME.equals(referenceName) || ASYNC_TEST_CASE_NAME.equals(referenceName)) {
        JSExpression[] arguments = JsPsiUtils.getArguments(testCaseCallExpression);
        if (arguments.length >= 1) {
          String testCaseName = JsPsiUtils.extractStringValue(arguments[0]);
          if (testCaseName != null) {
            JSObjectLiteralExpression testsObjectLiteral = null;
            if (arguments.length >= 2) {
              testsObjectLiteral = JsPsiUtils.extractObjectLiteralExpression(arguments[1]);
            }
            JstdTestCaseStructure testCaseStructure = new JstdTestCaseStructure(jsTestFileStructure, testCaseName, testCaseCallExpression, testsObjectLiteral);
            jsTestFileStructure.addTestCaseStructure(testCaseStructure);
            if (testsObjectLiteral != null) {
              fillTestCaseStructureByObjectLiteral(testCaseStructure, testsObjectLiteral);
            }
            return testCaseStructure;
          }
        }
      }
    }
    return null;
  }

  private static void fillTestCaseStructureByObjectLiteral(
      @NotNull JstdTestCaseStructure testCaseStructure,
      @NotNull JSObjectLiteralExpression testsObjectLiteral
  ) {
    JSProperty[] properties = JsPsiUtils.getProperties(testsObjectLiteral);
    for (JSProperty property : properties) {
      JstdTestStructure testStructure = JstdTestStructure.newPropertyBasedTestStructure(property);
      if (testStructure != null) {
        testCaseStructure.addTestStructure(testStructure);
      }
    }
  }

  public static JstdTestFileStructureBuilder getInstance() {
    return INSTANCE;
  }
}
