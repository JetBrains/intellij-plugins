// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil;
import com.intellij.lang.javascript.modules.ES6ImportAction;
import com.intellij.lang.javascript.modules.JSModuleNameInfo;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.MultiMap;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Module;
import org.angular2.entities.metadata.psi.Angular2MetadataModule;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.intellij.openapi.util.Pair.pair;
import static org.angular2.Angular2DecoratorUtil.IMPORTS_PROP;

public class AddNgModuleImportQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {

  private final String myModuleName;

  public AddNgModuleImportQuickFix(@NotNull PsiElement context,
                                   Collection<Angular2Declaration> declarations) {
    super(context);
    List<String> names = StreamEx.of(declarations)
      .flatCollection(new Angular2DeclarationsScope(context)::getPublicModulesExporting)
      .distinct()
      .map(Angular2Module::getName)
      .distinct()
      .toList();
    if (names.size() == 1) {
      myModuleName = names.get(0);
    }
    else {
      myModuleName = null;
    }
  }

  @NotNull
  @Override
  public String getText() {
    return Angular2Bundle.message(myModuleName == null ? "angular.quickfix.ngmodule.import.name.choice"
                                                       : "angular.quickfix.ngmodule.import.name",
                                  myModuleName);
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
    NgModuleImportAction action = new NgModuleImportAction(editor, startElement, getText());
    List<JSElement> candidates = action.getCandidates();
    if (candidates.size() == 1 || editor != null) {
      action.execute();
    }
  }

  static class NgModuleImportAction extends ES6ImportAction {

    @NotNull private final String myActionName;

    NgModuleImportAction(@Nullable Editor editor, @NotNull PsiElement element, @NotNull String actionName) {
      super(editor, element, "NgModule", DEFAULT_FILTER);
      myActionName = actionName;
    }

    @Override
    protected String getModuleSelectionPopupTitle() {
      return Angular2Bundle.message("angular.quickfix.ngmodule.import.select");
    }

    @NotNull
    @Override
    public String getName() {
      return myActionName;
    }

    @Override
    @NotNull
    public List<JSElement> getCandidates() {
      if (myContext == null) {
        return Collections.emptyList();
      }

      DistanceCalculator distanceCalculator = new DistanceCalculator();
      Angular2DeclarationsScope scope = new Angular2DeclarationsScope(myContext);

      MultiMap<DeclarationProximity, Angular2Declaration> candidates = Angular2FixesFactory.getCandidates(myContext);
      if (!candidates.get(DeclarationProximity.IN_SCOPE).isEmpty()) {
        return Collections.emptyList();
      }

      MultiMap<Angular2Module, Integer> distancesToDirectives = new MultiMap<>();
      StreamEx.of(candidates.get(DeclarationProximity.EXPORTED_BY_PUBLIC_MODULE))
        .map(declaration ->
               StreamEx.of(scope.getPublicModulesExporting(declaration))
                 .distinct()
                 .map(module -> pair(module, distanceCalculator.get(module, declaration))))
        .flatMap(Function.identity())
        .forEach(pair -> distancesToDirectives.putValue(pair.first, pair.second));

      Map<Angular2Module, Double> averageDistances = new HashMap<>();
      distancesToDirectives.entrySet().forEach(
        entry -> averageDistances.put(entry.getKey(), IntStreamEx.of(entry.getValue()).average().orElse(0)));

      return StreamEx.of(averageDistances.keySet())
        .sorted(Comparator.comparingDouble(averageDistances::get))
        .map(Angular2Module::getTypeScriptClass)
        .select(JSElement.class)
        .toList();
    }

    @NotNull
    @Override
    protected PsiElementListCellRenderer<JSElement> createRenderer(@NotNull Map<PsiElement, JSModuleNameInfo> preRenderedQNames) {
      return super.createRenderer(preRenderedQNames);
    }

    @Override
    protected void runAction(@Nullable Editor editor,
                             @NotNull String ignored,
                             @NotNull JSElement moduleClassToImport,
                             @NotNull PsiElement place) {
      assert myContext != null;
      Angular2DeclarationsScope scope = new Angular2DeclarationsScope(myContext);
      if (scope.getModule() == null || !scope.isInSource(scope.getModule())) {
        return;
      }
      TypeScriptClass destinationModuleClass = scope.getModule().getTypeScriptClass();
      Angular2Module moduleToImport = Angular2EntitiesProvider.getModule(moduleClassToImport);

      if (destinationModuleClass == null
          || scope.getModule().getDecorator() == null
          || moduleToImport == null) {
        return;
      }
      String name;
      if (moduleToImport instanceof Angular2MetadataModule) {
        name = ObjectUtils.notNull(((Angular2MetadataModule)moduleToImport).getStub().getMemberName(),
                                   moduleToImport.getName());
      }
      else {
        name = moduleToImport.getName();
      }
      WriteAction.run(() -> {
        ES6ImportPsiUtil.insertJSImport(destinationModuleClass, name, moduleClassToImport, editor);
        Angular2FixesPsiUtil.insertNgModuleMember(scope.getModule(), IMPORTS_PROP, name);
        // TODO support NgModuleWithProviders static methods
      });
    }
  }

  private static class DistanceCalculator {

    int get(Angular2Module module, Angular2Declaration declaration) {
      // For now very simple condition, if that's not enough we can
      // improve algorithm by providing proper distance calculations.
      return module.getExports().contains(declaration) ? 0 : 1;
    }
  }
}
