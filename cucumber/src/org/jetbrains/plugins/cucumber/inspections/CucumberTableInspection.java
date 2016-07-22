package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.containers.IntArrayList;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yole
 */
public class CucumberTableInspection extends GherkinInspection {
  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return CucumberBundle.message("unused.or.missing.columns.in.cucumber.tables");
  }

  @NotNull
  @Override
  public String getShortName() {
    return "CucumberTableInspection";
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new GherkinElementVisitor() {
      @Override
      public void visitScenarioOutline(GherkinScenarioOutline outline) {
        final List<GherkinExamplesBlock> examples = outline.getExamplesBlocks();
        if (examples.size() > 0) {
          Collection<String> columnNames = collectUsedColumnNames(outline);
          for (GherkinExamplesBlock block : examples) {
            checkTable(block.getTable(), columnNames, holder);
          }
        }
      }
    };
  }

  private static void checkTable(GherkinTable table, Collection<String> columnNames, ProblemsHolder holder) {
    final GherkinTableRow row = table != null ? table.getHeaderRow() : null;
    if (row == null) {
      return;
    }
    final List<GherkinTableCell> cells = row.getPsiCells();
    IntArrayList unusedIndices = new IntArrayList();

    for (int i = 0, cellsSize = cells.size(); i < cellsSize; i++) {
      String columnName = cells.get(i).getText().trim();
      if (!columnNames.contains(columnName)) {
        unusedIndices.add(i);
      }
    }

    if (!unusedIndices.isEmpty()) {
      highlightUnusedColumns(row, unusedIndices, holder);
      for (GherkinTableRow tableRow : table.getDataRows()) {
        highlightUnusedColumns(tableRow, unusedIndices, holder);
      }
    }
  }

  private static void highlightUnusedColumns(GherkinTableRow row, IntArrayList unusedIndices, ProblemsHolder holder) {
    final List<GherkinTableCell> cells = row.getPsiCells();
    final int cellsCount = cells.size();

    final GherkinTable table = (GherkinTable) row.getParent();
    for (int i : unusedIndices.toArray()) {
      if (i < cellsCount && cells.get(i).getTextLength() > 0) {
        holder.registerProblem(cells.get(i), CucumberBundle.message("unused.table.column"), ProblemHighlightType.LIKE_UNUSED_SYMBOL, new RemoveTableColumnFix(table, i));
      }
    }
  }

  private static Collection<String> collectUsedColumnNames(GherkinScenarioOutline outline) {
    Set<String> result = new HashSet<>();
    for (GherkinStep step : outline.getSteps()) {
      result.addAll(step.getParamsSubstitutions());
    }
    return result;
  }
}
