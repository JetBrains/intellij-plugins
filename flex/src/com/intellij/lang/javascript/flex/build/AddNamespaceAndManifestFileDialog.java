package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;

import static com.intellij.lang.javascript.flex.build.FlexBuildConfiguration.NamespaceAndManifestFileInfo;

public class AddNamespaceAndManifestFileDialog extends AddRemoveTableRowsDialog.AddObjectDialog<NamespaceAndManifestFileInfo> {
  private JPanel myMainPanel;
  private JTextField myNamespaceTextField;
  private TextFieldWithBrowseButton myManifestFilePathTextWithBrowse;
  private JCheckBox myIncludeInSWCCheckBox;

  private NamespaceAndManifestFileInfo myNamespaceAndManifestFileInfo = new NamespaceAndManifestFileInfo();

  public AddNamespaceAndManifestFileDialog(final Project project, final boolean isSwcLibrary) {
    super(project);
    setTitle(FlexBundle.message("add.namespace.and.path.to.manifest.file.title"));

    if (!isSwcLibrary) {
      myIncludeInSWCCheckBox.setVisible(false);
    }
    myNamespaceTextField.setText(myNamespaceAndManifestFileInfo.NAMESPACE);
    myManifestFilePathTextWithBrowse.setText(myNamespaceAndManifestFileInfo.MANIFEST_FILE_PATH);
    myIncludeInSWCCheckBox.setSelected(myNamespaceAndManifestFileInfo.INCLUDE_IN_SWC);

    myManifestFilePathTextWithBrowse.addBrowseFolderListener(FlexBundle.message("path.to.manifest.file"), null, project,
                                                             new FileChooserDescriptor(true, false, false, false, false, false) {
                                                               public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                                                                 return super.isFileVisible(file, showHiddenFiles) &&
                                                                        (file.isDirectory() ||
                                                                         FlexUtils.isXmlExtension(file.getExtension()));
                                                               }
                                                             });

    init();
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  public JComponent getPreferredFocusedComponent() {
    return myNamespaceTextField;
  }

  public NamespaceAndManifestFileInfo getAddedObject() {
    myNamespaceAndManifestFileInfo.NAMESPACE = myNamespaceTextField.getText().trim();
    myNamespaceAndManifestFileInfo.MANIFEST_FILE_PATH = FileUtil.toSystemIndependentName(myManifestFilePathTextWithBrowse.getText().trim());
    myNamespaceAndManifestFileInfo.INCLUDE_IN_SWC = myIncludeInSWCCheckBox.isSelected();
    return myNamespaceAndManifestFileInfo;
  }

}
