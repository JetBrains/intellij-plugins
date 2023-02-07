// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiCheckedRenameElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.TokenType;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GherkinStepImpl extends GherkinPsiElementBase implements GherkinStep, PsiCheckedRenameElement {

  private static final TokenSet TEXT_FILTER = TokenSet
    .create(GherkinTokenTypes.TEXT, GherkinElementTypes.STEP_PARAMETER, TokenType.WHITE_SPACE, GherkinTokenTypes.STEP_PARAMETER_TEXT,
            GherkinTokenTypes.STEP_PARAMETER_BRACE);

  private static final Pattern PARAMETER_SUBSTITUTION_PATTERN = Pattern.compile("<([^>\n\r]+)>");
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
  @Nullable
  public ASTNode getKeyword() {
    return getNode().findChildByType(GherkinTokenTypes.STEP_KEYWORD);
  }

  @Override
  @NotNull
  protected String getElementText() {
    final ASTNode node = getNode();
    final ASTNode[] children = node.getChildren(TEXT_FILTER);
    return StringUtil.join(children, astNode -> astNode.getText(), "").trim();
  }

  @Override
  @Nullable
  public GherkinPystring getPystring() {
    return PsiTreeUtil.findChildOfType(this, GherkinPystring.class);
  }

  @Override
  @Nullable
  public GherkinTable getTable() {
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
  @NotNull
  public List<String> getParamsSubstitutions() {
    synchronized (LOCK) {
      if (mySubstitutions == null) {
        final ArrayList<String> substitutions = new ArrayList<>();


        // step name
        final String text = getName();
        if (StringUtil.isEmpty(text)) {
          return Collections.emptyList();
        }
        addSubstitutionFromText(text, substitutions);

        // pystring
        final GherkinPystring pystring = getPystring();
        String pystringText = pystring != null ? pystring.getText() : null;
        if (!StringUtil.isEmpty(pystringText)) {
          addSubstitutionFromText(pystringText, substitutions);
        }

        // table
        final GherkinTable table = getTable();
        final String tableText = table == null ? null : table.getText();
        if (tableText != null) {
          addSubstitutionFromText(tableText, substitutions);
        }

        mySubstitutions = substitutions.isEmpty() ? Collections.emptyList() : substitutions;
      }
      return mySubstitutions;
    }
  }

  private static void addSubstitutionFromText(String text, ArrayList<String> substitutions) {
    final Matcher matcher = PARAMETER_SUBSTITUTION_PATTERN.matcher(text);
    boolean result = matcher.find();
    if (!result) {
      return;
    }

    do {
      final String substitution = matcher.group(1);
      if (!StringUtil.isEmpty(substitution) && !substitutions.contains(substitution)) {
        substitutions.add(substitution);
      }
      result = matcher.find();
    }
    while (result);
  }

  @Override
  public void subtreeChanged() {
    super.subtreeChanged();
    clearCaches();
  }

  @Override
  @Nullable
  public GherkinStepsHolder getStepHolder() {
    final PsiElement parent = getParent();
    return parent != null ? (GherkinStepsHolder)parent : null;
  }

  private void clearCaches() {
    synchronized (LOCK) {
      mySubstitutions = null;
    }
  }

  @Override
  @Nullable
  public String getSubstitutedName() {
    final GherkinStepsHolder holder = getStepHolder();
    if (!(holder instanceof GherkinScenarioOutline outline)) {
      return getName();
    }
    return CucumberUtil.substituteTableReferences(getName(), outline.getOutlineTableMap()).getSubstitution();
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    GherkinStep newStep = GherkinChangeUtil.createStep(getKeyword().getText() + " " + name, getProject());
    replace(newStep);
    return newStep;
  }

  @Override
  @NotNull
  public String getName() {
    return getElementText();
  }

  @NotNull
  @Override
  public Collection<AbstractStepDefinition> findDefinitions() {
    final List<AbstractStepDefinition> result = new ArrayList<>();
    for (final PsiReference reference : getReferences()) {
      if (reference instanceof CucumberStepReference) {
        result.addAll(((CucumberStepReference)reference).resolveToDefinitions());
      }
    }
    return result;
  }


  @Override
  public boolean isRenameAllowed(@Nullable final String newName) {
    final Collection<AbstractStepDefinition> definitions = findDefinitions();
    if (definitions.isEmpty()) {
      return false; // No sense to rename step with out of definitions
    }
    for (final AbstractStepDefinition definition : definitions) {
      if (!definition.supportsRename(newName)) {
        return false; //At least one definition does not support renaming
      }
    }
    return true; // Nothing prevents us from renaming
  }

  @Override
  public void checkSetName(final String name) {
    if (!isRenameAllowed(name)) {
      throw new IncorrectOperationException(CucumberBundle.message("cucumber.refactor.rename.bad_symbols"));
    }
  }
}
