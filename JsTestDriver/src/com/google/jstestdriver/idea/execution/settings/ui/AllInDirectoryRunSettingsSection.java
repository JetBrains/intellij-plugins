package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.JstdSettingsUtil;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.util.ProjectRootUtils;
import com.google.jstestdriver.idea.util.SwingUtils;
import com.google.jstestdriver.idea.util.TextChangeListener;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.util.ArrayUtil;
import com.intellij.util.NullableFunction;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.List;

class AllInDirectoryRunSettingsSection extends AbstractRunSettingsSection {

  private TextFieldWithBrowseButton myDirectoryTextFieldWithBrowseButton;
  private final JBLabel myLabel;

  AllInDirectoryRunSettingsSection() {
    myLabel = new JBLabel("Directory: ");
    setAnchor(myLabel);
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    myDirectoryTextFieldWithBrowseButton.setText(runSettings.getDirectory());
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    runSettingsBuilder.setDirectory(ObjectUtils.notNull(myDirectoryTextFieldWithBrowseButton.getText(), ""));
  }

  @NotNull
  @Override
  public JComponent createComponent(@NotNull CreationContext creationContext) {
    JPanel panel = new JPanel(new GridBagLayout());

    myLabel.setDisplayedMnemonic('D');
    myLabel.setLabelFor(myDirectoryTextFieldWithBrowseButton);
    panel.add(myLabel, new GridBagConstraints(
      0, 0,
      1, 1,
      0.0, 0.0,
      GridBagConstraints.WEST,
      GridBagConstraints.NONE,
      new Insets(UIUtil.DEFAULT_VGAP, 0, 0, UIUtil.DEFAULT_HGAP),
      0, 0
    ));

    myDirectoryTextFieldWithBrowseButton = new TextFieldWithBrowseButton();
    myDirectoryTextFieldWithBrowseButton.addBrowseFolderListener(
      "Select directory",
      "All JsTestDriver configuration files in this folder will be executed",
      creationContext.getProject(),
      FileChooserDescriptorFactory.createSingleFolderDescriptor()
    );
    panel.add(myDirectoryTextFieldWithBrowseButton, new GridBagConstraints(
      1, 0,
      1, 1,
      1.0, 0.0,
      GridBagConstraints.WEST,
      GridBagConstraints.HORIZONTAL,
      new Insets(UIUtil.DEFAULT_VGAP, 0, UIUtil.DEFAULT_VGAP, 0),
      0, 0
    ));

    JComponent infoComponent = createInfoComponent(creationContext.getProject(), myDirectoryTextFieldWithBrowseButton.getTextField());
    panel.add(infoComponent, new GridBagConstraints(
      0, 1,
      2, 1,
      1.0, 1.0,
      GridBagConstraints.WEST,
      GridBagConstraints.BOTH,
      new Insets(5, 0, 0, 0),
      0, 0
    ));

    return panel;
  }

  private static JComponent createInfoComponent(@NotNull final Project project,
                                                @NotNull JTextField directoryTextField) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new JLabel("Matched configuration files (*.jstd and jsTestDriver.conf):"), BorderLayout.NORTH);

    final JBList fileList = new JBList(ArrayUtil.EMPTY_STRING_ARRAY);
    fileList.setBorder(BorderFactory.createLineBorder(Color.gray));
    fileList.setCellRenderer(new ListCellRendererWrapper<String>(fileList.getCellRenderer()) {
      @Override
      public void customize(JList list, String value, int index, boolean selected, boolean hasFocus) {
        setText(value);
      }
    });
    SwingUtils.addTextChangeListener(directoryTextField, new TextChangeListener() {
      @Override
      public void textChanged(String oldText, @NotNull String newText) {
        List<String> configs = getConfigsInDir(project, newText);
        fileList.setListData(configs.toArray());
      }
    });

    panel.add(fileList, BorderLayout.CENTER);
    return panel;
  }

  @NotNull
  private static List<String> getConfigsInDir(@NotNull final Project project, @NotNull String dirPath) {
    List<String> result = Collections.emptyList();
    File dir = new File(dirPath);
    if (!StringUtil.isEmpty(dirPath) && dir.isDirectory() && dir.isAbsolute()) {
      VirtualFile directoryVFile = LocalFileSystem.getInstance().findFileByIoFile(dir);
      if (directoryVFile != null) {
        List<VirtualFile> configs = JstdSettingsUtil.collectJstdConfigFilesInDirectory(project, directoryVFile);
        result = ContainerUtil.filter(ContainerUtil.map(configs, new NullableFunction<VirtualFile, String>() {
          @Override
          public String fun(VirtualFile virtualFile) {
            return ProjectRootUtils.getRootRelativePath(project, virtualFile.getPath());
          }
        }), Condition.NOT_NULL);
        Collections.sort(result);
      }
    }
    return result;
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    super.setAnchor(anchor);
    myLabel.setAnchor(anchor);
  }
}
