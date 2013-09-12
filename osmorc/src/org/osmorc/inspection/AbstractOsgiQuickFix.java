package org.osmorc.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.osmorc.BundleManager;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.manifest.BundleManifest;

public abstract class AbstractOsgiQuickFix implements LocalQuickFix {
  @NotNull
  @Override
  public final String getFamilyName() {
    return OsmorcBundle.message("inspection.group");
  }

  @Nullable
  protected ManifestFile getVerifiedManifestFile(@NotNull PsiElement element) {
    Module module = ModuleUtilCore.findModuleForPsiElement(element);
    assert module != null : element;
    BundleManager bundleManager = ServiceManager.getService(element.getProject(), BundleManager.class);

    BundleManifest manifest = bundleManager.getManifestByObject(module);
    if (manifest == null) {
      String message = OsmorcBundle.message("inspection.fix.no.manifest");
      Notifications.Bus.notify(new Notification("osmorc", getFamilyName(), message, NotificationType.WARNING));
      return null;
    }

    ManifestFile manifestFile = manifest.getManifestFile();
    if (!CommonRefactoringUtil.checkReadOnlyStatus(manifestFile)) {
      return null;
    }

    return manifestFile;
  }
}
