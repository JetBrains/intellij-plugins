// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.template.postfix;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.template.postfix.JSPostfixTemplateTestCase;
import org.jetbrains.annotations.NotNull;

public class ActionScriptPostfixTemplatesTest extends JSPostfixTemplateTestCase {

  @Override
  protected @NotNull String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("/postfixTemplate/" + getSuffix());
  }

  @Override
  protected @NotNull String getSuffix() {
    return "js2";
  }

  public void testSimpleElse() {
    doTest();
  }

  public void testSimpleForIn() {
    doTestNotAvailable();
  }

  public void testSimpleIf() {
    doTest();
  }

  public void testSimpleNot() {
    doTest();
  }

  public void testSimpleNotNull() {
    doTest();
  }

  public void testSimpleNull() {
    doTest();
  }

  public void testSimplePar() {
    doTest();
  }

  public void testSimpleReturn() {
    doTest();
  }

  public void testSimpleThrow() {
    doTest();
  }

  public void testSimpleVar() {
    doTest();
  }

  @Override
  protected String getDefaultExtension() {
    return ".js2";
  }
}
