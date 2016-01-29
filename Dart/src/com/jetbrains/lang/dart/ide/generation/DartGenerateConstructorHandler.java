package com.jetbrains.lang.dart.ide.generation;

import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;

import java.util.List;

public class DartGenerateConstructorHandler extends BaseDartGenerateHandler {
  @Override
  protected String getTitle() {
    return DartBundle.message("dart.generate.constructor");
  }

  @Override
  protected BaseCreateMethodsFix createFix(DartClass dartClass) {
    return new CreateConstructorFix(dartClass);
  }

  @Override
  protected void collectCandidates(DartClass dartClass, List<DartComponent> candidates) {
    candidates.addAll(ContainerUtil.findAll(computeClassMembersMap(dartClass).values(), new Condition<DartComponent>() {
      @Override
      public boolean value(DartComponent component) {
        return DartComponentType.typeOf(component) == DartComponentType.FIELD;
      }
    }));
  }

}
