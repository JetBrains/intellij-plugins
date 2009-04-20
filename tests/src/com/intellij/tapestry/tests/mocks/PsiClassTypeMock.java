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

    @Nullable
    public PsiClass resolve() {
        return _resolve;
    }

    public PsiClassTypeMock setResolve(PsiClass resolve) {
        _resolve = resolve;

        return this;
    }

    public String getClassName() {
        return null;
    }

    @NotNull
    public PsiType[] getParameters() {
        return new PsiType[0];
    }

    @NotNull
    public ClassResolveResult resolveGenerics() {
        return null;
    }

    @NotNull
    public PsiClassType rawType() {
        return null;
    }

    @NotNull
    public GlobalSearchScope getResolveScope() {
        return null;
    }

    @NotNull
    public LanguageLevel getLanguageLevel() {
        return null;
    }

    public PsiClassType setLanguageLevel(final LanguageLevel languageLevel) {
        return null;
    }

    public String getPresentableText() {
        return null;
    }

    public String getCanonicalText() {
        return null;
    }

    public String getInternalCanonicalText() {
        return null;
    }

    public boolean isValid() {
        return false;
    }

    public boolean equalsToText(@NonNls String text) {
        return false;
    }
}
