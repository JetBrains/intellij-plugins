package com.jetbrains.lang.dart.ide.surroundWith.statement;

/**
 * @author: Fedor.Korotkov
 */
public class DartWithWhileSurrounder extends DartLiteralAndBlockStatementSurrounderBase {
  @Override
  public String getTemplateDescription() {
    return "while";
  }

  @Override
  protected String getTemplateText() {
    return "while(true) {\n}";
  }
}
