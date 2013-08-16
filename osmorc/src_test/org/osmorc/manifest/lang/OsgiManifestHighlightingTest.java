package org.osmorc.manifest.lang;

import com.intellij.codeInsight.daemon.LightDaemonAnalyzerTestCase;

import java.io.IOException;

public class OsgiManifestHighlightingTest extends LightDaemonAnalyzerTestCase {
  public void testBundleVersion() {
    doTest(
      "Bundle-Version: 1.<error descr=\"The minor component of the defined version is not a valid number\">0,u</error>\n"
    );
  }

  public void testBundleActivator() {
    doTest(
      "Bundle-Activator: com.<error descr=\"Cannot resolve\">acme</error>.Activator\n" +
      "Bundle-Activator: java.lang.<error descr=\"Not a valid activator class\">String</error>\n"
    );
  }

  private void doTest(String text) {
    try {
      configureFromFileText("MANIFEST.MF", text);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    doTestConfiguredFile(true, false, null);
  }
}
