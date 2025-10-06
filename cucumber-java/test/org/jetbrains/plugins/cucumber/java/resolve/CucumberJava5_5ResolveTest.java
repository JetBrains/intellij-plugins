// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class CucumberJava5_5ResolveTest extends BaseCucumberJavaResolveTest {
  public void testHighlightingOK() {
    init("stepResolve_cucumber_5");
    myFixture.testHighlighting("ShoppingStepdefs.java");
  }

  public void testResolveAnnotatedParameterType() {
    init("stepResolve_cucumber_5", "ShoppingStepdefs.java");

    checkReference("step {col<caret>or}", "color");
  }

  public void testOptionalsWithCyrillic() {
    //noinspection NonAsciiCharacters
    doTest("optionalsWithCyrillic", "суфф<caret>икс", "cucumberExpressionWithOptional");
  }

  // Test for IDEA-295155
  public void testOtherLanguagesWithConcatenatedWords() {
    doTest("frenchLongWord", "sim<caret>ple", "Quand");
    checkReference("com<caret>plex", "Etantdonnéque");
    checkReference("<caret>complex2", "Etantdonné");
  }

  public void testOtherLanguagesWithRemovedPunctuation() {
    doTest("australianWeirdWord", "Australian<caret>IsDifferent", "Yknow");
    checkReference("we have to<caret> support it anyway", "ButattheendofthedayIreckon");
  }

  public void testPolishWithConcatenatedWord() {
    doTest("polishConcatenatedWord", "jestem<caret> głodny", "Zakładającże");
    checkReference("je<caret>m ciastko", "Wtedy");
  }

  // Test for IDEA-238181
  public void testResolveAnnotatedParameterTypeWithName() {
    init("stepResolve_cucumber_5", "ShoppingStepdefs.java");
    checkReference("the {custom<caret>MoodName} mood is chosen", "customMoodName");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber5_5ProjectDescriptor();
  }
}
