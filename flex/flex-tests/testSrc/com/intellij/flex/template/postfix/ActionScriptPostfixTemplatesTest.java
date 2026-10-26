// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.template.postfix;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.lang.javascript.template.postfix.JSPostfixTemplateFixture;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.template.postfix.JSPostfixTemplateFixtureKt.jsPostfixTemplateFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;

@TestApplication
public class ActionScriptPostfixTemplatesTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);
  private final TestFixture<JSPostfixTemplateFixture> postfix =
    jsPostfixTemplateFixture(projectFixture, FlexTestUtils.getTestDataPath("/postfixTemplate/js2"), ".js2");

  @Test
  public void testSimpleElse() {
    postfix.get().doTest();
  }

  @Test
  public void testSimpleForIn() {
    postfix.get().doTestNotAvailable();
  }

  @Test
  public void testSimpleIf() {
    postfix.get().doTest();
  }

  @Test
  public void testSimpleNot() {
    postfix.get().doTest();
  }

  @Test
  public void testSimpleNotNull() {
    postfix.get().doTest();
  }

  @Test
  public void testSimpleNull() {
    postfix.get().doTest();
  }

  @Test
  public void testSimplePar() {
    postfix.get().doTest();
  }

  @Test
  public void testSimpleReturn() {
    postfix.get().doTest();
  }

  @Test
  public void testSimpleThrow() {
    postfix.get().doTest();
  }

  @Test
  public void testSimpleVar() {
    postfix.get().doTest();
  }
}
