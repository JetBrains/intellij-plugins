// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.javascript.web.codeInsight.html.attributes.WebSymbolHtmlAttributeInfo;
import com.intellij.webSymbols.WebSymbolHtmlAttributeValueData;
import com.intellij.lang.documentation.DocumentationTarget;
import com.intellij.lang.javascript.documentation.JSDocumentationUtils;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment;
import com.intellij.model.Pointer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import one.util.streamex.StreamEx;
import org.angular2.entities.impl.TypeScriptElementDocumentationTarget;
import org.angular2.lang.types.Angular2TypeUtils;
import org.angular2.web.Angular2PsiSourcedSymbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.KIND_NG_DIRECTIVE_OUTPUTS;

public interface Angular2DirectiveProperty extends Angular2PsiSourcedSymbol, Angular2Element {

  @Override
  @NotNull
  String getName();

  @Nullable
  JSType getRawJsType();

  @Override
  boolean isVirtual();

  @NotNull
  @Override
  Pointer<? extends Angular2DirectiveProperty> createPointer();

  @NotNull
  @Override
  default PsiElement getSource() {
    return getSourceElement();
  }

  @Override
  default @NotNull Project getProject() {
    return getSourceElement().getProject();
  }

  @NotNull
  @Override
  default String getNamespace() {
    return NAMESPACE_JS;
  }

  @Nullable
  @Override
  default Priority getPriority() {
    return Priority.LOW;
  }

  @Nullable
  @Override
  default JSType getType() {
    if (getKind().equals(KIND_NG_DIRECTIVE_OUTPUTS)) {
      return Angular2TypeUtils.extractEventVariableType(getRawJsType());
    }
    else {
      return getRawJsType();
    }
  }

  @NotNull
  @Override
  default DocumentationTarget getDocumentationTarget() {
    if (hasNonPrivateDocComment(getSourceElement())) {
      return new TypeScriptElementDocumentationTarget(getName(), getSourceElement());
    }
    var clazz = PsiTreeUtil.getContextOfType(getSource(), TypeScriptClass.class);
    if (clazz != null) {
      return new TypeScriptElementDocumentationTarget(getName(), clazz);
    }
    return Angular2PsiSourcedSymbol.super.getDocumentationTarget();
  }

  @Nullable
  @Override
  default AttributeValue getAttributeValue() {
    if (WebSymbolHtmlAttributeInfo.isBooleanType(getType())) {
      return new WebSymbolHtmlAttributeValueData(
        null, null, false, null, null
      );
    }
    else {
      return null;
    }
  }

  static boolean hasNonPrivateDocComment(@NotNull PsiElement element) {
    var comment = JSDocumentationUtils.findDocComment(element);
    if (comment instanceof JSDocComment) {
      var jsDoc = (JSDocComment)comment;
      var privateTag = StreamEx.of(jsDoc.getTags()).findFirst(tag -> "docs-private".equals(tag.getName()));
      if (privateTag.isEmpty()) {
        return true;
      }
    }
    return false;
  }
}
