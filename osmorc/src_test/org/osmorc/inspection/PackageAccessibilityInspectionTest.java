// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.osmorc.inspection;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiFile;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.osmorc.LightOsgiFixtureTestCase;
import org.osmorc.i18n.OsmorcBundle;

public class PackageAccessibilityInspectionTest extends LightOsgiFixtureTestCase {
  public void testEmpty() {
    doTest(
      "package pkg;\n" +
      "import aQute.bnd.repository.fileset.FileSetRepository;\n" +
      "import aQute.lib.fileset.FileSet;\n" +
      "public class C {\n" +
      "  public static void main() {\n" +
      "    <error descr=\"The package 'javax.swing' is not imported in the manifest\">javax.swing.Icon</error> icon = null;\n" +
      "    <error descr=\"The package 'aQute.bnd.repository.fileset' is not imported in the manifest\">FileSetRepository</error> repo = null;\n" +
      "    <error descr=\"The package 'aQute.lib.fileset' is not exported by the bundle dependencies\">FileSet</error> set = null;\n" +
      "  }\n" +
      "}");
  }

  public void testImportPackage() {
    doTest(
      "package pkg;\n" +
      "public class C {\n" +
      "  public static void main() {\n" +
      "    javax.swing.Icon icon = null;\n" +
      "    aQute.bnd.repository.fileset.FileSetRepository repo = null;\n" +
      "  }\n" +
      "}",

      "Import-Package: javax.swing, aQute.bnd.repository.fileset\n");
  }

  public void testRequireBundle() {
    doTest(
      "package pkg;\n" +
      "public class C {\n" +
      "  public static void main() {\n" +
      "    aQute.bnd.repository.fileset.FileSetRepository repo = null;\n" +
      "  }\n" +
      "}",

      "Require-Bundle: biz.aQute.repository\n");
  }

  public void testAutoImport() {
    myConfiguration.setManifestGenerationMode(ManifestGenerationMode.Bnd);
    doTest(
      "package pkg;\n" +
      "import aQute.lib.fileset.FileSet;\n" +
      "public class C {\n" +
      "  public static void main() {\n" +
      "    javax.swing.Icon icon = null;\n" +
      "    aQute.bnd.repository.fileset.FileSetRepository repo = null;\n" +
      "    <error descr=\"The package 'aQute.lib.fileset' is not exported by the bundle dependencies\">FileSet</error> set = null;\n" +
      "  }\n" +
      "}");
  }

  public void testAnnotation() {
    doTest(
      "package pkg;\n" +
      "import org.jetbrains.annotations.*;\n" +
      "public class C {\n" +
      "  @NotNull String s;\n" +
      "}");
  }

  public void testNonBundledDependency() {
    doTest(
      "package pkg;\n" +
      "import org.codehaus.plexus.util.IOUtil;\n" +
      "public class C {\n" +
      "  <weak_warning descr=\"The package 'org.codehaus.plexus.util' is inside a non-bundle dependency\">IOUtil</weak_warning> ref;\n" +
      "}");
  }

  public void testPrivatePackage() {
    doTest(
      "package pkg;\n" +
      "public class C {\n" +
      "  public static void main() {\n" +
      "    aQute.lib.fileset.FileSet set = null;\n" +
      "  }\n" +
      "}",

      "Private-Package: aQute.lib.fileset\n");
  }

  public void testQuickFixExported() {
    doTestFix(
      "package pkg;\n" +
      "import aQute.bnd.repository.fileset.*;\n" +
      "public abstract class C {\n" +
      "  private <caret>FileSetRepository repo = null;\n" +
      "}",

      "Import-Package: javax.sql\n",

      "Import-Package: javax.sql,\n" +
      " aQute.bnd.repository.fileset\n");
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

  private void doTest(String classText) {
    doTest(classText, "");
  }

  private void doTest(String classText, String manifestText) {
    myFixture.enableInspections(new PackageAccessibilityInspection());
    myFixture.addFileToProject("META-INF/MANIFEST.MF", manifestText);
    myFixture.configureByText("C.java", classText);
    myFixture.checkHighlighting(true, false, true);
  }

  private void doTestFix(String classText, String manifestText, String expected) {
    myFixture.enableInspections(new PackageAccessibilityInspection());
    PsiFile manifest = myFixture.addFileToProject("META-INF/MANIFEST.MF", manifestText);
    myFixture.configureByText("C.java", classText);
    IntentionAction intention = myFixture.findSingleIntention(OsmorcBundle.message("PackageAccessibilityInspection.fix"));
    myFixture.launchAction(intention);
    assertEquals(expected, manifest.getText());
  }
}
