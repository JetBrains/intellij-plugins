// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertSame;

@TestApplication
public class JadeInjectionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);

  @Test
  public void testInjected() {
    CodeInsightTestFixture myFixture = fixture.get();
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
    assertSame(JadeLanguage.INSTANCE, ReadAction.computeBlocking(() -> myFixture.getFile().getLanguage()));
  }
}
