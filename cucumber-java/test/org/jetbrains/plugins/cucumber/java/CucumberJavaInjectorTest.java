package org.jetbrains.plugins.cucumber.java;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class CucumberJavaInjectorTest extends BasePlatformTestCase {
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
}
