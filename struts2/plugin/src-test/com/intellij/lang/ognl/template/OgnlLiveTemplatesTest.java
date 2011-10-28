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

package com.intellij.lang.ognl.template;

import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.codeInsight.template.impl.actions.ListTemplatesAction;
import com.intellij.lang.ognl.OgnlTestUtils;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

/**
 * @author Yann C&eacute;bron
 */
public class OgnlLiveTemplatesTest extends LightCodeInsightFixtureTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    OgnlTestUtils.installOgnlFileType();
  }

  @Override
  protected void tearDown() throws Exception {
    OgnlTestUtils.removeOgnlFileType();

    super.tearDown();
  }

  public void testIn() {
    doTest("a in<caret>",
           "a in {<caret>, }");
  }

  public void testNotIn() {
    doTest("a nin<caret>",
           "a not in {<caret>, }");
  }

  private void doTest(final String before, final String after) {
    myFixture.configureByText(OgnlTestUtils.DUMMY_OGNL_FILE_NAME, before);
    expandLiveTemplate();
    myFixture.checkResult(after);
  }

  private void expandLiveTemplate() {
    new WriteCommandAction(myFixture.getProject()) {
      @Override
      protected void run(final Result result) throws Throwable {
        new ListTemplatesAction().actionPerformedImpl(myFixture.getProject(), myFixture.getEditor());
        final LookupImpl lookup = (LookupImpl) LookupManager.getActiveLookup(myFixture.getEditor());
        assert lookup != null;
        lookup.finishLookup(Lookup.NORMAL_SELECT_CHAR);
      }
    }.execute();
  }

}