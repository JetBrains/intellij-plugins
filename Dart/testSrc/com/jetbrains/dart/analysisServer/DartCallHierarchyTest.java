package com.jetbrains.dart.analysisServer;

import com.intellij.ide.hierarchy.HierarchyBrowserBaseEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.codeInsight.hierarchy.HierarchyViewTestBase;
import com.jetbrains.lang.dart.ide.hierarchy.call.DartCallHierarchyTreeStructure;
import com.jetbrains.lang.dart.ide.hierarchy.call.DartCalleeTreeStructure;
import com.jetbrains.lang.dart.ide.hierarchy.call.DartCallerTreeStructure;
import com.jetbrains.lang.dart.ide.index.DartClassIndex;
import com.jetbrains.lang.dart.ide.index.DartSymbolIndex;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.jetbrains.lang.dart.DartTokenTypes.CALL_EXPRESSION;

public class DartCallHierarchyTest extends HierarchyViewTestBase {

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
    return "analysisServer/callHierarchy/" + getTestName(false);
  }

  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH;
  }

  private void doCallHierarchyTest(final String className,
                                   final String methodName,
                                   final boolean caller,
                                   final String... fileNames) throws Exception {
    doHierarchyTest(() -> {
      final Project project = getProject();
      if (className != null) {
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
            if (caller) {
              return new DartCallerTreeStructure(project, member, HierarchyBrowserBaseEx.SCOPE_PROJECT);
            }
            else {
              return new DartCalleeTreeStructure(project, member, HierarchyBrowserBaseEx.SCOPE_PROJECT);
            }
          }
        }
      }
      else {
        final List<DartComponentName> dartComponentNames =
          DartSymbolIndex.getItemsByName(methodName, project, GlobalSearchScope.projectScope(project));
        for (DartComponentName name : dartComponentNames) {
          PsiElement parent = PsiTreeUtil.getParentOfType(name, DartFunctionDeclarationWithBodyOrNative.class);
          if (parent != null) {
            if (caller) {
              return new DartCallerTreeStructure(project, parent, HierarchyBrowserBaseEx.SCOPE_PROJECT);
            }
            else {
              return new DartCalleeTreeStructure(project, parent, HierarchyBrowserBaseEx.SCOPE_PROJECT);
            }
          }
          else {
            fail("Function not found");
          }
        }
      }
      return null;
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

  private static class ExitVisitor extends Error {
  }
}
