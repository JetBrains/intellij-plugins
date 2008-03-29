/*
 * Copyright 2007 The authors
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
package com.intellij.struts2.annotators;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.struts2.dom.struts.BasicStrutsHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;

import java.util.List;

/**
 * Tests {@link StrutsFileSetCheckingAnnotator}.
 *
 * @author Yann CŽbron
 */
public class StrutsFileSetCheckingAnnotatorTest extends BasicStrutsHighlightingTestCase<JavaModuleFixtureBuilder> {

  protected String getTestDataLocation() {
    return "strutsXmlHighlighting";
  }

  public void testStrutsXmlNoFacetSetup() throws Throwable {
    final List<IntentionAction> intentions = myFixture.getAvailableIntentions("struts-simple.xml");
    final IntentionAction action =
            CodeInsightTestUtil.findIntentionByText(intentions,
                                                    "Edit Struts 2 facet settings");
    assertNotNull(action);
  }

  public void testStrutsXmlNotInFileSet() throws Throwable {
    createStrutsFileSet("struts-simple.xml");
    final List<IntentionAction> intentions = myFixture.getAvailableIntentions("struts-default.xml");

    final IntentionAction action =
            CodeInsightTestUtil.findIntentionByText(intentions,
                                                    "Add struts-default.xml to file set");
    assertNotNull(action);
  }

}