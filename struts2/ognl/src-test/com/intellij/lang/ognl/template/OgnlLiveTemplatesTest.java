/*
 * Copyright 2014 The authors
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

package com.intellij.lang.ognl.template;

import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.codeInsight.template.impl.actions.ListTemplatesAction;
import com.intellij.lang.ognl.OgnlFileType;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * @author Yann C&eacute;bron
 */
public class OgnlLiveTemplatesTest extends BasePlatformTestCase {

  public void testIn() {
    doTest("a in<caret>",
           "a in {<caret>, }");
  }

  public void testNotIn() {
    doTest("a nin<caret>",
           "a not in {<caret>, }");
  }

  private void doTest(final String before, final String after) {
    myFixture.configureByText(OgnlFileType.INSTANCE, before);
    expandLiveTemplate();
    myFixture.checkResult(after);
  }

  private void expandLiveTemplate() {
    new ListTemplatesAction().actionPerformedImpl(myFixture.getProject(), myFixture.getEditor());
    LookupImpl lookup = (LookupImpl)LookupManager.getActiveLookup(myFixture.getEditor());
    assertNotNull(lookup);
    lookup.finishLookup(Lookup.NORMAL_SELECT_CHAR);
  }
}