package com.jetbrains.lang.dart.ide.hierarchy.call;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.psi.*;
import org.dartlang.analysis.server.protocol.ElementKind;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.jetbrains.lang.dart.analyzer.DartServerData.DartNavigationRegion;
import static com.jetbrains.lang.dart.analyzer.DartServerData.DartNavigationTarget;

public class DartCalleeTreeStructure extends DartCallHierarchyTreeStructure {

  public DartCalleeTreeStructure(Project project, PsiElement element, String currentScopeType) {
    super(project, element, currentScopeType);
  }

  private static void getCallees(@NotNull PsiElement element, @NotNull List<PsiElement> results) {
    DartComponentName name = (DartComponentName)element;
    DartComponent decl = (DartComponent)name.getParent();
    PsiFile file = PsiTreeUtil.getParentOfType(decl, PsiFile.class);
    if (file == null) return;
    VirtualFile vFile = file.getVirtualFile();
    List<DartNavigationRegion> navRegions =
      DartAnalysisServerService.getInstance().analysis_getNavigation(vFile, decl.getTextOffset(), decl.getTextLength());
    if (navRegions == null) return;
    resolveReferences(decl, navRegions, results);
  }

  private static void resolveReferences(@NotNull DartComponent component,
                                        @NotNull List<DartNavigationRegion> regions,
                                        @NotNull List<PsiElement> results) {
    component.acceptChildren(new DartRecursiveVisitor() {
      @Override
      public void visitReferenceExpression(@NotNull DartReferenceExpression reference) {
        int offset = reference.getTextOffset();
        List<DartNavigationTarget> targets = getRegionAt(offset, regions);
        for (DartNavigationTarget target : targets) {
          if (isExecutable(target)) {
            PsiElement element = getDeclaration(target, reference);
            if (element != null) {
              results.add(element);
            }
          }
        }
        super.visitReferenceExpression(reference);
      }
    });
  }

  private static boolean isExecutable(DartNavigationTarget target) {
    String kind = target.getKind();
    return ElementKind.METHOD.equals(kind) ||
           ElementKind.FUNCTION.equals(kind) ||
           ElementKind.GETTER.equals(kind) ||
           ElementKind.SETTER.equals(kind) ||
           ElementKind.CONSTRUCTOR.equals(kind);
  }

  private static PsiElement getDeclaration(DartNavigationTarget target, PsiElement reference) {
    String name = target.getFile();
    int offset = target.getOffset();
    VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(name);
    if (vFile != null) {
      final PsiFile targetFile = reference.getManager().findFile(vFile);
      if (targetFile != null) {
        final PsiElement[] result = new PsiElement[1];
        try {
          targetFile.accept(new DartRecursiveVisitor() {
            public void visitMethodDeclaration(@NotNull DartMethodDeclaration element) {
              if (element.getTextOffset() == offset) {
                result[0] = element;
                throw new ExitVisitor();
              }
              super.visitMethodDeclaration(element);
            }

            public void visitFunctionDeclarationWithBodyOrNative(@NotNull DartFunctionDeclarationWithBodyOrNative element) {
              if (element.getTextOffset() == offset) {
                result[0] = element;
                throw new ExitVisitor();
              }
              super.visitFunctionDeclarationWithBodyOrNative(element);
            }

            public void visitFunctionDeclarationWithBody(@NotNull DartFunctionDeclarationWithBody element) {
              if (element.getTextOffset() == offset) {
                result[0] = element;
                throw new ExitVisitor();
              }
              super.visitFunctionDeclarationWithBody(element);
            }

            public void visitSetterDeclaration(@NotNull DartSetterDeclaration element) {
              if (element.getTextOffset() == offset) {
                result[0] = element;
                throw new ExitVisitor();
              }
              super.visitSetterDeclaration(element);
            }

            public void visitGetterDeclaration(@NotNull DartGetterDeclaration element) {
              if (element.getTextOffset() == offset) {
                result[0] = element;
                throw new ExitVisitor();
              }
              super.visitGetterDeclaration(element);
            }

            public void visitFactoryConstructorDeclaration(@NotNull DartFactoryConstructorDeclaration element) {
              if (element.getTextOffset() == offset) {
                result[0] = element;
                throw new ExitVisitor();
              }
              super.visitFactoryConstructorDeclaration(element);
            }

            public void visitNamedConstructorDeclaration(@NotNull DartNamedConstructorDeclaration element) {
              if (element.getTextOffset() == offset) {
                result[0] = element;
                throw new ExitVisitor();
              }
              super.visitNamedConstructorDeclaration(element);
            }
          });
        }
        catch (ExitVisitor ex) {
          return result[0];
        }
      }
    }
    return null;
  }

  @NotNull
  private static List<DartNavigationTarget> getRegionAt(int offset, @NotNull List<DartNavigationRegion> regions) {
    for (DartNavigationRegion region : regions) {
      int targetStart = region.getOffset();
      int targetEnd = targetStart + region.getLength();
      if (offset >= targetStart && offset <= targetEnd) {
        return region.getTargets();
      }
    }
    return new ArrayList<>(0);
  }

  @NotNull
  @Override
  protected List<PsiElement> getChildren(@NotNull PsiElement element) {
    final List<PsiElement> list = new ArrayList<>();
    getCallees(element, list);
    return list;
  }

  private static class ExitVisitor extends Error {
  }
}
