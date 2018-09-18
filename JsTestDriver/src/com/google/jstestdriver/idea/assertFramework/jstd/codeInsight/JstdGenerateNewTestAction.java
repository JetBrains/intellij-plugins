package com.google.jstestdriver.idea.assertFramework.jstd.codeInsight;

import com.google.jstestdriver.idea.assertFramework.jstd.JstdTestCaseStructure;
import com.google.jstestdriver.idea.assertFramework.jstd.JstdTestFileStructure;
import com.google.jstestdriver.idea.assertFramework.jstd.JstdTestFileStructureBuilder;
import com.intellij.javascript.testFramework.codeInsight.AbstractJsGenerateAction;
import com.intellij.javascript.testFramework.codeInsight.GenerateActionContext;
import com.intellij.javascript.testFramework.codeInsight.JsGeneratorUtils;
import com.intellij.javascript.testFramework.util.JsPsiUtils;
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JstdGenerateNewTestAction extends AbstractJsGenerateAction {

  @NotNull
  @Override
  public String getHumanReadableDescription() {
    return "JsTestDriver Test";
  }

  @Override
  public boolean isEnabled(@NotNull GenerateActionContext context) {
    JstdTestFileStructureBuilder builder = JstdTestFileStructureBuilder.getInstance();
    JstdTestFileStructure fileStructure = builder.fetchCachedTestFileStructure(context.getJsFile());
    if (fileStructure.getTestCaseCount() == 0) {
      return false;
    }
    Runnable testGenerator = buildGenerator(fileStructure, context);
    return testGenerator != null;
  }

  @Override
  public void actionPerformed(@NotNull GenerateActionContext context) {
    JstdTestFileStructureBuilder builder = JstdTestFileStructureBuilder.getInstance();
    JstdTestFileStructure fileStructure = builder.fetchCachedTestFileStructure(context.getJsFile());
    Runnable testGenerator = buildGenerator(fileStructure, context);
    if (testGenerator != null) {
      testGenerator.run();
    }
  }

  @Nullable
  private static Runnable buildGenerator(@NotNull JstdTestFileStructure fileStructure, @NotNull GenerateActionContext context) {
    int caretOffset = context.getDocumentCaretOffset();
    JstdTestCaseStructure jstdTestCaseStructure = fileStructure.findEnclosingTestCaseByOffset(caretOffset);
    if (jstdTestCaseStructure != null) {
      JSObjectLiteralExpression testsObjectLiteral = jstdTestCaseStructure.getTestsObjectsLiteral();
      if (testsObjectLiteral != null) {
        return new TestGeneratorOnObjectLiteral(testsObjectLiteral, context);
      } else {
        if (jstdTestCaseStructure.getTestCount() == 0) {
          JSCallExpression callExpression = jstdTestCaseStructure.getEnclosingCallExpression();
          JSArgumentList argumentList = callExpression.getArgumentList();
          JSExpression[] arguments = JsPsiUtils.getArguments(argumentList);
          if (arguments.length == 1 && arguments[0] != null) {
            return new TestGeneratorOnNewlyCreatedObjectLiteral(argumentList, context);
          }
        }
      }
    } else {
      for (JstdTestCaseStructure testCaseStructure : fileStructure.getTestCaseStructures()) {
        JSObjectLiteralExpression testsObjectLiteral = testCaseStructure.getTestsObjectsLiteral();
        if (testsObjectLiteral != null && JsPsiUtils.containsOffsetStrictly(testsObjectLiteral.getTextRange(), caretOffset)) {
          return new TestGeneratorOnObjectLiteral(testsObjectLiteral, context);
        }
      }
    }
    return null;
  }

  private static class TestGeneratorOnObjectLiteral implements Runnable {

    private final JSObjectLiteralExpression myTestsObjectLiteral;
    private final GenerateActionContext myContext;

    TestGeneratorOnObjectLiteral(@NotNull JSObjectLiteralExpression testsObjectLiteral, GenerateActionContext context) {
      myTestsObjectLiteral = testsObjectLiteral;
      myContext = context;
    }

    @Override
    public void run() {
      JsGeneratorUtils.generateProperty(myTestsObjectLiteral, myContext, "\"test ${name}\": function() {|}");
    }
  }

  private static class TestGeneratorOnNewlyCreatedObjectLiteral implements Runnable {

    private final JSArgumentList myArgumentList;
    private final GenerateActionContext myContext;

    TestGeneratorOnNewlyCreatedObjectLiteral(@NotNull JSArgumentList argumentList,
                                                    @NotNull GenerateActionContext context) {
      myArgumentList = argumentList;
      myContext = context;
    }

    @Override
    public void run() {
      JsGeneratorUtils.generateObjectLiteralWithPropertyAsArgument(myContext, "{\n\"test ${name}\": function() {|}\n}", myArgumentList, 1);
    }
  }
}
