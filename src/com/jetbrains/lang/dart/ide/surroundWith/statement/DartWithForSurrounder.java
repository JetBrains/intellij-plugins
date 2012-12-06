package com.jetbrains.lang.dart.ide.surroundWith.statement;

import com.jetbrains.lang.dart.psi.DartForLoopParts;

/**
 * @author: Fedor.Korotkov
 */
public class DartWithForSurrounder extends DartBlockAndChildStatementSurrounderBase<DartForLoopParts> {
  @Override
  public String getTemplateDescription() {
    return "for";
  }

  @Override
  protected String getTemplateText() {
    return "for(a in []){\n}";
  }

  @Override
  protected Class<DartForLoopParts> getClassToDelete() {
    return DartForLoopParts.class;
  }
}
