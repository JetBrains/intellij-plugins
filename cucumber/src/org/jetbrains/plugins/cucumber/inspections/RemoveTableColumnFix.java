// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinTable;
import org.jetbrains.plugins.cucumber.psi.GherkinTableRow;


public class RemoveTableColumnFix implements LocalQuickFix {
  private final int myColumnIndex;

  public RemoveTableColumnFix(int columnIndex) {
    myColumnIndex = columnIndex;
  }

  @Override
  @NotNull
  public String getName() {
    return CucumberBundle.message("intention.name.remove.unused.column");
  }

  @Override
  @NotNull
  public String getFamilyName() {
    return "RemoveTableColumnFix";
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement element = descriptor.getPsiElement();
    GherkinTable table = PsiTreeUtil.getParentOfType(element, GherkinTable.class);
    if (table == null) {
      return;
    }
    GherkinTableRow headerRow = table.getHeaderRow();
    if (headerRow != null) {
      headerRow.deleteCell(myColumnIndex);
    }
    for (GherkinTableRow row : table.getDataRows()) {
      row.deleteCell(myColumnIndex);
    }
  }
}
