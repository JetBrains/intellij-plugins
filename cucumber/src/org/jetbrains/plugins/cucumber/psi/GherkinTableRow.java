package org.jetbrains.plugins.cucumber.psi;

import java.util.List;

/**
 * @author yole
 */
public interface GherkinTableRow extends GherkinPsiElement {
  GherkinTableRow[] EMPTY_ARRAY = new GherkinTableRow[0];

  List<GherkinTableCell> getPsiCells();

  int getColumnWidth(int columnIndex);

  void deleteCell(int columnIndex);
}
