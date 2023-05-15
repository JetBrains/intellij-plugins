// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight;

public class TFBackendCompletionTestCase extends TFBaseCompletionTestCase {
  public void testTerraformBlockAdvised() throws Exception {
    doBasicCompletionTest("<caret>", "terraform");
    doBasicCompletionTest("t<caret>", "terraform");
  }

  public void testBackendAllowedInTerraform() throws Exception {
    doBasicCompletionTest("terraform{<caret>}", "backend");
    doBasicCompletionTest("terraform{backend \"<caret>\" {}}", "s3");
    doBasicCompletionTest("terraform{backend <caret> {}}", "s3");

//    TODO: Investigate and uncomment. For now it's ok since autocompletion handler on 'backend' would add name and braces
//    doBasicCompletionTest("terraform{backend \"<caret>\"}", "s3");
//    doBasicCompletionTest("terraform{backend <caret>}", "s3");
  }

  public void testPropertiesInBackend() throws Exception {
    doBasicCompletionTest("terraform{backend \"s3\" {<caret>}}", "bucket", "key");
  }
}
