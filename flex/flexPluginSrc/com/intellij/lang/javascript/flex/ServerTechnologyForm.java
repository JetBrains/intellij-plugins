package com.intellij.lang.javascript.flex;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;

public class ServerTechnologyForm {

  private JPanel myMainPanel;
  private LabeledComponent<TextFieldWithBrowseButton> myPathToServicesConfigXmlComponent;
  private LabeledComponent<JTextField> myContextRootComponent;

  public ServerTechnologyForm() {
    initPathToServicesConfigXmlBrowseFolderListener();
  }

  private void initPathToServicesConfigXmlBrowseFolderListener() {
    final FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
      @Override
      public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
        return super.isFileVisible(file, showHiddenFiles) && (file.isDirectory() || "xml".equalsIgnoreCase(file.getExtension()));
      }
    };
    myPathToServicesConfigXmlComponent.getComponent().addBrowseFolderListener(FlexBundle.message("choose.services-config.xml.file.title"),
                                                                              FlexBundle.message(
                                                                                "choose.services-config.xml.file.description"), null,
                                                                              fileChooserDescriptor);
  }

  // may be absolute or relative to project root
  public String getPathToServicesConfigXml() {
    return FileUtil.toSystemIndependentName(myPathToServicesConfigXmlComponent.getComponent().getText().trim());
  }

  public void setPathToServicesConfigXml(final String pathToServicesConfigXml) {
    myPathToServicesConfigXmlComponent.getComponent().setText(FileUtil.toSystemDependentName(pathToServicesConfigXml));
  }

  public String getContextRoot() {
    return myContextRootComponent.getComponent().getText().trim();
  }

  public void setContextRoot(final String contextRoot) {
    myContextRootComponent.getComponent().setText(contextRoot);
  }
}
