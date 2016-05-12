package org.osmorc.refactoring;

import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.refactoring.JavaRefactoringFactory;
import com.intellij.refactoring.MoveDestination;
import com.intellij.refactoring.RefactoringFactory;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.osmorc.LightOsgiFixtureTestCase;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ActivatorRenameTest extends LightOsgiFixtureTestCase {
  private PsiClass myActivator;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    PsiFile activatorFile = myFixture.addFileToProject("src/t1/Activator.java",
        "package t1;\n\n" +
        "import org.osgi.framework.*;\n\n" +
        "public class Activator  implements BundleActivator {\n" +
        "    public void start(BundleContext context) throws Exception { }\n" +
        "    public void stop(BundleContext context) throws Exception { }\n" +
        "}\n");
    myActivator = ((PsiJavaFile)activatorFile).getClasses()[0];
  }

  public void testRenameForManuallyEditedManifest() throws Exception {
    PsiFile manifestFile = setupManualManifest();
    renameClass();
    assertThat(manifestFile.getText(), endsWith("Bundle-Activator: t1.RenamedActivator\n"));
  }

  public void testMoveForManuallyEditedManifest() {
    PsiFile manifestFile = setupManualManifest();
    renamePackage();
    assertThat(manifestFile.getText(), endsWith("Bundle-Activator: tx.Activator\n"));
  }

  public void testRenameForGeneratedManifest() {
    myFacet.getConfiguration().setManifestGenerationMode(ManifestGenerationMode.OsmorcControlled);
    myFacet.getConfiguration().setBundleActivator("t1.Activator");
    renameClass();
    assertThat(myFacet.getConfiguration().getBundleActivator(), equalTo("t1.RenamedActivator"));
  }

  public void testMoveForGeneratedManifest() {
    myFacet.getConfiguration().setManifestGenerationMode(ManifestGenerationMode.OsmorcControlled);
    myFacet.getConfiguration().setBundleActivator("t1.Activator");
    renamePackage();
    assertThat(myFacet.getConfiguration().getBundleActivator(), equalTo("tx.Activator"));
  }

  private PsiFile setupManualManifest() {
    myFacet.getConfiguration().setManifestGenerationMode(ManifestGenerationMode.Manually);
    return myFixture.addFileToProject("META-INF/MANIFEST.MF",
        "Manifest-Version: 1.0\n" +
        "Bundle-ManifestVersion: 2\n" +
        "Bundle-Name: T1\n" +
        "Bundle-SymbolicName: t1\n" +
        "Bundle-Version: 1.0.0\n" +
        "Import-Package: org.osgi.framework\n" +
        "Bundle-Activator: t1.Activator\n");
  }

  private void renameClass() {
    RefactoringFactory.getInstance(getProject()).createRename(myActivator, "RenamedActivator").run();
  }

  private void renamePackage() {
    VirtualFile sourceRoot = ModuleRootManager.getInstance(myModule).getSourceRoots()[0];
    JavaRefactoringFactory factory = JavaRefactoringFactory.getInstance(getProject());
    MoveDestination moveDestination = factory.createSourceRootMoveDestination("tx", sourceRoot);
    factory.createMoveClassesOrPackages(new PsiElement[]{myActivator}, moveDestination).run();
  }
}
