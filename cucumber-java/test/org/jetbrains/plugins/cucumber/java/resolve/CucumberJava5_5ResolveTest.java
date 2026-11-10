// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.testFramework.fixtures.LightJavaProject;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.resolve.CucumberResolveFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.testFramework.fixtures.LightJavaFixturesKt.lightJavaProjectFixture;
import static org.jetbrains.plugins.cucumber.java.resolve.CucumberJavaResolveFixtures.cucumberJavaResolveFixture;

@TestApplication
public class CucumberJava5_5ResolveTest {
  private static final TestFixture<LightJavaProject> projectFixture = lightJavaProjectFixture(CucumberJavaTestUtil.createCucumber5_5ProjectDescriptor());
  private final TestFixture<CucumberResolveFixture> cucumber = cucumberJavaResolveFixture(projectFixture);

  @Test
  public void testHighlightingOK() {
    cucumber.get().init("stepResolve_cucumber_5");
    cucumber.get().getFixture().testHighlighting("ShoppingStepdefs.java");
  }

  @Test
  public void testResolveAnnotatedParameterType() {
    cucumber.get().init("stepResolve_cucumber_5", "ShoppingStepdefs.java");

    cucumber.get().checkReference("step {col<caret>or}", "color");
  }

  @Test
  public void testOptionalsWithCyrillic() {
    //noinspection NonAsciiCharacters
    cucumber.get().doTest("optionalsWithCyrillic", "суфф<caret>икс", "cucumberExpressionWithOptional");
  }

  // Test for IDEA-295155
  @Test
  public void testOtherLanguagesWithConcatenatedWords() {
    CucumberResolveFixture resolve = cucumber.get();
    resolve.doTest("frenchLongWord", "sim<caret>ple", "Quand");
    resolve.checkReference("com<caret>plex", "Etantdonnéque");
    resolve.checkReference("<caret>complex2", "Etantdonné");
  }

  @Test
  public void testOtherLanguagesWithRemovedPunctuation() {
    cucumber.get().doTest("australianWeirdWord", "Australian<caret>IsDifferent", "Yknow");
    cucumber.get().checkReference("we have to<caret> support it anyway", "ButattheendofthedayIreckon");
  }

  @Test
  public void testPolishWithConcatenatedWord() {
    cucumber.get().doTest("polishConcatenatedWord", "jestem<caret> głodny", "Zakładającże");
    cucumber.get().checkReference("je<caret>m ciastko", "Wtedy");
  }

  // Test for IDEA-238181
  @Test
  public void testResolveAnnotatedParameterTypeWithName() {
    cucumber.get().init("stepResolve_cucumber_5", "ShoppingStepdefs.java");
    cucumber.get().checkReference("the {custom<caret>MoodName} mood is chosen", "customMoodName");
  }
}
