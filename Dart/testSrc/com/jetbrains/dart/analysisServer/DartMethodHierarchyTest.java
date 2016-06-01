package com.jetbrains.dart.analysisServer;

import com.intellij.ide.hierarchy.HierarchyBrowserManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.codeInsight.hierarchy.HierarchyViewTestBase;
import com.jetbrains.lang.dart.ide.hierarchy.method.DartMethodHierarchyTreeStructure;
import com.jetbrains.lang.dart.ide.index.DartClassIndex;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.jetbrains.dart.analysisServer.DartCallHierarchyTest.findReference;

public class DartMethodHierarchyTest extends HierarchyViewTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    // Some tests do this here but resolution does not work if the server is initialized prior to copying the files.
    //DartAnalysisServerService.getInstance().serverReadyForRequest(getProject());
  }

  @Override
  protected VirtualFile configureByFiles(@Nullable final File rawProjectRoot, @NotNull final VirtualFile... vFiles) throws IOException {
    VirtualFile root = super.configureByFiles(rawProjectRoot, vFiles);
    return DartTestUtils.configureNavigation(this, root, vFiles);
  }

  @Override
  protected String getBasePath() {
    return "analysisServer/methodHierarchy/" + getTestName(false);
  }

  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH;
  }

  private void doMethodHierarchyTest(final String className,
                                     final String methodName,
                                     final boolean shouldHide,
                                     final String... fileNames) throws Exception {
    doHierarchyTest(() -> {
      final Project project = getProject();
        final List<DartComponentName> dartComponentNames =
          DartClassIndex.getItemsByName(className, project, GlobalSearchScope.projectScope(project));
        for (DartComponentName name : dartComponentNames) {
          DartClass dartClass = PsiTreeUtil.getParentOfType(name, DartClass.class);
          if (dartClass != null && className.equals(dartClass.getName())) {
            PsiElement member = dartClass.findMemberByName(methodName);
            if (member == null) {
              member = findReference(dartClass, methodName);
            }
            if (member == null) {
              fail("Method not found");
            }
            if (shouldHide) {
              HierarchyBrowserManager.State state = HierarchyBrowserManager.getInstance(myProject).getState();
              assert state != null;
              state.HIDE_CLASSES_WHERE_METHOD_NOT_IMPLEMENTED = true;
            }
            return new DartMethodHierarchyTreeStructure(project, (DartMethodDeclaration) member);
          }
        }
      return null;
    }, fileNames);
  }

  private void doStandardMethodHierarchyTest(String className, String methodName, boolean shouldHide) throws Exception {
    doMethodHierarchyTest(className, methodName, shouldHide, "../t1.dart", "../t2.dart", "../t3.dart");
  }

  public void testT1m() throws Exception {
    doStandardMethodHierarchyTest("T1", "m", false);
  }
}
