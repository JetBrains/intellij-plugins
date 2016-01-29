package com.jetbrains.lang.dart.ide.generation;

public class DartGenerateConstructorAction extends BaseDartGenerateAction {
  @Override
  protected BaseDartGenerateHandler getGenerateHandler() {
    return new DartGenerateConstructorHandler();
  }
}
