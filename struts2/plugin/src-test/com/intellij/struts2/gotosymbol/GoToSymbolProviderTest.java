/*
 * Copyright 2011 The authors
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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.psi.PsiFile;
import com.intellij.struts2.BasicHighlightingTestCase;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.struts2.facet.StrutsFacetConfiguration;
import com.intellij.struts2.facet.ui.StrutsFileSet;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

/**
 * {@link GoToActionSymbolProvider} and {@link GoToPackageSymbolProvider}.
 *
 * @author Yann C&eacute;bron
 */
public class GoToSymbolProviderTest extends LightCodeInsightFixtureTestCase {

  private StrutsFacet myFacet;

  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    myFacet = null;
    super.tearDown();
  }

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return new DefaultLightProjectDescriptor() {
      @Override
      public void configureModule(final Module module,
                                  final ModifiableRootModel model,
                                  final ContentEntry contentEntry) {
        super.configureModule(module, model, contentEntry);
        myFacet = BasicHighlightingTestCase.createFacet(module);
      }
    };
  }

  public void testGotoAction() throws Exception {
    runGotoTest("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "\n" +
                "<!DOCTYPE struts PUBLIC\n" +
                "    \"-//Apache Software Foundation//DTD Struts Configuration 2.0//EN\"\n" +
                "    \"http://struts.apache.org/dtds/struts-2.0.dtd\">\n" +
                "\n" +
                "<struts>\n" +
                "\n" +
                "  <package name=\"testPackage\" namespace=\"/Test\">\n" +
                "    <action name=\"test1\"/>\n" +
                "    <action name=\"test2\"/>\n" +
                "    <action name=\"test3\"/>\n" +
                "  </package>\n" +
                "\n" +
                "</struts>",
                "test1", "test2", "test3");
  }

  public void testGotoPackage() throws Exception {
    runGotoTest("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "\n" +
                "<!DOCTYPE struts PUBLIC\n" +
                "    \"-//Apache Software Foundation//DTD Struts Configuration 2.0//EN\"\n" +
                "    \"http://struts.apache.org/dtds/struts-2.0.dtd\">\n" +
                "\n" +
                "<struts>\n" +
                "\n" +
                "  <package name=\"testPackage1\" namespace=\"/Test\">\n" +
                "  </package>\n" +
                "  <package name=\"testPackage2\" namespace=\"/Test2\">\n" +
                "  </package>\n" +
                "\n" +
                "</struts>",
                "testPackage1", "testPackage2");
  }

  private void runGotoTest(final String strutsXmlContent,
                           final String... expectedSymbols) {
    final PsiFile file = myFixture.configureByText(getTestName(true) + "-struts.xml", strutsXmlContent);

    final StrutsFacetConfiguration configuration = myFacet.getConfiguration();
    final StrutsFileSet strutsFileSet = new StrutsFileSet("test", "test", configuration);
    strutsFileSet.addFile(file.getVirtualFile());
    configuration.getFileSets().add(strutsFileSet);

    final GotoSymbolModel2 model2 = new GotoSymbolModel2(getProject());
    final String[] names = model2.getNames(false);

    for (final String expectedSymbol : expectedSymbols) {
      assertTrue(expectedSymbol, ArrayUtil.contains(expectedSymbol, names));
    }
  }
}