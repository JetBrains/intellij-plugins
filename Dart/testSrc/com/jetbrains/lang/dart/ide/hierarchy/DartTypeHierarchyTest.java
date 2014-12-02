package com.jetbrains.lang.dart.ide.hierarchy;

import com.intellij.ide.hierarchy.HierarchyBrowserBaseEx;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.codeInsight.hierarchy.HierarchyViewTestBase;
import com.jetbrains.lang.dart.ide.hierarchy.type.DartSupertypesHierarchyTreeStructure;
import com.jetbrains.lang.dart.ide.hierarchy.type.DartTypeHierarchyTreeStructure;
import com.jetbrains.lang.dart.ide.index.DartClassIndex;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.util.DartTestUtils;

import java.util.List;

public class DartTypeHierarchyTest extends HierarchyViewTestBase {
  @Override
  protected String getBasePath() {
    return "hierarchy/type/" + getTestName(false);
  }

  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH;
  }

  private void doDartTypeHierarchyTest(final String className, final boolean subtype, final String... fileNames) throws Exception {
    doHierarchyTest(new Computable<HierarchyTreeStructure>() {
      @Override
      public HierarchyTreeStructure compute() {
        final Project project = getProject();
        final List<DartComponentName> dartComponentNames =
          DartClassIndex.getItemsByName(className, project, GlobalSearchScope.projectScope(project));
        for (DartComponentName name : dartComponentNames) {
          DartClass dartClass = PsiTreeUtil.getParentOfType(name, DartClass.class);
          if (dartClass != null && dartClass.getName().equals(className)) {
            if (subtype) {
              return new DartTypeHierarchyTreeStructure(project, dartClass, HierarchyBrowserBaseEx.SCOPE_PROJECT);
            }
            else {
              return new DartSupertypesHierarchyTreeStructure(project, dartClass);
            }
          }
        }
        return null;
      }
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
