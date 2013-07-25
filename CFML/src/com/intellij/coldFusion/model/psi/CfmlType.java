package com.intellij.coldFusion.model.psi;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author vnikolaenko
 */
public class CfmlType extends PsiType {

  private String myName;

  public CfmlType(String name) {
    super(PsiAnnotation.EMPTY_ARRAY);
    myName = name;
  }

  @Override
  public String getPresentableText() {
    return myName;
  }

  @Override
  public String getCanonicalText() {
    return myName;
  }

  @Override
  public String getInternalCanonicalText() {
    return myName;
  }

  @Override
  public boolean isValid() {
    return false;
  }

  @Override
  public boolean equalsToText(@NonNls String text) {
    return text.endsWith(myName);
  }

  @Override
  public <A> A accept(@NotNull PsiTypeVisitor<A> visitor) {
    return visitor.visitType(this);
  }

  @Override
  public GlobalSearchScope getResolveScope() {
    return GlobalSearchScope.EMPTY_SCOPE;
  }

  @NotNull
  @Override
  public PsiType[] getSuperTypes() {
    return new PsiType[0];
  }
}
