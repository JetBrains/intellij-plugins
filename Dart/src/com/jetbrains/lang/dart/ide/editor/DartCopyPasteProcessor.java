package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.editorActions.CopyPastePostProcessor;
import com.intellij.codeInsight.editorActions.TextBlockTransferableData;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.ide.codeInsight.DartCodeInsightSettings;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.dartlang.analysis.server.protocol.ImportedElements;
import org.dartlang.analysis.server.protocol.SourceEdit;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DartCopyPasteProcessor extends CopyPastePostProcessor<DartCopyPasteProcessor.DartImportsTransferableData> {
  private static final DataFlavor FLAVOR = new DataFlavor(DartImportsTransferableData.class, "Dart imports");

  @NotNull
  @Override
  public List<DartImportsTransferableData> collectTransferableData(@NotNull final PsiFile psiFile,
                                                                   @NotNull final Editor editor,
                                                                   @NotNull final int[] startOffsets,
                                                                   @NotNull final int[] endOffsets) {
    if (!(psiFile instanceof DartFile)) return Collections.emptyList();
    final Project project = psiFile.getProject();
    final VirtualFile vFile = DartResolveUtil.getRealVirtualFile(psiFile);
    if (!DartAnalysisServerService.isLocalAnalyzableFile(vFile)) return Collections.emptyList();
    if (DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE == CodeInsightSettings.NO) return Collections.emptyList();
    final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
    if (module == null || !DartSdkLibUtil.isDartSdkEnabled(module)) return Collections.emptyList();
    if (!ProjectFileIndex.getInstance(project).isInContent(vFile)) return Collections.emptyList();
    final DartAnalysisServerService das = DartAnalysisServerService.getInstance(project);
    if (!das.serverReadyForRequest(project)) return Collections.emptyList();

    das.updateFilesContent();

    final List<ImportedElements> importedElements = new SmartList<>();
    for (int i = 0; i < startOffsets.length; i++) {
      final int offset = startOffsets[i];
      final int length = endOffsets[i] - startOffsets[i];
      final List<ImportedElements> elements = das.analysis_getImportedElements(vFile, offset, length);
      if (elements != null) {
        importedElements.addAll(elements);
      }
    }

    return !importedElements.isEmpty() ? Collections.singletonList(new DartImportsTransferableData(importedElements))
                                       : Collections.emptyList();
  }

  @NotNull
  @Override
  public List<DartImportsTransferableData> extractTransferableData(@NotNull final Transferable content) {
    try {
      final Object data = content.getTransferData(FLAVOR);
      if (data instanceof DartImportsTransferableData) {
        return Collections.singletonList((DartImportsTransferableData)data);
      }
    }
    catch (UnsupportedFlavorException | IOException ignored) {/**/}
    return Collections.emptyList();
  }

  @Override
  public void processTransferableData(@NotNull final Project project,
                                      @NotNull final Editor editor,
                                      @NotNull final RangeMarker bounds,
                                      final int caretOffset,
                                      @NotNull final Ref<Boolean> indented,
                                      @NotNull final List<DartImportsTransferableData> values) {
    final DartImportsTransferableData data = ContainerUtil.getFirstItem(values);
    if (data == null) return;

    final VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
    if (file == null || file.getFileType() != DartFileType.INSTANCE || !DartAnalysisServerService.isLocalAnalyzableFile(file)) return;

    if (DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE == CodeInsightSettings.NO) return;

    final List<SourceEdit> edits = DartAnalysisServerService.getInstance(project).edit_importElements(file, data.getImportedElements());
    if (edits != null && !edits.isEmpty()) {
      final String message = DartBundle.message("dialog.paste.on.import.text");
      final String title = DartBundle.message("dialog.paste.on.import.title");
      if (DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE == CodeInsightSettings.ASK &&
          Messages.showYesNoDialog(project, message, title, Messages.getQuestionIcon()) != Messages.YES) {
        return;
      }
      WriteAction.run(() -> AssistUtils.applySourceEdits(project, file, editor.getDocument(), edits, Collections.emptySet()));
    }
  }

  public static class DartImportsTransferableData implements TextBlockTransferableData {
    @NotNull private final List<ImportedElements> myImportedElements;

    public DartImportsTransferableData(@NotNull final List<ImportedElements> importedElements) {
      myImportedElements = importedElements;
    }

    @NotNull
    public List<ImportedElements> getImportedElements() {
      return myImportedElements;
    }

    @Override
    public DataFlavor getFlavor() {
      return FLAVOR;
    }

    @Override
    public int getOffsetCount() {
      return 0;
    }

    @Override
    public int getOffsets(int[] offsets, int index) {
      return index;
    }

    @Override
    public int setOffsets(int[] offsets, int index) {
      return index;
    }
  }
}
