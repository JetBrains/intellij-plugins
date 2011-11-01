package org.osmorc;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotifications;
import com.intellij.util.Alarm;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.manifest.ManifestFileTypeFactory;
import org.osmorc.manifest.lang.psi.ManifestFile;
import org.osmorc.settings.ProjectSettings;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A project component that watches changes to a manifest file and displays a notification bar offering to resynchronize dependencies.
 */
public class ManifestChangeWatcher implements EditorNotifications.Provider<ManifestChangeNotificationPanel> {

  private static final Key<ManifestChangeNotificationPanel> KEY = Key.create("ManifestChangeNotificationPanelKey");
  private Project myProject;
  private MessageBus myMessageBus;
  private AtomicBoolean myNeedsResync = new AtomicBoolean(false);

  public ManifestChangeWatcher(@NotNull Project project, @NotNull MessageBus messageBus) {
    myProject = project;
    myMessageBus = messageBus;
    final Alarm myAlarm = new Alarm(Alarm.ThreadToUse.OWN_THREAD, myProject);

    // Subscribe to VFS-change events.
    myMessageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
      @Override
      public void after(List<? extends VFileEvent> events) {
        final ProjectSettings settings = ProjectSettings.getInstance(myProject);
        if ( settings.getManifestSynchronizationType() == ProjectSettings.ManifestSynchronizationType.DoNotSynchronize) {
          // don't synchronize
          return;
        }
        for (VFileEvent event : events) { // get changed files
          VirtualFile virtualFile = event.getFile();

          if (virtualFile != null && virtualFile.isValid() && virtualFile.getFileType() == ManifestFileTypeFactory.MANIFEST) {
            // find out if the file belongs to an osmorc facet
            final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(virtualFile);
            if (psiFile instanceof ManifestFile) {
              OsmorcFacet of = OsmorcFacet.getInstance(psiFile);
              // check if the facet is manually edited and if the file is the manifest file for the facet.
              if (of != null && of.getConfiguration().isManifestManuallyEdited() && of.isManifestForThisFacet(virtualFile)) {
                // cancel all pending requests
                myAlarm.cancelAllRequests();
                // add notification bar by setting the needs resync to true and updating the notifications.
                myAlarm.addRequest(new Runnable() {
                  @Override
                  public void run() {
                    if ( settings.getManifestSynchronizationType() == ProjectSettings.ManifestSynchronizationType.ManuallySynchronize ) {
                      myNeedsResync.set(true);
                      EditorNotifications.getInstance(myProject).updateAllNotifications();
                    }
                    if ( settings.getManifestSynchronizationType() == ProjectSettings.ManifestSynchronizationType.AutomaticallySynchronize ) {
                      myNeedsResync.set(false);
                      EditorNotifications.getInstance(myProject).updateAllNotifications();
                     resynchronizeDependencies(psiFile);
                    }
                  }
                }, 250);
              }
            }
          }
        }
      }

      @Override
      public void before(List<? extends VFileEvent> events) {
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

  /**
   * Triggers a resynchronization of all dependencies of all bundles with manually edited manifests.
   * @param modifiedFile
   */
  static void resynchronizeDependencies(final PsiFile modifiedFile) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        new Task.Backgroundable(modifiedFile.getProject(), "Processing manifest change", false) {
          @Override
          public void run(@NotNull ProgressIndicator indicator) {
            indicator.setText("Synchronizing dependencies.");
            indicator.setIndeterminate(true);
            // sync the dependencies of ALL modules
            ModuleDependencySynchronizer.resynchronizeAll(modifiedFile.getProject());
          }
        }.queue();
      }
    });
  }
}
