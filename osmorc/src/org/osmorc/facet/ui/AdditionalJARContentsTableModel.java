/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.facet.ui;

import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
@ApiStatus.Internal
public final class AdditionalJARContentsTableModel extends AbstractTableModel {
  private final List<Pair<String, String>> myAdditionalContents;

  @VisibleForTesting
  public AdditionalJARContentsTableModel() {
    myAdditionalContents = new ArrayList<>();
  }

  public void replaceContent(@NotNull List<Pair<String, String>> content) {
    myAdditionalContents.clear();
    myAdditionalContents.addAll(content);
    fireTableDataChanged();
  }

  public @NotNull List<Pair<String, String>> getAdditionalContents() {
    return new ArrayList<>(myAdditionalContents);
  }

  public Pair<String, String> getAdditionalJARContent(int row) {
    Pair<String, String> pair = myAdditionalContents.get(row);
    return Pair.create(pair.getFirst(), pair.getSecond());
  }

  public void changeAdditionalJARContent(int row, @NotNull String sourcePath, String destPath) {
    Pair<String, String> changedContent = Pair.create(sourcePath, destPath);
    myAdditionalContents.set(row, changedContent);
    fireTableRowsUpdated(row, row);
  }

  public void deleteAdditionalJARContent(int row) {
    myAdditionalContents.remove(row);
    fireTableRowsDeleted(row, row);
  }

  public int addAdditionalJARContent(@NotNull String sourcePath, @NotNull String destPath) {
    myAdditionalContents.add(Pair.create(sourcePath, destPath));
    int lastRow = myAdditionalContents.size() - 1;
    fireTableRowsInserted(lastRow, lastRow);
    return lastRow;
  }

  @Override
  public int getRowCount() {
    return myAdditionalContents.size();
  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public String getColumnName(int column) {
    return OsmorcBundle.message(column == 0 ? "facet.editor.jar.src" : "facet.editor.jar.dst");
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return String.class;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return true;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Pair<String, String> row = myAdditionalContents.get(rowIndex);
    return columnIndex == 0 ? row.getFirst() : row.getSecond();
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    Pair<String, String> row = myAdditionalContents.get(rowIndex);
    Pair<String, String> updatedRow = Pair.create(
      columnIndex == 0 ? (String)aValue : row.getFirst(),
      columnIndex == 1 ? (String)aValue : row.getSecond());
    myAdditionalContents.set(rowIndex, updatedRow);
    fireTableCellUpdated(rowIndex, columnIndex);
  }
}
