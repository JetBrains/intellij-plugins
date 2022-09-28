// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.osmorc.manifest.lang;

import org.osmorc.LightOsgiFixtureTestCase;

public class OsgiManifestHighlightingTest extends LightOsgiFixtureTestCase {
  public void testBundleVersion() {
    doTest(
      """
        Bundle-Version: 1
        Bundle-Version: 1.0.0.FINAL
        Bundle-Version: <error descr="invalid version \\\\"1.0,u\\\\": non-numeric \\\\"0,u\\\\"">1.0,u</error>
        Bundle-Version: <error descr="invalid version \\\\"1.0.0.?\\\\": invalid qualifier \\\\"?\\\\"">1.0.0.?</error>
        """
    );
  }

  public void testBundleActivator() {
    myFixture.addClass(
      """
        package main;
        import org.osgi.framework.*;
        public class Activator extends BundleActivator {
          @Override public void start(BundleContext context)  { }
          @Override public void stop(BundleContext context)  { }
        }"""
    );

    doTest(
      """
        Bundle-Activator: <error descr="Cannot resolve class 'com.acme.Activator'">com.acme.Activator</error>
        Bundle-Activator: <error descr="Not a valid activator class">java.lang.String</error>
        Bundle-Activator: main.Activator
        """
    );
  }

  public void testInvalidPackages() {
    myFixture.addClass(
      "package main;\n" +
      "public class C { }"
    );

    doTest(
      """
        Import-Package: org.osgi.*,
         <error descr="Cannot resolve package com.acme">com.acme</error>
        Private-Package: <error descr="Cannot resolve package com.acme">com.acme</error>
        Ignore-Package: <error descr="Cannot resolve package com.acme">com.acme</error>
        Export-Package: main;uses:="<error descr="Invalid reference"> </error>,
         <error descr="Cannot resolve package com.acme.p1">com.acme.p1</error>
         ,<error descr="Cannot resolve package com.acme.p2">com.acme.p2</error>"
        """
    );
  }

  public void testSelfRequiringBundle() {
    doTest(
      """
        Bundle-SymbolicName: t0
        Bundle-Version: 1.0.0
        Require-Bundle: t0
        """
    );
  }

  private void doTest(String text) {
    myFixture.configureByText("MANIFEST.MF", text);
    myFixture.checkHighlighting(true, false, false);
  }
}
