// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection;
import com.intellij.lang.javascript.inspections.JSUndeclaredVariableInspection;
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.angularjs.AngularTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;

@TestApplication
public class DirectivesTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(getClass()) + "directives");
  }

  @Test
  public void testBindToControllerAttrs() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    enableInspections();
    myFixture.configureByFiles("bind-to-controller.html", "angular.js");
    myFixture.checkHighlighting(true, false, true);
  }

  @Test
  public void testBindToControllerProps1() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    enableInspections();
    myFixture.configureByFiles("bind-to-controller-1.html", "bind-to-controller.html", "angular.js");
    myFixture.checkHighlighting(true, false, true);
  }

  @Test
  public void testBindToControllerProps2() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    enableInspections();
    myFixture.configureByFiles("bind-to-controller-2.html", "bind-to-controller.html", "angular.js");
    myFixture.checkHighlighting(true, false, true);
  }

  @Test
  public void testHighlighting() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    enableInspections();
    myFixture.configureByFiles("create-titles-ids-injected.html", "create-titles-ids.js", "angular.js");
    myFixture.checkHighlighting(true, false, true);
  }

  private void enableInspections() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.enableInspections(JSUndeclaredVariableInspection.class,
                                JSUnresolvedReferenceInspection.class,
                                JSUnusedGlobalSymbolsInspection.class,
                                JSUnusedLocalSymbolsInspection.class,
                                HtmlUnknownAttributeInspection.class,
                                HtmlUnknownTagInspection.class);
  }
}
