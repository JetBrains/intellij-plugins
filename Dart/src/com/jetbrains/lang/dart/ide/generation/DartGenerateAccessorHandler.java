package com.jetbrains.lang.dart.ide.generation;

import com.intellij.openapi.util.Pair;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.DartResolveUtil;

import java.util.List;
import java.util.Map;

/**
 * @author: Fedor.Korotkov
 */
public abstract class DartGenerateAccessorHandler extends BaseDartGenerateHandler {

  private final CreateGetterSetterFix.Strategy myStrategy;

  protected DartGenerateAccessorHandler(CreateGetterSetterFix.Strategy strategy) {
    myStrategy = strategy;
  }

  @Override
  protected BaseCreateMethodsFix createFix(DartClass dartClass) {
    return new CreateGetterSetterFix(dartClass, myStrategy);
  }

  @Override
  protected void collectCandidates(DartClass dartClass, List<DartComponent> candidates) {
    final List<DartComponent> subComponents = DartResolveUtil.getNamedSubComponents(dartClass);

    for (DartComponent dartComponent : subComponents) {
      if (DartComponentType.typeOf(dartComponent) != DartComponentType.FIELD) continue;
      if (dartComponent.isStatic()) continue;

      if (!myStrategy.accept(dartComponent.getName(), subComponents)) continue;

      candidates.add(dartComponent);
    }
  }

  @Override
  protected void collectCandidates(Map<Pair<String, Boolean>, DartComponent> classMembersMap,
                                   Map<Pair<String, Boolean>, DartComponent> superClassesMembersMap,
                                   Map<Pair<String, Boolean>, DartComponent> superInterfacesMembersMap,
                                   List<DartComponent> candidates) {
    // ignore
  }
}
