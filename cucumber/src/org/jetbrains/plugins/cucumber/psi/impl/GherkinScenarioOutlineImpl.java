package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */
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
}
