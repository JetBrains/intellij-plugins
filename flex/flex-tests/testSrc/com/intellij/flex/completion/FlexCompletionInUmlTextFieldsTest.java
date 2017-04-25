package com.intellij.flex.completion;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment;
import com.intellij.lang.javascript.uml.actions.JSCreateFieldDialog;
import com.intellij.psi.PsiFile;
import com.intellij.util.ArrayUtil;

public class FlexCompletionInUmlTextFieldsTest extends FlexCompletionInTextFieldBase {
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testCreateUmlFieldType() throws Exception {
    configureByFiles(null, BASE_PATH + getTestName(false) + "_2.js2");
    PsiFile fragment =
      JSCreateFieldDialog.createTypeField(myProject, JSCreateFieldDialog.getTypeFieldScope(myModule, myProject)).getPsiFile();
    String[] included = new String[]{"Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher", "void", "*"};
    String[] excluded = ArrayUtil.mergeArrays(DEFALUT_VALUES, "public", "function", "while");
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, included, excluded, "Z111", BASE_PATH + getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testCreateUmlFieldInitializer() throws Exception {
    configureByFiles(null, BASE_PATH + getTestName(false) + "_2.js2");
    JSExpressionCodeFragment fragment =
      JSCreateFieldDialog.createInitializerCodeFragment(createFakeClass());
    String[] included = DEFALUT_VALUES;
    // TODO classes should be removed from completion list
    included = ArrayUtil.mergeArrays(included, "Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher");
    String[] excluded = new String[]{"public", "function", "while"};
    checkTextFieldCompletion(fragment, included, excluded, "EventDispatcher", BASE_PATH + getTestName(false) + ".txt");
  }
}
