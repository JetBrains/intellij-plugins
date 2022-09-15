// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
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
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;

public class CreateHtmlWrapperTemplateDialog extends DialogWrapper {

  public static final String HTML_TEMPLATE_FOLDER_NAME = "html-template";
  private static final String PLAYER_PRODUCT_INSTALL_SWF = "playerProductInstall.swf";
  private static final String USE_BROWSER_HISTORY_MACRO = "${useBrowserHistory}";
  private static final String EXPRESS_INSTALL_SWF_MACRO = "${expressInstallSwf}";

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
      @Override
      public void actionPerformed(final ActionEvent e) {
        myExpressInstallCheckBox.setEnabled(myCheckPlayerVersionCheckBox.isSelected());
      }
    });

    myWrapperFolderComponent.getComponent()
      .addBrowseFolderListener(null, null, module.getProject(), FileChooserDescriptorFactory.createSingleFolderDescriptor());

    setTitle(getTitleText());
    init();
  }

  private void setInitialPath(final Module module, final String initialPath) {
    if (initialPath != null) {
      myWrapperFolderComponent.getComponent().setText(FileUtil.toSystemDependentName(initialPath));
    }
    else {
      final String[] contentRootUrls = ModuleRootManager.getInstance(module).getContentRootUrls();
      final String path = contentRootUrls.length > 0
                          ? FileUtil.toSystemDependentName(VfsUtilCore.urlToPath(contentRootUrls[0]) + "/" + HTML_TEMPLATE_FOLDER_NAME)
                          : FileUtil.toSystemDependentName(PathUtil.getParentPath(module.getModuleFilePath())
                                                           + "/" + HTML_TEMPLATE_FOLDER_NAME);
      myWrapperFolderComponent.getComponent().setText(path);
    }
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myWrapperFolderComponent.getComponent().getTextField();
  }

  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  public String getWrapperFolderPath() {
    return FileUtil.toSystemIndependentName(myWrapperFolderComponent.getComponent().getText().trim());
  }

  @Override
  protected ValidationInfo doValidate() {
    final String wrapperFolderPath = getWrapperFolderPath();

    for (String url : ModuleRootManager.getInstance(myModule).getContentRootUrls()) {
      final String path = VfsUtilCore.urlToPath(url);
      if (FileUtil.isAncestor(wrapperFolderPath, path, false)) {
        return new ValidationInfo(
          FlexBundle.message("html.wrapper.folder.clash.for.dialog", "module content root", FileUtil.toSystemDependentName(path)),
          myWrapperFolderComponent.getComponent());
      }
    }

    for (String url : ModuleRootManager.getInstance(myModule).getSourceRootUrls()) {
      final String path = VfsUtilCore.urlToPath(url);
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

  @Override
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
    final VirtualFile folder = FlexUtils.createDirIfMissing(project, true, templateFolderPath, getTitleText());
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
                                             getTitleText(), options, 0, Messages.getWarningIcon());
      switch (choice) {
        case 0 -> {
          final IOException exception = ApplicationManager.getApplication().runWriteAction(new NullableComputable<>() {
            @Override
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
            Messages.showErrorDialog(project, FlexBundle.message("failed.to.delete", exception.getMessage()), getTitleText());
            return false;
          }
          return true;
        }
        case 1 -> {
          return true;
        }
        case 2 -> {
          return false;
        }
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
    if (!FlexSdkUtils.isAirSdkWithoutFlex(sdk) && StringUtil.compareVersionNumbers(sdk.getVersionString(), "4") < 0) {
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
      Messages.showErrorDialog(project, FlexBundle.message("html.wrapper.in.sdk.not.found", sdkTemplatePath), getTitleText());
      return false;
    }

    final boolean swfObjectWrapper = "swfobject".equals(sdkTemplateFolder.getName());

    final IOException exception = ApplicationManager.getApplication().runWriteAction(new NullableComputable<>() {
      @Override
      public IOException compute() {
        try {
          for (VirtualFile file : sdkTemplateFolder.getChildren()) {
            if (swfObjectWrapper) {
              if (FlexCommonUtils.HTML_WRAPPER_TEMPLATE_FILE_NAME.equals(file.getName())) {
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
            }
            file.copy(this, folder, file.getName());
          }
        }
        catch (IOException e) {
          return e;
        }

        return null;
      }
    });

    if (exception != null) {
      Messages.showErrorDialog(project, FlexBundle.message("html.wrapper.creation.failed", exception.getMessage()), getTitleText());
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
    final String text = VfsUtilCore.loadText(file);
    final String useBrowserHistory = enableHistory ? "--" : USE_BROWSER_HISTORY_MACRO;
    final String major = checkPlayerVersion ? FlexCommonUtils.VERSION_MAJOR_MACRO : "0";
    final String minor = checkPlayerVersion ? FlexCommonUtils.VERSION_MINOR_MACRO : "0";
    final String revision = checkPlayerVersion ? FlexCommonUtils.VERSION_REVISION_MACRO : "0";
    final String expressInstallSwf = checkPlayerVersion && expressInstall ? PLAYER_PRODUCT_INSTALL_SWF : "";

    final String fixedText = StringUtil.replace(text,
                                                Arrays.asList(
                                                  USE_BROWSER_HISTORY_MACRO,
                                                  FlexCommonUtils.VERSION_MAJOR_MACRO,
                                                  FlexCommonUtils.VERSION_MINOR_MACRO,
                                                  FlexCommonUtils.VERSION_REVISION_MACRO,
                                                  EXPRESS_INSTALL_SWF_MACRO),
                                                Arrays.asList(useBrowserHistory, major, minor, revision, expressInstallSwf));

    FlexUtils.addFileWithContent(file.getName(), fixedText, folder);
  }

  @Override
  protected String getHelpId() {
    return "flex.CreateHtmlWrapperTemplateDialog";
  }

  public static String getTitleText() {
    return FlexBundle.message("create.html.wrapper.template.title");
  }
}
