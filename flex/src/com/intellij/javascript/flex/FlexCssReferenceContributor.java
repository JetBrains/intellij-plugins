// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixProvider;
import com.intellij.javascript.flex.css.CssClassValueReference;
import com.intellij.javascript.flex.css.FlexCssUtil;
import com.intellij.javascript.flex.resolve.FlexResolveHelper;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.ReferenceSupport;
import com.intellij.lang.javascript.flex.actions.newfile.CreateFlexComponentFix;
import com.intellij.lang.javascript.psi.ecmal4.impl.ActionScriptReferenceSet;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateClassOrInterfaceFix;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.css.*;
import com.intellij.psi.css.impl.CssTokenImpl;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.module.ModuleUtilCore.findModuleForPsiElement;

public class FlexCssReferenceContributor extends PsiReferenceContributor {


  @Override
  public void registerReferenceProviders(final @NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(CssString.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, PsiElement context) {
        CssFunction fun = PsiTreeUtil.getParentOfType((PsiElement)element, CssFunction.class);
        String funName;
        return fun != null && (FlexReferenceContributor.CLASS_REFERENCE.equals(funName = fun.getName()) || "Embed".equals(funName));
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    })), new PsiReferenceProvider() {
      @Override
      public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement element, @NotNull ProcessingContext context) {
        CssFunction fun = PsiTreeUtil.getParentOfType(element, CssFunction.class);
        if (fun != null && "Embed".equals(fun.getName())) {
          // TODO: remove this stuff once css function will have proper psi
          PsiElement prev = PsiTreeUtil.prevLeaf(element);
          if (prev instanceof PsiWhiteSpace) prev = PsiTreeUtil.prevLeaf(prev);
          if (prev != null) prev = PsiTreeUtil.prevLeaf(prev);
          if (prev instanceof PsiWhiteSpace) prev = PsiTreeUtil.prevLeaf(prev);
          // prev.getText() == Embed if element is the first parameter and the name not specified
          if (prev != null && !FlexReferenceContributor.SOURCE_ATTR_NAME.equals(prev.getText()) && !"Embed".equals(prev.getText())) {
            return PsiReference.EMPTY_ARRAY;
          }
          return ReferenceSupport.getFileRefs(element, element, 1, ReferenceSupport.LookupOptions.EMBEDDED_ASSET);
        }
        final String value = StringUtil.unquoteString(element.getText());
        ActionScriptReferenceSet refSet = new ActionScriptReferenceSet(element, value, 1, false, true);
        if (fun != null && element instanceof CssString) {
          assert FlexReferenceContributor.CLASS_REFERENCE.equals(fun.getName());
          refSet.setLocalQuickFixProvider(new LocalQuickFixProvider() {
            @Override
            public @NotNull LocalQuickFix @Nullable [] getQuickFixes() {
              if (!FlexResolveHelper.isValidClassName(value, true)) {
                return LocalQuickFix.EMPTY_ARRAY;
              }

              ActionScriptCreateClassOrInterfaceFix[] fixes = new ActionScriptCreateClassOrInterfaceFix[]{
                new ActionScriptCreateClassOrInterfaceFix(value, null, element),
                new CreateFlexComponentFix(value, element)
              };
              for (ActionScriptCreateClassOrInterfaceFix fix : fixes) {
                fix.setCreatedClassFqnConsumer(newFqn -> {
                  if (FileModificationService.getInstance().preparePsiElementForWrite(element)){
                    ElementManipulators.handleContentChange(element, newFqn);
                  }
                });
              }
              return fixes;
            }
          });
        }
        return refSet.getReferences();
      }
    });

    registrar.registerReferenceProvider(PlatformPatterns.psiElement().and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, PsiElement context) {
        if (element instanceof CssTokenImpl || element instanceof CssString) {
          CssTermList cssTermList = PsiTreeUtil.getParentOfType((PsiElement)element, CssTermList.class);
          if (cssTermList != null) {
            CssDeclaration cssDeclaration = PsiTreeUtil.getParentOfType(cssTermList, CssDeclaration.class);
            if (cssDeclaration != null && cssDeclaration.getValue() == cssTermList) {
              if (FlexCssUtil.isStyleNameProperty(cssDeclaration.getPropertyName())) {
                PsiFile file = cssDeclaration.getContainingFile();
                if (file != null) {
                  if (file.getFileType() == CssFileType.INSTANCE) {
                    Module module = findModuleForPsiElement(cssDeclaration);
                    return module != null && ModuleType.get(module) == FlexModuleType.getInstance();
                  }
                  return JavaScriptSupportLoader.isFlexMxmFile(file);
                }
              }
            }
          }
        }
        return false;
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    })), new PsiReferenceProvider() {
      @Override
      public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        String styleName = CssClassValueReference.getValue(element);
        if (styleName.length() > 0) {
          return new PsiReference[]{new CssClassValueReference(element)};
        }
        return PsiReference.EMPTY_ARRAY;
      }
    });
  }
}
