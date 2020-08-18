// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinTable;
import org.jetbrains.plugins.cucumber.psi.GherkinTableRow;

/**
 * @author yole
 */
public class RemoveTableColumnFix implements LocalQuickFix {
  private final SmartPsiElementPointer<GherkinTable> myTable;
  private final int myColumnIndex;

  public RemoveTableColumnFix(@NotNull GherkinTable table, int columnIndex) {
    myTable = SmartPointerManager.createPointer(table);
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
    GherkinTable table = myTable.getElement();
    if (table == null) return;
    GherkinTableRow headerRow = table.getHeaderRow();
    if (headerRow != null) {
      headerRow.deleteCell(myColumnIndex);
    }
    for (GherkinTableRow row : table.getDataRows()) {
      row.deleteCell(myColumnIndex);
    }
  }
}
