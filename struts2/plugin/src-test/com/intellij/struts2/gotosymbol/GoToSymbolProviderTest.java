/*
 * Copyright 2017 The authors
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

package com.intellij.struts2.gotosymbol;

import com.intellij.ide.util.gotoByName.GotoSymbolModel2;
import com.intellij.psi.PsiFile;
import com.intellij.struts2.BasicLightHighlightingTestCase;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

/**
 * {@link GoToActionSymbolProvider} and {@link GoToPackageSymbolProvider}.
 *
 * @author Yann C&eacute;bron
 */
public class GoToSymbolProviderTest extends BasicLightHighlightingTestCase {

  @NotNull
  @Override
  protected String getTestDataLocation() {
    return "";
  }

  public void testGotoAction() {
    runGotoTest("""
                  <?xml version="1.0" encoding="UTF-8" ?>

                  <!DOCTYPE struts PUBLIC
                      "-//Apache Software Foundation//DTD Struts Configuration 2.0//EN"
                      "http://struts.apache.org/dtds/struts-2.0.dtd">

                  <struts>

                    <package name="testPackage" namespace="/Test">
                      <action name="test1"/>
                      <action name="test2"/>
                      <action name="test3"/>
                    </package>

                  </struts>""",
                "test1", "test2", "test3");
  }

  public void testGotoPackage() {
    runGotoTest("""
                  <?xml version="1.0" encoding="UTF-8" ?>

                  <!DOCTYPE struts PUBLIC
                      "-//Apache Software Foundation//DTD Struts Configuration 2.0//EN"
                      "http://struts.apache.org/dtds/struts-2.0.dtd">

                  <struts>

                    <package name="testPackage1" namespace="/Test">
                    </package>
                    <package name="testPackage2" namespace="/Test2">
                    </package>

                  </struts>""",
                "testPackage1", "testPackage2");
  }

  private void runGotoTest(final String strutsXmlContent,
                           final String... expectedSymbols) {
    final PsiFile file = myFixture.addFileToProject(getTestName(true) + "-struts.xml", strutsXmlContent);
    createStrutsFileSet(file.getName());

    final GotoSymbolModel2 model2 = new GotoSymbolModel2(getProject(), myFixture.getTestRootDisposable());
    final String[] names = model2.getNames(false);

    for (final String expectedSymbol : expectedSymbols) {
      assertTrue(expectedSymbol, ArrayUtil.contains(expectedSymbol, names));
    }
  }
}