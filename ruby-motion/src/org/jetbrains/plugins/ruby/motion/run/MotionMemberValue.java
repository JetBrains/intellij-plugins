/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.xdebugger.XSourcePosition;
import com.jetbrains.cidr.execution.debugger.backend.LLValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrPhysicalValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
* @author Dennis.Ushakov
*/
public class MotionMemberValue extends CidrPhysicalValue {
  private final CidrPhysicalValue myParent;

  public MotionMemberValue(final LLValue var,
                           final String displayName,
                           final CidrPhysicalValue parent) {
    super(var, displayName, parent.getProcess(), parent.getSourcePosition(), parent.getFrame());
    myParent = parent;
  }

  @Nullable
  @Override
  protected XSourcePosition doComputePosition(@NotNull XSourcePosition position) {
    return null;
  }

  @NotNull
  @Override
  public String getEvaluationExpression(boolean lvalue) {
    return myParent.getPreparedRenderer().getChildEvaluationExpression(this, lvalue);
  }
}
