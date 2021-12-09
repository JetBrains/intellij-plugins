// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.generation;

import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartGenerateToStringHandler extends BaseDartGenerateHandler {
  @Override
  @NotNull
  protected String getTitle() {
    //noinspection DialogTitleCapitalization
    return DartBundle.message("dart.generate.toString");
  }

  @Override
  @NotNull
  protected BaseCreateMethodsFix createFix(@NotNull final DartClass dartClass) {
    return new CreateToStringFix(dartClass);
  }


  @Override
  protected void collectCandidates(@NotNull final DartClass dartClass, @NotNull final List<DartComponent> candidates) {
    candidates.addAll(ContainerUtil.findAll(computeClassMembersMap(dartClass, false).values(),
                                            component -> DartComponentType.typeOf(component) == DartComponentType.FIELD));
  }

  @Override
  protected boolean doAllowEmptySelection() {
    return true;
  }
}
