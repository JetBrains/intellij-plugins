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
import com.intellij.lang.ognl.OgnlFileType;
import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileTypeManager;

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

    associateOgnlFileType(false);
  }

  @Override
  protected void tearDown() throws Exception {
    associateOgnlFileType(true);

    super.tearDown();
  }

  private static void associateOgnlFileType(final boolean remove) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        if (remove) {
          FileTypeManager.getInstance().removeAssociatedExtension(OgnlFileType.INSTANCE,
                                                                  OgnlFileType.INSTANCE.getDefaultExtension());
          return;
        }
        FileTypeManager.getInstance().associateExtension(OgnlFileType.INSTANCE,
                                                         OgnlFileType.INSTANCE.getDefaultExtension());
      }
    });
  }

  protected void doTest(final String ognlExpression,
                        final String... expectedCompletionItems) throws Throwable {
    configureFromFileText("test." + OgnlFileType.INSTANCE.getDefaultExtension(),
                          OgnlLanguage.EXPRESSION_PREFIX + ognlExpression + OgnlLanguage.EXPRESSION_SUFFIX);
    complete();

    Arrays.sort(expectedCompletionItems);
    assertStringItems(expectedCompletionItems);
  }

}