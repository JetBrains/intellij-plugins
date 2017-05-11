package org.jetbrains.plugins.cucumber.codeinsight;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;
import org.jetbrains.plugins.cucumber.psi.GherkinTable;
import org.jetbrains.plugins.cucumber.psi.GherkinTableRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GherkinTypedHandler extends TypedHandlerDelegate {

  public static final char PIPE = '|';

  /**
   * Looks for Example table where the caret is located
   * @param element to start looking for
   * @return GherkinTable or null
   */
  @Nullable
  private static GherkinTable getTable(@NotNull PsiElement element) {
    //noinspection ConstantConditions
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
   * @param editor
   * @param file
   * @return GherkinTableRow if caret is inside row, null otherwise
   */
  @Nullable
  private static GherkinTableRow findCurrentRow(@NotNull final Editor editor, @NotNull final PsiFile file) {
    int offset = editor.getCaretModel().getOffset();
    PsiElement cursorElement = file.findElementAt(offset - 1);
    if (cursorElement == null) {
      return null;
    }

    PsiElement element = getTable(cursorElement);
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

    final GherkinTable table = (GherkinTable)element;
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
    return rowList.get(rowList.size() - 1);
  }

  /**
   * Calculates GherkinTableRow above currentRow
   * @param currentRow row to start
   * @return GherkinTableRow (or header) if there is a row above or null otherwise
   */
  @Nullable
  private static GherkinTableRow getPreviousRow(@NotNull final GherkinTableRow currentRow) {
    if (currentRow.getParent() != null && currentRow.getParent() instanceof GherkinTable) {
      final GherkinTable table = (GherkinTable)currentRow.getParent();
      int i = table.getDataRows().indexOf(currentRow);
      if (i > 0) {
        return table.getDataRows().get(i - 1);
      } else if (i == 0) {
        return table.getHeaderRow();
      }
    }
    return null;
  }

  /**
   * Calculates number of column where the caret is located
   * @param row where to look for
   * @param caretPosition
   * @return number of column
   */
  private static int getColumnNumber(@NotNull final GherkinTableRow row, final int caretPosition) {
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
    final StringBuilder sb = new StringBuilder(n);
    sb.append(spaces);
    return sb.toString();
  }

  /**
   * Calculates where next Pipe symbol should be
   * @param row a row above current
   * @param columnNumber
   * @return offset in parent that corresponds preferred position of typed pipe symbol
   */
  private static int getPreferredPipeOffset(@NotNull final GherkinTableRow row, final int columnNumber) {
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
              EditorModificationUtil.insertStringAtCaret(editor, getSpaceLine(rightPosition - offsetInParent - 1));
            }
          }
        }
      }
    }

    return super.beforeCharTyped(c, project, editor, file, fileType);
  }
}
