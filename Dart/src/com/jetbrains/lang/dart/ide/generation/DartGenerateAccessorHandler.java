package com.jetbrains.lang.dart.ide.generation;

import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class DartGenerateAccessorHandler extends BaseDartGenerateHandler {

  private final CreateGetterSetterFix.Strategy myStrategy;

  protected DartGenerateAccessorHandler(CreateGetterSetterFix.Strategy strategy) {
    myStrategy = strategy;
  }

  @Override
  protected BaseCreateMethodsFix createFix(@NotNull final DartClass dartClass) {
    return new CreateGetterSetterFix(dartClass, myStrategy);
  }

  @Override
  protected void collectCandidates(final DartClass dartClass, List<DartComponent> candidates) {
    final List<DartComponent> subComponents = DartResolveUtil.getNamedSubComponents(dartClass);

    candidates.addAll(ContainerUtil.findAll(computeClassMembersMap(dartClass, true).values(), new Condition<DartComponent>() {
      @Override
      public boolean value(DartComponent component) {
        return DartComponentType.typeOf(component) == DartComponentType.FIELD && myStrategy.accept(component.getName(), subComponents);
      }
    }));
  }
}
