package org.jetbrains.lang.manifest;

import com.intellij.codeInsight.daemon.LightDaemonAnalyzerTestCase;

import java.io.IOException;

public class ManifestHighlightingTest extends LightDaemonAnalyzerTestCase {
  public void testMainClass() {
    doTest(
      "Main-Class: <error descr=\"Invalid reference\"></error>\n" +
      "Main-Class: org.<error descr=\"Cannot resolve\">acme</error>.Main\n" +
      "Main-Class: java.lang.<error descr=\"Invalid main class\">String</error>\n"
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
