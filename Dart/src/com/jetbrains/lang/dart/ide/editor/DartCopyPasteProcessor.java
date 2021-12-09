// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.editorActions.CopyPastePostProcessor;
import com.intellij.codeInsight.editorActions.TextBlockTransferableData;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.DoNotAskOption;
import com.intellij.openapi.ui.MessageDialogBuilder;
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
import org.dartlang.analysis.server.protocol.SourceFileEdit;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public final class DartCopyPasteProcessor extends CopyPastePostProcessor<DartCopyPasteProcessor.DartImportsTransferableData> {
  private static final DataFlavor FLAVOR = new DataFlavor(DartImportsTransferableData.class, "Dart imports");

  @NotNull
  @Override
  public List<DartImportsTransferableData> collectTransferableData(@NotNull final PsiFile psiFile,
                                                                   @NotNull final Editor editor,
                                                                   final int @NotNull [] startOffsets,
                                                                   final int @NotNull [] endOffsets) {
    if (!(psiFile instanceof DartFile)) return Collections.emptyList();
    final Project project = psiFile.getProject();
    final VirtualFile vFile = DartResolveUtil.getRealVirtualFile(psiFile);
    if (!DartAnalysisServerService.isLocalAnalyzableFile(vFile)) return Collections.emptyList();
    if (DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE == CodeInsightSettings.NO) return Collections.emptyList();
    final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
    if (module == null || !DartSdkLibUtil.isDartSdkEnabled(module)) return Collections.emptyList();
    if (!ProjectFileIndex.getInstance(project).isInContent(vFile)) return Collections.emptyList();
    final DartAnalysisServerService das = DartAnalysisServerService.getInstance(project);
    if (!das.serverReadyForRequest()) return Collections.emptyList();

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
                                      final Ref<? super Boolean> indented,
                                      final List<? extends DartImportsTransferableData> values) {
    final DartImportsTransferableData data = ContainerUtil.getFirstItem(values);
    if (data == null) return;

    final VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
    if (file == null || !FileTypeRegistry.getInstance().isFileOfType(file, DartFileType.INSTANCE) || !DartAnalysisServerService.isLocalAnalyzableFile(file)) return;

    if (DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE == CodeInsightSettings.NO) return;

    final SourceFileEdit edit =
      DartAnalysisServerService.getInstance(project).edit_importElements(file, data.getImportedElements(), caretOffset);

    if (edit != null && edit.getEdits() != null && !edit.getEdits().isEmpty()) {
      if (DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE == CodeInsightSettings.ASK &&
          !ApplicationManager.getApplication().isUnitTestMode()) {
        final String message = DartBundle.message("dialog.paste.on.import.text");
        final String title = DartBundle.message("dialog.paste.on.import.title");

        final DoNotAskOption doNotAskOption = new DoNotAskOption.Adapter() {
          @Override
          public boolean shouldSaveOptionsOnCancel() {
            return true;
          }

          @Override
          public void rememberChoice(boolean isSelected, int exitCode) {
            if (!isSelected) return;

            if (exitCode == DialogWrapper.OK_EXIT_CODE) {
              DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE = CodeInsightSettings.YES;
            }
            else if (exitCode == DialogWrapper.CANCEL_EXIT_CODE) {
              DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE = CodeInsightSettings.NO;
            }
          }
        };

        if (!MessageDialogBuilder.yesNo(title, message).doNotAsk(doNotAskOption).ask(project)) {
          return;
        }
      }

      WriteAction.run(() -> AssistUtils.applyFileEdit(project, edit));
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
