// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.osmorc.inspection;

import org.osmorc.LightOsgiFixtureTestCase;

public class WrongImportPackageInspectionTest extends LightOsgiFixtureTestCase {
  public void test() {
    myFixture.enableInspections(new WrongImportPackageInspection());
    myFixture.configureByText(
      "MANIFEST.MF",
      """
        Import-Package: aQute.bnd.deployer.http,
         aQute.bnd.deployer.repository.*,
         aQute.maven.api;version=1.1,
         javax.swing,
         <error descr="The package is not exported by the bundle dependencies">aQute.lib.fileset</error>
        """);
    myFixture.checkHighlighting(true, false, false);
  }
}
