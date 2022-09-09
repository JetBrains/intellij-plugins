package com.intellij.flex.completion;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment;
import com.intellij.lang.javascript.uml.actions.JSCreateFieldDialog;
import com.intellij.psi.PsiFile;
import com.intellij.util.ArrayUtil;

public class FlexCompletionInUmlTextFieldsTest extends FlexCompletionInTextFieldBase {
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath(BASE_PATH);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testCreateUmlFieldType() {
    setUpJdk();
    myFixture.configureByFiles(getTestName(false) + "_2.js2");
    PsiFile fragment =
      JSCreateFieldDialog.createTypeField(getProject(), JSCreateFieldDialog.getTypeFieldScope(getModule(), getProject())).getPsiFile();
    String[] included = new String[]{"Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher", "void", "*"};
    String[] excluded = ArrayUtil.mergeArrays(DEFALUT_VALUES, "public", "function", "while");
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, included, excluded, "Z111", getTestName(false) + ".txt");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testCreateUmlFieldInitializer() {
    setUpJdk();
    myFixture.configureByFiles(getTestName(false) + "_2.js2");
    JSExpressionCodeFragment fragment =
      JSCreateFieldDialog.createInitializerCodeFragment(createFakeClass());
    String[] included = DEFALUT_VALUES;
    // TODO classes should be removed from completion list
    included = ArrayUtil.mergeArrays(included, "Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher");
    String[] excluded = new String[]{"public", "function", "while"};
    checkTextFieldCompletion(fragment, included, excluded, "EventDispatcher", getTestName(false) + ".txt");
  }
}
