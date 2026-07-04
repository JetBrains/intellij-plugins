// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.css;

import com.intellij.css.util.CssClassUtil;
import com.intellij.css.util.color.CssColor;
import com.intellij.css.util.color.CssColorTextUtilKt;
import com.intellij.javascript.flex.css.FlexCssSupport;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.css.CssClassMarker;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.CssElementVisitor;
import com.intellij.psi.css.CssFunction;
import com.intellij.psi.css.CssString;
import com.intellij.psi.css.resolve.CssReferenceProviderUtil;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * CSS-backed implementation of {@link FlexCssSupport}, contributed by the {@code intellij.flex.css} content module and
 * instantiated by the platform through the content-module descriptor when the CSS plugin is available.
 */
public final class FlexCssSupportImpl implements FlexCssSupport {
  @Override
  public @Nullable String cssClassMarkerName(@NotNull PsiElement element) {
    if (element instanceof CssClassMarker) {
      String name = ((PsiNamedElement)element).getName();
      if (name != null) {
        return CssClassUtil.kebabToCamelCase(name);
      }
    }
    return null;
  }

  @Override
  public boolean isCssString(@Nullable PsiElement element) {
    return element instanceof CssString;
  }

  @Override
  public @NotNull String toCssHexString(@NotNull Color color) {
    return CssColorTextUtilKt.toHexString(CssColor.fromJavaAwtColor(color));
  }

  @Override
  public @NotNull PsiReference createCssPropertyValueReference(@NotNull PsiElement element) {
    return new CssPropertyValueReference(element);
  }

  @Override
  public @NotNull PsiReference createCssClassValueReference(@NotNull PsiElement element) {
    return new CssClassValueReference(element);
  }

  @Override
  public @NotNull PsiReferenceProvider cssClassOrIdReferenceProvider() {
    return CssReferenceProviderUtil.CSS_CLASS_OR_ID_KEY_PROVIDER.getProvider();
  }

  @Override
  public @NotNull List<InjectedCssClassDependency> collectInjectedCssClassDependencies(@NotNull PsiElement injectedRoot,
                                                                                       @NotNull String classReferenceFunctionName) {
    List<InjectedCssClassDependency> result = new ArrayList<>();
    injectedRoot.accept(new CssElementVisitor() {
      private boolean myInClassReference; // to prevent extra references resolve

      @Override
      public void visitElement(final @NotNull PsiElement element) {
        super.visitElement(element);
        element.acceptChildren(this);
      }

      @Override
      public void visitCssFunction(final CssFunction function) {
        if (classReferenceFunctionName.equals(function.getName())) {
          myInClassReference = true;
          try {
            super.visitCssFunction(function);
          }
          finally {
            myInClassReference = false;
          }
        }
      }

      @Override
      public void visitCssString(final CssString string) {
        if (myInClassReference) {
          CssDeclaration declaration = PsiTreeUtil.getParentOfType(string, CssDeclaration.class);
          if (declaration != null) {
            result.add(new InjectedCssClassDependency(declaration.getPropertyName(), declaration, string.getReferences()));
          }
        }
      }
    });
    return result;
  }
}
