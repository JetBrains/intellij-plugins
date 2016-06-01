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
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartGenerateToStringHandler extends BaseDartGenerateHandler {
  @Override
  protected String getTitle() {
    return DartBundle.message("dart.generate.toString");
  }

  @Override
  protected BaseCreateMethodsFix createFix(@NotNull final DartClass dartClass) {
    return new CreateToStringFix(dartClass);
  }


  @Override
  protected void collectCandidates(@NotNull final DartClass dartClass, @NotNull final List<DartComponent> candidates) {
    candidates.addAll(ContainerUtil.findAll(computeClassMembersMap(dartClass, false).values(),
                                            component -> DartComponentType.typeOf(component) == DartComponentType.FIELD));
  }

  @Override
  protected boolean doAllowEmptySelection() {
    return true;
  }
}
