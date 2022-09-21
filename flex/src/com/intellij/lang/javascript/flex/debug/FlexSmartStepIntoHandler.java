package com.intellij.lang.javascript.flex.debug;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.presentable.JSFormatUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.util.PsiFormatUtilBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.stepping.PsiBackedSmartStepIntoVariant;
import com.intellij.xdebugger.stepping.XSmartStepIntoHandler;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

final class FlexSmartStepIntoHandler extends XSmartStepIntoHandler<PsiBackedSmartStepIntoVariant> {
  private final FlexDebugProcess myDebugProcess;

  FlexSmartStepIntoHandler(FlexDebugProcess debugProcess) {
    myDebugProcess = debugProcess;
  }

  @NotNull
  @Override
  public List<PsiBackedSmartStepIntoVariant> computeSmartStepVariants(@NotNull XSourcePosition position) {
    final Document document = FileDocumentManager.getInstance().getDocument(position.getFile());

    final SortedMap<PsiElement, PsiBackedSmartStepIntoVariant> element2candidateMap =
      new TreeMap<>(Comparator.comparingInt(PsiElement::getTextOffset));

    compute(document, element2candidateMap, new HashSet<>(), position.getLine(), position.getOffset());

    final List<PsiBackedSmartStepIntoVariant> variants = new ArrayList<>();

    for (final PsiElement key : element2candidateMap.keySet()) {
      final PsiBackedSmartStepIntoVariant variant = element2candidateMap.get(key);
      if (!variants.contains(variant)) {
        variants.add(variant);
      }
    }

    return variants;
  }

  private void compute(Document document,
                       final Map<PsiElement, PsiBackedSmartStepIntoVariant> element2candidateMap,
                       final Set<PsiElement> visited,
                       final int line,
                       final int offset) {
    XDebuggerUtil.getInstance().iterateLine(myDebugProcess.getSession().getProject(), document, line, psiElement -> {
      addVariants(psiElement, element2candidateMap, visited, offset);
      return true;
    });
  }

  private void addVariants(PsiElement psiElement,
                           final Map<PsiElement, PsiBackedSmartStepIntoVariant> element2candidateMap,
                           final Set<PsiElement> visited,
                           final int offset) {
    PsiLanguageInjectionHost injectionHost = PsiTreeUtil.getParentOfType(psiElement, PsiLanguageInjectionHost.class);

    if (injectionHost != null) {
      visited.add(injectionHost);

      InjectedLanguageManager.getInstance(injectionHost.getProject()).enumerate(injectionHost, new JSResolveUtil.JSInjectedFilesVisitor() {
        @Override
        protected void process(JSFile file) {
          Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
          int fileOffsetInHost =
            InjectedLanguageManager.getInstance(file.getProject()).injectedToHost(file, file.getTextRange().getStartOffset());
          int offsetInInjected = offset - fileOffsetInHost;
          compute(document, element2candidateMap, visited, document.getLineNumber(offsetInInjected), offset);
        }
      });
      return;
    }

    JSReferenceExpression expr = PsiTreeUtil.getParentOfType(psiElement, JSReferenceExpression.class);
    if (expr != null) {
      if (!visited.contains(expr)) {
        visited.add(expr);
        PsiElement resolve = expr.resolve();

        if (resolve instanceof JSFunction) {
          JSFunction fun = (JSFunction)resolve;
          PsiElement responsibleElement = expr;

          PsiElement parent = responsibleElement.getParent();
          if (parent instanceof JSDefinitionExpression) {
            responsibleElement = parent.getParent();
          }
          else if (parent instanceof JSCallExpression) responsibleElement = parent;

          element2candidateMap.put(responsibleElement, new JSFunctionSmartStepIntoVariant(fun));
        }
      }
    }
  }

