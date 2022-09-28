// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class ActivatorRenameTest extends LightOsgiFixtureTestCase {
  private PsiClass myActivator;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    PsiFile activatorFile = myFixture.addFileToProject("src/t1/Activator.java",
                                                       """
                                                         package t1;

                                                         import org.osgi.framework.*;

                                                         public class Activator  implements BundleActivator {
                                                             public void start(BundleContext context) throws Exception { }
                                                             public void stop(BundleContext context) throws Exception { }
                                                         }
                                                         """);
    myActivator = ((PsiJavaFile)activatorFile).getClasses()[0];
  }

  public void testRenameForManuallyEditedManifest() {
    PsiFile manifestFile = setupManualManifest();
    renameClass();
    assertThat(manifestFile.getText()).endsWith("Bundle-Activator: t1.RenamedActivator\n");
  }

  public void testMoveForManuallyEditedManifest() {
    PsiFile manifestFile = setupManualManifest();
    renamePackage();
    assertThat(manifestFile.getText()).endsWith("Bundle-Activator: tx.Activator\n");
  }

  public void testRenameForGeneratedManifest() {
    myFacet.getConfiguration().setManifestGenerationMode(ManifestGenerationMode.OsmorcControlled);
    myFacet.getConfiguration().setBundleActivator("t1.Activator");
    renameClass();
    assertThat(myFacet.getConfiguration().getBundleActivator()).isEqualTo("t1.RenamedActivator");
  }

  public void testMoveForGeneratedManifest() {
    myFacet.getConfiguration().setManifestGenerationMode(ManifestGenerationMode.OsmorcControlled);
    myFacet.getConfiguration().setBundleActivator("t1.Activator");
    renamePackage();
    assertThat(myFacet.getConfiguration().getBundleActivator()).isEqualTo("tx.Activator");
  }

  private PsiFile setupManualManifest() {
    myFacet.getConfiguration().setManifestGenerationMode(ManifestGenerationMode.Manually);
    return myFixture.addFileToProject("META-INF/MANIFEST.MF",
                                      """
                                        Manifest-Version: 1.0
                                        Bundle-ManifestVersion: 2
                                        Bundle-Name: T1
                                        Bundle-SymbolicName: t1
                                        Bundle-Version: 1.0.0
                                        Import-Package: org.osgi.framework
                                        Bundle-Activator: t1.Activator
                                        """);
  }

  private void renameClass() {
    RefactoringFactory.getInstance(getProject()).createRename(myActivator, "RenamedActivator").run();
  }

  private void renamePackage() {
    VirtualFile sourceRoot = ModuleRootManager.getInstance(getModule()).getSourceRoots()[0];
    JavaRefactoringFactory factory = JavaRefactoringFactory.getInstance(getProject());
    MoveDestination moveDestination = factory.createSourceRootMoveDestination("tx", sourceRoot);
    factory.createMoveClassesOrPackages(new PsiElement[]{myActivator}, moveDestination).run();
  }
}