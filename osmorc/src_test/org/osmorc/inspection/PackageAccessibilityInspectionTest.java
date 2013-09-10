package org.osmorc.inspection;

import org.osmorc.AbstractOsgiTestCase;

public class PackageAccessibilityInspectionTest extends AbstractOsgiTestCase {
  private static final String POSITIVE_TEST =
    "package pkg;\n" +
    "import org.osgi.framework.*;\n" +
    "public class C implements <error descr=\"Package not accessible inside the OSGi context\">BundleActivator</error> {\n" +
    "  public void start(<error descr=\"Package not accessible inside the OSGi context\">BundleContext</error> context) throws Exception {\n" +
    "    System.out.println(new <error descr=\"Package not accessible inside the OSGi context\">Version</error>(\"1.0\"));\n" +
    "  }\n" +
    "  public void stop(<error descr=\"Package not accessible inside the OSGi context\">BundleContext</error> context) throws Exception { }\n" +
    "}";

  private static final String NEGATIVE_TEST =
    "package pkg;\n" +
    "import org.osgi.framework.*;\n" +
    "public class C implements BundleActivator {\n" +
    "  public void start(BundleContext context) throws Exception { }\n" +
    "  public void stop(BundleContext context) throws Exception { }\n" +
    "}";

  public void testEmpty() {
    doTest(POSITIVE_TEST, "");
  }

  public void testImportPackage() {
    doTest(NEGATIVE_TEST, "Import-Package: org.osgi.framework\n");
  }

  public void testRequireBundle() {
    doTest(NEGATIVE_TEST, "Require-Bundle: org.apache.felix.framework\n");
  }

  public void testRequireBundleVersionMiss() {
    doTest(POSITIVE_TEST, "Require-Bundle: org.apache.felix.framework;bundle-version=\"[0,3)\"\n");
  }

  private void doTest(String classText, String manifestText) {
    myFixture.enableInspections(new PackageAccessibilityInspection());
    myFixture.addFileToProject("META-INF/MANIFEST.MF", manifestText);
    myFixture.configureByText("C.java", classText);
    myFixture.checkHighlighting(true, false, false);
  }
}
