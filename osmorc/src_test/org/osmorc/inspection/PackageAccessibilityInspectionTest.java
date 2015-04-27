/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
      "import org.osgi.framework.launch.FrameworkFactory;\n" +
      "public class C {\n" +
      "  public static void main() {\n" +
      "    <error descr=\"The package 'javax.swing' is not imported in the manifest\">javax.swing.Icon</error> icon = null;\n" +
      "    <error descr=\"The package 'org.osgi.framework.launch' is not imported in the manifest\">FrameworkFactory</error> factory =\n" +
      "      new <error descr=\"The package 'org.apache.felix.framework' is not exported by the bundle dependencies\">org.apache.felix.framework.FrameworkFactory</error>();\n" +
      "  }\n" +
      "}");
  }

  public void testImportPackage() {
    doTest(
      "package pkg;\n" +
      "import org.osgi.framework.*;\n" +
      "public class C implements BundleActivator {\n" +
      "  public void start(BundleContext context) throws Exception { }\n" +
      "  public void stop(BundleContext context) throws Exception { }\n" +
      "}",

      "Import-Package: org.osgi.framework\n");
  }

  public void testRequireBundle() {
    doTest(
      "package pkg;\n" +
      "import org.osgi.framework.*;\n" +
      "public class C implements BundleActivator {\n" +
      "  public void start(BundleContext context) throws Exception { }\n" +
      "  public void stop(BundleContext context) throws Exception { }\n" +
      "}",

      "Require-Bundle: org.apache.felix.framework\n");
  }

  public void testAutoImport() {
    myConfiguration.setManifestGenerationMode(ManifestGenerationMode.Bnd);
    doTest(
      "package pkg;\n" +
      "import org.apache.felix.framework.FrameworkFactory;\n" +
      "import org.osgi.framework.launch.*;\n" +
      "public class C {\n" +
      "  public static void main() {\n" +
      "    javax.swing.Icon icon = null;\n" +
      "    <error descr=\"The package 'org.apache.felix.framework' is not exported by the bundle dependencies\">FrameworkFactory</error> factory =\n" +
      "      new <error descr=\"The package 'org.apache.felix.framework' is not exported by the bundle dependencies\">FrameworkFactory</error>();\n" +
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
      "    new org.apache.felix.framework.FrameworkFactory();\n" +
      "  }\n" +
      "}",

      "Private-Package: org.apache.felix.framework\n");
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
