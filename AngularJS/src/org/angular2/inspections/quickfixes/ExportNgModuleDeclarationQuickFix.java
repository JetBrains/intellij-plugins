// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.Consumer;
import com.intellij.util.ObjectUtils;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.source.Angular2SourceDeclaration;
import org.angular2.entities.source.Angular2SourceModule;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.angular2.Angular2DecoratorUtil.EXPORTS_PROP;

public class ExportNgModuleDeclarationQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {

  private final String myDeclarationName;
  private final SmartPsiElementPointer<ES6Decorator> myDeclarationDecorator;

  public ExportNgModuleDeclarationQuickFix(@NotNull PsiElement context,
                                           @NotNull Angular2SourceDeclaration declaration) {
    super(context);
    myDeclarationName = declaration.getTypeScriptClass().getName();
    myDeclarationDecorator = SmartPointerManager.createPointer(declaration.getDecorator());
  }

  @NotNull
  @Override
  public String getText() {
    return "Export " + myDeclarationName + " and import NgModule";
  }

  @Nls(capitalization = Nls.Capitalization.Sentence)
  @NotNull
  @Override
  public String getFamilyName() {
    return "Angular";
  }

  @Override
  public void invoke(@NotNull Project project,
                     @NotNull PsiFile file,
                     @Nullable("is null when called from inspection") Editor editor,
                     @NotNull PsiElement startElement,
                     @NotNull PsiElement endElement) {
    if (myDeclarationDecorator.getElement() == null) return;
    ExportNgModuleDeclarationAction action = new ExportNgModuleDeclarationAction(
      editor, startElement, myDeclarationDecorator, getText());
    List<JSElement> candidates = action.getCandidates();
    if (candidates.size() == 1 || editor != null) {
      action.execute();
    }
  }

  private static class ExportNgModuleDeclarationAction extends AddNgModuleImportQuickFix.NgModuleImportAction {

    private final SmartPsiElementPointer<ES6Decorator> myDecorator;

    ExportNgModuleDeclarationAction(@Nullable Editor editor,
                                    @NotNull PsiElement context,
                                    @NotNull SmartPsiElementPointer<ES6Decorator> decorator,
                                    @NotNull String actionName) {
      super(editor, context, actionName);
      myDecorator = decorator;
    }

    @Override
    public void executeForAllVariants(@Nullable Consumer<? super JSElement> postProcess) {
      if (addExport()) {
        super.executeForAllVariants(postProcess);
      }
    }

    protected boolean addExport() {
      ApplicationManager.getApplication().assertIsDispatchThread();
      return WriteAction.<Boolean, RuntimeException>compute(() -> {
        Angular2SourceDeclaration declaration = ObjectUtils.tryCast(Angular2EntitiesProvider.getDeclaration(myDecorator.getElement()),
                                                                    Angular2SourceDeclaration.class);
        if (declaration == null) {
          return null;
        }
        String className = declaration.getTypeScriptClass().getName();
        if (className == null) {
          return false;
        }
        Angular2SourceModule module = ObjectUtils.tryCast(declaration.getModule(), Angular2SourceModule.class);
        if (module == null) {
          return false;
        }
        ES6ImportPsiUtil.insertJSImport(module.getDecorator(), className, declaration.getTypeScriptClass(), myEditor);
        return Angular2FixesPsiUtil.insertNgModuleMember(module, EXPORTS_PROP, className);
      }) == Boolean.TRUE;
    }
  }
}
