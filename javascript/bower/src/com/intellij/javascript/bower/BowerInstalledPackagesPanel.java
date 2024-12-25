package com.intellij.javascript.bower;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ObjectUtils;
import com.intellij.webcore.packaging.InstalledPackagesPanel;
import com.intellij.webcore.packaging.ManagePackagesDialog;
import com.intellij.webcore.packaging.PackageManagementService;
import com.intellij.webcore.packaging.PackagesNotificationPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class BowerInstalledPackagesPanel extends InstalledPackagesPanel {

  public BowerInstalledPackagesPanel(@NotNull Project project,
                                     @NotNull PackagesNotificationPanel area) {
    super(project, area);
    myPackagesTable.setShowGrid(false);
  }

  @Override
  public void updatePackages(@Nullable PackageManagementService packageManagementService) {
    BowerPackagingService bowerService = ObjectUtils.tryCast(packageManagementService, BowerPackagingService.class);
    if (bowerService != null) {
      saveBowerConfig(bowerService);
    }
    myInstallEnabled = bowerService != null;
    super.updatePackages(packageManagementService);
  }

  private static void saveBowerConfig(final @NotNull BowerPackagingService bowerService) {
    ApplicationManager.getApplication().runWriteAction(() -> {
      File file;
      file = new File(bowerService.getSettings().getBowerJsonPath());
      if (file.isFile()) {
        VirtualFile vFile = VfsUtil.findFileByIoFile(file, false);
        if (vFile != null && vFile.isValid()) {
          FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
          Document document = fileDocumentManager.getDocument(vFile);
          if (document != null) {
            fileDocumentManager.saveDocument(document);
          }
        }
      }
    });
  }

  @Override
  protected @NotNull ManagePackagesDialog createManagePackagesDialog() {
    ManagePackagesDialog dialog = super.createManagePackagesDialog();
    dialog.setOptionsText("--save");
    return dialog;
  }
}
