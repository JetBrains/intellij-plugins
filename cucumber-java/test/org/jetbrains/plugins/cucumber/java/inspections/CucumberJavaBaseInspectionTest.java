package org.jetbrains.plugins.cucumber.java.inspections;

import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;

/**
 * User: Andrey.Vokin
 * Date: 1/9/13
 */
public abstract class CucumberJavaBaseInspectionTest extends JavaCodeInsightFixtureTestCase {
  public void setUp() throws Exception {
    super.setUp();

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
  }
}
