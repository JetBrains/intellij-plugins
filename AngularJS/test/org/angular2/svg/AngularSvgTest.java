// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.svg;

import com.intellij.webSymbols.WebTestUtil;
import one.util.streamex.StreamEx;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.inspections.Angular2TemplateInspectionsProvider;
import org.angularjs.AngularTestUtil;

import static com.intellij.util.containers.ContainerUtil.sorted;
import static org.angular2.modules.Angular2TestModule.*;
import static org.angularjs.AngularTestUtil.renderLookupItems;

public class AngularSvgTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  public void testHighlighting() {
    configureCopy(myFixture, ANGULAR_COMMON_4_0_0, ANGULAR_CORE_4_0_0);
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureByFiles("svg-highlighting.component.svg","svg-highlighting.component.ts");
    myFixture.checkHighlighting();
  }

  public void testCompletion() {
    configureLink(myFixture, ANGULAR_COMMON_4_0_0, ANGULAR_CORE_4_0_0);
    myFixture.configureByFiles("svg-completion.component.svg", "svg-completion.component.ts");
    AngularTestUtil.moveToOffsetBySignature("<<caret>paths></paths>", myFixture);
    myFixture.completeBasic();
    WebTestUtil.checkListByFile(myFixture, WebTestUtil.renderLookupItems(myFixture, true, true),
                                "svg-completion.component.txt", false);
  }

  public void testExpressionsCompletion() {
    configureCopy(myFixture, ANGULAR_COMMON_4_0_0, ANGULAR_CORE_4_0_0);
    myFixture.copyDirectoryToProject(".", ".");
    myFixture.configureByFiles("svg-completion.component.svg", "svg-completion.component.ts");
    AngularTestUtil.moveToOffsetBySignature("{{<caret>item.height}}", myFixture);
    myFixture.completeBasic();
    assertEquals(StreamEx.of(
      "!$any#any#4", "!height#number#101", "!item#null#101", "!items#null#101"
                 ).sorted().toList(),
                 sorted(renderLookupItems(myFixture, true, true, true)));
  }

  public void testExpressionsCompletion2() {
    configureCopy(myFixture, ANGULAR_COMMON_4_0_0, ANGULAR_CORE_4_0_0);
    myFixture.copyDirectoryToProject(".", ".");
    myFixture.configureByFiles("svg-completion.component.svg", "svg-completion.component.ts");
    AngularTestUtil.moveToOffsetBySignature("{{item.<caret>height}}", myFixture);
    myFixture.completeBasic();
    assertEquals(StreamEx.of(
      "!foo#string#101", "!width#number#101", "constructor#Function#98", "hasOwnProperty#boolean#98", "isPrototypeOf#boolean#98",
      "propertyIsEnumerable#boolean#98", "toLocaleString#string#98", "toString#string#98", "valueOf#Object#98"
                 ).sorted().toList(),
                 sorted(renderLookupItems(myFixture, true, true, false)));
  }

}
