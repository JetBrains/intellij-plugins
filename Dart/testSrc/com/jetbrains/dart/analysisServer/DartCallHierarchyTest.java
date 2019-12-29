// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.dart.analysisServer;

import com.intellij.ide.hierarchy.HierarchyBrowserBaseEx;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FindSymbolParameters;
import com.jetbrains.lang.dart.ide.DartClassContributor;
import com.jetbrains.lang.dart.ide.DartSymbolContributor;
import com.jetbrains.lang.dart.ide.hierarchy.call.DartCallHierarchyTreeStructure;
import com.jetbrains.lang.dart.ide.hierarchy.call.DartCalleeTreeStructure;
import com.jetbrains.lang.dart.ide.hierarchy.call.DartCallerTreeStructure;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBodyOrNative;
import com.jetbrains.lang.dart.psi.DartRecursiveVisitor;
import com.jetbrains.lang.dart.psi.DartReferenceExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.jetbrains.lang.dart.DartTokenTypes.CALL_EXPRESSION;

public class DartCallHierarchyTest extends DartHierarchyTestBase {
  @Override
  protected String getBasePath() {
    return "/analysisServer/callHierarchy/" + getTestName(false);
  }

  private void doCallHierarchyTest(final String className,
                                   final String methodName,
                                   final boolean caller,
                                   final String... fileNames) throws Exception {
    doHierarchyTest(() -> {
      Project project = getProject();
      Ref<HierarchyTreeStructure> result = Ref.create();
      if (className != null) {
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
            if (caller) {
              result.set(new DartCallerTreeStructure(project, member, HierarchyBrowserBaseEx.getSCOPE_PROJECT()));
            }
            else {
              result.set(new DartCalleeTreeStructure(project, member, HierarchyBrowserBaseEx.getSCOPE_PROJECT()));
            }
            return false;
          }
          return true;
        }, FindSymbolParameters.wrap("", GlobalSearchScope.projectScope(project)));
      }
      else {
        new DartSymbolContributor().processElementsWithName(methodName, item -> {
          PsiElement parent = PsiTreeUtil.getParentOfType((PsiElement)item, DartFunctionDeclarationWithBodyOrNative.class);
          if (parent != null) {
            if (caller) {
              result.set(new DartCallerTreeStructure(project, parent, HierarchyBrowserBaseEx.getSCOPE_PROJECT()));
            }
            else {
              result.set(new DartCalleeTreeStructure(project, parent, HierarchyBrowserBaseEx.getSCOPE_PROJECT()));
            }
            return false;
          }
          else {
            fail("Function not found");
          }
          return true;
        }, FindSymbolParameters.wrap("", GlobalSearchScope.projectScope(project)));
      }
      return result.get();
    }, fileNames);
  }

  public static PsiElement findReference(PsiElement dartClass, String referenceName) {
    // Find any reference to the named function or method in the test code.
    PsiElement[] result = new PsiElement[1];
    try {
      dartClass.acceptChildren(new DartRecursiveVisitor() {
        @Override
        public void visitReferenceExpression(@NotNull DartReferenceExpression reference) {
          if (referenceName.equals(reference.getText())) {
            if (reference.getNextSibling() == null) {
              PsiElement parent = reference.getParent();
              if (parent != null) {
                IElementType type = parent.getNode().getElementType();
                if (type == CALL_EXPRESSION) {
                  result[0] = reference;
                  throw new ExitVisitor();
                }
                parent = parent.getParent();
                if (parent != null) {
                  type = parent.getNode().getElementType();
                  if (type == CALL_EXPRESSION) {
                    List<PsiElement> results = new ArrayList<>();
                    DartCallHierarchyTreeStructure.collectDeclarations(reference.resolve(), results);
                    if (!results.isEmpty()) {
                      result[0] = results.get(0);
                      throw new ExitVisitor();
                    }
                  }
                }
              }
            }
          }
          super.visitReferenceExpression(reference);
        }
      });
    }
    catch (ExitVisitor ex) {
      return result[0]; // not null
    }
    return null;
  }

  public void testMethodCallers() throws Exception {
    doCallHierarchyTest("B", "b", true, "B.dart", "A.dart");
  }

  public void testMethodCallees() throws Exception {
    doCallHierarchyTest("B", "b", false, "B.dart", "C.dart");
  }

  public void testMethodRefCallers() throws Exception {
    doCallHierarchyTest("B", "c", true, "B.dart", "A.dart", "C.dart");
  }

  public void testConstructorCallers() throws Exception {
    doCallHierarchyTest("B", "b", true, "B.dart", "A.dart");
  }

  public void testConstructorCallees() throws Exception {
    doCallHierarchyTest("B", "b", false, "B.dart", "A.dart", "C.dart");
  }

  public void testFunctionCallers() throws Exception {
    doCallHierarchyTest(null, "b", true, "B.dart", "A.dart");
  }

  public void testFunctionCallees() throws Exception {
    doCallHierarchyTest(null, "a", false, "A.dart", "C.dart", "B.dart");
  }

  public void testFunctionRefCallers() throws Exception {
    doCallHierarchyTest(null, "c", true, "B.dart", "A.dart", "C.dart");
  }

  public void testMultiCallers() throws Exception {
    doCallHierarchyTest("A", "c", true, "A.dart", "B.dart", "C.dart");
  }

  public void testMultiCallees() throws Exception {
    doCallHierarchyTest("A", "a", false, "A.dart", "B.dart", "C.dart");
  }

  public void testLocalFnInMethod() throws Exception {
    doCallHierarchyTest(null, "baz", true, "C.dart");
  }

  public void testLocalFnInFunction() throws Exception {
    doCallHierarchyTest(null, "baz", true, "C.dart");
  }

  public void testNamedConstructorCallers() throws Exception {
    doCallHierarchyTest("X", "z7", true, "A.dart");
  }

  public void testGetterSetterCallers() throws Exception {
    doCallHierarchyTest("X", "it", true, "A.dart");
  }

  public void testFactoryConstructorCallers() throws Exception {
    doCallHierarchyTest("X", "zz", true, "A.dart");
  }

  private static class ExitVisitor extends Error {
  }
}
