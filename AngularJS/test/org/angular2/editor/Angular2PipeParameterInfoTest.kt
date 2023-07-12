// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.javascript.JSFunctionWithSubstitutor;
import com.intellij.javascript.JSParameterInfoHandler;
import com.intellij.lang.javascript.psi.JSFunctionType;
import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.typescript.hint.TypeScriptParameterInfoHandler;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.utils.parameterInfo.MockCreateParameterInfoContext;
import com.intellij.testFramework.utils.parameterInfo.MockParameterInfoUIContext;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

public class Angular2PipeParameterInfoTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  public void testPipeParameterInfo() {
    doTest("value: number, exponent: string");
  }

  @SuppressWarnings("SameParameterValue")
  private void doTest(String expected) {
    myFixture.configureByFiles(getTestName(false) + ".ts", "package.json");
    final CreateParameterInfoContext parameterInfoContext = new MockCreateParameterInfoContext(myFixture.getEditor(), myFixture.getFile());
    final Object list = new TypeScriptParameterInfoHandler().findElementForParameterInfo(parameterInfoContext);
    assertNotNull(list);
    Object[] items = parameterInfoContext.getItemsToShow();
    String[] strings = ContainerUtil.map2Array(items, String.class, Angular2PipeParameterInfoTest::getPresentation);
    assertSize(1, strings);
    assertEquals(expected, strings[0]);
  }

  private static String getPresentation(Object parameterInfoElement) {
    assertTrue(parameterInfoElement instanceof JSFunctionType);
    final JSFunctionType jsFunctionType = (JSFunctionType)parameterInfoElement;
    final JSParameterInfoHandler parameterInfoHandler = new JSParameterInfoHandler();
    MockParameterInfoUIContext<PsiElement> context = new MockParameterInfoUIContext<>(jsFunctionType.getSourceFunctionItem());
    parameterInfoHandler.updateUI(jsFunctionType, context);
    return context.getText();
  }
}
