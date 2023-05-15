// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.ExpressionInfo;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XValue;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.vmService.VmServiceWrapper;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.dartlang.vm.service.consumer.GetObjectConsumer;
import org.dartlang.vm.service.element.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DartVmServiceEvaluator extends XDebuggerEvaluator {
  private static final Pattern ERROR_PATTERN = Pattern.compile("Error:.* line \\d+ pos \\d+: (.+)");

  protected final @NotNull DartVmServiceDebugProcess myDebugProcess;

  public DartVmServiceEvaluator(@NotNull DartVmServiceDebugProcess debugProcess) {
    myDebugProcess = debugProcess;
  }

  @Override
  public void evaluate(@NotNull String expression,
                       @NotNull XEvaluationCallback callback,
                       @Nullable XSourcePosition expressionPosition) {
    final String isolateId = myDebugProcess.getCurrentIsolateId();
    final Project project = myDebugProcess.getSession().getProject();
    final FileEditorManager manager = FileEditorManager.getInstance(project);
    PsiElement element = null;
    PsiFile psiFile = null;
    final List<VirtualFile> libraryFiles = new ArrayList<>();
    // Turn off pausing on exceptions as it is confusing to mouse over an expression
    // and to have that trigger pausing at an exception.
    final VmServiceWrapper vmService = myDebugProcess.getVmServiceWrapper();
    if (vmService == null) {
      // not connected to the VM yet
      callback.errorOccurred(DartBundle.message("debugger.error.no.connection.to.dart.vm"));
      return;
    }

    vmService.setExceptionPauseMode(ExceptionPauseMode.None);
    final XEvaluationCallback wrappedCallback = new XEvaluationCallback() {
      @Override
      public void evaluated(@NotNull XValue result) {
        vmService.setExceptionPauseMode(myDebugProcess.getBreakOnExceptionMode());
        callback.evaluated(result);
      }

      @Override
      public void errorOccurred(@NotNull String errorMessage) {
        vmService.setExceptionPauseMode(myDebugProcess.getBreakOnExceptionMode());
        callback.errorOccurred(errorMessage);
      }
    };
    if (expressionPosition != null) {
      psiFile = PsiManager.getInstance(project).findFile(expressionPosition.getFile());
      if (psiFile != null) {
        element = psiFile.findElementAt(expressionPosition.getOffset());
      }
    }
    else {
      // TODO(jacobr): we could use the most recently selected Dart file instead
      // of using the selected file.
      final FileEditor editor = manager.getSelectedEditor();
      if (editor instanceof TextEditor textEditor) {
        final FileEditorLocation fileEditorLocation = textEditor.getCurrentLocation();
        final VirtualFile virtualFile = editor.getFile();
        if (virtualFile != null) {
          psiFile = PsiManager.getInstance(project).findFile(virtualFile);
          if (psiFile != null && fileEditorLocation instanceof TextEditorLocation textEditorLocation) {
            element = psiFile.findElementAt(textEditor.getEditor().logicalPositionToOffset(textEditorLocation.getPosition()));
          }
        }
      }
    }

    if (psiFile != null) {
      libraryFiles.addAll(DartResolveUtil.findLibrary(psiFile));
    }

    if (isolateId == null) {
      wrappedCallback.errorOccurred(DartBundle.message("debugger.error.no.running.isolate"));
      return;
    }
    final DartClass dartClass = element != null ? PsiTreeUtil.getParentOfType(element, DartClass.class) : null;
    final String dartClassName = dartClass != null ? dartClass.getName() : null;

    vmService.getCachedIsolate(isolateId).whenComplete((isolate, error) -> {
      if (error != null) {
        wrappedCallback.errorOccurred(error.getMessage());
        return;
      }

      LibraryRef libraryRef = isolate != null ? findMatchingLibrary(isolate, libraryFiles) : null;
      if (libraryRef == null) {
        wrappedCallback.errorOccurred(DartBundle.message("debugger.error.cannot.evaluate"));
        return;
      }

      if (dartClassName != null) {
        vmService.getObject(isolateId, libraryRef.getId(), new GetObjectConsumer() {

          @Override
          public void onError(RPCError error) {
            @NlsSafe String message = error.getMessage();
            wrappedCallback.errorOccurred(message);
          }

          @Override
          public void received(Obj response) {
            Library library = (Library)response;
            for (ClassRef classRef : library.getClasses()) {
              if (classRef.getName().equals(dartClassName)) {
                vmService.evaluateInTargetContext(isolateId, classRef.getId(), expression, wrappedCallback);
                return;
              }
            }

            // Class not found so just use the library.
            vmService.evaluateInTargetContext(isolateId, libraryRef.getId(), expression, wrappedCallback);
          }

          @Override
          public void received(Sentinel response) {
            @NlsSafe String errorMessage = response.getValueAsString();
            wrappedCallback.errorOccurred(errorMessage);
          }
        });
      }
      else {
        vmService.evaluateInTargetContext(isolateId, libraryRef.getId(), expression, wrappedCallback);
      }
    });
  }

  private @Nullable LibraryRef findMatchingLibrary(@NotNull Isolate isolate, @NotNull List<VirtualFile> libraryFiles) {
    if (!libraryFiles.isEmpty()) {
      Set<String> uris = new HashSet<>();

      for (VirtualFile libraryFile : libraryFiles) {
        uris.addAll(myDebugProcess.getUrisForFile(libraryFile));
      }

      for (LibraryRef library : isolate.getLibraries()) {
        if (uris.contains(library.getUri())) {
          return library;
        }
      }
    }
    return isolate.getRootLib();
  }

  @Override
  public @Nullable ExpressionInfo getExpressionInfoAtOffset(@NotNull Project project,
                                                            @NotNull Document document,
                                                            int offset,
                                                            boolean sideEffectsAllowed) {
    final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
    final PsiElement contextElement = psiFile == null ? null : psiFile.findElementAt(offset);
    return contextElement == null ? null : getExpressionInfo(contextElement);
  }

  public static @NotNull @Nls String getPresentableError(@NotNull String rawError) {
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

    return DartBundle.message("debugger.error.cannot.evaluate");
  }

  public static @Nullable ExpressionInfo getExpressionInfo(@NotNull PsiElement contextElement) {
    // todo if sideEffectsAllowed return method call like "foo()", not only "foo"
    /* WEB-11715
     dart psi: notes.text

     REFERENCE_EXPRESSION
     REFERENCE_EXPRESSION "notes"
     PsiElement(.) "."
     REFERENCE_EXPRESSION "text"
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
}
