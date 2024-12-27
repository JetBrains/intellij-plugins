// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public final class CucumberTableInspection extends GherkinInspection {
  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @Override
  public @NotNull String getShortName() {
    return "CucumberTableInspection";
  }

  @Override
  public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new GherkinElementVisitor() {
      @Override
      public void visitScenarioOutline(GherkinScenarioOutline outline) {
        final List<GherkinExamplesBlock> examples = outline.getExamplesBlocks();
        if (!examples.isEmpty()) {
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
    IntList unusedIndices = new IntArrayList();

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

  private static void highlightUnusedColumns(GherkinTableRow row, IntList unusedIndices, ProblemsHolder holder) {
    final List<GherkinTableCell> cells = row.getPsiCells();
    final int cellsCount = cells.size();

    for (int i : unusedIndices.toIntArray()) {
      if (i < cellsCount && cells.get(i).getTextLength() > 0) {
        holder.registerProblem(cells.get(i), CucumberBundle.message("unused.table.column"), new RemoveTableColumnFix(i));
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
