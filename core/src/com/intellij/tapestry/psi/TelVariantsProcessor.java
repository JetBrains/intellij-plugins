package com.intellij.tapestry.psi;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.resolve.JavaMethodCandidateInfo;
import com.intellij.psi.resolve.JavaMethodResolveHelper;
import com.intellij.psi.scope.BaseScopeProcessor;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.tapestry.core.TapestryConstants;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.intellij.psi.PsiModifier.*;
import static com.intellij.util.containers.ContainerUtil.addIfNotNull;

/**
 * @author peter
 * @author Alexey Chmutov
 */
abstract class TelVariantsProcessor<T> extends BaseScopeProcessor {

  @NonNls private static final THashSet<String> INSECURE_OBJECT_METHODS =
    new THashSet<String>(Arrays.asList("wait", "notify", "notifyAll"));

  private final Set<T> myResult = new LinkedHashSet<T>();
  private final boolean myForCompletion;
  private final boolean myMethodCall;
  private final String myReferenceName;
  private final JavaMethodResolveHelper myPropertyAccessors;
  private final JavaMethodResolveHelper myMethods;
  private final boolean myAllowStatic;

  protected TelVariantsProcessor(@Nullable PsiElement parent, @Nullable String referenceName, boolean allowStatic) {
    myAllowStatic = allowStatic;
    myForCompletion = referenceName == null;
    myReferenceName = referenceName;
    myMethodCall = parent instanceof TelMethodCallExpression;
    if (myMethodCall && !myForCompletion) {
      final PsiType[] parameterTypes = ((TelMethodCallExpression)parent).getArgumentTypes();
      myMethods = new JavaMethodResolveHelper(parent, parameterTypes);
      myPropertyAccessors = null;
    }
    else {
      myMethods = myForCompletion ? new JavaMethodResolveHelper(parent, null) : null;
      myPropertyAccessors = new JavaMethodResolveHelper(parent, null);
    }
  }

  public boolean execute(final PsiElement element, final ResolveState state) {
    if (!(element instanceof PsiNamedElement)) return true;

    final PsiNamedElement namedElement = (PsiNamedElement)element;
    if (StringUtil.isEmpty(namedElement.getName())) return true;

    if (namedElement instanceof PsiClass) {
      return true;
    }

    final boolean isMethod = namedElement instanceof PsiMethod;
    final boolean isField = namedElement instanceof PsiField;
    if (isMethod) {
      final PsiMethod method = (PsiMethod)namedElement;
      if (!method.hasModifierProperty(PUBLIC) ||
          method.isConstructor() ||
          !myAllowStatic && method.hasModifierProperty(STATIC) ||
          INSECURE_OBJECT_METHODS.contains(method.getName())) {
        return true;
      }
      if (!myMethodCall &&
          myPropertyAccessors != null &&
          PropertyUtil.isSimplePropertyGetter(method) &&
          (myReferenceName == null || myReferenceName.equalsIgnoreCase(PropertyUtil.getPropertyName(method)))) {
        myPropertyAccessors.addMethod(method, state.get(PsiSubstitutor.KEY), false);
      }
      if (myForCompletion || myMethodCall && myReferenceName.equalsIgnoreCase(namedElement.getName())) {
        if (myMethods != null) myMethods.addMethod((PsiMethod)namedElement, state.get(PsiSubstitutor.KEY), false);
      }
    }
    else if (isField) {
      final PsiField field = (PsiField)namedElement;
      final PsiModifierList modifierList = field.getModifierList();
      if (!field.hasModifierProperty(PRIVATE) || modifierList == null || field.hasModifierProperty(STATIC)) return true;
      String propertyAnnotation = null;
      for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
        if (TapestryConstants.PROPERTY_ANNOTATION.equals(psiAnnotation.getQualifiedName())) {
          propertyAnnotation = psiAnnotation.getQualifiedName();
        }
      }
      if (propertyAnnotation == null) return true;
      if (myForCompletion || !myMethodCall && myReferenceName.equalsIgnoreCase(namedElement.getName())) {
        addIfNotNull(createResult(namedElement, true), myResult);
      }
      final String getterName = PropertyUtil.suggestGetterName(field.getProject(), field);
      if (myForCompletion || myMethodCall && myReferenceName.equalsIgnoreCase(getterName)) {
        myMethods.addMethod(new TapestryAccessorMethod(field, true, getterName), state.get(PsiSubstitutor.KEY), false);
        //addIfNotNull(createResult(new PropertyAccessorElement(field, getterName, true), true), myResult);
      }
      final String setterName = PropertyUtil.suggestSetterName(field.getProject(), field);
      if (!field.hasModifierProperty(FINAL) && (myForCompletion || myMethodCall && myReferenceName.equalsIgnoreCase(setterName))) {
        myMethods.addMethod(new TapestryAccessorMethod(field, false, setterName), state.get(PsiSubstitutor.KEY), false);
        //addIfNotNull(createResult(new PropertyAccessorElement(field, setterName, false), true), myResult);
      }
    }
    return myForCompletion || myResult.size() != 1;
  }

  @Nullable
  protected abstract T createResult(final PsiNamedElement element, final boolean validResult);

  @NotNull
  public T[] getVariants(T[] array) {
    if (myPropertyAccessors != null) {
      for (final JavaMethodCandidateInfo methodCandidateInfo : myPropertyAccessors.getMethods()) {
        final BeanProperty property = BeanProperty.createBeanProperty(methodCandidateInfo.getMethod());
        if (property != null) {
          addIfNotNull(createResult(property.getPsiElement(), true), myResult);
        }
      }
    }
    if (myMethods != null) {
      for (final JavaMethodCandidateInfo methodCandidateInfo : myMethods.getMethods()) {
        final boolean validResult = myMethods.getResolveError() == JavaMethodResolveHelper.ErrorType.NONE;
        addIfNotNull(createResult(methodCandidateInfo.getMethod(), validResult), myResult);
      }
    }
    return myResult.toArray(array);
  }
}
