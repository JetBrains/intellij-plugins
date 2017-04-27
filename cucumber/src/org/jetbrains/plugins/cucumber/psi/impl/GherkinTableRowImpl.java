package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinTableCell;
import org.jetbrains.plugins.cucumber.psi.GherkinTableRow;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author yole
 */
public class GherkinTableRowImpl extends GherkinPsiElementBase implements GherkinTableRow {
  public GherkinTableRowImpl(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitTableRow(this);
  }

  @Override
  public String toString() {
    return "GherkinTableRow";
  }

  // ToDo: Andrey Vokin, remove code duplication
  @NotNull
  public static <T extends PsiElement> List<T> getChildrenByFilter(final PsiElement psiElement, final Class<T> c) {
    LinkedList<T> list = new LinkedList<>();
    for (PsiElement element : psiElement.getChildren()) {
      if (c.isInstance(element)) {
        //noinspection unchecked
        list.add((T)element);
      }
    }

    return list.isEmpty() ? Collections.emptyList() : list;
  }

  @NotNull
  public List<GherkinTableCell> getPsiCells() {

    return getChildrenByFilter(this, GherkinTableCell.class);
  }

  public int getColumnWidth(int columnIndex) {
    final List<GherkinTableCell> cells = getPsiCells();
    if (cells.size() <= columnIndex) {
      return 0;
    }

    final PsiElement cell = cells.get(columnIndex);
    if (cell != null && cell.getText() != null) {
      return cell.getText().trim().length();
    }
    return 0;
  }

  public void deleteCell(int columnIndex) {
    final List<GherkinTableCell> cells = getPsiCells();
    if (columnIndex < cells.size()) {
      PsiElement cell = cells.get(columnIndex);
      PsiElement nextPipe = cell.getNextSibling();
      if (nextPipe instanceof PsiWhiteSpace) {
        nextPipe = nextPipe.getNextSibling();
      }
      if (nextPipe != null && nextPipe.getNode().getElementType() == GherkinTokenTypes.PIPE) {
        nextPipe.delete();
      }
      cell.delete();
    }
  }
}
