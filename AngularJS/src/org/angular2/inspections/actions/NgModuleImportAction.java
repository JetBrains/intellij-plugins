// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.actions;

import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil;
import com.intellij.lang.javascript.modules.JSModuleNameInfo;
import com.intellij.lang.javascript.modules.imports.ES6ImportCandidate;
import com.intellij.lang.javascript.modules.imports.JSImportCandidate;
import com.intellij.lang.javascript.modules.imports.JSImportCandidateWithExecutor;
import com.intellij.lang.javascript.modules.imports.JSPlaceElementFilter;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.MultiMap;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.Angular2DeclarationsScope;
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

  NgModuleImportAction(@Nullable Editor editor, @NotNull PsiElement element, @NotNull @NlsContexts.Command String actionName, boolean codeCompletion) {
    super(editor, element, "NgModule", JSPlaceElementFilter.DEFAULT_FILTER, actionName, codeCompletion); //NON-NLS
  }

  @Override
  protected String getModuleSelectionPopupTitle() {
    return Angular2Bundle.message("angular.quickfix.ngmodule.import.select.module");
  }

  @Override
  public @NotNull List<? extends JSImportCandidate> getRawCandidates() {
    DistanceCalculator distanceCalculator = new DistanceCalculator();
    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(getContext());

    MultiMap<Angular2DeclarationsScope.DeclarationProximity, Angular2Declaration>
      candidates = Angular2FixesFactory.getCandidatesForResolution(getContext(), myCodeCompletion);
    if (!candidates.get(Angular2DeclarationsScope.DeclarationProximity.IN_SCOPE).isEmpty()) {
      return Collections.emptyList();
    }

    MultiMap<Angular2Module, Integer> distancesToDirectives = new MultiMap<>();
    StreamEx.of(candidates.get(Angular2DeclarationsScope.DeclarationProximity.IMPORTABLE))
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
      .map(el -> new ES6ImportCandidate(myName, el, getContext()))
      .toList();
  }

  @Override
  protected @NotNull List<? extends JSImportCandidate> filter(@NotNull List<? extends JSImportCandidate> candidates) {
    Collection<? extends JSImportCandidate> elementsFromLibraries = getElementsFromLibraries(candidates);
    Map<JSImportCandidate, JSModuleNameInfo> renderedTexts = new HashMap<>();
    candidates = removeMergedElements(candidates, elementsFromLibraries);
    candidates = fillModuleNamesAndFilterByBlacklist(renderedTexts, candidates);
    return removeSrcAndMinifiedLibraryFiles(candidates, createLibraryModulesInfos(elementsFromLibraries, renderedTexts));
  }

  @Override
  protected void runAction(@Nullable Editor editor,
                           @NotNull JSImportCandidateWithExecutor candidate,
                           @NotNull PsiElement place) {
    JSElement element = candidate.getElement();
    if (element == null) return;

    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(getContext());
    if (scope.getModule() == null || !scope.isInSource(scope.getModule())) {
      return;
    }
    TypeScriptClass destinationModuleClass = scope.getModule().getTypeScriptClass();
    Angular2Module moduleToImport = Angular2EntitiesProvider.getModule(element);

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
      ES6ImportPsiUtil.insertJSImport(destinationModuleClass, name, element, editor);
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
