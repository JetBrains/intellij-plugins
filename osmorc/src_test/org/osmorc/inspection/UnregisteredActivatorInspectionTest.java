package org.osmorc.inspection;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiFile;
import org.osmorc.AbstractOsgiTestCase;
import org.osmorc.i18n.OsmorcBundle;

public class UnregisteredActivatorInspectionTest extends AbstractOsgiTestCase {
  public void testPositive() {
    doTest(
      "package pkg;\n" +
      "import org.osgi.framework.*;\n" +
      "public class <error descr=\"Bundle activator is not registered in the manifest\">C</error> implements BundleActivator {\n" +
      "  public void start(BundleContext context) throws Exception { }\n" +
      "  public void stop(BundleContext context) throws Exception { }\n" +
      "}",
      "");
  }

  public void testNegative() {
    doTest(
      "package pkg;\n" +
      "import org.osgi.framework.*;\n" +
      "public class C implements BundleActivator {\n" +
      "  public void start(BundleContext context) throws Exception { }\n" +
      "  public void stop(BundleContext context) throws Exception { }\n" +
      "}",
      "Bundle-Activator: pkg.C\n");
  }

  public void testAbstract() {
    doTest(
      "package pkg;\n" +
      "import org.osgi.framework.*;\n" +
      "public abstract class C implements BundleActivator { }",
      "");
  }

  public void testFixEmptyManifest() {
    doTestFix(
      "",
      "Bundle-Activator: pkg.C\n");
  }

  public void testFixHalfEmptyManifest() {
    doTestFix(
      "\n",
      "Bundle-Activator: pkg.C\n\n");
  }

  public void testFixAppendToManifest() {
    doTestFix(
      "Bundle-Version: 1.0\n",
      "Bundle-Version: 1.0\n" +
      "Bundle-Activator: pkg.C\n");
  }

  public void testFixReplaceInManifest() {
    doTestFix(
      "Bundle-Activator: pkg.X\n",
      "Bundle-Activator: pkg.C\n");
  }

  private void doTest(String classText, String manifestText) {
    myFixture.enableInspections(new UnregisteredActivatorInspection());
    myFixture.addFileToProject("META-INF/MANIFEST.MF", manifestText);
    myFixture.configureByText("C.java", classText);
    myFixture.checkHighlighting(true, false, false);
  }

  private void doTestFix(String text, String expected) {
    myFixture.enableInspections(new UnregisteredActivatorInspection());
    PsiFile manifest = myFixture.addFileToProject("META-INF/MANIFEST.MF", text);
    myFixture.configureByText(
      "C.java",
      "package pkg;" +
      "import org.osgi.framework.*;\n" +
      "public class <caret>C implements BundleActivator {\n" +
      "  public void start(BundleContext context) throws Exception { }\n" +
      "  public void stop(BundleContext context) throws Exception { }\n" +
      "}");
    IntentionAction intention = myFixture.findSingleIntention(OsmorcBundle.message("UnregisteredActivatorInspection.fix.manifest"));
    myFixture.launchAction(intention);
    assertEquals(expected, manifest.getText());
  }
}
