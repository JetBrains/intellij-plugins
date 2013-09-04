package com.jetbrains.lang.dart.ide.generation;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;

import java.util.List;
import java.util.Map;

/**
 * Created by fedorkorotkov.
 */
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
  protected void collectCandidates(Map<Pair<String, Boolean>, DartComponent> classMembersMap,
                                   Map<Pair<String, Boolean>, DartComponent> superClassesMembersMap,
                                   Map<Pair<String, Boolean>, DartComponent> superInterfacesMembersMap,
                                   List<DartComponent> candidates) {
    candidates.addAll(ContainerUtil.findAll(classMembersMap.values(), new Condition<DartComponent>() {
      @Override
      public boolean value(DartComponent component) {
        return DartComponentType.typeOf(component) == DartComponentType.FIELD;
      }
    }));
  }
}
