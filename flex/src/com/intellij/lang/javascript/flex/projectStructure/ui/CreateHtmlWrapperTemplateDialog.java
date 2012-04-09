package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class CreateHtmlWrapperTemplateDialog extends DialogWrapper {

  public static final String HTML_TEMPLATE_FOLDER_NAME = "html-template";
  public static final String TITLE = FlexBundle.message("create.html.wrapper.template.title");
  public static final String PLAYER_PRODUCT_INSTALL_SWF = "playerProductInstall.swf";
  public static final String USE_BROWSER_HISTORY_MACRO = "${useBrowserHistory}";
  public static final String VERSION_MAJOR_MACRO = "${version_major}";
  public static final String VERSION_MINOR_MACRO = "${version_minor}";
  public static final String VERSION_REVISION_MACRO = "${version_revision}";
  public static final String HTML_WRAPPER_TEMPLATE_FILE_NAME = "index.template.html";

  private JPanel myMainPanel;
  private LabeledComponent<TextFieldWithBrowseButton> myWrapperFolderComponent;
  private JCheckBox myEnableHistoryCheckBox;
  private JCheckBox myCheckPlayerVersionCheckBox;
  private JCheckBox myExpressInstallCheckBox;

  private final Module myModule;
  private final @NotNull Sdk mySdk;
  private final String myOutputPath;

  public CreateHtmlWrapperTemplateDialog(final Module module,
                                         final @NotNull Sdk sdk,
                                         final String outputPath,
                                         final @Nullable String initialPath) {
    super(module.getProject());

    myModule = module;
    mySdk = sdk;
    myOutputPath = outputPath;

    setInitialPath(module, initialPath);

    myCheckPlayerVersionCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        myExpressInstallCheckBox.setEnabled(myCheckPlayerVersionCheckBox.isSelected());
      }
    });

    myWrapperFolderComponent.getComponent()
      .addBrowseFolderListener(null, null, module.getProject(), FileChooserDescriptorFactory.createSingleFolderDescriptor());

    setTitle(TITLE);
    init();
  }

  private void setInitialPath(final Module module, final String initialPath) {
    if (initialPath != null) {
      myWrapperFolderComponent.getComponent().setText(FileUtil.toSystemDependentName(initialPath));
    }
    else {
      final String[] contentRootUrls = ModuleRootManager.getInstance(module).getContentRootUrls();
      final String path = contentRootUrls.length > 0
                          ? FileUtil.toSystemDependentName(VfsUtil.urlToPath(contentRootUrls[0]) + "/" + HTML_TEMPLATE_FOLDER_NAME)
                          : FileUtil.toSystemDependentName(PathUtil.getParentPath(module.getModuleFilePath())
                                                           + "/" + HTML_TEMPLATE_FOLDER_NAME);
      myWrapperFolderComponent.getComponent().setText(path);
    }
  }

  public JComponent getPreferredFocusedComponent() {
    return myWrapperFolderComponent.getComponent().getTextField();
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  public String getWrapperFolderPath() {
    return FileUtil.toSystemIndependentName(myWrapperFolderComponent.getComponent().getText().trim());
  }

  protected ValidationInfo doValidate() {
    final String wrapperFolderPath = getWrapperFolderPath();

    for (String url : ModuleRootManager.getInstance(myModule).getContentRootUrls()) {
      final String path = VfsUtil.urlToPath(url);
      if (FileUtil.isAncestor(wrapperFolderPath, path, false)) {
        return new ValidationInfo(
          FlexBundle.message("html.wrapper.folder.clash.for.dialog", "module content root", FileUtil.toSystemDependentName(path)),
          myWrapperFolderComponent.getComponent());
      }
    }

    for (String url : ModuleRootManager.getInstance(myModule).getSourceRootUrls()) {
      final String path = VfsUtil.urlToPath(url);
      if (FileUtil.isAncestor(wrapperFolderPath, path, false)) {
        return new ValidationInfo(
          FlexBundle.message("html.wrapper.folder.clash.for.dialog", "source folder", FileUtil.toSystemDependentName(path)),
          myWrapperFolderComponent.getComponent());
      }
    }

    if (!myOutputPath.isEmpty() && FileUtil.isAncestor(wrapperFolderPath, myOutputPath, false)) {
      return new ValidationInfo(
        FlexBundle.message("html.wrapper.folder.clash.for.dialog", "output folder", FileUtil.toSystemDependentName(myOutputPath)),
        myWrapperFolderComponent.getComponent());
    }

    return null;
  }

  protected void doOKAction() {
    if (createHtmlWrapperTemplate(myModule.getProject(), mySdk, getWrapperFolderPath(),
                                  myEnableHistoryCheckBox.isSelected(), myCheckPlayerVersionCheckBox.isSelected(),
                                  myExpressInstallCheckBox.isEnabled() && myExpressInstallCheckBox.isSelected())) {
      super.doOKAction();
    }
  }

  public static boolean createHtmlWrapperTemplate(final Project project,
                                                  final Sdk sdk,
                                                  final String templateFolderPath,
                                                  final boolean enableHistory,
                                                  final boolean checkPlayerVersion,
                                                  final boolean expressInstall) {
    final VirtualFile folder = FlexUtils.createDirIfMissing(project, true, templateFolderPath, TITLE);
    return folder != null &&
           checkIfEmpty(project, folder) &&
           doCreateWrapper(project, sdk, folder, enableHistory, checkPlayerVersion, expressInstall);
  }

  private static boolean checkIfEmpty(final Project project, final VirtualFile folder) {
    final VirtualFile[] children = folder.getChildren();
    if (children.length > 0) {
      final String[] options = {
        FlexBundle.message("folder.not.empty.clear.option"),
        FlexBundle.message("folder.not.empty.keep.option"),
        FlexBundle.message("folder.not.empty.cancel.option")};
      final int choice = Messages.showDialog(project, FlexBundle.message("folder.not.empty.clear.or.overwrite"),
                                             TITLE, options, 0, Messages.getWarningIcon());
      switch (choice) {
        case 0:
          // noinspection ThrowableResultOfMethodCallIgnored
          final IOException exception = ApplicationManager.getApplication().runWriteAction(new NullableComputable<IOException>() {
            public IOException compute() {
              try {
                for (VirtualFile child : children) {
                  child.delete(this);
                }
              }
              catch (IOException e) {
                return e;
              }
              return null;
            }
          });

          if (exception != null) {
            Messages.showErrorDialog(project, FlexBundle.message("failed.to.delete", exception.getMessage()), TITLE);
            return false;
          }
          return true;
        case 1:
          return true;
        case 2:
          return false;
      }
    }
    return true;
  }

  private static boolean doCreateWrapper(final Project project,
                                         final Sdk sdk,
                                         final VirtualFile folder,
                                         final boolean enableHistory,
                                         final boolean checkPlayerVersion,
                                         final boolean expressInstall) {
    final String wrapperName;
    if (StringUtil.compareVersionNumbers(sdk.getVersionString(), "4") < 0) {
      final String prefix = checkPlayerVersion
                            ? expressInstall ? "express-installation"
                                             : "client-side-detection"
                            : "no-player-detection";
      wrapperName = prefix + (enableHistory ? "-with-history" : "");
    }
    else {
      wrapperName = "swfobject";
    }

    final String sdkTemplatePath = sdk.getHomePath() + "/templates/" + wrapperName;
    final VirtualFile sdkTemplateFolder = LocalFileSystem.getInstance().findFileByPath(sdkTemplatePath);
    if (sdkTemplateFolder == null || !sdkTemplateFolder.isDirectory()) {
      Messages.showErrorDialog(project, FlexBundle.message("html.wrapper.in.sdk.not.found", sdkTemplatePath), TITLE);
      return false;
    }

    final boolean swfObjectWrapper = "swfobject".equals(sdkTemplateFolder.getName());

    // noinspection ThrowableResultOfMethodCallIgnored
    final IOException exception = ApplicationManager.getApplication().runWriteAction(new NullableComputable<IOException>() {
      public IOException compute() {
        try {
          for (VirtualFile file : sdkTemplateFolder.getChildren()) {
            if (swfObjectWrapper) {
              if (HTML_WRAPPER_TEMPLATE_FILE_NAME.equals(file.getName())) {
                fixAndCopyIndexTemplateHtml(file, folder, enableHistory, checkPlayerVersion, expressInstall);
                continue;
              }
              else if ("history".equals(file.getName())) {
                if (!enableHistory) {
                  continue;
                }
              }
              else if (PLAYER_PRODUCT_INSTALL_SWF.equals(file.getName())) {
                if (!checkPlayerVersion || !expressInstall) {
                  continue;
                }
              }
              file.copy(this, folder, file.getName());
            }
            else {
              file.copy(this, folder, file.getName());
            }
          }
        }
        catch (IOException e) {
          return e;
        }

        return null;
      }
    });

    if (exception != null) {
      Messages.showErrorDialog(project, FlexBundle.message("html.wrapper.creation.failed", exception.getMessage()), TITLE);
      return false;
    }

    return true;
  }

  /**
   * For "swfobject" template only!
   */
  private static void fixAndCopyIndexTemplateHtml(final VirtualFile file,
                                                  final VirtualFile folder,
                                                  final boolean enableHistory,
                                                  final boolean checkPlayerVersion,
                                                  final boolean expressInstall) throws IOException {
    final String text = VfsUtil.loadText(file);
    final String useBrowserHistory = enableHistory ? "--" : USE_BROWSER_HISTORY_MACRO;
    final String major = checkPlayerVersion ? VERSION_MAJOR_MACRO : "0";
    final String minor = checkPlayerVersion ? VERSION_MINOR_MACRO : "0";
    final String revision = checkPlayerVersion ? VERSION_REVISION_MACRO : "0";
    final String expressInstallSwf = checkPlayerVersion && expressInstall ? PLAYER_PRODUCT_INSTALL_SWF : "";

    final String fixedText = StringUtil.replace(text,
                                                new String[]{USE_BROWSER_HISTORY_MACRO, VERSION_MAJOR_MACRO, VERSION_MINOR_MACRO,
                                                  VERSION_REVISION_MACRO, "${expressInstallSwf}"},
                                                new String[]{useBrowserHistory, major, minor, revision, expressInstallSwf});

    FlexUtils.addFileWithContent(file.getName(), fixedText, folder);
  }

  protected String getHelpId() {
    return "flex.CreateHtmlWrapperTemplateDialog";
  }
}
