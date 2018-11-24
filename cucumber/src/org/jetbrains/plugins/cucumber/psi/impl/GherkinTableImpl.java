package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinElementTypes;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinTable;
import org.jetbrains.plugins.cucumber.psi.GherkinTableRow;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */
public class GherkinTableImpl extends GherkinPsiElementBase implements GherkinTable {
  private static final TokenSet HEADER_ROW_TOKEN_SET = TokenSet.create(GherkinElementTypes.TABLE_HEADER_ROW);
  private static final TokenSet ROW_TOKEN_SET = TokenSet.create(GherkinElementTypes.TABLE_ROW);

  public GherkinTableImpl(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitTable(this);
  }

  @Nullable
  public GherkinTableRow getHeaderRow() {
    final ASTNode node = getNode();

    final ASTNode tableNode = node.findChildByType(HEADER_ROW_TOKEN_SET);
    return tableNode == null ? null : (GherkinTableRow)tableNode.getPsi();
  }

  public List<GherkinTableRow> getDataRows() {
    List<GherkinTableRow> result = new ArrayList<>();
    final GherkinTableRow[] rows = PsiTreeUtil.getChildrenOfType(this, GherkinTableRow.class);
    if (rows != null) {
      for (GherkinTableRow row : rows) {
        if (!(row instanceof GherkinTableHeaderRowImpl)) {
          result.add(row);
        }
      }
    }
    return result;
  }

  public int getColumnWidth(int columnIndex) {
    int result = 0;
    final GherkinTableRow headerRow = getHeaderRow();
    if (headerRow != null) {
      result = headerRow.getColumnWidth(columnIndex);
    }
    for (GherkinTableRow row : getDataRows()) {
      result = Math.max(result, row.getColumnWidth(columnIndex));
    }
    return result;
  }

  @Override
  public String toString() {
    return "GherkinTable";
  }
}
