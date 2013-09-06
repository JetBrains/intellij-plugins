package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.ide.index.DartComponentInfo;
import com.jetbrains.lang.dart.util.DartImportUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by fedorkorotkov.
 */
public class DartImportFix extends BaseCreateFix {
  private final String myComponentName;
  private final DartComponentInfo myInfo;

  public DartImportFix(String name, DartComponentInfo info) {
    myComponentName = name;
    myInfo = info;
  }


  @Override
  protected void applyFix(Project project, @NotNull PsiElement psiElement, @Nullable Editor editor) {
    final String libraryId = myInfo.getLibraryId();
    if (libraryId != null) {
      DartImportUtil.insertImport(psiElement.getContainingFile(), myComponentName, libraryId);
    }
  }

  @NotNull
  @Override
  public String getName() {
    final DartComponentType type = myInfo.getType();
    assert type != null;
    return DartBundle.message("dart.import.fix.name", type.toString(), myComponentName, myInfo.getLibraryId());
  }
}
