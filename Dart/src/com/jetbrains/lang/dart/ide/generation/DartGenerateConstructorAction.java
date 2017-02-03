package com.jetbrains.lang.dart.ide.generation;

import org.jetbrains.annotations.NotNull;

public class DartGenerateConstructorAction extends BaseDartGenerateAction {
  @Override
  @NotNull
  protected BaseDartGenerateHandler getGenerateHandler() {
    return new DartGenerateConstructorHandler();
  }
}
