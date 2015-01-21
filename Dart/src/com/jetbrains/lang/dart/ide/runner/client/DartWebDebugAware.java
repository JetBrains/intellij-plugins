package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.evaluation.ExpressionInfo;
import com.jetbrains.javascript.debugger.ExpressionInfoFactory;
import com.jetbrains.javascript.debugger.JavaScriptDebugAware;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.runner.DartLineBreakpointType;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class DartWebDebugAware extends JavaScriptDebugAware {
  @Nullable
  @Override
  protected LanguageFileType getFileType() {
    return DartFileType.INSTANCE;
  }

  @Override
  public boolean isOnlySourceMappedBreakpoints() {
    return false;
  }

  @Override
  @Nullable
  public Class<? extends XLineBreakpointType<?>> getBreakpointTypeClass() {
    return DartLineBreakpointType.class;
  }

  @Nullable
  @Override
  public ExpressionInfo getEvaluationInfo(@NotNull PsiElement elementAtOffset, @NotNull Document document, @NotNull ExpressionInfoFactory expressionInfoFactory) {
    /** WEB-11715
     dart psi: notes.text

     REFERENCE_EXPRESSION
     REFERENCE_EXPRESSION “notes”
     PsiElement(.) “.”
     REFERENCE_EXPRESSION “text”
     */
    // find topmost reference, but stop if argument list found
    DartReference reference = null;
    PsiElement element = elementAtOffset;
    while (true) {
      if (element instanceof DartReference) {
        reference = (DartReference)element;
      }

      element = element.getParent();
      if (element == null ||
          // int.parse(slider.value) - we must return reference expression "slider.value", but not the whole expression
          element instanceof DartArgumentList ||
          // "${seeds} seeds" - we must return only "seeds"
          element instanceof DartLongTemplateEntry ||
          element instanceof DartCallExpression ||
          element instanceof DartFunctionBody || element instanceof DartBlock) {
        break;
      }
    }

    if (reference != null) {
      TextRange textRange = reference.getTextRange();
      // note<CURSOR>s.text - the whole reference expression is notes.txt, but we must return only notes
      int endOffset = elementAtOffset.getTextOffset() + elementAtOffset.getTextLength();
      if (textRange.getEndOffset() != endOffset) {
        textRange = new TextRange(textRange.getStartOffset(), endOffset);
      }
      return new ExpressionInfo(textRange);
    }

    PsiElement parent = elementAtOffset.getParent();
    return parent instanceof DartId ? new ExpressionInfo(parent.getTextRange()) : null;
  }
}