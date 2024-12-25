// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.util;

import com.intellij.psi.*;
import com.intellij.psi.impl.PsiImplUtil;
import com.intellij.psi.impl.light.ImplicitVariableImpl;
import com.intellij.psi.impl.light.LightIdentifier;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DroolsLightVariable extends ImplicitVariableImpl {
  protected final PsiElement myDeclaration;

  public DroolsLightVariable(@NotNull String name, @NotNull PsiType type, @NotNull PsiElement psiElement) {
    super(psiElement.getManager(), new LightRenameableIdentifier(psiElement, name), getPropertyType(type),
          ElementManipulators.getManipulator(psiElement) != null, psiElement);
    myDeclaration = psiElement;
  }

  private static @NotNull PsiType getPropertyType(@NotNull PsiType type) {
    if (type instanceof PsiClassType) {
      final PsiClass psiClass = ((PsiClassType)type).resolve();
      if (psiClass != null && !InheritanceUtil.isInheritor(psiClass, CommonClassNames.JAVA_UTIL_COLLECTION) &&
          !InheritanceUtil.isInheritor(psiClass, CommonClassNames.JAVA_UTIL_MAP)) {

        return JavaPsiFacade.getElementFactory(psiClass.getProject()).createType(new DroolsLightClass(psiClass));
      }
    }
    return type;
  }

  @Override
  public @NotNull PsiElement getNavigationElement() {
    return myDeclaration;
  }

  @Override
  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    final PsiElement element = PsiImplUtil.setName(myNameIdentifier, name);
    if (element instanceof PsiIdentifier) myNameIdentifier = (PsiIdentifier)element;
    return this;
  }

  @Override
  public @Nullable PsiFile getContainingFile() {
    if (!isValid()) throw new PsiInvalidElementAccessException(this);
    return myDeclaration != null ? myDeclaration.getContainingFile() : null;
  }

  @Override
  public String getText() {
    final PsiIdentifier identifier = getNameIdentifier();
    return getType().getPresentableText() + " " + (identifier != null ?identifier.getText() : null);
  }

  @Override
  public boolean isEquivalentTo(final PsiElement another) {
    if (another == this) return true;
    if (another instanceof DroolsLightVariable implicitVariable) {

      final String name = implicitVariable.getName();
      return name.equals(getName()) &&
             another.getManager().areElementsEquivalent(
               implicitVariable.getDeclaration(),
               getDeclaration()
             );
    } else {
      return getManager().areElementsEquivalent(getDeclaration(), another);
    }
  }

  @Override
  public @NotNull SearchScope getUseScope() {
    final PsiFile file = (myDeclaration != null ? myDeclaration:getDeclarationScope()).getContainingFile();
    return file.getUseScope();
  }

  private static class LightRenameableIdentifier extends LightIdentifier {
    private final PsiElement myDeclaration;

    private LightRenameableIdentifier(@NotNull PsiElement declaration, String name) {
      super(declaration.getManager(), name);
      myDeclaration = declaration;
    }

    @Override
    public PsiElement replace(@NotNull PsiElement newElement) throws IncorrectOperationException {
      String newName = newElement.getText();
      if (myDeclaration.isValid()) {
        ElementManipulator<PsiElement> manipulator = ElementManipulators.getManipulator(myDeclaration);
        if (manipulator != null) {
          manipulator.handleContentChange(myDeclaration, newName);
        }
        else if (myDeclaration instanceof PsiNamedElement) {
          ((PsiNamedElement)myDeclaration).setName(newName);
        }
      }

      return new LightRenameableIdentifier(myDeclaration, newName);
    }

    @Override
    public boolean isValid() {
      return myDeclaration == null || myDeclaration.isValid();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DroolsLightVariable variable)) return false;

    return Objects.equals(getName(), variable.getName()) &&
           Objects.equals(myDeclaration, variable.myDeclaration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), myDeclaration);
  }

  public PsiElement getDeclaration() {
    return myDeclaration;
  }
}
