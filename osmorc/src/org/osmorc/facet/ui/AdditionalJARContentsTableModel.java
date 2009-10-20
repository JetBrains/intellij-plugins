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
import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
class AdditionalJARContentsTableModel extends AbstractTableModel
{
  AdditionalJARContentsTableModel()
  {
    _additionalContents = new ArrayList<Pair<String, String>>();
  }

  public void replaceContent(@NotNull List<Pair<String, String>> content)
  {
    _additionalContents.clear();
    _additionalContents.addAll(content);
    fireTableDataChanged();
  }

  @NotNull
  public List<Pair<String, String>> getAdditionalContents()
  {
    return new ArrayList<Pair<String, String>>(_additionalContents);
  }

  public Pair<String, String> getAdditionalJARContent(int row)
  {
    Pair<String, String> pair = _additionalContents.get(row);
    return Pair.create(pair.getFirst(), pair.getSecond());
  }

  public void changeAdditionalJARConent(int row, @NotNull String sourcePath, String destPath)
  {
    Pair<String, String> changedContent = Pair.create(sourcePath, destPath);
    _additionalContents.set(row, changedContent);
    fireTableRowsUpdated(row, row);
  }

  public void deleteAdditionalJARContent(int row)
  {
    _additionalContents.remove(row);
    fireTableRowsDeleted(row, row);
  }

  public int addAdditionalJARContent(@NotNull String sourcePath, @NotNull String destPath)
  {
    _additionalContents.add(Pair.create(sourcePath, destPath));
    int lastRow = _additionalContents.size() - 1;
    fireTableRowsInserted(lastRow, lastRow);
      return lastRow;
  }

  public int getRowCount()
  {
    return _additionalContents.size();
  }

  public int getColumnCount()
  {
    return 2;
  }

  @Override
  public String getColumnName(int column)
  {
    return column == 0
        ? "Source File/Folder"
        : "Destination File/Folder (relative to JAR root)";
  }

  @Override
  public Class<?> getColumnClass(int columnIndex)
  {
    return String.class;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return true;  //columnIndex == 1;
  }

  public Object getValueAt(int rowIndex, int columnIndex)
  {
    Pair<String, String> row = _additionalContents.get(rowIndex);
    return columnIndex == 0 ? row.getFirst() : row.getSecond();
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    Pair<String, String> row = _additionalContents.get(rowIndex);
    Pair<String, String> updatedRow = Pair.create(
        columnIndex == 0 ? (String) aValue : row.getFirst(),
        columnIndex == 1 ? (String) aValue : row.getSecond());
    _additionalContents.set(rowIndex, updatedRow);
    fireTableCellUpdated(rowIndex, columnIndex);
  }

  private List<Pair<String, String>> _additionalContents;
}
