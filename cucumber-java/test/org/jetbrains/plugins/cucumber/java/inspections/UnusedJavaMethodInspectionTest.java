package org.jetbrains.plugins.cucumber.java.inspections;

import com.intellij.codeInspection.deadCode.UnusedDeclarationInspection;
import com.intellij.codeInspection.unusedSymbol.UnusedSymbolLocalInspection;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

/**
 * User: Andrey.Vokin
 * Date: 10/20/12
 */
public class UnusedJavaMethodInspectionTest extends JavaCodeInsightFixtureTestCase {
  protected void doTest(final String file) {
    myFixture.addClass("package cucumber.annotation.en;\n" +
                       "\n" +
                       "import java.lang.annotation.ElementType;\n" +
                       "import java.lang.annotation.Retention;\n" +
                       "import java.lang.annotation.RetentionPolicy;\n" +
                       "import java.lang.annotation.Target;\n" +
                       "\n" +
                       "@Retention(RetentionPolicy.RUNTIME)\n" +
                       "@Target(ElementType.METHOD)\n" +
                       "public @interface Given {\n" +
                       "    String value();\n" +
                       "\n" +
                       "    int timeout() default 0;\n" +
                       "}\n" +
                       "\n");

    myFixture.addClass("package cucumber.annotation;\n" +
                       "\n" +
                       "import java.lang.annotation.ElementType;\n" +
                       "import java.lang.annotation.Retention;\n" +
                       "import java.lang.annotation.RetentionPolicy;\n" +
                       "import java.lang.annotation.Target;\n" +
                       "\n" +
                       "@Retention(RetentionPolicy.RUNTIME)\n" +
                       "@Target(ElementType.METHOD)\n" +
                       "public @interface Before {\n" +
                       "    /**\n" +
                       "     * @return a tag expression\n" +
                       "     */\n" +
                       "    String[] value() default {};\n" +
                       "\n" +
                       "    /**\n" +
                       "     * @return max amount of time this is allowed to run for. 0 (default) means no restriction.\n" +
                       "     */\n" +
                       "    int timeout() default 0;\n" +
                       "}\n");
    myFixture.enableInspections(new UnusedDeclarationInspection(), new UnusedSymbolLocalInspection());
    myFixture.configureByFile(file);
    myFixture.testHighlighting(true, false, true);
  }

  public void testStepDefinition() {
    doTest("ShoppingStepdefs.java");
  }

  public void testHooks() {
    doTest("Hooks.java");
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "inspections\\unusedMethod";
  }
}
