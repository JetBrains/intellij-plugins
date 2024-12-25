// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.generation;

import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartGenerateNamedConstructorHandler extends BaseDartGenerateHandler {
  @Override
  protected @NotNull String getTitle() {
    return DartBundle.message("dart.generate.named.constructor");
  }

  @Override
  protected @NotNull BaseCreateMethodsFix createFix(final @NotNull DartClass dartClass) {
    return new CreateNamedConstructorFix(dartClass);
  }

  @Override
  protected void collectCandidates(final @NotNull DartClass dartClass, final @NotNull List<DartComponent> candidates) {
    candidates.addAll(ContainerUtil.findAll(computeClassMembersMap(dartClass, false).values(),
                                            component -> DartComponentType.typeOf(component) == DartComponentType.FIELD));
  }

  @Override
  protected boolean doAllowEmptySelection() {
    return true;
  }
}
