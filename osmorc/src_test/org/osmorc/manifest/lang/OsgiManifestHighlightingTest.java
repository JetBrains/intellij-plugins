package org.osmorc.manifest.lang;

import org.osmorc.AbstractOsgiTestCase;

public class OsgiManifestHighlightingTest extends AbstractOsgiTestCase {
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

  private void doTest(String text) {
    myFixture.configureByText("MANIFEST.MF", text);
    myFixture.checkHighlighting(true, false, false);
  }
}
