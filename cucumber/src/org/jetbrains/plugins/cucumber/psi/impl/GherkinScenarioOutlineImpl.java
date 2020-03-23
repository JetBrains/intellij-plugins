package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GherkinScenarioOutlineImpl extends GherkinStepsHolderBase implements GherkinScenarioOutline {
  private static final TokenSet EXAMPLES_BLOCK_FILTER = TokenSet.create(GherkinElementTypes.EXAMPLES_BLOCK);

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

  @Override
  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitScenarioOutline(this);
  }

  @Override
  @NotNull
  public List<GherkinExamplesBlock> getExamplesBlocks() {
    List<GherkinExamplesBlock> result = new ArrayList<>();
    final ASTNode[] nodes = getNode().getChildren(EXAMPLES_BLOCK_FILTER);
    for (ASTNode node : nodes) {
      result.add((GherkinExamplesBlock) node.getPsi());
    }
    return result;
  }

  @Override
  @Nullable
  public Map<String, String> getOutlineTableMap() {
    return CachedValuesManager
      .getCachedValue(this, () -> CachedValueProvider.Result.create(buildOutlineTableMap(this), PsiModificationTracker.MODIFICATION_COUNT));
  }
  
  @Nullable
  private static Map<String, String> buildOutlineTableMap(@Nullable GherkinScenarioOutline scenarioOutline) {
    if (scenarioOutline == null) {
      return null;
    }
    final List<GherkinExamplesBlock> examplesBlocks = scenarioOutline.getExamplesBlocks();
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
