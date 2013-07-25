/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion;

import com.intellij.coldFusion.UI.editorActions.CfmlParameterInfoHandler;
import com.intellij.coldFusion.model.info.CfmlFunctionDescription;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightCodeInsightTestCase;
import com.intellij.testFramework.utils.parameterInfo.MockCreateParameterInfoContext;
import com.intellij.testFramework.utils.parameterInfo.MockParameterInfoUIContext;
import com.intellij.testFramework.utils.parameterInfo.MockUpdateParameterInfoContext;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author vnikolaenko
 */
public class CfmlParameterInfoTest extends CfmlCodeInsightFixtureTestCase {

  public void testScriptFunction() throws Exception {
    defaultTest("a : int, [b]");
  }

  public void testTagFunction() throws Exception {
    defaultTest("[arg1 : string], arg2 : int");
  }

  public void testStandardFunction() throws Exception {
    defaultTest("array : Array, object : Any");
  }

  private void defaultTest(String s/*, int highlightedParameterIndex*/) throws Exception {
    myFixture.configureByFile(getTestName(false) + ".cfml");

    CfmlParameterInfoHandler parameterInfoHandler = new CfmlParameterInfoHandler();
    MockCreateParameterInfoContext createContext = new MockCreateParameterInfoContext(myFixture.getEditor(), myFixture.getFile());
    PsiElement elt = parameterInfoHandler.findElementForParameterInfo(createContext);
    assertNotNull(elt);
    parameterInfoHandler.showParameterInfo(elt, createContext);
    Object[] items = createContext.getItemsToShow();
    assertTrue(items != null);
    assertTrue(items.length > 0);
    MockParameterInfoUIContext context = new MockParameterInfoUIContext<PsiElement>(elt);
    parameterInfoHandler.updateUI((CfmlFunctionDescription)items[0], context);
    assertEquals(s, parameterInfoHandler.getText());

    // index check
    MockUpdateParameterInfoContext updateContext = new MockUpdateParameterInfoContext(myFixture.getEditor(), myFixture.getFile());
    final PsiElement element = parameterInfoHandler.findElementForUpdatingParameterInfo(updateContext);
    parameterInfoHandler.updateParameterInfo(element, updateContext);
    // assertEquals(highlightedParameterIndex, updateContext.getCurrentParameter());
  }

  protected String getBasePath() {
      return "/paramInfo/";
  }
}
