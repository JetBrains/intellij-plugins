/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

public class DartGenerateEqualsAndHashcodeHandler extends BaseDartGenerateHandler {
  @Override
  protected String getTitle() {
    return DartBundle.message("dart.generate.equalsAndHashcode");
  }

  @Override
  protected BaseCreateMethodsFix createFix(DartClass dartClass) {
    return new CreateEqualsAndHashcodeFix(dartClass);
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

  @Override
  protected boolean doAllowEmptySelection() {
    return true;
  }
}
