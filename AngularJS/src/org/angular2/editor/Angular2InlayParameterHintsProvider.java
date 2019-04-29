// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.codeInsight.hints.Option;
import com.intellij.lang.javascript.psi.JSCallLikeExpression;
import com.intellij.lang.typescript.editing.TypeScriptInlayParameterHintsProvider;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.expr.psi.Angular2PipeExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Angular2InlayParameterHintsProvider extends TypeScriptInlayParameterHintsProvider {
  public static final Option NAMES_FOR_ALL_ARGS = new Option("angular.show.names.for.all.args", "Show name for all arguments", false);
  public static final Option NAMES_FOR_TAGGED_TEMPLATES = new Option("angular.show.names.for.tagged", "Show name for tagged template arguments", true);
  public static final Option NAMES_FOR_PIPES = new Option("angular.show.names.for.pipes", "Show name for pipe arguments", true);

  @Override
  protected Option getShowNameForAllArgsOption() {
    return NAMES_FOR_ALL_ARGS;
  }

  @Override
  protected Option getShowNameForTaggedOption() {
    return NAMES_FOR_TAGGED_TEMPLATES;
  }

  @NotNull
  @Override
  public List<Option> getSupportedOptions() {
    return ContainerUtil.list(getShowNameForAllArgsOption(),
                              getShowNameForTaggedOption(),
                              NAMES_FOR_PIPES);
  }

  @Override
  protected boolean isSuitableCallExpression(@Nullable JSCallLikeExpression expression) {
    if (!super.isSuitableCallExpression(expression)) return false;
    if (!NAMES_FOR_PIPES.get() && expression instanceof Angular2PipeExpression) return false;
    return true;
  }

  @Override
  protected boolean skipIndex(int i, JSCallLikeExpression expression) {
    if (expression instanceof Angular2PipeExpression && i == 0) return true;
    return super.skipIndex(i, expression);
  }
}
