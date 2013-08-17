package org.osmorc.manifest.lang;

import com.intellij.openapi.application.PluginPathManager;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

public class OsgiManifestHighlightingTest extends LightCodeInsightFixtureTestCase {
  public void testBundleVersion() {
    doTest(
      "Bundle-Version: 1.<error descr=\"The minor component of the defined version is not a valid number\">0,u</error>\n"
    );
  }

  public void testBundleActivator() {
    PsiTestUtil.addLibrary(myModule, "osgi.core", PluginPathManager.getPluginHomePath("Osmorc") + "/lib", "org.eclipse.osgi.jar");

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
