package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.*;

import java.util.List;

public class GherkinBrokenTableInspection extends GherkinInspection {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new GherkinElementVisitor() {
      @Override
      public void visitScenarioOutline(GherkinScenarioOutline outline) {
        final List<GherkinExamplesBlock> examples = outline.getExamplesBlocks();
        for (GherkinExamplesBlock block : examples) {
          if (block.getTable() != null) {
            checkTable(block.getTable(), holder);
          }
        }
      }

      @Override
      public void visitStep(GherkinStep step) {
        final GherkinTable table = PsiTreeUtil.getChildOfType(step, GherkinTable.class);
        if (table != null) {
          checkTable(table, holder);
        }
      }
    };
  }

  private static void checkTable(@NotNull final GherkinTable table, @NotNull final ProblemsHolder holder) {
    GherkinTableRow header = table.getHeaderRow();
    for (GherkinTableRow row : table.getDataRows()) {
      if (header == null) {
        header = row;
      }
      if (row.getPsiCells().size() != header.getPsiCells().size()) {
        holder.registerProblem(row, CucumberBundle.message("inspection.gherkin.table.is.broken.row.error.message"));
      }
    }
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return CucumberBundle.message("inspection.gherkin.table.is.broken.name");
  }

  @NotNull
  @Override
  public String getShortName() {
    return "GherkinBrokenTableInspection";
  }
}
