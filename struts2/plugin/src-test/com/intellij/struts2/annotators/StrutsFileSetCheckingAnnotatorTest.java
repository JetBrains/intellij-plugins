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

package com.intellij.struts2.annotators;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.struts2.BasicLightHighlightingTestCase;
import org.jetbrains.annotations.NotNull;

/**
 * Tests {@link StrutsFileSetCheckingAnnotator}.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsFileSetCheckingAnnotatorTest extends BasicLightHighlightingTestCase {

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "strutsXml/highlighting";
  }

  public void testStrutsXmlNoFacetSetup() {
    final IntentionAction intention = myFixture.getAvailableIntention("Edit Struts 2 facet settings",
                                                                      "struts-simple.xml");
    assertIntentionFound(intention);
  }

  public void testStrutsXmlNotInFileSet() {
    createStrutsFileSet("struts-simple.xml");
    final IntentionAction intention = myFixture.getAvailableIntention("Add struts-default.xml to file set",
                                                                      "struts-default.xml");
    assertIntentionFound(intention);
  }

  private void assertIntentionFound(IntentionAction intentionAction) {
    assertNotNull(toString(myFixture.getAvailableIntentions()), intentionAction);
  }
}