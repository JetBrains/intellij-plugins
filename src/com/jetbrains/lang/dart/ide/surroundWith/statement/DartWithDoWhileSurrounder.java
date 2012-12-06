package com.jetbrains.lang.dart.ide.surroundWith.statement;

/**
 * @author: Fedor.Korotkov
 */
public class DartWithDoWhileSurrounder extends DartLiteralAndBlockStatementSurrounderBase {
  @Override
  public String getTemplateDescription() {
    return "do / while";
  }

  @Override
  protected String getTemplateText() {
    return "do{\n} while(true);";
  }
}
