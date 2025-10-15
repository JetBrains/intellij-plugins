// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.codeinsight;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtilEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;
import org.jetbrains.plugins.cucumber.psi.GherkinTable;
import org.jetbrains.plugins.cucumber.psi.GherkinTableRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NotNullByDefault
public final class GherkinTypedHandler extends TypedHandlerDelegate {

  public static final char PIPE = '|';

  /**
   * Looks for {@code Example} table where the caret is located
   *
   * @param element to start looking for
   * @return GherkinTable or null
   */
  private static @Nullable GherkinTable getTable(PsiElement element) {
    while (element != null) {
      if (element instanceof GherkinTable) {
        break;
      }
      element = element.getParent();
    }
    return (GherkinTable)element;
  }

  /**
   * Looks for GherkinTableRow where the caret is located
   *
   * @return GherkinTableRow if caret is inside row, null otherwise
   */
  private static @Nullable GherkinTableRow findCurrentRow(Editor editor, PsiFile file) {
    int offset = editor.getCaretModel().getOffset();
    PsiElement cursorElement = file.findElementAt(offset - 1);
    if (cursorElement == null) {
      return null;
    }

    GherkinTable element = getTable(cursorElement);
    if (element == null) {
      cursorElement = file.findElementAt(offset - cursorElement.getTextLength());
      if (cursorElement == null) {
        return null;
      }
      element = getTable(cursorElement);
      if (element == null) {
        return null;
      }
    }

    final GherkinTable table = element;
    final int tableOffset = table.getTextOffset();
    final int caretOffsetInParent = editor.getCaretModel().getOffset() - tableOffset;

    final List<GherkinTableRow> rowList = new ArrayList<>();
    if (table.getHeaderRow() != null) {
      rowList.add(table.getHeaderRow());
    }
    rowList.addAll(table.getDataRows());

    for (int i = 0; i < rowList.size() - 1; i++) {
      final GherkinTableRow row = rowList.get(i);
      final GherkinTableRow nextRow = rowList.get(i + 1);

      final int start = row.getStartOffsetInParent();
      final int end = start + row.getTextLength();

      if (start <= caretOffsetInParent && caretOffsetInParent <= end) {
        return row;
      }
      if (end < caretOffsetInParent && caretOffsetInParent <= nextRow.getStartOffsetInParent()) {
        return row;
      }
    }
    return rowList.getLast();
  }

  /**
   * Calculates GherkinTableRow above currentRow
   *
   * @param currentRow row to start
   * @return GherkinTableRow (or header) if there is a row above or null otherwise
   */
  private static @Nullable GherkinTableRow getPreviousRow(GherkinTableRow currentRow) {
    if (currentRow.getParent() != null && currentRow.getParent() instanceof GherkinTable table) {
      int i = table.getDataRows().indexOf(currentRow);
      if (i > 0) {
        return table.getDataRows().get(i - 1);
      }
      else if (i == 0) {
        return table.getHeaderRow();
      }
    }
    return null;
  }

  /**
   * Calculates number of the column where the caret is located
   *
   * @param row where to look for
   * @return number of the column
   */
  private static int getColumnNumber(GherkinTableRow row, int caretPosition) {
    final String rowText = row.getText();
    final int length = Math.min(caretPosition, rowText.length());
    int count = 0;
    for (int i = 0; i < length; i++) {
      if (rowText.charAt(i) == PIPE) {
        count++;
      }
    }
    return count - 1;
  }

  private static String getSpaceLine(int n) {
    char[] spaces = new char[n];
    Arrays.fill(spaces, ' ');
    return String.valueOf(spaces);
  }

  /**
   * Calculates where the next Pipe symbol should be
   *
   * @param row a row above current
   * @return offset in parent that corresponds preferred position of typed pipe symbol
   */
  private static int getPreferredPipeOffset(GherkinTableRow row, int columnNumber) {
    final String rowText = row.getText();
    int i = 0;
    int passedPipeCount = 0;
    while (i < rowText.length() && passedPipeCount - 2 < columnNumber) {
      if (rowText.charAt(i) == '|') {
        passedPipeCount++;
      }
      i++;
    }
    return (passedPipeCount - 2 == columnNumber) ? i : -1;
  }

  @Override
  public Result beforeCharTyped(char c,
                                Project project,
                                Editor editor,
                                PsiFile file,
                                FileType fileType) {
    if (fileType.equals(GherkinFileType.INSTANCE)) {
      if (c == PIPE) {
        final GherkinTableRow currentRow = findCurrentRow(editor, file);
        if (currentRow != null) {
          final GherkinTableRow previousRow = getPreviousRow(currentRow);
          if (previousRow != null) {
            final int offsetInParent = editor.getCaretModel().getOffset() - currentRow.getTextOffset();
            final int cellNumber = getColumnNumber(currentRow, offsetInParent);
            int rightPosition = getPreferredPipeOffset(previousRow, cellNumber);
            if (offsetInParent < rightPosition - 1) {
              EditorModificationUtilEx.insertStringAtCaret(editor, getSpaceLine(rightPosition - offsetInParent - 1));
            }
          }
        }
      }
    }

    return super.beforeCharTyped(c, project, editor, file, fileType);
  }
}
