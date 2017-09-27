package com.jetbrains.lang.dart.analyzer;

import com.intellij.openapi.components.ServiceManager;
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
import com.intellij.util.ui.UIUtil;
import com.jetbrains.lang.dart.ide.codeInsight.DartCodeInsightSettings;
import org.dartlang.analysis.server.protocol.ClosingLabel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClosingLabelManager {
  public static ClosingLabelManager getInstance() {
    return ServiceManager.getService(ClosingLabelManager.class);
  }

  private List<PreferenceChangeListener> listeners = new ArrayList<>();

  interface PreferenceChangeListener {
    void closingLabelPreferenceChanged();
  }

  public ClosingLabelManager() {
    addListener(new PreferenceChangeListener() {
      @Override
      public void closingLabelPreferenceChanged() {
        if (!getShowClosingLabels()) {
          UIUtil.invokeLaterIfNeeded(() -> clearAllInlays());
        }
      }
    });
  }

  public void setShowClosingLabels(boolean value) {
    DartCodeInsightSettings.getInstance().SHOW_CLOSING_LABELS = value;

    notifyPreferenceChanged();
  }

  public boolean getShowClosingLabels() {
    return DartCodeInsightSettings.getInstance().SHOW_CLOSING_LABELS;
  }

  public void addListener(PreferenceChangeListener listener) {
    listeners.add(listener);
  }

  public void removeListener(PreferenceChangeListener listener) {
    listeners.remove(listener);
  }

  void computedClosingLabels(@NotNull Project project, @NotNull final String filePath, @NotNull final List<ClosingLabel> labels) {
    if (!getShowClosingLabels()) {
      return;
    }

    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
    if (file == null) {
      return;
    }

    UIUtil.invokeLaterIfNeeded(() -> {
      for (final FileEditor fileEditor : FileEditorManager.getInstance(project).getEditors(file)) {
        if (!(fileEditor instanceof TextEditor)) {
          continue;
        }

        final TextEditor textEditor = (TextEditor)fileEditor;
        final Editor editor = textEditor.getEditor();
        final InlayModel inlayModel = editor.getInlayModel();

        clearEditorInlays(editor);

        // Display combined labels as `// Foo, Bar`, not `// Foo // Bar`.

        // sort the new inlays by starting offset, reversed order
        labels.sort((label1, label2) -> label2.getOffset() - label1.getOffset());

        // create the inlay text for each line
        Map<Integer, String> lineText = new HashMap<>();
        for (ClosingLabel label : labels) {
          final int offset = label.getOffset() + label.getLength();
          final Integer line = editor.getDocument().getLineNumber(offset);

          if (lineText.containsKey(line)) {
            lineText.put(line, lineText.get(line) + ", " + label.getLabel());
          }
          else {
            lineText.put(line, "// " + label.getLabel());
          }
        }

        // build inlays from the line labels
        for (Integer line : lineText.keySet()) {
          inlayModel.addInlineElement(
            editor.getDocument().getLineEndOffset(line), true, new TextLabelCustomElementRenderer(lineText.get(line)));
        }
      }
    });
  }

  private static void clearEditorInlays(@NotNull Editor editor) {
    List<Inlay> existingInlays = editor.getInlayModel().getInlineElementsInRange(0, editor.getDocument().getTextLength());
    for (Inlay inlay : existingInlays) {
      if (inlay.getRenderer() instanceof TextLabelCustomElementRenderer) {
        Disposer.dispose(inlay);
      }
    }
  }

  private static void clearAllInlays() {
    for (Project project : ProjectManager.getInstance().getOpenProjects()) {
      final FileEditor[] editors = FileEditorManager.getInstance(project).getAllEditors();

      for (FileEditor fileEditor : editors) {
        if (fileEditor instanceof TextEditor) {
          Editor editor = ((TextEditor)fileEditor).getEditor();
          List<Inlay> existingInlays = editor.getInlayModel().getInlineElementsInRange(0, editor.getDocument().getTextLength());
          for (Inlay inlay : existingInlays) {
            if (inlay.getRenderer() instanceof TextLabelCustomElementRenderer) {
              Disposer.dispose(inlay);
            }
          }
        }
      }
    }
  }

  private void notifyPreferenceChanged() {
    for (PreferenceChangeListener listener : listeners) {
      listener.closingLabelPreferenceChanged();
    }
  }
}

class TextLabelCustomElementRenderer implements EditorCustomElementRenderer {
  private static TextAttributesKey TEXT_ATTRIBUTES = DefaultLanguageHighlighterColors.LINE_COMMENT;

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
  public int calcWidthInPixels(@NotNull Editor editor) {
    FontInfo fontInfo = getFontInfo(editor);
    return fontInfo.fontMetrics().stringWidth(label);
  }

  @Override
  public void paint(@NotNull Editor editor, @NotNull Graphics g, @NotNull Rectangle r, @NotNull TextAttributes textAttributes) {
    TextAttributes attributes = editor.getColorsScheme().getAttributes(TEXT_ATTRIBUTES);
    if (attributes == null) return;
    Color fgColor = attributes.getForegroundColor();
    if (fgColor == null) return;
    g.setColor(fgColor);
    FontInfo fontInfo = getFontInfo(editor);
    g.setFont(fontInfo.getFont());
    FontMetrics metrics = fontInfo.fontMetrics();
    g.drawString(label, r.x, r.y + metrics.getAscent());
  }
}
