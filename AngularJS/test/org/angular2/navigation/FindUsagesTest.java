// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationOrUsageHandler2;
import com.intellij.javascript.web.WebTestUtil;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class FindUsagesTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "/findUsages";
  }

  public void testPrivateComponentField() {
    myFixture.configureByFiles("private.ts", "private.html", "package.json");
    checkUsages("f<caret>oo",
                "<private.html:(3,6):(0,3)>\tfoo",
                "<private.html:(69,72):(0,3)>\tfoo",
                "<private.ts:(350,358):(5,8)>\tthis.foo");
  }

  public void testPrivateComponentFieldLocalHighlighting() {
    RangeHighlighter[] highlighters = myFixture.testHighlightUsages("private_highlighting.ts", "private.html", "package.json");
    String fileText = myFixture.getFile().getText();
    String[] actualHighlightWords = Arrays.stream(highlighters).map(highlighter -> {
      RangeHighlighterEx highlighterEx = (RangeHighlighterEx)highlighter;
      return fileText.substring(highlighterEx.getAffectedAreaStartOffset(), highlighterEx.getAffectedAreaEndOffset());
    }).toArray(String[]::new);
    assertSameElements(actualHighlightWords, "foo");
  }

  public void testPrivateComponentMethod() {
    myFixture.configureByFiles("private.ts", "private.html", "package.json");
    checkUsages("b<caret>ar",
                "<private.html:(13,16):(0,3)>\tbar()",
                "<private.html:(49,52):(0,3)>\tbar()",
                "<private.ts:(369,377):(5,8)>\tthis.bar()");
  }

  public void testPrivateConstructorField() {
    myFixture.configureByFiles("private.ts", "private.html", "package.json");
    checkUsages("fooB<caret>ar",
                "<private.html:(120,126):(0,6)>\tfoo + fooBar",
                "<private.html:(25,31):(0,6)>\tfooBar",
                "<private.ts:(385,396):(5,11)>\tthis.fooBar");
  }

  public void testComponentCustomElementSelector() {
    myFixture.configureByFiles("slots.component.ts", "slots.test.component.html", "slots.test.component.ts", "package.json");

    checkUsages("slots-<caret>component",
                "<slots.test.component.html:(0,187):(1,16)>\tslots-component",
                "<slots.test.component.html:(0,187):(126,141)>\tslots-component");
  }

  public void testComponentStandardElementSelector() {
    myFixture.configureByFiles("standardSelectors.ts", "package.json");
    AngularTestUtil.moveToOffsetBySignature("\"di<caret>v,", myFixture);
    // Cannot find usages of standard tags and attributes, just check outcome
    WebTestUtil.checkGTDUOutcome(myFixture, GotoDeclarationOrUsageHandler2.GTDUOutcome.GTD);
  }

  public void testComponentStandardAttributeSelector() {
    myFixture.configureByFiles("standardSelectors.ts", "package.json");
    AngularTestUtil.moveToOffsetBySignature(",[cl<caret>ass]", myFixture);
    // Cannot find usages of standard tags and attributes, just check outcome
    WebTestUtil.checkGTDUOutcome(myFixture, GotoDeclarationOrUsageHandler2.GTDUOutcome.GTD);
  }

  public void testSlotComponentElementSelector() {
    myFixture.configureByFiles("slots.component.ts", "slots.test.component.html", "slots.test.component.ts", "package.json");

    checkUsages("tag<caret>-slot",
                "<slots.test.component.html:(0,187):(21,29)>\ttag-slot",
                "<slots.test.component.html:(0,187):(61,69)>\ttag-slot");
  }

  public void testSlotComponentAttributeSelector() {
    myFixture.configureByFiles("slots.component.ts", "slots.test.component.html", "slots.test.component.ts", "package.json");

    checkUsages("attr<caret>-slot",
                "<slots.test.component.html:(0,187):(78,87)>\tattr-slot");
  }

  private void checkUsages(@NotNull String signature,
                           String @NotNull ... usages) {
    WebTestUtil.checkGTDUOutcome(myFixture, GotoDeclarationOrUsageHandler2.GTDUOutcome.SU, signature);
    assertEquals(Arrays.asList(usages), WebTestUtil.usagesAtCaret(myFixture) );
  }

}
