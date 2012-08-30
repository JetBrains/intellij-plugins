package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.google.jstestdriver.idea.assertFramework.AbstractTestFileStructureBuilder;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.intellij.lang.javascript.psi.*;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JasmineFileStructureBuilder extends AbstractTestFileStructureBuilder<JasmineFileStructure> {

  private static final JasmineFileStructureBuilder INSTANCE = new JasmineFileStructureBuilder();
  public static final String DESCRIBE_NAME = "describe";
  public static final String IT_NAME = "it";

  @NotNull
  @Override
  public JasmineFileStructure buildTestFileStructure(@NotNull JSFile jsFile) {
    return new Builder(jsFile).build();
  }

  public static JasmineFileStructureBuilder getInstance() {
    return INSTANCE;
  }

  private static class Builder {
    private final JasmineFileStructure myFileStructure;

    public Builder(JSFile jsFile) {
      myFileStructure = new JasmineFileStructure(jsFile);
    }

    public JasmineFileStructure build() {
      List<JSStatement> statements = JsPsiUtils.listStatementsInExecutionOrder(myFileStructure.getJsFile());
      for (JSStatement statement : statements) {
        JSCallExpression jsCallExpression = JsPsiUtils.toCallExpressionFromStatement(statement);
        if (jsCallExpression != null) {
          handleCallExpr(null, jsCallExpression);
        }
      }
      return myFileStructure;
    }

    private void handleCallExpr(@Nullable JasmineSuiteStructure parentSuiteStructure, @NotNull JSCallExpression callExpression) {
      JSReferenceExpression methodExpression = ObjectUtils.tryCast(callExpression.getMethodExpression(), JSReferenceExpression.class);
      JSArgumentList argumentList = callExpression.getArgumentList();
      if (methodExpression != null && argumentList != null) {
        String methodName = methodExpression.getReferencedName();
        JSExpression[] arguments = ObjectUtils.notNull(argumentList.getArguments(), JSExpression.EMPTY_ARRAY);
        if (DESCRIBE_NAME.equals(methodName) && arguments.length == 2) {
          String name = JsPsiUtils.extractStringValue(arguments[0]);
          JSFunctionExpression specDefinitions = JsPsiUtils.extractFunctionExpression(arguments[1]);
          if (name != null && specDefinitions != null) {
            JasmineSuiteStructure suiteStructure = new JasmineSuiteStructure(name, callExpression, specDefinitions);
            if (parentSuiteStructure != null) {
              parentSuiteStructure.addChild(suiteStructure);
            } else {
              myFileStructure.addDescribeStructure(suiteStructure);
            }
            handleDescribeSpecDefinitions(suiteStructure, specDefinitions);
          }
        }
        if (IT_NAME.equals(methodName) && arguments.length == 2) {
          String name = JsPsiUtils.extractStringValue(arguments[0]);
          if (name != null) {
            JasmineSpecStructure specStructure = new JasmineSpecStructure(name, callExpression);
            if (parentSuiteStructure != null) {
              parentSuiteStructure.addChild(specStructure);
            }
          }
        }
      }
    }

    private void handleDescribeSpecDefinitions(@NotNull JasmineSuiteStructure suiteStructure,
                                               @NotNull JSFunctionExpression specDefinitions) {
      JSSourceElement[] sourceElements = ObjectUtils.notNull(specDefinitions.getBody(), JSSourceElement.EMPTY_ARRAY);
      for (JSSourceElement sourceElement : sourceElements) {
        JSBlockStatement jsBlockStatement = ObjectUtils.tryCast(sourceElement, JSBlockStatement.class);
        if (jsBlockStatement != null) {
          JSStatement[] statements = ObjectUtils.notNull(jsBlockStatement.getStatements(), JSStatement.EMPTY);
          for (JSStatement statement : statements) {
            JSCallExpression jsCallExpression = JsPsiUtils.toCallExpressionFromStatement(statement);
            if (jsCallExpression != null) {
              handleCallExpr(suiteStructure, jsCallExpression);
            }
          }
        }
      }
    }
  }
}
