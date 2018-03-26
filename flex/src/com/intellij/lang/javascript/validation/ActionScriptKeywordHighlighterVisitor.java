package com.intellij.lang.javascript.validation;


import com.intellij.lang.actionscript.psi.impl.ActionScriptGotoStatementImpl;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.highlighting.ECMAL4Highlighter;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSStatementWithLabelReference;
import org.jetbrains.annotations.NotNull;

public class ActionScriptKeywordHighlighterVisitor extends JSKeywordHighlighterVisitor {
  public ActionScriptKeywordHighlighterVisitor(AnnotationHolder holder) {
    super(holder, new ECMAL4Highlighter());
  }

  @Override
  public void visitJSStatementWithLabelReference(JSStatementWithLabelReference element) {
    if (element instanceof ActionScriptGotoStatementImpl) {
      highlightChildKeywordOfType(element, JSTokenTypes.GOTO_KEYWORD);
    }

    super.visitJSStatementWithLabelReference(element);
  }

  @Override
  protected void highlightKeywordGetterAndSetter(@NotNull JSFunction function) {
    //do nothing
  }
}
