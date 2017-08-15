package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yole
 */
public class GherkinScenarioOutlineImpl extends GherkinStepsHolderBase implements GherkinScenarioOutline {
  private static final TokenSet EXAMPLES_BLOCK_FILTER = TokenSet.create(GherkinElementTypes.EXAMPLES_BLOCK);

  private Ref<Map<String, String>> myOutlineTableMap;

  public GherkinScenarioOutlineImpl(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "GherkinScenarioOutline:" + getElementText();
  }

  @Override
  protected String getPresentableText() {
    return buildPresentableText("Scenario Outline");
  }

  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitScenarioOutline(this);
  }

  @NotNull
  public List<GherkinExamplesBlock> getExamplesBlocks() {
    List<GherkinExamplesBlock> result = new ArrayList<>();
    final ASTNode[] nodes = getNode().getChildren(EXAMPLES_BLOCK_FILTER);
    for (ASTNode node : nodes) {
      result.add((GherkinExamplesBlock) node.getPsi());
    }
    return result;
  }

  @Nullable
  public Map<String, String> getOutlineTableMap() {
    if (myOutlineTableMap == null) {
      myOutlineTableMap = new Ref<>(buildOutlineTableMap());
    }
    return myOutlineTableMap.get();
  }

  @Nullable
  private Map<String, String> buildOutlineTableMap() {
    final List<GherkinExamplesBlock> examplesBlocks = getExamplesBlocks();
    for (GherkinExamplesBlock examplesBlock : examplesBlocks) {
      GherkinTable table = examplesBlock.getTable();
      if (table == null || table.getHeaderRow() == null || table.getDataRows().size() == 0) {
        continue;
      }
      List<GherkinTableCell> headerCells = table.getHeaderRow().getPsiCells();
      List<GherkinTableCell> dataCells = table.getDataRows().get(0).getPsiCells();

      Map<String, String> result = new HashMap<>();
      for (int i = 0; i < headerCells.size(); i++) {
        if (i >= dataCells.size()) {
          break;
        }
        result.put(headerCells.get(i).getText().trim(), dataCells.get(i).getText().trim());
      }
      return result;
    }
    return null;
  }
}
