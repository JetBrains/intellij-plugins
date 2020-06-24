// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.codeInsight.hints.Option;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.psi.JSCallLikeExpression;
import com.intellij.lang.typescript.editing.TypeScriptInlayParameterHintsProvider;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.expr.psi.Angular2PipeExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class Angular2InlayParameterHintsProvider extends TypeScriptInlayParameterHintsProvider {
  public static final Option NAMES_FOR_ALL_ARGS = new Option(
    "angular.show.names.for.all.args", JavaScriptBundle.messagePointer("js.param.hints.show.names.for.all.args"), false);
  public static final Option NAMES_FOR_PIPES = new Option(
    "angular.show.names.for.pipes", Angular2Bundle.messagePointer("angular.inlay.params.option.pipe.arguments"), true);

  @Override
  protected Option getShowNameForAllArgsOption() {
    return NAMES_FOR_ALL_ARGS;
  }

  @Override
  public @NotNull List<Option> getSupportedOptions() {
    return Arrays.asList(getShowNameForAllArgsOption(), NAMES_FOR_PIPES);
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
