package org.osmorc.manifest;

import com.intellij.codeInsight.completion.LightCompletionTestCase;

public class OsgiManifestCompletionTest extends LightCompletionTestCase {
  public void testAttributeCompletion() {
    configureFromFileText("MANIFEST.MF", "Export-Package: org.osgi;v<caret>\n");
    complete();
    checkResultByText("Export-Package: org.osgi;version=<caret>\n");
  }

  public void testDirectiveCompletion() {
    configureFromFileText("MANIFEST.MF", "Export-Package: org.osgi;u<caret>\n");
    complete();
    checkResultByText("Export-Package: org.osgi;uses:=<caret>\n");
  }

  public void testExportPackageCompletion() {
    configureFromFileText("MANIFEST.MF", "Export-Package: org.osgi;<caret>\n");
    complete();
    assertContainsItems("version", "uses");
  }

  public void testImportPackageCompletion() {
    configureFromFileText("MANIFEST.MF", "Import-Package: org.osgi;<caret>\n");
    complete();
    assertContainsItems("version", "resolution");
  }

  public void testImportPackageMiddleCompletion() {
    configureFromFileText("MANIFEST.MF", "Import-Package: org.osgi;<caret>;version=1.0\n");
    complete();
    assertContainsItems("version", "resolution");
  }

  public void testImportPackageResolutionCompletion() {
    configureFromFileText("MANIFEST.MF", "Import-Package: org.osgi;resolution:=<caret>\n");
    complete();
    assertContainsItems("mandatory", "optional");
  }
}
