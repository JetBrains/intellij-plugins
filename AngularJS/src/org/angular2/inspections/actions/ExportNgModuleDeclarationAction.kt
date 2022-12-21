// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.actions;

import com.intellij.codeInsight.hint.QuestionAction;
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil;
import com.intellij.lang.javascript.modules.imports.JSImportCandidate;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.ObjectUtils;
import one.util.streamex.StreamEx;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.entities.source.Angular2SourceDeclaration;
import org.angular2.entities.source.Angular2SourceModule;
import org.angular2.inspections.quickfixes.Angular2FixesPsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.angular2.Angular2DecoratorUtil.EXPORTS_PROP;

public class ExportNgModuleDeclarationAction implements QuestionAction {

  private final @Nullable Editor myEditor;
  private final @NotNull PsiElement myContext;
  private final @NotNull SmartPsiElementPointer<ES6Decorator> myDecorator;
  private final @NotNull @Nls String myName;
  private final boolean myCodeCompletion;
  private final NotNullLazyValue<NgModuleImportAction> myImportAction;

  ExportNgModuleDeclarationAction(@Nullable Editor editor,
                                  @NotNull PsiElement context,
                                  @NotNull SmartPsiElementPointer<ES6Decorator> decorator,
                                  @NotNull @Nls String actionName,
                                  boolean codeCompletion) {
    myEditor = editor;
    myContext = context;
    myDecorator = decorator;
    myName = actionName;
    myCodeCompletion = codeCompletion;
    myImportAction = NotNullLazyValue.createValue(
      () -> Angular2ActionFactory.createNgModuleImportAction(myEditor, myContext, myCodeCompletion));
  }

  @Override
  public boolean execute() {
    if (addExport()) {
      myImportAction.getValue().executeForAllVariants();
    }
    return true;
  }

  public List<? extends JSImportCandidate> getCandidates() {
    return myImportAction.getValue().getRawCandidates();
  }

  protected boolean addExport() {
    ApplicationManager.getApplication().assertIsDispatchThread();

    PsiElement element = myDecorator.getElement();
    Ref<Boolean> result = new Ref<>(false);
    CommandProcessor.getInstance().executeCommand(
      myContext.getProject(),
      () -> {
        if (!myContext.isValid() || element == null || !element.isValid()) {
          return;
        }
        result.set(executeFor(element));
      },
      myName,
      this
    );
    return result.get() == Boolean.TRUE;
  }

  private boolean executeFor(@NotNull PsiElement element) {
    return WriteAction.<Boolean, RuntimeException>compute(() -> {
      Angular2SourceDeclaration declaration = ObjectUtils.tryCast(Angular2EntitiesProvider.getDeclaration(element),
                                                                  Angular2SourceDeclaration.class);
      if (declaration == null) {
        return null;
      }
      String className = declaration.getTypeScriptClass().getName();
      if (className == null) {
        return false;
      }
      Angular2SourceModule module = Angular2EntityUtils.defaultChooseModule(
        StreamEx.of(declaration.getAllDeclaringModules()).select(Angular2SourceModule.class));
      if (module == null) {
        return false;
      }
      ES6ImportPsiUtil.insertJSImport(module.getDecorator(), className, declaration.getTypeScriptClass(), myEditor);
      return Angular2FixesPsiUtil.insertEntityDecoratorMember(module, EXPORTS_PROP, className);
    }) == Boolean.TRUE;
  }
}
