// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.*;

import java.util.List;

public final class GherkinBrokenTableInspection extends GherkinInspection {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
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

  private static void checkTable(final @NotNull GherkinTable table, final @NotNull ProblemsHolder holder) {
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

  @Override
  public @NotNull String getShortName() {
    return "GherkinBrokenTableInspection";
  }
}
