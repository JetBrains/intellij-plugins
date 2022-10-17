// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.plugins.drools.lang.psi.DroolsIdentifier;
import com.intellij.plugins.drools.lang.psi.DroolsParExpr;
import com.intellij.plugins.drools.lang.psi.DroolsReference;
import com.intellij.plugins.drools.lang.psi.util.DroolsElementsFactory;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.impl.beanProperties.BeanPropertyElement;
import com.intellij.psi.impl.light.LightClass;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.infos.CandidateInfo;
import com.intellij.psi.util.PropertyUtilBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.IconManager;
import com.intellij.util.CommonProcessors;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public abstract class DroolsReferenceImpl extends DroolsPsiCompositeElementImpl implements DroolsIdentifier, DroolsReference {
  public DroolsReferenceImpl(@NotNull ASTNode node) {
    super(node);
  }

  @NotNull
  @Override
  public PsiElement getElement() {
    return this;
  }

  @Override
  public PsiReference getReference() {
    return this;
  }

  @NotNull
  @Override
  public TextRange getRangeInElement() {
    final TextRange textRange = getTextRange();
    return new TextRange(0, textRange.getEndOffset() - textRange.getStartOffset());
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return getText();
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    PsiElement element = this;

    String newName = newElementName;
    final PsiElement resolve = resolve();
    if (resolve instanceof BeanPropertyElement) {
      final String propertyName = PropertyUtilBase.getPropertyName(newElementName);
      if (propertyName != null) {
        newName = propertyName;
      }
    }
    else if (resolve instanceof PsiMethod) {
      if (PropertyUtilBase.isSimplePropertyGetter((PsiMethod)resolve)) {
        final String getterName = PropertyUtilBase.suggestGetterName(newElementName, null);
        if (getterName != null) {
          newName = getterName;
        }
      }
    }
    final DroolsIdentifier identifier = DroolsElementsFactory.createDroolsIdentifier(newName, getProject());
    if (identifier != null) {
      return element.replace(identifier);
    }
    return element;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    if (element instanceof PsiClass) {
      return handleElementRename(((PsiClass)element).getName());
    }
    return element;
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    final PsiElement resolve = resolve();
    if (resolve instanceof BeanPropertyElement) {
      if (element instanceof BeanPropertyElement) {
        return ((BeanPropertyElement)element).getMethod().equals(((BeanPropertyElement)resolve).getMethod());
      }
      else if (element instanceof PsiMethod) {
        return element.equals(((BeanPropertyElement)resolve).getMethod());
      }
    }
    else if (resolve instanceof LightClass) {
      return ((LightClass)resolve).getDelegate().equals(element);
    }
    return element.equals(resolve);
  }

  @Override
  public Object @NotNull [] getVariants() {
    CommonProcessors.CollectProcessor<PsiElement> processor = new CommonProcessors.CollectProcessor<>(new HashSet<>()) {
      @Override
      protected boolean accept(PsiElement psiElement) {
        if (psiElement instanceof PsiPackage) {
          return !StringUtil.isEmptyOrSpaces(((PsiPackage)psiElement).getName());
        }
        if (psiElement instanceof PsiMethod) {
          final PsiMethod psiMethod = (PsiMethod)psiElement;
          if (psiMethod.isConstructor()) return false;
          final PsiClass containingClass = psiMethod.getContainingClass();
          if (containingClass != null && CommonClassNames.JAVA_LANG_OBJECT.equals(containingClass.getQualifiedName())) return false;
          return psiMethod.getModifierList().hasModifierProperty(PsiModifier.PUBLIC);
        }
        return super.accept(psiElement);
      }

      @Override
      public boolean process(PsiElement psiElement) {
        if (psiElement instanceof PsiMethod) {
          if (PropertyUtilBase.isSimplePropertyGetter((PsiMethod)psiElement)) {
            final BeanProperty property = BeanProperty.createBeanProperty((PsiMethod)psiElement);
            if (property != null) {
              getResults().add(property.getPsiElement());
            }
          }
        }
        return super.process(psiElement);
      }
    };
    DroolsResolveUtil.processVariables(processor, this, false);
    if (isProcessQualifiers()) {
      DroolsResolveUtil
        .processQualifiedIdentifier(processor, this); // todo check place for identifier   - testModifyPairStatementCompletion2
    }

    return ContainerUtil.map2Array(processor.getResults(), Object.class, psiElement -> {
      if (psiElement instanceof PsiVariable) {
        final PsiVariable variable = (PsiVariable)psiElement;
        return LookupElementBuilder.create(variable.getName()).withIcon(
            IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Variable))
          .withTypeText(variable.getType().getCanonicalText());
      }
      else if (psiElement instanceof PsiMethod) {
        final PsiMethod psiMethod = (PsiMethod)psiElement;
        final PsiType type = psiMethod.getReturnType();
        return LookupElementBuilder.create(psiMethod.getName()).withIcon(
            IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Method))
          .withTypeText(type == null ? "" : type.getCanonicalText()).withInsertHandler(
            ParenthesesInsertHandler.WITH_PARAMETERS);
      }
      return psiElement;
    });
  }

  private boolean isProcessQualifiers() {
    // todo find the best way!!!
    return PsiTreeUtil.getParentOfType(this, DroolsParExpr.class) == null;
  }

  @Override
  public boolean isSoft() {
    return false;
  }

  @Override
  public PsiElement resolve() {
    final ResolveResult[] resolveResults = multiResolve(true);

    return resolveResults.length == 0  ? null : DroolsResolveUtil.chooseDroolsTypeResult(resolveResults);
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
    return ResolveCache.getInstance(getElement().getProject()).resolveWithCaching(this, MyResolver.INSTANCE, false, false);
  }

  static class MyResolver implements ResolveCache.PolyVariantResolver<DroolsReference> {
    static final MyResolver INSTANCE = new MyResolver();
    @Override
    public ResolveResult @NotNull [] resolve(@NotNull DroolsReference ref, boolean incompleteCode) {
      return ContainerUtil
        .map2Array(DroolsResolveUtil.resolve(ref, false), ResolveResult.class,
                   psiElement -> new CandidateInfo(psiElement, PsiSubstitutor.EMPTY));
    }
  }
}
