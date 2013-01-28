/*
 * Copyright 2013 The authors
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
package com.intellij.struts2.jsp.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.struts2.BasicLightHighlightingTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
public class HardcodedActionUrlInspectionTest extends BasicLightHighlightingTestCase {

  private static final String INTENTION_NAME = "Wrap with Struts <url> tag";

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "/jsp/inspection/hardcodedUrl";
  }

  @Override
  protected LocalInspectionTool[] getHighlightingInspections() {
    return new LocalInspectionTool[]{new HardcodedActionUrlInspection()};
  }

  public void testDynamic() {
    myFixture.testHighlighting("dynamic.jsp");
  }

  public void testHost() {
    myFixture.testHighlighting("host.jsp");
  }

  public void testProtocol() {
    myFixture.testHighlighting("protocol.jsp");
  }

  public void testCustomTag() {
    doTest();
  }

  public void testSimple() {
    doTest();
  }

  public void testSimpleTaglibPrefixDefined() {
    doTest();
  }

  public void testNamespace() {
    doTest();
  }

  public void testMethod() {
    doTest();
  }

  public void testOneParam() {
    doTest();
  }

  public void testMultiParams() {
    doTest();
  }

  public void testMultiParamsEscaped() {
    doTest();
  }

  public void testCustomActionExtension() {
    myFixture.copyFileToProject("struts.properties"); // custom action extension
    doTest();
  }

  private void doTest() {
    createStrutsFileSet("struts.xml"); // dummy for action-extension

    final String fileName = getTestName(true);
    CodeInsightTestUtil.doIntentionTest(myFixture, INTENTION_NAME, fileName + ".jsp", fileName + "_after.jsp");
  }
}
