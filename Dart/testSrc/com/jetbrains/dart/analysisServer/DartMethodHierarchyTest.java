// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.dart.analysisServer;

import com.intellij.ide.hierarchy.HierarchyBrowserManager;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FindSymbolParameters;
import com.jetbrains.lang.dart.ide.DartClassContributor;
import com.jetbrains.lang.dart.ide.hierarchy.method.DartMethodHierarchyTreeStructure;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;

import static com.jetbrains.dart.analysisServer.DartCallHierarchyTest.findReference;

public class DartMethodHierarchyTest extends DartHierarchyTestBase {
  @Override
  protected String getBasePath() {
    return "/analysisServer/methodHierarchy";
  }

  private void doMethodHierarchyTest(final String className,
                                     final String methodName,
                                     final boolean shouldHide,
                                     final String... fileNames) throws Exception {
    doHierarchyTest(() -> {
      Project project = getProject();
      Ref<HierarchyTreeStructure> result = Ref.create();
      new DartClassContributor().processElementsWithName(className, item -> {
        DartClass dartClass = PsiTreeUtil.getParentOfType((PsiElement)item, DartClass.class);
        if (dartClass != null && className.equals(dartClass.getName())) {
          PsiElement member = dartClass.findMemberByName(methodName);
          if (member == null) {
            member = findReference(dartClass, methodName);
          }
          if (member == null) {
            fail("Method not found");
          }
          if (shouldHide) {
            HierarchyBrowserManager.State state = HierarchyBrowserManager.getInstance(project).getState();
            assert state != null;
            state.HIDE_CLASSES_WHERE_METHOD_NOT_IMPLEMENTED = true;
          }
          result.set(new DartMethodHierarchyTreeStructure(project, (DartComponent)member));
          return false;
        }
        return true;
      }, FindSymbolParameters.wrap("", GlobalSearchScope.projectScope(project)));
      return result.get();
    }, fileNames);
  }

  private void doStandardMethodHierarchyTest(String className, String methodName, boolean shouldHide) throws Exception {
    doMethodHierarchyTest(className, methodName, shouldHide, "t1.dart", "t2.dart", "t3.dart");
  }

  public void testT1m() throws Exception {
    // If icons were in the XML then T1 would have base, all others '+' or '-'
    doStandardMethodHierarchyTest("T1", "m", false);
  }

  public void testT2m() throws Exception {
    // If icons were in the XML then C2x would have a '!'
    doStandardMethodHierarchyTest("T2", "m", false);
  }

  public void testC1m() throws Exception {
    // Class Z1 is not displayed
    doStandardMethodHierarchyTest("C1", "m", true);
  }

  public void testM1m() throws Exception {
    // Check mixin
    doStandardMethodHierarchyTest("M1", "m", true);
  }
}
