// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.resolve;

import com.intellij.lang.actionscript.psi.ActionScriptPsiImplUtil;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSNamespaceDeclaration;
import com.intellij.lang.javascript.psi.ecmal4.JSUseNamespaceDirective;
import com.intellij.lang.javascript.psi.ecmal4.impl.ActionScriptAttributeListImpl;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSPackageStatementImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ActionScriptFlexPsiImplUtil {
  private static final ResolveProcessor.ProcessingOptions ourNsProcessingOptions =
    new JSResolveUtil.StructureProcessingOptions() {
      @Override
      public boolean toProcessPackageImports(PsiElement lastParent, JSPackageStatementImpl packageStatement) {
        return lastParent != null;
      }
    };

  public static @Nullable String calcNamespaceReference(PsiElement element) {
    String nsName = null;
    PsiElement startFrom = null;

    if (element instanceof ActionScriptAttributeListImpl) {
      String s = ((ActionScriptAttributeListImpl)element).tryResolveNamespaceValueFromStub();
      if (s != null) return s;
      nsName = ActionScriptPsiImplUtil.getNamespace((JSAttributeList)element);
      startFrom = JSResolveUtil.findParent(element.getParent());
    }
    else if (element instanceof JSReferenceExpression) {
      JSReferenceExpression namespaceElement = ((JSReferenceExpression)element).getNamespaceElement();
      if (namespaceElement == null) return null;
      nsName = namespaceElement.getReferenceName();
    }
    else if (element instanceof JSUseNamespaceDirective) {
      JSReferenceExpression namespaceElement = ((JSUseNamespaceDirective)element).getNamespaceReference();
      if (namespaceElement == null) return null;
      nsName = namespaceElement.getReferenceName();
    }

    if (nsName == null) return null;

    final Ref<String> nsValueCalculated = new Ref<>();

    ActionScriptFlexResolveUtil.StructureResolveProcessor processor = new ActionScriptFlexResolveUtil.StructureResolveProcessor(nsName) {
      {
        setLocalResolve(false);
        setForceImportsForPlace(true);
        setProcessingOptions(ourNsProcessingOptions);
      }

      @Override
      public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        String nsValue = null;
        if (element instanceof JSNamespaceDeclaration) {
          nsValue = ((JSNamespaceDeclaration)element).getInitialValueString();
          if (nsValue == null) {
            nsValue = ((JSNamespaceDeclaration)element).getName();
          }
        }
        else if (element instanceof JSVariable var) {
          if (var.isConst()) {
            nsValue = var.getLiteralOrReferenceInitializerText();
          }
          else {
            nsValue = JSAttributeList.UNKNOWN_NAMESPACE;
          }
        }

        if (nsValue != null) {
          nsValueCalculated.set(StringUtil.unquoteString(nsValue));
          return false;
        }
        return true;
      }
    };

    PsiElement context = startFrom != null ? startFrom : element;
    ActionScriptFlexResolveUtil.walkOverStructure(context, processor);

    String result = nsValueCalculated.get();
    if (JSAttributeList.UNKNOWN_NAMESPACE.equals(result)) {
      return null;
    }

    if (result == null) {
      result = nsName;
    }

    return result;
  }

}
