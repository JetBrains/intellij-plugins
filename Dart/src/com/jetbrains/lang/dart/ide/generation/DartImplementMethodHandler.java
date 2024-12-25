// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.generation;

import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DartImplementMethodHandler extends BaseDartGenerateHandler {
  @Override
  protected @NotNull String getTitle() {
    return DartBundle.message("dialog.title.choose.methods.to.implement");
  }

  @Override
  protected @NotNull BaseCreateMethodsFix createFix(final @NotNull DartClass dartClass) {
    return new OverrideImplementMethodFix(dartClass, true);
  }

  @Override
  protected void collectCandidates(final @NotNull DartClass dartClass, final @NotNull List<DartComponent> candidates) {
    Map<Pair<String, Boolean>, DartComponent> result = new HashMap<>(computeSuperInterfacesMembersMap(dartClass));
    Map<Pair<String, Boolean>, DartComponent> superClassesMemberMap = new HashMap<>(computeSuperClassesMemberMap(dartClass));
    result.keySet().removeAll(superClassesMemberMap.keySet());
    for (Map.Entry<Pair<String, Boolean>, DartComponent> entry : superClassesMemberMap.entrySet()) {
      final DartComponent component = entry.getValue();
      if (component.isAbstract() && !result.containsKey(entry.getKey())) {
        result.put(entry.getKey(), entry.getValue());
      }
    }
    result.keySet().removeAll(computeClassMembersMap(dartClass, false).keySet());
    candidates.addAll(ContainerUtil.findAll(result.values(),
                                            component -> component.isPublic() || DartResolveUtil.sameLibrary(dartClass, component)));
  }
}
