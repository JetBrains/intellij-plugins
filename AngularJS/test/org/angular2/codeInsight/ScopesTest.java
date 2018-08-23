// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

public class ScopesTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "scopes";
  }

  public void testReferencesAndVariablesScopes() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.configureByFiles("refsAndVars.html", "refsAndVars.ts", "package.json");
      final String fileText = myFixture.getFile().getText();
      final String TEST_PREFIX = "resolveRef-";
      int lastCase = 0;
      int i = 0;
      int offset;
      while((offset = fileText.indexOf(TEST_PREFIX, lastCase)) > 0) {
        i++;
        lastCase = offset + 8;
        char result = fileText.charAt(offset + TEST_PREFIX.length());
        assert result == 'T' || result == 'F' : "Bad result spec for test case " + i + ": " + result;

        PsiReference ref = myFixture.getFile().findReferenceAt(offset + TEST_PREFIX.length() + 6);
        assertNotNull("Ref is empty for test case " + i, ref);
        PsiElement resolve = ref.resolve();
        assertEquals("Bad result for test case " + i, result == 'T', resolve != null);
      }
    });
  }

}
