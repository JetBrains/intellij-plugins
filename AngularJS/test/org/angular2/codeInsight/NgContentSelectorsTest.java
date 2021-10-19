// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.html.HtmlTag;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.inspections.AngularUndefinedBindingInspection;
import org.angularjs.AngularTestUtil;

import java.util.Arrays;

import static com.intellij.javascript.web.WebTestUtil.resolveWebSymbolReference;
import static com.intellij.openapi.util.Pair.pair;
import static org.angularjs.AngularTestUtil.resolveReference;

public class NgContentSelectorsTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "ngContentSelectors";
  }

  public void testHighlightingSource() {
    myFixture.configureByFiles("highlighting.html", "component.ts", "package.json");
    doTestHighlighting();
  }

  public void testHighlightingPureIvy() {
    myFixture.copyDirectoryToProject("node_modules/ivy-lib", "node_modules/ivy-lib");
    myFixture.configureByFiles("highlighting.html", "package.json");
    doTestHighlighting();
  }

  public void testHighlightingMixedIvy() {
    myFixture.copyDirectoryToProject("node_modules/mixed-lib", "node_modules/mixed-lib");
    myFixture.configureByFiles("highlighting.html", "package.json");
    doTestHighlighting();
  }

  public void testHighlightingMetadata() {
    myFixture.copyDirectoryToProject("node_modules/metadata-lib", "node_modules/metadata-lib");
    myFixture.configureByFiles("highlighting.html", "package.json");
    doTestHighlighting();
  }

  private void doTestHighlighting() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class,
                                HtmlUnknownTagInspection.class,
                                AngularUndefinedBindingInspection.class);
    myFixture.checkHighlighting();
  }

  public void testResolutionSource() {
    myFixture.configureByFiles("resolution.html", "component.ts", "package.json");
    for (Pair<String, String> test : Arrays.asList(
      pair("<fo<caret>o b>", "foo,[bar]"),
      pair("<fo<caret>o c>", "foo,[bar]"),
      pair("<div b<caret>ar", "foo,[bar]"),
      pair("<bar f<caret>oo", "bar[foo]"),
      pair("<span g<caret>oo", ":not([goo])")
    )) {
      try {
        assertEquals(test.second, resolveWebSymbolReference(myFixture, test.first).getSource().getText());
      }
      catch (AssertionError error) {
        throw new AssertionError("Failed with signature: " + test.first, error);
      }
    }
    assertInstanceOf(resolveReference("<fo<caret>o a>", myFixture), HtmlTag.class);
    assertInstanceOf(resolveReference("<go<caret>o", myFixture), HtmlTag.class);
    AngularTestUtil.assertUnresolvedReference("<div f<caret>oo", myFixture);
  }
}
