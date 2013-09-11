package org.osmorc.inspection;

import org.osmorc.AbstractOsgiTestCase;

public class UnknownManifestPackageInspectionTest extends AbstractOsgiTestCase {
  public void test() {
    doTest("Import-Package: org.osgi.*, <error descr=\"Unknown package\">wtf</error>.<error descr=\"Unknown package\">acme</error>\n");
  }

  private void doTest(String text) {
    myFixture.enableInspections(new UnknownManifestPackageInspection());
    myFixture.configureByText("MANIFEST.MF", text);
    myFixture.checkHighlighting(true, false, false);
  }
}
