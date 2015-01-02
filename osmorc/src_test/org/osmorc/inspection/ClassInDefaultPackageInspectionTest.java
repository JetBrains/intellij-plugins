package org.osmorc.inspection;

import org.osmorc.LightOsgiFixtureTestCase;

public class ClassInDefaultPackageInspectionTest extends LightOsgiFixtureTestCase {
  public void testNegative() {
    doTest("package pkg;\npublic class C { }");
  }

  public void testPositive() {
    doTest("public class <error descr=\"Class is in the default package\">C</error> { }");
  }

  private void doTest(String text) {
    myFixture.enableInspections(new ClassInDefaultPackageInspection());
    myFixture.configureByText("C.java", text);
    myFixture.checkHighlighting(true, false, false);
  }
}
