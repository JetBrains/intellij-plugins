package org.osmorc.util;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.osmorc.AbstractOsgiTestCase;

public class OsgiPsiUtilTest extends AbstractOsgiTestCase {
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
      "Bundle-Version: 1.0\n" +
      "TestHeader: TestValue\n",
      true);
  }

  public void testReplaceHeaderInManifest() {
    doTest(
      "TestHeader: OldValue\n",
      "TestHeader: TestValue\n",
      true);
  }

  public void testAppendToEmptyHeader() throws Exception {
    doTest(
      "TestHeader: \n",
      "TestHeader: TestValue\n",
      false);
  }

  public void testAppendToNonEmptyHeader() throws Exception {
    doTest(
      "TestHeader: \n" +
      " OldValue1,\n" +
      " OldValue2\n",
      "TestHeader: \n" +
      " OldValue1,\n" +
      " OldValue2,\n" +
      " TestValue\n",
      false);
  }

  private void doTest(String original, String expected, final boolean replace) {
    myFixture.configureByText("MANIFEST.MF", original);

    new WriteAction() {
      @Override
      protected void run(Result result) throws Throwable {
        ManifestFile manifestFile = (ManifestFile)myFixture.getFile();
        if (replace) {
          OsgiPsiUtil.setHeader(manifestFile, "TestHeader", "TestValue");
        }
        else {
          OsgiPsiUtil.appendToHeader(manifestFile, "TestHeader", "TestValue");
        }
      }
    }.execute().throwException();

    myFixture.checkResult(expected);
  }
}
