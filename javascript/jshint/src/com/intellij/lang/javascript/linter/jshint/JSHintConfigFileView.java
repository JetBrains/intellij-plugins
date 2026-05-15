package com.intellij.lang.javascript.linter.jshint;

import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.linter.JSLinterUtil;
import com.intellij.lang.javascript.linter.jshint.config.JSHintConfigFileType;
import com.intellij.lang.javascript.linter.ui.JSLinterConfigFileView;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.SwingHelper;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentEvent;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JSHintConfigFileView {

  private final JPanel myComponent;
  private final JRadioButton myDefaultRadioButton;
  private final JRadioButton myCustomRadioButton;
  private final TextFieldWithHistoryWithBrowseButton myCustomConfigField;
  private final List<Pair<JRadioButton, JComponent>> myRadioButtonWithContentList;
  private final JLabel myCustomConfigFileErrorLabel;

  public JSHintConfigFileView(@NotNull Project project) {
    myDefaultRadioButton = new JBRadioButton(JSHintBundle.message("jshint.config.option.default.name"));
    final JEditorPane defaultContentEditor = JSLinterUtil.createDefaultContent(JSHintBundle.message("jshint.config.default.description"));
    JPanel defaultContent = SwingHelper.wrapWithHorizontalStretch(defaultContentEditor);
    myCustomRadioButton = new JBRadioButton(JSHintBundle.message("jshint.config.option.custom.name"));
    myCustomConfigField = createCustomConfigField(project);
    myCustomConfigFileErrorLabel = new JLabel();
    myCustomConfigFileErrorLabel.setFont(UIUtil.getTitledBorderFont());
    JPanel customContent = createCustomContent(myCustomConfigField, myCustomConfigFileErrorLabel);

    ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(myDefaultRadioButton);
    buttonGroup.add(myCustomRadioButton);

    myRadioButtonWithContentList = List.of(
      Pair.create(myDefaultRadioButton, defaultContent),
      Pair.create(myCustomRadioButton, customContent)
    );
    selectOption(myDefaultRadioButton, true);
    myCustomConfigField.getChildComponent().getTextEditor().getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull DocumentEvent e) {
        updateCustomErrorMessage();
      }
    });
    myComponent = createResultPanel(myRadioButtonWithContentList);
  }

  private void selectOption(@NotNull JRadioButton radioButton, boolean selected) {
    for (Pair<JRadioButton, JComponent> pair : myRadioButtonWithContentList) {
      boolean pairSelected = pair.getFirst() == radioButton ? selected : !selected;
      pair.getFirst().setSelected(pairSelected);
      UIUtil.setEnabled(pair.getSecond(), pairSelected, true);
    }
    updateCustomErrorMessage();
  }

  private void updateCustomErrorMessage() {
    boolean visible = myCustomRadioButton.isSelected() && myCustomRadioButton.isEnabled();
    myCustomConfigFileErrorLabel.setVisible(visible);
    if (visible) {
      String errorMessage = JSLinterConfigFileView.formatErrorMessage(myCustomConfigField.getChildComponent().getText());
      if (errorMessage != null) {
        String html = JSLinterUtil.getRedErrorTextHtml(errorMessage);
        myCustomConfigFileErrorLabel.setText(html);
      }
      else {
        myCustomConfigFileErrorLabel.setText("");
      }
    }
  }

  private @NotNull JPanel createResultPanel(final @NotNull List<? extends Pair<JRadioButton, JComponent>> contentByRadioButton) {
    for (Pair<JRadioButton, JComponent> pair : contentByRadioButton) {
      final JRadioButton radioButton = pair.getFirst();
      radioButton.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          selectOption(radioButton, radioButton.isSelected());
        }
      });
    }
    List<Component> components = new ArrayList<>();
    for (Pair<JRadioButton, JComponent> pair : contentByRadioButton) {
      if (!components.isEmpty()) {
        components.add(Box.createVerticalStrut(10));
      }
      JRadioButton radioButton = pair.getFirst();
      components.add(radioButton);

      int leftMargin = JSLinterUtil.getMarginForRadioButton(radioButton);
      JPanel wrap = SwingHelper.wrapWithHorizontalStretch(pair.getSecond());
      wrap.setBorder(BorderFactory.createEmptyBorder(0, leftMargin, 0, 0));

      components.add(wrap);
    }
    JPanel panel = SwingHelper.newLeftAlignedVerticalPanel(components);
    panel.setBorder(IdeBorderFactory.createTitledBorder(
      JavaScriptBundle.message("border.title.inspection.export.results.capitalized.location"), false));
    return SwingHelper.wrapWithHorizontalStretch(panel);
  }

  private static @NotNull JPanel createCustomContent(@NotNull TextFieldWithHistoryWithBrowseButton configField,
                                                     @NotNull JLabel configErrorLabel) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    panel.add(configErrorLabel);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
    return FormBuilder
      .createFormBuilder()
      .setHorizontalGap(UIUtil.DEFAULT_HGAP)
      .setVerticalGap(UIUtil.DEFAULT_VGAP)
      .addLabeledComponent(JavaScriptBundle.message("label.path"), configField)
      .addComponentToRightColumn(panel)
      .getPanel();
  }

  private static @NotNull TextFieldWithHistoryWithBrowseButton createCustomConfigField(final @NotNull Project project) {
    var descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
      .withTitle(JSHintBundle.message("jshint.config.option.custom.browser.title"));
    return SwingHelper.createTextFieldWithHistoryWithBrowseButton(project, descriptor, () -> {
      List<VirtualFile> files = listConfigFilesInContentScope(project);
      return ContainerUtil.sorted(ContainerUtil.map(files, file -> FileUtil.toSystemDependentName(file.getPath())));
    });
  }

  private static @NotNull List<VirtualFile> listConfigFilesInContentScope(@NotNull Project project) {
    GlobalSearchScope scope = ProjectScope.getContentScope(project);
    Collection<VirtualFile> configs = FileTypeIndex.getFiles(JSHintConfigFileType.INSTANCE, scope);
    List<VirtualFile> result = new ArrayList<>();
    for (VirtualFile config : configs) {
      if (config != null && config.isValid() && !config.isDirectory()) {
        String path = config.getPath();
        if (!path.contains("/node_modules/") && !path.contains("/bower_components/")) {
          result.add(config);
        }
      }
    }
    return result;
  }

  public Component getComponent() {
    return myComponent;
  }

  public void onEnabledStateChange(boolean enabled) {
    if (enabled) {
      selectOption(myDefaultRadioButton, myDefaultRadioButton.isSelected());
    }
    updateCustomErrorMessage();
  }

  public boolean isCustomConfigFileUsed() {
    return myCustomRadioButton.isSelected();
  }

  public @NotNull String getCustomConfigFilePath() {
    return StringUtil.notNullize(myCustomConfigField.getChildComponent().getText());
  }

  public void setCustomConfigFileUsed(boolean used) {
    myCustomRadioButton.setSelected(used);
  }

  public void setCustomConfigFilePath(@NotNull String customConfigFilePath) {
    TextFieldWithHistory history = myCustomConfigField.getChildComponent();
    history.setText(customConfigFilePath);
    history.addCurrentTextToHistory();
  }
}
