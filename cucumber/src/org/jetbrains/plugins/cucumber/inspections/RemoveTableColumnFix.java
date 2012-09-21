package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinTable;
import org.jetbrains.plugins.cucumber.psi.GherkinTableRow;

/**
 * @author yole
 */
public class RemoveTableColumnFix implements LocalQuickFix {
  private final GherkinTable myTable;
  private final int myColumnIndex;

  public RemoveTableColumnFix(GherkinTable table, int columnIndex) {
    myTable = table;
    myColumnIndex = columnIndex;
  }

  @NotNull
  public String getName() {
    return "Remove unused column";
  }

  @NotNull
  public String getFamilyName() {
    return "RemoveTableColumnFix";
  }

  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    final GherkinTableRow headerRow = myTable.getHeaderRow();
    if (headerRow != null) {
      headerRow.deleteCell(myColumnIndex);
    }
    for (GherkinTableRow row : myTable.getDataRows()) {
      row.deleteCell(myColumnIndex);
    }
  }
}
