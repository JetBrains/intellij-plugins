/*
 * Copyright 2010 The authors
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
import org.jetbrains.annotations.NotNull;

/**
 * Tests {@link StrutsFileSetCheckingAnnotator}.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsFileSetCheckingAnnotatorTest extends BasicStrutsHighlightingTestCase<JavaModuleFixtureBuilder> {

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "strutsXmlHighlighting";
  }

  public void testStrutsXmlNoFacetSetup() throws Throwable {
    final IntentionAction intention = myFixture.getAvailableIntention("Edit Struts 2 facet settings",
                                                                      "struts-simple.xml");
    assertNotNull(intention);
  }

  public void testStrutsXmlNotInFileSet() throws Throwable {
    createStrutsFileSet("struts-simple.xml");
    final IntentionAction intention = myFixture.getAvailableIntention("Add struts-default.xml to file set",
                                                                      "struts-default.xml");
    assertNotNull(intention);
  }

}