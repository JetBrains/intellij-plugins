package org.jetbrains.plugins.cucumber.java;

import com.intellij.ide.highlighter.JavaFileType;

/**
 * User: Andrey.Vokin
 * Date: 10/30/2014.
 */
public class CucumberJavaInjectorTest extends CucumberJavaCodeInsightTestCase {
  public void testRegexIntoHookAndStepAnnotation() {
    myFixture.configureByText(JavaFileType.INSTANCE,
      "package test;\n" +
      "\n" +
      "import cucumber.api.java.en.Given;\n" +
      "\n" +
      "public class MyStepdefs {\n" +
      "    @cucumber.api.java.en.Given(\"^te<caret>st step$\")\n" +
      "    public void test_step() throws Throwable {\n" +
      "        // Express the Regexp above with the code you wish you had\n" +
      "\n" +
      "    }" +
      "}"
    );

    assertEquals(myFixture.getFile().getLanguage(), CucumberJavaInjector.regexpLanguage);
  }
}
