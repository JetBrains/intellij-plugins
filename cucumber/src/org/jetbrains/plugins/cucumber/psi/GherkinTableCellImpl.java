// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinPsiElementBase;

@ApiStatus.Internal
@NotNullByDefault
public class GherkinTableCellImpl extends GherkinPsiElementBase implements GherkinTableCell {
  public GherkinTableCellImpl(ASTNode node) {
    super(node);
  }

  @Override
  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitGherkinTableCell(this);
  }

  @Override
  public String toString() {
    return "GherkinTableCell:" + getName();
  }

  @Override
  protected String getPresentableText() {
    return String.format("Step parameter '%s'", getName());
  }

  @Override
  public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
    final LeafPsiElement content = PsiTreeUtil.getChildOfType(this, LeafPsiElement.class);
    if (content != null) {
      PsiElement[] elements = GherkinElementFactory.getTopLevelElements(getProject(), name);
      getNode().replaceChild(content, elements[0].getNode());
    }
    return this;
  }

  @Override
  public String getName() {
    return getText();
  }

  @Override
  public @Nullable PsiElement getNameIdentifier() {
    return PsiTreeUtil.getChildOfType(this, LeafPsiElement.class);
  }

  @Override
  public SearchScope getUseScope() {
    return new LocalSearchScope(getContainingFile());
  }

  @Override
  public PsiReference[] getReferences() {
    return CachedValuesManager.getCachedValue(this, () -> CachedValueProvider.Result.create(getReferencesInner(), this));
  }

  private PsiReference[] getReferencesInner() {
    return ReferenceProvidersRegistry.getReferencesFromProviders(this);
  }
}
