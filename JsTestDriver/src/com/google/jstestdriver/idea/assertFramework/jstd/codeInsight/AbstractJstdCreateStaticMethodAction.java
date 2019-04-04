package com.google.jstestdriver.idea.assertFramework.jstd.codeInsight;

import com.intellij.javascript.testFramework.codeInsight.AbstractJsGenerateAction;
import com.intellij.javascript.testFramework.codeInsight.GenerateActionContext;
import com.intellij.javascript.testFramework.codeInsight.JsGeneratorUtils;
import com.google.jstestdriver.idea.assertFramework.jstd.JstdTestCaseStructure;
import com.google.jstestdriver.idea.assertFramework.jstd.JstdTestFileStructure;
import com.google.jstestdriver.idea.assertFramework.jstd.JstdTestFileStructureBuilder;
import com.intellij.javascript.testFramework.util.JsPsiUtils;
import com.intellij.lang.javascript.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractJstdCreateStaticMethodAction extends AbstractJsGenerateAction {

  @NotNull
  @Override
  public abstract String getHumanReadableDescription();

  @NotNull
  public abstract String getMethodName();

  @Override
  public boolean isEnabled(@NotNull GenerateActionContext context) {
    JstdTestFileStructureBuilder builder = JstdTestFileStructureBuilder.getInstance();
    JstdTestFileStructure fileStructure = builder.fetchCachedTestFileStructure(context.getJsFile());
    Runnable generator = buildGenerator(context, fileStructure);
    return generator != null;
  }

  @Nullable
  private Runnable buildGenerator(@NotNull GenerateActionContext context, @NotNull JstdTestFileStructure fileStructure) {
    if (fileStructure.getTestCaseCount() == 0) {
      return null;
    }
    int caretOffset = context.getDocumentCaretOffset();
    JstdTestCaseStructure testCaseStructure = fileStructure.findEnclosingTestCaseByOffset(caretOffset);
    if (testCaseStructure != null) {
      JSObjectLiteralExpression testsObjectLiteral = testCaseStructure.getTestsObjectsLiteral();
      if (testsObjectLiteral != null) {
        if (!findMethod(testsObjectLiteral)) {
          return new StaticMethodGenerator(testsObjectLiteral, context);
        }
      } else {
        JSCallExpression callExpression = testCaseStructure.getEnclosingCallExpression();
        JSArgumentList argumentList = callExpression.getArgumentList();
        JSExpression[] arguments = JsPsiUtils.getArguments(argumentList);
        if (arguments.length == 1 && arguments[0] != null) {
          return new StaticMethodGeneratorOnNewlyCreatedObjectLiteral(argumentList, context);
        }
      }
    } else {
      for (JstdTestCaseStructure testCase : fileStructure.getTestCaseStructures()) {
        JSObjectLiteralExpression testsObjectLiteral = testCase.getTestsObjectsLiteral();
        if (testsObjectLiteral != null && JsPsiUtils.containsOffsetStrictly(testsObjectLiteral.getTextRange(), caretOffset)) {
          if (!findMethod(testsObjectLiteral)) {
            return new StaticMethodGenerator(testsObjectLiteral, context);
          }
        }
      }
    }
    return null;
  }

  private boolean findMethod(@NotNull JSObjectLiteralExpression objectLiteralExpression) {
    JSProperty[] properties = JsPsiUtils.getProperties(objectLiteralExpression);
    String methodName = getMethodName();
    for (JSProperty property : properties) {
      String propertyName = JsPsiUtils.getPropertyName(property);
      if (methodName.equals(propertyName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void actionPerformed(@NotNull GenerateActionContext context) {
    JstdTestFileStructureBuilder builder = JstdTestFileStructureBuilder.getInstance();
    JstdTestFileStructure fileStructure = builder.fetchCachedTestFileStructure(context.getJsFile());
    Runnable generator = buildGenerator(context, fileStructure);
    if (generator != null) {
      generator.run();
    }
  }

  private class StaticMethodGenerator implements Runnable {
    private final JSObjectLiteralExpression myTestsObjectLiteral;
    private final GenerateActionContext myContext;

    StaticMethodGenerator(
        @NotNull JSObjectLiteralExpression testsObjectLiteral,
        @NotNull GenerateActionContext context) {
      myTestsObjectLiteral = testsObjectLiteral;
      myContext = context;
    }

    @Override
    public void run() {
      JsGeneratorUtils.generateProperty(myTestsObjectLiteral, myContext, getMethodName() + ": function() {|}");
    }
  }

  private class StaticMethodGeneratorOnNewlyCreatedObjectLiteral implements Runnable {

    private final JSArgumentList myArgumentList;
    private final GenerateActionContext myContext;

    StaticMethodGeneratorOnNewlyCreatedObjectLiteral(JSArgumentList argumentList, GenerateActionContext context) {
      myArgumentList = argumentList;
      myContext = context;
    }

    @Override
    public void run() {
      JsGeneratorUtils
        .generateObjectLiteralWithPropertyAsArgument(myContext, "{\n" + getMethodName() + ": function() {|}\n}", myArgumentList, 1);
    }
  }

}
