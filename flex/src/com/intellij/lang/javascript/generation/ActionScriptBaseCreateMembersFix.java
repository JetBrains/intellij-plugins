// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.generation;

import com.intellij.javascript.flex.resolve.ActionScriptFlexResolveUtil;
import com.intellij.lang.actionscript.psi.ActionScriptPsiImplUtil;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.index.JSPackageIndex;
import com.intellij.lang.javascript.index.JSPackageIndexInfo;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.intellij.lang.javascript.flex.FlexSupportLoader.ECMA_SCRIPT_L4;

public final class ActionScriptBaseCreateMembersFix {
  public static
  @Nullable
  String calcNamespaceId(@NotNull JSAttributeList attributeList, final String namespace, @NotNull PsiElement anchor) {
    if (namespace == null) return null;
    final Map<String, String> ns2Id = ActionScriptFlexResolveUtil.calculateOpenNses(anchor);

    String namespaceId = ns2Id.get(namespace);
    if (namespaceId != null) return namespaceId;

    final GlobalSearchScope searchScope = GlobalSearchScope.allScope(attributeList.getProject());
    final Ref<String> namespaceVar = new Ref<>();
    JSPackageIndex.processElementsInScopeRecursive("", new JSPackageIndex.PackageQualifiedElementsProcessor() {
      @Override
      public boolean process(String qualifiedName, JSPackageIndexInfo.Kind kind, boolean isPublic) {
        if (kind != JSPackageIndexInfo.Kind.VARIABLE) return true;
        final PsiElement classByQName = JSDialectSpecificHandlersFactory.forLanguage(ECMA_SCRIPT_L4).getClassResolver()
          .findClassByQName(qualifiedName, searchScope);
        if (classByQName instanceof JSVariable) {
          final String initializerText = ((JSVariable)classByQName).getLiteralOrReferenceInitializerText();
          if (initializerText != null && namespace.equals(StringUtil.stripQuotesAroundValue(initializerText))) {
            namespaceVar.set(((JSVariable)classByQName).getName());
            return false;
          }
        }
        return true;
      }
    }, searchScope, attributeList.getProject());
    namespaceId = namespaceVar.get();
    if (namespaceId == null) namespaceId = ActionScriptPsiImplUtil.getNamespace(attributeList);
    return namespaceId;
  }
}
