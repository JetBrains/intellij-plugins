// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.intentions;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSIntentionFixture;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSIntentionFixture.jsIntentionFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class ActionScriptAnonymousToNamedFunctionIntentionTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSIntentionFixture> js =
    jsIntentionFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("/highlighting/intention/anonymousToNamedFunction")));

  @Test
  public void testAnonymousToNamedFunction_AS() {
    js.get().doCompositeNameBeforeAfterTest("as", false);
  }

  @Test
  public void testAnonymousToNamedFunction_ActionScriptClassMember() {
    js.get().doCompositeNameBeforeAfterTest("as", false);
  }
}
