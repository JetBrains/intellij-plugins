package org.jetbrains.plugins.cucumber.java.resolve;

/**
 * User: Andrey.Vokin
 * Date: 3/4/13
 */
public class CucumberJavaSrcResolveTest extends BaseCucumberJavaResolveTest {
  public void testNavigationToSrc() throws Exception {
    doTest("stepResolve_03", "tes<caret>t \"test\"", "test");
  }
}
