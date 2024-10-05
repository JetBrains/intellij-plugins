// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.structuralsearch;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.structuralsearch.plugin.replace.impl.Replacer;
import junit.framework.TestCase;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.intellij.lang.javascript.flex.FlexSupportLoader.ECMA_SCRIPT_L4;
import static com.intellij.lang.javascript.JavaScriptSupportLoader.JAVASCRIPT;

public class ActionScriptStructuralReplaceTest extends JSStructuralReplaceTestBase {

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("structuralsearch/");
  }

  @Override
  protected void doTest(@Language("JavaScript") String s,
                        String what,
                        String by,
                        @Language("JavaScript") String expected,
                        boolean wrapAsSourceWithFunction) {
    if (wrapAsSourceWithFunction) {
      s = "class A { function f() {" + s + "} }";
      expected = "class A { function f() {" + expected + "} }";
    }
    doTest(s, what, by, expected, ActionScriptFileType.INSTANCE, null, ActionScriptFileType.INSTANCE,
           ActionScriptFileType.INSTANCE.getLanguage());
  }

  public void testInMxml() throws IOException {
    doTestByFile("Script.mxml", "Script_replacement1.mxml", "var $i$ = $val$", "var $i$:int = $val$", JAVASCRIPT, ECMA_SCRIPT_L4);
    doTestByFile("Script.mxml", "script_replacement2.mxml", "$func$();", "func();", JAVASCRIPT, ECMA_SCRIPT_L4);
    doTestByFile("Script.mxml", "script_replacement3.mxml", "function f(n:int) { '_st*; }", "function g(n:int) {\n  $st$;\n}", JAVASCRIPT,
                 ECMA_SCRIPT_L4);
  }

  public void testUnsupportedPatterns() {
    String s = "someCode()";
    try {
      Replacer.testReplace(s, "doc.method()", "", options, getProject(), false, true, JAVASCRIPT, ECMA_SCRIPT_L4);
      TestCase.fail();
    }
    catch (UnsupportedPatternException ignored) {}
  }
}
