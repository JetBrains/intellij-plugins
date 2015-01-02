package org.osmorc.manifest.lang;

import org.osmorc.LightOsgiFixtureTestCase;

public class OsgiManifestHighlightingTest extends LightOsgiFixtureTestCase {
  public void testBundleVersion() {
    doTest(
      "Bundle-Version: 1\n" +
      "Bundle-Version: 1.0.0.FINAL\n" +
      "Bundle-Version: <error descr=\"invalid version \\\\\"1.0,u\\\\\": non-numeric \\\\\"0,u\\\\\"\">1.0,u</error>\n" +
      "Bundle-Version: <error descr=\"invalid version \\\\\"1.0.0.?\\\\\": invalid qualifier \\\\\"?\\\\\"\">1.0.0.?</error>\n"
    );
  }

  public void testBundleActivator() {
    myFixture.addClass(
      "package main;\n" +
      "import org.osgi.framework.*;\n" +
      "public class Activator extends BundleActivator {\n" +
      "  public void start(BundleContext context) throws Exception { }\n" +
      "  public void stop(BundleContext context) throws Exception { }\n" +
      "}"
    );

    doTest(
      "Bundle-Activator: com.<error descr=\"Cannot resolve\">acme</error>.Activator\n" +
      "Bundle-Activator: java.lang.<error descr=\"Not a valid activator class\">String</error>\n" +
      "Bundle-Activator: main.Activator\n"
    );
  }

  public void testSelfRequiringBundle() {
    doTest(
      "Bundle-SymbolicName: t0\n" +
      "Bundle-Version: 1.0.0\n" +
      "Require-Bundle: t0\n"
    );
  }

  private void doTest(String text) {
    myFixture.configureByText("MANIFEST.MF", text);
    myFixture.checkHighlighting(true, false, false);
  }
}
