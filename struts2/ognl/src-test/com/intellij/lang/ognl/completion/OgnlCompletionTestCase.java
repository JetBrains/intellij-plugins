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

package com.intellij.lang.ognl.completion;

import com.intellij.codeInsight.completion.LightCompletionTestCase;
import com.intellij.lang.ognl.OgnlTestUtils;

import java.util.Arrays;

/**
 * Light completion test base.
 *
 * @author Yann C&eacute;bron
 */
abstract class OgnlCompletionTestCase extends LightCompletionTestCase {

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

  protected void doTest(final String ognlExpression,
                        final String... expectedCompletionItems) throws Throwable {
    configureFromFileText(OgnlTestUtils.DUMMY_OGNL_FILE_NAME,
                          OgnlTestUtils.createExpression(ognlExpression));
    complete();

    Arrays.sort(expectedCompletionItems);
    assertStringItems(expectedCompletionItems);
  }

}