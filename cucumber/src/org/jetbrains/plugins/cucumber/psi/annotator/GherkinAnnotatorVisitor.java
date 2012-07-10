package org.jetbrains.plugins.cucumber.psi.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.impl.*;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinHighlighter;
import org.jetbrains.plugins.cucumber.psi.GherkinTableCell;

import java.util.List;

/**
 * @author Roman.Chernyatchik
 */
public class GherkinAnnotatorVisitor extends GherkinElementVisitor {
  private final AnnotationHolder myHolder;

  public GherkinAnnotatorVisitor(@NotNull final AnnotationHolder holder) {
    myHolder = holder;
  }

  private void highlight(final PsiElement element, final TextAttributesKey colorKey) {
    myHolder.createInfoAnnotation(element, null).setTextAttributes(colorKey);
  }

  @Override
  public void visitElement(final PsiElement element) {
    ProgressManager.checkCanceled();

    super.visitElement(element);
  }

  @Override
  public void visitTableHeaderRow(GherkinTableHeaderRowImpl row) {
    super.visitTableRow(row);

    ProgressManager.checkCanceled();

    final GherkinTableImpl table = GherkinTableNavigator.getTableByRow(row);
    final GherkinExamplesBlockImpl examplesSection = table != null
                                                     ? GherkinExamplesNavigator.getExamplesByTable(table)
                                                     : null;
    if (examplesSection == null) {
      // do noting if table isn't in Examples section
      return;
    }
    final List<GherkinTableCell> cells = row.getPsiCells();
    for (PsiElement cell : cells) {
      highlight(cell, GherkinHighlighter.TABLE_HEADER_CELL);
    }
  }
}
