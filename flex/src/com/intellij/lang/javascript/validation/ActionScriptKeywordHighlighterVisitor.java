package com.intellij.lang.javascript.validation;


import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.lang.actionscript.highlighting.ECMAL4Highlighter;
import com.intellij.lang.actionscript.psi.impl.ActionScriptGotoStatementImpl;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSStatementWithLabelReference;
import org.jetbrains.annotations.NotNull;

public class ActionScriptKeywordHighlighterVisitor extends JSKeywordHighlighterVisitor {
  public ActionScriptKeywordHighlighterVisitor(HighlightInfoHolder holder) {
    super(holder, new ECMAL4Highlighter());
  }

  @Override
  public void visitJSStatementWithLabelReference(@NotNull JSStatementWithLabelReference element) {
    if (element instanceof ActionScriptGotoStatementImpl) {
      highlightChildKeywordOfType(element, JSTokenTypes.GOTO_KEYWORD);
    }

    super.visitJSStatementWithLabelReference(element);
  }
}
