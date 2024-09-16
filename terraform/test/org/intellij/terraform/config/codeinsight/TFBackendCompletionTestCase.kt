// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight;

public class TFBackendCompletionTestCase extends TFBaseCompletionTestCase {
  public void testTerraformBlockAdvised() {
    doBasicCompletionTest("<caret>", "terraform");
    doBasicCompletionTest("t<caret>", "terraform");
  }

  public void testBackendAllowedInTerraform() {
    doBasicCompletionTest("terraform{<caret>}", "backend");
    doBasicCompletionTest("terraform{backend \"<caret>\" {}}", "s3");
    doBasicCompletionTest("terraform{backend <caret> {}}", "s3");

//    TODO: Investigate and uncomment. For now it's ok since autocompletion handler on 'backend' would add name and braces
//    doBasicCompletionTest("terraform{backend \"<caret>\"}", "s3");
//    doBasicCompletionTest("terraform{backend <caret>}", "s3");
  }

  public void testPropertiesInBackend() {
    doBasicCompletionTest("terraform{backend \"s3\" {<caret>}}", "bucket", "key");
  }

  public void testImportBlockCompletion() {
    doTheOnlyVariantCompletionTest("im<caret>",
                                   """
                                     import {
                                       id = ""
                                       to = ""
                                     }
                                     """.trim(), true);
  }

  public void testImportToCompletion() {
    doBasicCompletionTest("""
                           import {
                             id = "abcdef"
                             to = aws_instance.<caret>
                           }
                           
                           resource "aws_instance" "a" {}
                           resource "aws_instance" "b" {}
                            """, 2, "a", "b");
  }
}
