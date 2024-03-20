// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.assists;

import com.google.common.collect.Maps;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.lookup.LookupElementRenderer;
import com.intellij.codeInsight.template.TemplateBuilderFactory;
import com.intellij.codeInsight.template.TemplateBuilderImpl;
import com.intellij.codeInsight.template.impl.ConstantNode;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.IconManager;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartFileInfo;
import com.jetbrains.lang.dart.analyzer.DartFileInfoKt;
import com.jetbrains.lang.dart.analyzer.DartLocalFileInfo;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

public final class AssistUtils {
  /**
   * @return {@code true} if file contents changed, {@code false} otherwise
   */
  public static boolean applyFileEdit(@NotNull final Project project, @NotNull final SourceFileEdit fileEdit) {
    final VirtualFile file = findVirtualFile(fileEdit);
    final Document document = file == null ? null : FileDocumentManager.getInstance().getDocument(file);
    if (document == null) return false;

    final long initialModStamp = document.getModificationStamp();
    applySourceEdits(project, file, document, fileEdit.getEdits(), Collections.emptySet());
    return document.getModificationStamp() != initialModStamp;
  }

  public static void applySourceChange(@NotNull final Project project,
                                       @NotNull final SourceChange sourceChange,
                                       final boolean withLinkedEdits) throws DartSourceEditException {
    applySourceChange(project, sourceChange, withLinkedEdits, Collections.emptySet());
  }

