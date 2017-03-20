package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.ExpressionInfo;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import com.jetbrains.lang.dart.psi.*;
import org.dartlang.vm.service.element.Frame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DartVmServiceEvaluator extends XDebuggerEvaluator {
  private static final Pattern ERROR_PATTERN = Pattern.compile("Error:.* line \\d+ pos \\d+: (.+)");

  @NotNull private final DartVmServiceDebugProcess myDebugProcess;
  @NotNull private final String myIsolateId;
  @NotNull private final Frame myFrame;

  public DartVmServiceEvaluator(@NotNull final DartVmServiceDebugProcess debugProcess,
                                @NotNull final String isolateId,
                                @NotNull final Frame vmFrame) {
    myDebugProcess = debugProcess;
    myIsolateId = isolateId;
    myFrame = vmFrame;
  }

  @NotNull
  public static String getPresentableError(@NotNull final String rawError) {
    //Error: Unhandled exception:
    //No top-level getter 'foo' declared.
    //
    //NoSuchMethodError: method not found: 'foo'
    //Receiver: top-level
    //Arguments: [...]
    //#0      NoSuchMethodError._throwNew (dart:core-patch/errors_patch.dart:176)
    //#1      _startIsolate.<anonymous closure> (dart:isolate-patch/isolate_patch.dart:260)
    //#2      _RawReceivePortImpl._handleMessage (dart:isolate-patch/isolate_patch.dart:142)

    //Error: '': error: line 1 pos 9: receiver 'this' is not in scope
    //() => 1+this.foo();
    //        ^
    final List<String> lines = StringUtil.split(StringUtil.convertLineSeparators(rawError), "\n");

    if (!lines.isEmpty()) {
      if ((lines.get(0).equals("Error: Unhandled exception:") || lines.get(0).equals("Unhandled exception:")) && lines.size() > 1) {
        return lines.get(1);
      }
      final Matcher matcher = ERROR_PATTERN.matcher(lines.get(0));
      if (matcher.find()) {
        return matcher.group(1);
      }
    }

    return "Cannot evaluate";
  }

  @Nullable
  public static ExpressionInfo getExpressionInfo(@NotNull final PsiElement contextElement) {
    // todo if sideEffectsAllowed return method call like "foo()", not only "foo"
    /* WEB-11715
     dart psi: notes.text

     REFERENCE_EXPRESSION
     REFERENCE_EXPRESSION “notes”
     PsiElement(.) “.”
     REFERENCE_EXPRESSION “text”
     */
    // find topmost reference, but stop if argument list found
    DartReference reference = null;
    PsiElement element = contextElement;
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
          element instanceof DartFunctionBody || element instanceof IDartBlock) {
        break;
      }
    }

    if (reference != null) {
      TextRange textRange = reference.getTextRange();
      // note<CURSOR>s.text - the whole reference expression is notes.txt, but we must return only notes
      int endOffset = contextElement.getTextRange().getEndOffset();
      if (textRange.getEndOffset() != endOffset) {
        textRange = new TextRange(textRange.getStartOffset(), endOffset);
      }
      return new ExpressionInfo(textRange);
    }

    PsiElement parent = contextElement.getParent();
    return parent instanceof DartId ? new ExpressionInfo(parent.getTextRange()) : null;
  }

  @Override
  public boolean isCodeFragmentEvaluationSupported() {
    return false;
  }

  @Override
  public void evaluate(@NotNull final String expression,
                       @NotNull final XEvaluationCallback callback,
                       @Nullable final XSourcePosition expressionPosition) {
    myDebugProcess.getVmServiceWrapper().evaluateInFrame(myIsolateId, myFrame, expression, callback);
  }

  @Nullable
  @Override
  public ExpressionInfo getExpressionInfoAtOffset(@NotNull final Project project,
                                                  @NotNull final Document document,
                                                  final int offset,
                                                  final boolean sideEffectsAllowed) {
    final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
    final PsiElement contextElement = psiFile == null ? null : psiFile.findElementAt(offset);
    return contextElement == null ? null : getExpressionInfo(contextElement);
  }
}
