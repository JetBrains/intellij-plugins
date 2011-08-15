package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.assertFramework.AbstractJsTestFileStructureBuilder;
import com.google.jstestdriver.idea.assertFramework.JsTestFileStructure;
import com.google.jstestdriver.idea.javascript.navigation.Test;
import com.google.jstestdriver.idea.javascript.navigation.TestCase;
import com.google.jstestdriver.idea.javascript.navigation.TestCaseBuilder;
import com.google.jstestdriver.idea.util.CastUtils;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.intellij.execution.PsiLocation;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class QUnitFileStructureBuilder extends AbstractJsTestFileStructureBuilder {

  @NotNull
  @Override
  public JsTestFileStructure buildJsTestFileStructure(@NotNull JSFile jsFile) {
    List<JSElement> jsElements = JsPsiUtils.listJsElementsInExecutionOrder(jsFile);
    return null;
  }

  /*
  private List<TestCase> extractQUnitTestCases(JSFile jsFile) {
    List<TestCaseBuilder> testCaseBuilders = Lists.newArrayList();
    testCaseBuilders.add(new TestCaseBuilder("Default Module", null));
    collectQUnitTestCases(jsFile, testCaseBuilders);
    return new ArrayList<TestCase>(Lists.transform(testCaseBuilders, new Function<TestCaseBuilder, TestCase>() {
      @Override
      public TestCase apply(TestCaseBuilder testCaseBuilder) {
        return testCaseBuilder.build();
      }
    }));
  }

  private void collectQUnitTestCases(JSElement jsElement, List<TestCaseBuilder> container) {
    JSCallExpression callExpression = CastUtils.tryCast(jsElement, JSCallExpression.class);
    if (callExpression != null) {
      JSReferenceExpression referenceExpression = CastUtils.tryCast(callExpression.getMethodExpression(), JSReferenceExpression.class);
      if (referenceExpression != null) {
        if ("module".equals(referenceExpression.getReferencedName())) {
          JSExpression[] argumentExprs = callExpression.getArgumentList().getArguments();
          if (argumentExprs.length > 0) {
            JSLiteralExpression literalExpression = CastUtils.tryCast(argumentExprs[0], JSLiteralExpression.class);
            if (literalExpression != null) {
              String name = removeQuotes(literalExpression.getText());
              TestCaseBuilder testCaseBuilder = new TestCaseBuilder(name, PsiLocation.fromPsiElement(callExpression));
              container.add(testCaseBuilder);
            }
          }
        }
        if ("test".equals(referenceExpression.getReferencedName())) {
          JSExpression[] argumentExprs = callExpression.getArgumentList().getArguments();
          if (argumentExprs.length > 0) {
            JSLiteralExpression literalExpression = CastUtils.tryCast(argumentExprs[0], JSLiteralExpression.class);
            if (literalExpression != null) {
              String name = removeQuotes(literalExpression.getText());
              TestCaseBuilder testCaseBuilder = container.get(container.size() - 1);
              testCaseBuilder.addTest(new Test("test " + name, PsiLocation.fromPsiElement(callExpression)));
            }
          }
        }
      }
    }
    for (PsiElement child : jsElement.getChildren()) {
      JSElement childJsElement = CastUtils.tryCast(child, JSElement.class);
      if (childJsElement != null) {
        collectQUnitTestCases(childJsElement, container);
      }
    }
  }
  */

}
