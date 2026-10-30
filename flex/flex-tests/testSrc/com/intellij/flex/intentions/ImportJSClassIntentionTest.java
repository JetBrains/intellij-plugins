// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.intentions;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSIntentionFixture;
import com.intellij.lang.javascript.flex.ActionScriptAutoImportOptionsProvider;
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSIntentionFixture.jsIntentionFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class ImportJSClassIntentionTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSIntentionFixture> js =
    jsIntentionFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("") + "/importclass"));

  @BeforeEach
  void setUp() {
    js.get().getFixture().enableInspections(new JSUnresolvedReferenceInspection());
  }

  @Test
  public void testImportClass() {
    js.get().doTestFor("Test1.as", "FooClass.as");
  }

  @Test
  public void testImportComponent() {
    js.get().doTestForAndCheckFirstFile("Test2.as", "aPackage/FooComponent.mxml");
  }

  @Test
  public void testPackageLocal() {
    js.get().doTestFor(js.get().getTestName(false) + ".as", js.get().getTestName(false) + "_2.as");
  }

  @Test
  public void testUnambiguousImportsOnTheFly() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    boolean old = ActionScriptAutoImportOptionsProvider.isAddUnambiguousImportsOnTheFly();
    ActionScriptAutoImportOptionsProvider.setAddUnambiguousImportsOnTheFly(true);
    try {
      ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
      CodeInsightSettings.runWithTemporarySettings(settings -> {
        settings.ADD_UNAMBIGIOUS_IMPORTS_ON_THE_FLY = false;
        DaemonCodeAnalyzerSettings.getInstance().setImportHintEnabled(true);

        final String testName = js.get().getTestName(false);
        myFixture.configureByFiles(testName + ".as", testName + "_2.as");
        myFixture.type(" ");
        myFixture.type("\b");
        myFixture.doHighlighting();
        CodeInsightTestFixtureImpl.waitForLazyQuickFixesUnderCaret(js.get().getProject(), myFixture.getEditor());
        EdtTestUtil.runInEdtAndWait(() -> PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue());
        myFixture.checkResultByFile(testName + "_after.as");
        return null;
      });
    }
    finally {
      ActionScriptAutoImportOptionsProvider.setAddUnambiguousImportsOnTheFly(old);
    }
  }
}