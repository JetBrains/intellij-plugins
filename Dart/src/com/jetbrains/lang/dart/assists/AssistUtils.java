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
import com.google.common.collect.Sets;
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
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.PlatformIcons;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AssistUtils {
  public static void applyFileEdit(@NotNull final SourceFileEdit fileEdit) {
    final Set<String> excludedIds = Sets.newHashSet();
    applyFileEdit(fileEdit, excludedIds);
  }

  public static void applyFileEdit(@NotNull final SourceFileEdit fileEdit, @NotNull final Set<String> excludedIds) {
    final VirtualFile file = findVirtualFile(fileEdit);
    if (file != null) {
      applyFileEdit(file, fileEdit, excludedIds);
    }
  }

  public static void applySourceChange(@NotNull final Project project, @NotNull final SourceChange sourceChange) {
    Set<String> excludedIds = Sets.newHashSet();
    applySourceChange(project, sourceChange, excludedIds);
  }

  public static void applySourceChange(@NotNull final Project project,
                                       @NotNull final SourceChange sourceChange,
                                       @NotNull final Set<String> excludedIds) {
    final Map<VirtualFile, SourceFileEdit> changeMap = getContentFilesChanges(project, sourceChange);
    // ensure not read-only
    {
      final Set<VirtualFile> files = changeMap.keySet();
      final boolean okToWrite = FileModificationService.getInstance().prepareVirtualFilesForWrite(project, files);
      if (!okToWrite) {
        return;
      }
    }
    // do apply the change
    CommandProcessor.getInstance().executeCommand(project, new Runnable() {
      @Override
      public void run() {
        for (Map.Entry<VirtualFile, SourceFileEdit> entry : changeMap.entrySet()) {
          final VirtualFile file = entry.getKey();
          final SourceFileEdit fileEdit = entry.getValue();
          applyFileEdit(file, fileEdit, excludedIds);
        }
        runLinkedEdits(project, sourceChange);
      }
    }, sourceChange.getMessage(), null);
  }

  public static void applySourceEdits(@NotNull final Document document, @NotNull final List<SourceEdit> edits) {
    final Set<String> excludedIds = Sets.newHashSet();
    applySourceEdits(document, edits, excludedIds);
  }

  public static void applySourceEdits(@NotNull final Document document,
                                      @NotNull final List<SourceEdit> edits,
                                      @NotNull final Set<String> excludedIds) {
    for (SourceEdit edit : edits) {
      if (excludedIds.contains(edit.getId())) {
        continue;
      }
      final int offset = edit.getOffset();
      final int length = edit.getLength();
      document.replaceString(offset, offset + length, edit.getReplacement());
    }
  }

  @NotNull
  public static Map<VirtualFile, SourceFileEdit> getContentFilesChanges(@NotNull final Project project,
                                                                        @NotNull final SourceChange sourceChange) {
    final Map<VirtualFile, SourceFileEdit> map = Maps.newHashMap();
    final List<SourceFileEdit> fileEdits = sourceChange.getEdits();
    for (SourceFileEdit fileEdit : fileEdits) {
      final VirtualFile file = findVirtualFile(fileEdit);
      if (file != null && isInContent(project, file)) {
        map.put(file, fileEdit);
      }
    }
    return map;
  }

  private static void applyFileEdit(@NotNull final VirtualFile file,
                                    @NotNull final SourceFileEdit fileEdit,
                                    @NotNull final Set<String> excludedIds) {
    final Document document = FileDocumentManager.getInstance().getDocument(file);
    if (document != null) {
      applySourceEdits(document, fileEdit.getEdits(), excludedIds);
    }
  }

  private static ChangeTarget findChangeTarget(@NotNull Project project, final SourceChange sourceChange) {
    for (LinkedEditGroup group : sourceChange.getLinkedEditGroups()) {
      final List<Position> positions = group.getPositions();
      if (!positions.isEmpty()) {
        final Position position = positions.get(0);
        final String path = position.getFile();
        // find VirtualFile
        VirtualFile virtualFile = findVirtualFileByPath(path);
        if (virtualFile == null) {
          return null;
        }
        // find PsiFile
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile == null) {
          return null;
        }
        // done
        return new ChangeTarget(project, path, position.getOffset(), virtualFile, psiFile);
      }
    }
    return null;
  }

  private static VirtualFile findVirtualFile(@NotNull SourceFileEdit fileEdit) {
    final String path = FileUtil.toSystemIndependentName(fileEdit.getFile());
    return findVirtualFileByPath(path);
  }

  @Nullable
  private static VirtualFile findVirtualFileByPath(String path) {
    path = FileUtil.toSystemIndependentName(path);
    return LocalFileSystem.getInstance().findFileByPath(path);
  }

  private static boolean isInContent(@NotNull Project project, @NotNull VirtualFile file) {
    return ProjectRootManager.getInstance(project).getFileIndex().isInContent(file);
  }

  @Nullable
  private static Editor navigate(@NotNull final Project project, @NotNull final VirtualFile file, final int offset) {
    final OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file, offset);
    descriptor.setScrollType(ScrollType.MAKE_VISIBLE);
    descriptor.navigate(true);

    final FileEditor fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(file);
    return fileEditor instanceof TextEditor ? ((TextEditor)fileEditor).getEditor() : null;
  }

  private static void runLinkedEdits(@NotNull Project project, @NotNull SourceChange sourceChange) {
    final ChangeTarget target = findChangeTarget(project, sourceChange);
    if (target == null) {
      return;
    }

    final Editor editor = navigate(project, target.virtualFile, target.offset);
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
        if (position.getFile().equals(target.path)) {
          hasTextRanges = true;
          final int offset = position.getOffset();
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
  @NotNull final String path;
  final int offset;
  @NotNull final VirtualFile virtualFile;
  @NotNull final PsiFile psiFile;

  ChangeTarget(@NotNull Project project, @NotNull String path, int offset, @NotNull VirtualFile virtualFile, @NotNull PsiFile psiFile) {
    this.project = project;
    this.path = path;
    this.offset = offset;
    this.virtualFile = virtualFile;
    this.psiFile = psiFile;
  }
}