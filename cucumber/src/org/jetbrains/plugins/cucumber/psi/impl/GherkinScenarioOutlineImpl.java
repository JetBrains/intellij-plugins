// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinElementTypes;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinExamplesBlock;
import org.jetbrains.plugins.cucumber.psi.GherkinScenarioOutline;
import org.jetbrains.plugins.cucumber.psi.GherkinTable;
import org.jetbrains.plugins.cucumber.psi.GherkinTableCell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GherkinScenarioOutlineImpl extends GherkinStepsHolderBase implements GherkinScenarioOutline {
  private static final TokenSet EXAMPLES_BLOCK_FILTER = TokenSet.create(GherkinElementTypes.EXAMPLES_BLOCK);

  public GherkinScenarioOutlineImpl(@NotNull ASTNode node) {
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
  public @NotNull List<GherkinExamplesBlock> getExamplesBlocks() {
    List<GherkinExamplesBlock> result = new ArrayList<>();
    final ASTNode[] nodes = getNode().getChildren(EXAMPLES_BLOCK_FILTER);
    for (ASTNode node : nodes) {
      result.add((GherkinExamplesBlock)node.getPsi());
    }
    return result;
  }

  @Override
  public @Nullable Map<String, String> getOutlineTableMap() {
    return CachedValuesManager
      .getCachedValue(this, () -> CachedValueProvider.Result.create(buildOutlineTableMap(this), PsiModificationTracker.MODIFICATION_COUNT));
  }

  @Override
  public List<String> getParamsSubstitutions() {
    final String text = getScenarioName();
    if (StringUtil.isEmpty(text)) return Collections.emptyList();

    final List<String> substitutions = new ArrayList<>();
    CucumberUtil.addSubstitutionFromText(text, substitutions);
    return substitutions;
  }

  private static @Nullable Map<String, String> buildOutlineTableMap(@Nullable GherkinScenarioOutline scenarioOutline) {
    if (scenarioOutline == null) {
      return null;
    }
    final List<GherkinExamplesBlock> examplesBlocks = scenarioOutline.getExamplesBlocks();
    for (GherkinExamplesBlock examplesBlock : examplesBlocks) {
      GherkinTable table = examplesBlock.getTable();
      if (table == null || table.getHeaderRow() == null || table.getDataRows().isEmpty()) {
        continue;
      }
      List<GherkinTableCell> headerCells = table.getHeaderRow().getPsiCells();
      List<GherkinTableCell> dataCells = table.getDataRows().getFirst().getPsiCells();

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
