package com.jetbrains.lang.dart.ide.generation;

import com.intellij.openapi.util.Pair;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import gnu.trove.THashMap;

import java.util.List;
import java.util.Map;

/**
 * @author: Fedor.Korotkov
 */
public class DartImplementMethodHandler extends BaseDartGenerateHandler {
  @Override
  protected String getTitle() {
    return DartBundle.message("dart.implement.method");
  }

  @Override
  protected void collectCandidates(Map<Pair<String, Boolean>, DartComponent> classMembersMap,
                                   Map<Pair<String, Boolean>, DartComponent> superClassesMembersMap,
                                   Map<Pair<String, Boolean>, DartComponent> superInterfacesMembersMap,
                                   List<DartComponent> candidates) {
    Map<Pair<String, Boolean>, DartComponent> result = new THashMap<Pair<String, Boolean>, DartComponent>(superInterfacesMembersMap);
    result.keySet().removeAll(superClassesMembersMap.keySet());
    for (Map.Entry<Pair<String, Boolean>, DartComponent> entry : superClassesMembersMap.entrySet()) {
      final DartComponent component = entry.getValue();
      if (component.isAbstract() && !result.containsKey(entry.getKey())) {
        result.put(entry.getKey(), entry.getValue());
      }
    }
    result.keySet().removeAll(classMembersMap.keySet());
    candidates.addAll(result.values());
  }

  @Override
  protected BaseCreateMethodsFix createFix(DartClass dartClass) {
    return new OverrideImplementMethodFix(dartClass);
  }
}
