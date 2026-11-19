// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import static org.assertj.core.api.Assertions.assertThat;

@TestApplication
public class ComponentsTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(getClass()) + "components");
  }

  @Test
  public void testEditableFieldInspections() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.enableInspections(JSUndeclaredVariableInspection.class,
                                JSUnresolvedReferenceInspection.class,
                                JSUnusedGlobalSymbolsInspection.class,
                                JSUnusedLocalSymbolsInspection.class,
                                HtmlUnknownAttributeInspection.class,
                                HtmlUnknownTagInspection.class);
    myFixture.configureByFiles("editableField.html", "editableField.js", "angular.js");
    myFixture.checkHighlighting(true, false, true);
  }

  @Test
  public void testHeroDetailInspections() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.enableInspections(JSUndeclaredVariableInspection.class,
                                JSUnresolvedReferenceInspection.class,
                                JSUnusedGlobalSymbolsInspection.class,
                                JSUnusedLocalSymbolsInspection.class,
                                HtmlUnknownAttributeInspection.class,
                                HtmlUnknownTagInspection.class);
    myFixture.configureByFiles("heroDetail.html", "editableField.html", "editableField.js", "heroDetail.js", "angular.js");
    myFixture.checkHighlighting(true, false, true);
  }

  @Test
  public void testHeroDetailContentAssist() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("heroDetail.html", "editableField.html", "editableField.js", "heroDetail.js", "angular.js");
    AngularTestUtil.moveToOffsetBySignature("\"<caret>vm.delete()\"", myFixture);
    myFixture.completeBasic();
    assertThat(myFixture.getLookupElementStrings()).contains("vm");
  }

  @Test
  public void testHeroDetailContentAssistOnVM() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("heroDetail.html", "editableField.html", "editableField.js", "heroDetail.js", "angular.js");
    AngularTestUtil.moveToOffsetBySignature("\"vm.<caret>delete()\"", myFixture);
    myFixture.completeBasic();
    assertThat(myFixture.getLookupElementStrings()).contains("delete", "hero", "update", "onDelete", "onUpdate");
  }

  @Test
  public void testEditableFieldContentAssist() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("editableField.html", "editableField.js", "angular.js");
    AngularTestUtil.moveToOffsetBySignature("<div>{{<caret>}}</div>", myFixture);
    myFixture.completeBasic();
    assertThat(myFixture.getLookupElementStrings()).contains("$ctrl");
  }

  @Test
  public void testEditableFieldContentAssistOnCtrl() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("editableField.html", "editableField.js", "angular.js");
    AngularTestUtil.moveToOffsetBySignature("\"$ctrl.<caret>editMode\"", myFixture);
    myFixture.completeBasic();
    assertThat(myFixture.getLookupElementStrings()).contains("editMode", "fieldValue", "fieldType", "onUpdate", "reset", "handleModeChange");
    assertThat(myFixture.getLookupElementStrings()).doesNotContain("$onInit");
  }

  @Test
  public void testTemplateReferencedThroughImport() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("heroDetail.html", "editableField.html", "editableField.js", "heroDetail.import.js", "angular.js");
    AngularTestUtil.moveToOffsetBySignature("\"<caret>vm.delete()\"", myFixture);
    myFixture.completeBasic();
    assertThat(myFixture.getLookupElementStrings()).contains("vm");
  }

  @Test
  public void testTemplateReferencedThroughRequire() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("heroDetail.html", "editableField.html", "editableField.js", "heroDetail.require.js", "angular.js");
    AngularTestUtil.moveToOffsetBySignature("\"<caret>vm.delete()\"", myFixture);
    myFixture.completeBasic();
    assertThat(myFixture.getLookupElementStrings()).contains("vm");
  }

  @Test
  public void testSimilarComponents() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                HtmlUnknownTagInspection.class);
    myFixture.configureByFiles("similarComponents.html", "angular.js");
    myFixture.checkHighlighting(true, false, true);
  }

}