  @Override
  public void startStepInto(@NotNull final PsiBackedSmartStepIntoVariant stepIntoVariant) {
    myDebugProcess.sendCommand(new DebuggerCommand("bt", CommandOutputProcessingType.SPECIAL_PROCESSING) {
      @Override
      CommandOutputProcessingMode onTextAvailable(@NonNls String s) {
        startStepInto(stepIntoVariant, getStackTraceFromBtCommandOutput(s));
        return super.onTextAvailable(s);
      }
    });
  }

  private void startStepInto(final PsiBackedSmartStepIntoVariant stepIntoVariant, final String[] originalStackTrace) {
    myDebugProcess.sendCommand(new DebuggerCommand("step", CommandOutputProcessingType.SPECIAL_PROCESSING) {

      @Override
      CommandOutputProcessingMode onTextAvailable(@NonNls String s) {
        myDebugProcess.sendCommand(new DebuggerCommand("bt", CommandOutputProcessingType.SPECIAL_PROCESSING) {

          @Override
          CommandOutputProcessingMode onTextAvailable(@NonNls String s) {
            handleStepInto(stepIntoVariant, originalStackTrace, s);
            return super.onTextAvailable(s);
          }
        });

        return super.onTextAvailable(s);
      }
    });
  }

  private void handleStepInto(final PsiBackedSmartStepIntoVariant stepIntoVariant,
                              final String[] originalStackTrace,
                              final String btCommandOutput) {
    if (isCorrectFrameReached(stepIntoVariant, btCommandOutput)) {
      // ok
      myDebugProcess.sendCommand(new DumpSourceLocationCommand(myDebugProcess));
    }
    else if (!arrayEndsWith(getStackTraceFromBtCommandOutput(btCommandOutput), originalStackTrace)) {
      // Impossible to perform step into, so just continue.
      myDebugProcess.sendCommand(new FlexDebugProcess.ContinueCommand());
    }
    else {
      // step out and try to step in once more
      myDebugProcess.sendCommand(new DebuggerCommand("finish", CommandOutputProcessingType.SPECIAL_PROCESSING));
      startStepInto(stepIntoVariant, originalStackTrace);
    }
  }

  /**
   * Splits input string and removes frame number
   */
  private static String[] getStackTraceFromBtCommandOutput(final String btCommandOutput) {
    final String[] frames = FlexSuspendContext.splitStackFrames(btCommandOutput);
    for (int i = 0; i < frames.length; i++) {
      final String frame = frames[i];
      frames[i] = frame.substring(frame.indexOf(" ") + 1).trim();
    }
    return frames;
  }

  private static boolean isCorrectFrameReached(final PsiBackedSmartStepIntoVariant stepIntoVariant, final String btCommandOutput) {
    final String functionName = stepIntoVariant.getElement().getName();
    final String scope = FlexSuspendContext.extractScope(FlexSuspendContext.splitStackFrames(btCommandOutput)[0]);
    return scope.equals(functionName) || scope.startsWith(functionName + ":") || scope.contains(" " + functionName + ":");
  }


  private static boolean arrayEndsWith(final String[] array, final String[] subArray) {
    return ArrayUtil.startsWith(ArrayUtil.reverseArray(array), ArrayUtil.reverseArray(subArray));
  }

  @Override
  public String getPopupTitle(@NotNull XSourcePosition position) {
    return FlexBundle.message("popup.title.step.into.function");
  }

  private static class JSFunctionSmartStepIntoVariant extends PsiBackedSmartStepIntoVariant<JSFunction> {

    JSFunctionSmartStepIntoVariant(@NotNull JSFunction element) {
      super(element);
    }

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final JSFunctionSmartStepIntoVariant that = (JSFunctionSmartStepIntoVariant)o;

      if (!getElement().equals(that.getElement())) return false;

      return true;
    }

    public int hashCode() {
      return getElement().hashCode();
    }

    @Override
    public String getText() {
      return JSFormatUtil.formatMethod(getElement(), PsiFormatUtilBase.SHOW_NAME | PsiFormatUtilBase.SHOW_PARAMETERS, PsiFormatUtilBase.SHOW_TYPE);
    }
  }
}
