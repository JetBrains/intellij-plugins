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
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.EnumComboBoxModel;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.plugins.markdown.MarkdownBundle;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanelProvider;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class MarkdownSettingsForm implements MarkdownCssSettings.Holder, MarkdownPreviewSettings.Holder, Disposable {
  private static final String JAVA_FX_HTML_PANEL_PROVIDER = "JavaFxHtmlPanelProvider";
  private static final String LOBO_HTML_PANEL_PROVIDER = "LoboHtmlPanelProvider";
  private JPanel myMainPanel;
  private JBCheckBox myCssFromURIEnabled;
  private TextFieldWithBrowseButton myCssURI;
  private JBCheckBox myApplyCustomCssText;
  private JPanel myEditorPanel;
  private JPanel myCssTitledSeparator;
  private ComboBox myPreviewProvider;
  private ComboBox myDefaultSplitLayout;
  private JBCheckBox myUseGrayscaleRenderingForJBCheckBox;
  private JPanel myPreviewTitledSeparator;
  private JBCheckBox myAutoScrollCheckBox;
  private JBCheckBox myPreviewWithJavaFX;
  private JPanel myMultipleProvidersPreviewPanel;

  @NotNull
  private String myCssText = "";
  @Nullable
  private EditorEx myEditor;
  @NotNull
  private final ActionListener myCssURIListener;
  @NotNull
  private final ActionListener myCustomCssTextListener;

  private Object myLastItem;
  private EnumComboBoxModel<SplitFileEditor.SplitEditorLayout> mySplitLayoutModel;
  private CollectionComboBoxModel<MarkdownHtmlPanelProvider.ProviderInfo> myPreviewPanelModel;

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

    boolean multipleProviders = isMultipleProviders();
    myPreviewWithJavaFX.setVisible(!multipleProviders);
    myMultipleProvidersPreviewPanel.setVisible(multipleProviders);

    updateUseGrayscaleEnabled();
  }

  void adjustCSSRulesAvailability() {
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

  private void createJavaFXPreviewSettings() {
    myPreviewWithJavaFX = new JBCheckBox(MarkdownBundle.message("markdown.settings.preview.with.javafx"));
    myPreviewWithJavaFX.addActionListener(e -> {
      final MarkdownHtmlPanelProvider provider = MarkdownHtmlPanelProvider.createFromInfo(getSelectedProvider());
      final MarkdownHtmlPanelProvider.AvailabilityInfo availability = provider.isAvailable();

      if (!availability.checkAvailability(myMainPanel)) {
        //todo Do we need somehow inform user about unavailability?
        myPreviewWithJavaFX.setSelected(false);
      }
      else {
        updateUseGrayscaleEnabled();
      }
    });
  }

  private static boolean isMultipleProviders() {
    return MarkdownHtmlPanelProvider.getProviders().length > 2;
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
    myCssText = settings.getStylesheetText();
    resetEditor();

    //noinspection ConstantConditions
    myCssURIListener.actionPerformed(null);
    myCustomCssTextListener.actionPerformed(null);
  }

  void resetEditor() {
    if (myEditor != null && !myEditor.isDisposed()) {
      ApplicationManager.getApplication().runWriteAction(() -> {
        boolean writable = myEditor.getDocument().isWritable();
        myEditor.getDocument().setReadOnly(false);
        myEditor.getDocument().setText(myCssText);
        myEditor.getDocument().setReadOnly(!writable);
      });
    }
  }

  @NotNull
  @Override
  public MarkdownCssSettings getMarkdownCssSettings() {
    if (myEditor != null && !myEditor.isDisposed()) {
      myCssText = ReadAction.compute(() -> myEditor.getDocument().getText());
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

  private void createPreviewUIComponents() {
    myPreviewTitledSeparator = new TitledSeparator(MarkdownBundle.message("markdown.settings.preview.name"));
    mySplitLayoutModel = new EnumComboBoxModel<>(SplitFileEditor.SplitEditorLayout.class);
    myDefaultSplitLayout = new ComboBox<>(mySplitLayoutModel);

    createMultipleProvidersSettings();
    createJavaFXPreviewSettings();
  }

  private void createMultipleProvidersSettings() {
    //noinspection unchecked
    final List<MarkdownHtmlPanelProvider.ProviderInfo> providerInfos =
      ContainerUtil.mapNotNull(MarkdownHtmlPanelProvider.getProviders(),
                               provider -> {
                                 if (provider.isAvailable() == MarkdownHtmlPanelProvider.AvailabilityInfo.UNAVAILABLE) {
                                   return null;
                                 }
                                 return provider.getProviderInfo();
                               });
    myPreviewPanelModel = new CollectionComboBoxModel<>(providerInfos, providerInfos.get(0));
    myPreviewProvider = new ComboBox<>(myPreviewPanelModel);

    myLastItem = myPreviewProvider.getSelectedItem();
    myPreviewProvider.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        final Object item = e.getItem();
        if (e.getStateChange() != ItemEvent.SELECTED || !(item instanceof MarkdownHtmlPanelProvider.ProviderInfo)) {
          return;
        }

        final MarkdownHtmlPanelProvider provider = MarkdownHtmlPanelProvider.createFromInfo((MarkdownHtmlPanelProvider.ProviderInfo)item);
        final MarkdownHtmlPanelProvider.AvailabilityInfo availability = provider.isAvailable();

        if (!availability.checkAvailability(myMainPanel)) {
          myPreviewProvider.setSelectedItem(myLastItem);
        }
        else {
          myLastItem = item;
          updateUseGrayscaleEnabled();
        }
      }
    });
  }

  private void updateUseGrayscaleEnabled() {
    final MarkdownHtmlPanelProvider.ProviderInfo selected = getSelectedProvider();
    myUseGrayscaleRenderingForJBCheckBox.setEnabled(isProviderOf(selected, JAVA_FX_HTML_PANEL_PROVIDER));
  }

  private static boolean isProviderOf(@NotNull MarkdownHtmlPanelProvider.ProviderInfo selected, @NotNull String provider) {
    return selected.getClassName().contains(provider);
  }

  @NotNull
  private static MarkdownHtmlPanelProvider getProvider(@NotNull String providerClass) {
    for (MarkdownHtmlPanelProvider provider : MarkdownHtmlPanelProvider.getProviders()) {
      if (isProviderOf(provider.getProviderInfo(), providerClass)) return provider;
    }

    throw new RuntimeException("Cannot find " + providerClass);
  }

  @NotNull
  private MarkdownHtmlPanelProvider.ProviderInfo getSelectedProvider() {
    if (isMultipleProviders()) {
      return Objects.requireNonNull(myPreviewPanelModel.getSelected());
    }
    else {
      return myPreviewWithJavaFX.isEnabled() && myPreviewWithJavaFX.isSelected()
             ? getProvider(JAVA_FX_HTML_PANEL_PROVIDER).getProviderInfo()
             : getProvider(LOBO_HTML_PANEL_PROVIDER).getProviderInfo();
    }
  }

  @Override
  public void setMarkdownPreviewSettings(@NotNull MarkdownPreviewSettings settings) {
    if (isMultipleProviders()) {
      if (myPreviewPanelModel.contains(settings.getHtmlPanelProviderInfo())) {
        myPreviewPanelModel.setSelectedItem(settings.getHtmlPanelProviderInfo());
      }
    }
    else {
      myPreviewWithJavaFX.setSelected(isProviderOf(settings.getHtmlPanelProviderInfo(), JAVA_FX_HTML_PANEL_PROVIDER));
    }

    mySplitLayoutModel.setSelectedItem(settings.getSplitEditorLayout());
    myUseGrayscaleRenderingForJBCheckBox.setSelected(settings.isUseGrayscaleRendering());
    myAutoScrollCheckBox.setSelected(settings.isAutoScrollPreview());

    updateUseGrayscaleEnabled();
  }

  @NotNull
  @Override
  public MarkdownPreviewSettings getMarkdownPreviewSettings() {
    MarkdownHtmlPanelProvider.ProviderInfo provider = getSelectedProvider();

    Objects.requireNonNull(provider);
    return new MarkdownPreviewSettings(mySplitLayoutModel.getSelectedItem(),
                                       provider,
                                       myUseGrayscaleRenderingForJBCheckBox.isSelected(),
                                       myAutoScrollCheckBox.isSelected());
  }
}
