// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion;

import com.intellij.coldFusion.UI.editorActions.CfmlParameterInfoHandler;
import com.intellij.coldFusion.model.info.CfmlFunctionDescription;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.utils.parameterInfo.MockCreateParameterInfoContext;
import com.intellij.testFramework.utils.parameterInfo.MockParameterInfoUIContext;
import com.intellij.testFramework.utils.parameterInfo.MockUpdateParameterInfoContext;

public class CfmlParameterInfoTest extends CfmlCodeInsightFixtureTestCase {

  public void testScriptFunction() {
    defaultTest("a : int, [b]");
  }

  public void testTagFunction() {
    defaultTest("[arg1 : string], arg2 : int");
  }

  public void testStandardFunction() {
    defaultTest("array : Array, object : Any");
  }

  private void defaultTest(String s/*, int highlightedParameterIndex*/) {
    myFixture.configureByFile(getTestName(false) + ".cfml");

    CfmlParameterInfoHandler parameterInfoHandler = new CfmlParameterInfoHandler();
    MockCreateParameterInfoContext createContext = new MockCreateParameterInfoContext(myFixture.getEditor(), myFixture.getFile());
    PsiElement elt = parameterInfoHandler.findElementForParameterInfo(createContext);
    assertNotNull(elt);
    parameterInfoHandler.showParameterInfo(elt, createContext);
    Object[] items = createContext.getItemsToShow();
    assertTrue(items != null);
    assertTrue(items.length > 0);
    MockParameterInfoUIContext context = new MockParameterInfoUIContext<>(elt);
    parameterInfoHandler.updateUI((CfmlFunctionDescription)items[0], context);
    assertEquals(s, parameterInfoHandler.getText());

    // index check
    MockUpdateParameterInfoContext updateContext = new MockUpdateParameterInfoContext(myFixture.getEditor(), myFixture.getFile());
    final PsiElement element = parameterInfoHandler.findElementForUpdatingParameterInfo(updateContext);
    parameterInfoHandler.updateParameterInfo(element, updateContext);
    // assertEquals(highlightedParameterIndex, updateContext.getCurrentParameter());
  }

  @Override
  protected String getBasePath() {
      return "/paramInfo/";
  }
}
