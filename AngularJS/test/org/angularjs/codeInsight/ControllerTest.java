// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection;
import com.intellij.lang.javascript.inspections.JSUndeclaredVariableInspection;
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

public class ControllerTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "components";
  }

  public void testEditableFieldInspections() {
    myFixture.enableInspections(JSUndeclaredVariableInspection.class,
                                JSUnresolvedVariableInspection.class,
                                JSUnusedGlobalSymbolsInspection.class,
                                JSUnusedLocalSymbolsInspection.class,
                                HtmlUnknownAttributeInspection.class,
                                HtmlUnknownTagInspection.class);
    myFixture.configureByFiles("editableField.html", "editableField.js", "angular.js");
    myFixture.checkHighlighting(true, false, true);
  }


  public void testHeroDetailInspections() {
    myFixture.enableInspections(JSUndeclaredVariableInspection.class,
                                JSUnresolvedVariableInspection.class,
                                JSUnusedGlobalSymbolsInspection.class,
                                JSUnusedLocalSymbolsInspection.class,
                                HtmlUnknownAttributeInspection.class,
                                HtmlUnknownTagInspection.class);
    myFixture.configureByFiles("heroDetail.html", "editableField.html", "editableField.js", "heroDetail.js", "angular.js");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testHeroDetailContentAssist() {
    myFixture.configureByFiles("heroDetail.html", "editableField.html", "editableField.js", "heroDetail.js", "angular.js");
    AngularTestUtil.moveToOffsetBySignature("\"<caret>vm.delete()\"", myFixture);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "vm");
  }

  public void testHeroDetailContentAssistOnVM() {
    myFixture.configureByFiles("heroDetail.html", "editableField.html", "editableField.js", "heroDetail.js", "angular.js");
    AngularTestUtil.moveToOffsetBySignature("\"vm.<caret>delete()\"", myFixture);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "delete", "hero", "update", "onDelete", "onUpdate");
  }

  public void testEditableFieldContentAssist() {
    myFixture.configureByFiles("editableField.html", "editableField.js", "angular.js");
    AngularTestUtil.moveToOffsetBySignature("<div>{{<caret>}}</div>", myFixture);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "$ctrl");
  }

  public void testEditableFieldContentAssistOnCtrl() {
    myFixture.configureByFiles("editableField.html", "editableField.js", "angular.js");
    AngularTestUtil.moveToOffsetBySignature("\"$ctrl.<caret>editMode\"", myFixture);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "editMode", "fieldValue", "fieldType", "onUpdate", "reset", "handleModeChange");
    assertDoesntContain(myFixture.getLookupElementStrings(),"$onInit");
  }

}
