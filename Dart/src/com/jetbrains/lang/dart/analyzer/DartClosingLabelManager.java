// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.analyzer;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.FontPreferences;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.impl.ComplementaryFontsRegistry;
import com.intellij.openapi.editor.impl.FontInfo;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EventDispatcher;
import com.jetbrains.lang.dart.ide.codeInsight.DartCodeInsightSettings;
import org.dartlang.analysis.server.protocol.ClosingLabel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DartClosingLabelManager implements @NotNull Disposable {
  private final EventDispatcher<PreferenceChangeListener> myEventDispatcher = EventDispatcher.create(PreferenceChangeListener.class);

  interface PreferenceChangeListener extends EventListener {
    void closingLabelPreferenceChanged();
  }

  public DartClosingLabelManager() {
    addListener(new PreferenceChangeListener() {
                  @Override
                  public void closingLabelPreferenceChanged() {
                    if (!getShowClosingLabels()) {
                      clearAllInlays();
                    }
                  }
                },
                this);
  }

  public static DartClosingLabelManager getInstance() {
    return ApplicationManager.getApplication().getService(DartClosingLabelManager.class);
  }

  public void setShowClosingLabels(boolean value) {
    DartCodeInsightSettings settings = DartCodeInsightSettings.getInstance();
    if (settings.SHOW_CLOSING_LABELS != value) {
      settings.SHOW_CLOSING_LABELS = value;
      myEventDispatcher.getMulticaster().closingLabelPreferenceChanged();
    }
  }

  public boolean getShowClosingLabels() {
    return DartCodeInsightSettings.getInstance().SHOW_CLOSING_LABELS;
  }

  void addListener(@NotNull PreferenceChangeListener listener, @NotNull Disposable parentDisposable) {
    myEventDispatcher.addListener(listener, parentDisposable);
  }

  void computedClosingLabels(@NotNull Project project, @NotNull String filePath, @NotNull List<ClosingLabel> labels) {
    if (!getShowClosingLabels()) {
      return;
    }

    VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
    if (file == null) {
      return;
    }

    Runnable runnable = () -> {
      for (FileEditor fileEditor : FileEditorManager.getInstance(project).getAllEditors(file)) {
        if (!(fileEditor instanceof TextEditor)) {
          continue;
        }

        TextEditor textEditor = (TextEditor)fileEditor;
        Editor editor = textEditor.getEditor();
        InlayModel inlayModel = editor.getInlayModel();

        clearEditorInlays(editor);

        // Display combined labels as `// Foo, Bar`, not `// Foo // Bar`.

        // sort the new inlays by starting offset, reversed order
        labels.sort((label1, label2) -> label2.getOffset() - label1.getOffset());

        DartAnalysisServerService service = DartAnalysisServerService.getInstance(project);

        // create the inlay text for each line
        Map<Integer, String> lineText = new HashMap<>();
        for (ClosingLabel label : labels) {
          int offset = service.getConvertedOffset(file, label.getOffset() + label.getLength());
          if (offset >= editor.getDocument().getTextLength()) {
            // probably outdated data from server; up-to-date data will come soon
            lineText.clear();
            break;
          }

          Integer line = editor.getDocument().getLineNumber(offset);

          if (lineText.containsKey(line)) {
            lineText.put(line, lineText.get(line) + ", " + label.getLabel());
          }
          else {
            lineText.put(line, "// " + label.getLabel());
          }
        }

        // build inlays from the line labels
        for (Integer line : lineText.keySet()) {
          inlayModel.addAfterLineEndElement(
            editor.getDocument().getLineEndOffset(line), true, new TextLabelCustomElementRenderer(lineText.get(line)));
        }
      }
    };

    ApplicationManager.getApplication()
      .invokeLater(runnable, ModalityState.NON_MODAL, DartAnalysisServerService.getInstance(project).getDisposedCondition());
  }

  private static void clearEditorInlays(@NotNull Editor editor) {
    editor.getInlayModel().getAfterLineEndElementsInRange(0, editor.getDocument().getTextLength(), TextLabelCustomElementRenderer.class)
      .forEach(Disposer::dispose);
  }

  private static void clearAllInlays() {
    for (Project project : ProjectManager.getInstance().getOpenProjects()) {
      FileEditor[] editors = FileEditorManager.getInstance(project).getAllEditors();

      for (FileEditor fileEditor : editors) {
        if (fileEditor instanceof TextEditor) {
          Editor editor = ((TextEditor)fileEditor).getEditor();
          editor.getInlayModel().getInlineElementsInRange(0, editor.getDocument().getTextLength(), TextLabelCustomElementRenderer.class)
            .forEach(Disposer::dispose);
        }
      }
    }
  }

  @Override
  public void dispose() {
    clearAllInlays();
  }
}

class TextLabelCustomElementRenderer implements EditorCustomElementRenderer {
  private static final TextAttributesKey TEXT_ATTRIBUTES = DefaultLanguageHighlighterColors.LINE_COMMENT;

  private final String label;

  TextLabelCustomElementRenderer(@NotNull String label) {
    this.label = " " + label;
  }

  private static FontInfo getFontInfo(@NotNull Editor editor) {
    EditorColorsScheme colorsScheme = editor.getColorsScheme();
    FontPreferences fontPreferences = colorsScheme.getFontPreferences();
    TextAttributes attributes = editor.getColorsScheme().getAttributes(TEXT_ATTRIBUTES);
    int fontStyle = attributes == null ? Font.PLAIN : attributes.getFontType();
    return ComplementaryFontsRegistry.getFontAbleToDisplay(
      'a', fontStyle, fontPreferences,
      FontInfo.getFontRenderContext(editor.getContentComponent()));
  }

  @Override
  public int calcWidthInPixels(@NotNull Inlay inlay) {
    FontInfo fontInfo = getFontInfo(inlay.getEditor());
    return fontInfo.fontMetrics().stringWidth(label);
  }

  @Override
  public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle r, @NotNull TextAttributes textAttributes) {
    Editor editor = inlay.getEditor();
    TextAttributes attributes = editor.getColorsScheme().getAttributes(TEXT_ATTRIBUTES);
    if (attributes == null) return;
    Color fgColor = attributes.getForegroundColor();
    if (fgColor == null) return;
    g.setColor(fgColor);
    FontInfo fontInfo = getFontInfo(editor);
    int ascent = editor.getAscent();
    g.setFont(fontInfo.getFont());
    g.drawString(label, r.x, r.y + ascent);
  }
}
