package com.intellij.flex.refactoring;

import com.intellij.execution.RunManager;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.javascript.flex.refactoring.moveClass.FlexMoveClassProcessor;
import com.intellij.lang.javascript.JSMoveTestBase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveDirectoryWithClassesProcessor;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexMoveTest extends JSMoveTestBase {
  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

  @NotNull
  @Override
  protected String getTestRoot() {
    return "/move/";
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  @Override
  protected FlexMoveClassProcessor createCustomMoveProcessor(Collection<PsiElement> files,
                                                             PsiDirectory targetDirectory,
                                                             String targetDirName) {
    return new FlexMoveClassProcessor(ContainerUtil.map(files, psiElement -> (JSQualifiedNamedElement)psiElement), targetDirectory,
                                      targetDirName.replace("/", ".").replace("\\", "."), true, true, null);
  }

  public void testMovePackage() throws Exception {
    doTest("foo", "bar");
  }

  public void testMoveFile() throws Exception {
    doTest("bar/MoveFile.as", "foo");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMoveFile2() throws Exception {
    doTest("MoveFile2.mxml", "xxx");
  }

  public void testMoveFileWithImport() throws Exception {
    doTest("foo/MoveFileWithImport.as", "");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMoveMxmlFileWithImport() throws Exception {
    doTest("foo/MoveMxmlFileWithImport.mxml", "");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMoveFileWithImportInMxml() throws Exception {
    doTest("Two.as", "foo");
  }

  // IDEADEV-40449: short references in the moved file may become ambiguous
  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void _testAmbiguous1() throws Exception {
    doTest("foo/Test.as", "bar");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMxmlUsagesUpdated() throws Exception {
    doTest("one/Foo.mxml", "two");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testNoImport() throws Exception {
    doTest("two/Baz.mxml", "one");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMxmlNamespacesUpdated() throws Exception {
    doTest("pack/Bar.as", "pack/sub");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testConfigUpdatedOnClassMove() throws Exception {
    final RunManager runManager = RunManager.getInstance(myProject);
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeClass.testSomething()", myModule, FlexUnitRunnerParameters.Scope.Method, "",
                               "foo.bar.SomeClass", "testSomething", true);
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeClass", myModule, FlexUnitRunnerParameters.Scope.Class, "", "foo.bar.SomeClass", "", true);
    FlexTestUtils.createFlashRunConfig(runManager, myModule, "SomeClass", "foo.bar.SomeClass", true);

    doTest("foo/bar/SomeClass.mxml", "");

    //assertEquals("SomeClass", config.MAIN_CLASS);
    FlexTestUtils.checkFlexUnitRunConfig(runManager, myModule, "SomeClass.testSomething()", "", "SomeClass", "testSomething");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, myModule, "SomeClass", "", "SomeClass", "");
    FlexTestUtils.checkFlashRunConfig(runManager, myModule, "SomeClass", "SomeClass");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testConfigUpdatedOnPackageMove() throws Exception {
    FlexTestUtils.modifyBuildConfiguration(myModule, bc -> bc.setMainClass("foo.SomeClass"));

    final RunManager runManager = RunManager.getInstance(myProject);
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeClass.testSomething()", myModule, FlexUnitRunnerParameters.Scope.Method, "",
                               "foo.SomeClass", "testSomething", true);
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeClass", myModule, FlexUnitRunnerParameters.Scope.Class, "", "foo.SomeClass", "", true);
    FlexTestUtils.createFlexUnitRunConfig(runManager, "foo", myModule, FlexUnitRunnerParameters.Scope.Package, "foo", "", "", true);
    FlexTestUtils.createFlashRunConfig(runManager, myModule, "SomeClass", "foo.SomeClass", true);

    doTest("foo", "bar");

    assertEquals("bar.foo.SomeClass", FlexBuildConfigurationManager.getInstance(myModule).getActiveConfiguration().getMainClass());
    FlexTestUtils.checkFlexUnitRunConfig(runManager, myModule, "SomeClass.testSomething()", "", "bar.foo.SomeClass", "testSomething");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, myModule, "SomeClass", "", "bar.foo.SomeClass", "");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, myModule, "bar.foo", "bar.foo", "", "");
    FlexTestUtils.checkFlashRunConfig(runManager, myModule, "SomeClass", "bar.foo.SomeClass");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testReferencesToAssetsUpdated() throws Exception {
    doTest("one/asset.css", "two");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMxmlWithReferencesToAssetsMoved() throws Exception {
    doTest("one/Foo.mxml", "");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMxmlImplementsList() throws Exception {
    doTest("from/MyInterface.as", "to");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMoveClasses1() {
    doTest(new String[]{"a.MyClass", "a.MyFunc", "a.MyNs", "a.MyVar", "a.MyConst"}, "b", ArrayUtil.EMPTY_STRING_ARRAY);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportForConstuctorUsage() {
    doTest(new String[]{"from.Test"}, "to", ArrayUtil.EMPTY_STRING_ARRAY);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testConflicts1() {
    String[] conflicts = new String[]{
      "Class Subj1 with internal visibility won't be accessible from class Usage1",
      "Class Subj1 with internal visibility won't be accessible from field Usage1.f",
      "Class Subj1 with internal visibility won't be accessible from constructor Usage1.Usage1(Subj1)",
      "Class Subj2 with internal visibility won't be accessible from constant Usage1.c",
      "Class Subj2 with internal visibility won't be accessible from constructor Usage1.Usage1(Subj1)",
      "Class Subj2 with internal visibility won't be accessible from class Usage2",
      "Class Usage1 with internal visibility won't be accessible from class Subj1",
      "Class Usage2 with internal visibility won't be accessible from class Subj2",
      "Class Subj1 with internal visibility won't be accessible from field Usage3.v",
      "Class Subj1 with internal visibility won't be accessible from function &lt;anonymous&gt;(*) in class Usage3"};
    doTest(new String[]{"a.Subj1", "a.Subj2"}, "b", conflicts);
  }

  public void testStarImport() {
    doTest(new String[]{"from.Foo"}, "to", ArrayUtil.EMPTY_STRING_ARRAY);
  }

  public void testNoWhitespaceForPackage() {
    doTest(new String[]{"Test", "Test2"}, "com", ArrayUtil.EMPTY_STRING_ARRAY);
  }

  private void doTestDirectoryWithClasses(final String[] toMove, final String target, final boolean justRename) {
    doTest(new PerformAction() {
      @Override
      public void performAction(VirtualFile rootDir, VirtualFile rootAfter) {
        Collection<PsiDirectory> dirsToMove = new ArrayList<>();
        for (String s : toMove) {
          final VirtualFile child = VfsUtilCore.findRelativeFile(s, rootDir);
          assertNotNull("Folder " + s + " not found", child);
          assertTrue("Folder " + s + " not found", child.isDirectory());
          dirsToMove.add(myPsiManager.findDirectory(child));
        }

        PsiDirectory targetDir;
        if (justRename) {
          targetDir = null;
        }
        else {
          final VirtualFile f = VfsUtilCore.findRelativeFile(target, rootDir);
          assertNotNull("Target dir " + target + " not found", f);
          targetDir = myPsiManager.findDirectory(f);
          assertNotNull(targetDir);
        }
        new MoveDirectoryWithClassesProcessor(myProject, dirsToMove.toArray(new PsiDirectory[dirsToMove.size()]), targetDir, true, true,
                                              false, null) {
          @Override
          protected String getTargetName() {
            if (justRename) {
              assertFalse(target.contains("/"));
              return target;
            }
            return super.getTargetName();
          }

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
}
