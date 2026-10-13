// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.parsing;

import com.intellij.lang.html.HTMLParserDefinition;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.testFramework.junit5.TestApplication;
import org.junit.jupiter.api.Test;

@TestApplication
public class HbHtmlParserTest extends HbParserTest {

  public HbHtmlParserTest() {
    super(new HTMLParserDefinition(), new JavascriptParserDefinition());
  }

  @Override
  protected boolean checkAllPsiRoots() {
    return true;
  }

  @Test
  public void testInjectionInJSString() {
    doTest(true);
  }
}
