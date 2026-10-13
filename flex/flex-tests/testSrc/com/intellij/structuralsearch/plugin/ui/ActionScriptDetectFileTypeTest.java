// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.structuralsearch.plugin.ui;

import com.intellij.lang.javascript.ActionScriptFileType;

public class ActionScriptDetectFileTypeTest extends DetectFileTypeTestCase {

  public void testDetectActionScript() {
    doTest(ActionScriptFileType.INSTANCE,
           """
             package {
             public class From {
                 public static var v;

                 private function foo() { v=<caret>0;}
             }
             }""");
  }
}
