package com.jetbrains.lang.dart.ide.generation;

/**
 * @author: Fedor.Korotkov
 */
public class DartGenerateConstructorAction extends BaseDartGenerateAction {
  @Override
  protected BaseDartGenerateHandler getGenerateHandler() {
    return new DartGenerateConstructorHandler();
  }
}