  public static void applySourceChange(@NotNull final Project project,
                                       @NotNull final SourceChange sourceChange,
                                       final boolean withLinkedEdits,
                                       @NotNull final Set<String> excludedIds) throws DartSourceEditException {
    final Map<VirtualFile, SourceFileEdit> changeMap = getContentFilesChanges(project, sourceChange, excludedIds);
    // ensure not read-only
    {
      final Set<VirtualFile> files = changeMap.keySet();
      final boolean okToWrite = FileModificationService.getInstance().prepareVirtualFilesForWrite(project, files);
      if (!okToWrite) {
        return;
      }
    }

    // do apply the change
    @NlsSafe String message = sourceChange.getMessage();
    CommandProcessor.getInstance().executeCommand(project, () -> {
      final ChangeTarget linkedEditTarget = withLinkedEdits ? findChangeTarget(project, sourceChange) : null;
      List<SourceEditInfo> sourceEditInfos = null;

      for (Map.Entry<VirtualFile, SourceFileEdit> entry : changeMap.entrySet()) {
        final VirtualFile file = entry.getKey();
        final SourceFileEdit fileEdit = entry.getValue();
        final Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document != null) {
          final List<SourceEditInfo> infos = applySourceEdits(project, file, document, fileEdit.getEdits(), excludedIds);

          if (linkedEditTarget != null && linkedEditTarget.virtualFile.equals(file)) {
            sourceEditInfos = infos;
          }
        }
      }

      if (withLinkedEdits) {
        if (sourceEditInfos != null) {
          runLinkedEdits(project, sourceChange, linkedEditTarget, sourceEditInfos);
        }
        else if (sourceChange.getSelection() != null) {
          Position selection = sourceChange.getSelection();
          String filePathOrUri = selection.getFile();
          DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(filePathOrUri);
          VirtualFile file = fileInfo instanceof DartLocalFileInfo localFileInfo ? localFileInfo.findFile() : null;
          if (file != null) {
            int offset = selection.getOffset();
            offset = DartAnalysisServerService.getInstance(project).getConvertedOffset(file, offset);
            navigate(project, file, offset);
          }
        }
      }
    }, message, null);
  }

  public static List<SourceEditInfo> applySourceEdits(@NotNull final Project project,
                                                      @NotNull final VirtualFile file,
                                                      @NotNull final Document document,
                                                      @NotNull final List<? extends SourceEdit> edits,
                                                      @NotNull final Set<String> excludedIds) {
    final List<SourceEditInfo> result = new ArrayList<>(edits.size());
    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(project);

    for (SourceEdit edit : edits) {
      if (excludedIds.contains(edit.getId())) {
        continue;
      }

      final int offset = service.getConvertedOffset(file, edit.getOffset());
      final int length = service.getConvertedOffset(file, edit.getOffset() + edit.getLength()) - offset;
      final String replacement = StringUtil.convertLineSeparators(edit.getReplacement());

      for (SourceEditInfo info : result) {
        if (info.resultingOriginalOffset > edit.getOffset()) {
          info.resultingOriginalOffset -= edit.getLength();
          info.resultingOriginalOffset += edit.getReplacement().length();
          info.resultingConvertedOffset -= length;
          info.resultingConvertedOffset += replacement.length();
        }
      }

      result.add(new SourceEditInfo(edit.getOffset(), offset, edit.getLength(), length,
                                    edit.getReplacement(), replacement));

      if (length != replacement.length() ||
          !replacement.equals(document.getText(TextRange.create(offset, offset + length)))) {
        document.replaceString(offset, offset + length, replacement);
      }
    }

    return result;
  }

  @NotNull
  private static Map<VirtualFile, SourceFileEdit> getContentFilesChanges(@NotNull final Project project,
                                                                         @NotNull final SourceChange sourceChange,
                                                                         @NotNull final Set<String> excludedIds)
    throws DartSourceEditException {

    final Map<VirtualFile, SourceFileEdit> map = Maps.newHashMap();
    final List<SourceFileEdit> fileEdits = sourceChange.getEdits();
    for (SourceFileEdit fileEdit : fileEdits) {
      boolean allEditsExcluded = true;
      for (SourceEdit edit : fileEdit.getEdits()) {
        if (!excludedIds.contains(edit.getId())) {
          allEditsExcluded = false;
          break;
        }
      }

      if (allEditsExcluded) {
        continue;
      }

      final VirtualFile file = findVirtualFile(fileEdit);
      if (file == null) {
        String filePathOrUri = fileEdit.getFile();
        throw new DartSourceEditException(DartBundle.message("error.failed.to.edit.file.file.not.found.0", filePathOrUri));
      }

      if (isInContent(project, file)) {
        map.put(file, fileEdit);
      }
    }
    if (map.isEmpty() && !fileEdits.isEmpty()) {
      String filePathOrUri = fileEdits.get(0).getFile();
      throw new DartSourceEditException(DartBundle.message("error.none.of.the.files.were.in.this.project.content.0", filePathOrUri));
    }
    return map;
  }

  @Nullable
  private static ChangeTarget findChangeTarget(@NotNull final Project project, @NotNull final SourceChange sourceChange) {
    for (LinkedEditGroup group : sourceChange.getLinkedEditGroups()) {
      final List<Position> positions = group.getPositions();
      if (!positions.isEmpty()) {
        Position position = positions.get(0);
        String filePathOrUri = position.getFile();
        DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(filePathOrUri);
        VirtualFile virtualFile = fileInfo instanceof DartLocalFileInfo localFileInfo ? localFileInfo.findFile() : null;
        if (virtualFile == null) {
          return null;
        }
        // find PsiFile
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile == null) {
          return null;
        }

        return new ChangeTarget(project, virtualFile, psiFile, position.getOffset());
      }
    }
    return null;
  }

  private static int getLinkedEditConvertedOffset(@NotNull final Project project,
                                                  @NotNull final VirtualFile file,
                                                  final int linkedEditOffset,
                                                  @NotNull final List<? extends SourceEditInfo> editInfos) {
    // first check if linkedEditOffset is inside of some SourceEdit
    for (SourceEditInfo info : editInfos) {
      if (linkedEditOffset >= info.resultingOriginalOffset &&
          linkedEditOffset <= info.resultingOriginalOffset + info.originalReplacement.length()) {
        final String substring = info.originalReplacement.substring(0, linkedEditOffset - info.resultingOriginalOffset);
        final int crlfCount = StringUtil.getOccurrenceCount(substring, "\r\n");
        return info.resultingConvertedOffset + linkedEditOffset - info.resultingOriginalOffset - crlfCount;
      }
    }

    // if we are here, it means that linkedEditOffset is outside of all SourceEdits
    int leOffset = linkedEditOffset;

    // 1. find offset before any SourceEdits applied
    for (int i = editInfos.size() - 1; i >= 0; i--) {
      final SourceEditInfo info = editInfos.get(i);
      if (linkedEditOffset >= info.originalOffset) {
        leOffset -= info.originalReplacement.length();
        leOffset += info.originalLength;
      }
    }

    // 2. convert offset
    leOffset = DartAnalysisServerService.getInstance(project).getConvertedOffset(file, leOffset);

    // 3. find offset after all SourceEdits applied
    for (SourceEditInfo info : editInfos) {
      if (leOffset >= info.convertedOffset) {
        leOffset -= info.convertedLength;
        leOffset += info.normalizedReplacement.length();
      }
    }

    return leOffset;
  }

  public static @Nullable VirtualFile findVirtualFile(@NotNull SourceFileEdit fileEdit) {
    String filePathOrUri = fileEdit.getFile();
    DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(filePathOrUri);
    return fileInfo instanceof DartLocalFileInfo localFileInfo ? localFileInfo.findFile() : null;
  }

  private static boolean isInContent(@NotNull Project project, @NotNull VirtualFile file) {
    return ProjectRootManager.getInstance(project).getFileIndex().isInContent(file);
  }

  @Nullable
  public static Editor navigate(@NotNull final Project project, @NotNull final VirtualFile file, final int offset) {
    final OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file, offset);
    descriptor.setScrollType(ScrollType.MAKE_VISIBLE);
    descriptor.navigate(true);

    final FileEditor fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(file);
    return fileEditor instanceof TextEditor ? ((TextEditor)fileEditor).getEditor() : null;
  }

  private static void runLinkedEdits(@NotNull final Project project,
                                     @NotNull final SourceChange sourceChange,
                                     @NotNull final ChangeTarget target,
                                     @NotNull final List<? extends SourceEditInfo> sourceEditInfos) {
    final int caretOffset = getLinkedEditConvertedOffset(project, target.virtualFile, target.originalOffset, sourceEditInfos);
    final Editor editor = navigate(project, target.virtualFile, caretOffset);
    if (editor == null) {
      return;
    }

    // Commit changes, otherwise TemplateBuilderImpl#buildTemplate() sees the old psiFile text.
    PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
    final TemplateBuilderImpl builder = (TemplateBuilderImpl)TemplateBuilderFactory.getInstance().createTemplateBuilder(target.psiFile);
    boolean hasTextRanges = false;

    // fill the builder with ranges
    int groupIndex = 0;
    for (LinkedEditGroup group : sourceChange.getLinkedEditGroups()) {
      String mainVar = "group_" + groupIndex++;
      boolean firstPosition = true;
      groupIndex++;
      for (Position position : group.getPositions()) {
        String filePathOrUri = position.getFile();
        DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(filePathOrUri);
        if (fileInfo instanceof DartLocalFileInfo localFileInfo && localFileInfo.getFilePath().equals(target.virtualFile.getPath())) {
          hasTextRanges = true;

          final int offset = getLinkedEditConvertedOffset(project, target.virtualFile, position.getOffset(), sourceEditInfos);
          final int end = offset + group.getLength();
          final TextRange range = new TextRange(offset, end);
          if (firstPosition) {
            firstPosition = false;
            final String text = editor.getDocument().getText(range);
            ConstantNode expression =
              new ConstantNode(text).withLookupItems(ContainerUtil.map(group.getSuggestions(), AssistUtils::createLookupElement));
            builder.replaceRange(range, mainVar, expression, true);
          }
          else {
            final String positionVar = mainVar + "_" + offset;
            builder.replaceElement(range, positionVar, mainVar, false);
          }
        }
      }
    }
    // run the template
    if (hasTextRanges) {
      builder.run(editor, true);
    }
  }

  private static class SourceEditInfo {
    private final int originalOffset;
    private int resultingOriginalOffset;
    private final int convertedOffset;
    private int resultingConvertedOffset;
    private final int originalLength;
    private final int convertedLength;
    private final String originalReplacement;
    private final String normalizedReplacement;

    SourceEditInfo(int originalOffset, int convertedOffset, int originalLength, int convertedLength,
                   String originalReplacement, String normalizedReplacement) {
      this.originalOffset = originalOffset;
      resultingOriginalOffset = originalOffset;
      this.convertedOffset = convertedOffset;
      resultingConvertedOffset = convertedOffset;
      this.originalLength = originalLength;
      this.convertedLength = convertedLength;
      this.originalReplacement = originalReplacement;
      this.normalizedReplacement = normalizedReplacement;
    }
  }

  private static LookupElement createLookupElement(LinkedEditSuggestion suggestion) {
    String value = suggestion.getValue();
    return LookupElementBuilder.create(value).withRenderer(new LookupElementRenderer<>() {
      @Override
      public void renderElement(final LookupElement element, final LookupElementPresentation presentation) {
        final Icon icon = getIcon(suggestion.getKind());
        presentation.setIcon(icon);
        presentation.setItemText(value);
      }
    });
  }

  private static Icon getIcon(String suggestionKind) {
    if (LinkedEditSuggestionKind.METHOD.equals(suggestionKind)) {
      return IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Method);
    }
    if (LinkedEditSuggestionKind.PARAMETER.equals(suggestionKind)) {
      return IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Parameter);
    }
    if (LinkedEditSuggestionKind.TYPE.equals(suggestionKind)) {
      return IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Class);
    }
    if (LinkedEditSuggestionKind.VARIABLE.equals(suggestionKind)) {
      return IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Variable);
    }
    return null;
  }
}

class ChangeTarget {
  @NotNull final Project project;
  @NotNull final VirtualFile virtualFile;
  @NotNull final PsiFile psiFile;
  final int originalOffset;

  ChangeTarget(@NotNull final Project project,
               @NotNull final VirtualFile virtualFile,
               @NotNull final PsiFile psiFile,
               final int originalOffset) {
    this.project = project;
    this.originalOffset = originalOffset;
    this.virtualFile = virtualFile;
    this.psiFile = psiFile;
  }
}
