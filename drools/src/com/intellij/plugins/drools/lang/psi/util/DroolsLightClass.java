package com.intellij.plugins.drools.lang.psi.util;

import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.impl.light.LightClass;
import com.intellij.psi.scope.DelegatingScopeProcessor;
import com.intellij.psi.scope.ElementClassHint;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PropertyUtilBase;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DroolsLightClass extends LightClass {
  private final PsiClass myResolve;

  public DroolsLightClass(PsiClass resolve) {
    super(resolve);
    myResolve = resolve;
  }

  @Override
  public boolean processDeclarations(@NotNull final PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    if (DroolsCommonUtil.isMvelDialect(place)) {

      final DelegatingScopeProcessor delegatingScopeProcessor = new DelegatingScopeProcessor(processor) {
        @Override
        public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
          if (element instanceof PsiMethod) {
            if (!processBeanProperty((PsiMethod)element, state)) return false;
          }
          else if (element instanceof PsiField) {
            if (!processBeanProperty(PropertyUtilBase.findGetterForField((PsiField)element), state)) return false;
          }
          return super.execute(element, state);
        }

        private boolean processBeanProperty(@Nullable PsiMethod psiMethod,
                                            @NotNull ResolveState state) {
          if (PropertyUtilBase.isSimplePropertyGetter(psiMethod)) {
            final BeanProperty beanProperty = BeanProperty.createBeanProperty(psiMethod);
            if (beanProperty != null) {
              if (!super
                .execute(new DroolsBeanPropertyLightVariable(beanProperty), state)) {
                return false;
              }
            }
          }
          return true;
        }

        @Override
        public <T> T getHint(@NotNull final Key<T> hintKey) {
          if (hintKey == ElementClassHint.KEY) {
            return (T)new ElementClassHint() {
              @Override
              public boolean shouldProcess(@NotNull DeclarationKind kind) {
                if (kind == DeclarationKind.METHOD) {
                  return true;
                }
                final ElementClassHint hint = processor.getHint(ElementClassHint.KEY);
                return hint == null || hint.shouldProcess(kind);
              }
            };
          }
          return super.getHint(hintKey);
        }
      };
      return super.processDeclarations(delegatingScopeProcessor, state, lastParent, place);
    }
    return super.processDeclarations(processor, state, lastParent, place);
  }

  @Override
  public Icon getIcon(int flags) {
    return getDelegate().getIcon(flags);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DroolsLightClass)) return false;

    DroolsLightClass aClass = (DroolsLightClass)o;

    if (myResolve != null ? !myResolve.equals(aClass.myResolve) : aClass.myResolve != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return myResolve != null ? myResolve.hashCode() : 0;
  }

  @Override
  public PsiElement add(@NotNull PsiElement element) throws IncorrectOperationException {
    return getDelegate().add(element);
  }

  @Override
  public PsiClassType @NotNull [] getImplementsListTypes() {
    return getDelegate().getImplementsListTypes();
  }
}
