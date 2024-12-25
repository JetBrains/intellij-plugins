// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.CfmlScopesInfo;
import com.intellij.coldFusion.model.psi.impl.CfmlNamedAttributeImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlTagInvokeImpl;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.resolve.JavaMethodCandidateInfo;
import com.intellij.psi.resolve.JavaMethodResolveHelper;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.intellij.psi.PsiModifier.*;
import static com.intellij.util.containers.ContainerUtil.addIfNotNull;

public abstract class CfmlVariantsProcessor<T> implements PsiScopeProcessor {

  public static final class CfmlProcessorEvent implements PsiScopeProcessor.Event {
    private CfmlProcessorEvent() {
    }

    public static final CfmlProcessorEvent SET_INITIAL_CLASS = new CfmlProcessorEvent();
    public static final CfmlProcessorEvent START_STATIC = new CfmlProcessorEvent();
  }

  private final Set<T> myResult = new LinkedHashSet<>();
  private final String myReferenceName;
  private final JavaMethodResolveHelper myMethods;
  private final boolean myIsMethodCall;
  private final boolean myIsForCompletion;
  private final PsiElement myElement;
  private boolean myStaticScopeFlag = false;
  private PsiClass myInitialClass = null;
  private boolean myWasConstructorFound = false;
  private int myScope = CfmlScopesInfo.DEFAULT_SCOPE;

  protected CfmlVariantsProcessor(final PsiElement element, final PsiElement parent, @Nullable String referenceName) {
    if (element instanceof CfmlReferenceExpression) {
      final PsiElement scope = ((CfmlReferenceExpression)element).getScope();
      if (scope != null) {
        myScope = CfmlScopesInfo.getScopeByString(scope.getText());
      }
    }
    myElement = element;
    myIsForCompletion = referenceName == null;
    myReferenceName = referenceName != null ? StringUtil.toLowerCase(referenceName) : null;
    myIsMethodCall = parent instanceof CfmlFunctionCallExpression || parent instanceof CfmlTagInvokeImpl;
    if (parent instanceof CfmlFunctionCallExpression && !myIsForCompletion) {
      final PsiType[] parameterTypes = ((CfmlFunctionCallExpression)parent).getArgumentTypes();
      myMethods = new JavaMethodResolveHelper(parent, parent.getContainingFile(), parameterTypes);
    }
    else {
      myMethods = new JavaMethodResolveHelper(parent, parent.getContainingFile(), null);
    }
  }

  @Override
  public void handleEvent(@NotNull Event event, Object associated) {
    if (event == CfmlProcessorEvent.START_STATIC) {
      myStaticScopeFlag = true;
    }
    else if (event == CfmlProcessorEvent.SET_INITIAL_CLASS && associated instanceof PsiClass) {
      myInitialClass = (PsiClass)associated;
      myWasConstructorFound = false;
    }
  }

  @Override
  public boolean execute(final @NotNull PsiElement element, final @NotNull ResolveState state) {
    // continue if not a definition
    if (!(element instanceof PsiNamedElement)) {
      return true;
    }

    // continue if has no name
    if (StringUtil.isEmpty(((PsiNamedElement)element).getName())) {
      return true;
    }

    String elementName = ((PsiNamedElement)element).getName();
    PsiElement namedElement = element instanceof CfmlNamedAttributeImpl ? element.getParent() : element;


    // if declared after using
    /*
    if (myElement.getContainingFile() == element.getContainingFile()) {
      if (myElement.getTextRange().getStartOffset() < element.getTextRange().getStartOffset()) {
        return true;
      }
    }
    */
    if (!CfmlScopesInfo.isConvenient(namedElement, myScope))   {
      return true;
    }

    // continue if a field or a class
    if (namedElement instanceof PsiClass) {
      return true;
    }

    // continue if element is hidden (has private modifier, package_local or protected) (?)
    if (namedElement instanceof PsiModifierListOwner owner) {
      if (owner.hasModifierProperty(PRIVATE) || owner.hasModifierProperty(PACKAGE_LOCAL) || owner.hasModifierProperty(PROTECTED)) {
        return true;
      }
    }

    boolean isJavaMethodCall = namedElement instanceof PsiMethod;
    if (isJavaMethodCall) {
      final PsiMethod method = (PsiMethod)namedElement;
      if (method.isConstructor()) {
        final PsiClass methodClass = method.getContainingClass();
        if (methodClass == null) {
          return true;
        }
        if (myStaticScopeFlag &&
            (methodClass.equals(myInitialClass) || !myWasConstructorFound) &&
            (myIsForCompletion || "init".equals(myReferenceName))) {
          myWasConstructorFound = true;
          if (!methodClass.equals(myInitialClass) && !myIsForCompletion) {
            addIfNotNull(myResult, execute(myInitialClass, false));
          }
          else {
            addIfNotNull(myResult, execute(method, myMethods.getResolveError() == JavaMethodResolveHelper.ErrorType.RESOLVE));
          }
          return true;
        }
      }
    }

    if (namedElement instanceof PsiModifierListOwner owner && myStaticScopeFlag && !owner.hasModifierProperty(STATIC)) {
      return true;
    }

    boolean isMyMethodCall = namedElement instanceof CfmlFunction;

    // continue if names differ
    if (!myIsForCompletion) {
      final String referenceNameLoweCase = StringUtil.toLowerCase(myReferenceName);
      if (myIsMethodCall &&
          (referenceNameLoweCase.startsWith("get") || referenceNameLoweCase.startsWith("set")) &&
          referenceNameLoweCase.substring(3).equalsIgnoreCase(elementName)
        ) {
        if (!referenceNameLoweCase.startsWith("get") || methodCallArity() == 0) {
          addIfNotNull(myResult, execute((PsiNamedElement)element, false));
        }
        return myResult.isEmpty();
      }
      if (!referenceNameLoweCase.equalsIgnoreCase(elementName)) {
        return true;
      }
    }

    // continue if not the same type as parent
    if (!myIsForCompletion && (isJavaMethodCall || isMyMethodCall) != myIsMethodCall) {
      return true;
    }

    if (isJavaMethodCall) {
      myMethods.addMethod((PsiMethod)namedElement, state.get(PsiSubstitutor.KEY), false);
      return true;
    }

    T execute = execute((PsiNamedElement)element, false);
    if (execute != null) {
      addIfNotNull(myResult, execute);
      if (myIsForCompletion || myResult.isEmpty()) {
        return true;
      } else if (namedElement instanceof CfmlVariable) {
        return !((CfmlVariable)namedElement).isTrulyDeclaration();
      }
      return false;
    }
    return true;
  }

  private int methodCallArity() {
    if (!myIsMethodCall) return 0;
    final CfmlArgumentList argumentList = ((CfmlFunctionCall)myElement.getParent()).findArgumentList();
    if (argumentList != null) {
      return argumentList.getArguments().length;
    }
    return 0;
  }

  protected abstract @Nullable T execute(final PsiNamedElement element, final boolean error);

  public T[] getVariants(T[] array) {
    if (myMethods != null) {
      for (final JavaMethodCandidateInfo method : myMethods.getMethods()) {
        T execute = execute(method.getMethod(), myMethods.getResolveError() == JavaMethodResolveHelper.ErrorType.RESOLVE);
        if (execute != null) {
          addIfNotNull(myResult, execute);
        }
      }
    }
    return myResult.toArray(array);
  }
}
