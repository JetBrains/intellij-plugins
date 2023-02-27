/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
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
package org.intellij.terraform.hcl.psi;

import junit.framework.TestCase;

public class HCLPsiUtilTest extends TestCase {

  public void testStripQuotes() throws Exception {
    doTestStripQuotes("'a'", "a");
    doTestStripQuotes("\"b\"", "b");
    doTestStripQuotes("\"\\\"c\\\"\"", "\\\"c\\\"");
    doTestStripQuotes("'\"d\"'", "\"d\"");
    doTestStripQuotes("'${\"e\"}'", "${\"e\"}");
  }

  public void testStripQuotesUnfinished() throws Exception {
    doTestStripQuotes("'", "");
    doTestStripQuotes("\"", "");
    doTestStripQuotes("\\\"", "\\\"");
    doTestStripQuotes("'a", "a");
    doTestStripQuotes("\"b", "b");
    doTestStripQuotes("\"\\\"c\\\"", "\\\"c\\\"");
    doTestStripQuotes("'\\\"d\\\"", "\\\"d\\\"");
    doTestStripQuotes("\"'e'", "'e'");
    doTestStripQuotes("'${\"f\"}", "${\"f\"}");
  }

  private void doTestStripQuotes(String input, String expected) {
    assertEquals(expected, HCLPsiUtil.stripQuotes(input));
  }
}
