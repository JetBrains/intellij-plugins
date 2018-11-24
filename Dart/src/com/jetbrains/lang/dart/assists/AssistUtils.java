/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.assists;

import com.google.common.collect.Maps;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.lookup.LookupElementRenderer;
import com.intellij.codeInsight.template.*;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.PlatformIcons;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

public class AssistUtils {
  /**
   * @return <code>true</code> if file contents changed, <code>false</code> otherwise
   */
  public static boolean applyFileEdit(@NotNull final SourceFileEdit fileEdit) {
    final VirtualFile file = findVirtualFile(fileEdit);
    final Document document = file == null ? null : FileDocumentManager.getInstance().getDocument(file);
    if (document == null) return false;

    final long initialModStamp = document.getModificationStamp();
    applySourceEdits(file, document, fileEdit.getEdits(), Collections.emptySet());
    return document.getModificationStamp() != initialModStamp;
  }

  public static void applySourceChange(@NotNull final Project project,
                                       @NotNull final SourceChange sourceChange,
                                       final boolean withLinkedEdits) throws DartSourceEditException {
    Set<String> excludedIds = Collections.emptySet();
    applySourceChange(project, sourceChange, withLinkedEdits, excludedIds);
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
    CommandProcessor.getInstance().executeCommand(project, () -> {
      final ChangeTarget linkedEditTarget = withLinkedEdits ? findChangeTarget(project, sourceChange) : null;
      List<SourceEditInfo> sourceEditInfos = null;

      for (Map.Entry<VirtualFile, SourceFileEdit> entry : changeMap.entrySet()) {
        final VirtualFile file = entry.getKey();
        final SourceFileEdit fileEdit = entry.getValue();
        final Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document != null) {
          final List<SourceEditInfo> infos = applySourceEdits(file, document, fileEdit.getEdits(), excludedIds);

          if (linkedEditTarget != null && linkedEditTarget.virtualFile.equals(file)) {
            sourceEditInfos = infos;
          }
        }
      }

      if (withLinkedEdits && sourceEditInfos != null) {
        runLinkedEdits(project, sourceChange, linkedEditTarget, sourceEditInfos);
      }
    }, sourceChange.getMessage(), null);
  }

  public static void applySourceEdits(@NotNull final VirtualFile file,
                                      @NotNull final Document document,
                                      @NotNull final List<SourceEdit> edits) {
    final Set<String> excludedIds = Collections.emptySet();
    applySourceEdits(file, document, edits, excludedIds);
  }

  public static List<SourceEditInfo> applySourceEdits(@NotNull final VirtualFile file,
                                                      @NotNull final Document document,
                                                      @NotNull final List<SourceEdit> edits,
                                                      @NotNull final Set<String> excludedIds) {
    final List<SourceEditInfo> result = new ArrayList<>(edits.size());
    final DartAnalysisServerService service = DartAnalysisServerService.getInstance();

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
  public static Map<VirtualFile, SourceFileEdit> getContentFilesChanges(@NotNull final Project project,
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
        throw new DartSourceEditException("Failed to edit file, file not found: " + fileEdit.getFile());
      }
      if (!isInContent(project, file)) {
        throw new DartSourceEditException("Can't edit file outside of the project content: " + fileEdit.getFile());
      }
      map.put(file, fileEdit);
    }
    return map;
  }

  @Nullable
  private static ChangeTarget findChangeTarget(@NotNull final Project project, @NotNull final SourceChange sourceChange) {
    for (LinkedEditGroup group : sourceChange.getLinkedEditGroups()) {
      final List<Position> positions = group.getPositions();
      if (!positions.isEmpty()) {
        final Position position = positions.get(0);
        // find VirtualFile
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(FileUtil.toSystemIndependentName(position.getFile()));
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

  private static int getLinkedEditConvertedOffset(@NotNull final VirtualFile file,
                                                  final int linkedEditOffset,
                                                  @NotNull final List<SourceEditInfo> editInfos) {
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
    leOffset = DartAnalysisServerService.getInstance().getConvertedOffset(file, leOffset);

    // 3. find offset after all SourceEdits applied
    for (SourceEditInfo info : editInfos) {
      if (leOffset >= info.convertedOffset) {
        leOffset -= info.convertedLength;
        leOffset += info.normalizedReplacement.length();
      }
    }

    return leOffset;
  }

  @Nullable
  public static VirtualFile findVirtualFile(@NotNull final SourceFileEdit fileEdit) {
    return LocalFileSystem.getInstance().findFileByPath(FileUtil.toSystemIndependentName(fileEdit.getFile()));
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
                                     @NotNull final List<SourceEditInfo> sourceEditInfos) {
    final int caretOffset = getLinkedEditConvertedOffset(target.virtualFile, target.originalOffset, sourceEditInfos);
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
        if (FileUtil.toSystemIndependentName(position.getFile()).equals(target.virtualFile.getPath())) {
          hasTextRanges = true;

          final int offset = getLinkedEditConvertedOffset(target.virtualFile, position.getOffset(), sourceEditInfos);
          final int end = offset + group.getLength();
          final TextRange range = new TextRange(offset, end);
          if (firstPosition) {
            firstPosition = false;
            final String text = editor.getDocument().getText(range);
            DartLookupExpression expression = new DartLookupExpression(text, group.getSuggestions());
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

    public SourceEditInfo(int originalOffset, int convertedOffset, int originalLength, int convertedLength,
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
}

class DartLookupExpression extends Expression {
  private final @NotNull String myText;
  private final @NotNull List<LinkedEditSuggestion> mySuggestions;

  DartLookupExpression(@NotNull String text, @NotNull List<LinkedEditSuggestion> suggestions) {
    myText = text;
    mySuggestions = suggestions;
  }

  @Nullable
  @Override
  public LookupElement[] calculateLookupItems(final ExpressionContext context) {
    final int length = mySuggestions.size();
    final LookupElement[] elements = new LookupElement[length];
    for (int i = 0; i < length; i++) {
      final LinkedEditSuggestion suggestion = mySuggestions.get(i);
      final String value = suggestion.getValue();
      elements[i] = LookupElementBuilder.create(value).withRenderer(new LookupElementRenderer<LookupElement>() {
        @Override
        public void renderElement(final LookupElement element, final LookupElementPresentation presentation) {
          final Icon icon = getIcon(suggestion.getKind());
          presentation.setIcon(icon);
          presentation.setItemText(value);
        }
      });
    }
    return elements;
  }

  @Override
  public Result calculateQuickResult(ExpressionContext context) {
    return new TextResult(myText);
  }

  @Override
  public Result calculateResult(ExpressionContext context) {
    return calculateQuickResult(context);
  }

  private static Icon getIcon(String suggestionKind) {
    if (LinkedEditSuggestionKind.METHOD.equals(suggestionKind)) {
      return PlatformIcons.METHOD_ICON;
    }
    if (LinkedEditSuggestionKind.PARAMETER.equals(suggestionKind)) {
      return PlatformIcons.PARAMETER_ICON;
    }
    if (LinkedEditSuggestionKind.TYPE.equals(suggestionKind)) {
      return PlatformIcons.CLASS_ICON;
    }
    if (LinkedEditSuggestionKind.VARIABLE.equals(suggestionKind)) {
      return PlatformIcons.VARIABLE_ICON;
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
