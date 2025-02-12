// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight;

public class TfLocalsCompletionTestCase extends TfBaseCompletionTestCase {
  public void testLocalsBlockAdvised() throws Exception {
    doBasicCompletionTest("<caret>", "locals");
    doBasicCompletionTest("l<caret>", "locals");
  }

  public void testNothingShowedInLocals() throws Exception {
    doBasicCompletionTest("locals{<caret>}", 0);
  }
}
