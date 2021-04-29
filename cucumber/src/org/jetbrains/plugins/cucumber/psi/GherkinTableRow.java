// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi;

import java.util.List;


public interface GherkinTableRow extends GherkinPsiElement {
  GherkinTableRow[] EMPTY_ARRAY = new GherkinTableRow[0];

  List<GherkinTableCell> getPsiCells();

  int getColumnWidth(int columnIndex);

  void deleteCell(int columnIndex);
}
