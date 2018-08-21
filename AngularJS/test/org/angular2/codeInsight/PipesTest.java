// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

import java.util.List;

public class PipesTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "pipes";
  }

  public void testPipeCompletion() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), () -> {
      myFixture.configureByFiles("pipe.html", "angular2.js", "custom.ts");
      final List<String> variants = myFixture.getCompletionVariants("pipe.html");
      assertContainsElements(variants, "filta");
    });
  }

  public void testPipeResolve() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), () -> {
      myFixture.configureByFiles("pipeCustom.resolve.html", "angular2.js", "custom.ts");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("fil<caret>ta", myFixture.getFile());
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom.ts", resolve.getContainingFile().getName());
      assertEquals("Pipe({\n" +
                   "    name: \"filta\"\n" +
                   "})", resolve.getParent().getText());
    });
  }

}
