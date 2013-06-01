package com.jetbrains.lang.dart.ide.surroundWith.statement;

/**
 * @author: Fedor.Korotkov
 */
public class DartWithIfElseSurrounder extends DartLiteralAndBlockStatementSurrounderBase {
  @Override
  public String getTemplateDescription() {
    return "if / else";
  }

  @Override
  protected String getTemplateText() {
    return "if(true) {\n}\nelse {\n\n}";
  }
}
