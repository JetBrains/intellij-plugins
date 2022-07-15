// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.actions;

import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil;
import com.intellij.lang.javascript.modules.imports.ES6ImportCandidate;
import com.intellij.lang.javascript.modules.imports.JSImportCandidate;
import com.intellij.lang.javascript.modules.imports.JSImportCandidateWithExecutor;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.Angular2Module;
import org.angular2.entities.source.Angular2SourceDeclaration;
import org.angular2.entities.source.Angular2SourceModule;
import org.angular2.inspections.quickfixes.AddNgModuleDeclarationQuickFix;
import org.angular2.inspections.quickfixes.Angular2FixesPsiUtil;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.Angular2DecoratorUtil.DECLARATIONS_PROP;
import static org.angular2.Angular2DecoratorUtil.EXPORTS_PROP;

public class AddNgModuleDeclarationAction extends Angular2NgModuleSelectAction {

  private final @NotNull String myDeclarationName;
  private final @NotNull SmartPsiElementPointer<ES6Decorator> myDecorator;

  AddNgModuleDeclarationAction(@Nullable Editor editor,
                               @NotNull PsiElement context,
                               @NotNull SmartPsiElementPointer<ES6Decorator> declarationDecorator,
                               @NotNull String declarationName,
                               @NotNull @NlsContexts.Command String actionName,
                               boolean codeCompletion) {
    super(editor, context, "", actionName, codeCompletion);
    myDeclarationName = declarationName;
    myDecorator = declarationDecorator;
  }

  @Override
  protected String getModuleSelectionPopupTitle() {
    return Angular2Bundle.message("angular.quickfix.ngmodule.declare.select.module", myDeclarationName);
  }

  @Override
  public @NotNull List<? extends JSImportCandidate> getRawCandidates() {
    Angular2SourceDeclaration declaration = tryCast(
      Angular2EntitiesProvider.getDeclaration(myDecorator.getElement()),
      Angular2SourceDeclaration.class);
    if (declaration == null) {
      return Collections.emptyList();
    }
    return StreamEx.of(AddNgModuleDeclarationQuickFix.getCandidateModules(getContext()))
      .map(Angular2Entity::getTypeScriptClass)
      .select(JSElement.class)
      .map(el -> new ES6ImportCandidate(myName, el, getContext()))
      .toList();
  }

  @Override
  protected @NotNull List<? extends JSImportCandidate> filter(@NotNull List<? extends JSImportCandidate> candidates) {
    return candidates;
  }

  @Override
  protected void runAction(@Nullable Editor editor,
                           @NotNull JSImportCandidateWithExecutor candidate,
                           @NotNull PsiElement place) {
    PsiElement context = getContext();
    if (!context.isValid()) {
      return;
    }
    JSElement element = candidate.getElement();
    Angular2SourceModule module = tryCast(Angular2EntitiesProvider.getModule(element),
                                          Angular2SourceModule.class);
    Angular2SourceDeclaration declaration = tryCast(Angular2EntitiesProvider.getDeclaration(myDecorator.getElement()),
                                                    Angular2SourceDeclaration.class);

    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(context);
    Angular2Module contextModule = tryCast(scope.getImportsOwner(), Angular2Module.class);
    if (module == null || declaration == null || contextModule == null) {
      return;
    }

    WriteAction.run(() -> {
      ES6ImportPsiUtil.insertJSImport(module.getTypeScriptClass(), myDeclarationName, declaration.getTypeScriptClass(), editor);
      Angular2FixesPsiUtil.insertEntityDecoratorMember(module, DECLARATIONS_PROP, myDeclarationName);
      if (contextModule != module) {
        Angular2FixesPsiUtil.insertEntityDecoratorMember(module, EXPORTS_PROP, myDeclarationName);
      }
    });
  }
}
