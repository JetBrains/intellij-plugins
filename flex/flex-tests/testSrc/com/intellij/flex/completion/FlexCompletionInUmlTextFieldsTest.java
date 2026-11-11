package com.intellij.flex.completion;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment;
import com.intellij.lang.javascript.uml.actions.JSCreateFieldDialog;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.ArrayUtil;
import org.junit.jupiter.api.Test;

import static com.intellij.flex.completion.FlexCompletionInTextFieldFixture.DEFALUT_VALUES;
import static com.intellij.flex.completion.FlexCompletionInTextFieldFixture.flexCompletionInTextFieldFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class FlexCompletionInUmlTextFieldsTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<FlexCompletionInTextFieldFixture> textField = flexCompletionInTextFieldFixture(projectFixture);

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testCreateUmlFieldType() {
    FlexCompletionInTextFieldFixture flex = textField.get();
    flex.setUpJdk();
    flex.getFixture().configureByFiles(flex.getTestName(false) + "_2.js2");
    PsiFile fragment = ReadAction.computeBlocking(
      () -> JSCreateFieldDialog.createTypeField(flex.getProject(), JSCreateFieldDialog.getTypeFieldScope(flex.getFixture().getModule(), flex.getProject())).getPsiFile());
    String[] included = new String[]{"Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher", "void", "*"};
    String[] excluded = ArrayUtil.mergeArrays(DEFALUT_VALUES, "public", "function", "while");
    flex.checkTextFieldCompletion((JSExpressionCodeFragment)fragment, included, excluded, "Z111", flex.getTestName(false) + ".txt");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testCreateUmlFieldInitializer() {
    FlexCompletionInTextFieldFixture flex = textField.get();
    flex.setUpJdk();
    flex.getFixture().configureByFiles(flex.getTestName(false) + "_2.js2");
    JSExpressionCodeFragment fragment =
      ReadAction.computeBlocking(() -> JSCreateFieldDialog.createInitializerCodeFragment(flex.createFakeClass()));
    String[] included = DEFALUT_VALUES;
    // TODO classes should be removed from completion list
    included = ArrayUtil.mergeArrays(included, "Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher");
    String[] excluded = new String[]{"public", "function", "while"};
    flex.checkTextFieldCompletion(fragment, included, excluded, "EventDispatcher", flex.getTestName(false) + ".txt");
  }
}
