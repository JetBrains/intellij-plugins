// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.editor;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSLiveTemplatesFixture;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.angularjs.AngularTestUtil;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSLiveTemplatesFixture.jsLiveTemplatesFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class AngularJSLiveTemplateTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSLiveTemplatesFixture> liveTemplates =
    jsLiveTemplatesFixture(projectFixture, new JSFixtureOptions(AngularTestUtil.getBaseTestDataPath(AngularJSLiveTemplateTest.class)));

  @Test
  public void testFactory() {
    liveTemplates.get().doTest("ngmfa", "js", "AngularJS");
  }

  @Test
  public void testClassMemberContext() {
    JSLiveTemplatesFixture templatesFixture = liveTemplates.get();
    templatesFixture.getFixture().configureByText("a.js", "class C {\n<caret>\n}");
    Template template = TemplateSettings.getInstance().getTemplate("ngmfa", "AngularJS");
    templatesFixture.checkTemplateApplicability(template, false);
  }
}
