package com.jetbrains.lang.dart.ide.generation;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import gnu.trove.THashMap;

import java.util.List;
import java.util.Map;

public class DartOverrideMethodHandler extends BaseDartGenerateHandler {
  @Override
  protected String getTitle() {
    return DartBundle.message("dart.override.method");
  }

  @Override
  protected void collectCandidates(DartClass dartClass, List<DartComponent> candidates) {
    Map<Pair<String, Boolean>, DartComponent> result =
      new THashMap<Pair<String, Boolean>, DartComponent>(computeSuperClassesMemberMap(dartClass));
    result.keySet().removeAll(computeClassMembersMap(dartClass).keySet());
    candidates.addAll(ContainerUtil.findAll(result.values(), new Condition<DartComponent>() {
      @Override
      public boolean value(DartComponent component) {
        return component.isPublic() && DartComponentType.typeOf(component) != DartComponentType.FIELD;
      }
    }));
  }

  @Override
  protected BaseCreateMethodsFix createFix(DartClass dartClass) {
    return new OverrideImplementMethodFix(dartClass, false);
  }
}
