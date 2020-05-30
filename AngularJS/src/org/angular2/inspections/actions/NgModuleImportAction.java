// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.actions;

import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil;
import com.intellij.lang.javascript.modules.JSModuleNameInfo;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.entities.Angular2ComponentLocator;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Module;
import org.angular2.entities.metadata.psi.Angular2MetadataModule;
import org.angular2.inspections.quickfixes.Angular2FixesFactory;
import org.angular2.inspections.quickfixes.Angular2FixesPsiUtil;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.openapi.util.Pair.pair;
import static org.angular2.Angular2DecoratorUtil.IMPORTS_PROP;

public class NgModuleImportAction extends Angular2NgModuleSelectAction {

  NgModuleImportAction(@Nullable Editor editor, @NotNull PsiElement element, @NotNull String actionName, boolean codeCompletion) {
    super(editor, element, "NgModule", DEFAULT_FILTER, actionName, codeCompletion); //NON-NLS
  }

  @Override
  protected String getModuleSelectionPopupTitle() {
    return Angular2Bundle.message("angular.quickfix.ngmodule.import.select.module");
  }

  @Override
  public @NotNull List<JSElement> getCandidates() {
    if (myContext == null) {
      return Collections.emptyList();
    }

    DistanceCalculator distanceCalculator = new DistanceCalculator();
    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(myContext);

    MultiMap<Angular2DeclarationsScope.DeclarationProximity, Angular2Declaration>
      candidates = Angular2FixesFactory.getCandidatesForResolution(myContext, myCodeCompletion);
    if (!candidates.get(Angular2DeclarationsScope.DeclarationProximity.IN_SCOPE).isEmpty()) {
      return Collections.emptyList();
    }

    MultiMap<Angular2Module, Integer> distancesToDirectives = new MultiMap<>();
    StreamEx.of(candidates.get(Angular2DeclarationsScope.DeclarationProximity.EXPORTED_BY_PUBLIC_MODULE))
      .flatMap(declaration ->
                 StreamEx.of(scope.getPublicModulesExporting(declaration))
                   .distinct()
                   .map(module -> pair(module, distanceCalculator.get(module, declaration))))
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

  @Override
  protected @NotNull List<JSElement> getFinalElements(@NotNull Project project,
                                                      @NotNull PsiFile file,
                                                      @NotNull List<JSElement> candidates,
                                                      @NotNull Collection<JSElement> elementsFromLibraries,
                                                      @NotNull Map<PsiElement, JSModuleNameInfo> renderedTexts) {
    if (!file.isValid() || project.isDisposed() || !project.isOpen()) {
      return ContainerUtil.emptyList();
    }
    TypeScriptClass component = Angular2ComponentLocator.findComponentClass(file);
    if (component != null) {
      file = component.getContainingFile();
    }
    candidates = removeMergedElements(candidates, elementsFromLibraries);
    candidates = fillExternalModuleNamesAndFilterByBlacklist(renderedTexts, candidates, file);
    return removeSrcLibraryFiles(candidates,
                                 createLibraryOnlyModulesInfos(elementsFromLibraries, renderedTexts));
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

  private static class DistanceCalculator {

    int get(Angular2Module module, Angular2Declaration declaration) {
      // For now very simple condition, if that's not enough we can
      // improve algorithm by providing proper distance calculations.
      return module.getExports().contains(declaration) ? 0 : 1;
    }
  }
}
