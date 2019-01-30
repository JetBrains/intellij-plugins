// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
      "  @Override public void start(BundleContext context)  { }\n" +
      "  @Override public void stop(BundleContext context)  { }\n" +
      "}"
    );

    doTest(
      "Bundle-Activator: <error descr=\"Cannot resolve class 'com.acme.Activator'\">com.acme.Activator</error>\n" +
      "Bundle-Activator: <error descr=\"Not a valid activator class\">java.lang.String</error>\n" +
      "Bundle-Activator: main.Activator\n"
    );
  }

  public void testInvalidPackages() {
    myFixture.addClass(
      "package main;\n" +
      "public class C { }"
    );

    doTest(
      "Import-Package: org.osgi.*,\n" +
      " <error descr=\"Cannot resolve package com.acme\">com.acme</error>\n" +
      "Private-Package: <error descr=\"Cannot resolve package com.acme\">com.acme</error>\n" +
      "Ignore-Package: <error descr=\"Cannot resolve package com.acme\">com.acme</error>\n" +
      "Export-Package: main;uses:=\"<error descr=\"Invalid reference\"> </error>,\n" +
      " <error descr=\"Cannot resolve package com.acme.p1\">com.acme.p1</error>\n" +
      " ,<error descr=\"Cannot resolve package com.acme.p2\">com.acme.p2</error>\"\n"
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
