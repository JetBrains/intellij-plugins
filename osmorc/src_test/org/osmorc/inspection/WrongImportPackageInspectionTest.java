// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.osmorc.inspection;

import org.osmorc.LightOsgiFixtureTestCase;

public class WrongImportPackageInspectionTest extends LightOsgiFixtureTestCase {
  public void test() {
    myFixture.enableInspections(new WrongImportPackageInspection());
    myFixture.configureByText(
      "MANIFEST.MF",
      "Import-Package: aQute.bnd.deployer.http,\n" +
      " aQute.bnd.deployer.repository.*,\n" +
      " aQute.maven.api;version=1.1,\n" +
      " javax.swing,\n" +
      " <error descr=\"The package is not exported by the bundle dependencies\">aQute.lib.fileset</error>\n");
    myFixture.checkHighlighting(true, false, false);
  }
}
