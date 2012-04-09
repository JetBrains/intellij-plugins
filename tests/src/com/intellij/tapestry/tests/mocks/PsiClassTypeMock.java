package com.intellij.tapestry.tests.mocks;

import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class PsiClassTypeMock extends PsiClassType {
  public PsiClassTypeMock() {
    super(LanguageLevel.JDK_1_5);
  }

  private PsiClass _resolve;

    @Override
    @Nullable
    public PsiClass resolve() {
        return _resolve;
    }

    public PsiClassTypeMock setResolve(PsiClass resolve) {
        _resolve = resolve;

        return this;
    }

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    @NotNull
    public PsiType[] getParameters() {
        return new PsiType[0];
    }

    @Override
    @NotNull
    public ClassResolveResult resolveGenerics() {
        return null;
    }

    @Override
    @NotNull
    public PsiClassType rawType() {
        return null;
    }

    @Override
    @NotNull
    public GlobalSearchScope getResolveScope() {
        return null;
    }

    @Override
    @NotNull
    public LanguageLevel getLanguageLevel() {
        return null;
    }

    @NotNull
    @Override
    public PsiClassType setLanguageLevel(@NotNull final LanguageLevel languageLevel) {
        return this;
    }

    @Override
    public String getPresentableText() {
        return null;
    }

    @Override
    public String getCanonicalText() {
        return null;
    }

    @Override
    public String getInternalCanonicalText() {
        return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean equalsToText(@NonNls String text) {
        return false;
    }
}
