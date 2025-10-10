// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.psi.*;
import org.jetbrains.plugins.cucumber.psi.refactoring.GherkinChangeUtil;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class GherkinStepImpl extends GherkinPsiElementBase implements GherkinStep, PsiCheckedRenameElement {

  private static final TokenSet TEXT_FILTER = TokenSet
    .create(GherkinTokenTypes.TEXT, GherkinElementTypes.STEP_PARAMETER, TokenType.WHITE_SPACE, GherkinTokenTypes.STEP_PARAMETER_TEXT,
            GherkinTokenTypes.STEP_PARAMETER_BRACE);

  private final Object LOCK = new Object();

  private List<String> mySubstitutions;

  public GherkinStepImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "GherkinStep:" + getName();
  }

  @Override
  public @Nullable ASTNode getKeyword() {
    return getNode().findChildByType(GherkinTokenTypes.STEP_KEYWORD);
  }

  @Override
  protected @NotNull String getElementText() {
    final ASTNode node = getNode();
    final ASTNode[] children = node.getChildren(TEXT_FILTER);
    return StringUtil.join(children, astNode -> astNode.getText(), "").trim();
  }

  @Override
  public @Nullable GherkinPystring getPystring() {
    return PsiTreeUtil.findChildOfType(this, GherkinPystring.class);
  }

  @Override
  public @Nullable GherkinTable getTable() {
    final ASTNode tableNode = getNode().findChildByType(GherkinElementTypes.TABLE);
    return tableNode == null ? null : (GherkinTable)tableNode.getPsi();
  }

  @Override
  protected String getPresentableText() {
    final ASTNode keywordNode = getKeyword();
    final String prefix = keywordNode != null ? keywordNode.getText() + ": " : "Step: ";
    return prefix + getName();
  }

  @Override
  public PsiReference @NotNull [] getReferences() {
    return CachedValuesManager.getCachedValue(this, () -> CachedValueProvider.Result.create(getReferencesInner(), this));
  }

  private PsiReference[] getReferencesInner() {
    return ReferenceProvidersRegistry.getReferencesFromProviders(this);
  }

  @Override
  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitStep(this);
  }

  @Override
  public @NotNull List<String> getParamsSubstitutions() {
    synchronized (LOCK) {
      if (mySubstitutions == null) {
        final ArrayList<String> substitutions = new ArrayList<>();

        // step name
        final String text = getName();
        if (StringUtil.isEmpty(text)) {
          return Collections.emptyList();
        }
        CucumberUtil.addSubstitutionFromText(text, substitutions);

        // pystring
        final GherkinPystring pystring = getPystring();
        String pystringText = pystring != null ? pystring.getText() : null;
        if (!StringUtil.isEmpty(pystringText)) {
          CucumberUtil.addSubstitutionFromText(pystringText, substitutions);
        }

        // table
        final GherkinTable table = getTable();
        final String tableText = table == null ? null : table.getText();
        if (tableText != null) {
          CucumberUtil.addSubstitutionFromText(tableText, substitutions);
        }

        mySubstitutions = substitutions.isEmpty() ? Collections.emptyList() : substitutions;
      }
      return mySubstitutions;
    }
  }

  @Override
  public void subtreeChanged() {
    super.subtreeChanged();
    clearCaches();
  }

  @Override
  public @Nullable GherkinStepsHolder getStepHolder() {
    final PsiElement parent = getParent();
    return parent != null ? (GherkinStepsHolder)parent : null;
  }

  private void clearCaches() {
    synchronized (LOCK) {
      mySubstitutions = null;
    }
  }

  @Override
  public @Nullable String getSubstitutedName() {
    final GherkinStepsHolder holder = getStepHolder();
    if (!(holder instanceof GherkinScenarioOutline outline)) {
      return getName();
    }
    return CucumberUtil.substituteTableReferences(getName(), outline.getOutlineTableMap()).getSubstitution();
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    PsiFile containingFile = getContainingFile();
    if (containingFile == null) {
      throw new IllegalStateException("Cannot rename step whose containing file is null");
    }
    if (!(containingFile instanceof GherkinFile gherkinFile)) {
      throw new IllegalStateException("Cannot rename step whose containing file isn't GherkinFile");
    }
    ASTNode keyword = getKeyword();
    if (keyword == null) {
      throw new IllegalStateException("Cannot rename step whose keyword is null");
    }

    GherkinStep newStep = GherkinChangeUtil.createStep(keyword.getText() + " " + name, gherkinFile, getProject());
    replace(newStep);
    return newStep;
  }

  @Override
  public @NotNull String getName() {
    return getElementText();
  }

  @Override
  public @NotNull Collection<AbstractStepDefinition> findDefinitions() {
    final List<AbstractStepDefinition> result = new ArrayList<>();
    for (final PsiReference reference : getReferences()) {
      if (reference instanceof CucumberStepReference cucumberStepReference) {
        result.addAll(cucumberStepReference.resolveToDefinitions());
      }
    }
    return result;
  }


  @Override
  public boolean isRenameAllowed(@Nullable String newName) {
    final Collection<AbstractStepDefinition> definitions = findDefinitions();
    if (definitions.isEmpty()) {
      return false; // Cannot rename a step without definitions
    }
    for (final AbstractStepDefinition definition : definitions) {
      if (!definition.supportsRename(newName)) {
        return false; // Cannot rename a step if at least one of its definitions does not support renaming
      }
    }
    return true; // Nothing prevents us from renaming
  }

  @Override
  public void checkSetName(String name) {
    if (!isRenameAllowed(name)) {
      throw new IncorrectOperationException(CucumberBundle.message("cucumber.refactor.rename.bad_symbols"));
    }
  }
}
