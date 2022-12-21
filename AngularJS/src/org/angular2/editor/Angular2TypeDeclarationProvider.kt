// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.codeInsight.navigation.SymbolTypeProvider;
import com.intellij.codeInsight.navigation.actions.TypeDeclarationProvider;
import com.intellij.webSymbols.WebSymbol;
import com.intellij.model.Symbol;
import com.intellij.model.psi.PsiSymbolService;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveSelectorSymbol;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.web.Angular2WebSymbolsQueryConfigurator.PROP_SYMBOL_DIRECTIVE;

public class Angular2TypeDeclarationProvider implements TypeDeclarationProvider, SymbolTypeProvider {

  @Override
  public PsiElement @Nullable [] getSymbolTypeDeclarations(@NotNull PsiElement symbol) {
    Angular2Component component;
    if (symbol instanceof Angular2DirectiveSelectorSymbol
        && (component = Angular2EntitiesProvider.findComponent((Angular2DirectiveSelectorSymbol)symbol)) != null) {
      PsiFile htmlFile = component.getTemplateFile();
      if (htmlFile != null) {
        return new PsiElement[]{htmlFile};
      }
    }
    return null;
  }

  @Override
  public @Nullable String getActionText(@NotNull DataContext context) {
    List<Angular2Directive> directives = Angular2EditorUtils.getDirectivesAtCaret(context);
    if (ContainerUtil.find(directives, Angular2Directive::isComponent) != null) {
      return Angular2Bundle.message("angular.action.goto-type-declaration.component-template");
    }
    return null;
  }

  @Override
  public @NotNull List<? extends @NotNull Symbol> getSymbolTypes(@NotNull Symbol symbol) {
    if (symbol instanceof WebSymbol) {
      var webSymbol = (WebSymbol) symbol;
      var component = tryCast(webSymbol.getProperties().get(PROP_SYMBOL_DIRECTIVE), Angular2Component.class);
      if (component != null && component.isComponent()) {
        PsiFile htmlFile = component.getTemplateFile();
        if (htmlFile != null) {
          return Collections.singletonList(PsiSymbolService.getInstance().asSymbol(htmlFile));
        }
      }
    }
    return Collections.emptyList();
  }
}
