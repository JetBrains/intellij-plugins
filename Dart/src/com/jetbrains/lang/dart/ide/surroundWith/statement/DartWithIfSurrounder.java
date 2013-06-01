package com.jetbrains.lang.dart.ide.surroundWith.statement;

/**
 * @author: Fedor.Korotkov
 */
public class DartWithIfSurrounder extends DartLiteralAndBlockStatementSurrounderBase {
  @Override
  public String getTemplateDescription() {
    return "if";
  }

  @Override
  protected String getTemplateText() {
    return "if(true) {\n}";
  }
}
