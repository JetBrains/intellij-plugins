// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.lang.javascript.psi.types.JSNamedType;
import com.intellij.lang.javascript.psi.types.evaluable.JSCustomElementType;
import com.intellij.lang.javascript.psi.types.evaluable.JSTypeOfPsiElementBase;
import com.intellij.lang.javascript.psi.types.primitives.JSObjectType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.psi.resolve.AccessibilityProcessingHandler.processWithStatic;

public class ActionScriptQualifiedItemProcessor<T extends ResultSink> extends QualifiedItemProcessor<T> {
  public ActionScriptQualifiedItemProcessor(@NotNull T sink, @NotNull PsiFile containingFile) {
    super(sink, containingFile);
  }

  @Override
  public void process(@NotNull JSType type, @NotNull JSEvaluateContext evaluateContext) {
    if (type instanceof JSAnyType) {
      boolean currentIsNotResolved = isActionScriptDummyResolve();
      resolved = currentIsNotResolved ? TypeResolveState.PrefixUnknown : TypeResolveState.Unknown;
    }

    if (type instanceof JSCustomElementType) {
      PsiElement element = ((JSTypeOfPsiElementBase<?>)type).getElement();
      if (element instanceof JSPackage) {
        element.processDeclarations(this, ResolveState.initial(), null, element);
        forceResolvedState();
      }
    }

    if (!JSTypeUtils.processExpandedType(this, type, evaluateContext)) return;

    processActionScriptClass(type, evaluateContext);
  }

  private boolean isActionScriptDummyResolve() {
    if (place instanceof JSReferenceExpressionImpl) {
      JSExpression originalQualifier = BaseJSSymbolProcessor.getOriginalQualifier(((JSReferenceExpressionImpl)place).getQualifier());
      if (originalQualifier instanceof JSCallExpression) originalQualifier = ((JSCallExpression)originalQualifier).getMethodExpression();
      if (originalQualifier instanceof JSReferenceExpression &&
          ((JSReferenceExpression)originalQualifier).multiResolve(false).length == 0) {
        return true;
      }
    }
    return false;
  }

  private void processActionScriptClass(@NotNull JSType type,
                                        @NotNull JSEvaluateContext evaluateContext) {
    String typeString = JSTypeUtils.getQualifiedNameMatchingType(type, true);
    if (type instanceof JSAnyType || typeString == null) {
      return;
    }

    final PsiElement placeParent = place.getParent();
    boolean setTypeContext = placeParent instanceof JSReferenceListMember;
    PsiElement clazz = JSClassResolver.findClassFromNamespace(typeString, place);
    if (!(clazz instanceof XmlBackedJSClass)) {
      JSClass resolvedFromType = type.resolveClass();
      if (clazz == null || resolvedFromType instanceof XmlBackedJSClass) {
        clazz = resolvedFromType;
      }
    }
    if (clazz instanceof JSClass finalClass) {
      PsiElement typeSource = evaluateContext.getSource();
      processWithStatic(this, false, () -> {
        addTypeFromClass(type, evaluateContext, typeString, typeSource, setTypeContext, finalClass);
        return true;
      });
    }
    if (type instanceof JSObjectType) {
      resolved = TypeResolveState.Unknown;
    }
    else if ((!(clazz instanceof JSQualifiedNamedElement)) && type.isSourceStrict()) {
      forceResolvedState();
    }
  }

  private void addTypeFromClass(JSType type,
                                JSEvaluateContext evaluateContext,
                                String typeString,
                                PsiElement typeSource,
                                boolean setTypeContext, JSQualifiedNamedElement jsClass) {
    if ("RemoteObject".equals(jsClass.getName()) &&
        typeSource instanceof JSOffsetBasedImplicitElement &&
        ((JSOffsetBasedImplicitElement)typeSource).getType() == JSImplicitElement.Type.Tag) {
      final XmlTag tag = PsiTreeUtil.getParentOfType(((JSOffsetBasedImplicitElement)typeSource).getElementAtOffset(), XmlTag.class);
      for (XmlTag method : tag.findSubTags("method", tag.getNamespace())) {
        if (!execute(method, ResolveState.initial())) break;
      }

      forceResolvedState();
      return;
    }

    boolean statics = false;
    JSReferenceExpression evaluateQualifier;
    if (JSPsiImplUtils.isTheSameClass(typeSource, jsClass) &&
        (evaluateQualifier = evaluateContext.getProcessedExpression()) != null) {
      //  AAA(), (AAA)(), ((AAA))()  is not static context
      JSExpression placeQualifier = evaluateQualifier.getQualifier();
      if (!(placeQualifier instanceof JSCallExpression) &&
          !(evaluateQualifier.getParent() instanceof JSCallExpression) &&
          !(evaluateQualifier.getParent() instanceof JSParenthesizedExpression) &&
          !(placeQualifier instanceof JSParenthesizedExpression)
      ) {
        statics = true;
      }
    }
    if (type instanceof JSNamedType && ((JSNamedType)type).isStaticOrInstance() == JSContext.STATIC) {
      statics = true;
    }

    getAccessibilityProcessingHandler().setProcessStatics(statics);
    if (statics) {
      setTypeName(jsClass.getQualifiedName());
    }

    final boolean saveSetTypeContext = isTypeContext();
    final boolean saveToProcessMembers = isToProcessMembers();

    if (setTypeContext) {
      setTypeContext(setTypeContext);
      setToProcessMembers(false);
    }

    try {
      if (!resolved.isResolved() && ("XML".equals(typeString) || "XMLList".equals(typeString))) {
        resolved = TypeResolveState.PrefixUnknown; // TODO: fix when we start to index xml literals!
      }

      if (!jsClass.processDeclarations(this, ResolveState.initial(), jsClass, place)) {
        forceResolvedState();
      }

      final JSAttributeList attrList = ((JSAttributeListOwner)jsClass).getAttributeList();
      if (attrList == null || !attrList.hasModifier(JSAttributeList.ModifierType.DYNAMIC)) {
        forceResolvedState();
      }
    }
    finally {
      if (setTypeContext) {
        setTypeContext(saveSetTypeContext);
        setToProcessMembers(saveToProcessMembers);
      }
    }
  }

  @Override
  public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
    boolean b = needPackages() && !(element instanceof JSPackage) || super.execute(element, state);
    if (getResult() != null) forceResolvedState();
    return b;
  }

  @Override
  public void prefixResolved() {
    super.prefixResolved();
    forceResolvedState();
  }

  @Override
  protected void elementIsNotAccessible(PsiElement element) {
    super.elementIsNotAccessible(element);
    forceResolvedState();
  }

  @Override
  public boolean noMoreResultsPossible() {
    return resolved == TypeResolveState.Resolved ||
           resolved == TypeResolveState.PrefixUnknown;
  }
}
