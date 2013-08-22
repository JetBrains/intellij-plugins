package com.intellij.tapestry.psi;

import com.intellij.codeInsight.completion.util.SimpleMethodCallLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.impl.beanProperties.BeanPropertyElement;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.tapestry.TapestryBundle;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.tapestry.lang.TelLanguage;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 *         Date: 09.10.2009
 *         Time: 16:34:47
 */
public abstract class TelQualifiedReference implements PsiPolyVariantReference {

  private static final ResolveCache.PolyVariantResolver<TelQualifiedReference> MY_RESOLVER =
    new ResolveCache.PolyVariantResolver<TelQualifiedReference>() {
      @NotNull
      public ResolveResult[] resolve(@NotNull final TelQualifiedReference expression, final boolean incompleteCode) {
        return expression.resolveInner();
      }
    };

  private final TelReferenceQualifier myElement;

  protected TelQualifiedReference(@NotNull final TelReferenceQualifier element) {
    myElement = element;
  }

  @NotNull
  public TelExpression getElement() {
    return myElement;
  }

  @NotNull
  public String getCanonicalText() {
    return myElement.getText();
  }

  public boolean isSoft() {
    return true;
  }

  public PsiElement bindToElement(@NotNull final PsiElement element) throws IncorrectOperationException {
    if (isReferenceTo(element)) return myElement;

    if (element instanceof PsiNamedElement) {
      return handleElementRename(((PsiNamedElement)element).getName());
    }
    return myElement;
  }

  public boolean isReferenceTo(final PsiElement element) {
    final PsiManager manager = myElement.getManager();
    for (final ResolveResult result : multiResolve(false)) {
      final PsiElement target = result.getElement();
      if (manager.areElementsEquivalent(element, target)) return true;
      if (target instanceof BeanPropertyElement && manager.areElementsEquivalent(element, ((BeanPropertyElement)target).getMethod())) {
        return true;
      }
      if (target instanceof TapestryAccessorMethod && manager.areElementsEquivalent(element, ((TapestryAccessorMethod)target).getProperty())) {
        return true;
      }
    }
    return false;
  }

  @NotNull
  public final ResolveResult[] multiResolve(final boolean incompleteCode) {
    return ResolveCache.getInstance(myElement.getProject()).resolveWithCaching(this, MY_RESOLVER, true, false);
  }

  @Nullable
  public final PsiElement resolve() {
    final ResolveResult[] results = multiResolve(false);
    return results.length == 1 ? results[0].getElement() : null;
  }

  @NotNull
  private ResolveResult[] resolveInner() {
    final String referenceName = getReferenceName();
    if (referenceName == null) return ResolveResult.EMPTY_ARRAY;

    final TelVariantsProcessor<ResolveResult> processor =
      new TelVariantsProcessor<ResolveResult>(myElement.getParent(), referenceName, getReferenceQualifier() == null) {
        protected ResolveResult createResult(PsiNamedElement element, final boolean validResult) {
          if (element instanceof BeanPropertyElement) {
            element = ((BeanPropertyElement)element).getMethod();
          }
          return new PsiElementResolveResult(element, validResult);
        }
      };
    processVariantsInner(processor, ResolveState.initial());
    return processor.getVariants(ResolveResult.EMPTY_ARRAY);
  }

  private void processVariantsInner(final PsiScopeProcessor processor, final ResolveState state) {
    TelReferenceQualifier qualifier = getReferenceQualifier();
    if (qualifier == null) {
      final IntellijJavaClassType intellijJavaClassType = getPsiClassTypeForContainingTmlFile();
      if (intellijJavaClassType == null) return;
      PsiClass psiClass = intellijJavaClassType.getPsiClass();
      if (psiClass == null) return;
      psiClass.processDeclarations(processor, ResolveState.initial(), null, myElement);
      return;
    }
    PsiType type = qualifier.getPsiType();
    if (type instanceof PsiClassType) {
      final PsiClass psiClass = PsiUtil.resolveClassInType(type);
      if (psiClass != null && !psiClass.processDeclarations(processor, ResolveState.initial(), null, myElement)) {
        return;
      }
    }
    final PsiReference reference = qualifier.getReference();
    if (reference instanceof TelQualifiedReference) {
      final PsiElement psiElement = reference.resolve();
      if (psiElement != null) {
        psiElement.processDeclarations(processor, state, null, myElement);
      }
    }
  }

  @Nullable
  private IntellijJavaClassType getPsiClassTypeForContainingTmlFile() {
    PsiFile file = myElement.getContainingFile();
    if (file.getLanguage() == TelLanguage.INSTANCE) file =
      InjectedLanguageManager.getInstance(file.getProject()).getInjectionHost(file).getContainingFile();
    final TapestryProject project = TapestryUtils.getTapestryProject(file);
    if (project == null) return null;
    PresentationLibraryElement libraryElement = project.findElementByTemplate(file);
    if (libraryElement == null) return null;
    return ((IntellijJavaClassType)libraryElement.getElementClass());
  }

