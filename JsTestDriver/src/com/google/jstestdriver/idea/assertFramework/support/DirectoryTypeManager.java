package com.google.jstestdriver.idea.assertFramework.support;

import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

/*
 * All operations should be executed on EDT.
 */
class DirectoryTypeManager {

  private final Project myProject;
  private final JPanel myDirectoryTypeContent;
  private final String myAssertionFrameworkName;
  private final File myDefaultDir;
  private final List<ChangeListener> myChangeListeners = Lists.newArrayList();
  private TextFieldWithBrowseButton myCustomDirectoryTextFieldWithBrowseButton;
  private DirectoryType mySelectedDirectoryType;

  private DirectoryTypeManager(Project project, JPanel directoryTypeContent, String assertionFrameworkName) {
    myProject = project;
    myDirectoryTypeContent = directoryTypeContent;
    myAssertionFrameworkName = assertionFrameworkName;
    myDefaultDir = getDefaultDir(assertionFrameworkName);
  }

  private void populate(JRadioButton defaultRadioButton, JRadioButton customRadioButton) {
    JPanel defaultDirectoryTypePanel = createDefaultDirectoryPanel();
    addCardToDirectoryTypeContent(defaultDirectoryTypePanel, DirectoryType.DEFAULT);

    JPanel customDirectoryPanel = createCustomDirectoryPanel();
    addCardToDirectoryTypeContent(customDirectoryPanel, DirectoryType.CUSTOM);

    defaultRadioButton.addActionListener(new MyActionListener(DirectoryType.DEFAULT));
    customRadioButton.addActionListener(new MyActionListener(DirectoryType.CUSTOM));

    selectDirectoryType(DirectoryType.DEFAULT);
  }

  private void addCardToDirectoryTypeContent(@NotNull JComponent child, @NotNull DirectoryType directoryType) {
    myDirectoryTypeContent.add(child, directoryType.name());
  }

  private JPanel createDefaultDirectoryPanel() {
    JPanel defaultDirectoryTypePanel = new JPanel();
    defaultDirectoryTypePanel.add(new JLabel(myDefaultDir.getPath()));
    return defaultDirectoryTypePanel;
  }

  private JPanel createCustomDirectoryPanel() {
    FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true, false, false, false, false);
    String adapterName = myAssertionFrameworkName + " adapter";
    String title = "Select a folder for " + adapterName;
    String description = adapterName + " source files will be extracted into selected folder";
    myCustomDirectoryTextFieldWithBrowseButton = new TextFieldWithBrowseButton();
    myCustomDirectoryTextFieldWithBrowseButton.addBrowseFolderListener(title, description, myProject, fileChooserDescriptor);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(myCustomDirectoryTextFieldWithBrowseButton, BorderLayout.NORTH);
    return panel;
  }

  public static DirectoryTypeManager install(
      @NotNull Project project,
      @NotNull JPanel directoryTypeContent,
      @NotNull String assertionFrameworkName,
      @NotNull JRadioButton defaultRadioButton,
      @NotNull JRadioButton customRadioButton
  ) {
    DirectoryTypeManager directoryTypeManager = new DirectoryTypeManager(project, directoryTypeContent, assertionFrameworkName);
    directoryTypeManager.populate(defaultRadioButton, customRadioButton);
    return directoryTypeManager;
  }

  public void addChangeListener(ChangeListener changeListener) {
    myChangeListeners.add(changeListener);
  }

  private File getExtractDir() {
    if (mySelectedDirectoryType == DirectoryType.DEFAULT) {
      return myDefaultDir;
    } else {
      return new File(myCustomDirectoryTextFieldWithBrowseButton.getText());
    }
  }

  public DialogWrapper.ValidationInfo validate() {
    if (mySelectedDirectoryType == DirectoryType.DEFAULT) {
      return null;
    }
    File extractDirectory = getExtractDir();
    if (!extractDirectory.isDirectory()) {
      return new DialogWrapper.ValidationInfo("'" + extractDirectory.getPath() + "' is not a directory.", myCustomDirectoryTextFieldWithBrowseButton);
    }
    return null;
  }

  private void selectDirectoryType(DirectoryType directoryType) {
    CardLayout cardLayout = (CardLayout) myDirectoryTypeContent.getLayout();
    cardLayout.show(myDirectoryTypeContent, directoryType.name());
    mySelectedDirectoryType = directoryType;
    fireDirectoryTypeSelected(directoryType);
  }

  private void fireDirectoryTypeSelected(DirectoryType newDirectoryType) {
    File directory = newDirectoryType == DirectoryType.DEFAULT ? myDefaultDir : new File(myCustomDirectoryTextFieldWithBrowseButton.getText());
    for (ChangeListener changeListener : myChangeListeners) {
      changeListener.onExtractDirectoryChanged(directory);
    }
  }

  /**
   *
   * @param bundledAdapterFiles files for coping
   * @return extracted file list or null if extraction was failed
   */
  public List<VirtualFile> extractAdapterFiles(final List<VirtualFile> bundledAdapterFiles) {
    return ApplicationManager.getApplication().runWriteAction(new Computable<List<VirtualFile>>() {

      @Override
      public List<VirtualFile> compute() {
        VirtualFile extractDir = getExtractDirAsVirtualFile();
        List<VirtualFile> copiedFiles = copyVirtualFilesToDir(bundledAdapterFiles, extractDir);
        return copiedFiles;
      }
    });
  }

  @NotNull
  private VirtualFile getExtractDirAsVirtualFile() {
    if (mySelectedDirectoryType == DirectoryType.DEFAULT) {
      if (!myDefaultDir.isDirectory() && !myDefaultDir.mkdirs()) {
        throw new RuntimeException("Can't create VirtualFile " + myDefaultDir.getAbsolutePath());
      }
    }
    File extractDir = getExtractDir();
    VirtualFile vfExtractDir = LocalFileSystem.getInstance().findFileByIoFile(extractDir);
    if (vfExtractDir == null) {
      throw new RuntimeException("Can't find VirtualFile for " + extractDir.getAbsolutePath());
    }
    return vfExtractDir;
  }

  @Nullable
  private List<VirtualFile> copyVirtualFilesToDir(List<VirtualFile> virtualFiles, @NotNull VirtualFile targetDir) {
    List<VirtualFile> copiedFiles = Lists.newArrayList();
    for (VirtualFile virtualFile : virtualFiles) {
      try {
        copiedFiles.add(VfsUtil.copyFile(null, virtualFile, targetDir));
      } catch (IOException e) {
        Messages.showErrorDialog("Extract operation failed!\nUnable to copy " + virtualFile.getPath() + " to " + targetDir.getPath(),
            "Adding " + myAssertionFrameworkName + " adapter support for JsTestDriver");
        return null;
      }
    }
    return copiedFiles;
  }

  public void init() {
    selectDirectoryType(DirectoryType.DEFAULT);
  }

  private class MyActionListener implements ActionListener {

    private final DirectoryType myDirectoryType;

    private MyActionListener(DirectoryType directoryType) {
      myDirectoryType = directoryType;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      selectDirectoryType(myDirectoryType);
    }
  }

  interface ChangeListener {
    void onExtractDirectoryChanged(File extractDirectory);
  }

  private static File getDefaultDir(String assertionFrameworkName) {
    return new File(PathManager.getSystemPath(), "extLibs/" + assertionFrameworkName + "AdapterForJsTestDriver");
  }

  private enum DirectoryType {
    DEFAULT, CUSTOM
  }

}
