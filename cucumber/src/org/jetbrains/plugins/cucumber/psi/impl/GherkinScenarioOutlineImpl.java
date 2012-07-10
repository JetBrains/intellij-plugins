package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */
public class GherkinScenarioOutlineImpl extends GherkinPsiElementBase implements GherkinScenarioOutline {
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

  public String getScenarioName() {
    return getElementText();
  }

  @NotNull
  public List<GherkinExamplesBlock> getExamplesBlocks() {
    List<GherkinExamplesBlock> result = new ArrayList<GherkinExamplesBlock>();
    final ASTNode[] nodes = getNode().getChildren(EXAMPLES_BLOCK_FILTER);
    for (ASTNode node : nodes) {
      result.add((GherkinExamplesBlock) node.getPsi());
    }
    return result;
  }

  public GherkinStep[] getSteps() {
    final GherkinStep[] steps = PsiTreeUtil.getChildrenOfType(this, GherkinStep.class);
    return steps == null ? GherkinStep.EMPTY_ARRAY : steps;
  }

  @Override
  public GherkinTag[] getTags() {
    final GherkinTag[] tags = PsiTreeUtil.getChildrenOfType(this, GherkinTag.class);
    return tags == null ? GherkinTag.EMPTY_ARRAY : tags;
  }
}
