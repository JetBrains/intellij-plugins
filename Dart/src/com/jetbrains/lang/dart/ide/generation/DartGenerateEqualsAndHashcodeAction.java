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

import com.jetbrains.lang.dart.psi.DartClass;
import org.jetbrains.annotations.Nullable;

public class DartGenerateEqualsAndHashcodeAction extends BaseDartGenerateAction {

  private static final String EQUALS_OP = "==";
  private static final String HASHCODE = "hashCode";

  @Override
  protected BaseDartGenerateHandler getGenerateHandler() {
    return new DartGenerateEqualsAndHashcodeHandler();
  }

  @Override
  protected boolean doEnable(@Nullable DartClass dartClass) {
    if (dartClass == null) {
      return false;
    }
    return !doesClassContainMethod(dartClass, EQUALS_OP) && !doesClassContainGetter(dartClass, HASHCODE);
  }
}
