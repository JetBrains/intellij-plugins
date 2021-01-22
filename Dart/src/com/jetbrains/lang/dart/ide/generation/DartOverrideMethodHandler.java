// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.generation;

import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DartOverrideMethodHandler extends BaseDartGenerateHandler {
  @Override
  @NotNull
  protected String getTitle() {
    return DartBundle.message("dialog.title.choose.methods.to.override");
  }

  @Override
  @NotNull
  protected BaseCreateMethodsFix createFix(@NotNull final DartClass dartClass) {
    return new OverrideImplementMethodFix(dartClass, false);
  }

  @Override
  protected void collectCandidates(@NotNull final DartClass dartClass, @NotNull final List<DartComponent> candidates) {
    Map<Pair<String, Boolean>, DartComponent> result = new HashMap<>(computeSuperClassesMemberMap(dartClass));
    result.keySet().removeAll(computeClassMembersMap(dartClass, false).keySet());
    candidates.addAll(ContainerUtil.findAll(result.values(), component -> DartComponentType.typeOf(component) != DartComponentType.FIELD &&
                                                                          (component.isPublic() ||
                                                                           DartResolveUtil.sameLibrary(dartClass, component))));
  }
}
