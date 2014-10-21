package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.util.DartImportUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartImportFix extends BaseCreateFix {
  private final @NotNull String myUrlToImport;
  private final @NotNull String myComponentName;

  public DartImportFix(@NotNull final String urlToImport, @NotNull final String componentName) {
    myUrlToImport = urlToImport;
    myComponentName = componentName;
  }

  @Override
  protected void applyFix(Project project, @NotNull PsiElement psiElement, @Nullable Editor editor) {
    DartImportUtil.insertImport(psiElement.getContainingFile(), myComponentName, myUrlToImport);
  }

  @NotNull
  @Override
  public String getName() {
    return DartBundle.message("import.0", myUrlToImport);
  }
}
