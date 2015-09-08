package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.dartlang.analysis.server.protocol.SourceEdit;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartPreFormatProcessor implements PreFormatProcessor {
  public static Key<Boolean> FORMAT_MARK = new Key<Boolean>("FORMAT_MARK");
  public static Boolean FORMAT_MARKER = Boolean.TRUE;

  @NotNull
  @Override
  public TextRange process(@NotNull ASTNode element, @NotNull TextRange range) {
    // TODO return range if not in a paste operation
    final PsiElement psiElement = element.getPsi();
    if (psiElement == null) return range;

    if (!psiElement.getLanguage().is(DartLanguage.INSTANCE)) return range;

    if (!psiElement.isValid()) return range;
    PsiFile file = psiElement.getContainingFile();
    if (file == null || !file.isWritable()) return range;

    Project project = psiElement.getProject();

    final FileViewProvider viewProvider = file.getViewProvider();
    final Document document = viewProvider.getDocument();
    if (document == null) return range;

    PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
    documentManager.doPostponedOperationsAndUnblockDocument(document);
    documentManager.commitDocument(document);

    //final FileDocumentManager fileDocMgr = FileDocumentManager.getInstance();
    //fileDocMgr.saveDocument(document);

    FileEditorManager fileManager = FileEditorManager.getInstance(project);
    Editor editor = null;
    int selectionStart = 0, selectionLength = 0;
    if (fileManager != null) {
      TextEditor textEditor = (TextEditor)fileManager.getSelectedEditor(file.getVirtualFile());
      if (textEditor != null) {
        editor = textEditor.getEditor();
        selectionStart = editor.getSelectionModel().getSelectionStart();
        selectionLength = editor.getSelectionModel().getSelectionEnd() - selectionStart;
      }
    }

    final String path = FileUtil.toSystemDependentName(file.getVirtualFile().getPath());
    final int lineLength = CodeStyleSettingsManager.getSettings(project).getCommonSettings(DartLanguage.INSTANCE).RIGHT_MARGIN;
    final DartAnalysisServerService das = DartAnalysisServerService.getInstance();
    das.updateFilesContent();

    final DartAnalysisServerService.FormatResult formatResult = das.edit_format(path, selectionStart, selectionLength, lineLength);
    if (formatResult == null) return range;

    final List<SourceEdit> edits = formatResult.getEdits();
    if (edits == null || edits.size() != 1) return range;
    final int selectionOffset = formatResult.getOffset();
    final int selectionEnd = formatResult.getLength() + selectionOffset;
    final String code = edits.get(0).getReplacement();
    if (code == null) return range;

    final RangeMarker marker = document.createRangeMarker(range.getStartOffset(), range.getEndOffset(), true);
    document.replaceString(0, document.getTextLength(), code);

    element.putUserData(FORMAT_MARK, FORMAT_MARKER);
    // Without this commit, an error is thrown by some tests.
    // With it, some files get garbled. TODO Remove this comment.
    documentManager.commitDocument(document);

    if (editor != null) {
      editor.getCaretModel().moveToOffset(selectionOffset); // TODO Is this needed?
      editor.getSelectionModel().setSelection(selectionOffset, selectionEnd);
    }
    return TextRange.create(marker.getStartOffset(), marker.getEndOffset());
  }
}
