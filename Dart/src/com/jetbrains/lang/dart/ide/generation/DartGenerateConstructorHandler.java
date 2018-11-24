package com.jetbrains.lang.dart.ide.generation;

import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartGenerateConstructorHandler extends BaseDartGenerateHandler {
  @Override
  protected String getTitle() {
    return DartBundle.message("dart.generate.constructor");
  }

  @Override
  protected BaseCreateMethodsFix createFix(@NotNull final DartClass dartClass) {
    return new CreateConstructorFix(dartClass);
  }

  @Override
  protected void collectCandidates(@NotNull final DartClass dartClass, @NotNull final List<DartComponent> candidates) {
    candidates.addAll(ContainerUtil.findAll(computeClassMembersMap(dartClass, false).values(),
                                            component -> DartComponentType.typeOf(component) == DartComponentType.FIELD));
  }

}
