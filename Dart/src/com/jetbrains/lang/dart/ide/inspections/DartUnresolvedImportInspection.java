package com.jetbrains.lang.dart.ide.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.util.ArrayUtil;
import com.intellij.util.io.URLUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartPathOrLibraryReference;
import com.jetbrains.lang.dart.psi.DartRecursiveVisitor;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DartUnresolvedImportInspection extends LocalInspectionTool {
  @NotNull
  public String getGroupDisplayName() {
    return DartBundle.message("inspections.group.name");
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return DartBundle.message("inspections.unresolved.import.name");
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  @Override
  public String getShortName() {
    return "DartUnresolvedImport";
  }

  @Nullable
  @Override
  public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
    final List<ProblemDescriptor> result = new ArrayList<ProblemDescriptor>();
    for (PsiElement root : DartResolveUtil.findDartRoots(file)) {
      root.acceptChildren(new DartRecursiveVisitor() {
        @Override
        public void visitPathOrLibraryReference(@NotNull DartPathOrLibraryReference pathOrLibraryReference) {
          String pathOrLibraryReferenceText = StringUtil.unquoteString(pathOrLibraryReference.getText());
          if (URLUtil.containsScheme(pathOrLibraryReferenceText) || !pathOrLibraryReferenceText.endsWith(".dart")) {
            return;
          }
          for (PsiReference reference : pathOrLibraryReference.getReferences()) {
            if (reference instanceof FileReference && reference.resolve() == null) {
              result.add(manager.createProblemDescriptor(
                reference.getElement(),
                reference.getRangeInElement(),
                getDisplayName(),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly,
                ((FileReference)reference).getQuickFixes()
              ));
            }
          }
        }
      });
    }
    return result.isEmpty() ? super.checkFile(file, manager, isOnTheFly) : ArrayUtil.toObjectArray(result, ProblemDescriptor.class);
  }
}
