package org.osmorc.inspection;

import org.osmorc.AbstractOsgiTestCase;

public class WrongImportPackageInspectionTest extends AbstractOsgiTestCase {
  public void test() {
    doTest(
      "Import-Package: org.osgi.framework,\n" +
      " org.osgi.util.*,\n" +
      " org.osgi.service.startlevel;version=1.0,\n" +
      " <error descr=\"The package is not exported by the bundle dependencies\">org.osgi.resource;version=2.0</error>,\n" +
      " <error descr=\"The package is not exported by the bundle dependencies\">org.apache.felix.framework</error>\n");
  }

  private void doTest(String text) {
    myFixture.enableInspections(new WrongImportPackageInspection());
    myFixture.configureByText("MANIFEST.MF", text);
    myFixture.checkHighlighting(true, false, false);
  }
}
