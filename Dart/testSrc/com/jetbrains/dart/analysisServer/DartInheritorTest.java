package com.jetbrains.dart.analysisServer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.codeInsight.hierarchy.HierarchyViewTestBase;
import com.jetbrains.lang.dart.ide.index.DartClassIndex;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DartInheritorTest extends HierarchyViewTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
  }

  @Override
  protected VirtualFile configureByFiles(@Nullable final File rawProjectRoot, @NotNull final VirtualFile... vFiles) throws IOException {
    VirtualFile root = super.configureByFiles(rawProjectRoot, vFiles);
    return DartTestUtils.configureNavigation(this, root, vFiles);
  }

  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/inheritor/");
  }

  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH;
  }

  protected void doTest(String superclassName, String subclassName) throws Exception {
    doTest(superclassName, subclassName, true);
  }

  protected void doTest(String superclassName, String subclassName, boolean expectPass) throws Exception {
    doTest(superclassName, subclassName, expectPass, getTestName(false) + ".dart");
  }

  protected void doTest(String superclassName, String subclassName, boolean expectPass, String... fileNames) throws Exception {
    final String[] relFilePaths = new String[fileNames.length];
    for (int i = 0; i < fileNames.length; i++) {
      relFilePaths[i] = getBasePath() + fileNames[i];
    }
    configureByFiles(null, relFilePaths);
    DartClass dartSuperclass = findDartClass(superclassName);
    assertNotNull(dartSuperclass);
    DartClass dartSubClass = findDartClass(subclassName);
    assertNotNull(dartSubClass);
    boolean result = dartSubClass.isInheritor(dartSuperclass);
    if (expectPass) {
      assertTrue(result);
    } else {
      assertFalse(result);
    }
  }

  private DartClass findDartClass(String className) {
    final Project project = getProject();
    if (className != null) {
      final List<DartComponentName> dartComponentNames =
        DartClassIndex.getItemsByName(className, project, GlobalSearchScope.projectScope(project));
      for (DartComponentName name : dartComponentNames) {
        DartClass dartClass = PsiTreeUtil.getParentOfType(name, DartClass.class);
        if (dartClass != null && className.equals(dartClass.getName())) {
          return dartClass;
        }
      }
    }
    return null;
  }

  public void testDirectSubclass() throws Exception {
    doTest("A", "B");
  }

  public void testIndirectSubclass() throws Exception {
    doTest("A", "D");
  }

  public void testDirectImplements() throws Exception {
    doTest("A", "B");
  }

  public void testIndirectImplements() throws Exception {
    doTest("A", "D");
  }

  public void testDirectMixin() throws Exception {
    doTest("B", "C");
  }

  public void testIndirectMixin() throws Exception {
    doTest("B", "D");
  }

  public void testUndefined() throws Exception {
    doTest("Q", "Z", false);
  }
}
