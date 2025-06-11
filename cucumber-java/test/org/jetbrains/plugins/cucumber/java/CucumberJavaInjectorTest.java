package org.jetbrains.plugins.cucumber.java;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.testFramework.LightProjectDescriptor;

public class CucumberJavaInjectorTest extends CucumberJavaCodeInsightTestCase {
  public void testRegexIntoHookAndStepAnnotation() {
    myFixture.configureByText(JavaFileType.INSTANCE,
                              """
                                package test;

                                import cucumber.api.java.en.Given;

                                public class MyStepdefs {
                                    @cucumber.api.java.en.Given("^te<caret>st step$")
                                    public void test_step() throws Throwable {
                                        // Express the Regexp above with the code you wish you had

                                    }}"""
    );

    assertEquals(CucumberJavaInjector.regexpLanguage, myFixture.getFile().getLanguage());
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return null;
  }
}
