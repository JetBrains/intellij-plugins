package org.osmorc.inspection;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiFile;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.osmorc.LightOsgiFixtureTestCase;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.i18n.OsmorcBundle;

public class PackageAccessibilityInspectionTest extends LightOsgiFixtureTestCase {
  private static final String POSITIVE_TEST =
    "package pkg;\n" +
    "import org.osgi.framework.launch.*;\n" +
    "public class C {\n" +
    "  public static void main() {\n" +
    "    <error descr=\"The package is not imported in the manifest\">javax.swing.Icon</error> icon = null;\n" +
    "    <error descr=\"The package is not imported in the manifest\">FrameworkFactory</error> factory =\n" +
    "      new <error descr=\"The package is not exported by the bundle dependencies\">org.apache.felix.framework.FrameworkFactory</error>();\n" +
    "  }\n" +
    "}";

  private static final String NEGATIVE_TEST =
    "package pkg;\n" +
    "import org.osgi.framework.*;\n" +
    "public class C implements BundleActivator {\n" +
    "  public void start(BundleContext context) throws Exception { }\n" +
    "  public void stop(BundleContext context) throws Exception { }\n" +
    "}";

  public void testEmpty() {
    doTest(POSITIVE_TEST, "");
  }

  public void testImportPackage() {
    doTest(NEGATIVE_TEST, "Import-Package: org.osgi.framework\n");
  }

  public void testRequireBundle() {
    doTest(NEGATIVE_TEST, "Require-Bundle: org.apache.felix.framework\n");
  }

  public void testRequireBundleVersionMiss() {
    doTest(POSITIVE_TEST, "Require-Bundle: org.apache.felix.framework;bundle-version=\"[0,3)\"\n");
  }

  public void testAutoImport() {
    OsmorcFacet facet = OsmorcFacet.getInstance(myModule);
    assert facet != null;
    try {
      facet.getConfiguration().setManifestGenerationMode(ManifestGenerationMode.Bnd);
      doTest(
        "package pkg;\n" +
        "import org.osgi.framework.launch.*;\n" +
        "public class C {\n" +
        "  public static void main() {\n" +
        "    javax.swing.Icon icon = null;\n" +
        "    FrameworkFactory factory =\n" +
        "      new <error descr=\"The package is not exported by the bundle dependencies\">org.apache.felix.framework.FrameworkFactory</error>();\n" +
        "  }\n" +
        "}", "");
    }
    finally {
      facet.getConfiguration().setManifestGenerationMode(ManifestGenerationMode.Manually);
    }
  }

  public void testQuickFixExported() {
    doTestFix(
      "package pkg;\n" +
      "import org.osgi.framework.*;\n" +
      "public abstract class C implements <caret>BundleActivator { }",

      "Import-Package: javax.sql\n",

      "Import-Package: javax.sql,\n" +
      " org.osgi.framework\n");
  }

  public void testQuickFixImplicit() {
    doTestFix(
      "package pkg;\n" +
      "import javax.swing.*;\n" +
      "public class C {\n" +
      "  public static void main() {\n" +
      "    <caret>Icon icon = null;\n" +
      "  }\n" +
      "}",

      "Import-Package: org.osgi.framework\n",

      "Import-Package: org.osgi.framework,\n" +
      " javax.swing\n");
  }

  public void testAnnotation() {
    doTest(
      "package pkg;\n" +
      "import org.jetbrains.annotations.*;\n" +
      "public class C {\n" +
      "  @NotNull String s;\n" +
      "}",

      "");
  }

  private void doTest(String classText, String manifestText) {
    myFixture.enableInspections(new PackageAccessibilityInspection());
    myFixture.addFileToProject("META-INF/MANIFEST.MF", manifestText);
    myFixture.configureByText("C.java", classText);
    myFixture.checkHighlighting(true, false, false);
  }

  private void doTestFix(String classSource, String manifestSource, String expected) {
    myFixture.enableInspections(new PackageAccessibilityInspection());
    PsiFile manifest = myFixture.addFileToProject("META-INF/MANIFEST.MF", manifestSource);
    myFixture.configureByText("C.java", classSource);
    IntentionAction intention = myFixture.findSingleIntention(OsmorcBundle.message("PackageAccessibilityInspection.fix"));
    myFixture.launchAction(intention);
    assertEquals(expected, manifest.getText());
  }
}