  @NotNull
  public Object[] getVariants() {
    final TelVariantsProcessor<PsiNamedElement> processor =
      new TelVariantsProcessor<PsiNamedElement>(myElement.getParent(), null, getReferenceQualifier() == null) {
        protected PsiNamedElement createResult(final PsiNamedElement element, final boolean validResult) {
          return element;
        }
      };
    processVariantsInner(processor, ResolveState.initial());

    final PsiNamedElement[] elements = processor.getVariants(PsiNamedElement.EMPTY_ARRAY);
    return ContainerUtil.map2Array(elements, LookupElement.class, new Function<PsiNamedElement, LookupElement>() {
      public LookupElement fun(final PsiNamedElement element) {
        if (element instanceof PsiMethod) {
          return new SimpleMethodCallLookupElement((PsiMethod)element);
        }
        final String name = element.getName();
        assert name != null;
        LookupElementBuilder lookupElement = LookupElementBuilder.create(element, name);
        lookupElement = lookupElement.withLookupString(name);
        if (element instanceof PsiField) {
          return lookupElement.withTypeText(((PsiField)element).getType().getPresentableText());
        }
        if (element instanceof BeanPropertyElement) {
          final PsiType type = ((BeanPropertyElement)element).getPropertyType();
          if (type != null) {
            return lookupElement.withTypeText(type.getPresentableText());
          }
        }
        return lookupElement;
      }
    });
  }

  public boolean isQualifierResolved() {
    TelReferenceQualifier qualifier = getReferenceQualifier();
    if (qualifier == null) return true;
    final PsiReference reference = qualifier.getReference();
    return reference == null || reference.resolve() != null;
  }

  @Nullable
  public abstract TelReferenceQualifier getReferenceQualifier();

  @Nullable
  public abstract String getReferenceName();

  @Nullable
  public PsiType getPsiType() {
    final PsiElement element = resolve();
    if (element instanceof PsiMethod) {
      PsiMethod method = (PsiMethod)element;
      return getSubstitutedType(method, method.getReturnType());
    }
    if (element instanceof BeanProperty) {
      final BeanProperty beanProperty = (BeanProperty)element;
      return getSubstitutedType(beanProperty.getMethod(), beanProperty.getPropertyType());
    }
    //if (element instanceof PropertyAccessorElement) {
    //  return ((PropertyAccessorElement)element).getMethodReturnType();
    //}
    if (element instanceof PsiField) {
      return ((PsiField)element).getType();
    }
    return null;
  }

  private PsiType getSubstitutedType(PsiMethod method, PsiType result) {
    if (!(result instanceof PsiClassType)) return result;
    PsiClassType resultClassType = (PsiClassType)result;
    PsiType qualifierType = getQualifierClassType();
    if (!(qualifierType instanceof PsiClassType)) return result;
    final PsiSubstitutor substitutor = getSuperClassSubstitutor(method.getContainingClass(), (PsiClassType)qualifierType);
    return substitutor.substitute(resultClassType);
  }

  @Nullable
  private PsiType getQualifierClassType() {
    final TelReferenceQualifier qualifier = getReferenceQualifier();
    if (qualifier != null) return qualifier.getPsiType();
    IntellijJavaClassType psiClassType = getPsiClassTypeForContainingTmlFile();
    return psiClassType == null ? null : (PsiClassType)psiClassType.getUnderlyingObject();
  }

  @NotNull
  public static PsiSubstitutor getSuperClassSubstitutor(@NotNull PsiClass superClass, @NotNull PsiClassType classType) {
    final PsiClassType.ClassResolveResult classResolveResult = classType.resolveGenerics();
    return TypeConversionUtil.getSuperClassSubstitutor(superClass, classResolveResult.getElement(), classResolveResult.getSubstitutor());
  }

  public String getUnresolvedMessage(boolean resolvedWithError) {
    final String referenceName = getReferenceName();
    String typeName = TelPsiUtil.getPresentableText(getQualifierClassType());
    final PsiElement elementParent = myElement.getParent();
    if (!(elementParent instanceof TelMethodCallExpression)) {
      return TapestryBundle.message("error.cannot.resolve.property", referenceName, typeName);
    }
    if (!resolvedWithError) {
      return TapestryBundle.message("error.cannot.resolve.method", referenceName, typeName);
    }
    String argumentTypes = StringUtil.join(((TelMethodCallExpression)elementParent).getArgumentTypes(), new Function<PsiType, String>() {
      public String fun(final PsiType psiType) {
        return TelPsiUtil.getPresentableText(psiType);
      }
    }, ", ");
    return TapestryBundle.message("error.no.applicable.method", referenceName, typeName, "(" + argumentTypes + ")");
  }
}
