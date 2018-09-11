// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptParameterImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.angular2.index.Angular2IndexingHandler.isDirective;

public class Angular2RefUtil {

  @Nullable
  public static TypeScriptClass getParentClass(@Nullable PsiElement element) {
    if (element instanceof TypeScriptParameterImpl) {
      element = PsiTreeUtil.getParentOfType(element, TypeScriptFunction.class);
    }
    if (element instanceof TypeScriptField && element.getParent() != null) {
      return ObjectUtils.tryCast(element.getParent().getParent(), TypeScriptClass.class);
    }
    else if (element instanceof TypeScriptFunction) {
      return ObjectUtils.tryCast(element.getParent(), TypeScriptClass.class);
    }
    return null;
  }

  @Nullable
  public static HtmlFileImpl findAngularComponentTemplate(@NotNull TypeScriptClass cls) {
    if (cls.getAttributeList() == null) {
      return null;
    }
    JSAttributeList list = cls.getAttributeList();
    for (ES6Decorator decorator : PsiTreeUtil.getChildrenOfTypeAsList(list, ES6Decorator.class)) {
      JSCallExpression call = ObjectUtils.tryCast(decorator.getExpression(), JSCallExpression.class);
      if (call != null
          && call.getMethodExpression() instanceof JSReferenceExpression
          && isDirective(((JSReferenceExpression)call.getMethodExpression()).getReferenceName())
          && call.getArguments().length == 1
          && call.getArguments()[0] instanceof JSObjectLiteralExpression) {

        JSObjectLiteralExpression props = (JSObjectLiteralExpression)call.getArguments()[0];
        for (JSProperty property : props.getProperties()) {
          if ("templateUrl".equals(property.getName())
              && property.getValue() != null) {
            for (PsiReference ref : property.getValue().getReferences()) {
              PsiElement el = ref.resolve();
              if (el instanceof HtmlFileImpl) {
                return (HtmlFileImpl)el;
              }
            }
            break;
          }
          if ("template".equals(property.getName())
              && property.getValue() != null) {
            List<Pair<PsiElement, TextRange>> injections =
              InjectedLanguageManager.getInstance(cls.getProject()).getInjectedPsiFiles(property.getValue());
            if (injections != null) {
              for (Pair<PsiElement, TextRange> injection : injections) {
                if (injection.getFirst() instanceof HtmlFileImpl) {
                  return (HtmlFileImpl)injection.getFirst();
                }
              }
            }
            break;
          }
        }
        break;
      }
    }
    return null;
  }

  public static boolean isPrivateMember(JSPsiElementBase element) {
    if (element instanceof JSAttributeListOwner) {
      JSAttributeListOwner attributeListOwner = (JSAttributeListOwner)element;
      return attributeListOwner.getAttributeList() != null
             && attributeListOwner.getAttributeList().getAccessType() == JSAttributeList.AccessType.PRIVATE;
    }
    return false;
  }
}
