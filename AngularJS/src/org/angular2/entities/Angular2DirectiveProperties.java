// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Angular2DirectiveProperties {

  private final Collection<? extends Angular2DirectiveProperty> myInputs;
  private final Collection<? extends Angular2DirectiveProperty> myOutputs;
  private final NotNullLazyValue<List<Pair<Angular2DirectiveProperty, Angular2DirectiveProperty>>> myInOuts =
    NotNullLazyValue.createValue(() -> {
      @NonNls final String OUTPUT_CHANGE_SUFFIX = "Change";

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
            result.add(Pair.create(input, output));
          }
        }
      }
      return result;
    });

  public Angular2DirectiveProperties(Collection<? extends Angular2DirectiveProperty> inputs,
                                     Collection<? extends Angular2DirectiveProperty> outputs) {
    myInputs = Collections.unmodifiableCollection(inputs);
    myOutputs = Collections.unmodifiableCollection(outputs);
  }

  public @NotNull Collection<? extends Angular2DirectiveProperty> getInputs() {
    return myInputs;
  }

  public @NotNull Collection<? extends Angular2DirectiveProperty> getOutputs() {
    return myOutputs;
  }

  public List<Pair<Angular2DirectiveProperty, Angular2DirectiveProperty>> getInOuts() {
    return myInOuts.getValue();
  }
}
