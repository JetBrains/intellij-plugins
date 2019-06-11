// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.dart.analysisServer;

import com.intellij.ide.hierarchy.HierarchyBrowserBaseEx;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FindSymbolParameters;
import com.jetbrains.lang.dart.ide.DartClassContributor;
import com.jetbrains.lang.dart.ide.hierarchy.type.DartServerSupertypesHierarchyTreeStructure;
import com.jetbrains.lang.dart.ide.hierarchy.type.DartServerTypeHierarchyTreeStructure;
import com.jetbrains.lang.dart.psi.DartClass;

public class DartTypeHierarchyTest extends DartHierarchyTestBase {
  @Override
  protected String getBasePath() {
    return "/analysisServer/typeHierarchy/" + getTestName(false);
  }

  private void doDartTypeHierarchyTest(final String className, final boolean subtype, final String... fileNames) throws Exception {
    doHierarchyTest(() -> {
      Project project = getProject();
      Ref<HierarchyTreeStructure> result = Ref.create();
      new DartClassContributor().processElementsWithName(className, item -> {
        DartClass dartClass = PsiTreeUtil.getParentOfType((PsiElement)item, DartClass.class);
        if (dartClass != null && dartClass.getName().equals(className)) {
          if (subtype) {
            result.set(new DartServerTypeHierarchyTreeStructure(project, dartClass, HierarchyBrowserBaseEx.SCOPE_PROJECT));
          }
          else {
            result.set(new DartServerSupertypesHierarchyTreeStructure(project, dartClass));
          }
          return false;
        }
        return true;
      }, FindSymbolParameters.wrap("", GlobalSearchScope.projectScope(project)));
      return result.get();
    }, fileNames);
  }

  public void testPartOfExtends() throws Exception {
    doDartTypeHierarchyTest("B", true, getTestName(false) + ".dart", "SomePart.dart");
  }

  public void testSubtypeExtends() throws Exception {
    doDartTypeHierarchyTest("B", true, getTestName(false) + ".dart");
  }

  public void testSubtypeImplements() throws Exception {
    doDartTypeHierarchyTest("B", true, getTestName(false) + ".dart");
  }

  public void testSubtypeMixins() throws Exception {
    doDartTypeHierarchyTest("B", true, getTestName(false) + ".dart");
  }

  public void testSupertypeExtends() throws Exception {
    doDartTypeHierarchyTest("D", false, getTestName(false) + ".dart");
  }

  public void testSupertypeImplements() throws Exception {
    doDartTypeHierarchyTest("E", false, getTestName(false) + ".dart");
  }

  public void testSupertypeMixins() throws Exception {
    doDartTypeHierarchyTest("B", false, getTestName(false) + ".dart");
  }

  public void testRedSuperClass() throws Exception {
    doDartTypeHierarchyTest("B", true, getTestName(false) + ".dart");
  }
}
