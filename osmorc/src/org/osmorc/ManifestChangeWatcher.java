package org.osmorc;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.manifest.lang.psi.ManifestFile;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A project component that watches changes to a manifest file and displays a notification bar offering to resynchronize dependencies.
 */
public class ManifestChangeWatcher implements EditorNotifications.Provider<ManifestChangeNotificationPanel> {

  private static final Key<ManifestChangeNotificationPanel> KEY = Key.create("ManifestChangeNotificationPanelKey");
  private Project myProject;
  private AtomicBoolean myNeedsResync = new AtomicBoolean(false);

  public ManifestChangeWatcher(@NotNull Project project) {
    myProject = project;

    PsiManager.getInstance(myProject).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {
      public void childrenChanged(PsiTreeChangeEvent event) {
        final PsiFile file = event.getFile();
        if (file instanceof ManifestFile) {
          OsmorcFacet of = OsmorcFacet.getInstance(file);
          if (of != null && of.getConfiguration().isManifestManuallyEdited() && !myNeedsResync.get()) {
            myNeedsResync.set(true);
            EditorNotifications.getInstance(myProject).updateAllNotifications();
          }
        }
      }
    });
  }

  @Override
  public Key<ManifestChangeNotificationPanel> getKey() {
    return KEY;
  }


  @Override
  public ManifestChangeNotificationPanel createNotificationPanel(VirtualFile file) {
    if (!myNeedsResync.get()) {
      return null;
    }

    final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
    if (!(psiFile instanceof ManifestFile)) {
      return null;
    }

    OsmorcFacet facet = OsmorcFacet.getInstance(psiFile);
    if (facet == null) { // not under osmorc control, ignore
      return null;
    }

    if (!facet.isManifestForThisFacet(file)) {
      // this is not the manifest of this module, therefore it would not affect synchronization. ignore.
      return null;
    }

    return new ManifestChangeNotificationPanel(psiFile, myNeedsResync);
  }
}
