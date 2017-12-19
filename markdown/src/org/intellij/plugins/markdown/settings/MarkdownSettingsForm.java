package org.intellij.plugins.markdown.settings;

import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EnumComboBoxModel;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import org.intellij.plugins.markdown.MarkdownBundle;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class MarkdownSettingsForm implements MarkdownCssSettings.Holder, MarkdownPreviewSettings.Holder, Disposable {
  private JPanel myMainPanel;
  private JBCheckBox myCssFromURIEnabled;
  private TextFieldWithBrowseButton myCssURI;
  private JBCheckBox myApplyCustomCssText;
  private JPanel myEditorPanel;
  private JPanel myCssTitledSeparator;
  private ComboBox myDefaultSplitLayout;
  private JBCheckBox myUseGrayscaleRenderingForJBCheckBox;
  private JPanel myPreviewTitledSeparator;
  private JBCheckBox myAutoScrollCheckBox;

  @Nullable
  private EditorEx myEditor;
  @NotNull
  private final ActionListener myCssURIListener;
  @NotNull
  private final ActionListener myCustomCssTextListener;

  private EnumComboBoxModel<SplitFileEditor.SplitEditorLayout> mySplitLayoutModel;

  public MarkdownSettingsForm() {
    myCssURIListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        myCssURI.setEnabled(myCssFromURIEnabled.isSelected());
      }
    };

    myCustomCssTextListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        adjustCSSRulesAvailability();
      }
    };

    adjustCSSRulesAvailability();

    myCssFromURIEnabled.addActionListener(myCssURIListener);
    myApplyCustomCssText.addActionListener(myCustomCssTextListener);
    myCssURI.addBrowseFolderListener(new TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFileDescriptor("css")) {
      @NotNull
      @Override
      protected String chosenFileToResultingText(@NotNull VirtualFile chosenFile) {
        return chosenFile.getUrl();
      }
    });

    myDefaultSplitLayout.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        adjustAutoScroll();
      }
    });

    adjustAutoScroll();
  }

  private void adjustAutoScroll() {
    myAutoScrollCheckBox.setEnabled(myDefaultSplitLayout.getSelectedItem() == SplitFileEditor.SplitEditorLayout.SPLIT);
  }

  private void adjustCSSRulesAvailability() {
    if (myEditor != null) {
      boolean enabled = myApplyCustomCssText.isSelected();
      myEditor.getDocument().setReadOnly(!enabled);
      myEditor.getContentComponent().setEnabled(enabled);
      myEditor.setCaretEnabled(enabled);
    }
  }

  public JComponent getComponent() {
    return myMainPanel;
  }

  private void createUIComponents() {
    myEditorPanel = new JPanel(new BorderLayout());

    myEditor = createEditor();
    myEditorPanel.add(myEditor.getComponent(), BorderLayout.CENTER);

    myCssTitledSeparator = new TitledSeparator(MarkdownBundle.message("markdown.settings.css.title.name"));

    createPreviewUIComponents();
  }

  public void validate() throws ConfigurationException {
    if (!myCssFromURIEnabled.isSelected()) return;

    try {
      new URL(myCssURI.getText()).toURI();
    }
    catch (URISyntaxException | MalformedURLException e) {
      throw new ConfigurationException("URI '" + myCssURI.getText() + "' parsing reports the error: " + e.getMessage());
    }
  }

  @NotNull
  private static EditorEx createEditor() {
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
    resetEditor(settings.getStylesheetText());

    //noinspection ConstantConditions
    myCssURIListener.actionPerformed(null);
    myCustomCssTextListener.actionPerformed(null);
  }

  void resetEditor(@NotNull String cssText) {
    if (myEditor != null && !myEditor.isDisposed()) {
      ApplicationManager.getApplication().runWriteAction(() -> {
        boolean writable = myEditor.getDocument().isWritable();
        myEditor.getDocument().setReadOnly(false);
        myEditor.getDocument().setText(cssText);
        myEditor.getDocument().setReadOnly(!writable);
      });
    }
  }

  @NotNull
  @Override
  public MarkdownCssSettings getMarkdownCssSettings() {
    return new MarkdownCssSettings(myCssFromURIEnabled.isSelected(),
                                   myCssURI.getText(),
                                   myApplyCustomCssText.isSelected(),
                                   myEditor != null && !myEditor.isDisposed() ?
                                   ReadAction.compute(() -> myEditor.getDocument().getText()) : "");
  }

  @Override
  public void dispose() {
    if (myEditor != null && !myEditor.isDisposed()) {
      EditorFactory.getInstance().releaseEditor(myEditor);
    }
    myEditor = null;
  }

  private void createPreviewUIComponents() {
    myPreviewTitledSeparator = new TitledSeparator(MarkdownBundle.message("markdown.settings.preview.name"));
    mySplitLayoutModel = new EnumComboBoxModel<>(SplitFileEditor.SplitEditorLayout.class);
    myDefaultSplitLayout = new ComboBox<>(mySplitLayoutModel);
    myDefaultSplitLayout.setRenderer(new ListCellRendererWrapper<SplitFileEditor.SplitEditorLayout>() {
      @Override
      public void customize(JList list, SplitFileEditor.SplitEditorLayout value, int index, boolean selected, boolean hasFocus) {
        setText(value.getPresentationText());
      }
    });
  }

  @Override
  public void setMarkdownPreviewSettings(@NotNull MarkdownPreviewSettings settings) {
    mySplitLayoutModel.setSelectedItem(settings.getSplitEditorLayout());
    myUseGrayscaleRenderingForJBCheckBox.setSelected(settings.isUseGrayscaleRendering());
    myAutoScrollCheckBox.setSelected(settings.isAutoScrollPreview());
  }

  @NotNull
  @Override
  public MarkdownPreviewSettings getMarkdownPreviewSettings() {
    return new MarkdownPreviewSettings(mySplitLayoutModel.getSelectedItem(),
                                       myUseGrayscaleRenderingForJBCheckBox.isSelected(),
                                       myAutoScrollCheckBox.isSelected());
  }
}
