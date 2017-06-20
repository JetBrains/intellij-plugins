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
package org.jetbrains.plugins.ruby.motion.run.renderers;

import com.intellij.execution.ExecutionException;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerCommandException;
import com.jetbrains.cidr.execution.debugger.backend.LLValueData;
import com.jetbrains.cidr.execution.debugger.evaluation.EvaluationContext;
import com.jetbrains.cidr.execution.debugger.evaluation.ValueRendererFactory;
import com.jetbrains.cidr.execution.debugger.evaluation.renderers.NSCollectionValueRenderer;
import com.jetbrains.cidr.execution.debugger.evaluation.renderers.ValueRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class MotionValueRendererFactory implements ValueRendererFactory {
  @Nullable
  @Override
  public ValueRenderer createRenderer(@NotNull FactoryContext context) throws ExecutionException,
                                                                              DebuggerCommandException {
    try {
      LLValueData data = context.getLLValueData();
      if (data.isValidPointer()) {
        context.getEvaluationContext().evaluate(EvaluationContext.cast("rb_inspect(" + EvaluationContext.cast(data.getPointer(), "id") + ")", "id"));
        // we want ObjC renderers for our collections
        // Ruby collections are inheriting from native ones and don't have any useful instance variables
        final ValueRenderer collectionRenderer = new NSCollectionValueRenderer.Factory().createRenderer(context);
        return collectionRenderer != null ? collectionRenderer : new MotionObjectRenderer(context.getPhysicalValue());
      }
    } catch (DebuggerCommandException ignored) {}
    return null;
  }
}
