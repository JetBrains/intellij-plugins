// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
