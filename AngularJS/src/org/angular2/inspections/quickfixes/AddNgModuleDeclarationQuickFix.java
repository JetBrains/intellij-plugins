// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil;
import com.intellij.lang.javascript.modules.ES6ImportAction;
import com.intellij.lang.javascript.modules.JSModuleNameInfo;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.Queue;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.Angular2Module;
import org.angular2.entities.source.Angular2SourceDeclaration;
import org.angular2.entities.source.Angular2SourceModule;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.angular2.Angular2DecoratorUtil.DECLARATIONS_PROP;
import static org.angular2.Angular2DecoratorUtil.EXPORTS_PROP;

public class AddNgModuleDeclarationQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {

  private final String myDeclarationName;
  private final SmartPsiElementPointer<ES6Decorator> myDeclarationDecorator;
  private final String myModuleName;

  public AddNgModuleDeclarationQuickFix(@NotNull PsiElement context,
                                        @NotNull Angular2SourceDeclaration declaration) {
    super(context);
    myDeclarationName = declaration.getTypeScriptClass().getName();
    myDeclarationDecorator = SmartPointerManager.createPointer(declaration.getDecorator());

    List<Angular2Module> candidates = getCandidates(context);
    if (candidates.size() == 1) {
      myModuleName = candidates.get(0).getName();
    }
    else {
      myModuleName = null;
    }
  }

  @NotNull
  @Override
  public String getText() {
    return Angular2Bundle.message(myModuleName == null ? "angular.quickfix.ngmodule.declare.name.choice"
                                                       : "angular.quickfix.ngmodule.declare.name",
                                  myDeclarationName, myModuleName);
  }

  @Nls(capitalization = Nls.Capitalization.Sentence)
  @NotNull
  @Override
  public String getFamilyName() {
    return Angular2Bundle.message("angular.quickfix.family");
  }

  @Override
  public void invoke(@NotNull Project project,
                     @NotNull PsiFile file,
                     @Nullable Editor editor,
                     @NotNull PsiElement startElement,
                     @NotNull PsiElement endElement) {
    if (myDeclarationDecorator.getElement() == null) return;
    AddNgModuleDeclarationAction action = new AddNgModuleDeclarationAction(
      editor, startElement, myDeclarationDecorator, myDeclarationName, getText());
    List<JSElement> candidates = action.getCandidates();
    if (candidates.size() == 1 || editor != null) {
      action.execute();
    }
  }

  private static List<Angular2Module> getCandidates(@NotNull PsiElement context) {
    Queue<Angular2Module> processingQueue = new Queue<>(20);
    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(context);
    Angular2Module contextModule = scope.getModule();
    if (contextModule == null || !scope.isInSource(contextModule)) {
      return Collections.emptyList();
    }
    processingQueue.addLast(contextModule);
    Set<Angular2Module> processed = new HashSet<>();
    List<Angular2Module> result = new ArrayList<>();
    while (!processingQueue.isEmpty()) {
      Angular2Module module = processingQueue.pullFirst();
      if (processed.add(module) && scope.isInSource(module)) {
        result.add(module);
        module.getImports().forEach(processingQueue::addLast);
        for (Angular2Entity entity : module.getExports()) {
          if (entity instanceof Angular2Module) {
            processingQueue.addLast((Angular2Module)entity);
          }
        }
      }
    }
    return result;
  }

  private static class AddNgModuleDeclarationAction extends ES6ImportAction {

    @NotNull private final String myDeclarationName;
    @NotNull private final String myActionName;
    @NotNull private final SmartPsiElementPointer<ES6Decorator> myDecorator;

    AddNgModuleDeclarationAction(@Nullable Editor editor,
                                 @NotNull PsiElement context,
                                 @NotNull SmartPsiElementPointer<ES6Decorator> declarationDecorator,
                                 @NotNull String declarationName,
                                 @NotNull String actionName) {
      super(editor, context, "", DEFAULT_FILTER);
      myDeclarationName = declarationName;
      myActionName = actionName;
      myDecorator = declarationDecorator;
    }

    @Override
    protected String getModuleSelectionPopupTitle() {
      return Angular2Bundle.message("angular.quickfix.ngmodule.declare.select", myDeclarationName);
    }

    @NotNull
    @Override
    public String getName() {
      return myActionName;
    }

    @NotNull
    @Override
    public List<JSElement> getCandidates() {
      Angular2SourceDeclaration declaration = ObjectUtils.tryCast(
        Angular2EntitiesProvider.getDeclaration(myDecorator.getElement()),
        Angular2SourceDeclaration.class);
      if (myContext == null || declaration == null) {
        return Collections.emptyList();
      }
      return StreamEx.of(AddNgModuleDeclarationQuickFix.getCandidates(myContext))
        .map(Angular2Entity::getTypeScriptClass)
        .select(JSElement.class)
        .toList();
    }

    @NotNull
    @Override
    protected List<JSElement> getFinalElements(@NotNull Project project,
                                               @NotNull PsiFile file,
                                               @NotNull List<JSElement> candidates,
                                               @NotNull Collection<JSElement> elementsFromLibraries,
                                               @NotNull Map<PsiElement, JSModuleNameInfo> renderedTexts) {
      return candidates;
    }

    @Override
    protected void runAction(@Nullable Editor editor,
                             @NotNull String ignored,
                             @NotNull JSElement moduleClassToDeclareIn,
                             @NotNull PsiElement place) {
      if (myContext == null || !myContext.isValid()) {
        return;
      }
      Angular2SourceModule module = ObjectUtils.tryCast(Angular2EntitiesProvider.getModule(moduleClassToDeclareIn),
                                                        Angular2SourceModule.class);
      Angular2SourceDeclaration declaration = ObjectUtils.tryCast(Angular2EntitiesProvider.getDeclaration(myDecorator.getElement()),
                                                                  Angular2SourceDeclaration.class);

      Angular2DeclarationsScope scope = new Angular2DeclarationsScope(myContext);
      Angular2Module contextModule = scope.getModule();
      if (module == null || declaration == null || contextModule == null) {
        return;
      }

      WriteAction.run(() -> {
        ES6ImportPsiUtil.insertJSImport(module.getTypeScriptClass(), myDeclarationName, declaration.getTypeScriptClass(), editor);
        Angular2FixesPsiUtil.insertNgModuleMember(module, DECLARATIONS_PROP, myDeclarationName);
        if (contextModule != module) {
          Angular2FixesPsiUtil.insertNgModuleMember(module, EXPORTS_PROP, myDeclarationName);
        }
      });
    }
  }
}
