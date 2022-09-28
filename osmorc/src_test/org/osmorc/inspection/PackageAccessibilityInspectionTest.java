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
      """
        package pkg;
        import aQute.bnd.repository.fileset.FileSetRepository;
        import aQute.lib.fileset.FileSet;
        public class C {
          public static void main() {
            <error descr="The package 'javax.swing' is not imported in the manifest">javax.swing.Icon</error> icon = null;
            <error descr="The package 'aQute.bnd.repository.fileset' is not imported in the manifest">FileSetRepository</error> repo = null;
            <error descr="The package 'aQute.lib.fileset' is not exported by the bundle dependencies">FileSet</error> set = null;
          }
        }""");
  }

  public void testImportPackage() {
    doTest(
      """
        package pkg;
        public class C {
          public static void main() {
            javax.swing.Icon icon = null;
            aQute.bnd.repository.fileset.FileSetRepository repo = null;
          }
        }""",

      "Import-Package: javax.swing, aQute.bnd.repository.fileset\n");
  }

  public void testRequireBundle() {
    doTest(
      """
        package pkg;
        public class C {
          public static void main() {
            aQute.bnd.repository.fileset.FileSetRepository repo = null;
          }
        }""",

      "Require-Bundle: biz.aQute.repository\n");
  }

  public void testAutoImport() {
    myConfiguration.setManifestGenerationMode(ManifestGenerationMode.Bnd);
    doTest(
      """
        package pkg;
        import aQute.lib.fileset.FileSet;
        public class C {
          public static void main() {
            javax.swing.Icon icon = null;
            aQute.bnd.repository.fileset.FileSetRepository repo = null;
            <error descr="The package 'aQute.lib.fileset' is not exported by the bundle dependencies">FileSet</error> set = null;
          }
        }""");
  }

  public void testAnnotation() {
    doTest(
      """
        package pkg;
        import org.jetbrains.annotations.*;
        public class C {
          @NotNull String s;
        }""");
  }

  public void testNonBundledDependency() {
    doTest(
      """
        package pkg;
        import org.codehaus.plexus.util.IOUtil;
        public class C {
          <weak_warning descr="The package 'org.codehaus.plexus.util' is inside a non-bundle dependency">IOUtil</weak_warning> ref;
        }""");
  }

  public void testPrivatePackage() {
    doTest(
      """
        package pkg;
        public class C {
          public static void main() {
            aQute.lib.fileset.FileSet set = null;
          }
        }""",

      "Private-Package: aQute.lib.fileset\n");
  }

  public void testQuickFixExported() {
    doTestFix(
      """
        package pkg;
        import aQute.bnd.repository.fileset.*;
        public abstract class C {
          private <caret>FileSetRepository repo = null;
        }""",

      "Import-Package: javax.sql\n",

      """
        Import-Package: javax.sql,
         aQute.bnd.repository.fileset
        """);
  }

  public void testQuickFixImplicit() {
    doTestFix(
      """
        package pkg;
        import javax.swing.*;
        public class C {
          public static void main() {
            <caret>Icon icon = null;
          }
        }""",

      "Import-Package: org.osgi.framework\n",

      """
        Import-Package: org.osgi.framework,
         javax.swing
        """);
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
