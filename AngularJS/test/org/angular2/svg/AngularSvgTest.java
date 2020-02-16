// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.svg;

import one.util.streamex.StreamEx;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.inspections.Angular2TemplateInspectionsProvider;
import org.angularjs.AngularTestUtil;

import static com.intellij.util.containers.ContainerUtil.sorted;
import static org.angularjs.AngularTestUtil.renderLookupItems;

public class AngularSvgTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  public void testHighlighting() {
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.copyDirectoryToProject(".", ".");
    myFixture.configureFromTempProjectFile("svg-highlighting.component.svg");
    myFixture.checkHighlighting();
  }

  public void testCompletion() {
    myFixture.copyDirectoryToProject(".", ".");
    myFixture.configureFromTempProjectFile("svg-completion.component.svg");
    AngularTestUtil.moveToOffsetBySignature("<<caret>paths></paths>", myFixture);
    myFixture.completeBasic();
    assertEquals(StreamEx.of(
      "a", "altGlyphDef", "animate", "animateColor", "animateMotion", "animateTransform", "circle", "clipPath", "color-profile", "cursor",
      "defs", "desc", "ellipse", "filter", "font", "font-face", "foreignObject", "g", "image", "line", "linearGradient", "marker", "mask",
      "metadata", "path", "pattern", "polygon", "polyline", "radialGradient", "rect", "script", "set", "style", "svg", "switch", "symbol",
      "text", "title", "use", "view"
                 ).map(str -> str + "#http://www.w3.org/2000/svg").sorted().toList(),
                 sorted(renderLookupItems(myFixture, false, true)));
  }

  public void testExpressionsCompletion() {
    myFixture.copyDirectoryToProject(".", ".");
    myFixture.configureFromTempProjectFile("svg-completion.component.svg");
    AngularTestUtil.moveToOffsetBySignature("{{<caret>item.height}}", myFixture);
    myFixture.completeBasic();
    assertEquals(StreamEx.of(
      "!$any#any#4", "!height#number#101", "!item#null#101", "!items#null#101"
                 ).sorted().toList(),
                 sorted(renderLookupItems(myFixture, true, true)));
  }

  public void testExpressionsCompletion2() {
    myFixture.copyDirectoryToProject(".", ".");
    myFixture.configureFromTempProjectFile("svg-completion.component.svg");
    AngularTestUtil.moveToOffsetBySignature("{{item.<caret>height}}", myFixture);
    myFixture.completeBasic();
    assertEquals(StreamEx.of(
      "!foo#string#101", "!width#number#101", "constructor#Function#99", "hasOwnProperty#boolean#99", "isPrototypeOf#boolean#99",
      "propertyIsEnumerable#boolean#99", "toLocaleString#string#99", "toString#string#99", "valueOf#Object#99"
                 ).sorted().toList(),
                 sorted(renderLookupItems(myFixture, true, true)));
  }

}
