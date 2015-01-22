package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartId;
import com.jetbrains.lang.dart.psi.DartLibraryNameElement;
import com.jetbrains.lang.dart.util.DartElementGenerator;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class DartLibraryNameElementBase extends DartPsiCompositeElementImpl implements PsiNameIdentifierOwner {
  public DartLibraryNameElementBase(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String newLibraryName) throws IncorrectOperationException {
    final DartLibraryNameElement libraryNameElementNew =
      DartElementGenerator.createLibraryNameElementFromText(getProject(), newLibraryName);

    if (libraryNameElementNew != null) {
      getNode().replaceAllChildrenToChildrenOf(libraryNameElementNew.getNode());
    }

    return this;
  }

  @NotNull
  @Override
  public String getName() {
    StringBuilder name = new StringBuilder();
    for (DartId id : PsiTreeUtil.getChildrenOfTypeAsList(this, DartId.class)) {
      if (name.length() > 0) {
        name.append('.');
      }
      name.append(id.getText());
    }
    return name.toString();
  }

  @Nullable
  public ItemPresentation getPresentation() {
    final PsiElement parent = getParent();
    if (parent instanceof NavigationItem) {
      return ((NavigationItem)parent).getPresentation();
    }
    return null;
  }

  @Override
  public Icon getIcon(int flags) {
    final ItemPresentation presentation = getPresentation();
    return presentation == null ? super.getIcon(flags) : presentation.getIcon(true);
  }

  @Override
  public PsiElement getNameIdentifier() {
    return this;
  }

  @NotNull
  @Override
  public SearchScope getUseScope() {
    final DartComponentType type = DartComponentType.typeOf(getParent());
    final DartComponent component = PsiTreeUtil.getParentOfType(getParent(), DartComponent.class, true);
    final boolean localType = type == DartComponentType.FUNCTION
                              || type == DartComponentType.PARAMETER
                              || type == DartComponentType.VARIABLE;
    if (localType && component != null) {
      return new LocalSearchScope(component);
    }
    return super.getUseScope();
  }
}
