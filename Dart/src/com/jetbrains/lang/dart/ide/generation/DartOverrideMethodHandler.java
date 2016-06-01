package com.jetbrains.lang.dart.ide.generation;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class DartOverrideMethodHandler extends BaseDartGenerateHandler {
  @Override
  protected String getTitle() {
    return DartBundle.message("dart.override.method");
  }

  @Override
  protected BaseCreateMethodsFix createFix(@NotNull final DartClass dartClass) {
    return new OverrideImplementMethodFix(dartClass, false);
  }

  @Override
  protected void collectCandidates(@NotNull final DartClass dartClass, @NotNull final List<DartComponent> candidates) {
    Map<Pair<String, Boolean>, DartComponent> result =
      new THashMap<Pair<String, Boolean>, DartComponent>(computeSuperClassesMemberMap(dartClass));
    result.keySet().removeAll(computeClassMembersMap(dartClass, false).keySet());
    candidates.addAll(ContainerUtil.findAll(result.values(), component -> DartComponentType.typeOf(component) != DartComponentType.FIELD &&
                                                                      (component.isPublic() || DartResolveUtil.sameLibrary(dartClass, component))));
  }

}
