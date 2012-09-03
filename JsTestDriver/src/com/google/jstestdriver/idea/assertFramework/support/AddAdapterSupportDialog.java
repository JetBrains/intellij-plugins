package com.google.jstestdriver.idea.assertFramework.support;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.util.ProjectRootUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.Gray;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.components.JBList;
import com.intellij.util.ArrayUtil;
import com.intellij.webcore.ScriptingFrameworkDescriptor;
import com.intellij.webcore.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class AddAdapterSupportDialog extends DialogWrapper {

  private static final Logger LOG = Logger.getInstance(AddAdapterSupportDialog.class);

  private final Project myProject;
  private final String myAssertFrameworkName;
  private final List<VirtualFile> myAdapterSourceFiles;
  private final VirtualFile myFileRequestor;

  private final JPanel myContent;
  private final JTextField myDirectoryTextField;

  public AddAdapterSupportDialog(@NotNull Project project,
                                 @NotNull PsiFile psiFileRequestor,
                                 @NotNull String assertionFrameworkName,
                                 @NotNull List<VirtualFile> adapterSourceFiles,
                                 @Nullable String adapterHomePageUrl) {
    super(project);
    myProject = project;
    myAssertFrameworkName = assertionFrameworkName;
    myAdapterSourceFiles = adapterSourceFiles;
    myFileRequestor = psiFileRequestor.getVirtualFile();

    setModal(true);
    setTitle("Add " + getAssertFrameworkAdapterName());

    myDirectoryTextField = new JTextField();
    VirtualFile initialDir = findInitialDir(psiFileRequestor);
    if (initialDir != null) {
      myDirectoryTextField.setText(FileUtil.toSystemDependentName(initialDir.getPath()));
    }
    // widen preferred size to fit dialog's title
    myDirectoryTextField.setPreferredSize(new Dimension(350, myDirectoryTextField.getPreferredSize().height));

    List<Component> components = Lists.newArrayList();
    components.add(createFilesViewPanel(adapterSourceFiles));
    components.add(Box.createVerticalStrut(10));
    components.add(createSelectDirectoryPanel(project, myDirectoryTextField));
    if (adapterHomePageUrl != null) {
      components.add(Box.createVerticalStrut(10));
      components.add(createInformationPanel(adapterHomePageUrl));
    }
    myContent = SwingHelper.newLeftAlignedVerticalPanel(components);

    setOKButtonText("Add");
    super.init();
  }

  @NotNull
  private static JComponent createInformationPanel(@NotNull final String adapterHomePageUrl) {
    JLabel label1 = new JLabel("See");
    HyperlinkLabel hyperlink = SwingHelper.createWebHyperlink(adapterHomePageUrl);

    JLabel label2 = new JLabel("for details.");

    return SwingHelper.newHorizontalPanel(
      Component.BOTTOM_ALIGNMENT,
      SwingHelper.newLeftAlignedVerticalPanel(label1, Box.createVerticalStrut(2)),
      hyperlink,
      Box.createHorizontalStrut(5),
      SwingHelper.newLeftAlignedVerticalPanel(label2, Box.createVerticalStrut(2))
    );
  }

  @Nullable
  private static VirtualFile findInitialDir(@NotNull final PsiFile psiFileRequestor) {
    if (!psiFileRequestor.isValid()) {
      return null;
    }
    return ApplicationManager.getApplication().runReadAction(new Computable<VirtualFile>() {
      @Override
      @Nullable
      public VirtualFile compute() {
        Project project = psiFileRequestor.getProject();
        VirtualFile virtualFile = psiFileRequestor.getVirtualFile();
        if (virtualFile != null) {
          ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
          VirtualFile contentRoot = fileIndex.getContentRootForFile(virtualFile);
          if (contentRoot != null) {
            return contentRoot;
          }
        }
        return project.getBaseDir();
      }
    });

  }
  @NotNull
  private static JPanel createFilesViewPanel(@NotNull List<VirtualFile> files) {
    JPanel panel = new JPanel(new BorderLayout(0, 2));
    panel.add(new JLabel("Files to add:"), BorderLayout.NORTH);

    final JBList fileList = new JBList(ArrayUtil.EMPTY_STRING_ARRAY);
    fileList.setBorder(BorderFactory.createLineBorder(Color.lightGray));
    fileList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        fileList.clearSelection();
      }
    });
    fileList.setFocusable(false);
    fileList.setRequestFocusEnabled(false);
    fileList.setBackground(Gray._242);
    fileList.setCellRenderer(new ListCellRendererWrapper<VirtualFile>() {
      @Override
      public void customize(JList list, VirtualFile value, int index, boolean selected, boolean hasFocus) {
        setText(" " + value.getName());
      }
    });
    fileList.setListData(files.toArray());
    panel.add(fileList, BorderLayout.CENTER);
    return panel;
  }

  @NotNull
  private JPanel createSelectDirectoryPanel(@NotNull Project project, @NotNull JTextField directoryTextField) {
    FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
    String adapterName = getAssertFrameworkAdapterName();
    String title = "Select a directory for " + adapterName + " files";
    String description = adapterName + " source files will be copied to the selected directory";
    TextFieldWithBrowseButton directoryTextFieldWithBrowseButton = new TextFieldWithBrowseButton(directoryTextField);
    directoryTextFieldWithBrowseButton.addBrowseFolderListener(
      title, description, project, fileChooserDescriptor
    );
    Dimension oldDimension = directoryTextFieldWithBrowseButton.getPreferredSize();
    directoryTextFieldWithBrowseButton.setMaximumSize(oldDimension);
    JPanel panel = new JPanel(new BorderLayout(0, 2));
    panel.add(new JLabel("Copy these files to directory:"), BorderLayout.NORTH);
    panel.add(directoryTextFieldWithBrowseButton, BorderLayout.CENTER);
    return panel;
  }

  private String getAssertFrameworkAdapterName() {
    return myAssertFrameworkName + " JsTestDriver Adapter";
  }

  @Override
  protected JComponent createCenterPanel() {
    return myContent;
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myDirectoryTextField;
  }

  @Override
  @Nullable
  protected ValidationInfo doValidate() {
    String text = myDirectoryTextField.getText();
    File dir = new File(text);
    if (!dir.isDirectory() || !dir.isAbsolute()) {
      return new ValidationInfo("Not a valid directory", myDirectoryTextField);
    }
    return null;
  }

  @Override
  protected void doOKAction() {
    List<VirtualFile> extractedVirtualFiles = extractAdapterFiles();
    if (extractedVirtualFiles != null) {
      installCodeAssistance(extractedVirtualFiles);
    }
    super.doOKAction();
  }

  /**
   * @return extracted file list or null if extraction failed
   */
  @Nullable
  private List<VirtualFile> extractAdapterFiles() {
    return ApplicationManager.getApplication().runWriteAction(new Computable<List<VirtualFile>>() {
      @Override
      @Nullable
      public List<VirtualFile> compute() {
        try {
          VirtualFile extractDir = getOrCreateExtractDirVirtualFile();
          return copyVirtualFilesToDir(extractDir);
        } catch (Exception e) {
          LOG.warn("Extraction of " + getAssertFrameworkAdapterName() + " files failed", e);
          return null;
        }
      }
    });
  }

  @NotNull
  private VirtualFile getOrCreateExtractDirVirtualFile() {
    File extractDir = new File(myDirectoryTextField.getText());
    VirtualFile vFile = LocalFileSystem.getInstance().findFileByIoFile(extractDir);
    if (vFile == null || !vFile.isValid()) {
      vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(extractDir);
      if (vFile == null || !vFile.isValid()) {
        throw new RuntimeException("Can't find valid VirtualFile for " + extractDir.getAbsolutePath());
      }
    }
    return vFile;
  }

  @Nullable
  private List<VirtualFile> copyVirtualFilesToDir(@NotNull VirtualFile targetDir) {
    List<VirtualFile> copiedFiles = Lists.newArrayList();
    for (VirtualFile virtualFile : myAdapterSourceFiles) {
      try {
        //noinspection NullableProblems
        copiedFiles.add(VfsUtilCore.copyFile(null, virtualFile, targetDir));
      } catch (IOException e) {
        Messages.showErrorDialog("Extract operation failed!\nUnable to copy " + virtualFile.getPath() + " to " + targetDir.getPath(),
                                 "Adding " + getAssertFrameworkAdapterName());
        return null;
      }
    }
    return copiedFiles;
  }

  public void installCodeAssistance(final List<VirtualFile> extractedAdapterSourceFiles) {
    boolean createLibrary = ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
      @Override
      public Boolean compute() {
        File extractDir = new File(myDirectoryTextField.getText());
        return !ProjectRootUtils.isInsideContentRoots(myProject, extractDir);
      }
    });
    if (createLibrary) {
      ChooseScopeAndCreateLibraryDialog dialog = new ChooseScopeAndCreateLibraryDialog(
        myProject,
        getAssertFrameworkAdapterName(),
        extractedAdapterSourceFiles,
        new ScriptingFrameworkDescriptor(getAssertFrameworkAdapterName(), null),
        myFileRequestor,
        true
      );
      dialog.show();
    }
  }

}
