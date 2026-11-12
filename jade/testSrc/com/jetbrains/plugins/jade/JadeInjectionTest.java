// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class JadeInjectionTest extends BasePlatformTestCase {
  public void testInjected() {
    myFixture.configureByText("injectedJade.js", """
      // language=Jade
      var template = "\\
      h1 Pug - node template engine\\
          #container.col\\
            if youAreUsingPug\\
              p You are amazing\\
            else\\
              p <caret>Get on it!\\
            p.\\
              Pug is a terse and simple templating language with a\\
              strong focus on performance and powerful features.\\
      ";""");
    assertSame(JadeLanguage.INSTANCE, myFixture.getFile().getLanguage());
  }
}
