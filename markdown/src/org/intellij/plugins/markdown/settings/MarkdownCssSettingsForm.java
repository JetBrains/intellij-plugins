package org.intellij.plugins.markdown.settings;

import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.util.Computable;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MarkdownCssSettingsForm implements MarkdownCssSettings.Holder,
                                                Disposable {
  private JPanel myMainPanel;
  private JBCheckBox myCssFromURIEnabled;
  private JTextField myCssURI;
  private JBCheckBox myApplyCustomCssText;
  private JPanel myEditorPanel;

  @NotNull
  private String myCssText = "";
  @Nullable
  private Editor myEditor;
  @NotNull
  private final ActionListener myUpdateListener;


  public MarkdownCssSettingsForm() {
    myUpdateListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        myCssURI.setEnabled(myCssFromURIEnabled.isSelected());
        if (myEditor != null && !myEditor.isDisposed()) {
          final boolean canEditCss = myApplyCustomCssText.isSelected();

          myEditor.getDocument().setReadOnly(!canEditCss);
          myEditor.getSettings().setCaretRowShown(canEditCss);

          Color baseColor = myEditor.getColorsScheme().getDefaultBackground();
          if (canEditCss) {
            ((EditorEx)myEditor).setBackgroundColor(baseColor);
          }
          else {
            ((EditorEx)myEditor).setBackgroundColor(ColorUtil.isDark(baseColor) ?
                                                    ColorUtil.brighter(baseColor, 1) : ColorUtil.darker(baseColor, 1));
          }
        }
      }
    };
    myCssFromURIEnabled.addActionListener(myUpdateListener);
    myApplyCustomCssText.addActionListener(myUpdateListener);
  }

  public JComponent getComponent() {
    return myMainPanel;
  }

  private void createUIComponents() {
    myEditorPanel = new JPanel(new BorderLayout());

    myEditor = createEditor();
    myEditorPanel.add(myEditor.getComponent(), BorderLayout.CENTER);
  }

  @NotNull
  private static Editor createEditor() {
    EditorFactory editorFactory = EditorFactory.getInstance();
    Document editorDocument = editorFactory.createDocument("");
    EditorEx editor = (EditorEx)editorFactory.createEditor(editorDocument);
    fillEditorSettings(editor.getSettings());
    setHighlighting(editor);
    return editor;
  }

  private static void setHighlighting(EditorEx editor) {
    final FileType cssFileType = FileTypeManager.getInstance().getFileTypeByExtension("css");
    if (cssFileType == UnknownFileType.INSTANCE) {
      return;
    }
    final EditorHighlighter editorHighlighter =
      HighlighterFactory.createHighlighter(cssFileType, EditorColorsManager.getInstance().getGlobalScheme(), null);
    editor.setHighlighter(editorHighlighter);
  }

  private static void fillEditorSettings(final EditorSettings editorSettings) {
    editorSettings.setWhitespacesShown(false);
    editorSettings.setLineMarkerAreaShown(false);
    editorSettings.setIndentGuidesShown(false);
    editorSettings.setLineNumbersShown(true);
    editorSettings.setFoldingOutlineShown(false);
    editorSettings.setAdditionalColumnsCount(1);
    editorSettings.setAdditionalLinesCount(1);
    editorSettings.setUseSoftWraps(false);
  }

  @Override
  public void setMarkdownCssSettings(@NotNull MarkdownCssSettings settings) {
    myCssFromURIEnabled.setSelected(settings.isUriEnabled());
    myCssURI.setText(settings.getStylesheetUri());
    myApplyCustomCssText.setSelected(settings.isTextEnabled());
    myCssText = settings.getStylesheetText();
    if (myEditor != null && !myEditor.isDisposed()) {
      ApplicationManager.getApplication().runWriteAction(() -> myEditor.getDocument().setText(myCssText));
    }

    //noinspection ConstantConditions
    myUpdateListener.actionPerformed(null);
  }

  @NotNull
  @Override
  public MarkdownCssSettings getMarkdownCssSettings() {
    if (myEditor != null && !myEditor.isDisposed()) {
      myCssText = ApplicationManager.getApplication().runReadAction(new Computable<String>() {
        @Override
        public String compute() {
          return myEditor.getDocument().getText();
        }
      });
    }
    return new MarkdownCssSettings(myCssFromURIEnabled.isSelected(),
                                   myCssURI.getText(),
                                   myApplyCustomCssText.isSelected(),
                                   myCssText);
  }

  @Override
  public void dispose() {
    if (myEditor != null && !myEditor.isDisposed()) {
      EditorFactory.getInstance().releaseEditor(myEditor);
    }
    myEditor = null;
  }
}
