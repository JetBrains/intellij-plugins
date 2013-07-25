/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.CfmlScopesInfo;
import com.intellij.coldFusion.model.psi.impl.CfmlNamedAttributeImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlTagInvokeImpl;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.resolve.JavaMethodCandidateInfo;
import com.intellij.psi.resolve.JavaMethodResolveHelper;
import com.intellij.psi.scope.BaseScopeProcessor;
import com.intellij.psi.scope.JavaScopeProcessorEvent;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.intellij.psi.PsiModifier.*;
import static com.intellij.util.containers.ContainerUtil.addIfNotNull;

abstract public class CfmlVariantsProcessor<T> extends BaseScopeProcessor {

  public static class CfmlProcessorEvent implements PsiScopeProcessor.Event {
    private CfmlProcessorEvent() {
    }

    public static final CfmlProcessorEvent SET_INITIAL_CLASS = new CfmlProcessorEvent();
  }

  private final Set<T> myResult = new LinkedHashSet<T>();
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
    myReferenceName = referenceName != null ? referenceName.toLowerCase() : null;
    myIsMethodCall = parent instanceof CfmlFunctionCallExpression || parent instanceof CfmlTagInvokeImpl;
    if (parent instanceof CfmlFunctionCallExpression && !myIsForCompletion) {
      final PsiType[] parameterTypes = ((CfmlFunctionCallExpression)parent).getArgumentTypes();
      myMethods = new JavaMethodResolveHelper(parent, parameterTypes);
    }
    else {
      myMethods = new JavaMethodResolveHelper(parent, null);
    }
  }

  @Override
  public void handleEvent(Event event, Object associated) {
    if (event == JavaScopeProcessorEvent.START_STATIC) {
      myStaticScopeFlag = true;
    }
    else if (event == CfmlProcessorEvent.SET_INITIAL_CLASS && associated instanceof PsiClass) {
      myInitialClass = (PsiClass)associated;
      myWasConstructorFound = false;
    }
  }

  public boolean execute(@NotNull final PsiElement element, final ResolveState state) {
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
    if (namedElement instanceof PsiModifierListOwner) {
      final PsiModifierListOwner owner = (PsiModifierListOwner)namedElement;
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
            addIfNotNull(execute(myInitialClass, false), myResult);
            return true;
          }
          else {
            addIfNotNull(execute(method, myMethods.getResolveError() == JavaMethodResolveHelper.ErrorType.RESOLVE), myResult);
            return true;
          }
        }
      }
    }

    if (namedElement instanceof PsiModifierListOwner) {
      final PsiModifierListOwner owner = (PsiModifierListOwner)namedElement;
      if (myStaticScopeFlag && !owner.hasModifierProperty(STATIC)) {
        return true;
      }
    }

    boolean isMyMethodCall = namedElement instanceof CfmlFunction;

    // continue if names differ
    if (!myIsForCompletion) {
      if (myIsMethodCall && (myReferenceName.toLowerCase().startsWith("get") || myReferenceName.toLowerCase().startsWith("set")) &&
        myReferenceName.substring(3).equalsIgnoreCase(elementName.toLowerCase())) {
        addIfNotNull(execute((PsiNamedElement)element,  false), myResult);
        return myResult.size() == 0;
      }
      if (!myReferenceName.toLowerCase().equals(elementName.toLowerCase())) {
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
      addIfNotNull(execute, myResult);
      if (myIsForCompletion || myResult.size() == 0) {
        return true;
      } else if (namedElement instanceof CfmlVariable) {
        return !((CfmlVariable)namedElement).isTrulyDeclaration();
      }
      return false;
    }
    return true;
  }

  @Nullable
  protected abstract T execute(final PsiNamedElement element, final boolean error);

  public T[] getVariants(T[] array) {
    if (myMethods != null) {
      for (final JavaMethodCandidateInfo method : myMethods.getMethods()) {
        T execute = execute(method.getMethod(), myMethods.getResolveError() == JavaMethodResolveHelper.ErrorType.RESOLVE);
        if (execute != null) {
          addIfNotNull(execute, myResult);
        }
      }
    }
    return myResult.toArray(array);
  }
}
