// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lang.javascript.inspections.JSMethodCanBeStaticInspection;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.lang.javascript.inspections.UnterminatedStatementJSInspection;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.ThrowableRunnable;
import org.angularjs.AngularTestUtil;

public class InspectionsTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "inspections";
  }

  public void testUnusedSymbol() {
    JSTestUtils.testES6(getProject(), () -> {
      myFixture.enableInspections(JSUnusedGlobalSymbolsInspection.class);
      myFixture.enableInspections(JSUnusedLocalSymbolsInspection.class);
      myFixture.configureByFiles("unused.ts", "unused.html", "package.json");
      myFixture.checkHighlighting();
    });
  }

  public void testUnusedSetter() {
    JSTestUtils.testES6(getProject(), () -> {
      myFixture.enableInspections(JSUnusedGlobalSymbolsInspection.class);
      myFixture.enableInspections(JSUnusedLocalSymbolsInspection.class);
      myFixture.configureByFiles("unusedSetter.ts", "unusedSetter.html", "package.json");
      myFixture.checkHighlighting();
    });
  }

  public void testMethodCanBeStatic() {
    JSTestUtils.testES6(getProject(), () -> {
      myFixture.enableInspections(JSMethodCanBeStaticInspection.class);
      myFixture.configureByFiles("methodCanBeStatic.ts", "methodCanBeStatic.html", "package.json");
      myFixture.checkHighlighting();
    });
  }

  public void testUnterminated() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.enableInspections(UnterminatedStatementJSInspection.class);
      myFixture.configureByFiles("unterminated.ts", "package.json");
      myFixture.checkHighlighting();
    });
  }

  public void testUnusedReference() {
    JSTestUtils.testES6(getProject(), () -> {
      myFixture.enableInspections(JSUnusedGlobalSymbolsInspection.class);
      myFixture.enableInspections(JSUnusedLocalSymbolsInspection.class);
      myFixture.configureByFiles( "unusedReference.html", "unusedReference.ts", "package.json");
      myFixture.checkHighlighting();
    });
  }
}
