/*
 * Copyright 2008 The authors
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
package com.intellij.struts2.dom.struts;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;

import java.util.List;

/**
 * Test intentions in struts.xml.
 *
 * @author Yann CŽbron
 */
public class StrutsIntentionTest extends BasicStrutsHighlightingTestCase<JavaModuleFixtureBuilder> {

  protected String getTestDataLocation() {
    return "strutsXmlIntentions";
  }

  /**
   * Check {@link com.intellij.struts2.dom.struts.action.ActionMethodConverter}.
   *
   * @throws Throwable Any exceptions.
   */
  public void testCreateActionMethodIntention() throws Throwable {
    createStrutsFileSet("struts-action-method.xml");
    final List<IntentionAction> list = myFixture.getAvailableIntentions("struts-action-method.xml");

    final IntentionAction action =
            CodeInsightTestUtil.findIntentionByText(list,
                                                    "Create action-method 'unknownMethod'");
    assertNotNull(action);

    // myFixture.launchAction(action);
    // myFixture.checkResultByFile("/src/MyClass.java", "/src/MyClassAfterCreateActionMethod.java", false);
  }

}