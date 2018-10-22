// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.angular2.lang.selector.Angular2DirectiveSelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.*;

public interface Angular2Directive extends Angular2Declaration {

  @NotNull
  String getSelector();

  @Nullable
  String getExportAs();

  @NotNull
  Collection<? extends Angular2DirectiveProperty> getInputs();

  @NotNull
  Collection<? extends Angular2DirectiveProperty> getOutputs();

  boolean isTemplate();

  default boolean isComponent() {
    return this instanceof Angular2Component;
  }

  default List<Angular2DirectiveSelector> getDirectiveSelectors() {
    PsiElement source = getSourceElement();
    String selector = getSelector();
    return CachedValuesManager.getCachedValue(source, () -> {
      try {
        return CachedValueProvider.Result.create(Angular2DirectiveSelector.parse(selector), source);
      }
      catch (ParseException e) {
        return CachedValueProvider.Result.create(Collections.emptyList(), source);
      }
    });
  }

  default List<Pair<Angular2DirectiveProperty, Angular2DirectiveProperty>> getInOuts() {
    final String OUTPUT_CHANGE_SUFFIX = "Change";

    Collection<? extends Angular2DirectiveProperty> outputs = getOutputs();
    Collection<? extends Angular2DirectiveProperty> inputs = getInputs();

    if (inputs.isEmpty() || outputs.isEmpty()) {
      return Collections.emptyList();
    }
    Map<String, Angular2DirectiveProperty> inputMap = new HashMap<>();
    for (Angular2DirectiveProperty p : inputs) {
      inputMap.putIfAbsent(p.getName(), p);
    }
    List<Pair<Angular2DirectiveProperty, Angular2DirectiveProperty>> result = new ArrayList<>();
    for (Angular2DirectiveProperty output : outputs) {
      String name = output.getName();
      if (output.getName().endsWith(OUTPUT_CHANGE_SUFFIX)) {
        Angular2DirectiveProperty input = inputMap.get(
          name.substring(0, name.length() - OUTPUT_CHANGE_SUFFIX.length()));
        if (input != null) {
          result.add(Pair.create(output, input));
        }
      }
    }
    return result;
  }
}
