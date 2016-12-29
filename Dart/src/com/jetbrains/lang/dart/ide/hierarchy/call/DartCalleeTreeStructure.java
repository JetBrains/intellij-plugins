package com.jetbrains.lang.dart.ide.hierarchy.call;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartRecursiveVisitor;
import com.jetbrains.lang.dart.psi.DartReferenceExpression;
import com.jetbrains.lang.dart.resolve.DartResolver;
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
    PsiFile file = decl.getContainingFile();
    if (file == null) return;
    VirtualFile vFile = file.getVirtualFile();
    List<DartNavigationRegion> navRegions =
      DartAnalysisServerService.getInstance(element.getProject()).analysis_getNavigation(vFile, decl.getTextOffset(), decl.getTextLength());
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
    PsiElement found = DartResolver.getElementForNavigationTarget(reference.getProject(), target);
    return found == null ? null : found.getParent();
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
}
