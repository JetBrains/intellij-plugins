package com.jetbrains.lang.dart.ide.generation;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class DartImplementMethodHandler extends BaseDartGenerateHandler {
  @Override
  protected String getTitle() {
    return DartBundle.message("dart.implement.method");
  }

  @Override
  protected BaseCreateMethodsFix createFix(@NotNull final DartClass dartClass) {
    return new OverrideImplementMethodFix(dartClass, true);
  }

  @Override
  protected void collectCandidates(@NotNull final DartClass dartClass, @NotNull final List<DartComponent> candidates) {
    Map<Pair<String, Boolean>, DartComponent> result =
      new THashMap<>(computeSuperInterfacesMembersMap(dartClass));
    Map<Pair<String, Boolean>, DartComponent> superClassesMemberMap =
      new THashMap<>(computeSuperClassesMemberMap(dartClass));
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
