// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.refactoring;

import com.intellij.execution.RunManager;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.refactoring.moveClass.FlexMoveClassProcessor;
import com.intellij.lang.javascript.JSMoveTestBase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.PackageWrapper;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesProcessor;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveDirectoryWithClassesProcessor;
import com.intellij.refactoring.move.moveClassesOrPackages.SingleSourceRootMoveDestination;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FlexMoveTest extends JSMoveTestBase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  @NotNull
  @Override
  protected String getTestRoot() {
    return "/move/";
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  @Nullable
  @Override
  protected PsiElement findElementToMove(VirtualFile rootDir, String toMove) {
    final PsiElement clazz = JSDialectSpecificHandlersFactory.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).getClassResolver()
      .findClassByQName(toMove, GlobalSearchScope.moduleScope(getModule()));
    if (clazz != null) {
      return clazz;
    }
    PsiPackage psiPackage = JavaPsiFacade.getInstance(myFixture.getProject()).findPackage(toMove);
    if (psiPackage != null) {
      return psiPackage;
    }
    return super.findElementToMove(rootDir, toMove);
  }

  @NotNull
  @Override
  protected BaseRefactoringProcessor createProcessor(List<PsiElement> toMove, PsiDirectory targetDirectory, String targetDirectoryName) {
    PsiElement first = ContainerUtil.getFirstItem(toMove);
    if (first instanceof PsiPackage) {
      PsiPackage newParentPackage = JavaPsiFacade.getInstance(myFixture.getPsiManager().getProject()).findPackage(targetDirectoryName);
      assertNotNull(newParentPackage);
      final PsiDirectory[] dirs = newParentPackage.getDirectories();
      assertEquals(1, dirs.length);
      return new MoveClassesOrPackagesProcessor(myFixture.getProject(), PsiUtilCore.toPsiElementArray(toMove),
                                                new SingleSourceRootMoveDestination(PackageWrapper.create(newParentPackage),
                                                                                    newParentPackage.getDirectories()[0]),
                                                true, false, null);
    }
    if (ContainerUtil.all(toMove, Conditions.instanceOf(JSQualifiedNamedElement.class))) {
      return new FlexMoveClassProcessor(ContainerUtil.map(toMove, psiElement -> (JSQualifiedNamedElement)psiElement), targetDirectory,
                                        targetDirectoryName.replace("/", ".").replace("\\", "."), true, true, null);
    }
    return super.createProcessor(toMove, targetDirectory, targetDirectoryName);
  }

  public void testMovePackage() {
    doTest("foo", "bar");
  }

  public void testMoveFile() {
    doTest("bar/MoveFile.as", "foo");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMoveFile2() {
    doTest("MoveFile2.mxml", "xxx");
  }

  public void testMoveFileWithImport() {
    doTest("foo/MoveFileWithImport.as", "");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMoveMxmlFileWithImport() {
    doTest("foo/MoveMxmlFileWithImport.mxml", "");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMoveFileWithImportInMxml() {
    doTest("Two.as", "foo");
  }

  // IDEADEV-40449: short references in the moved file may become ambiguous
  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void _testAmbiguous1() {
    doTest("foo/Test.as", "bar");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMxmlUsagesUpdated() {
    doTest("one/Foo.mxml", "two");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testNoImport() {
    doTest("two/Baz.mxml", "one");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMxmlNamespacesUpdated() {
    doTest("pack/Bar.as", "pack/sub");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testConfigUpdatedOnClassMove() {
    final RunManager runManager = RunManager.getInstance(myFixture.getProject());
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeClass.testSomething()", getModule(), FlexUnitRunnerParameters.Scope.Method, "",
                               "foo.bar.SomeClass", "testSomething", true);
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeClass", getModule(), FlexUnitRunnerParameters.Scope.Class, "", "foo.bar.SomeClass", "", true);
    FlexTestUtils.createFlashRunConfig(runManager, getModule(), "SomeClass", "foo.bar.SomeClass", true);

    doTest("foo/bar/SomeClass.mxml", "");

    //assertEquals("SomeClass", config.MAIN_CLASS);
    FlexTestUtils.checkFlexUnitRunConfig(runManager, getModule(), "SomeClass.testSomething()", "", "SomeClass", "testSomething");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, getModule(), "SomeClass", "", "SomeClass", "");
    FlexTestUtils.checkFlashRunConfig(runManager, getModule(), "SomeClass", "SomeClass");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testConfigUpdatedOnPackageMove() {
    FlexTestUtils.modifyBuildConfiguration(getModule(), bc -> bc.setMainClass("foo.SomeClass"));

    final RunManager runManager = RunManager.getInstance(myFixture.getProject());
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeClass.testSomething()", getModule(), FlexUnitRunnerParameters.Scope.Method, "",
                               "foo.SomeClass", "testSomething", true);
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeClass", getModule(), FlexUnitRunnerParameters.Scope.Class, "", "foo.SomeClass", "", true);
    FlexTestUtils.createFlexUnitRunConfig(runManager, "foo", getModule(), FlexUnitRunnerParameters.Scope.Package, "foo", "", "", true);
    FlexTestUtils.createFlashRunConfig(runManager, getModule(), "SomeClass", "foo.SomeClass", true);

    doTest("foo", "bar");

    assertEquals("bar.foo.SomeClass", FlexBuildConfigurationManager.getInstance(getModule()).getActiveConfiguration().getMainClass());
    FlexTestUtils.checkFlexUnitRunConfig(runManager, getModule(), "SomeClass.testSomething()", "", "bar.foo.SomeClass", "testSomething");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, getModule(), "SomeClass", "", "bar.foo.SomeClass", "");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, getModule(), "bar.foo", "bar.foo", "", "");
    FlexTestUtils.checkFlashRunConfig(runManager, getModule(), "SomeClass", "bar.foo.SomeClass");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testReferencesToAssetsUpdated() {
    doTest("one/asset.css", "two");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMxmlWithReferencesToAssetsMoved() {
    doTest("one/Foo.mxml", "");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMxmlImplementsList() {
    doTest("from/MyInterface.as", "to");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMoveClasses1() {
    doTest(new String[]{"a.MyClass", "a.MyFunc", "a.MyNs", "a.MyVar", "a.MyConst"}, "b", ArrayUtilRt.EMPTY_STRING_ARRAY);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportForConstuctorUsage() {
    doTest(new String[]{"from.Test"}, "to", ArrayUtilRt.EMPTY_STRING_ARRAY);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testConflicts1() {
    String[] conflicts = new String[]{
      "Class Subj1 with internal visibility won't be accessible from class Usage1",
      "Class Subj1 with internal visibility won't be accessible from field Usage1.f",
      "Class Subj1 with internal visibility won't be accessible from constructor Usage1.Usage1(Subj1)",
      "Class Subj2 with internal visibility won't be accessible from constant field Usage1.c",
      "Class Subj2 with internal visibility won't be accessible from constructor Usage1.Usage1(Subj1)",
      "Class Subj2 with internal visibility won't be accessible from class Usage2",
      "Class Usage1 with internal visibility won't be accessible from class Subj1",
      "Class Usage2 with internal visibility won't be accessible from class Subj2",
      "Class Subj1 with internal visibility won't be accessible from field Usage3.v",
      "Class Subj1 with internal visibility won't be accessible from function &lt;anonymous&gt;(*) in class Usage3"};
    doTest(new String[]{"a.Subj1", "a.Subj2"}, "b", conflicts);
  }

  public void testStarImport() {
    doTest(new String[]{"from.Foo"}, "to", ArrayUtilRt.EMPTY_STRING_ARRAY);
  }

  public void testNoWhitespaceForPackage() {
    doTest(new String[]{"Test", "Test2"}, "com", ArrayUtilRt.EMPTY_STRING_ARRAY);
  }

  private void doTestDirectoryWithClasses(final String[] toMove, final String target, final boolean justRename) {
    doTest(new PerformAction() {
      @Override
      public void performAction(VirtualFile rootDir, VirtualFile rootAfter) {
        Collection<PsiDirectory> dirsToMove = new ArrayList<>();
        PsiManager psiManager = myFixture.getPsiManager();

        for (String s : toMove) {
          final VirtualFile child = VfsUtilCore.findRelativeFile(s, rootDir);
          assertNotNull("Folder " + s + " not found", child);
          assertTrue("Folder " + s + " not found", child.isDirectory());
          dirsToMove.add(psiManager.findDirectory(child));
        }

        PsiDirectory targetDir;
        if (justRename) {
          targetDir = null;
        }
        else {
          final VirtualFile f = VfsUtilCore.findRelativeFile(target, rootDir);
          assertNotNull("Target dir " + target + " not found", f);
          targetDir = psiManager.findDirectory(f);
          assertNotNull(targetDir);
        }
        new MoveDirectoryWithClassesProcessor(myFixture.getProject(), dirsToMove.toArray(PsiDirectory.EMPTY_ARRAY), targetDir, true,
                                              true,
                                              false, null) {
          @Override
          protected String getTargetName() {
            if (justRename) {
              assertFalse(target.contains("/"));
              return target;
            }
            return super.getTargetName();
          }

          @NotNull
          @Override
          public TargetDirectoryWrapper getTargetDirectory(final PsiDirectory dir) {
            if (justRename) {
              return new TargetDirectoryWrapper(dir.getParentDirectory(), target);
            }
            return super.getTargetDirectory(dir);
          }
        }.run();
        FileDocumentManager.getInstance().saveAllDocuments();
      }
    });
  }

  public void testRenameDirWithClasses() {
    doTestDirectoryWithClasses(new String[]{"foo"}, "bar", true);
  }

  public static class BranchTest extends FlexMoveTest {
    @Override
    protected void setUp() throws Exception {
      super.setUp();
      Registry.get("run.refactorings.in.model.branch").setValue(true, getTestRootDisposable());
    }
  }

}
