// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.template;

import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionSignature;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.Angular2ComponentPropertyResolveResult;
import org.angular2.entities.Angular2ComponentLocator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class Angular2ComponentScopeProvider extends Angular2TemplateScopesProvider {

  @Override
  public @NotNull List<? extends Angular2TemplateScope> getScopes(@NotNull PsiElement element, @Nullable PsiElement hostElement) {
    return Optional.ofNullable(Angular2ComponentLocator.findComponentClass(element))
      .map(Angular2ComponentScope::new)
      .map(Collections::singletonList)
      .orElseGet(Collections::emptyList);
  }

  private static final class Angular2ComponentScope extends Angular2TemplateScope {

    private final TypeScriptClass myClass;

    private Angular2ComponentScope(@NotNull TypeScriptClass aClass) {
      super(null);
      myClass = aClass;
    }

    @Override
    public void resolve(@NotNull Consumer<? super ResolveResult> consumer) {
      StreamEx.of(TypeScriptTypeParser
                    .buildTypeFromClass(myClass, false)
                    .getProperties())
        .mapToEntry(prop -> prop.getMemberSource().getAllSourceElements())
        .flatMapValues(Collection::stream)
        .selectValues(JSPsiElementBase.class)
        .filterValues(el -> !(el instanceof TypeScriptFunctionSignature))
        .filterValues(el -> !(el instanceof TypeScriptFunction) || !((TypeScriptFunction)el).isOverloadImplementation())
        .map(entry -> new Angular2ComponentPropertyResolveResult(entry.getValue(), entry.getKey()))
        .forEach(consumer);
    }
  }
}
