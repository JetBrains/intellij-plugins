package org.osmorc.util;

import com.intellij.openapi.command.WriteCommandAction;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.osmorc.LightOsgiFixtureTestCase;

public class OsgiPsiUtilTest extends LightOsgiFixtureTestCase {
  public void testSetHeaderInEmptyManifest() {
    doTest(
      "",
      "TestHeader: TestValue\n",
      true);
  }

  public void testSetHeaderInHalfEmptyManifest() {
    doTest(
      "\n",
      "TestHeader: TestValue\n\n",
      true);
  }

  public void testSetHeaderInNonEmptyManifest() {
    doTest(
      "Bundle-Version: 1.0\n",
      """
        Bundle-Version: 1.0
        TestHeader: TestValue
        """,
      true);
  }

  public void testReplaceHeaderInManifest() {
    doTest(
      "TestHeader: OldValue\n",
      "TestHeader: TestValue\n",
      true);
  }

  public void testAppendToEmptyHeader() {
    doTest(
      "TestHeader: \n",
      "TestHeader: TestValue\n",
      false);
  }

  public void testAppendToNonEmptyHeader() {
    doTest(
      """
        TestHeader:\s
         OldValue1,
         OldValue2
        """,
      """
        TestHeader:\s
         OldValue1,
         OldValue2,
         TestValue
        """,
      false);
  }

  private void doTest(String original, String expected, final boolean replace) {
    myFixture.configureByText("MANIFEST.MF", original);

    WriteCommandAction.runWriteCommandAction(null, () -> {
  ManifestFile manifestFile = (ManifestFile)myFixture.getFile();
  if (replace) {
    OsgiPsiUtil.setHeader(manifestFile, "TestHeader", "TestValue");
  }
  else {
    OsgiPsiUtil.appendToHeader(manifestFile, "TestHeader", "TestValue");
  }
});

    myFixture.checkResult(expected);
  }
}
